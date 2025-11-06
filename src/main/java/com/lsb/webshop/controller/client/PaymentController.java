package com.lsb.webshop.controller.client;
import com.lsb.webshop.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.UnsupportedEncodingException;
import java.util.Map;

@Controller
public class PaymentController {

    // CHỈ CẦN TIÊM SERVICE
    @Autowired
    private PaymentService paymentService;

    /**
     * HÀM 1: Tạo thanh toán
     */
    @PostMapping("/create-payment")
    public String createPayment(HttpServletRequest req,
                                @RequestParam("orderId") Long orderId,
                                @RequestParam("amount") Long amount) {
        try {
            String paymentUrl = paymentService.createPayment(req, orderId, amount);
            // Service trả về URL đầy đủ
            return "redirect:" + paymentUrl;
        } catch (UnsupportedEncodingException e) {
            // Xử lý lỗi
            e.printStackTrace();
            return "redirect:/error-page"; // (Trang lỗi của bạn)
        }
    }

    /**
     * HÀM 2: Xử lý Return URL (Dành cho User)
     */
    @GetMapping("/vnpay-return")
    public String vnpayReturn(HttpServletRequest request, Model model) {
        // Gọi service để xử lý logic
        Map<String, String> result = paymentService.processVnPayReturn(request);

        // Thêm kết quả vào Model để View hiển thị
        model.addAttribute("message", result.get("message"));
        model.addAttribute("status", result.get("status"));

        return "client/payment_result"; // Trả về view
    }

    /**
     * HÀM 3: Xử lý IPN (Dành cho VNPAY Server)
     */
    @GetMapping("/vnpay-ipn")
    @ResponseBody // Rất quan trọng
    public ResponseEntity<String> vnpayIpn(HttpServletRequest request) {
        // Gọi service để xử lý logic
        String responseJson = paymentService.processVnPayIpn(request);

        // Trả về JSON cho VNPAY
        return new ResponseEntity<>(responseJson, HttpStatus.OK);
    }
}
