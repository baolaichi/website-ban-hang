package com.lsb.webshop.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.lsb.webshop.domain.CartDetail;
import com.lsb.webshop.repository.CartDetailRepository;
import com.lsb.webshop.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.lsb.webshop.domain.Cart;
import com.lsb.webshop.domain.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class CartService {

    @Autowired
    private ProductService productService;
    @Autowired
    private CartDetailRepository cartDetailRepository;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private String getCartKey(Long userId) {
        return "cart:" + userId; // ví dụ: cart:5
    }

    public Map<String, Object> updateQuantity(String email, Long productId, int quantity) {
        Map<String, Object> result = new HashMap<>();

        try {
            // Cập nhật số lượng sản phẩm
            productService.updateProductQuantity(email, productId, quantity);

            // Lấy giỏ hàng sau cập nhật
            User currentUser = userService.findByUsername(email);
            Cart cart = productService.fetchByUser(currentUser);

            if (cart == null || cart.getCartDetails() == null) {
                result.put("message", "Giỏ hàng không tồn tại");
                return result;
            }

            // Tính tổng tiền
            double totalPrice = cart.getCartDetails().stream()
                    .mapToDouble(cd -> cd.getPrice() * cd.getQuantity())
                    .sum();

            result.put("message", "Cập nhật thành công");
            result.put("totalPrice", totalPrice);
        } catch (Exception e) {
            result.put("message", "Cập nhật thất bại: " + e.getMessage());
        }

        return result;
    }

    public Map<String, Object> handleUpdateQuantity(HttpServletRequest request, Long productId, int quantity) {
    Map<String, Object> result = new HashMap<>();
    HttpSession session = request.getSession(false);
    if (session == null || session.getAttribute("email") == null) {
        result.put("status", HttpStatus.UNAUTHORIZED);
        result.put("message", "Bạn cần đăng nhập để cập nhật giỏ hàng");
        return result;
    }

    String email = (String) session.getAttribute("email");

    try {
        Map<String, Object> updateResult = updateQuantity(email, productId, quantity); // gọi method đã có
        String message = updateResult.get("message").toString();

        if (message.startsWith("Cập nhật thành công")) {
            result.put("status", HttpStatus.OK);
        } else {
            result.put("status", HttpStatus.BAD_REQUEST);
        }

        result.putAll(updateResult);
        return result;

    } catch (Exception e) {
        log.error("[CartService] handleUpdateQuantity() - Lỗi: {}", e.getMessage(), e);
        result.put("status", HttpStatus.INTERNAL_SERVER_ERROR);
        result.put("message", "Đã xảy ra lỗi khi cập nhật giỏ hàng");
        return result;
    }
    }

    public void removeProductCart(long cartDetailId, HttpSession session) {
        try {
            log.info("Bắt đầu xóa sản phẩm trong giỏ hàng với cartDetailId={}", cartDetailId);

            Optional<CartDetail> cartDetailOptional = cartDetailRepository.findById(cartDetailId);

            if (cartDetailOptional.isEmpty()) {
                log.warn("Không tìm thấy chi tiết giỏ hàng với ID = {}. Hủy thao tác xóa.", cartDetailId);
                return;
            }

            CartDetail cartDetail = cartDetailOptional.get();
            Cart cart = cartDetail.getCart();

            cartDetailRepository.deleteById(cartDetailId);
            log.info("Đã xóa thành công CartDetail có ID = {}", cartDetailId);

            if (cart != null) {
                int currentSum = cart.getSum();
                log.debug("Số lượng sản phẩm hiện tại trong giỏ là {}", currentSum);

                if (currentSum > 1) {
                    cart.setSum(currentSum - 1);
                    cartRepository.save(cart);
                    session.setAttribute("sum", cart.getSum());
                    log.info("Đã cập nhật số lượng sản phẩm trong giỏ xuống còn {} cho giỏ hàng có ID = {}", cart.getSum(), cart.getId());
                } else {
                    cartRepository.deleteById(cart.getId());
                    session.setAttribute("sum", 0);
                    log.info("Giỏ hàng có ID = {} đã bị xóa do không còn sản phẩm nào.", cart.getId());
                }
            } else {
                log.error("Không thể xác định giỏ hàng tương ứng với CartDetail ID = {}", cartDetailId);
            }

        } catch (Exception e) {
            log.error("Lỗi khi xóa sản phẩm khỏi giỏ hàng với CartDetail ID = {}: {}", cartDetailId, e.getMessage(), e);
        }
    }

    public void updateCartBeforeCheckout(List<CartDetail> cartDetails) {
        for(CartDetail cartDetail : cartDetails){
            Optional<CartDetail> cdOptional = this.cartDetailRepository.findById(cartDetail.getId());
            if(cdOptional.isPresent()){
                CartDetail currentCartDetail = cdOptional.get();
                currentCartDetail.setQuantity(cartDetail.getQuantity());
                this.cartDetailRepository.save(currentCartDetail);
            }
        }
    }

    public boolean addProductToCart(HttpServletRequest request, long productId) {
        HttpSession session = request.getSession(false);

        if (session != null && session.getAttribute("email") != null) {
            String email = (String) session.getAttribute("email");
            productService.addProductToCart(email, productId, session);
            return true; // Thêm thành công
        }

        return false; // Chưa đăng nhập hoặc session hết hạn
    }



}

