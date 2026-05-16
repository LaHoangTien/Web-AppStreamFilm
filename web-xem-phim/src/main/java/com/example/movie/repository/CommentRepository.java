package com.example.movie.repository;

import com.example.movie.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByMovie_MovieId(Long movieId); // Sử dụng Movie_MovieId để truy vấn
    @Modifying
    @Query("DELETE FROM Comment r WHERE r.movie.id = :movieId")
    void deleteByMovieId(@Param("movieId") Long movieId);
    // Trong CommentRepository (Repository của bạn)
    @Query("SELECT c FROM Comment c WHERE c.movie.movieId = :movieId ORDER BY c.createdAt DESC")
    List<Comment> findByMovieIdSortedByCreatedAt(@Param("movieId") Long movieId);

}
