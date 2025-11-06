package com.lsb.webshop.API.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lsb.webshop.domain.Category;
import com.lsb.webshop.service.CategoryService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/categories")
@Slf4j
public class CategoryApi {
    
    private final CategoryService categoryService;

    public CategoryApi(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping("/add")
    public ResponseEntity<Category> createCategory(@RequestBody Category category) {
        Category savedCategory = categoryService.saveCategory(category);
        return ResponseEntity.ok(savedCategory);
    }
}
