package com.lsb.webshop.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lsb.webshop.domain.Cart;
import com.lsb.webshop.domain.User;

@Service
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
}

