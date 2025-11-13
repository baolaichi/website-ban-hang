package com.lsb.webshop.API.admin;

import com.lsb.webshop.domain.dto.ChartData;
import com.lsb.webshop.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Controller này chỉ phục vụ việc cung cấp dữ liệu (JSON)
 * cho các biểu đồ (Charts) trên trang Dashboard.
 */
@RestController
@RequestMapping("/api/admin/stats") // (Phân biệt với /admin là trang view)
public class DashboardApiController {

    private final DashboardService dashboardService;

    public DashboardApiController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * API Endpoint cho biểu đồ Doanh thu (Area Chart)
     */
    @GetMapping("/revenue")
    public ResponseEntity<Map<String, List<?>>> getRevenueChartData() {
        List<ChartData> monthlyData = dashboardService.getMonthlyRevenueStats();

        // Tách data thành 2 list: labels (String) và values (Double)
        List<String> labels = monthlyData.stream()
                .map(ChartData::getLabel)
                .toList();
        List<Double> values = monthlyData.stream()
                .map(ChartData::getValue)
                .toList();

        // Trả về JSON có cấu trúc: { "labels": [...], "values": [...] }
        return ResponseEntity.ok(Map.of("labels", labels, "values", values));
    }

    /**
     * API Endpoint cho biểu đồ Đơn hàng (Bar Chart)
     */
    @GetMapping("/orders")
    public ResponseEntity<Map<String, List<?>>> getOrderChartData() {
        List<ChartData> monthlyData = dashboardService.getMonthlyOrderStats();

        List<String> labels = monthlyData.stream()
                .map(ChartData::getLabel)
                .toList();
        List<Double> values = monthlyData.stream()
                .map(ChartData::getValue)
                .toList();

        return ResponseEntity.ok(Map.of("labels", labels, "values", values));
    }
}
