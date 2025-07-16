package com.lsb.webshop.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.lsb.webshop.domain.Cart;
import com.lsb.webshop.domain.CartDetail;
import com.lsb.webshop.domain.Product;
import com.lsb.webshop.domain.User;
import com.lsb.webshop.repository.CartDetailRepository;
import com.lsb.webshop.repository.CartRepository;
import com.lsb.webshop.repository.ProductRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final CartDetailRepository cartDetailRepository;
    private final UserService userService;

    public ProductService(ProductRepository productRepository,
                        CartRepository cartRepository,
                        CartDetailRepository cartDetailRepository,
                        UserService userService) {
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
        this.cartDetailRepository = cartDetailRepository;
        this.userService = userService;
    }

    // Lưu hoặc cập nhật sản phẩm
    public Product SaveProduct(Product product) {
        String productName = product.getName();

        try {
            boolean isExisted;

            if (product.getId() != null) {
                isExisted = productRepository.existsByNameAndIdNot(productName, product.getId());
            } else {
                isExisted = productRepository.existsByName(productName);
            }

            if(isExisted){
                log.warn("[SaveProduct] Tên sản phẩm '{}' đã tồn tại", productName);
                throw new IllegalArgumentException("Tên sản phẩm đã tồn tại");
            }

            Product savedProduct = productRepository.save(product);
            log.info("[SaveProduct] Sản phẩm '{}' đã được lưu thành công", savedProduct.getName());
            return savedProduct;
        }catch (IllegalArgumentException e) {
            log.warn("[SaveProduct] Lỗi khi lưu sản phẩm: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[SaveProduct] Lỗi hệ thống khi lưu sản phẩm '{}': {}", productName, e.getMessage(), e);
            throw new RuntimeException("Đã xảy ra lỗi khi lưu sản phẩm", e);
        }
    }

    // Lấy tất cả sản phẩm (kể cả đã xóa mềm)
    public List<Product> getAllProducts() {
        log.debug("[getAllProducts] Đang lấy toàn bộ sản phẩm...");
        List<Product> products = productRepository.findAll();
        log.info("[getAllProducts] Tổng số sản phẩm: {}", products.size());
        return products;
    }

    // Lấy sản phẩm theo ID (kể cả đã xóa mềm)
    public Optional<Product> getByIdProduct(long id) {
        log.debug("[getByIdProduct] Tìm sản phẩm theo ID: {}", id);
        Optional<Product> product = productRepository.findById(id);
        if (product.isPresent()) {
            log.info("[getByIdProduct] Đã tìm thấy sản phẩm: {}", product.get().getName());
        } else {
            log.warn("[getByIdProduct] Không tìm thấy sản phẩm với ID: {}", id);
        }
        return product;
    }

    // Lấy sản phẩm theo ID nhưng chưa bị xóa mềm
    public Optional<Product> getByIdAndNotDeleted(Long id) {
        return productRepository.findByIdAndDeletedFalse(id);
    }

    // Xóa mềm sản phẩm
    @Transactional
    public void softDeleteProduct(Long id) {
        try {
            log.info("Bắt đầu xóa mềm sản phẩm với ID: {}", id);

            Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + id));

            product.setDeleted(true);
            productRepository.save(product);

            log.info("Đã xóa mềm sản phẩm thành công với ID: {}", id);

        } catch (IllegalArgumentException e) {
            log.warn("Lỗi khi xóa mềm sản phẩm: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Lỗi hệ thống khi xóa mềm sản phẩm với ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Đã xảy ra lỗi khi xóa mềm sản phẩm", e);
        }
    }

    // Lấy tất cả sản phẩm chưa bị xóa mềm
    public List<Product> getAllActiveProducts() {
        return productRepository.findByDeletedFalse();
    }

    // Lấy danh sách các nhà sản xuất
    public List<String> getAllFactories() {
        log.debug("[getAllFactories] Đang lấy danh sách nhà sản xuất...");
        List<String> factories = productRepository.findAll()
                .stream()
                .map(Product::getFactory)
                .distinct()
                .collect(Collectors.toList());
        log.info("[getAllFactories] Tìm thấy {} nhà sản xuất: {}", factories.size(), factories);
        return factories;
    }

    // Tìm kiếm sản phẩm theo tên
    public List<Product> searchProducts(String keyword) {
        log.debug("[searchProducts] Tìm kiếm sản phẩm với từ khóa: '{}'", keyword);
        List<Product> results = productRepository.findByNameContainingIgnoreCase(keyword);
        log.info("[searchProducts] Tìm thấy {} sản phẩm với từ khóa '{}'", results.size(), keyword);
        return results;
    }

    public Product getProductById(Long id) {
    Optional<Product> productOptional = productRepository.findById(id);
    if (productOptional.isPresent()) {
        return productOptional.get();
    } else {
        throw new IllegalArgumentException("Không tìm thấy sản phẩm với id = " + id);
    }
    }

    public void addProductToCart(String email, long id, HttpSession session) {
        User user = this.userService.findByUsername(email);
        log.info("[addProductToCart] Người dùng '{}' đang thêm sản phẩm với ID: {}", email, id);
        Cart cart = this.cartRepository.findByUser(user);
        if(cart == null){
            Cart OtheCart = new Cart();
            OtheCart.setUser(user);
            OtheCart.setSum(0);

            cart = this.cartRepository.save(OtheCart);
        }

        Optional<Product> productOptional = this.productRepository.findById(id);
        if(productOptional.isPresent()){
            Product realProduct = productOptional.get();

            CartDetail cartDetail = this.cartDetailRepository.findByCartAndProduct(cart, realProduct);
            log.info("[addProductToCart] Kiểm tra xem sản phẩm đã có trong giỏ hàng hay chưa");
            if(cartDetail == null){
                CartDetail newCartDetail = new CartDetail();
                newCartDetail.setCart(cart);
                newCartDetail.setProduct(realProduct);
                newCartDetail.setPrice(realProduct.getPrice());
                newCartDetail.setQuantity(1);

                this.cartDetailRepository.save(newCartDetail);

                int s = cart.getSum() + 1;
                cart.setSum(s);
                this.cartRepository.save(cart);
                session.setAttribute("sum", s);
            }else{
                log.warn(email + " đã có sản phẩm này trong giỏ hàng, tăng thêm số lượng");
                cartDetail.setQuantity(cartDetail.getQuantity() + 1);
                this.cartDetailRepository.save(cartDetail);
            }
        }

    }

    public Cart fetchByUser(User user){
        return this.cartRepository.findByUser(user);
    }

    @Transactional
