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
    public String viewProfile(Model model, Principal principal){
        String userName = principal.getName();
        UserDTO userDTO = userService.getUserByUserName(userName);
        model.addAttribute("user", userDTO);
        return "client/account/profile";
    }

   @PostMapping("/update")
public String updateAccount(@ModelAttribute UserDTO userDTO,
                            @RequestParam("avatarFile") MultipartFile avatarFile,
                            HttpServletRequest request) {
    try {
        if (!avatarFile.isEmpty()) {
            // Gọi service lưu ảnh
            String savedFileName = uploadService.HandleSaveUploadFile(avatarFile, "avatar");

            // Lưu tên file ảnh vào DTO
            userDTO.setAvatarUrl(savedFileName);

            // Cập nhật session nếu có dùng ảnh
            request.getSession().setAttribute("avatar", savedFileName);
        }

        userService.updateAccount(userDTO);
        return "redirect:/account/profile?success";

    } catch (Exception e) {
        e.printStackTrace();
        return "redirect:/account/profile?error";
    }
}




}
