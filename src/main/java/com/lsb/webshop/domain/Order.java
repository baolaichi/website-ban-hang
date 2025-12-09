package com.lsb.webshop.domain;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Getter
@Setter
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private double totalPrice;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @OneToMany(mappedBy = "order")
    List<OrderDetail> orderDetail;

    private String receiverName;
    private String receiverAddress;
    private String receiverPhone;
    private String status; // e.g., "PENDING", "SHIPPED", "DELIVERED", "CANCELLED"

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Payment> payments = new HashSet<>();

    @CreationTimestamp // Tự động gán ngày giờ khi tạo
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}