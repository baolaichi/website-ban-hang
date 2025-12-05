package com.lsb.webshop.controller.admin;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Controller cha cho tất cả các Controller Admin.
 * Cung cấp các dữ liệu chung (như currentPath) cho giao diện.
 */
public abstract class BaseController {

    @ModelAttribute("currentPath")
    public String getCurrentPath(HttpServletRequest request) {
        return request.getRequestURI();
    }
}