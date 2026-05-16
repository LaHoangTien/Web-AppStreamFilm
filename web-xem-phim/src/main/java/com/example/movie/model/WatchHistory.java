package com.example.movie.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Setter
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "watch_history")
public class WatchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long historyId; // ID của lịch sử xem

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Người dùng đã xem

    @ManyToOne
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie; // Phim đã xem

    @Column(name = "watched_at", nullable = false)
    private Date watchedAt; // Thời gian xem phim
}
