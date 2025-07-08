package com.lsb.web_shop.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.lsb.web_shop.domain.Role;
import com.lsb.web_shop.domain.User;
import com.lsb.web_shop.repository.RoleRepository;
import com.lsb.web_shop.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public String HandlleHello() {
        log.info("Gọi HandlleHello()");
        return "Hello world from LSB-163";
    }

    public User findById(long id) {
        log.info("Gọi findById với id: {}", id);
        return this.userRepository.findById(id);
    }

    public List<User> getAllUser() {
        log.info("Gọi getAllUser()");
        return this.userRepository.findAll();
    }

    public User getUserById(long id) {
        log.info("Gọi getUserById với id: {}", id);
        return this.userRepository.findById(id);
    }

    public User HandlUser(User user) {
        log.info("Gọi HandlUser với user: {}", user);
        User newUser = this.userRepository.save(user);
        log.info("Đã lưu user mới: {}", newUser);
        return newUser;
    }

    public void deleteUser(Long id) {
        log.info("Gọi deleteUser với id: {}", id);
        this.userRepository.deleteById(id);
        log.info("Đã xóa user với id: {}", id);
    }

    public Role getRoleByName(String name) {
        log.info("Gọi getRoleByName với name: {}", name);
        Role role = this.roleRepository.findByName(name);
        if (role == null) {
            log.warn("Không tìm thấy role với name: {}", name);
        } else {
            log.info("Tìm thấy role: {}", role);
        }
        return role;
    }
}
