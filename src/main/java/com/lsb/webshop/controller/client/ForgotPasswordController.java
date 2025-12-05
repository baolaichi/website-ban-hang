package com.lsb.webshop.controller.client;

import com.lsb.webshop.service.PasswordResetService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ForgotPasswordController {

    @Autowired
    private PasswordResetService passwordResetService;

    // 1. Trang nhập Email
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "client/auth/forgot-password";
    }

    // 2. Xử lý gửi OTP
    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email, HttpSession session) {
        // Gửi OTP (Code bên trong service đã xử lý việc tìm user)
        passwordResetService.sendEmail(email);

        // Lưu tạm email vào session để dùng ở bước sau
        session.setAttribute("resetEmail", email);

        // Chuyển sang trang nhập OTP
        return "redirect:/verify-otp";
    }

    // 3. Trang nhập OTP
    @GetMapping("/verify-otp")
    public String showVerifyOtpForm() {
        return "client/auth/verify-otp";
    }

    // 4. Xử lý đổi mật khẩu
    @PostMapping("/verify-otp")
    public String processVerifyOtp(@RequestParam("otp") String otp,
                                   @RequestParam("newPassword") String newPassword,
                                   HttpSession session, Model model) {

        String email = (String) session.getAttribute("resetEmail");
        if (email == null) {
            return "redirect:/forgot-password"; // Session hết hạn, làm lại từ đầu
        }

        boolean success = passwordResetService.verifyOtpAndResetPassword(email, otp, newPassword);

        if (success) {
            session.removeAttribute("resetEmail"); // Xóa session tạm
            return "redirect:/login?resetSuccess";
        } else {
            model.addAttribute("error", "Mã OTP không đúng hoặc đã hết hạn.");
            return "client/auth/verify-otp";
        }
    }
}