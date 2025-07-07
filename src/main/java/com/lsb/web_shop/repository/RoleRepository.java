package com.lsb.web_shop.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lsb.web_shop.domain.Role;


public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(String name);
}
