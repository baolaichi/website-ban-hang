package com.lsb.webshop.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class OrderController {
    @GetMapping("/admin/order")
    public ModelAndView getDashboard() {
        ModelAndView modelAndView = new ModelAndView("admin/order/show");
        modelAndView.addObject("title", "Quản lý đơn hàng");
        return modelAndView;
    }
}