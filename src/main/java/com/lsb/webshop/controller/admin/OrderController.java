package com.lsb.webshop.controller.admin;
import com.lsb.webshop.domain.Order;
import com.lsb.webshop.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/order") // Đặt mapping gốc
public class OrderController extends BaseController{ // Đổi tên class

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * HÀM 1: Hiển thị danh sách tất cả đơn hàng
     */
    @GetMapping
    public String getOrderListPage(Model model) {
        List<Order> orders = this.orderService.adminFindAllOrders(); // Gọi hàm mới
        model.addAttribute("orders", orders);
        model.addAttribute("title", "Quản lý Đơn hàng");
        return "admin/order/show"; // Trả về view
    }

    /**
     * HÀM 2: Hiển thị trang chi tiết đơn hàng
     */
    @GetMapping("/{id}")
    public String getOrderDetailPage(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Order> orderOpt = this.orderService.adminFindOrderById(id);

        if (orderOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng với ID: " + id);
            return "redirect:/admin/order";
        }

        // Lấy danh sách trạng thái để cho vào dropdown
        List<String> statuses = List.of("PENDING", "PROCESSING", "DELIVERING", "COMPLETED", "CANCELED");

        model.addAttribute("order", orderOpt.get());
        model.addAttribute("statuses", statuses); // (Dùng cho form cập nhật)
        model.addAttribute("title", "Chi tiết Đơn hàng #" + id);
        return "admin/order/detail"; // Trả về view chi tiết
    }

    /**
     * HÀM 3: Xử lý cập nhật trạng thái
     */
    @PostMapping("/update-status")
    public String handleUpdateStatus(@RequestParam("orderId") Long orderId,
                                     @RequestParam("status") String status,
                                     RedirectAttributes redirectAttributes) {

        boolean success = this.orderService.adminUpdateOrderStatus(orderId, status);

        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái đơn hàng thành công!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Cập nhật thất bại (đơn hàng có thể đã kết thúc).");
        }

        return "redirect:/admin/order/" + orderId; // Quay lại trang chi tiết
    }

    /**
     * HÀM 4: Xử lý hủy đơn hàng
     */
    @PostMapping("/cancel/{id}")
    public String handleCancelOrder(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        boolean success = this.orderService.adminCancelOrder(id);

        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "Đã hủy đơn hàng thành công.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể hủy đơn hàng (có thể đã hoàn thành).");
        }

        return "redirect:/admin/order/" + id; // Quay lại trang chi tiết
    }
}