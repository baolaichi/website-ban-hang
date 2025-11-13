package com.lsb.webshop.service;

import com.lsb.webshop.domain.*;
import com.lsb.webshop.repository.*;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
// Đặt @Transactional ở cấp Class
// Mọi hàm public sẽ tự động là 1 Giao dịch (Transaction)
@Transactional
public class OrderService {

    // Tiêm (Inject) tất cả các Repository và Service cần thiết
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final CartRepository cartRepository;
    // (CartDetailRepository có thể không cần nếu đã set cascade,
    // nhưng giữ lại để xóa chi tiết đơn hàng cũ)
    private final CartDetailRepository cartDetailRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final HttpSession session;

    @Autowired
    public OrderService(OrderRepository orderRepository,
                        OrderDetailRepository orderDetailRepository,
                        CartRepository cartRepository,
                        CartDetailRepository cartDetailRepository,
                        UserService userService,
                        UserRepository userRepository,
                        HttpSession session) {
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.cartRepository = cartRepository;
        this.cartDetailRepository = cartDetailRepository;
        this.userService = userService;
        this.userRepository = userRepository;
        this.session = session;
    }

    // ===================================================================
    // ===== CÁC HÀM CHO ADMIN (QUẢN LÝ ĐƠN HÀNG & THỐNG KÊ) =====
    // ===================================================================

    /**
     * [ADMIN] Lấy TẤT CẢ đơn hàng, sắp xếp theo ngày mới nhất
     * (Dùng cho trang /admin/order)
     */
    @Transactional(readOnly = true)
    public List<Order> adminFindAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * [ADMIN] Tìm 1 đơn hàng bất kỳ bằng ID
     * (Dùng cho trang /admin/order/{id})
     */
    @Transactional(readOnly = true)
    public Optional<Order> adminFindOrderById(Long id) {
        return orderRepository.findById(id);
    }

