package com.lsb.web_shop.service;


import java.util.List;

import org.springframework.stereotype.Service;

import com.lsb.web_shop.domain.Role;
import com.lsb.web_shop.domain.User;
import com.lsb.web_shop.repository.RoleRepository;
import com.lsb.web_shop.repository.UserRepository;


@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public String HandlleHello() {
        return "Hello world from LSB-163";
    }

    public User findById(long id) {
        return this.userRepository.findById(id);
    }

    public List<User> getAllUser() {
        return this.userRepository.findAll();
    }

    public User getUserById(long id) {
        return this.userRepository.findById(id);
    }

    public User HandlUser(User user) {
        User newUser = this.userRepository.save(user);
        return newUser;
    }

    public void deleteUser(Long id) {
        this.userRepository.deleteById(id);
    }

    public Role getRoleByName(String name) {
        return this.roleRepository.findByName(name);
    }

}