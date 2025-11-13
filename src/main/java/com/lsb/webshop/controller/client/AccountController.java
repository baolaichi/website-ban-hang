package com.lsb.webshop.controller.client;

import com.lsb.webshop.domain.Order;
import com.lsb.webshop.domain.User;
import com.lsb.webshop.domain.dto.UserDTO;
import com.lsb.webshop.service.OrderService;
import com.lsb.webshop.service.UploadService;
import com.lsb.webshop.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/account")
public class AccountController {

    private final UserService userService;
    private final UploadService uploadService;
    private final OrderService orderService; // Thêm OrderService

    public AccountController(UserService userService, UploadService uploadService, OrderService orderService) {
        this.userService = userService;
        this.uploadService = uploadService;
        this.orderService = orderService; // Tiêm
    }

    // ===== HỒ SƠ CÁ NHÂN =====

    @GetMapping("/profile")
    public ModelAndView viewProfile(Principal principal) {
        UserDTO userDTO = userService.getUserByEmail(principal.getName());
        ModelAndView mav = new ModelAndView("client/account/profile");
        mav.addObject("user", userDTO);
        return mav;
    }

    @PostMapping("/update")
    public ModelAndView updateProfile(@ModelAttribute("user") UserDTO userDTO,
                                      @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
                                      Principal principal,
                                      RedirectAttributes redirectAttributes) { // Thêm RedirectAttributes
        ModelAndView mav = new ModelAndView();
        try {
            userService.updateProfile(userDTO, avatarFile, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật hồ sơ thành công!");
            mav.setViewName("redirect:/account/profile");
            return mav;
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            mav.setViewName("redirect:/account/profile");
            return mav;
        }
    }

    // ===== THAY ĐỔI MẬT KHẨU (MỚI) =====

    @GetMapping("/change-password")
    public ModelAndView getChangePasswordPage() {
        ModelAndView mav = new ModelAndView("client/account/change-password");
        return mav;
    }

    @PostMapping("/change-password")
    public String handleChangePassword(
            @RequestParam("oldPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            Principal principal,
            RedirectAttributes redirectAttributes) { // Dùng để gửi thông báo

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu xác nhận không khớp!");
            return "redirect:/account/change-password";
        }

        try {
            userService.changePassword(principal.getName(), oldPassword, newPassword);
            redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu thành công!");
            return "redirect:/account/change-password";
        } catch (AuthenticationException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu cũ không chính xác!");
            return "redirect:/account/change-password";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi: " + e.getMessage());
            return "redirect:/account/change-password";
        }
    }

    // ===== LỊCH SỬ ĐƠN HÀNG (MỚI) =====

    @GetMapping("/orders")
    public ModelAndView getOrderHistoryPage(Principal principal) {
        ModelAndView mav = new ModelAndView("client/account/orders");
        User currentUser = userService.findByUsername(principal.getName());
        List<Order> orders = orderService.findOrdersByUser(currentUser);
        mav.addObject("orders", orders);
        return mav;
    }

    @GetMapping("/orders/{id}")
    public ModelAndView getOrderDetailPage(@PathVariable("id") Long orderId, Principal principal) {
        ModelAndView mav = new ModelAndView("client/account/order-detail");
        User currentUser = userService.findByUsername(principal.getName());

        // Tìm đơn hàng bằng ID VÀ User (để bảo mật)
        Optional<Order> order = orderService.findOrderByIdAndUser(orderId, currentUser);

        if (order == null) {
            // Nếu không phải đơn hàng của user này, chuyển hướng
            mav.setViewName("redirect:/account/orders?error=notfound");
            return mav;
        }

        mav.addObject("order", order);
        return mav;
    }

    // ===== BẮT ĐẦU SỬA LỖI (THÊM HÀM NÀY) =====
    /**
     * Thêm URL hiện tại vào Model cho TẤT CẢ các request
     * để Thymeleaf có thể đọc và set 'active' class.
     * Đây là cách sửa lỗi "request object is no longer available".
     */
    @ModelAttribute("currentPath")
    public String getCurrentPath(HttpServletRequest request) {
        return request.getRequestURI();
    }
}