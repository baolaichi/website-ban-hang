package com.lsb.webshop.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnore; // Import

@Entity
@Table(name = "cart_details")
@Data
@Getter
@Setter
public class CartDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private long quantity;
    private double price;

    @ManyToOne
    @JoinColumn(name = "cart_id")
    @JsonIgnore // <--- CẮT VÒNG LẶP
    private Cart cart;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
}