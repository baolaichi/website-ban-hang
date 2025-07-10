package com.lsb.webshop.controller.client;


import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.lsb.webshop.domain.Product;
import com.lsb.webshop.domain.dto.registerDTO;
import com.lsb.webshop.service.ProductService;
import com.lsb.webshop.service.UserService;



@Controller
public class HomePageController {
    private final ProductService productService;
    private final UserService userService;

    public HomePageController(ProductService productService, UserService userService) {
        this.productService = productService;
        this.userService = userService;
    }

    @GetMapping("/")
    public String getMethodName(Model model) {
        List<Product> products = this.productService.getAllActiveProducts();
        model.addAttribute("products", products);
        return "client/homepage/show";
    }

    @GetMapping("/register")
    public String getRegisterPage(Model model) {
        model.addAttribute("title", "Đăng ký tài khoản");
        model.addAttribute("newUser", new registerDTO());
        return "client/auth/register";
    }

    @PostMapping("/register")
    public String handleRegisterPage(@ModelAttribute("newUser") registerDTO dto, Model model) {
    try {
        userService.register(dto);
        return "redirect:/login?success";
    } catch (Exception e) {
        model.addAttribute("error", e.getMessage());
        return "client/auth/register";
    }
}

     @GetMapping("/login")
    public String loginPage() {
        return "client/auth/login";
    }

}

