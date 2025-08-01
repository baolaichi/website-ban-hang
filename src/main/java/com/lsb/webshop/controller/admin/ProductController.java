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
    private final UploadService uploadService;
    private final ProductService productService;
    private final CategoryService categoryService;

    public ProductController(UploadService uploadService, ProductService productService, CategoryService categoryService) {
        this.uploadService = uploadService;
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping("/product")
    public String getDashboard(Model model) {
        List<Product> products = this.productService.getAllActiveProducts(); // ✅ chỉ lấy sản phẩm chưa bị xóa mềm
        model.addAttribute("products", products);
        return "admin/product/show";
    }

    @GetMapping("/product/create")
    public String getMethodName(Model model) {
        model.addAttribute("newProduct", new Product());
        model.addAttribute("categories", categoryService.getAllCategorys());
        return "admin/product/create";
    }

    @PostMapping("/product/create")
    public String create(Model model, @ModelAttribute("newProduct") @Valid Product sp,
        BindingResult productBindingResult,
        @RequestParam("productFile") MultipartFile file) {

    if (productBindingResult.hasErrors()) {
        model.addAttribute("categories", categoryService.getAllCategorys());
        return "/admin/product/create";
    }

    try {
        String productImg = this.uploadService.HandleSaveUploadFile(file, "product");
        sp.setImage(productImg);
        this.productService.SaveProduct(sp); // có thể ném IllegalArgumentException
        return "redirect:/admin/product";

    } catch (IllegalArgumentException e) {
        productBindingResult.rejectValue("name", "error.newProduct", e.getMessage());
        model.addAttribute("categories", categoryService.getAllCategorys());
        return "/admin/product/create";
    }
}

    @GetMapping("/product/{id}")
    public String getProductById(@PathVariable Long id, Model model) {
        Product product = this.productService.getByIdProduct(id).get();
        model.addAttribute("product", product);
        model.addAttribute("id", id);

        return "/admin/product/detail";
    }

    // Hiển thị trang xác nhận xóa
    @GetMapping("/product/delete/{id}")
    public String confirmDeleteProduct(@PathVariable Long id, Model model) {
    Optional<Product> productOpt = productService.getByIdAndNotDeleted(id); // dùng soft-delete filter

    if (productOpt.isPresent()) {
        model.addAttribute("deleteProduct", productOpt.get());
        return "/admin/product/delete"; // form xác nhận
    } else {
        model.addAttribute("errorMessage", "Không tìm thấy sản phẩm hoặc sản phẩm đã bị xóa.");
        return "redirect:/admin/product"; // quay về danh sách nếu lỗi
    }
}


    // Xử lý xóa sản phẩm (soft delete)
    @PostMapping("/product/delete")
    public String softDeleteProduct(@ModelAttribute("deleteProduct") Product product, RedirectAttributes redirectAttributes) {
    try {
        productService.softDeleteProduct(product.getId());
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa sản phẩm thành công.");
    } catch (IllegalArgumentException e) {
        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi khi xóa sản phẩm.");
    }
    return "redirect:/admin/product";
}


    @GetMapping("/product/update/{id}")
    public String updateProduct(Model model, @PathVariable Long id) {
    Optional<Product> productOpt = productService.getByIdProduct(id);
    if (productOpt.isPresent()) {
        model.addAttribute("updateProduct", productOpt.get());
        return "/admin/product/update";
    } else {
        return "redirect:/admin/product?notfound";
    }
}

    @PostMapping("/product/update")
    public String postUpdateProduct(Model model,
                @ModelAttribute("updateProduct") @Valid Product sp,
                BindingResult productBindingResult,
                @RequestParam("lsb") MultipartFile file) {

    if (productBindingResult.hasErrors()) {
        model.addAttribute("categories", categoryService.getAllCategorys());
        return "/admin/product/update";
    }

    try {
        productService.updateProductWithImage(sp, file);
        return "redirect:/admin/product";

    } catch (IllegalArgumentException e) {
        String[] parts = e.getMessage().split("\\|", 2);
        if (parts.length == 2) {
            productBindingResult.rejectValue(parts[0], "error.updateProduct", parts[1]);
        } else {
            productBindingResult.reject("error.updateProduct", e.getMessage());
        }
        model.addAttribute("categories", categoryService.getAllCategorys());
        return "/admin/product/update";

    } catch (Exception e) {
        productBindingResult.reject("globalError", "Đã xảy ra lỗi khi cập nhật sản phẩm.");
        log.error("[ProductController] Lỗi hệ thống: {}", e.getMessage(), e);
        model.addAttribute("categories", categoryService.getAllCategorys());
        return "/admin/product/update";
    }
}


}

