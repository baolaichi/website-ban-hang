package com.lsb.webshop.controller.client;

import com.lsb.webshop.service.RatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RatingController {

    @Autowired
    private RatingService ratingService;

    /**
     * Nhận form đánh giá từ trang chi tiết sản phẩm
     */
    @PostMapping("/submit-rating")
    public String submitRating(@RequestParam("productId") Long productId,
                               @RequestParam("score") int score,
                               @RequestParam(value = "comment", required = false) String comment,
                               RedirectAttributes redirectAttributes) {

        try {
            ratingService.submitRating(productId, score, comment);
            redirectAttributes.addFlashAttribute("ratingSuccess", "Cảm ơn bạn đã đánh giá sản phẩm!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("ratingError", "Đã xảy ra lỗi: " + e.getMessage());
        }

        // Quay lại trang sản phẩm vừa đánh giá
        return "redirect:/product/" + productId;
    }
}