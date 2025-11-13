package com.lsb.webshop.controller.client;


import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.lsb.webshop.service.HomeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody; // <-- THÊM IMPORT

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
    private final HomeService homeService;

    public HomePageController(ProductService productService, UserService userService, HomeService homeService) {
        this.productService = productService;
        this.userService = userService;
        this.homeService = homeService;
    }

    @GetMapping("/")
    public ModelAndView homepage(HttpSession session, Principal principal) {
        ModelAndView mav = new ModelAndView("client/homepage/show");

        // 1. Gọi HomeService để lấy TẤT CẢ dữ liệu 1 LẦN
        Map<String, Object> data = homeService.getHomepageData(principal);

        // 2. Thêm TẤT CẢ dữ liệu (products, categories, recommendedProducts...) vào Model
        mav.addAllObjects(data);

        // 3. Set thông tin user vào session (Logic cũ của bạn)
        if (data.containsKey("userFullName")) {
            session.setAttribute("fullName", data.get("userFullName"));
            session.setAttribute("avatar", data.get("userAvatar"));
        }

        // (Không cần code gợi ý ở đây nữa vì HomeService đã làm)

        return mav;
    }

    @GetMapping("/products")
    public ModelAndView getFullProduct(HttpSession session, Principal principal) {
        // (Giữ nguyên)
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
        // (Giữ nguyên)
        ModelAndView mav = new ModelAndView("client/auth/register");
        mav.addObject("title", "Đăng ký tài khoản");
        mav.addObject("newUser", new registerDTO());
        return mav;
    }


    @PostMapping("/register")
    public ModelAndView handleRegisterPage(@ModelAttribute("newUser") registerDTO dto) {
        // (Giữ nguyên)
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
        // (Giữ nguyên)
        return new ModelAndView("client/auth/login");
    }


    @GetMapping("/access-deny")
    public ModelAndView accessDeniedPage() {
        // (Giữ nguyên)
        return new ModelAndView("client/auth/deny");
    }


    @GetMapping("/search")
    @ResponseBody // <-- SỬA LỖI: THÊM DÒNG NÀY
    public List<ProductDTO> searchProducts(@RequestParam("keyword") String keyword) {
        // (Hàm này trả về JSON, cần @ResponseBody)
        return productService.searchProductsDTO(keyword);
    }

}