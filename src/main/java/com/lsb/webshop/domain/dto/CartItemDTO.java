package com.lsb.webshop.domain.dto;

import com.lsb.webshop.domain.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItemDTO {
    private Long ProductId;
    private String ProductName;
    private Double Price;
    private Integer Quantity;
    private String UserId;

}
