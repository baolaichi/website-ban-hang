package com.lsb.webshop.controller.client;

import com.lsb.webshop.domain.*;
import com.lsb.webshop.repository.CartDetailRepository;
import com.lsb.webshop.service.CartService;
import com.lsb.webshop.service.OrderService;
import com.lsb.webshop.service.PaymentService;
import com.lsb.webshop.service.ProductService;
import com.lsb.webshop.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType; // <-- Import cần thiết
import org.springframework.http.ResponseEntity; // <-- Import cần thiết
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.HashMap; // <-- Import cần thiết
import java.util.List;
import java.util.Map;     // <-- Import cần thiết
import java.util.Optional;

@Controller
public class ItemController {

    private final ProductService productService;
    private final UserService userService;
    private final CartService cartService;
    private final CartDetailRepository cartDetailRepository;
    private final OrderService orderService;
    private final PaymentService paymentService;

    public ItemController(ProductService productService, UserService userService,
                          CartService cartService, CartDetailRepository cartDetailRepository,
                          OrderService orderService, PaymentService paymentService) {
        this.productService = productService;
        this.userService = userService;
        this.cartService = cartService;
        this.cartDetailRepository = cartDetailRepository;
        this.orderService = orderService;
        this.paymentService = paymentService;
    }

    // ===== 1. TRANG CHI TIẾT SẢN PHẨM =====
    @GetMapping("/product/{id}")
    public ModelAndView showProductDetail(@PathVariable long id) {
        ModelAndView mav = new ModelAndView("client/product/detail");

        Optional<Product> productOptional = this.productService.getByIdProduct(id);
        if (productOptional.isPresent()) {
            Product pr = productOptional.get();
            mav.addObject("product", pr);
            mav.addObject("id", id);
            mav.addObject("factories", this.productService.getAllFactories());
            mav.addObject("products", this.productService.getAllProducts());

            List<Product> similarProducts = productService.getSimilarProducts(pr);
            mav.addObject("similarProducts", similarProducts);
        } else {
            mav.setViewName("redirect:/products");
        }
        return mav;
    }

    // ===== 2. THÊM VÀO GIỎ HÀNG (AJAX API) =====
    // Hàm này trả về JSON để JS cập nhật giao diện mà không reload trang
    @PostMapping("/add-product-to-cart/{id}")
    public String addProductToCart(@PathVariable long id, HttpServletRequest request) {
        // Gọi service để thêm sản phẩm
        boolean success = cartService.addProductToCart(request, id);

        if (success) {
            // Lấy trang người dùng vừa đứng (Referer) để redirect quay lại đó
            String referer = request.getHeader("Referer");
            return "redirect:" + (referer != null ? referer : "/");
        } else {
            // Nếu chưa đăng nhập hoặc lỗi
            return "redirect:/login";
        }
    }

    // ===== 3. TRANG GIỎ HÀNG =====
    @GetMapping("/cart")
    public ModelAndView getCartPage(HttpServletRequest request) {
        ModelAndView mav = new ModelAndView();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("id") == null) {
            mav.setViewName("redirect:/login");
            return mav;
        }

        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            mav.setViewName("redirect:/login");
            return mav;
        }

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

    // ===== 4. CẬP NHẬT SỐ LƯỢNG GIỎ HÀNG (AJAX API) =====
    // Hàm này xử lý khi bấm nút +/- trong trang giỏ hàng
    @PostMapping(value = "/cart/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateQuantity(
            @RequestParam("productId") Long productId,
            @RequestParam("quantity") int quantity,
            HttpServletRequest request) {

        Map<String, Object> response = cartService.handleUpdateQuantity(request, productId, quantity);

        HttpStatus status = (HttpStatus) response.getOrDefault("status", HttpStatus.OK);
        response.remove("status"); // Xóa status code khỏi body JSON

        return ResponseEntity.status(status).body(response);
    }

    // ===== 5. TRANG THANH TOÁN (CHECKOUT) =====
    @GetMapping("/checkout")
    public ModelAndView getCheckoutPage(HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("client/cart/checkout");

        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            mav.setViewName("redirect:/login");
            return mav;
        }

        Cart cart = this.productService.fetchByUser(currentUser);
        List<CartDetail> cartDetails = (cart == null || cart.getCartDetails() == null)
                ? new ArrayList<>()
                : cart.getCartDetails();

        if (cartDetails.isEmpty()) {
            mav.setViewName("redirect:/cart"); // Không cho checkout giỏ hàng rỗng
            return mav;
        }

        double totalPrice = 0;
        for (CartDetail detail : cartDetails) {
            totalPrice += detail.getPrice() * detail.getQuantity();
        }

        mav.addObject("cartDetails", cartDetails);
        mav.addObject("totalPrice", totalPrice);

        return mav;
    }

    // ===== 6. XỬ LÝ ĐẶT HÀNG (COD & VNPAY) =====
    @PostMapping("/place-order")
    public String handlePlaceOrder(
            HttpServletRequest request,
            @RequestParam("receiverName") String receiverName,
            @RequestParam("receiverAddress") String receiverAddress,
            @RequestParam("receiverPhone") String receiverPhone,
            @RequestParam("paymentMethod") String paymentMethod) {

        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        try {
            // Bước 1: Tạo đơn hàng (PENDING)
            Order newOrder = orderService.createOrderFromCart(receiverName, receiverAddress, receiverPhone);

            // Bước 2: Phân luồng thanh toán
            if ("COD".equals(paymentMethod)) {
                // COD: Cập nhật trạng thái, xóa giỏ, chuyển trang thành công
                orderService.updateOrderStatus(newOrder, "PROCESSING");
                orderService.clearUserCart(currentUser);
                return "redirect:/order-success";

            } else if ("VNPAY".equals(paymentMethod)) {
                // VNPAY: Tạo URL và chuyển hướng
                Long amount = (long) newOrder.getTotalPrice();
                String paymentUrl = paymentService.createPayment(request, newOrder.getId(), amount);
                return "redirect:" + paymentUrl;
            }

            return "redirect:/checkout?error=invalid_payment";

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/checkout?error=order_failed";
        }
    }

    // ===== 7. CÁC TRANG PHỤ TRỢ =====

    @GetMapping("/thanks")
    public ModelAndView getThankYouPage() {
        return new ModelAndView("client/cart/thanks");
    }

    // ===== 8. XÓA SẢN PHẨM KHỎI GIỎ =====
    @PostMapping("/delete-product-from-cart/{id}")
    public String deleteProductFromCart(@PathVariable long id, HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        // Kiểm tra đăng nhập
        if (session == null || userService.getCurrentUser() == null) {
            return "redirect:/login";
        }

        // Gọi service xóa
        this.cartService.removeProductCart(id, session);

        // Quay lại trang giỏ hàng
        return "redirect:/cart";
    }

    @GetMapping("/order-success")
    public String orderSuccess() {
        return "client/order_success_page";
    }
}