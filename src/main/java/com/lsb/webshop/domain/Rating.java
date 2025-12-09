package com.lsb.webshop.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore; // Import
import java.time.LocalDateTime;

@Entity
@Table(name = "ratings")
@Getter
@Setter
public class Rating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore // <--- CẮT VÒNG LẶP
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore // <--- CẮT VÒNG LẶP
    private Product product;

    @Column(nullable = false)
    private Integer score;
    @Column(columnDefinition = "TEXT")
    private String comment;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    // Trick: Nếu muốn hiện tên người đánh giá ra JSON
    public String getAuthorName() {
        return user != null ? user.getFullName() : "Anonymous";
    }
}