package com.lsb.webshop.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lsb.webshop.domain.Category;
import com.lsb.webshop.repository.CategoryRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    public Category saveCategory(Category category) {
        try {
            log.info("[CategoryService] saveCategory() - Saving category: {}", category.getName());
            return categoryRepository.save(category);
        } catch (Exception e) {
            log.error("[CategoryService] saveCategory() - Error saving category: {}", e.getMessage(), e);
            return null; // trả null nếu lỗi
        }
    }

    public List<Category> getAllCategorys() {
        log.info("[CategoryService] getAll() - Fetching all categories");
        return categoryRepository.findAll();
    }
}
