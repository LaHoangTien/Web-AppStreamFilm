package com.example.movie.controller;

import com.example.movie.model.*;
import com.example.movie.repository.MovieRepository;
import com.example.movie.repository.UserRepository;
import com.example.movie.repository.WatchHistoryRepository;
import com.example.movie.repository.FavoritesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.stereotype.Controller;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class RecommendationController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WatchHistoryRepository watchHistoryRepository;

    @Autowired
    private FavoritesRepository favoritesRepository;

    @Autowired
    private MovieRepository movieRepository;

    @GetMapping("/recommendations")
    public String getRecommendations(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Long userId = null;

        // Lấy thông tin người dùng từ UserDetails
        if (userDetails != null) {
            User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            if (user != null) {
                userId = user.getId(); // Lấy ID người dùng
            }
        }

        if (userId == null) {
            return "error/403"; // Trả về trang lỗi nếu không tìm thấy người dùng
        }

        // Lấy danh sách phim yêu thích
        List<Favorites> favoritesList = favoritesRepository.findByUserId(userId);
        List<Movie> favoriteMovies = favoritesList.stream().map(Favorites::getMovie).collect(Collectors.toList());

        // Lấy lịch sử xem phim
        List<WatchHistory> historyList = watchHistoryRepository.findByUserId(userId);
        List<Movie> watchedMovies = historyList.stream().map(WatchHistory::getMovie).collect(Collectors.toList());

        // Gợi ý phim dựa trên phim yêu thích và lịch sử xem
        Set<Movie> recommendedMovies = new HashSet<>();

        // Gợi ý dựa trên phim cùng thể loại với phim yêu thích
        for (Movie favoriteMovie : favoriteMovies) {
            List<Genre> genres = favoriteMovie.getGenres();
            if (genres != null && !genres.isEmpty()) {
                for (Genre genre : genres) {
                    List<Movie> similarMovies = movieRepository.findByGenresContaining(genre);
                    recommendedMovies.addAll(similarMovies);
                }
            }
        }

        // Gợi ý dựa trên phim cùng thể loại với lịch sử xem gần đây
        for (Movie watchedMovie : watchedMovies) {
            List<Genre> genres = watchedMovie.getGenres();
            if (genres != null && !genres.isEmpty()) {
                for (Genre genre : genres) {
                    List<Movie> similarMovies = movieRepository.findByGenresContaining(genre);
                    recommendedMovies.addAll(similarMovies);
                }
            }
        }
        // Nếu danh sách gợi ý rỗng, lấy danh sách phim mới nhất
        if (recommendedMovies.isEmpty()) {
            List<Movie> newestMovies = movieRepository.findTop10ByOrderByCreatedAtDesc(); // Lấy 10 phim mới nhất
            model.addAttribute("newestMovies", newestMovies); // Truyền danh sách phim mới nhất vào model
        }
        // Loại bỏ các phim mà người dùng đã xem hoặc đã yêu thích
        recommendedMovies.removeIf(movie -> watchedMovies.stream().anyMatch(watched -> watched.getMovieId().equals(movie.getMovieId())));
        recommendedMovies.removeIf(movie -> favoriteMovies.stream().anyMatch(favorite -> favorite.getMovieId().equals(movie.getMovieId())));

        model.addAttribute("recommendedMovies", new ArrayList<>(recommendedMovies));
        return "Home/recommendations"; // Đảm bảo rằng đường dẫn này đúng
    }
}