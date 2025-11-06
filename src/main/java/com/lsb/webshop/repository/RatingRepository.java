package com.lsb.webshop.repository;

import com.lsb.webshop.domain.Rating;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RatingRepository extends JpaRepository<Rating, Long> {
}
