package com.lsb.webshop.controller.client;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import com.lsb.webshop.service.HomeService;
import org.springframework.data.domain.Page;
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
    private final HomeService homeService;

    public HomePageController(ProductService productService, UserService userService, HomeService homeService) {
        this.productService = productService;
        this.userService = userService;
        this.homeService = homeService;
    }

    @GetMapping("/")
    public ModelAndView homepage(HttpSession session, Principal principal) {
        ModelAndView mav = new ModelAndView("client/homepage/show");
        Map<String, Object> data = homeService.getHomepageData(principal);
        mav.addAllObjects(data);

        if (data.containsKey("userFullName")) {
            session.setAttribute("fullName", data.get("userFullName"));
            session.setAttribute("avatar", data.get("userAvatar"));
        }
        return mav;
    }

    /**
     * TRANG DANH SÁCH SẢN PHẨM & KẾT QUẢ TÌM KIẾM (HTML)
     * URL: /products?keyword=...
     */
    @GetMapping("/products")
    public String getProductPage(Model model,
                                 HttpSession session,
                                 Principal principal,
                                 @RequestParam(value = "keyword", required = false) String keyword,
                                 @RequestParam(value = "page", required = false, defaultValue = "1") int page) {

        // Cấu hình số lượng sản phẩm trên 1 trang
        int pageSize = 6;

        Page<Product> productPage;

        if (keyword != null && !keyword.trim().isEmpty()) {
            // Có từ khóa -> Tìm kiếm phân trang
            productPage = productService.searchClientProducts(keyword, page - 1, pageSize);
            model.addAttribute("keyword", keyword);
        } else {
            // Không có từ khóa -> Lấy tất cả phân trang
            productPage = productService.fetchClientProducts(page - 1, pageSize);
        }

        List<Product> products = productPage.getContent();

        model.addAttribute("products", products);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());

        // Cập nhật session user info nếu cần
        if (principal != null) {
            var user = userService.findByUsername(principal.getName());
            session.setAttribute("fullName", user.getFullName());
            session.setAttribute("avatar", user.getAvatar());
        }

        return "client/product/show";
    }

    /**
     * API TÌM KIẾM AJAX (JSON)
     * URL: /api/search?keyword=...
     * Dùng cho ô tìm kiếm gợi ý khi gõ
     */
    @GetMapping("/api/search")
    @ResponseBody // Bắt buộc có để trả về JSON
    public List<ProductDTO> searchProductsApi(@RequestParam("keyword") String keyword) {
        return productService.searchProductsDTO(keyword);
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
}