public void updateProductQuantity(String email, Long productId, int quantity) {
    if (quantity < 1) {
        throw new IllegalArgumentException("Số lượng phải lớn hơn hoặc bằng 1");
    }

    User user = userService.findByUsername(email);
    if (user == null) {
        throw new IllegalArgumentException("Người dùng không tồn tại");
    }

    Cart cart = cartRepository.findByUser(user);
    if (cart == null) {
        // Nếu chưa có giỏ hàng thì tạo mới
        cart = new Cart();
        cart.setUser(user);
        cart.setSum(0);
        cart = cartRepository.save(cart);
    }

    Optional<Product> productOptional = productRepository.findById(productId);
    if (productOptional.isEmpty()) {
        throw new IllegalArgumentException("Sản phẩm không tồn tại");
    }
    Product product = productOptional.get();

    // Tìm CartDetail (sản phẩm trong giỏ hàng)
    CartDetail cartDetail = cartDetailRepository.findByCartAndProduct(cart, product);
    if (cartDetail == null) {
        // Nếu sản phẩm chưa có trong giỏ hàng thì tạo mới
        cartDetail = new CartDetail();
        cartDetail.setCart(cart);
        cartDetail.setProduct(product);
        cartDetail.setPrice(product.getPrice());
        cartDetail.setQuantity(quantity);
        cartDetailRepository.save(cartDetail);
    } else {
        // Cập nhật lại số lượng sản phẩm
        cartDetail.setQuantity(quantity);
        cartDetailRepository.save(cartDetail);
    }

    if (cart != null) {
    List<CartDetail> cartDetails = cartDetailRepository.findByCart(cart);

    if (cartDetails != null) {
        long totalQuantity = cartDetails.stream()
                .mapToLong(CartDetail::getQuantity)
                .sum();

        cart.setSum((int) totalQuantity);
        cartRepository.save(cart);
    }
}

}

    

    }


