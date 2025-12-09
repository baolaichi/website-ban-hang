package com.lsb.webshop.domain;

import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnore; // Import

@Entity
@Data
@Getter
@Setter
@Table(name = "users")
@SQLDelete(sql = "UPDATE users SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Email(message = "Email không hợp lệ", regexp = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$")
    private String email;

    @NotNull
    @Size(min = 6, message = "Mật khẩu phải có tối thiểu 6 kí tự")
    private String password;

    @NotNull
    private String fullName;

    private String address;
    private String phone;
    private String avatar;
    private boolean deleted = false;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role; // Giữ lại Role để biết User là ai

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore // <--- CẮT VÒNG LẶP: Xem User không cần lôi giỏ hàng ra ngay
    private Cart cart;

    @OneToMany(mappedBy = "user")
    @JsonIgnore // <--- CẮT: Không load lịch sử đánh giá
    private Set<Rating> ratings = new HashSet<>();

    @OneToMany(mappedBy = "user")
    @JsonIgnore // <--- CẮT: Không load lịch sử xem hàng
    private Set<ProductView> productViews = new HashSet<>();

    @OneToMany(mappedBy = "user")
    @JsonIgnore // <--- CẮT: Không load lịch sử chat
    private Set<ChatLog> chatLogs = new HashSet<>();

    @OneToMany(mappedBy = "user")
    @JsonIgnore // <--- CẮT: Không load toàn bộ đơn hàng
    private Set<Order> orders = new HashSet<>();
}