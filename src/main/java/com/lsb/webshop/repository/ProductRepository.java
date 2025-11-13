package com.lsb.webshop.repository;

import java.util.List;
import java.util.Optional;

import com.lsb.webshop.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param; // Thêm import

import com.lsb.webshop.domain.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findById(long id);
    // Tìm sản phẩm theo từ khóa trong tên hoặc mô tả
    List<Product> findByNameContainingIgnoreCase(String name);

    // Trả về sản phẩm chưa bị xóa mềm
    @Query("SELECT p FROM Product p WHERE p.deleted = false")
    List<Product> findAllAvailable();

    // Nếu có thêm điều kiện
    @Query("SELECT p FROM Product p WHERE p.id = :id AND p.deleted = false")
    Product findByIdAndNotDeleted(Long id);

    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, Long id);

    Optional<Product> findByIdAndDeletedFalse(Long id);

    // Lọc danh sách sản phẩm chưa bị xóa
    List<Product> findByDeletedFalse();

    Optional<Product> findFirstByNameContainingIgnoreCase(String name);

    List<Product> findTop5ByCategoryAndIdNot(Category category, Long productId, Pageable pageable);

    @Query(value =
            " SELECT p.* FROM products p " +
                    " JOIN ratings r ON p.id = r.product_id " +
                    " WHERE " +
                    "   r.score >= :limit_score AND r.user_id IN ( " +
                    "       SELECT DISTINCT r2.user_id FROM ratings r2 " + // 1. Tìm User giống bạn
                    "       WHERE r2.user_id != :userId AND r2.product_id IN ( " +
                    "           SELECT r3.product_id FROM ratings r3 " + // 2. (Những sản phẩm bạn thích)
                    "           WHERE r3.user_id = :userId AND r3.score >= :limit_score " +
                    "       ) " +
                    "   ) " +
                    "   AND p.id NOT IN ( " +
                    "       SELECT r4.product_id FROM ratings r4 " + // 3. (Trừ SP bạn đã đánh giá)
                    "       WHERE r4.user_id = :userId " +
                    "   ) " +
                    " GROUP BY p.id " +
                    " ORDER BY COUNT(p.id) DESC, p.sold DESC " + // 4. Sắp xếp theo độ "hot"
                    " LIMIT :result_limit ",
            nativeQuery = true)
    List<Product> findRecommendedProductsForUser(
            @Param("userId") Long userId,
            @Param("limit_score") int limit_score,
            @Param("result_limit") int result_limit
    );

    // Dùng cho Dashboard: Lấy top 5 sản phẩm bán chạy nhất
    List<Product> findTop5ByOrderBySoldDesc();

    /**
     * [QUẢN LÝ] Lấy danh sách các hãng sản xuất (factory)
     */
    @Query("SELECT DISTINCT p.factory FROM Product p WHERE p.factory IS NOT NULL AND p.factory != ''")
    List<String> findAllFactories();


    Optional<Product> findByNameAndIdNot(String name, Long id);

    long countByCategory(Category category);
}
