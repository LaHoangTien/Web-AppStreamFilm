package com.example.movie.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "favorites")
public class Favorites {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long favoriteId; // ID của danh sách yêu thích

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Người dùng

    @ManyToOne
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie; // Phim yêu thích

    private LocalDateTime addedAt;

    // Thêm constructor mới nếu cần thiết
    public Favorites(User user, Movie movie) {
        this.user = user;
        this.movie = movie;
    }
    @PrePersist
    protected void onCreate() {
        this.addedAt = LocalDateTime.now(); // Gán thời gian hiện tại khi tạo mới
    }

}
