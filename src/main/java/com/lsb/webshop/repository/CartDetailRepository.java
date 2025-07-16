package com.lsb.webshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lsb.webshop.domain.CartDetail;
import com.lsb.webshop.domain.Cart;
import com.lsb.webshop.domain.Product;

public interface CartDetailRepository extends JpaRepository<CartDetail, Long>{
    boolean existsByCartAndProduct(Cart cart, Product product);

    CartDetail findByCartAndProduct(Cart cartId, Product productId);
}
