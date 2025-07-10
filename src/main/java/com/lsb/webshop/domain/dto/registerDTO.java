package com.lsb.webshop.domain.dto;

import lombok.Data;

@Data
public class registerDTO {
    private  String firstName;
    private  String lastName;
    private  String email;
    private String password;
    private String confirmPassword;
    
}
