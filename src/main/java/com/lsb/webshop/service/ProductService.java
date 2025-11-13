package com.lsb.webshop.service;

// (Import các thư viện cũ)
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.lsb.webshop.domain.dto.ProductDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import com.lsb.webshop.domain.Cart;
import com.lsb.webshop.domain.CartDetail;
import com.lsb.webshop.domain.Order;
import com.lsb.webshop.domain.OrderDetail;
import com.lsb.webshop.domain.Product;
import com.lsb.webshop.domain.Category;
import com.lsb.webshop.domain.User;
import com.lsb.webshop.repository.CartDetailRepository;
import com.lsb.webshop.repository.CartRepository;
import com.lsb.webshop.repository.OrderDetailRepository;
import com.lsb.webshop.repository.OrderRepository;
import com.lsb.webshop.repository.ProductRepository;
import java.util.Collections;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

// ===== BẮT ĐẦU CÁC IMPORT MỚI CHO CACHING =====
import com.lsb.webshop.config.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
// ===== KẾT THÚC CÁC IMPORT MỚI =====

@Slf4j
@Service
@Transactional // Đặt Transactional ở cấp class
public class ProductService {

    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final CartDetailRepository cartDetailRepository;
    private final UserService userService;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final UploadService uploadService;

    // (Constructor của bạn đã đúng, không cần @Autowired cho constructor)
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

    /**
     * EVICT (XÓA CACHE):
     * Khi tạo 1 sản phẩm mới, tất cả cache liên quan đến danh sách sản phẩm
     * (products, categories, recommend) phải bị XÓA.
     */
    @CacheEvict(cacheNames = {
            CacheConfig.CACHE_PRODUCTS,
            CacheConfig.CACHE_CATEGORIES,
            CacheConfig.CACHE_RECOMMEND
    }, allEntries = true)
    public Product createProduct(Product product, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("image|Product image is required");
        }

        // (Giả sử bạn đã có hàm 'existsByName' trong Repository)
        // if (productRepository.existsByName(product.getName())) {
        //     throw new IllegalArgumentException("name|Tên sản phẩm đã tồn tại");
        // }

        String productImg = uploadService.HandleSaveUploadFile(file, "product");
        product.setImage(productImg);

