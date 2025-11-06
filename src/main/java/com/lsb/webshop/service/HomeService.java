package com.lsb.webshop.service;

import com.lsb.webshop.domain.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class HomeService {
    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    public Map<String, Object> getHomepageData(Principal principal) {
        Map<String, Object> data = new HashMap<>();

        // Lấy danh sách sản phẩm active
        List<Product> products = productService.getAllActiveProducts();
        data.put("products", products);

        // Nếu đã đăng nhập, lấy thông tin user
        if (principal != null) {
            String username = principal.getName();
            var user = userService.findByUsername(username);

            // Trả về session data
            data.put("userFullName", user.getFullName());
            data.put("userAvatar", user.getAvatar());
        }

        return data;
    }

}
