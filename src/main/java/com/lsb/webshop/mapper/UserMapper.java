package com.lsb.webshop.mapper;

import org.mapstruct.Mapper;

import com.lsb.webshop.domain.User;
import com.lsb.webshop.domain.dto.UserDTO;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDTO toDto(User user);
    User toEntity(UserDTO userDTO);
}
