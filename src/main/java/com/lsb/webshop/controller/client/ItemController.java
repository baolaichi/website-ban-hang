package com.lsb.webshop.controller.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.lsb.webshop.domain.Cart;
import com.lsb.webshop.domain.CartDetail;
import com.lsb.webshop.domain.Product;
import com.lsb.webshop.domain.User;
import com.lsb.webshop.service.CartService;
import com.lsb.webshop.service.ProductService;
import com.lsb.webshop.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class ItemController {

    private  ProductService productService;
    private UserService userService;
    private CartService cartService;

    public ItemController(ProductService productService, UserService userService, CartService cartService) {
        this.productService = productService;
        this.userService = userService;
        this.cartService = cartService;
    }

    @GetMapping("/product/{id}")
    public ModelAndView showProductDetail(@PathVariable long id) {
    ModelAndView mav = new ModelAndView("client/product/detail");

    // Lấy sản phẩm theo id (nên kiểm tra tồn tại)
    Optional<Product> productOptional = this.productService.getByIdProduct(id);
    if (productOptional.isPresent()) {
        Product pr = productOptional.get();
        mav.addObject("product", pr);
        mav.addObject("id", id);
        mav.addObject("factories", this.productService.getAllFactories());
        mav.addObject("products", this.productService.getAllProducts());
    } else {
        // Nếu không tìm thấy sản phẩm, có thể chuyển hướng về trang lỗi hoặc danh sách sản phẩm
        mav.setViewName("redirect:/products"); // hoặc "error/404"
    }

    return mav;
    }


    @PostMapping("/add-product-to-cart/{id}")
    public ModelAndView addProductToCart(@PathVariable long id, HttpServletRequest request) {
    ModelAndView mav = new ModelAndView();

    HttpSession session = request.getSession(false);

    if (session != null && session.getAttribute("email") != null) {
        String email = (String) session.getAttribute("email");
        this.productService.addProductToCart(email, id, session);
        mav.setViewName("redirect:/");
    } else {
        // Nếu người dùng chưa đăng nhập hoặc session hết hạn
        mav.setViewName("redirect:/login");
    }

    return mav;
    }

    @GetMapping("/cart")
    public ModelAndView getCartPage(HttpServletRequest request) {
    ModelAndView mav = new ModelAndView();

    HttpSession session = request.getSession(false);
    if (session == null || session.getAttribute("id") == null) {
        mav.setViewName("redirect:/login");
        return mav;
    }

    long id = (long) session.getAttribute("id");
    User currentUser = new User();
    currentUser.setId(id);

    Cart cart = productService.fetchByUser(currentUser);
    List<CartDetail> cartDetails = new ArrayList<>();
    if (cart != null && cart.getCartDetails() != null) {
        cartDetails = cart.getCartDetails();
    }

    double totalPrice = 0;
    for (CartDetail detail : cartDetails) {
        totalPrice += detail.getPrice() * detail.getQuantity();
    }

    mav.setViewName("client/cart/show");
    mav.addObject("cartDetails", cartDetails);
    mav.addObject("totalPrice", totalPrice);
    mav.addObject("cart", cart);

    return mav;
    }


    @PostMapping(value = "/cart/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateQuantity(
        @RequestParam("productId") Long productId,
        @RequestParam("quantity") int quantity,
        HttpServletRequest request) {
    Map<String, Object> response = cartService.handleUpdateQuantity(request, productId, quantity);
    HttpStatus status = (HttpStatus) response.getOrDefault("status", HttpStatus.OK);
    response.remove("status");
    return ResponseEntity.status(status).body(response);
    }


    @GetMapping("/checkout")
    public ModelAndView getCheckoutPage(HttpServletRequest request) {
    ModelAndView mav = new ModelAndView("client/cart/checkout");

    User currentUser = new User();
    HttpSession session = request.getSession(false);
    
    if (session != null && session.getAttribute("id") != null) {
        long id = (long) session.getAttribute("id");
        currentUser.setId(id);

        Cart cart = this.productService.fetchByUser(currentUser);
        List<CartDetail> cartDetails = cart == null ? new ArrayList<>() : cart.getCartDetails();

        double totalPrice = 0;
        for (CartDetail detail : cartDetails) {
            totalPrice += detail.getPrice() * detail.getQuantity();
        }

        mav.addObject("cartDetails", cartDetails);
        mav.addObject("totalPrice", totalPrice);
    } else {
        // Nếu session không tồn tại hoặc chưa đăng nhập, chuyển hướng về trang login
        mav.setViewName("redirect:/login");
    }

    return mav;
    }

    @PostMapping("/confirm-checkout")
    public ModelAndView getCheckOutPage(@ModelAttribute("cart") Cart cart) {
    List<CartDetail> cartDetails = cart == null ? new ArrayList<>() : cart.getCartDetails();
    this.productService.updateCartBeforeCheckout(cartDetails);

    ModelAndView mav = new ModelAndView("redirect:/checkout");
    return mav;
    }

    @PostMapping("/place-order")
    public ModelAndView handlePlaceOrder(
            HttpServletRequest request,
            @RequestParam("receiverName") String receiverName,
            @RequestParam("receiverAddress") String receiverAddress,
            @RequestParam("receiverPhone") String receiverPhone) {

        ModelAndView mav = new ModelAndView();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("id") == null) {
            mav.setViewName("redirect:/login");
            return mav;
        }

        long id = (long) session.getAttribute("id");
        User currentUser = new User();
        currentUser.setId(id);

        // Lấy cart từ user rồi tính tổng tiền ở server
        Cart cart = productService.fetchByUser(currentUser);
        double totalPrice = 0;
        if (cart != null && cart.getCartDetails() != null) {
            for (CartDetail detail : cart.getCartDetails()) {
                totalPrice += detail.getPrice() * detail.getQuantity();
            }
        }

        this.productService.handlePlaceOrder(currentUser, session, receiverName, receiverAddress, receiverPhone, totalPrice);
        mav.setViewName("redirect:/thanks");
        return mav;
    }


    @GetMapping("/thanks")
    public ModelAndView getThankYouPage() {
    return new ModelAndView("client/cart/thanks");
    }


    @PostMapping("/delete-product-from-cart/{id}")
    public ModelAndView deleteProductFromCart(@PathVariable long id, HttpServletRequest request) {
    ModelAndView mav = new ModelAndView("redirect:/cart");

    HttpSession session = request.getSession(false);
    if (session != null) {
        this.productService.removeProductCart(id, session);
    } else {
        mav.setViewName("redirect:/login");
    }

    return mav;
    }

}