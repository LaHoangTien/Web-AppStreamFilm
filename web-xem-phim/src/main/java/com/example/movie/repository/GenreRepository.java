package com.example.movie.repository;

import com.example.movie.model.Genre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GenreRepository extends JpaRepository<Genre, Long> {
    boolean existsByGenreName(String genreName);
    Page<Genre> findByGenreNameContainingIgnoreCase(String genreName, Pageable pageable);
    List<Genre> findAll();
    Genre findByGenreName(String genreName);
}
