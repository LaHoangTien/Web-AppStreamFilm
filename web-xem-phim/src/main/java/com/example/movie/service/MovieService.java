package com.example.movie.service;

import com.example.movie.model.Movie;
import com.example.movie.repository.*;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.movie.model.*;

import com.example.movie.repository.MovieRepository;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;


import java.util.*;
import java.util.stream.Collectors;

@Service
public class MovieService {
//    @Autowired
//    private MovieDocumentRepository movieDocumentRepository;
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private FavoritesRepository favoritesRepository;

    @Autowired
    private WatchHistoryRepository watchHistoryRepository;

//    @Autowired
//    private MovieSynchronizationService movieSynchronizationService;

    @Autowired
    private RatingRepository ratingRepository;

//    @Autowired
//    public MovieService(MovieRepository movieRepository, MovieSynchronizationService movieSynchronizationService) {
//        this.movieRepository = movieRepository;
//        this.movieSynchronizationService = movieSynchronizationService;
//    }


    public Map<String, List<Movie>> categorizeMovies(List<Movie> movies) {
        List<Movie> moviesTheater = new ArrayList<>();
        List<Movie> moviesSeries = new ArrayList<>();
        List<Movie> moviesAnime = new ArrayList<>();
        List<Movie> moviesHorror = new ArrayList<>();
        List<Movie> moviesDramaKorean = new ArrayList<>();
        List<Movie> moviesChineseHistoricalDrama = new ArrayList<>();

        // Phân loại phim vào các danh sách riêng
        for (Movie movie : movies) {
            boolean isAnime = movie.getGenres().stream()
                    .anyMatch(genre -> genre.getGenreName().equalsIgnoreCase("Hoạt Hình"));
            boolean isHorror = movie.getGenres().stream()
                    .anyMatch(genre -> genre.getGenreName().equalsIgnoreCase("Kinh Dị"));
            boolean isDramaKorean = movie.getGenres().stream()
                    .anyMatch(genre -> genre.getGenreName().equalsIgnoreCase("Chính Kịch")) &&
                    movie.getCountry() != null &&
                    movie.getCountry().getCountryName().equalsIgnoreCase("Hàn Quốc");
            boolean isChineseHistoricalDrama = movie.getGenres().stream()
                    .anyMatch(genre -> genre.getGenreName().equalsIgnoreCase("Cổ Trang")) &&
                    movie.getCountry() != null &&
                    movie.getCountry().getCountryName().equalsIgnoreCase("Trung Quốc");


            if (isAnime) {
                moviesAnime.add(movie);
            }else if (isHorror) {
                moviesHorror.add(movie);
            }else if (isDramaKorean) {
                moviesDramaKorean.add(movie);
            }else if (isChineseHistoricalDrama) {
                moviesChineseHistoricalDrama.add(movie);
            } else if (movie.isSeries()) {
                moviesSeries.add(movie);
            } else {
                moviesTheater.add(movie);
            }
        }

        // Chỉ lấy 8 phim đầu tiên từ mỗi danh sách
        moviesTheater = moviesTheater.size() > 8 ? moviesTheater.subList(0, 8) : moviesTheater;
        moviesSeries = moviesSeries.size() > 8 ? moviesSeries.subList(0, 8) : moviesSeries;
        moviesAnime = moviesAnime.size() > 8 ? moviesAnime.subList(0, 8) : moviesAnime;
        moviesHorror = moviesHorror.size() > 8 ? moviesHorror.subList(0, 8) : moviesHorror;
        moviesDramaKorean = moviesDramaKorean.size() > 8 ? moviesDramaKorean.subList(0, 8) : moviesDramaKorean;
        moviesChineseHistoricalDrama = moviesChineseHistoricalDrama.size() > 8 ? moviesChineseHistoricalDrama.subList(0, 8) : moviesChineseHistoricalDrama;

        // Đưa các danh sách vào bản đồ để trả về
        Map<String, List<Movie>> categorizedMovies = new HashMap<>();
        categorizedMovies.put("moviesTheater", moviesTheater);
        categorizedMovies.put("moviesSeries", moviesSeries);
        categorizedMovies.put("moviesAnime", moviesAnime);
        categorizedMovies.put("moviesHorror", moviesHorror);
        categorizedMovies.put("moviesDramaKorean", moviesDramaKorean); // Thêm danh mục mới
        categorizedMovies.put("moviesChineseHistoricalDrama", moviesChineseHistoricalDrama);
        return categorizedMovies;
    }



