package com.lsb.webshop.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.lsb.webshop.domain.Product;
import com.lsb.webshop.repository.ProductRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
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

}
