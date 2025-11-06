package com.lsb.webshop.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_views")
@Getter
@Setter
public class ProductView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Liên kết với Người dùng (có thể null nếu là khách)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // nullable = true là mặc định
    private User user;

    @Column(name = "session_id", length = 255)
    private String sessionId; // Dùng để theo dõi khách vãng lai

    // Liên kết với Sản phẩm
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @CreationTimestamp
    @Column(name = "viewed_at", updatable = false, nullable = false)
    private LocalDateTime viewedAt;
}
