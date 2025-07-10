package com.lsb.webshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lsb.webshop.domain.Category;

public interface CategoryRepository extends JpaRepository<Category, Long>{
    
}
