package com.lsb.webshop.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.lsb.webshop.domain.Category;
import com.lsb.webshop.service.CategoryService;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/admin/categories")
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/add")
    public String showAddCategoryForm(Model model) {
        model.addAttribute("category", new Category());
        return "admin/category/add";  // Tên template Thymeleaf: templates/category/add.html
    }

    @PostMapping("/add")
    public String saveCategory(@ModelAttribute Category category,
                               RedirectAttributes redirectAttributes) {
        try {
            Category saved = categoryService.saveCategory(category);
            redirectAttributes.addFlashAttribute("success", "Thêm danh mục thành công: " + saved.getName());
            return "redirect:/admin/categories/add";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Thêm danh mục thất bại: " + e.getMessage());
            return "redirect:/admin/categories/add";
        }
    }
}
