package com.lsb.webshop.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long id;
    private String name;
    private double price;
    private String image;
    private String shortDesc;

    // constructor chỉ 3 tham số
    public ProductDTO(Long id, String name, String shortDesc) {
        this.id = id;
        this.name = name;
        this.shortDesc = shortDesc;
    }
}

