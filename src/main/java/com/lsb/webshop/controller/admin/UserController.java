package com.lsb.webshop.controller.admin;


import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.lsb.webshop.domain.Role;
import com.lsb.webshop.domain.User;
import com.lsb.webshop.service.UploadService;
import com.lsb.webshop.service.UserService;

import jakarta.validation.Valid;

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

@RequestMapping("/user/create")
public String getUserPage(Model model) {
    model.addAttribute("newUser", new User());
    model.addAttribute("roles", userService.getAllRoles()); // bổ sung dòng này
    return "/admin/user/create";
}

@RequestMapping(value = "/user/create", method = RequestMethod.POST)
public String create(Model model,
                     @ModelAttribute("newUser") @Valid User lsb,
                     BindingResult newUserBindingResult,
                     @RequestParam("avatarFile") MultipartFile file) {

    if (newUserBindingResult.hasErrors()) {
        model.addAttribute("roles", userService.getAllRoles()); // thêm dòng này
        return "/admin/user/create";
    }

    try {
        String avatar = this.uploadService.HandleSaveUploadFile(file, "avatar");
        String hashPassword = this.passwordEncoder.encode(lsb.getPassword());

        lsb.setAvatar(avatar);
        lsb.setPassword(hashPassword);

        // Không nên lấy bằng role.getName() vì binding đã chọn role theo id
        Role selectedRole = userService.getRoleById(lsb.getRole().getId()); // sửa lại dòng này
        lsb.setRole(selectedRole);

        userService.SaveUser(lsb);

        return "redirect:/admin/user/";

    } catch (IllegalArgumentException e) {
        model.addAttribute("errorMessage", e.getMessage());
        model.addAttribute("roles", userService.getAllRoles()); // thêm dòng này
        return "/admin/user/create";

    } catch (Exception e) {
        model.addAttribute("errorMessage", "Có lỗi xảy ra khi tạo người dùng.");
        model.addAttribute("roles", userService.getAllRoles()); // thêm dòng này
        return "/admin/user/create";
    }
}


    @RequestMapping(value = "/user/")
    public String getList(Model model) {
        List<User> users = this.userService.getAllUser();
        model.addAttribute("users", users);
        return "/admin/user/show";
    }

    @RequestMapping("/user/view/{id}")
    public String getDetail(Model model, @PathVariable Long id) {
        User user = this.userService.getUserById(id);
        model.addAttribute("id", id);
        model.addAttribute("user", user);
        return "/admin/user/detail";
    }

    @RequestMapping("/user/delete/{id}")
    public String deleteUser(Model model, @PathVariable Long id) {
        model.addAttribute("id", id);
        User user = new User();
        user.setId(id);
        model.addAttribute("deleteUser", user);
        return "/admin/user/delete";
    }

    @PostMapping("/user/delete/")
    public String deleteUserPage(Model model, @ModelAttribute("deleteUser") User user) {
        try {
            userService.deleteUser(user.getId());
            model.addAttribute("successMessage", "Đã xóa người dùng thành công.");
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", "Không tìm thấy người dùng.");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Đã xảy ra lỗi khi xóa người dùng.");
        }
        return "redirect:/admin/user/";
    }

    @RequestMapping("/user/update/{id}")
    public String updateUserPage(Model model, @PathVariable Long id) {
        User user = this.userService.findById(id);
        model.addAttribute("updateUser", user);
        return "/admin/user/update";
    }

@RequestMapping(value = "/user/update/{id}", method = RequestMethod.POST)
public String postUpdate(Model model, @ModelAttribute("updateUser") User user) {
    try {
        User upUser = this.userService.getUserById(user.getId());
        if (upUser != null) {
            upUser.setAddress(user.getAddress());
            upUser.setFullName(user.getFullName());
            upUser.setPhone(user.getPhone());
            upUser.setRole(this.userService.getRoleByName(user.getRole().getName()));

            this.userService.SaveUser(upUser);
        }
        return "redirect:/admin/user/";

    } catch (IllegalArgumentException e) {
        model.addAttribute("errorMessage", e.getMessage());
        model.addAttribute("updateUser", user);
        return "/admin/user/update"; // Trả về form nếu có lỗi hợp lệ (trùng tên/email)

    } catch (Exception e) {
        model.addAttribute("errorMessage", "Đã xảy ra lỗi khi cập nhật người dùng.");
        model.addAttribute("updateUser", user);
        return "/admin/user/update"; // Trả về form nếu lỗi bất ngờ
    }
}


}