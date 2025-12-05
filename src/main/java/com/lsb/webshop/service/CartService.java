package com.lsb.webshop.service;

import com.lsb.webshop.domain.Cart;
import com.lsb.webshop.domain.CartDetail;
import com.lsb.webshop.domain.Product;
import com.lsb.webshop.domain.User;
import com.lsb.webshop.repository.CartDetailRepository;
import com.lsb.webshop.repository.CartRepository;
import com.lsb.webshop.repository.ProductRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
@Slf4j
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartDetailRepository cartDetailRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserService userService;

    /**
     * Thêm sản phẩm vào giỏ hàng
     * (Được gọi bởi ItemController - hàm addProductToCart)
     */
    public boolean addProductToCart(HttpServletRequest request, long productId) {
        // 1. Lấy User hiện tại (từ Spring Security - CHUẨN)
        User user = userService.getCurrentUser();

        // 2. Kiểm tra User và Sản phẩm
        Optional<Product> productOpt = productRepository.findById(productId);

        if (user == null || productOpt.isEmpty()) {
            return false; // Chưa đăng nhập hoặc SP không tồn tại
        }

        // Tải lại User 'managed' để đảm bảo tính nhất quán của JPA
        User managedUser = userService.findByUsername(user.getEmail());
        Product product = productOpt.get();

        // 3. Tìm hoặc Tạo giỏ hàng
        Cart cart = managedUser.getCart(); // Dùng quan hệ OneToOne từ User

        if (cart == null) {
            cart = new Cart();
            cart.setUser(managedUser);
            cart.setSum(0);
            cart.setStatus(true);
            cart = cartRepository.save(cart);
        }

        // 4. Kiểm tra xem sản phẩm đã có trong giỏ hàng chưa
        Optional<CartDetail> existingDetailOpt = cartDetailRepository.findByCartAndProduct(cart, product);

        if (existingDetailOpt.isPresent()) {
            // Đã có -> Tăng số lượng lên 1
            CartDetail detail = existingDetailOpt.get();
            detail.setQuantity(detail.getQuantity() + 1);
            cartDetailRepository.save(detail);
        } else {
            // Chưa có -> Tạo chi tiết mới
            CartDetail newDetail = new CartDetail();
            newDetail.setCart(cart);
            newDetail.setProduct(product);
            newDetail.setPrice(product.getPrice());
            newDetail.setQuantity(1); // Số lượng ban đầu là 1
            cartDetailRepository.save(newDetail);
        }

        // 5. Cập nhật session 'sum' (để hiển thị số lượng trên header ngay lập tức)
        HttpSession session = request.getSession();
        updateSessionSum(session, managedUser);

        return true;
    }

    /**
     * Xử lý cập nhật số lượng sản phẩm (AJAX)
     * (Được gọi bởi ItemController - hàm updateQuantity)
     */
    public Map<String, Object> handleUpdateQuantity(HttpServletRequest request, Long productId, int quantity) {
        Map<String, Object> response = new HashMap<>();

        // Lấy User từ Security Context
        User user = userService.getCurrentUser();

        if (user == null) {
            response.put("status", HttpStatus.UNAUTHORIZED);
            response.put("message", "Bạn cần đăng nhập để cập nhật giỏ hàng");
            return response;
        }

        if (quantity < 1) {
            response.put("status", HttpStatus.BAD_REQUEST);
            response.put("message", "Số lượng phải lớn hơn 0");
            return response;
        }

        // Tải User managed và Cart
        User managedUser = userService.findByUsername(user.getEmail());
        Cart cart = managedUser.getCart();
        Optional<Product> productOpt = productRepository.findById(productId);

        if (cart == null || productOpt.isEmpty()) {
            response.put("status", HttpStatus.NOT_FOUND);
            response.put("message", "Giỏ hàng hoặc sản phẩm không tồn tại");
            return response;
        }

        // Tìm chi tiết giỏ hàng cần sửa
        Optional<CartDetail> detailOpt = cartDetailRepository.findByCartAndProduct(cart, productOpt.get());

        if (detailOpt.isPresent()) {
            try {
                // Cập nhật số lượng mới
                CartDetail detail = detailOpt.get();
                detail.setQuantity(quantity);
                cartDetailRepository.save(detail);

                // Tính lại tổng tiền mới của toàn bộ giỏ hàng để trả về cho giao diện
                double newTotalPrice = 0;
                List<CartDetail> allDetails = cart.getCartDetails(); // Lấy lại list mới nhất (do @Transactional)
                for (CartDetail d : allDetails) {
                    newTotalPrice += d.getPrice() * d.getQuantity();
                }

                // Cập nhật session sum (vì tổng số lượng thay đổi)
                updateSessionSum(request.getSession(), managedUser);

                response.put("status", HttpStatus.OK);
                response.put("message", "Cập nhật thành công");
                response.put("totalPrice", newTotalPrice);
                return response;

            } catch (Exception e) {
                log.error("[CartService] Lỗi khi cập nhật: {}", e.getMessage());
                response.put("status", HttpStatus.INTERNAL_SERVER_ERROR);
                return response;
            }
        }

        response.put("status", HttpStatus.NOT_FOUND);
        return response;
    }

    /**
     * Xóa sản phẩm khỏi giỏ hàng
     * (Được gọi bởi ItemController - hàm deleteProductFromCart)
     */
    public void removeProductCart(long cartDetailId, HttpSession session) {
        User user = userService.getCurrentUser();
        if (user == null) return;

        User managedUser = userService.findByUsername(user.getEmail());
        Cart cart = managedUser.getCart();

        if (cart == null) return;

        Optional<CartDetail> detailOpt = cartDetailRepository.findById(cartDetailId);

        if (detailOpt.isPresent()) {
            CartDetail detail = detailOpt.get();

            // ===== BẮT ĐẦU SỬA LỖI =====

            // Sử dụng Objects.equals để so sánh an toàn cho cả Long và long
            // Hoặc nếu id của bạn là 'long' (nguyên thủy), dùng '=='

            Long cartIdFromDetail = detail.getCart().getId();
            Long currentCartId = cart.getId();

            if (Objects.equals(cartIdFromDetail, currentCartId)) {

                // 1. Xóa khỏi danh sách của Cart (quan trọng cho Hibernate)
                cart.getCartDetails().remove(detail);

                // 2. Xóa khỏi CSDL
                cartDetailRepository.delete(detail);

                // 3. Cập nhật session
                int newSum = 0;
                // (Lưu ý: cart.getCartDetails() lúc này đã mất 1 phần tử trong bộ nhớ)
                for (CartDetail cd : cart.getCartDetails()) {
                    newSum += cd.getQuantity();
                }
                session.setAttribute("sum", newSum);
            }

            // ===== KẾT THÚC SỬA LỖI =====
        }
    }

    /**
     * Hàm phụ trợ (Helper): Cập nhật tổng số lượng sản phẩm vào Session
     * Biến session 'sum' được dùng để hiển thị số nhỏ trên icon giỏ hàng ở Header
     */
    private void updateSessionSum(HttpSession session, User user) {
        if (session != null && user != null) {
            Cart cart = cartRepository.findByUser(user);
            int sum = 0;
            if (cart != null && cart.getCartDetails() != null) {
                // Tính tổng số lượng tất cả sản phẩm
                for (CartDetail cd : cart.getCartDetails()) {
                    sum += cd.getQuantity();
                }
            }
            session.setAttribute("sum", sum);
        }
    }

    // (Hàm cũ này có thể giữ lại nếu bạn dùng form confirm checkout,
    // nhưng với logic AJAX hiện tại thì ít dùng hơn)
    public void updateCartBeforeCheckout(List<CartDetail> cartDetails) {
        for(CartDetail cartDetail : cartDetails){
            Optional<CartDetail> cdOptional = this.cartDetailRepository.findById(cartDetail.getId());
            if(cdOptional.isPresent()){
                CartDetail currentCartDetail = cdOptional.get();
                currentCartDetail.setQuantity(cartDetail.getQuantity());
                this.cartDetailRepository.save(currentCartDetail);
            }
        }
    }
}