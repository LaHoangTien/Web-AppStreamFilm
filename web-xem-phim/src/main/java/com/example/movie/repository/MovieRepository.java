package com.example.movie.repository;

import com.example.movie.model.Movie;
import com.example.movie.model.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
    List<Movie> findByGenresContaining(Genre genre);

    List<Movie> findTop10ByOrderByCreatedAtDesc();

    @Modifying
    @Query("UPDATE Movie m SET m.weeklyViewCount = 0")
    void resetWeeklyViewCount();

    List<Movie> findAllByOrderByUpdatedAtDesc();

    // Truy vấn các phim có cùng thể loại
    @Query("SELECT m FROM Movie m JOIN m.genres g WHERE g IN :genres AND m.slug != :slug")
    List<Movie> findMoviesByGenres(@Param("genres") List<Genre> genres, @Param("slug") String slug);

    List<Movie> findByStatusNot(String status);

    Optional<Movie> findBySlug(String slug);


}
