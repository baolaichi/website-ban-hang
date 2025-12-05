package com.lsb.webshop.repository;

import com.lsb.webshop.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.lsb.webshop.domain.Cart;
import com.lsb.webshop.domain.User;

import java.util.Optional;


@Repository
public interface CartRepository extends JpaRepository<Cart, Long>{
    Cart findByUser(User user);

    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.cartDetails WHERE c.user = :user AND c.status = :status")
    Cart findByUserAndStatus(@Param("user") User user, @Param("status") boolean status);


}
