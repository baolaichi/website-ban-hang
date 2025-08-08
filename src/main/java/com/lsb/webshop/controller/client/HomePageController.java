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
import org.springframework.web.servlet.ModelAndView;


@Controller
public class HomePageController {
    private final ProductService productService;
    private final UserService userService;

    public HomePageController(ProductService productService, UserService userService) {
        this.productService = productService;
        this.userService = userService;
    }

    @GetMapping("/")
    public ModelAndView getMethodName(HttpSession session, Principal principal) {
        ModelAndView mav = new ModelAndView("client/homepage/show");

        List<Product> products = this.productService.getAllActiveProducts();
        mav.addObject("products", products);

        // Nếu đã đăng nhập thì lấy thông tin user
        if (principal != null) {
            String username = principal.getName(); // lấy username hiện tại
            var user = userService.findByUsername(username);
            session.setAttribute("fullName", user.getFullName());
            session.setAttribute("avatar", user.getAvatar());
        }

        return mav;
    }

    @GetMapping("/products")
    public ModelAndView getFullProduct(HttpSession session, Principal principal) {
        ModelAndView mav = new ModelAndView("client/product/show");

        List<Product> products = this.productService.getAllActiveProducts();
        mav.addObject("products", products);

        if (principal != null) {
            String userName = principal.getName();
            var user = userService.findByUsername(userName);

            session.setAttribute("fullName", user.getFullName());
            session.setAttribute("avatar", user.getAvatar());
        }

        return mav;
    }


    @GetMapping("/register")
    public ModelAndView getRegisterPage() {
        ModelAndView mav = new ModelAndView("client/auth/register");
        mav.addObject("title", "Đăng ký tài khoản");
        mav.addObject("newUser", new registerDTO());
        return mav;
    }


    @PostMapping("/register")
    public ModelAndView handleRegisterPage(@ModelAttribute("newUser") registerDTO dto) {
        try {
            userService.register(dto);
            return new ModelAndView("redirect:/login?success");
        } catch (Exception e) {
            ModelAndView mav = new ModelAndView("client/auth/register");
            mav.addObject("error", e.getMessage());
            return mav;
        }
    }


    @GetMapping("/login")
    public ModelAndView loginPage() {
        return new ModelAndView("client/auth/login");
    }


    @GetMapping("/access-deny")
    public ModelAndView accessDeniedPage() {
        return new ModelAndView("client/auth/deny");
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

