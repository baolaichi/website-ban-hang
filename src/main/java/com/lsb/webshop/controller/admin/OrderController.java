package com.lsb.webshop.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import com.lsb.webshop.domain.Order;
import com.lsb.webshop.service.OrderService;

import java.util.List;

@Controller
public class OrderController {
    private final OrderService orderService;
    

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }


    @GetMapping("/admin/order")
    public ModelAndView getDashboard() {
        List<Order> orders = this.orderService.fetchAllOrders();
        ModelAndView mav = new ModelAndView("admin/order/show");
        mav.addObject("orders", orders);
        return mav;
    }



}