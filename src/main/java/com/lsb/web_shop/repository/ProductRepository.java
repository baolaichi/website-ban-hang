package com.lsb.web_shop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lsb.web_shop.domain.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findById(long id);
    // Tìm sản phẩm theo từ khóa trong tên hoặc mô tả
    List<Product> findByNameContainingIgnoreCase(String name);
}
