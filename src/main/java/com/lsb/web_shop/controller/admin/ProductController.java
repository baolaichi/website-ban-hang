package com.lsb.web_shop.controller.admin;


import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.lsb.web_shop.domain.Product;
import com.lsb.web_shop.service.ProductService;
import com.lsb.web_shop.service.UploadService;

import jakarta.validation.Valid;

@Controller
public class ProductController {
    private final UploadService uploadService;
    private final ProductService productService;

    public ProductController(UploadService uploadService, ProductService productService) {
        this.uploadService = uploadService;
        this.productService = productService;
    }

    @GetMapping("/admin/product")
    public String getDashboard(Model model) {
        List<Product> products = this.productService.getAllProducts();
        model.addAttribute("products", products);
        return "admin/product/show";
    }

    @GetMapping("/admin/product/create")
    public String getMethodName(Model model) {
        model.addAttribute("newProduct", new Product());
        return "admin/product/create";
    }

    @PostMapping("/admin/product/create")
    public String create(Model model, @ModelAttribute("newProduct") @Valid Product sp,
            BindingResult productBindingResult,
            @RequestParam("productFile") MultipartFile file) {
        List<FieldError> errors = productBindingResult.getFieldErrors();
        if (productBindingResult.hasErrors()) {
            return "/admin/product/create";
        }
        // this.userService.HandlUser(lsb);
        String productImg = this.uploadService.HandleSaveUploadFile(file, "product");

        sp.setImage(productImg);
        this.productService.handlProduct(sp);
        return "redirect:/admin/product";
    }

    @GetMapping("/admin/product/{id}")
    public String getProductById(@PathVariable Long id, Model model) {
        Product product = this.productService.getByIdProduct(id).get();
        model.addAttribute("product", product);
        model.addAttribute("id", id);

        return "/admin/product/detail";
    }

   // Hiển thị trang xác nhận xóa
@GetMapping("/admin/product/delete/{id}")
public String deleteProduct(Model model, @PathVariable Long id) {
    Product product = new Product();
    product.setId(id);
    model.addAttribute("deleteProduct", product);
    return "/admin/product/delete";
}

// Xử lý khi nhấn xác nhận xóa
@PostMapping("/admin/product/delete")
public String deleteProductPage(@ModelAttribute("deleteProduct") Product product) {
    productService.deleteProduct(product.getId());
    return "redirect:/admin/product";
}



@GetMapping("/admin/product/update/{id}")
public String updateProduct(Model model, @PathVariable Long id) {
    Optional<Product> productOpt = productService.getByIdProduct(id);
    if (productOpt.isPresent()) {
        model.addAttribute("updateProduct", productOpt.get());
        return "/admin/product/update";
    } else {
        return "redirect:/admin/product?notfound";
    }
}


    @PostMapping("/admin/product/update")
    public String postMethodName(@ModelAttribute("updateProduct") @Valid Product product,
            BindingResult newProducResult,
            @RequestParam("lsb") MultipartFile file) {
        // TODO: process POST request
        if (newProducResult.hasErrors()) {
            return "/admin/product/update";
        }

        Product pr = this.productService.getByIdProduct(product.getId()).get();
        if (pr != null) {
            if (!file.isEmpty()) {
                String img = this.uploadService.HandleSaveUploadFile(file, "product");
                pr.setImage(img);
            }

            pr.setName(product.getName());
            pr.setPrice(product.getPrice());
            pr.setQuantity(product.getQuantity());
            pr.setDetailDesc(product.getDetailDesc());
            pr.setShortDesc(product.getShortDesc());
            pr.setFactory(product.getFactory());
            pr.setTarget(product.getTarget());

            this.productService.handlProduct(pr);
        }

        return "redirect:/admin/product";
    }

}

