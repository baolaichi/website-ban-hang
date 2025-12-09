package com.lsb.webshop.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore; // Import
import java.time.LocalDateTime;

@Entity
@Table(name = "product_views")
@Getter
@Setter
public class ProductView {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore // <--- CẮT
    private User user;

    @Column(name = "session_id", length = 255)
    private String sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore // <--- CẮT
    private Product product;

    @CreationTimestamp
    @Column(name = "viewed_at", updatable = false, nullable = false)
    private LocalDateTime viewedAt;
}