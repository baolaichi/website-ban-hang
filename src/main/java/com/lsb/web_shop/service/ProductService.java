package com.lsb.web_shop.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.lsb.web_shop.domain.Product;
import com.lsb.web_shop.repository.ProductRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product handlProduct(Product product) {
        log.info("Gọi handlProduct với product: {}", product);
        Product newProduct = this.productRepository.save(product);
        log.info("Đã lưu product: {}", newProduct);
        return newProduct;
    }

    public List<Product> getAllProducts() {
        log.info("Gọi getAllProducts()");
        return this.productRepository.findAll();
    }

    public Optional<Product> getByIdProduct(long id) {
        log.info("Gọi getByIdProduct với id: {}", id);
        return this.productRepository.findById(id);
    }

    public void deleteProduct(Long id) {
        log.info("Gọi deleteProduct với id: {}", id);
        productRepository.deleteById(id);
        log.info("Đã xóa product với id: {}", id);
    }

    public List<String> getAllFactories() {
        log.info("Gọi getAllFactories()");
        List<String> factories = productRepository.findAll()
            .stream()
            .map(Product::getFactory)
            .distinct()
            .collect(Collectors.toList());
        log.info("Danh sách factory: {}", factories);
        return factories;
    }

    public List<Product> searchProducts(String keyword) {
        log.info("Gọi searchProducts với từ khóa: {}", keyword);
        List<Product> results = productRepository.findByNameContainingIgnoreCase(keyword);
        log.info("Số sản phẩm tìm thấy: {}", results.size());
        return results;
    }
}
