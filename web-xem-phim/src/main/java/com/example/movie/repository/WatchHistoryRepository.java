package com.example.movie.repository;

import com.example.movie.model.Favorites;
import com.example.movie.model.Movie;
import com.example.movie.model.User;
import com.example.movie.model.WatchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface WatchHistoryRepository extends JpaRepository<WatchHistory, Long> {
    List<WatchHistory> findByUser(User user);
    List<WatchHistory> findByUserId(Long userId);
    Optional<WatchHistory> findByUserAndMovie(User user, Movie movie);
    List<WatchHistory> findDistinctByUser(User user);
    @Modifying
    @Query("DELETE FROM WatchHistory w WHERE w.movie.id = :movieId")
    void deleteByMovieId(@Param("movieId") Long movieId);

    @Transactional
    @Modifying
    @Query("DELETE FROM WatchHistory w WHERE w.watchedAt < :thresholdDate")
    void deleteOldHistories(@Param("thresholdDate") Date thresholdDate);

}
