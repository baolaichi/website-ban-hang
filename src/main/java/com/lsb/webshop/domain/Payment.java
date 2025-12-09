package com.lsb.webshop.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Liên kết với Đơn hàng
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore
    private Order order;

    @Column(name = "payment_method", length = 50, nullable = false)
    private String paymentMethod; // Ví dụ: "VNPAY", "MOMO", "COD"

    @Column(nullable = false)
    private Double amount;

    @Column(name = "transaction_code", length = 255)
    private String transactionCode; // Mã giao dịch từ cổng thanh toán

    @Column(length = 50, nullable = false)
    private String status; // Ví dụ: "PENDING", "SUCCESS", "FAILED"

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;
}
