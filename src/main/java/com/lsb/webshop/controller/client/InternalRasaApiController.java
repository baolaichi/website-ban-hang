package com.lsb.webshop.controller.client;


import com.lsb.webshop.domain.Order;
import com.lsb.webshop.domain.Product;
import com.lsb.webshop.repository.OrderRepository;
import com.lsb.webshop.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/internal-rasa")
public class InternalRasaApiController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    /**
     * API này được gọi bởi ActionGetPrice trong Python
     * GET /api/internal-rasa/product-price?name=iPhone 15
     */
    @GetMapping("/product-price")
    public ResponseEntity<?> getProductPrice(@RequestParam String name) {
        // Trả về danh sách sản phẩm chứa tên đó
        List<Product> products = productRepository.findByNameContainingIgnoreCase(name);

        if (!products.isEmpty()) {
            // Lấy sản phẩm đầu tiên (hoặc có thể lọc thêm nếu bạn muốn)
            Product product = products.get(0);
            return ResponseEntity.ok(Map.of("price", product.getPrice()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    /**
     * API này được gọi bởi ActionGetOrderStatus trong Python
     * GET /api/internal-rasa/order-status/12345
     */
    @GetMapping("/order-status/{orderId}")
    public ResponseEntity<?> getOrderStatus(@PathVariable String orderId) {
        try {
            Long id = Long.parseLong(orderId);
            Optional<Order> order = orderRepository.findById(id);

            if (order.isPresent()) {
                // Trả về JSON: { "status": "Đang giao hàng" }
                // Bạn nên dịch trạng thái (ví dụ: PROCESSING -> "Đang xử lý")
                return ResponseEntity.ok(Map.of("status", order.get().getStatus()));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (NumberFormatException e) {
            // Xử lý nếu user gõ "abc" thay vì số
            return ResponseEntity.badRequest().body("Mã đơn hàng không hợp lệ");
        }
    }
}
