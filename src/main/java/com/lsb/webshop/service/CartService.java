package com.lsb.webshop.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.lsb.webshop.domain.Cart;
import com.lsb.webshop.domain.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CartService {

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

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


}

