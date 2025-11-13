package com.lsb.webshop.service;

import com.lsb.webshop.config.CacheConfig; // (Import config cache)
import com.lsb.webshop.domain.Category;
import com.lsb.webshop.repository.CategoryRepository;
import com.lsb.webshop.repository.ProductRepository; // (Cần để kiểm tra SP)
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@Transactional // Đặt Transactional ở cấp class
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository; // (Tiêm repo SP)

    @Autowired
    public CategoryService(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    // ===================================================================
    // ===== HÀM ĐỌC (READ) - CÓ CACHING =====
    // ===================================================================

    /**
     * Lấy TẤT CẢ danh mục (Dùng cho Client & Admin)
     * Kết quả được cache vào "ngăn" CACHE_CATEGORIES (sống 6 tiếng)
     */
    @Transactional(readOnly = true)
    @Cacheable(CacheConfig.CACHE_CATEGORIES)
    public List<Category> findAll() {
        log.info("[CategoryService] findAll() - Đang tải từ CSDL...");
        return this.categoryRepository.findAll();
    }

    /**
     * Tìm 1 danh mục theo ID (Dùng cho trang Sửa)
     * Kết quả được cache vào "ngăn" CACHE_CATEGORIES với 1 key duy nhất
     */
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CACHE_CATEGORIES, key = "#id")
    public Optional<Category> findById(Long id) {
        log.info("[CategoryService] findById({}) - Đang tải từ CSDL...", id);
        return this.categoryRepository.findById(id);
    }

    // ===================================================================
    // ===== HÀM GHI (CREATE, UPDATE, DELETE) - CÓ XÓA CACHE =====
    // ===================================================================

    /**
     * Tạo 1 danh mục mới
     * (Xóa toàn bộ cache 'categories' sau khi tạo thành công)
     */
    @CacheEvict(cacheNames = CacheConfig.CACHE_CATEGORIES, allEntries = true)
    public Category createCategory(Category category) {
        // Kiểm tra tên trùng lặp (phía server)
        if (categoryRepository.existsByName(category.getName())) {
            log.warn("[CategoryService] createCategory() - Lỗi: Tên danh mục đã tồn tại '{}'", category.getName());
            throw new IllegalArgumentException("Tên danh mục '" + category.getName() + "' đã tồn tại.");
        }

        try {
            log.info("[CategoryService] createCategory() - Đang lưu danh mục mới: {}", category.getName());
            return categoryRepository.save(category);
        } catch (Exception e) {
            log.error("[CategoryService] createCategory() - Lỗi khi lưu: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi máy chủ khi lưu danh mục.");
        }
    }

    /**
     * Cập nhật 1 danh mục
     * (Xóa toàn bộ cache 'categories' VÀ cache chi tiết 'categories::id')
     */
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.CACHE_CATEGORIES, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.CACHE_CATEGORIES, key = "#id")
    })
    public Category updateCategory(Long id, Category category) {
        // 1. Kiểm tra xem ID có tồn tại không
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục với ID: " + id));

        // 2. Kiểm tra tên trùng lặp (với ID khác)
        Optional<Category> duplicate = categoryRepository.findByNameAndIdNot(category.getName(), id);
        if (duplicate.isPresent()) {
            log.warn("[CategoryService] updateCategory() - Lỗi: Tên danh mục đã tồn tại '{}'", category.getName());
            throw new IllegalArgumentException("Tên danh mục '" + category.getName() + "' đã tồn tại.");
        }

        // 3. Cập nhật và lưu
        existingCategory.setName(category.getName());
        existingCategory.setDescription(category.getDescription());

        try {
            log.info("[CategoryService] updateCategory() - Đang cập nhật danh mục: {}", category.getName());
            return categoryRepository.save(existingCategory);
        } catch (Exception e) {
            log.error("[CategoryService] updateCategory() - Lỗi khi cập nhật: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi máy chủ khi cập nhật danh mục.");
        }
    }

    /**
     * Xóa 1 danh mục
     * (Xóa toàn bộ cache 'categories' VÀ cache chi tiết 'categories::id')
     */
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.CACHE_CATEGORIES, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.CACHE_CATEGORIES, key = "#id")
    })
    public void deleteCategory(Long id) {
        // 1. Kiểm tra xem danh mục có tồn tại không
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục với ID: " + id));

        // 2. (LOGIC QUAN TRỌNG) Kiểm tra xem có sản phẩm nào đang dùng danh mục này không
        long productCount = productRepository.countByCategory(category);
        if (productCount > 0) {
            log.warn("[CategoryService] deleteCategory() - Lỗi: Không thể xóa danh mục '{}' vì đang có {} sản phẩm", category.getName(), productCount);
            throw new IllegalStateException("Không thể xóa danh mục '" + category.getName() + "' vì vẫn còn " + productCount + " sản phẩm thuộc danh mục này. Vui lòng chuyển sản phẩm sang danh mục khác trước khi xóa.");
        }

        // 3. Nếu không, tiến hành xóa
        try {
            log.info("[CategoryService] deleteCategory() - Đang xóa danh mục: {}", category.getName());
            categoryRepository.delete(category);
        } catch (Exception e) {
            log.error("[CategoryService] deleteCategory() - Lỗi khi xóa: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi máy chủ khi xóa danh mục.");
        }
    }


    // (Hàm cũ của bạn, đã được thay thế bằng findAll())
    @Deprecated
    @Transactional(readOnly = true)
    public List<Category> getAllCategorys() {
        log.warn("Hàm 'getAllCategorys' (đã cũ) vừa được gọi. Nên thay thế bằng 'findAll'.");
        return findAll();
    }

    // (Hàm cũ của bạn, đã được thay thế bằng createCategory())
    @Deprecated
    public Category saveCategory(Category category) {
        log.warn("Hàm 'saveCategory' (đã cũ) vừa được gọi. Nên thay thế bằng 'createCategory' hoặc 'updateCategory'.");
        // (Chuyển qua logic kiểm tra cơ bản)
        if(category.getId() == null) {
            return createCategory(category);
        } else {
            return updateCategory(category.getId(), category);
        }
    }
}