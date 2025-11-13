package com.lsb.webshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lsb.webshop.domain.Category;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long>{
    Optional<Category> findByName(String name);

    boolean existsByName(String name);

    Optional<Category> findByNameAndIdNot(String name, Long id);

}
