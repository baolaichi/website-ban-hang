package com.lsb.webshop.domain;

import java.util.List;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnore; // Import

@Entity
@Table(name="carts")
@Data
@Getter
@Setter
public class Cart {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private long id;

    @Min(value = 0)
    private int sum;
    private boolean status;

    @OneToOne()
    @JoinColumn(name = "user_id")
    @JsonIgnore // <--- CẮT VÒNG LẶP (Quan trọng)
    private User user;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartDetail> cartDetails;
}