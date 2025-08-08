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


    @PostMapping(value = "/user/create")
    public ModelAndView create(@ModelAttribute("newUser") @Valid User lsb,
                               BindingResult newUserBindingResult,
                               @RequestParam("avatarFile") MultipartFile file) {

        ModelAndView mav = new ModelAndView();

        if (newUserBindingResult.hasErrors()) {
            mav.setViewName("/admin/user/create");
            mav.addObject("roles", userService.getAllRoles());
            return mav;
        }

        try {
            String avatar = this.uploadService.HandleSaveUploadFile(file, "avatar");
            String hashPassword = this.passwordEncoder.encode(lsb.getPassword());

            lsb.setAvatar(avatar);
            lsb.setPassword(hashPassword);

            Role selectedRole = userService.getRoleById(lsb.getRole().getId());
            lsb.setRole(selectedRole);

            userService.SaveUser(lsb);

            // Redirect sau khi tạo thành công
            mav.setViewName("redirect:/admin/user/");
            return mav;

        } catch (IllegalArgumentException e) {
            mav.setViewName("/admin/user/create");
            mav.addObject("errorMessage", e.getMessage());
            mav.addObject("roles", userService.getAllRoles());
            return mav;

        } catch (Exception e) {
            mav.setViewName("/admin/user/create");
            mav.addObject("errorMessage", "Có lỗi xảy ra khi tạo người dùng.");
            mav.addObject("roles", userService.getAllRoles());
            return mav;
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


    @RequestMapping("/user/update/{id}")
    public ModelAndView updateUserPage(@PathVariable Long id) {
        User user = this.userService.findById(id);

        ModelAndView mav = new ModelAndView("/admin/user/update");
        mav.addObject("updateUser", user);
        return mav;
    }


    @RequestMapping(value = "/user/update/{id}", method = RequestMethod.POST)
    public ModelAndView postUpdate(@ModelAttribute("updateUser") User user) {
        ModelAndView mav = new ModelAndView();

        try {
            User upUser = this.userService.getUserById(user.getId());
            if (upUser != null) {
                upUser.setAddress(user.getAddress());
                upUser.setFullName(user.getFullName());
                upUser.setPhone(user.getPhone());
                upUser.setRole(this.userService.getRoleByName(user.getRole().getName()));

                this.userService.SaveUser(upUser);
            }

            mav.setViewName("redirect:/admin/user/");
            return mav;

        } catch (IllegalArgumentException e) {
            mav.setViewName("/admin/user/update");
            mav.addObject("errorMessage", e.getMessage());
            mav.addObject("updateUser", user);
            return mav;

        } catch (Exception e) {
            mav.setViewName("/admin/user/update");
            mav.addObject("errorMessage", "Đã xảy ra lỗi khi cập nhật người dùng.");
            mav.addObject("updateUser", user);
            return mav;
        }
    }

}