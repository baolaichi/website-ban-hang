package com.lsb.webshop.controller.client;


import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.lsb.webshop.domain.dto.UserDTO;
import com.lsb.webshop.service.UploadService;
import com.lsb.webshop.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.ModelAndView;


@Controller
@RequestMapping("/account")
public class AccountController {
    
    private final UserService userService;
    private final UploadService uploadService;

    public AccountController(UserService userService, UploadService uploadService) {
        this.userService = userService;
        this.uploadService = uploadService;
    }

    @GetMapping("/profile")
    public ModelAndView viewProfile(Principal principal) {
        UserDTO userDTO = userService.getUserByEmail(principal.getName());

        ModelAndView mav = new ModelAndView("client/account/profile");
        mav.addObject("user", userDTO);
        return mav;
    }

    @PostMapping("/update")
    public ModelAndView updateProfile(@ModelAttribute("user") UserDTO userDTO,
                                      @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
                                      Principal principal) {
        ModelAndView mav = new ModelAndView();

        try {
            UserDTO updated = userService.updateProfile(userDTO, avatarFile, principal.getName());
            mav.setViewName("redirect:/account/profile?success");
            return mav;
        } catch (RuntimeException e) {
            mav.setViewName("client/account/profile");
            mav.addObject("errorMessage", e.getMessage());
            mav.addObject("user", userDTO);
            return mav;
        }
    }

}
