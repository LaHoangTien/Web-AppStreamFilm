package com.example.movie.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "episodes")
public class Episode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long episodeId; // ID tập phim

    @ManyToOne
    @JoinColumn(name = "movie_id", nullable = false)
    @JsonBackReference
    private Movie movie; // Phim mà tập này thuộc về

    @Column(name = "episode_number", nullable = false)
    private String episodeNumber; // Số thứ tự của tập phim


    @Column(name = "title")
    private String title; // Tên tập phim

    @Column(name = "video_url")
    private String videoUrl; // Đường dẫn đến video của tập phim
}
