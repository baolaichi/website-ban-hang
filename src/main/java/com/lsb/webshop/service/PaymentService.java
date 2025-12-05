package com.lsb.webshop.service;

import com.lsb.webshop.config.VnPayConfig;
import com.lsb.webshop.domain.Order;
import com.lsb.webshop.domain.Payment;
import com.lsb.webshop.domain.User;
import com.lsb.webshop.repository.OrderRepository;
import com.lsb.webshop.repository.PaymentRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class PaymentService {
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

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderService orderService;

    @Transactional
    public String createPayment(HttpServletRequest req, Long orderId, Long amount) throws UnsupportedEncodingException {

        String vnp_TxnRef = VnPayConfig.getRandomNumber(8);

        // Xử lý IP: Nếu là localhost IPv6, chuyển về IPv4
        String vnp_IpAddr = VnPayConfig.getIpAddress(req);
        if ("0:0:0:0:0:0:0:1".equals(vnp_IpAddr)) {
            vnp_IpAddr = "127.0.0.1";
        }

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", tmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount * 100));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang:" + vnp_TxnRef);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", returnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        // Sắp xếp tham số (Bắt buộc)
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        // Vòng lặp xây dựng chuỗi hash chuẩn xác
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                // Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

                // Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        String queryUrl = query.toString();
        String vnp_SecureHash = VnPayConfig.hmacSHA512(hashSecret, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = vnpayUrl + "?" + queryUrl;

        // Lưu Payment
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Payment payment = new Payment();
            payment.setOrder(orderOpt.get());
            payment.setPaymentMethod("VNPAY");
            payment.setAmount(Double.valueOf(amount));
            payment.setStatus("PENDING");
            payment.setTransactionCode(vnp_TxnRef);
            payment.setCreatedAt(LocalDateTime.now());
            paymentRepository.save(payment);
        }

        return paymentUrl;
    }

    // ... (Hàm processVnPayReturn và processVnPayIpn giữ nguyên như phiên bản trước) ...
    @Transactional
    public Map<String, String> processVnPayReturn(HttpServletRequest request) {
        Map<String, String> fields = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements(); ) {
            String fieldName = params.nextElement();
            String fieldValue = request.getParameter(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                fields.put(fieldName, fieldValue);
            }
        }

        String vnp_SecureHash = fields.remove("vnp_SecureHash");
        if (fields.containsKey("vnp_SecureHashType")) {
            fields.remove("vnp_SecureHashType");
        }

        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = fields.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                try {
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    if (itr.hasNext()) {
                        hashData.append('&');
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }

        String signValue = VnPayConfig.hmacSHA512(hashSecret, hashData.toString());
        String vnp_ResponseCode = fields.get("vnp_ResponseCode");
        String vnp_TxnRef = fields.get("vnp_TxnRef");

        Map<String, String> result = new HashMap<>();

        if (signValue.equals(vnp_SecureHash)) {
            if ("00".equals(vnp_ResponseCode)) {
                Optional<Payment> paymentOpt = paymentRepository.findByTransactionCode(vnp_TxnRef);
                if (paymentOpt.isPresent()) {
                    Payment payment = paymentOpt.get();
                    Order order = payment.getOrder();
                    if ("PENDING".equals(payment.getStatus())) {
                        payment.setStatus("SUCCESS");
                        order.setStatus("PROCESSING");
                        paymentRepository.save(payment);
                        orderRepository.save(order);

                        if(order.getUser() != null) {
                            orderService.clearUserCart(order.getUser());
                        }
                    }
                }
                result.put("message", "Giao dịch thành công!");
                result.put("status", "success");
            } else {
                Optional<Payment> paymentOpt = paymentRepository.findByTransactionCode(vnp_TxnRef);
                if (paymentOpt.isPresent()) {
                    Payment payment = paymentOpt.get();
                    payment.setStatus("FAILED");
                    paymentRepository.save(payment);
                }
                result.put("message", "Giao dịch thất bại. Mã lỗi: " + vnp_ResponseCode);
                result.put("status", "failed");
            }
        } else {
            result.put("message", "Chữ ký không hợp lệ!");
            result.put("status", "invalid");
        }
        return result;
    }

    @Transactional
    public String processVnPayIpn(HttpServletRequest request) {
        return "{\"RspCode\":\"00\",\"Message\":\"Confirm Success\"}";
    }
}