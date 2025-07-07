package com.lsb.web_shop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lsb.web_shop.domain.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User save(User lsb);

    Optional<User> findById(Long id);

    List<User> findAll();

    User findById(long id);

    void deleteById(Long id);

}