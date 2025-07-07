package com.lsb.web_shop.controller.admin;


import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.lsb.web_shop.domain.User;
import com.lsb.web_shop.service.UploadService;
import com.lsb.web_shop.service.UserService;

import jakarta.validation.Valid;

@Controller
public class UserController {
    private final UserService userService;
    private final UploadService uploadService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, UploadService uploadService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.uploadService = uploadService;
        this.passwordEncoder = passwordEncoder;
    }

    // @RequestMapping("/")
    // public String getHomePage(Model model) {
    // List<User> users = this.userService.findByFullName("li chi hao");
    // System.out.println(users);

    // model.addAttribute("message", users);
    // model.addAttribute("title", "Home page");
    // return "hello";
    // }

    @RequestMapping("/admin/user/create")
    public String getUserPage(Model model) {
        model.addAttribute("newUser", new User());
        return "/admin/user/create";
    }

    @RequestMapping(value = "/admin/user/create", method = RequestMethod.POST)
    public String create(Model model, @ModelAttribute("newUser") @Valid User lsb,
            BindingResult newUserBindingResult,
            @RequestParam("avatarFile") MultipartFile file) {
        List<FieldError> errors = newUserBindingResult.getFieldErrors();
        for (FieldError error : errors) {
            System.out.println(">>>>" + error.getField() + " - " + error.getDefaultMessage());
        }

        if (newUserBindingResult.hasErrors()) {
            return "/admin/user/create";
        }
        // this.userService.HandlUser(lsb);
        String avatar = this.uploadService.HandleSaveUploadFile(file, "avatar");
        String hashPassword = this.passwordEncoder.encode(lsb.getPassword());

        lsb.setAvatar(avatar);
        lsb.setPassword(hashPassword);
        lsb.setRole(this.userService.getRoleByName(lsb.getRole().getName()));

        this.userService.HandlUser(lsb);
        return "redirect:/admin/user/";
    }

    @RequestMapping(value = "/admin/user/")
    public String getList(Model model) {
        List<User> users = this.userService.getAllUser();
        model.addAttribute("users", users);
        return "/admin/user/show";
    }

    @RequestMapping("/admin/user/view/{id}")
    public String getDetail(Model model, @PathVariable Long id) {
        User user = this.userService.getUserById(id);
        model.addAttribute("id", id);
        model.addAttribute("user", user);
        return "/admin/user/detail";
    }

    @RequestMapping("/admin/user/delete/{id}")
    public String deleteUser(Model model, @PathVariable Long id) {
        model.addAttribute("id", id);
        User user = new User();
        user.setId(id);
        model.addAttribute("deleteUser", user);
        return "/admin/user/delete";
    }

    @PostMapping("/admin/user/delete/")
    public String deleteUserPage(Model model, @ModelAttribute("deleteUser") User user) {
        this.userService.deleteUser(user.getId());
        return "redirect:/admin/user/";
    }

    @RequestMapping("/admin/user/update/{id}")
    public String updateUserPage(Model model, @PathVariable Long id) {
        User user = this.userService.findById(id);
        model.addAttribute("updateUser", user);
        return "/admin/user/update";
    }

    @RequestMapping(value = "/admin/user/update/{id}", method = RequestMethod.POST)
    public String postUpdate(Model model, @ModelAttribute("updateUser") User user) {
        User upUser = this.userService.getUserById(user.getId());
        if (upUser != null) {
            upUser.setAddress(user.getAddress());
            upUser.setFullName(user.getFullName());
            upUser.setPhone(user.getPhone());
            upUser.setRole(this.userService.getRoleByName(user.getRole().getName()));

            this.userService.HandlUser(upUser);
        }
        return "redirect:/admin/user/";

    }

}