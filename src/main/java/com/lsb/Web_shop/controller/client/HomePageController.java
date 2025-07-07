package com.lsb.web_shop.controller.client;


import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.lsb.web_shop.domain.Product;
import com.lsb.web_shop.service.ProductService;


@Controller
public class HomePageController {
    private final ProductService productService;

    public HomePageController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/")
    public String getMethodName(Model model) {
        List<Product> products = this.productService.getAllProducts();
        model.addAttribute("products", products);
        return "client/homepage/show";
    }

    @GetMapping("/register")
    public String registerAccount(Model model) {
        
        model.addAttribute("title", "Đăng ký tài khoản");
        return "client/auth/register";
    }
    

}

