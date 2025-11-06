package com.lsb.webshop.domain;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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
    private User user;

    @OneToMany(mappedBy = "order")
    List<OrderDetail> orderDetail;

    private String receiverName;
    private String receiverAddress;
    private String receiverPhone;
    private String status; // e.g., "PENDING", "SHIPPED", "DELIVERED", "CANCELLED"

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Payment> payments = new HashSet<>();
}