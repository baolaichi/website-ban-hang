package com.lsb.webshop.service;

import java.util.List;
import java.util.Optional;

// Import mới (Giữ nguyên)
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

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
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final UploadService uploadService;
    // private static final Logger log = LoggerFactory.getLogger(UserService.class); // (Đã có @Slf4j)

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, UserMapper userMapper,  UploadService uploadService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.uploadService = uploadService;
    }

    // ========================================================
    // ===== BẮT ĐẦU SỬA LỖI - HOÀN TÁC VỀ CODE GỐC CỦA BẠN =====
    // ========================================================

    public User findById(long id) {
        log.info("[UserService] findById() - ID: {}", id);
        // Trả về code gốc của bạn (vì `findById` của bạn trả về User)
        return this.userRepository.findById(id);
    }

    // ... (getAllUser giữ nguyên) ...
    public List<User> getAllUser() {
        log.info("[UserService] getAllUser() - Lấy danh sách tất cả người dùng");
        return this.userRepository.findAll();
    }

    public User getUserById(long id) {
        log.info("[UserService] getUserById() - ID: {}", id);
        // Trả về code gốc của bạn (vì `findById` của bạn trả về User)
        return this.userRepository.findById(id);
    }

    // ========================================================
    // ===== KẾT THÚC SỬA LỖI =====
    // ========================================================


    public Optional<User> getById(Long id) {
        // (Hàm này có vẻ dùng JpaRepository chuẩn, cứ để nguyên)
        return userRepository.findById(id);
    }

    @Transactional
    public User createUser(User user, MultipartFile avatarFile) {
        log.info("[UserService] createUser() - Bắt đầu tạo user: email='{}'", user.getEmail());

        try {
            // ... (Code của bạn giữ nguyên) ...
            if (user.getEmail() == null || user.getEmail().isBlank()) {
                throw new IllegalArgumentException("Email không được để trống");
            }
            if (userRepository.existsByEmail(user.getEmail())) {
                throw new IllegalArgumentException("Email người dùng đã tồn tại");
            }
            if (avatarFile == null || avatarFile.isEmpty()) {
                throw new IllegalArgumentException("Ảnh đại diện là bắt buộc");
            }
            String avatar = uploadService.HandleSaveUploadFile(avatarFile, "avatar");
            user.setAvatar(avatar);
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            Role selectedRole = getRoleById(user.getRole().getId());
            user.setRole(selectedRole);
            User savedUser = userRepository.save(user);
            log.info("[UserService] createUser() - Tạo user thành công: ID={}, email='{}'", savedUser.getId(), savedUser.getEmail());
            return savedUser;

        } catch (IllegalArgumentException e) {
            log.warn("[UserService] createUser() - Lỗi dữ liệu: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[UserService] createUser() - Lỗi hệ thống: {}", e.getMessage(), e);
            throw new RuntimeException("Đã xảy ra lỗi khi tạo người dùng, vui lòng thử lại sau.");
        }
    }

    @Transactional
    public User updateUser(User user, MultipartFile avatarFile) {
        log.info("[UserService] updateUser() - Bắt đầu cập nhật user: ID={}", user.getId());

        try {
            // Lấy user cũ (Chỗ này nên dùng getById(Long id) vì nó trả về Optional)
            User existingUser = userRepository.getById(user.getId());
            // .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

            String currentEmail = existingUser.getEmail();
            if (avatarFile != null && !avatarFile.isEmpty()) {
                String avatar = uploadService.HandleSaveUploadFile(avatarFile, "avatar");
                existingUser.setAvatar(avatar);
            }
            existingUser.setFullName(user.getFullName());
            existingUser.setPhone(user.getPhone());
            existingUser.setAddress(user.getAddress());
            existingUser.setEmail(currentEmail);
            if (user.getPassword() != null && !user.getPassword().isBlank()) {
                existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
            }
            Role selectedRole = getRoleById(user.getRole().getId());
            existingUser.setRole(selectedRole);
            User savedUser = userRepository.save(existingUser);
            log.info("[UserService] updateUser() - Cập nhật user thành công: ID={}, email='{}'", savedUser.getId(), savedUser.getEmail());
            return savedUser;

        } catch (IllegalArgumentException e) {
            log.warn("[UserService] updateUser() - Lỗi dữ liệu: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[UserService] updateUser() - Lỗi hệ thống: {}", e.getMessage(), e);
            throw new RuntimeException("Đã xảy ra lỗi khi cập nhật người dùng, vui lòng thử lại sau.");
        }
    }


    @Transactional
    public void deleteUser(Long id) {
        log.info("[UserService] deleteUser() - Yêu cầu xóa người dùng ID: {}", id);

        try {
            User user = userRepository.findById(id) // (Giả sử hàm này của bạn trả về User)
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
        // ... (Giữ nguyên code của bạn) ...
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
        // ... (Giữ nguyên code của bạn) ...
        log.info("[UserService] getRoleById() - Tìm role theo ID: {}", id);
        return roleRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[UserService] getRoleById() - Không tìm thấy role với ID: {}", id);
                    return new IllegalArgumentException("Không tìm thấy vai trò với ID: " + id);
                });
    }

    public List<Role> getAllRoles() {
        // ... (Giữ nguyên code của bạn) ...
        log.info("[UserService] getAllRoles() - Lấy danh sách tất cả vai trò");
        return this.roleRepository.findAll();
    }

    public void register(registerDTO dto) {
        // ... (Giữ nguyên code của bạn) ...
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
        // ... (Giữ nguyên code của bạn) ...
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng: " + email));
    }

    @Transactional
    public UserDTO updateProfile(UserDTO dto, MultipartFile avatarFile, String email) {
        // ... (Giữ nguyên code của bạn) ...
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        user.setFullName(dto.getFullName());
        user.setPhone(dto.getPhoneNumber());
        user.setAddress(dto.getAddress());
        if (avatarFile != null && !avatarFile.isEmpty()) {
            String avatar = uploadService.HandleSaveUploadFile(avatarFile, "avatar");
            user.setAvatar(avatar);
        }
        User saved = userRepository.save(user);
        UserDTO result = new UserDTO();
        result.setId(saved.getId());
        result.setFullName(saved.getFullName());
        result.setEmail(saved.getEmail());
        result.setPhoneNumber(saved.getPhone());
        result.setAddress(saved.getAddress());
        result.setAvatarUrl(saved.getAvatar());
        return result;
    }

    public UserDTO getUserByEmail(String email) {
        // ... (Giữ nguyên code của bạn) ...
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhone());
        dto.setAddress(user.getAddress());
        dto.setAvatarUrl(user.getAvatar());
        return dto;
    }


    // ==================================================================
    // ===== BẮT ĐẦU PHẦN THÊM MỚI (PHẦN NÀY ĐÃ ĐÚNG) =====
    // ==================================================================

    /**
     * Lấy thông tin User (Entity) từ CSDL dựa trên người dùng đã đăng nhập.
     * OrderService sẽ gọi hàm này.
     * (Code này vẫn đúng vì findByEmail của bạn trả về Optional<User>)
     */
    public User getCurrentUser() {
        // Lấy thông tin xác thực từ Spring Security Context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            // Người dùng chưa đăng nhập
            log.warn("[UserService] getCurrentUser() - Người dùng chưa xác thực (anonymousUser).");
            return null;
        }

        String email = "";
        Object principal = authentication.getPrincipal();

        // Lấy username (email) từ principal
        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else {
            email = principal.toString();
        }

        if (email.isEmpty()) {
            log.error("[UserService] getCurrentUser() - Không thể trích xuất email từ principal.");
            return null;
        }

        // Tìm User entity (đầy đủ thông tin) bằng email
        User user = this.userRepository.findByEmail(email)
                .orElse(null); // Trả về null nếu không tìm thấy

        if (user == null) {
            log.warn("[UserService] getCurrentUser() - Đã xác thực nhưng không tìm thấy User entity với email: {}", email);
        }

        return user;
    }

    // ==================================================================
    // ===== KẾT THÚC PHẦN THÊM MỚI =====
    // ==================================================================

}