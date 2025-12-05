package com.lsb.webshop.controller.admin;

import com.lsb.webshop.domain.Product;
import com.lsb.webshop.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // <-- Sửa: Dùng Model
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
// (Xóa ModelAndView)

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin") // <-- Đặt mapping gốc cho controller
public class DashBoardController extends BaseController{

    private final DashboardService dashboardService;

    public DashBoardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping // <-- Sửa: Dùng @GetMapping (cho /admin)
    public String getDashboard(Model model) { // <-- Sửa: Dùng Model

        // 1. Lấy các số liệu thống kê
        Map<String, Object> stats = dashboardService.getDashboardStats();
        model.addAttribute("totalRevenue", stats.get("totalRevenue"));
        model.addAttribute("newOrdersCount", stats.get("newOrdersCount"));
        model.addAttribute("totalCustomers", stats.get("totalCustomers"));

        // 2. Lấy sản phẩm bán chạy
        List<Product> bestSellingProducts = dashboardService.getBestSellingProducts();
        model.addAttribute("bestSellingProducts", bestSellingProducts);

        // 3. Đặt tiêu đề cho trang
        model.addAttribute("title", "Dashboard");

        // 4. Trả về tên view
        return "admin/dashboard/show"; // <-- Sửa: Trả về String
    }
}