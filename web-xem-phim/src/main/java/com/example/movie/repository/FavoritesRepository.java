package com.example.movie.repository;

import com.example.movie.model.Favorites;
import com.example.movie.model.Movie;
import com.example.movie.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FavoritesRepository extends JpaRepository<Favorites, Long> {
    List<Favorites> findByUserId(Long userId);
    Optional<Favorites> findByUserAndMovie(User user, Movie movie); //
    // Phương thức kiểm tra sự tồn tại của Favorites
    boolean existsByUserAndMovie(User user, Movie movie);// Tìm phim yêu thích theo người dùng và phim
    // Phương thức để lấy danh sách yêu thích theo userId và sắp xếp theo ngày thêm vào
    List<Favorites> findByUserIdOrderByAddedAtDesc(Long userId);
    @Modifying
    @Query("DELETE FROM Favorites f WHERE f.movie.id = :movieId")
    void deleteByMovieId(@Param("movieId") Long movieId);

}
