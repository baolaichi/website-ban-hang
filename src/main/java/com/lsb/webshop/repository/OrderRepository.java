package com.lsb.webshop.repository;

import com.lsb.webshop.domain.User;
import com.lsb.webshop.domain.dto.ChartData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.lsb.webshop.domain.Order;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>{

    List<Order> findByUserOrderByCreatedAtDesc(User user);

    Optional<Order> findByIdAndUser(Long id, User user);

    // 1. Dùng cho Dashboard: Đếm đơn hàng mới (PENDING hoặc PROCESSING)
    Long countByStatus(String status);

    // (Hoặc đếm nhiều trạng thái)
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'PENDING' OR o.status = 'PROCESSING'")
    Long countNewOrders();

    // 2. Dùng cho Dashboard: Tính tổng doanh thu (chỉ đơn hàng đã HOÀN THÀNH)
    @Query("SELECT SUM(o.totalPrice) FROM Order o WHERE o.status = 'COMPLETED'")
    Double sumTotalPriceByStatusCompleted();

    // 3. Dùng cho Admin Order List: Tìm tất cả và sắp xếp
    List<Order> findAllByOrderByCreatedAtDesc();

    @Query(value = "SELECT " +
            "    DATE_FORMAT(o.created_at, '%Y-%m') as label, " +
            "    SUM(o.total_price) as value " +
            "FROM orders o " +
            "WHERE o.status = 'COMPLETED' " +
            "GROUP BY label " +
            "ORDER BY label ASC LIMIT 12",
            nativeQuery = true)
    List<ChartData> getMonthlyRevenueStats();

    /**
     * Lấy Số lượng đơn hàng (tất cả trạng thái) của 12 tháng gần nhất
     * Trả về một List các đối tượng ChartData (gồm label và value)
     */
    @Query(value = "SELECT " +
            "    DATE_FORMAT(o.created_at, '%Y-%m') as label, " +
            "    COUNT(o.id) as value " +
            "FROM orders o " +
            "GROUP BY label " +
            "ORDER BY label ASC LIMIT 12",
            nativeQuery = true)
    List<ChartData> getMonthlyOrderStats();
}
