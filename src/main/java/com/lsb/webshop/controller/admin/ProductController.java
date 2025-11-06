package com.lsb.webshop.controller.admin;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.lsb.webshop.domain.Product;
import com.lsb.webshop.service.CategoryService;
import com.lsb.webshop.service.ProductService;
import com.lsb.webshop.service.UploadService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequestMapping("/admin")
public class ProductController {
    private final ProductService productService;
    private final CategoryService categoryService;

    public ProductController( ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping("/product")
    public ModelAndView getDashboard() {
        List<Product> products = this.productService.getAllActiveProducts();
        ModelAndView mav = new ModelAndView("admin/product/show");
        mav.addObject("products", products);

        return mav;
    }


    @GetMapping("/product/create")
    public ModelAndView getCreateProductPage() {
        ModelAndView mav = new ModelAndView("admin/product/create");
        mav.addObject("newProduct", new Product());
        mav.addObject("categories", categoryService.getAllCategorys());

        return mav;
    }


    @GetMapping("/product/{id}")
    public ModelAndView getProductById(@PathVariable Long id) {
        Product product = this.productService.getByIdProduct(id).get();

        ModelAndView mav = new ModelAndView("/admin/product/detail");
        mav.addObject("product", product);
        mav.addObject("id", id);

        return mav;
    }


    // Hiển thị trang xác nhận xóa
    @GetMapping("/product/delete/{id}")
    public ModelAndView confirmDeleteProduct(@PathVariable Long id) {
        ModelAndView mav = new ModelAndView();
        Optional<Product> productOpt = productService.getByIdAndNotDeleted(id); // dùng soft-delete filter

        if (productOpt.isPresent()) {
            mav.addObject("deleteProduct", productOpt.get());
            mav.setViewName("/admin/product/delete"); // form xác nhận
        } else {
            mav.addObject("errorMessage", "Không tìm thấy sản phẩm hoặc sản phẩm đã bị xóa.");
            mav.setViewName("redirect:/admin/product"); // quay về danh sách nếu lỗi
        }
        return mav;
    }

    // Xử lý xóa sản phẩm (soft delete)
    @PostMapping("/product/delete")
    public ModelAndView softDeleteProduct(
            @ModelAttribute("deleteProduct") Product product,
            RedirectAttributes redirectAttributes) {

        ModelAndView mav = new ModelAndView("redirect:/admin/product");

        try {
            productService.softDeleteProduct(product.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa sản phẩm thành công.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi khi xóa sản phẩm.");
        }
        return mav;
    }

    // Hiển thị form cập nhật sản phẩm
    @GetMapping("/product/update/{id}")
    public ModelAndView updateProduct(@PathVariable Long id) {
        ModelAndView mav = new ModelAndView();
        Optional<Product> productOpt = productService.getByIdProduct(id);

        if (productOpt.isPresent()) {
            mav.addObject("updateProduct", productOpt.get());
            mav.setViewName("/admin/product/update");
        } else {
            mav.setViewName("redirect:/admin/product?notfound");
        }
        return mav;
    }

    // Dùng chung để load view + categories khi có lỗi
    private ModelAndView prepareErrorView(String viewName, BindingResult bindingResult) {
        ModelAndView mav = new ModelAndView(viewName);
        mav.addObject("categories", categoryService.getAllCategorys());
        mav.addAllObjects(bindingResult.getModel());
        return mav;
    }

    @PostMapping("/product/create")
    public ModelAndView postCreateProduct(
            @ModelAttribute("newProduct") @Valid Product sp,
            BindingResult bindingResult,
            @RequestParam("productFile") MultipartFile file) {

        if (bindingResult.hasErrors()) {
            return prepareErrorView("/admin/product/create", bindingResult);
        }

        try {
            productService.createProduct(sp, file);
            return new ModelAndView("redirect:/admin/product");

        } catch (IllegalArgumentException e) {
            handleServiceException(e, bindingResult);
            return prepareErrorView("/admin/product/create", bindingResult);

        } catch (Exception e) {
            bindingResult.reject("globalError", "Đã xảy ra lỗi khi tạo sản phẩm.");
            return prepareErrorView("/admin/product/create", bindingResult);
        }
    }

    @PostMapping("/product/update")
    public ModelAndView postUpdateProduct(
            @ModelAttribute("updateProduct") @Valid Product sp,
            BindingResult bindingResult,
            @RequestParam(value = "lsb", required = false) MultipartFile file) {

        if (bindingResult.hasErrors()) {
            return prepareErrorView("/admin/product/update", bindingResult);
        }

        try {
            productService.updateProduct(sp.getId(), sp, file);
            return new ModelAndView("redirect:/admin/product");

        } catch (IllegalArgumentException e) {
            handleServiceException(e, bindingResult);
            return prepareErrorView("/admin/product/update", bindingResult);

        } catch (Exception e) {
            bindingResult.reject("globalError", "Đã xảy ra lỗi khi cập nhật sản phẩm.");
            return prepareErrorView("/admin/product/update", bindingResult);
        }
    }

    // Hàm xử lý lỗi trả từ service dạng "field|message"
    private void handleServiceException(IllegalArgumentException e, BindingResult bindingResult) {
        String[] parts = e.getMessage().split("\\|", 2);
        if (parts.length == 2) {
            bindingResult.rejectValue(parts[0], "error", parts[1]);
        } else {
            bindingResult.reject("error", e.getMessage());
        }
    }


}

