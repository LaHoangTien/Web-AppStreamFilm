package com.example.movie.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Setter
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "actors")
public class Actor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long actorId; // ID diễn viên

    @Column(name = "actor_name", nullable = false, unique = true)
    private String actorName; // Tên diễn viên

    @ManyToMany(mappedBy = "actors")
    @JsonBackReference
    private List<Movie> movies; // Danh sách phim mà diễn viên tham gia

    private long movieCount; // Số lượng phim mà diễn viên tham gia
}