        Product savedProduct = productRepository.save(product);
        log.info("[CreateProduct] Sản phẩm '{}' đã được tạo thành công", savedProduct.getName());
        return savedProduct;
    }

    /**
     * EVICT (XÓA CACHE):
     * Khi cập nhật sản phẩm, XÓA cache danh sách (allEntries = true)
     * VÀ XÓA cache chi tiết (key = "#id") của chính nó.
     */
    @Caching(evict = {
            @CacheEvict(cacheNames = {CacheConfig.CACHE_PRODUCTS, CacheConfig.CACHE_CATEGORIES, CacheConfig.CACHE_RECOMMEND}, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.CACHE_PRODUCT, key = "#id")
    })
    public Product updateProduct(Long id, Product updatedProduct, MultipartFile file) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("globalError|Không tìm thấy sản phẩm với ID: " + id));

        // (Giả sử bạn đã có hàm 'existsByNameAndIdNot' trong Repository)
        // if (productRepository.existsByNameAndIdNot(updatedProduct.getName(), id)) {
        //     throw new IllegalArgumentException("name|Tên sản phẩm đã tồn tại");
        // }

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

    /**
     * CACHED: Cache danh sách (kể cả đã xóa) cho Admin.
     * key = "'allWithDeleted'" để phân biệt với cache 'getAllActiveProducts'
     */
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CACHE_PRODUCTS, key = "'allWithDeleted'")
    public List<Product> getAllProducts() {
        log.debug("[getAllProducts] Đang lấy toàn bộ sản phẩm...");
        List<Product> products = productRepository.findAll();
        log.info("[getAllProducts] Tổng số sản phẩm: {}", products.size());
        return products;
    }

    /**
     * CACHED: Cache chi tiết 1 SP (kể cả đã xóa).
     * key = "#id" (ví dụ: product::1)
     */
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CACHE_PRODUCT, key = "#id")
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

    /**
     * CACHED: Cache chi tiết 1 SP (chỉ active).
     * key = "'active:' + #id" để phân biệt với cache 'getByIdProduct'
     */
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CACHE_PRODUCT, key = "'active:' + #id")
    public Optional<Product> getByIdAndNotDeleted(Long id) {
        // (Giả sử bạn có hàm này trong Repository)
        // return productRepository.findByIdAndDeletedFalse(id);

        // (Nếu không, dùng tạm logic này)
        Optional<Product> product = productRepository.findById(id);
        if (product.isPresent() && product.get().isDeleted()) {
            return Optional.empty(); // Trả về rỗng nếu đã bị xóa
        }
        return product;
    }

    /**
     * EVICT (XÓA CACHE):
     * Khi xóa mềm sản phẩm, XÓA cache danh sách
     * VÀ XÓA CẢ 2 cache chi tiết (full và active).
     */
    @Caching(evict = {
            @CacheEvict(cacheNames = {CacheConfig.CACHE_PRODUCTS, CacheConfig.CACHE_CATEGORIES, CacheConfig.CACHE_RECOMMEND}, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.CACHE_PRODUCT, key = "#id"),
            @CacheEvict(cacheNames = CacheConfig.CACHE_PRODUCT, key = "'active:' + #id")
    })
    public void softDeleteProduct(Long id) {
        try {
            log.info("Bắt đầu xóa mềm sản phẩm với ID: {}", id);
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + id));

            product.setDeleted(true);
            productRepository.save(product);
            log.info("Đã xóa mềm sản phẩm thành công với ID: {}", id);
        } catch (Exception e) {
            log.error("Lỗi hệ thống khi xóa mềm sản phẩm với ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Đã xảy ra lỗi khi xóa mềm sản phẩm", e);
        }
    }

    /**
     * CACHED: Đây là cache chính cho Trang chủ.
     */
    @Transactional(readOnly = true)
    @Cacheable(CacheConfig.CACHE_PRODUCTS)
    public List<Product> getAllActiveProducts() {
        // (Giả sử bạn có hàm này trong Repository)
        // return productRepository.findByDeletedFalse();

        // (Nếu không, dùng tạm logic này)
        return productRepository.findAll().stream()
                .filter(p -> !p.isDeleted())
                .collect(Collectors.toList());
    }

    /**
     * CACHED: Cache danh sách hãng (ít thay đổi).
     */
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CACHE_PRODUCTS, key = "'allFactories'")
    public List<String> getAllFactories() {
        log.debug("[getAllFactories] Đang lấy danh sách nhà sản xuất...");
        // (Logic cũ của bạn đã đúng)
        List<String> factories = productRepository.findAll()
                .stream()
                .map(Product::getFactory)
                .distinct()
                .collect(Collectors.toList());
        log.info("[getAllFactories] Tìm thấy {} nhà sản xuất: {}", factories.size(), factories);
        return factories;
    }


    // (KHÔNG CACHE: Tìm kiếm theo keyword vì có quá nhiều biến thể)
    @Transactional(readOnly = true)
    public List<ProductDTO> searchProductsDTO(String keyword) {
        if (keyword == null || keyword.trim().length() < 2) {
            return List.of();
        }
        String trimmed = keyword.trim();
        // (Giả sử bạn có hàm này trong Repository)
        // List<Product> results = productRepository.findByNameContainingIgnoreCase(trimmed);

        // (Nếu không, dùng tạm logic này)
        List<Product> results = productRepository.findAll().stream()
                .filter(p -> p.getName().toLowerCase().contains(trimmed.toLowerCase()))
                .collect(Collectors.toList());

        return results.stream()
                .map(p -> new ProductDTO(p.getId(), p.getName(), p.getShortDesc()))
                .collect(Collectors.toList());
    }

    // (Duplicate của getByIdProduct, vẫn thêm cache)
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CACHE_PRODUCT, key = "#id")
    public Product getProductById(Long id) {
        Optional<Product> productOptional = productRepository.findById(id);
        if (productOptional.isPresent()) {
            return productOptional.get();
        } else {
            throw new IllegalArgumentException("Không tìm thấy sản phẩm với id = " + id);
        }
    }

    // (KHÔNG CACHE: Các hàm liên quan đến Giỏ hàng (Cart)
    // vì chúng mang tính cá nhân (session-scoped) và thay đổi liên tục)
    public void addProductToCart(String email, long id, HttpSession session) {
        // (Logic của bạn giữ nguyên)
// ... (code)
    }

    @Transactional(readOnly = true) // fetchByUser là (readOnly)
    public Cart fetchByUser(User user){
        // (Giả sử bạn có hàm này trong Repository)
        // return this.cartRepository.findByUserAndStatus(user, false);

        // (Nếu không, dùng tạm logic này)
        return this.cartRepository.findByUser(user);
    }

    @Transactional
    public void updateProductQuantity(String email, Long productId, int quantity) {
        // (Logic của bạn giữ nguyên)
// ... (code)
    }


    /**
     * CACHED (Sống lâu): Cache danh sách "Sản phẩm tương tự".
     * key = "'similar:' + #product.id"
     */
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CACHE_PRODUCTS, key = "'similar:' + #product.id")
    public List<Product> getSimilarProducts(Product product) {
        if (product == null || product.getCategory() == null) {
            return Collections.emptyList();
        }
        Pageable pageable = PageRequest.of(0, 5, Sort.by("sold").descending());

        // (Giả sử bạn có hàm này trong Repository)
        // return productRepository.findTop5ByCategoryAndIdNot(
        //         product.getCategory(),
        //         product.getId(),
        //         pageable
        // );

        // (Nếu không, dùng tạm logic này)
        return productRepository.findAll().stream()
                .filter(p -> p.getCategory() != null &&
                        p.getCategory().getId().equals(product.getCategory().getId()) &&
                        !p.getId().equals(product.getId()))
                .limit(5)
                .collect(Collectors.toList());
    }

    /**
     * CACHED (Sống ngắn): Cache kết quả gợi ý (AI).
     * key = "'user:' + (#user != null ? #user.id : 'guest')"
     */
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CACHE_RECOMMEND, key = "'user:' + (#user != null ? #user.id : 'guest')")
    public List<Product> getRecommendedProducts(User user) {
        if (user == null) {
            Pageable pageable = PageRequest.of(0, 5, Sort.by("sold").descending());
            return productRepository.findAll(pageable).getContent();
        }

        // (Giả sử bạn có hàm này trong Repository)
        // return productRepository.findRecommendedProductsForUser(user.getId(), 4, 5);

        // (Nếu không, dùng tạm logic này - trả về bán chạy nhất)
        Pageable pageable = PageRequest.of(0, 5, Sort.by("sold").descending());
        return productRepository.findAll(pageable).getContent();
    }
}