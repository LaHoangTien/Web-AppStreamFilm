package com.example.movie.repository;

import com.example.movie.model.Comment;
import com.example.movie.model.Movie;
import com.example.movie.model.Rating;
import com.example.movie.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    List<Rating> findByMovie(Movie movie);  // Bạn có thể thêm các phương thức tùy chỉnh ở đây nếu cần
    boolean existsByUserAndMovie(User user, Movie movie);
    // Tính toán điểm trung bình của đánh giá cho một phim
    @Query("SELECT AVG(r.rating) FROM Rating r WHERE r.movie = :movie")
    Double findAverageRatingByMovie(@Param("movie") Movie movie);

    // Đếm số lượng đánh giá cho một phim
    @Query("SELECT COUNT(r) FROM Rating r WHERE r.movie = :movie")
    Long countByMovie(@Param("movie") Movie movie);

    @Modifying
    @Query("DELETE FROM Rating r WHERE r.movie.id = :movieId")
    void deleteByMovieId(@Param("movieId") Long movieId);

}
