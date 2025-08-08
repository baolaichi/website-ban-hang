package com.lsb.webshop.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
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
    public ModelAndView showAddCategoryForm() {
        ModelAndView modelAndView = new ModelAndView("admin/category/add");
        modelAndView.addObject("category", new Category());
        return modelAndView;  // Tên template Thymeleaf: templates/category/add.html
    }

   @PostMapping("/add")
    public ModelAndView saveCategory(@ModelAttribute Category category) {
    ModelAndView mav = new ModelAndView();

    try {
        Category saved = categoryService.saveCategory(category);
        mav.addObject("success", "Thêm danh mục thành công: " + saved.getName());
    } catch (Exception e) {
        mav.addObject("error", "Thêm danh mục thất bại: " + e.getMessage());
    }

    // Điều hướng lại form thêm (giống redirect)
    mav.setViewName("redirect:/admin/categories/add");

    return mav;
    }

}
