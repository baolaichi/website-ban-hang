package com.lsb.webshop.controller.client;

import com.lsb.webshop.domain.*;
import com.lsb.webshop.repository.CartDetailRepository;
import com.lsb.webshop.service.CartService;
import com.lsb.webshop.service.OrderService;
import com.lsb.webshop.service.PaymentService; // <-- Thêm import
import com.lsb.webshop.service.ProductService;
import com.lsb.webshop.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
// (Xóa import @Transactional nếu còn)

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class ItemController {

    private final ProductService productService;
    private final UserService userService;
    private final CartService cartService;
    private final CartDetailRepository cartDetailRepository;
    private final OrderService orderService;
    private final PaymentService paymentService; // <-- Thêm PaymentService

    public ItemController(ProductService productService, UserService userService,
                          CartService cartService, CartDetailRepository cartDetailRepository,
                          OrderService orderService, PaymentService paymentService) { // <-- Thêm vào constructor
        this.productService = productService;
        this.userService = userService;
        this.cartService = cartService;
        this.cartDetailRepository = cartDetailRepository;
        this.orderService = orderService;
        this.paymentService = paymentService; // <-- Gán
    }

    // (Các hàm /product/{id}, /add-product-to-cart, /cart, /cart/update giữ nguyên)

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
        } else {
            mav.setViewName("redirect:/products");
        }
        return mav;
    }

    @PostMapping("/add-product-to-cart/{id}")
    public ModelAndView addProductToCart(@PathVariable long id, HttpServletRequest request) {
        ModelAndView mav = new ModelAndView();
        if (cartService.addProductToCart(request, id)) {
            mav.setViewName("redirect:/");
        } else {
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

        // Lấy User đầy đủ
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

    // (Hàm /cart/update giữ nguyên)

    @GetMapping("/checkout")
    public ModelAndView getCheckoutPage(HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("client/cart/checkout");

        // Lấy User đầy đủ
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

    /**
     * HÀM NÀY XỬ LÝ THANH TOÁN (CẢ COD VÀ VNPAY)
     * (ĐÃ CẬP NHẬT)
     */
    // (Xóa @Transactional nếu còn)
    @PostMapping("/place-order")
    public String handlePlaceOrder(
            HttpServletRequest request, // Cần cho VNPAY
            @RequestParam("receiverName") String receiverName,
            @RequestParam("receiverAddress") String receiverAddress,
            @RequestParam("receiverPhone") String receiverPhone,
            @RequestParam("paymentMethod") String paymentMethod) {

        // 1. Kiểm tra session và lấy User (Cách này an toàn hơn)
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        try {
            // Bước 1: Tạo đơn hàng (Order) ở trạng thái PENDING
            Order newOrder = orderService.createOrderFromCart(receiverName, receiverAddress, receiverPhone);

            // Bước 2: Kiểm tra phương thức thanh toán
            if ("COD".equals(paymentMethod)) {

                // Cập nhật trạng thái đơn hàng (ví dụ: PROCESSING)
                orderService.updateOrderStatus(newOrder, "PROCESSING");

                // ===== BẮT ĐẦU SỬA LỖI (CẬP NHẬT DÒNG NÀY) =====
                // Xóa giỏ hàng (vì đã xác nhận COD)
                orderService.clearUserCart(currentUser); // Truyền User vào
                // ===== KẾT THÚC SỬA LỖI =====

                // Chuyển đến trang đặt hàng thành công
                return "redirect:/order-success";

            } else if ("VNPAY".equals(paymentMethod)) {

                // Nếu là VNPAY, gọi PaymentService để tạo URL
                Long amount = (long) newOrder.getTotalPrice();
                String paymentUrl = paymentService.createPayment(request, newOrder.getId(), amount);

                // Chuyển hướng người dùng sang VNPAY
                return "redirect:" + paymentUrl;
            }

            // Xử lý lỗi nếu phương thức thanh toán không hợp lệ
            return "redirect:/checkout?error=invalid_payment";

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/checkout?error=order_failed";
        }
    }

    // (Các hàm /thanks, /delete-product-from-cart, /order-success giữ nguyên)
    @GetMapping("/thanks")
    public ModelAndView getThankYouPage() {
        return new ModelAndView("client/cart/thanks");
    }

    @PostMapping("/delete-product-from-cart/{id}")
    public ModelAndView deleteProductFromCart(@PathVariable long id, HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("redirect:/cart");
        HttpSession session = request.getSession(false);
        if (session != null) {
            this.cartService.removeProductCart(id, session);
        } else {
            mav.setViewName("redirect:/login");
        }
        return mav;
    }

    @GetMapping("/order-success")
    public String orderSuccess() {
        return "client/order_success_page"; // (Bạn cần tạo file: client/order_success_page.html)
    }
}