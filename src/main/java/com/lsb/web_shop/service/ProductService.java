package com.lsb.web_shop.service;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.lsb.web_shop.domain.Product;
import com.lsb.web_shop.repository.ProductRepository;


@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product handlProduct(Product product) {
        Product newProduct = this.productRepository.save(product);
        return newProduct;
    }

    public List<Product> getAllProducts() {
        return this.productRepository.findAll();
    }

    public Optional<Product> getByIdProduct(long id) {
        return this.productRepository.findById(id);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    public List<String> getAllFactories() {
    return productRepository.findAll()
            .stream()
            .map(Product::getFactory)
            .distinct()
            .collect(Collectors.toList());
}

public List<Product> searchProducts(String keyword) {
    return productRepository.findByNameContainingIgnoreCase(keyword);
}

}
