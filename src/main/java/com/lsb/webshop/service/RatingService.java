package com.lsb.webshop.service;

import com.lsb.webshop.domain.Product;
import com.lsb.webshop.domain.Rating;
import com.lsb.webshop.domain.User;
import com.lsb.webshop.repository.ProductRepository;
import com.lsb.webshop.repository.RatingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class RatingService {

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserService userService; // (Bạn đã có)

    /**
     * Xử lý việc người dùng gửi đánh giá
     * @param productId ID sản phẩm
     * @param score điểm số (1-5)
     * @param comment bình luận
     */
    @Transactional
    public void submitRating(Long productId, int score, String comment) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new SecurityException("Bạn cần đăng nhập để đánh giá.");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại"));

        // Kiểm tra xem user đã đánh giá sản phẩm này chưa, nếu có thì cập nhật
        Rating rating = ratingRepository.findByUserAndProduct(currentUser, product)
                .orElse(new Rating()); // Tạo mới nếu chưa có

        rating.setUser(currentUser);
        rating.setProduct(product);
        rating.setScore(score);
        rating.setComment(comment);
        rating.setCreatedAt(LocalDateTime.now()); // (Nếu bạn có cột này)

        ratingRepository.save(rating);
    }
}