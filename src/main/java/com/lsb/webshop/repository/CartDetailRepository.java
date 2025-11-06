package com.lsb.webshop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lsb.webshop.domain.Cart;
import com.lsb.webshop.domain.CartDetail;
import com.lsb.webshop.domain.Product;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface CartDetailRepository extends JpaRepository<CartDetail, Long>{
    boolean existsByCartAndProduct(Cart cart, Product product);

    CartDetail findByCartAndProduct(Cart cartId, Product productId);

    List<CartDetail> findByCart(Cart cart);

    @Modifying
    @Query("DELETE FROM CartDetail cd WHERE cd.cart.id = :cartId")
    void deleteByCartId(@Param("cartId") Long cartId);

}
