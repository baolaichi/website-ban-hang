package com.lsb.webshop.controller.admin;


import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.lsb.webshop.domain.Role;
import com.lsb.webshop.domain.User;
import com.lsb.webshop.service.UploadService;
import com.lsb.webshop.service.UserService;

import jakarta.validation.Valid;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/admin")
public class UserController {
    private final UserService userService;
    private final UploadService uploadService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, UploadService uploadService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.uploadService = uploadService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/user/create")
    public ModelAndView getUserPage() {
        ModelAndView mav = new ModelAndView("/admin/user/create");
        mav.addObject("newUser", new User());
        mav.addObject("roles", userService.getAllRoles());
        return mav;
    }


    @PostMapping("/user/create")
    public ModelAndView createUser(@ModelAttribute("newUser") @Valid User newUser,
                                   BindingResult bindingResult,
                                   @RequestParam("avatarFile") MultipartFile file) {
        if (bindingResult.hasErrors()) {
            return new ModelAndView("/admin/user/create")
                    .addObject("roles", userService.getAllRoles());
        }

        try {
            userService.createUser(newUser, file);
            return new ModelAndView("redirect:/admin/user/");
        } catch (IllegalArgumentException e) {
            bindingResult.reject("error.newUser", e.getMessage());
            return new ModelAndView("/admin/user/create")
                    .addObject("roles", userService.getAllRoles());
        }
    }



    @GetMapping(value = "/user/")
    public ModelAndView getList() {
        List<User> users = this.userService.getAllUser();
        ModelAndView mav = new ModelAndView("/admin/user/show");
        mav.addObject("users", users);

        return mav;
    }


    @GetMapping("/user/view/{id}")
    public ModelAndView getDetail(@PathVariable Long id) {
        User user = this.userService.getUserById(id);
        ModelAndView mav = new ModelAndView("/admin/user/detail");
        mav.addObject("id", id);
        mav.addObject("user", user);
        return mav;
    }


    @GetMapping("/user/delete/{id}")
    public ModelAndView deleteUser(@PathVariable Long id) {
        User user = new User();
        user.setId(id);
        ModelAndView mav = new ModelAndView("/admin/user/delete");
        mav.addObject("id", id);
        mav.addObject("deleteUser", user);
        return mav;
    }


    @PostMapping("/user/delete/")
    public ModelAndView deleteUserPage(@ModelAttribute("deleteUser") User user) {
        ModelAndView mav = new ModelAndView("redirect:/admin/user/");
        try {
            userService.deleteUser(user.getId());
            // Flash messages có thể xử lý qua RedirectAttributes nếu cần
            mav.addObject("successMessage", "Đã xóa người dùng thành công.");
        } catch (IllegalArgumentException e) {
            mav.addObject("errorMessage", "Không tìm thấy người dùng.");
        } catch (Exception e) {
            mav.addObject("errorMessage", "Đã xảy ra lỗi khi xóa người dùng.");
        }

        return mav;
    }


    @GetMapping("/user/update/{id}")
    public ModelAndView updateUserPage(@PathVariable Long id) {
        User user = userService.getUserById(id);

        if (user.getRole() == null) {
            user.setRole(new Role());
        }

        ModelAndView mav = new ModelAndView("/admin/user/update");
        mav.addObject("updateUser", user);
        mav.addObject("roles", userService.getAllRoles());
        return mav;
    }


    @PostMapping("/user/update/{id}")
    public ModelAndView postUpdate(@PathVariable Long id,
                                   @ModelAttribute("updateUser") User user) {
        ModelAndView mav = new ModelAndView();

        try {
            userService.updateUser(user, null); // không update avatar
            return new ModelAndView("redirect:/admin/user/");
        } catch (RuntimeException e) {
            mav.setViewName("/admin/user/update");
            mav.addObject("errorMessage", e.getMessage());
            mav.addObject("updateUser", user);
            mav.addObject("roles", userService.getAllRoles());
            return mav;
        }
    }


}
