package com.example.movie.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Setter
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "reviews")
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId; // ID của đánh giá

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user; // Người dùng đã đánh giá

    @ManyToOne
    @JoinColumn(name = "movie_id", nullable = false)
    @JsonBackReference
    private Movie movie; // Phim mà đánh giá thuộc về

    @Column(name = "rating", nullable = false)
    private Integer rating; // Điểm đánh giá (1-10 sao)

    @Column(name = "created_at", nullable = false)
    private Date createdAt; // Ngày tạo đánh giá

    // Kiểm tra tính hợp lệ của rating
    public void setRating(Integer rating) {
        if (rating < 1 || rating > 10) {
            throw new IllegalArgumentException("Rating must be between 1 and 10.");
        }
        this.rating = rating;
    }
}
