package com.lsb.webshop.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class registerDTO {
    @Size(min = 3, message = "Tên người dùng phải có ít nhất 3 ký tự")
    private  String firstName;

    private  String lastName;
    @Email(message = "Email không hợp lệ", regexp = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$")
    private  String email;
    
    private String password;
    
    @Size(min = 6, message = "Mật khẩu phải có tối thiểu 6 kí tự")
    private String confirmPassword;
    
}
