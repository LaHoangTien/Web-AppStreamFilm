package com.example.movie.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Setter
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "genres")
public class Genre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long genreId; // ID của thể loại

    @Column(name = "genre_name", nullable = false)
    private String genreName; // Tên của thể loại

    @ManyToMany(mappedBy = "genres")
    @JsonBackReference
    private List<Movie> movies;


    private long movieCount;

    private long totalViewCount;
}
