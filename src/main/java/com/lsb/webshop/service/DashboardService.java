package com.lsb.webshop.service;

import com.lsb.webshop.domain.Product;
import com.lsb.webshop.domain.dto.ChartData;
import com.lsb.webshop.repository.OrderRepository;
import com.lsb.webshop.repository.ProductRepository;
import com.lsb.webshop.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true) // Hầu hết các hàm ở đây chỉ đọc
public class DashboardService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public DashboardService(OrderRepository orderRepository,
                            ProductRepository productRepository,
                            UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    /**
     * Lấy 3 số liệu thống kê chính cho Dashboard
     */
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // 1. Tổng doanh thu (chỉ tính đơn COMPLETED)
        Double totalRevenue = orderRepository.sumTotalPriceByStatusCompleted();
        stats.put("totalRevenue", totalRevenue != null ? totalRevenue : 0.0);

        // 2. Đơn hàng mới (PENDING + PROCESSING)
        Long newOrdersCount = orderRepository.countNewOrders();
        stats.put("newOrdersCount", newOrdersCount != null ? newOrdersCount : 0L);

        // 3. Tổng số khách hàng (Role "USER")
        Long totalCustomers = userRepository.countByRole_Name("USER");
        stats.put("totalCustomers", totalCustomers != null ? totalCustomers : 0L);

        return stats;
    }

    /**
     * Lấy Top 5 sản phẩm bán chạy nhất
     */
    public List<Product> getBestSellingProducts() {
        return productRepository.findTop5ByOrderBySoldDesc();
    }

    public List<ChartData> getMonthlyRevenueStats() {
        return orderRepository.getMonthlyRevenueStats();
    }

    public List<ChartData> getMonthlyOrderStats() {
        return orderRepository.getMonthlyOrderStats();
    }

}