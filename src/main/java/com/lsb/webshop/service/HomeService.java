package com.lsb.webshop.service;

import com.lsb.webshop.domain.Category;
import com.lsb.webshop.domain.Product;
import com.lsb.webshop.domain.User;
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

    @Autowired
    private CategoryService categoryService;

    public Map<String, Object> getHomepageData(Principal principal) {
        Map<String, Object> data = new HashMap<>();

        // 1. Lấy danh sách sản phẩm (Logic cũ của bạn)
        List<Product> products = productService.getAllActiveProducts();
        data.put("products", products);

        // 2. Lấy danh sách danh mục (Logic mới cho Tab Lọc)
        List<Category> categories = categoryService.findAll();
        data.put("categories", categories);

        // 3. Xử lý User và Gợi ý AI (Logic mới)
        User currentUser = null;
        if (principal != null) {
            String username = principal.getName();
            currentUser = userService.findByUsername(username);

            data.put("userFullName", currentUser.getFullName());
            data.put("userAvatar", currentUser.getAvatar());
        }

        // 4. Lấy danh sách Gợi ý (AI)
        List<Product> recommendedProducts = productService.getRecommendedProducts(currentUser);
        String recommendTitle = (currentUser != null) ? "Gợi ý dành riêng cho bạn" : "Sản phẩm bán chạy nhất";

        data.put("recommendedProducts", recommendedProducts);
        data.put("recommendTitle", recommendTitle);


        return data;
    }

}
