package com.lsb.webshop.controller.client;


import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lsb.webshop.domain.Product;
import com.lsb.webshop.domain.dto.ProductDTO;
import com.lsb.webshop.domain.dto.registerDTO;
import com.lsb.webshop.service.ProductService;
import com.lsb.webshop.service.UserService;

import jakarta.servlet.http.HttpSession;



@Controller
public class HomePageController {
    private final ProductService productService;
    private final UserService userService;

    public HomePageController(ProductService productService, UserService userService) {
        this.productService = productService;
        this.userService = userService;
    }

    @GetMapping("/")
    public String getMethodName(Model model, HttpSession session, Principal principal) {
    List<Product> products = this.productService.getAllActiveProducts();
    model.addAttribute("products", products);

    // Nếu đã đăng nhập thì lấy thông tin user
    if (principal != null) {
        String username = principal.getName(); // lấy username hiện tại
        var user = userService.findByUsername(username);
        session.setAttribute("fullName", user.getFullName());
        session.setAttribute("avatar", user.getAvatar());
    }

    return "client/homepage/show";
    }

    @GetMapping("/products")
    public String getFullProduct(Model model, HttpSession session, Principal principal){
        List<Product> products = this.productService.getAllActiveProducts();
        model.addAttribute("products", products);

        if(principal != null){
            String userName = principal.getName();
            var user = userService.findByUsername(userName);

            session.setAttribute("fullName", user.getFullName());
            session.setAttribute("avatar", user.getAvatar());
        }

        return "client/product/show";

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

    @GetMapping("/access-deny")
    public String accessDeniedPage() {
        return "client/auth/deny";
    }

     @GetMapping("/api/search")
    @ResponseBody
    public List<ProductDTO> searchProducts(@RequestParam("keyword") String keyword) {
        if (keyword == null || keyword.trim().length() < 2) {
            return List.of();
        }

        List<Product> products = productService.searchProducts(keyword);
        return products.stream()
                .map(product -> new ProductDTO(product.getId(), product.getName(), product.getShortDesc()))
                .collect(Collectors.toList());
    }

}

