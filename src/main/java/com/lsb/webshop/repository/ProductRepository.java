package com.lsb.webshop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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


}
