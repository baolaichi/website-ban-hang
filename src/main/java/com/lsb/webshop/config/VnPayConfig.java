package com.lsb.webshop.config;

import org.springframework.stereotype.Component;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Component
public class VnPayConfig {

    // Hàm này dùng để tạo chữ ký HmacSHA512
    public static String hmacSHA512(final String key, final String data) {
        try {
            if (key == null || data == null) {
                throw new NullPointerException();
            }
            final Mac hmac512 = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes(StandardCharsets.UTF_8);
            final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);

            // Chuyển byte array sang hex string
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();

        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tạo chữ ký HMAC-SHA512", e);
        }
    }

    // Hàm này dùng để tạo hash data từ một Map các tham số
    public static String hashAllFields(Map<String, String> fields, String hashSecret) {
        // Sắp xếp các trường theo thứ tự alphabet
        SortedMap<String, String> sortedFields = new TreeMap<>(fields);

        // Tạo chuỗi hash data
        String hashData = sortedFields.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));

        return hmacSHA512(hashSecret, hashData);
    }

    // Hàm tạo số ngẫu nhiên cho vnp_TxnRef
    public static String getRandomNumber(int len) {
        Random rnd = new Random();
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }
}