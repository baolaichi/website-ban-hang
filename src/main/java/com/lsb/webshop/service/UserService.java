package com.lsb.webshop.service;

import java.util.List;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.lsb.webshop.domain.Role;
import com.lsb.webshop.domain.User;
import com.lsb.webshop.domain.dto.registerDTO;
import com.lsb.webshop.domain.dto.UserDTO;
import com.lsb.webshop.mapper.UserMapper;
import com.lsb.webshop.repository.RoleRepository;
import com.lsb.webshop.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    public User findById(long id) {
        log.info("[UserService] findById() - ID: {}", id);
        return this.userRepository.findById(id);
    }

    public List<User> getAllUser() {
        log.info("[UserService] getAllUser() - Lấy danh sách tất cả người dùng");
        return this.userRepository.findAll();
    }

    public User getUserById(long id) {
        log.info("[UserService] getUserById() - ID: {}", id);
        return this.userRepository.findById(id);
    }

    public User SaveUser(User user) {
        String fullName = user.getFullName();
        String email = user.getEmail();

        log.info("[UserService] SaveUser() - Bắt đầu lưu user: fullName='{}', email='{}'", fullName, email);

        try {
            boolean isExisted;

            if (user.getId() != null) {
                isExisted = userRepository.existsByFullNameAndIdNot(fullName, user.getId()) ||
                            userRepository.existsByEmailAndIdNot(email, user.getId());
            } else {
                isExisted = userRepository.existsByFullName(fullName) ||
                            userRepository.existsByEmail(email);
            }

            if (isExisted) {
                log.warn("[UserService] SaveUser() - Tên hoặc email đã tồn tại: fullName='{}', email='{}'", fullName, email);
                throw new IllegalArgumentException("Tên hoặc email người dùng đã tồn tại");
            }

            User savedUser = userRepository.save(user);
            log.info("[UserService] SaveUser() - Lưu user thành công: ID={}, fullName='{}'", savedUser.getId(), savedUser.getFullName());
            return savedUser;

        } catch (IllegalArgumentException e) {
            log.error("[UserService] SaveUser() - Lỗi dữ liệu: {}", e.getMessage());
            throw e;

        } catch (Exception e) {
            log.error("[UserService] SaveUser() - Lỗi hệ thống khi lưu user: {}", e.getMessage(), e);
            throw new RuntimeException("Đã xảy ra lỗi khi lưu người dùng, vui lòng thử lại sau.");
        }
    }

    @Transactional
    public void deleteUser(Long id) {
        log.info("[UserService] deleteUser() - Yêu cầu xóa người dùng ID: {}", id);

        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("[UserService] deleteUser() - Không tìm thấy người dùng với ID: {}", id);
                        return new IllegalArgumentException("Người dùng không tồn tại");
                    });

            userRepository.delete(user);
            log.info("[UserService] deleteUser() - Đã xóa người dùng ID: {}", id);

        } catch (IllegalArgumentException e) {
            log.error("[UserService] deleteUser() - Lỗi dữ liệu: {}", e.getMessage());
            throw e;

        } catch (Exception e) {
            log.error("[UserService] deleteUser() - Lỗi hệ thống khi xóa user ID: {} - {}", id, e.getMessage(), e);
            throw new RuntimeException("Đã xảy ra lỗi nội bộ, vui lòng thử lại sau");
        }
    }

    public Role getRoleByName(String name) {
        log.info("[UserService] getRoleByName() - Tìm role theo tên: {}", name);
        Role role = this.roleRepository.findByName(name).orElse(null);

        if (role == null) {
            log.warn("[UserService] getRoleByName() - Không tìm thấy role với tên: {}", name);
        } else {
            log.info("[UserService] getRoleByName() - Tìm thấy role: {}", role.getName());
        }

        return role;
    }

    public Role getRoleById(Long id) {
        log.info("[UserService] getRoleById() - Tìm role theo ID: {}", id);
        return roleRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[UserService] getRoleById() - Không tìm thấy role với ID: {}", id);
                    return new IllegalArgumentException("Không tìm thấy vai trò với ID: " + id);
                });
    }

    public List<Role> getAllRoles() {
    log.info("[UserService] getAllRoles() - Lấy danh sách tất cả vai trò");
    return this.roleRepository.findAll();
    }

    public void register(registerDTO dto) {
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setFullName(dto.getFirstName() + " " + dto.getLastName());
        user.setDeleted(false);

        Role userRole = roleRepository.findByName("USER")
                            .orElseThrow(() -> new RuntimeException("Vai trò USER chưa tồn tại!"));
        user.setRole(userRole);

        userRepository.save(user);
    }

    public User findByUsername(String email) {
    return userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng: " + email));
    }

    public UserDTO getUserByUserName(String emai){
        User user = userRepository.findByEmail(emai).orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng: " + emai));
        return userMapper.toDto(user);
    }

    public UserDTO updateAccount(UserDTO userDto){
        User user = userRepository.findByEmail(userDto.getEmail()).orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        user.setFullName(userDto.getFullName());
        user.setEmail(userDto.getEmail());
        user.setPhone(userDto.getPhoneNumber());
        user.setAddress(userDto.getAddress());
        user.setAvatar(userDto.getAvatarUrl());

        this.userRepository.save(user);

        return userMapper.toDto(user);
    }

}

