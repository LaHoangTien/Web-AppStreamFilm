package com.example.movie.service;

import com.example.movie.model.Genre;
import com.example.movie.model.Movie;
import com.example.movie.repository.GenreRepository;
import com.example.movie.repository.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GenreService {

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private MovieRepository movieRepository;

    // Phương thức tính tổng lượt xem của từng thể loại
    public void calculateViewCountForGenres() {
        List<Genre> genres = genreRepository.findAll();

        for (Genre genre : genres) {
            long totalViewCount = 0;

            // Tính tổng lượt xem cho mỗi phim thuộc thể loại này
            for (Movie movie : genre.getMovies()) {
                totalViewCount += movie.getViewCount(); // Thêm lượt xem của mỗi phim
            }

            genre.setMovieCount(genre.getMovies().size()); // Cập nhật số lượng phim
            System.out.println("Thể loại: " + genre.getGenreName() + " có " + genre.getMovieCount() + " phim với tổng lượt xem là: " + totalViewCount);
        }
    }
}
