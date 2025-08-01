package com.lsb.webshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lsb.webshop.domain.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>{
    
}
