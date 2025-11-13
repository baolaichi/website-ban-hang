package com.lsb.webshop.repository;

import com.lsb.webshop.domain.Product;
import com.lsb.webshop.domain.Rating;
import com.lsb.webshop.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    // Hàm này dùng để kiểm tra xem user đã đánh giá SP này chưa
    Optional<Rating> findByUserAndProduct(User user, Product product);
}
