package com.lsb.webshop.controller.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
    public String showProductDetail(Model model, @PathVariable long id) {
        Product pr = this.productService.getByIdProduct(id).get();
        model.addAttribute("product", pr);
        model.addAttribute("id", id);
        model.addAttribute("factories", this.productService.getAllFactories());
        model.addAttribute("products", this.productService.getAllProducts());
        return "client/product/detail";
    }

    @PostMapping("/add-product-to-cart/{id}")
    public String addProductToCart(@PathVariable long id, HttpServletRequest request){
        HttpSession session = request.getSession(false);

        long productId = id;
        String email = (String) session.getAttribute("email");

        this.productService.addProductToCart(email, productId, session);

        return "redirect:/";
    }

    @GetMapping("/cart")
    public String getCartPage(Model model, HttpServletRequest request){
    HttpSession session = request.getSession(false);
    if (session == null || session.getAttribute("id") == null) {
        return "redirect:/login"; 
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

    model.addAttribute("cartDetails", cartDetails);
    model.addAttribute("totalPrice", totalPrice);
    model.addAttribute("cart", cart);

    return "client/cart/show";
    }

    @PostMapping(value = "/cart/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateQuantity(
        @RequestParam("productId") Long productId,
        @RequestParam("quantity") int quantity,
        HttpServletRequest request) {

    HttpSession session = request.getSession(false);
    if (session == null || session.getAttribute("email") == null) {
        Map<String, Object> error = new HashMap<>();
        error.put("message", "Bạn cần đăng nhập để cập nhật giỏ hàng");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    String email = (String) session.getAttribute("email");

    Map<String, Object> result = cartService.updateQuantity(email, productId, quantity);

    if (result.get("message").toString().startsWith("Cập nhật thành công")) {
        return ResponseEntity.ok(result);
    } else {
        return ResponseEntity.badRequest().body(result);
    }
    }

}