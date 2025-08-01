package com.lsb.webshop.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class DashBoardController {
    @GetMapping("/admin")
    public ModelAndView getDashboard() {
        ModelAndView modelAndView = new ModelAndView("admin/dashboard/show");
        return modelAndView;
    }
}