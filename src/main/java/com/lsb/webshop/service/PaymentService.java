package com.lsb.webshop.service;

import com.lsb.webshop.config.VnPayConfig;
import com.lsb.webshop.domain.Order;
import com.lsb.webshop.domain.Payment;
import com.lsb.webshop.domain.User; // <-- 1. THÊM IMPORT
import com.lsb.webshop.repository.OrderRepository;
import com.lsb.webshop.repository.PaymentRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional; // <-- 2. THÊM IMPORT

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class PaymentService {
    // Tiêm các giá trị config (Giữ nguyên)
    @Value("${vnpay.tmnCode}")
    private String tmnCode;

    @Value("${vnpay.hashSecret}")
    private String hashSecret;

    @Value("${vnpay.url}")
    private String vnpayUrl;

    @Value("${vnpay.returnUrl}")
    private String returnUrl;

    @Value("${vnpay.ipnUrl}")
    private String ipnUrl;

    // Tiêm Repositories (Giữ nguyên)
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    // ===== BẮT ĐẦU SỬA LỖI (THÊM DÒNG NÀY) =====
    @Autowired
    private OrderService orderService; // <-- 3. TIÊM OrderService
    // ===== KẾT THÚC SỬA LỖI =====


    /**
     * HÀM 1: Tạo thanh toán
     * (Hàm này đã đúng, nên thêm @Transactional)
     */
    @Transactional // (Nên thêm)
    public String createPayment(HttpServletRequest req, Long orderId, Long amount)
            throws UnsupportedEncodingException {

        String vnp_TxnRef = VnPayConfig.getRandomNumber(8);
        String vnp_IpAddr = req.getRemoteAddr();

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", tmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount * 100)); // Số tiền * 100
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang " + orderId);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", returnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new RuntimeException("Order not found with id: " + orderId);
        }

        Payment payment = new Payment();
        payment.setOrder(orderOpt.get());
        payment.setPaymentMethod("VNPAY");
        payment.setAmount(Double.valueOf(amount));
        payment.setStatus("PENDING");
        payment.setTransactionCode(vnp_TxnRef); // Lưu mã TxnRef
        payment.setCreatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        String hashData = VnPayConfig.hashAllFields(vnp_Params, hashSecret);
        vnp_Params.put("vnp_SecureHash", hashData);

        StringBuilder queryUrl = new StringBuilder();
        for (Map.Entry<String, String> entry : vnp_Params.entrySet()) {
            queryUrl.append(URLEncoder.encode(entry.getKey(), StandardCharsets.US_ASCII.toString()));
            queryUrl.append('=');
            queryUrl.append(URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII.toString()));
            queryUrl.append('&');
        }
        queryUrl.deleteCharAt(queryUrl.length() - 1);

        return vnpayUrl + "?" + queryUrl.toString();
    }

    /**
     * HÀM 2: Xử lý Return URL (Giữ nguyên, hàm này đã đúng)
     */
    public Map<String, String> processVnPayReturn(HttpServletRequest request) {
        Map<String, String> fields = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements(); ) {
            String fieldName = params.nextElement();
            String fieldValue = request.getParameter(fieldName);
            fields.put(fieldName, fieldValue);
        }

        String vnp_SecureHash = fields.remove("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");

        String hashData = VnPayConfig.hashAllFields(fields, hashSecret);
        String vnp_ResponseCode = fields.get("vnp_ResponseCode");

        Map<String, String> result = new HashMap<>();

        if (hashData.equals(vnp_SecureHash)) {
            if ("00".equals(vnp_ResponseCode)) {
                result.put("message", "Giao dịch thành công!");
                result.put("status", "success");
            } else {
                result.put("message", "Giao dịch thất bại. Mã lỗi: " + vnp_ResponseCode);
                result.put("status", "failed");
            }
        } else {
            result.put("message", "Chữ ký không hợp lệ!");
            result.put("status", "invalid");
        }
        return result;
    }

    /**
     * HÀM 3: Xử lý IPN
     * (Đã được cập nhật)
     */
    @Transactional // <-- 4. THÊM @Transactional VÀO ĐÂY
    public String processVnPayIpn(HttpServletRequest request) {
        Map<String, String> fields = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements(); ) {
            String fieldName = params.nextElement();
            String fieldValue = request.getParameter(fieldName);
            fields.put(fieldName, fieldValue);
        }

        String vnp_SecureHash = fields.remove("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");

        String hashData = VnPayConfig.hashAllFields(fields, hashSecret);
        String vnp_ResponseCode = fields.get("vnp_ResponseCode");
        String vnp_TxnRef = fields.get("vnp_TxnRef");

        String RspCode;
        String Message;

        if (hashData.equals(vnp_SecureHash)) {
            Optional<Payment> paymentOpt = paymentRepository.findByTransactionCode(vnp_TxnRef);

            if (paymentOpt.isPresent()) {
                Payment payment = paymentOpt.get();
                Order order = payment.getOrder();

                if ("PENDING".equals(payment.getStatus())) {
                    if ("00".equals(vnp_ResponseCode)) {
                        payment.setStatus("SUCCESS");
                        order.setStatus("PROCESSING"); // Cập nhật trạng thái Order
                        paymentRepository.save(payment);
                        orderRepository.save(order);

                        // ===== BẮT ĐẦU SỬA LỖI (THÊM 2 DÒNG NÀY) =====
                        User user = order.getUser(); // Lấy user từ đơn hàng
                        orderService.clearUserCart(user); // <-- 5. GỌI HÀM XÓA GIỎ HÀNG
                        // ===== KẾT THÚC SỬA LỖI =====

                        RspCode = "00";
                        Message = "Confirm Success";
                    } else {
                        payment.setStatus("FAILED");
                        paymentRepository.save(payment);
                        RspCode = "00";
                        Message = "Confirm Success (but payment failed)";
                    }
                } else {
                    RspCode = "02";
                    Message = "Order already confirmed";
                }
            } else {
                RspCode = "01";
                Message = "Order not found";
            }
        } else {
            RspCode = "97";
            Message = "Invalid Checksum";
        }

        return "{\"RspCode\":\"" + RspCode + "\",\"Message\":\"" + Message + "\"}";
    }
}