package com.lsb.webshop.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.lsb.webshop.domain.dto.ProductDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.lsb.webshop.domain.Cart;
import com.lsb.webshop.domain.CartDetail;
import com.lsb.webshop.domain.Order;
import com.lsb.webshop.domain.OrderDetail;
import com.lsb.webshop.domain.Product;
import com.lsb.webshop.domain.User;
import com.lsb.webshop.repository.CartDetailRepository;
import com.lsb.webshop.repository.CartRepository;
import com.lsb.webshop.repository.OrderDetailRepository;
import com.lsb.webshop.repository.OrderRepository;
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
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final UploadService uploadService;

    public ProductService(ProductRepository productRepository,
                        CartRepository cartRepository,
                        CartDetailRepository cartDetailRepository,
                        UserService userService,
                        OrderRepository orderRepository,
                        OrderDetailRepository orderDetailRepository,
                        UploadService uploadService) {
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
        this.cartDetailRepository = cartDetailRepository;
        this.userService = userService;
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.uploadService = uploadService;
    }

    public Product createProduct(Product product, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("image|Product image is required");
        }

        if (productRepository.existsByName(product.getName())) {
            throw new IllegalArgumentException("name|Tên sản phẩm đã tồn tại");
        }

        String productImg = uploadService.HandleSaveUploadFile(file, "product");
        product.setImage(productImg);

        Product savedProduct = productRepository.save(product);
        log.info("[CreateProduct] Sản phẩm '{}' đã được tạo thành công", savedProduct.getName());
        return savedProduct;
    }

    public Product updateProduct(Long id, Product updatedProduct, MultipartFile file) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("globalError|Không tìm thấy sản phẩm với ID: " + id));

        if (productRepository.existsByNameAndIdNot(updatedProduct.getName(), id)) {
            throw new IllegalArgumentException("name|Tên sản phẩm đã tồn tại");
        }

        if (file != null && !file.isEmpty()) {
            String productImg = uploadService.HandleSaveUploadFile(file, "product");
            updatedProduct.setImage(productImg);
        } else {
            updatedProduct.setImage(existingProduct.getImage());
        }

        updatedProduct.setId(id);
        Product savedProduct = productRepository.save(updatedProduct);
        log.info("[UpdateProduct] Sản phẩm '{}' đã được cập nhật thành công", savedProduct.getName());
        return savedProduct;
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



    public List<ProductDTO> searchProductsDTO(String keyword) {
        if (keyword == null || keyword.trim().length() < 2) {
            return List.of();
        }

        String trimmed = keyword.trim();
        List<Product> results = productRepository.findByNameContainingIgnoreCase(trimmed);

        return results.stream()
                .map(p -> new ProductDTO(p.getId(), p.getName(), p.getShortDesc()))
                .collect(Collectors.toList());
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
                newCartDetail.setQuantity(1L);

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
        return this.cartRepository.findByUserAndStatus(user, false);
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


