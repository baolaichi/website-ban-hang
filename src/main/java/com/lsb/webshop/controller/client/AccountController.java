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
        String userName = principal.getName();
        UserDTO userDTO = userService.getUserByUserName(userName);

        ModelAndView mav = new ModelAndView("client/account/profile");
        mav.addObject("user", userDTO);

        return mav;
    }


    @PostMapping("/update")
    public ModelAndView updateAccount(@ModelAttribute UserDTO userDTO,
                                      @RequestParam("avatarFile") MultipartFile avatarFile,
                                      HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("client/account/profile");

        try {
            if (!avatarFile.isEmpty()) {
                String savedFileName = uploadService.HandleSaveUploadFile(avatarFile, "avatar");
                userDTO.setAvatarUrl(savedFileName);
                request.getSession().setAttribute("avatar", savedFileName);
            }

            userService.updateAccount(userDTO);
            mav.addObject("user", userDTO);
            mav.addObject("success", "Cập nhật thành công!");

        } catch (Exception e) {
            e.printStackTrace();
            mav.addObject("user", userDTO);
            mav.addObject("error", "Đã xảy ra lỗi: " + e.getMessage());
        }

        return mav;
    }


}
