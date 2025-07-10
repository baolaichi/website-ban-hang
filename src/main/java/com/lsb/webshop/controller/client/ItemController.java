package com.lsb.webshop.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.lsb.webshop.domain.Product;
import com.lsb.webshop.service.ProductService;

@Controller
public class ItemController {

    private  ProductService productService;
    
    public ItemController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/product/{id}")
    public String getMethodName(Model model, @PathVariable long id) {
        Product pr = this.productService.getByIdProduct(id).get();
        model.addAttribute("product", pr);
        model.addAttribute("id", id);
        model.addAttribute("factories", this.productService.getAllFactories());
        model.addAttribute("products", this.productService.getAllProducts());
        return "client/product/detail";
    }

}