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
import java.util.Optional;

@Controller
@RequestMapping("/account")
public class AccountController {

    private final UserService userService;
    private final UploadService uploadService;
    private final OrderService orderService;

    public AccountController(UserService userService, UploadService uploadService, OrderService orderService) {
        this.userService = userService;
        this.uploadService = uploadService;
        this.orderService = orderService;
    }

    @ModelAttribute("currentPath")
    public String getCurrentPath(HttpServletRequest request) {
        return request.getRequestURI();
    }

    @GetMapping("/profile")
    public ModelAndView viewProfile(Principal principal,
                                    @ModelAttribute("successMessage") String successMessage,
                                    @ModelAttribute("errorMessage") String errorMessage) {
        UserDTO userDTO = userService.getUserByEmail(principal.getName());
        ModelAndView mav = new ModelAndView("client/account/profile");
        mav.addObject("user", userDTO);
        mav.addObject("successMessage", successMessage);
        mav.addObject("errorMessage", errorMessage);
        return mav;
    }

    @PostMapping("/update")
    public ModelAndView updateProfile(@ModelAttribute("user") UserDTO userDTO,
                                      @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
                                      Principal principal,
                                      RedirectAttributes redirectAttributes) {
        ModelAndView mav = new ModelAndView();
        try {
            userService.updateProfile(userDTO, avatarFile, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật hồ sơ thành công!");
            mav.setViewName("redirect:/account/profile");
            return mav;
        } catch (RuntimeException e) {
            mav.setViewName("client/account/profile");
            mav.addObject("errorMessage", e.getMessage());
            mav.addObject("user", userDTO);
            return mav;
        }
    }

    @GetMapping("/change-password")
    public ModelAndView getChangePasswordPage(
            @ModelAttribute("successMessage") String successMessage,
            @ModelAttribute("errorMessage") String errorMessage) {
        ModelAndView mav = new ModelAndView("client/account/change-password");
        mav.addObject("successMessage", successMessage);
        mav.addObject("errorMessage", errorMessage);
        return mav;
    }

    @PostMapping("/change-password")
    public String handleChangePassword(
            @RequestParam("oldPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu mới không khớp!");
            return "redirect:/account/change-password";
        }

        try {
            userService.changePassword(principal.getName(), oldPassword, newPassword);
            redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu thành công!");
            return "redirect:/account/change-password";
        } catch (AuthenticationException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/account/change-password";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi, vui lòng thử lại.");
            return "redirect:/account/change-password";
        }
    }

    @GetMapping("/orders")
    public ModelAndView getOrderHistoryPage(Principal principal,
                                            @ModelAttribute("error") String error) {
        ModelAndView mav = new ModelAndView("client/account/orders");
        User user = userService.findByUsername(principal.getName());
        // OrderService trả về List<Order> nên không cần .get()
        mav.addObject("orders", orderService.findOrdersByUser(user));

        if ("notfound".equals(error)) {
            mav.addObject("errorMessage", "Không tìm thấy đơn hàng hoặc đơn hàng không thuộc về bạn.");
        }
        return mav;
    }

    // ===== HÀM NÀY LÀ NƠI XẢY RA LỖI =====
    @GetMapping("/orders/{id}")
    public ModelAndView getOrderDetailPage(@PathVariable("id") Long orderId, Principal principal,
                                           RedirectAttributes redirectAttributes,
                                           @ModelAttribute("successMessage") String successMessage,
                                           @ModelAttribute("errorMessage") String errorMessage) {
        ModelAndView mav = new ModelAndView("client/account/order-detail");
        User user = userService.findByUsername(principal.getName());

        // Hàm này trả về Optional<Order>
        Optional<Order> orderOpt = orderService.findOrderByIdAndUser(orderId, user);

        if (orderOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "notfound");
            mav.setViewName("redirect:/account/orders");
            return mav;
        }

        // === QUAN TRỌNG NHẤT: PHẢI CÓ .get() ===
        // Sai: mav.addObject("order", orderOpt);
        // Đúng:
        mav.addObject("order", orderOpt.get());
        // =======================================

        mav.addObject("successMessage", successMessage);
        mav.addObject("errorMessage", errorMessage);
        return mav;
    }

    @PostMapping("/orders/cancel/{id}")
    public String handleCancelOrder(@PathVariable("id") Long orderId, Principal principal, RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(principal.getName());

        try {
            boolean success = orderService.cancelOrder(orderId, user);
            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Đã hủy đơn hàng thành công.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không thể hủy đơn hàng (có thể đã giao).");
            }
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền hủy đơn hàng này.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi khi hủy đơn hàng.");
        }

        return "redirect:/account/orders/" + orderId;
    }
}