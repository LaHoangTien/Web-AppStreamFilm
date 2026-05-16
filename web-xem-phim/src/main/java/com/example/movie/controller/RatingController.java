package com.example.movie.controller;

import com.example.movie.model.*;
import com.example.movie.repository.MovieRepository;
import com.example.movie.repository.RatingRepository;
import com.example.movie.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;

import org.elasticsearch.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;



import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.security.Principal;
import java.util.*;

@Controller
@RequestMapping("/phim")
public class RatingController {

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MovieRepository movieRepository;

    @PostMapping("/{id}/ratings")
    public String addRating(@PathVariable Long id,
                            @RequestParam Integer rating,
                            Principal principal,
                            RedirectAttributes redirectAttributes,
                            HttpServletRequest request) {
        if (principal == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần đăng nhập để bình luận.");
            return "redirect:/login"; // Chuyển hướng tới trang đăng nhập
        }

        // Lưu đường dẫn cũ để trả về
        String referer = request.getHeader("Referer");
        String username = principal.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found"));

        if (ratingRepository.existsByUserAndMovie(user, movie)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn đã đánh giá phim này rồi."); // Thông báo lỗi
            return "redirect:" + referer; // Chuyển hướng lại trang phim
        }

        Rating review = new Rating();
        review.setRating(rating);
        review.setUser(user);
        review.setMovie(movie);
        review.setCreatedAt(new Date());

        ratingRepository.save(review);

        // Cập nhật điểm trung bình và số lượng đánh giá
        double averageRating = ratingRepository.findAverageRatingByMovie(movie);
        long totalRatings = ratingRepository.countByMovie(movie);

        redirectAttributes.addFlashAttribute("successMessage", "Đánh giá của bạn đã thành công."); // Thông báo thành công
        redirectAttributes.addFlashAttribute("averageRating", averageRating);
        redirectAttributes.addFlashAttribute("totalRatings", totalRatings);

        return "redirect:" + referer; // Chuyển hướng lại đường dẫn cũ
    }

}
