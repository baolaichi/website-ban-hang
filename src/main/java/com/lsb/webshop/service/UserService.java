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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

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
        String email = user.getEmail();

        log.info("[UserService] SaveUser() - Bắt đầu lưu user: email='{}'", email);

        try {
            if (email == null || email.isBlank()) {
                throw new IllegalArgumentException("Email không được để trống");
            }

            boolean isExisted;

            if (user.getId() != null) {
                isExisted = userRepository.existsByEmailAndIdNot(email, user.getId());
            } else {
                isExisted = userRepository.existsByEmail(email);
            }

            if (isExisted) {
                log.warn("[UserService] SaveUser() - Email đã tồn tại: {}", email);
                throw new IllegalArgumentException("Email người dùng đã tồn tại");
            }

            User savedUser = userRepository.save(user);
            log.info("[UserService] SaveUser() - Lưu user thành công: ID={}, email='{}'", savedUser.getId(), savedUser.getEmail());
            return savedUser;

        } catch (IllegalArgumentException e) {
            StackTraceElement element = e.getStackTrace()[0];
            log.warn("[UserService] SaveUser() - Lỗi dữ liệu: {} - {} tại {}.{}(): dòng {}",
                    e.getClass().getSimpleName(), 
                    e.getMessage(), 
                    element.getClassName(), 
                    element.getMethodName(), 
                    element.getLineNumber());
            throw e;

        } catch (Exception e) {
            StackTraceElement element = e.getStackTrace()[0];
            log.error("[UserService] SaveUser() - Lỗi hệ thống: {} - {} tại {}.{}(): dòng {}",
                    e.getClass().getSimpleName(), 
                    e.getMessage(), 
                    element.getClassName(), 
                    element.getMethodName(), 
                    element.getLineNumber(), 
                    e); // vẫn truyền e để log stack trace đầy đủ
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

    public UserDTO updateAccount(UserDTO userDto) {
    try {
        System.out.println("Cập nhật tài khoản cho email: " + userDto.getEmail());

        User user = userRepository.findByEmail(userDto.getEmail())
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại với email: " + userDto.getEmail()));

        user.setFullName(userDto.getFullName());
        user.setEmail(userDto.getEmail());
        user.setPhone(userDto.getPhoneNumber());
        user.setAddress(userDto.getAddress());
        user.setAvatar(userDto.getAvatarUrl());

        userRepository.save(user);

        System.out.println("Cập nhật thành công cho người dùng: " + userDto.getEmail());

        return userMapper.toDto(user);
    } catch (Exception e) {
        System.err.println("Lỗi khi cập nhật tài khoản: " + e.getMessage());
        e.printStackTrace();
        throw new RuntimeException("Đã xảy ra lỗi khi cập nhật tài khoản.");
    }
}


}