    /**
     * [ADMIN] Cập nhật trạng thái đơn hàng
     * (Dùng cho form /admin/order/update-status)
     */
    public boolean adminUpdateOrderStatus(Long orderId, String status) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            // Logic nghiệp vụ: Không cho phép cập nhật đơn hàng đã kết thúc
            if ("CANCELED".equals(order.getStatus()) || "COMPLETED".equals(order.getStatus())) {
                log.warn("Admin cố gắng cập nhật đơn hàng đã kết thúc (ID: {})", orderId);
                return false;
            }
            order.setStatus(status);
            orderRepository.save(order);
            log.info("Admin đã cập nhật trạng thái đơn hàng {} thành {}", orderId, status);
            return true;
        }
        return false;
    }

    /**
     * [ADMIN] Hủy đơn hàng
     * (Dùng cho form /admin/order/cancel/{id})
     */
    public boolean adminCancelOrder(Long orderId) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            // Logic nghiệp vụ: Không cho phép hủy đơn đã hoàn thành
            if ("COMPLETED".equals(order.getStatus())) {
                log.warn("Admin cố gắng hủy đơn hàng đã hoàn thành (ID: {})", orderId);
                return false;
            }
            // Nếu đơn đã CANCELED rồi thì thôi
            if (!"CANCELED".equals(order.getStatus())) {
                order.setStatus("CANCELED");
                orderRepository.save(order);
                log.info("Admin đã hủy đơn hàng {}", orderId);
                // (Sau này bạn có thể thêm logic hoàn kho (refund inventory) ở đây)
                return true;
            }
            return true; // (Đã hủy từ trước, vẫn tính là thành công)
        }
        return false;
    }

    // ===================================================================
    // ===== CÁC HÀM CHO CLIENT (ĐẶT HÀNG & TRANG TÀI KHOẢN) =====
    // ===================================================================

    /**
     * [CLIENT] Tạo đơn hàng (luôn ở trạng thái PENDING) từ giỏ hàng
     * (Dùng cho /place-order)
     */
    public Order createOrderFromCart(String receiverName, String receiverAddress, String receiverPhone) {
        // 1. Lấy User (đã đăng nhập)
        User currentUser = this.userService.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Không tìm thấy thông tin người dùng. Vui lòng đăng nhập.");
        }

        // 2. Lấy User 'managed' (đang được quản lý bởi JPA)
        User managedUser = this.userService.findByUsername(currentUser.getEmail());
        if(managedUser == null) {
            throw new RuntimeException("Không tìm thấy thông tin người dùng (managed).");
        }

        // 3. Lấy giỏ hàng từ User (dùng liên kết OneToOne)
        Cart cart = managedUser.getCart();
        if (cart == null || cart.getCartDetails() == null || cart.getCartDetails().isEmpty()) {
            throw new RuntimeException("Giỏ hàng của bạn đang rỗng.");
        }

        // 4. Tính tổng tiền (phía server, để bảo mật)
        List<CartDetail> cartDetails = cart.getCartDetails();
        Double totalPrice = 0.0;
        for (CartDetail cd : cartDetails) {
            totalPrice += cd.getPrice() * cd.getQuantity();
        }

        // 5. Tạo và lưu Order (Trạng thái PENDING)
        Order order = new Order();
        order.setUser(managedUser); // Liên kết với User 'managed'
        order.setReceiverName(receiverName);
        order.setReceiverAddress(receiverAddress);
        order.setReceiverPhone(receiverPhone);
        order.setTotalPrice(totalPrice);
        order.setStatus("PENDING");
        // (createdAt được gán tự động bởi @CreationTimestamp trong Order.java)

        Order savedOrder = this.orderRepository.save(order);

        // 6. Sao chép sản phẩm từ CartDetail sang OrderDetail
        for (CartDetail cd : cartDetails) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrder(savedOrder);
            orderDetail.setProduct(cd.getProduct());
            orderDetail.setPrice(cd.getPrice());
            orderDetail.setQuantity(cd.getQuantity());
            this.orderDetailRepository.save(orderDetail);
        }

        // 7. Trả về Order đã lưu (cho Controller)
        return savedOrder;
    }

    /**
     * [CLIENT] Cập nhật trạng thái (dùng cho COD)
     * (Dùng cho /place-order)
     */
    public void updateOrderStatus(Order order, String status) {
        if (order == null) {
            throw new IllegalArgumentException("Đơn hàng không được null");
        }
        // Lấy lại đối tượng 'managed' từ CSDL để cập nhật
        Order managedOrder = this.orderRepository.findById(order.getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng để cập nhật"));

        managedOrder.setStatus(status);
        this.orderRepository.save(managedOrder);
    }

    /**
     * [CLIENT/SYSTEM] Xóa giỏ hàng sau khi đặt hàng thành công
     * (Dùng cho /place-order (COD) và /vnpay-ipn (VNPAY))
     * (Sử dụng cơ chế 'orphanRemoval' đã cấu hình trong User.java)
     */
    public void clearUserCart(User user) {
        if (user == null) {
            log.warn("Không thể xóa giỏ hàng vì User bị null.");
            return;
        }

        // 1. Tải đối tượng User 'managed'
        User managedUser = this.userRepository.findByEmail(user.getEmail())
                .orElse(null);

        if(managedUser == null) {
            log.warn("Không tìm thấy managed user để xóa giỏ hàng: {}", user.getEmail());
            return;
        }

        // 2. Lấy Cart từ User
        Cart cart = managedUser.getCart();

        if (cart != null) {
            // 3. Phá vỡ liên kết từ phía Cha (User)
            // 'orphanRemoval=true' trong User.java sẽ tự động xóa 'Cart' (và 'CartDetail' do cascade)
            managedUser.setCart(null);

            // 4. Lưu lại Cha (User)
            this.userRepository.save(managedUser);

            // 5. Cập nhật session (chỉ ảnh hưởng nếu đây là luồng COD)
            try {
                session.setAttribute("sum", 0);
            } catch (Exception e) {
                log.warn("Không thể cập nhật session (có thể đây là luồng IPN của VNPAY)");
            }

            log.info("Đã xóa giỏ hàng cho user: " + user.getEmail());
        }
    }

    /**
     * [CLIENT] Lấy lịch sử đơn hàng của User
     * (Dùng cho trang /account/orders)
     */
    @Transactional(readOnly = true)
    public List<Order> findOrdersByUser(User user) {
        return orderRepository.findByUserOrderByCreatedAtDesc(user);
    }

    /**
     * [CLIENT] Lấy chi tiết 1 đơn hàng (có kiểm tra ownership)
     * (Dùng cho trang /account/orders/{id})
     */
    @Transactional(readOnly = true)
    public Optional<Order> findOrderByIdAndUser(Long id, User user) {
        return orderRepository.findByIdAndUser(id, user);
    }

    /**
     * [CLIENT] Hủy đơn hàng (có kiểm tra ownership)
     * (Dùng cho trang /account/orders/cancel/{id})
     */
    public boolean cancelOrder(Long orderId, User user) {
        Optional<Order> orderOpt = orderRepository.findByIdAndUser(orderId, user);
        if (orderOpt.isEmpty()) {
            // User cố gắng hủy đơn hàng không phải của mình
            throw new SecurityException("Bạn không có quyền hủy đơn hàng này.");
        }
        Order order = orderOpt.get();
        // Logic nghiệp vụ: Chỉ cho hủy khi đơn còn đang xử lý
        if ("PENDING".equals(order.getStatus()) || "PROCESSING".equals(order.getStatus())) {
            order.setStatus("CANCELED");
            orderRepository.save(order);
            log.info("User {} đã hủy đơn hàng {}", user.getEmail(), orderId);
            return true;
        }
        // Không cho hủy đơn đã/đang giao
        log.warn("User {} cố gắng hủy đơn hàng đã giao (ID: {})", user.getEmail(), orderId);
        return false;
    }

    /**
     * Hàm này đã được thay thế bằng 'adminFindAllOrders'
     */
    @Deprecated
    @Transactional(readOnly = true)
    public List<Order> fetchAllOrders() {
        log.warn("Hàm 'fetchAllOrders' (đã cũ) vừa được gọi. Nên thay thế bằng 'adminFindAllOrders'.");
        return orderRepository.findAllByOrderByCreatedAtDesc(); // Tạm thời trỏ đến hàm mới
    }
}