    // Gợi ý phim dựa trên lịch sử xem
    public Set<Movie> getRecommendedMovies(Long userId) {
        Set<Movie> recommendedMovies = new HashSet<>();

        if (userId != null) {
            List<WatchHistory> historyList = watchHistoryRepository.findByUserId(userId);
            List<Movie> watchedMovies = historyList.stream()
                    .map(WatchHistory::getMovie)
                    .collect(Collectors.toList());

            // Duyệt qua các thể loại từ phim yêu thích và lịch sử xem
            List<Movie> similarMovies;

            for (Movie watchedMovie : watchedMovies) {
                for (Genre genre : watchedMovie.getGenres()) {
                    similarMovies = movieRepository.findByGenresContaining(genre);

                    // Thêm phim vào danh sách nếu điểm trung bình của nó lớn hơn 7
                    for (Movie movie : similarMovies) {
                        Double averageRating = ratingRepository.findAverageRatingByMovie(movie);
                        if (averageRating != null && averageRating >= 7) {
                            recommendedMovies.add(movie);
                        }
                    }
                }
            }

            // Loại bỏ các phim đã xem
            recommendedMovies.removeAll(watchedMovies);
        }

        return recommendedMovies;
    }


    // Đặt lịch chạy vào mỗi thứ Hai lúc 00:00
    @Scheduled(cron = "0 0 0 * * MON")
    @Transactional
    public void resetWeeklyViewCount() {
        movieRepository.resetWeeklyViewCount();
    }

    public List<Movie> getTop6HighestRatedMovies() {
        List<Movie> allMovies = movieRepository.findAll();

        return allMovies.stream()
                .filter(movie -> {
                    Double averageRating = ratingRepository.findAverageRatingByMovie(movie);
                    return averageRating != null;
                })
                .sorted((m1, m2) -> {
                    Double rating1 = ratingRepository.findAverageRatingByMovie(m1);
                    Double rating2 = ratingRepository.findAverageRatingByMovie(m2);
                    return Double.compare(rating2, rating1); // Sắp xếp giảm dần theo điểm đánh giá
                })
                .limit(6)
                .collect(Collectors.toList());
    }

    public List<Movie> getTop10HighestRatedMovies() {
        List<Movie> allMovies = movieRepository.findAll();

        return allMovies.stream()
                .filter(movie -> {
                    Double averageRating = ratingRepository.findAverageRatingByMovie(movie);
                    return averageRating != null;
                })
                .sorted((m1, m2) -> {
                    Double rating1 = ratingRepository.findAverageRatingByMovie(m1);
                    Double rating2 = ratingRepository.findAverageRatingByMovie(m2);
                    return Double.compare(rating2, rating1); // Sắp xếp giảm dần theo điểm đánh giá
                })
                .limit(10)
                .collect(Collectors.toList());
    }
    public List<Movie> getTop5HotMoviesSeries() {
        List<Movie> allMovies = movieRepository.findAll();
        return allMovies.stream()
                .filter(Movie::isSeries) // Lọc chỉ phim bộ
                .sorted(Comparator
                        .comparing(Movie::getCalculatedViewCount) // So sánh theo weeklyViewCount
                        .thenComparing(Movie::getViewCount) // Nếu weeklyViewCount bằng nhau, so sánh theo viewCount
                        .reversed()) // Sắp xếp theo thứ tự giảm dần
                .limit(5)
                .collect(Collectors.toList());
    }

    public List<Movie> getTop5HotMoviesTheater() {
        List<Movie> allMovies = movieRepository.findAll();
        return allMovies.stream()
                .filter(movie -> !movie.isSeries()) // Lọc chỉ phim lẻ
                .sorted(Comparator
                        .comparing(Movie::getCalculatedViewCount) // So sánh theo weeklyViewCount
                        .thenComparing(Movie::getViewCount) // Nếu weeklyViewCount bằng nhau, so sánh theo viewCount
                        .reversed()) // Sắp xếp theo thứ tự giảm dần
                .limit(5)
                .collect(Collectors.toList());
    }


}
