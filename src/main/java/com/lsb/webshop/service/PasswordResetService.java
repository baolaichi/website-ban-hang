package com.lsb.webshop.service;

import com.lsb.webshop.domain.User;
import com.lsb.webshop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class PasswordResetService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 1. Gửi mã OTP 6 số
    public void sendEmail(String email) {
        // Tìm user
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            // (Để bảo mật, không báo lỗi nếu email không tồn tại, chỉ log ra console)
            System.out.println("Email không tồn tại: " + email);
            return;
        }

        // Tạo mã OTP 6 số ngẫu nhiên
        String otp = String.format("%06d", new Random().nextInt(999999));

        // Lưu OTP vào CSDL (Hết hạn sau 5 phút)
        user.setResetToken(otp);
        user.setTokenExpiryDate(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);

        // Gửi Email
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Mã xác nhận đổi mật khẩu - LaptopShop");
            message.setText("Mã xác nhận (OTP) của bạn là: " + otp + "\n\nMã này sẽ hết hạn sau 5 phút.");
            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 2. Xác thực OTP và Đổi mật khẩu
    public boolean verifyOtpAndResetPassword(String email, String otp, String newPassword) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return false;

        // Kiểm tra OTP có khớp không
        if (user.getResetToken() == null || !user.getResetToken().equals(otp)) {
            return false;
        }

        // Kiểm tra OTP có hết hạn chưa
        if (user.getTokenExpiryDate().isBefore(LocalDateTime.now())) {
            return false;
        }

        // Đổi mật khẩu
        user.setPassword(passwordEncoder.encode(newPassword));

        // Xóa OTP sau khi dùng xong
        user.setResetToken(null);
        user.setTokenExpiryDate(null);
        userRepository.save(user);

        return true;
    }
}