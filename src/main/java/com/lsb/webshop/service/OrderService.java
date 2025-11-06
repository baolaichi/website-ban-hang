package com.lsb.webshop.service;

import com.lsb.webshop.domain.*;
import com.lsb.webshop.repository.*; // (Import các repository)
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
// Đặt @Transactional ở cấp Class, tất cả hàm public sẽ là 1 giao dịch
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final CartRepository cartRepository;
    private final CartDetailRepository cartDetailRepository; // (Dù không dùng trực tiếp, vẫn nên tiêm)
    private final UserService userService;
    private final UserRepository userRepository; // (Cần để lưu User trong clearUserCart)
    private final HttpSession session;

    @Autowired
    public OrderService(OrderRepository orderRepository,
                        OrderDetailRepository orderDetailRepository,
                        CartRepository cartRepository,
                        CartDetailRepository cartDetailRepository,
                        UserService userService,
                        UserRepository userRepository, // Thêm
                        HttpSession session) {
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.cartRepository = cartRepository;
        this.cartDetailRepository = cartDetailRepository;
        this.userService = userService;
        this.userRepository = userRepository; // Thêm
        this.session = session;
    }

    // (Các hàm cũ fetchAllOrders, fetchOrderById, deleteOderById giữ nguyên)

    @Transactional(readOnly = true) // Giao dịch chỉ đọc
    public List<Order> fetchAllOrders() {
        return orderRepository.findAll();
    }

    @Transactional(readOnly = true) // Giao dịch chỉ đọc
    public Optional<Order> fetchOrderById(long id) {
        return orderRepository.findById(id);
    }

    public void deleteOderById(long id) {
        // (Giả định Order.java đã thêm cascade=ALL, orphanRemoval=true)
        log.info("delete order id {}", id);
        if (this.orderRepository.existsById(id)) {
            this.orderRepository.deleteById(id);
        } else {
            log.warn("Không tìm thấy Order để xóa, id: {}", id);
        }
    }

    // --- CÁC HÀM MỚI CHO LUỒNG THANH TOÁN ---

    public Order createOrderFromCart(String receiverName, String receiverAddress, String receiverPhone) {
        // Lấy User từ Security Context (Hàm này đã đúng)
        User currentUser = this.userService.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Không tìm thấy thông tin người dùng. Vui lòng đăng nhập.");
        }

        // Tải lại User để đảm bảo nó là 'managed' (được quản lý)
        User managedUser = this.userService.findByUsername(currentUser.getEmail());
        if(managedUser == null) {
            throw new RuntimeException("Không tìm thấy thông tin người dùng (managed).");
        }

        // Lấy giỏ hàng từ User (đã managed)
        Cart cart = managedUser.getCart();
        if (cart == null || cart.getCartDetails() == null || cart.getCartDetails().isEmpty()) {
            throw new RuntimeException("Giỏ hàng của bạn đang rỗng.");
        }

        List<CartDetail> cartDetails = cart.getCartDetails();
        Double totalPrice = 0.0;
        for (CartDetail cd : cartDetails) {
            totalPrice += cd.getPrice() * cd.getQuantity();
        }

        Order order = new Order();
        order.setUser(managedUser); // Quan trọng: Dùng user 'managed'
        order.setReceiverName(receiverName);
        order.setReceiverAddress(receiverAddress);
        order.setReceiverPhone(receiverPhone);
        order.setTotalPrice(totalPrice);
        order.setStatus("PENDING");

        Order savedOrder = this.orderRepository.save(order);

        for (CartDetail cd : cartDetails) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrder(savedOrder);
            orderDetail.setProduct(cd.getProduct());
            orderDetail.setPrice(cd.getPrice());
            orderDetail.setQuantity(cd.getQuantity());
            this.orderDetailRepository.save(orderDetail);
        }
        return savedOrder;
    }

    public void updateOrderStatus(Order order, String status) {
        if (order == null) {
            throw new IllegalArgumentException("Đơn hàng không được null");
        }
        // Tải lại Order để đảm bảo 'managed'
        Order managedOrder = this.orderRepository.findById(order.getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng để cập nhật"));

        managedOrder.setStatus(status);
        this.orderRepository.save(managedOrder);
    }

    /**
     * HÀM NÀY ĐÃ ĐƯỢC CẬP NHẬT
     * Bây giờ nhận tham số User (thay vì lấy từ SecurityContext)
     * để cả COD (có session) và VNPAY IPN (không session) đều gọi được.
     */
    public void clearUserCart(User user) {
        if (user == null) {
            log.warn("Không thể xóa giỏ hàng vì User bị null.");
            return;
        }

        // 1. Tải đối tượng User (persistent) đang được quản lý
        // (Chúng ta cần ID, nhưng tốt hơn là dùng email nếu có)
        User managedUser = this.userRepository.findByEmail(user.getEmail())
                .orElse(null);

        if(managedUser == null) {
            log.warn("Không tìm thấy managed user: {}", user.getEmail());
            return;
        }

        // 2. Lấy Cart từ User
        Cart cart = managedUser.getCart();

        if (cart != null) {
            // 3. Phá vỡ liên kết từ phía Cha (User)
            // 'orphanRemoval=true' trong User.java sẽ tự động xóa Cart
            managedUser.setCart(null);

            // 4. Lưu lại Cha (User)
            this.userRepository.save(managedUser);

            // 5. Cập nhật session (chỉ ảnh hưởng nếu đây là luồng COD)
            try {
                session.setAttribute("sum", 0);
            } catch (Exception e) {
                log.warn("Không thể cập nhật session (có thể đây là luồng IPN)");
            }

            log.info("Đã xóa giỏ hàng cho user: " + user.getEmail());
        }
    }
}