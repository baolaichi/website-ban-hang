package com.lsb.webshop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lsb.webshop.domain.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User save(User lsb);

    Optional<User> findById(Long id);

    List<User> findAll();

    User findById(long id);

    void deleteById(Long id);

    boolean existsByFullName(String fullName);
    
    boolean existsByEmail(String email);

    boolean existsByFullNameAndIdNot(String fullName, Long id);
    
    boolean existsByEmailAndIdNot(String email, Long id);

    Optional<User> findByEmail(String email);

}