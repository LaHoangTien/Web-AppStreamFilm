package com.example.movie.controller;

import com.example.movie.model.*;
import com.example.movie.repository.*;
import com.example.movie.service.BannerAdsService;
import com.example.movie.service.MovieService;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

import org.elasticsearch.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.PageRequest;


import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;



import org.springframework.data.domain.Pageable;

import java.security.Principal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class HomeController {
    @Autowired
    private  MovieService movieService;

    @Autowired
    private MovieRepository movieRepository; // Khai báo repository

    @Autowired
    private WatchHistoryRepository watchHistoryRepository; // Khai báo repository

    @Autowired
    private CommentRepository commentRepository; // Khai báo repository

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private UserRepository userRepository; // Khai báo repository

    @Autowired
    private EpisodeRepository episodeRepository;

    @Autowired
    private FavoritesRepository favoritesRepository;

    @Autowired
    private  BannerAdsService bannerAdsService;
    @GetMapping("/api")
    public String moviesPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String profileImage = null;
        if (userDetails != null) {
            User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            if (user != null) {
                profileImage = user.getProfileImage(); // Lấy đường dẫn ảnh đại diện
            }
        }
        model.addAttribute("profileImage", profileImage);
        return "Admin/Ophim"; // Tên file HTML, không cần đuôi .html
    }
    @GetMapping("/add-ad")
    public String ads() {
        return "Admin/Ads"; // Tên file HTML, không cần đuôi .html
    }
    @GetMapping("/mv")
    public String movies() {
        return "list"; // Tên file HTML, không cần đuôi .html
    }
    @GetMapping("/")
    public String getMoviesPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Long userId = null;
        String profileImage = null;
        if (userDetails != null) {
            User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            if (user != null) {
                userId = user.getId(); // Lấy ID người dùng
                profileImage = user.getProfileImage(); // Lấy đường dẫn ảnh đại diện
            }
        }

        // Lấy danh sách phim đã sắp xếp theo thời gian cập nhật
        List<Movie> movies = movieRepository.findAllByOrderByUpdatedAtDesc();

        Map<String, List<Movie>> categorizedMovies = movieService.categorizeMovies(movies);
        List<Movie> moviesTheater = categorizedMovies.get("moviesTheater");
        List<Movie> moviesSeries = categorizedMovies.get("moviesSeries");
        List<Movie> moviesAnime = categorizedMovies.get("moviesAnime");

        // Lấy gợi ý phim
        Set<Movie> recommendedMovies = movieService.getRecommendedMovies(userId);

        List<Movie> top6HotMovies = movieService.getTop6HighestRatedMovies();


        List<Movie> top5HotMoviesSeries = movieService.getTop5HotMoviesSeries();
        List<Movie> top5HotMoviesTheater = movieService.getTop5HotMoviesTheater();

        List<BannerAds> topBanners = bannerAdsService.getActiveBannersByPosition("top");
        List<BannerAds> bottomBanners = bannerAdsService.getActiveBannersByPosition("bottom");
        model.addAttribute("topBanners", topBanners);
        model.addAttribute("bottomBanners", bottomBanners);
        model.addAttribute("top6HotMovies", top6HotMovies);
        model.addAttribute("moviesSeriesWithRatings", mapMoviesWithRatings(top5HotMoviesSeries));
        model.addAttribute("moviesTheaterWithRatings", mapMoviesWithRatings(top5HotMoviesTheater));
        model.addAttribute("moviesTheater", moviesTheater);
        model.addAttribute("moviesSeries", moviesSeries);
        model.addAttribute("moviesAnime", moviesAnime);
        model.addAttribute("recommendedMovies", new ArrayList<>(recommendedMovies));
        model.addAttribute("userId", userId);
        model.addAttribute("profileImage", profileImage);
        return "Home/home";
    }

    private List<Map<String, Object>> mapMoviesWithRatings(List<Movie> movies) {
        return movies.stream()
                .map(movie -> {
                    Double averageRating = ratingRepository.findAverageRatingByMovie(movie);
                    Map<String, Object> movieData = new HashMap<>();
                    movieData.put("movie", movie);
                    movieData.put("averageRating", averageRating != null ? averageRating : 0.0);
                    return movieData;
                })
                .collect(Collectors.toList());
    }





    @GetMapping("/403")
    public String getIndex() {
        return "/403"; // Trả về tên của tập tin HTML html
    }

    @Transactional
    @GetMapping("/phimbo/xemtap/{slug}")
    public String getXemtap(@AuthenticationPrincipal UserDetails userDetails, @PathVariable("slug") String slug, @RequestParam(required = false) Long episodeId, Model model, Principal principal) {
        Movie movie = movieRepository.findBySlug(slug).orElse(null);
        Long userId = null;
        String profileImage = null;
        if (userDetails != null) {
            User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            if (user != null) {
                userId = user.getId(); // Lấy ID người dùng
                profileImage = user.getProfileImage(); // Lấy đường dẫn ảnh đại diện
            }
        }
        // Kiểm tra và khởi tạo viewCount nếu là null
        if (movie.getViewCount() == null) {
            movie.setViewCount(0L);  // Đặt giá trị mặc định là 0
        }


        // Tăng lượt xem sau 10 phút
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // Cập nhật lượt xem
                movie.setViewCount(movie.getViewCount() + 1);
                movie.setWeeklyViewCount(movie.getWeeklyViewCount() + 1);
                movieRepository.save(movie); // Lưu cập nhật vào cơ sở dữ liệu
            }
        },60000 ); // 10 phút (600000 milliseconds)


        model.addAttribute("movie", movie);
        List<Episode> episodes = movie.getEpisodes();
        Episode selectedEpisode = null;
        String videoUrl;

        if (episodes == null || episodes.isEmpty()) {
            // Không có tập nào, sử dụng trailer
            videoUrl = movie.getTrailerUrl(); // Lấy đường dẫn trailer từ cơ sở dữ liệu
        } else {
            // Có tập phim, chọn tập phim phù hợp
            if (episodeId != null) {
                selectedEpisode = episodeRepository.findById(episodeId)
                        .orElseThrow(() -> new ResourceNotFoundException("Episode not found"));
            } else {
                // Nếu không chọn tập cụ thể, lấy tập đầu tiên
                selectedEpisode = episodes.get(0);
            }

            // Nếu selectedEpisode không null, lấy đường dẫn video từ tập phim
            videoUrl = selectedEpisode != null ? selectedEpisode.getVideoUrl() : movie.getTrailerUrl();
        }

        // Lấy người dùng hiện tại nếu có
        if (principal != null) {
            User user = userRepository.findByUsername(principal.getName()).orElse(null);
            if (user != null) {
                // Tạo lịch sử xem phim
                WatchHistory history = WatchHistory.builder()
                        .user(user)
                        .movie(movie)
                        .watchedAt(new Date())
                        .build();
                watchHistoryRepository.save(history);
            }
        }
        List<Movie> top5HotMoviesSeries = movieService.getTop5HotMoviesSeries();
        List<Movie> top5HotMoviesTheater = movieService.getTop5HotMoviesTheater();


        double averageRating = movie.getRatings()
                .stream()
                .mapToInt(Rating::getRating)
                .average()
                .orElse(0.0); // Tính điểm trung bình ban đầu

        model.addAttribute("movie", movie);

        // Lấy danh sách tất cả bình luận cho phim
        // Lấy danh sách tất cả bình luận cho phim
        List<Comment> allComments = commentRepository.findByMovieIdSortedByCreatedAt(movie.getMovieId());

        // Tạo danh sách bình luận cha
        List<Comment> parentComments = allComments.stream()
                .filter(comment -> comment.getParentComment() == null)
                .collect(Collectors.toList());

        long parentCommentsCount = allComments.stream()
                .filter(comment -> comment.getParentComment() == null) // lọc bình luận chính
                .count();


        long repliesCount = allComments.stream()
                .filter(comment -> comment.getParentComment() != null) // lọc bình luận phản hồi
                .count();
        List<BannerAds> topBanners = bannerAdsService.getActiveBannersByPosition("top");
        List<BannerAds> bottomBanners = bannerAdsService.getActiveBannersByPosition("bottom");
        model.addAttribute("topBanners", topBanners);
        model.addAttribute("bottomBanners", bottomBanners);

        model.addAttribute("parentCommentsCount", parentCommentsCount);
        model.addAttribute("repliesCount", repliesCount);
        model.addAttribute("comments", parentComments);

        // Lấy danh sách đánh giá từ movie
        model.addAttribute("ratings", movie.getRatings());
        model.addAttribute("averageRating", averageRating);
        model.addAttribute("moviesSeriesWithRatings", mapMoviesWithRatings(top5HotMoviesSeries));
        model.addAttribute("moviesTheaterWithRatings", mapMoviesWithRatings(top5HotMoviesTheater));
        model.addAttribute("episodes", episodes);
        model.addAttribute("selectedEpisode", selectedEpisode);
        model.addAttribute("videoUrl", videoUrl); // Thay đổi ở đây
        model.addAttribute("userId", userId);
        model.addAttribute("profileImage", profileImage);
        return "Home/xemtap"; // Trả về view
    }

    @Transactional
    @GetMapping("/phimle/xemphim/{slug}")
    public String getXemPhim(@AuthenticationPrincipal UserDetails userDetails,@RequestParam(required = false) Long episodeId, @PathVariable("slug") String slug, Model model, Principal principal) {
        // Tìm phim theo ID
        Movie movie = movieRepository.findBySlug(slug).orElse(null);
        Long userId = null;
        String profileImage = null;
        if (userDetails != null) {
            User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            if (user != null) {
                userId = user.getId(); // Lấy ID người dùng
                profileImage = user.getProfileImage(); // Lấy đường dẫn ảnh đại diện
            }
        }
        // Kiểm tra và khởi tạo viewCount nếu là null
        if (movie.getViewCount() == null) {
            movie.setViewCount(0L);  // Đặt giá trị mặc định là 0
        }

        // Tăng lượt xem sau 10 phút
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // Cập nhật lượt xem
                movie.setViewCount(movie.getViewCount() + 1);
                movie.setWeeklyViewCount(movie.getWeeklyViewCount() + 1);
                movieRepository.save(movie); // Lưu cập nhật vào cơ sở dữ liệu
            }
        },60000 ); // 10 phút (600000 milliseconds)
        model.addAttribute("movie", movie);

        List<Episode> episodes = movie.getEpisodes();
        Episode selectedEpisode = null;
        String videoUrl;
        if (episodes == null || episodes.isEmpty()) {
            // Không có tập nào, sử dụng trailer
            videoUrl = movie.getTrailerUrl(); // Lấy đường dẫn trailer từ cơ sở dữ liệu
        } else {
            // Có tập phim, chọn tập phim phù hợp
            if (episodeId != null) {
                selectedEpisode = episodeRepository.findById(episodeId)
                        .orElseThrow(() -> new ResourceNotFoundException("Episode not found"));
            } else {
                // Nếu không chọn tập cụ thể, lấy tập đầu tiên
                selectedEpisode = episodes.get(0);
            }

            // Nếu selectedEpisode không null, lấy đường dẫn video từ tập phim
            videoUrl = selectedEpisode != null ? selectedEpisode.getVideoUrl() : movie.getTrailerUrl();
        }
        model.addAttribute("videoUrl", videoUrl);
        // Lấy người dùng hiện tại nếu có
        if (principal != null) {
            User user = userRepository.findByUsername(principal.getName()).orElse(null);
            if (user != null) {
                // Tạo lịch sử xem phim
                WatchHistory history = WatchHistory.builder()
                        .user(user)
                        .movie(movie)
                        .watchedAt(new Date())
                        .build();
                watchHistoryRepository.save(history);
            }
        }
        List<Movie> top5HotMoviesSeries = movieService.getTop5HotMoviesSeries();
        List<Movie> top5HotMoviesTheater = movieService.getTop5HotMoviesTheater();


        double averageRating = movie.getRatings()
                .stream()
                .mapToInt(Rating::getRating)
                .average()
                .orElse(0.0); // Tính điểm trung bình ban đầu

        model.addAttribute("movie", movie);

        // Lấy danh sách tất cả bình luận cho phim
        // Lấy danh sách tất cả bình luận cho phim
        List<Comment> allComments = commentRepository.findByMovieIdSortedByCreatedAt(movie.getMovieId());

        // Tạo danh sách bình luận cha
        List<Comment> parentComments = allComments.stream()
                .filter(comment -> comment.getParentComment() == null)
                .collect(Collectors.toList());

        long parentCommentsCount = allComments.stream()
                .filter(comment -> comment.getParentComment() == null) // lọc bình luận chính
                .count();


        long repliesCount = allComments.stream()
                .filter(comment -> comment.getParentComment() != null) // lọc bình luận phản hồi
                .count();

        List<BannerAds> topBanners = bannerAdsService.getActiveBannersByPosition("top");
        List<BannerAds> bottomBanners = bannerAdsService.getActiveBannersByPosition("bottom");
        model.addAttribute("topBanners", topBanners);
        model.addAttribute("bottomBanners", bottomBanners);
        model.addAttribute("parentCommentsCount", parentCommentsCount);
        model.addAttribute("repliesCount", repliesCount);
        model.addAttribute("comments", parentComments);

        // Lấy danh sách đánh giá từ movie
        model.addAttribute("ratings", movie.getRatings());
        model.addAttribute("averageRating", averageRating);
        model.addAttribute("moviesSeriesWithRatings", mapMoviesWithRatings(top5HotMoviesSeries));
        model.addAttribute("moviesTheaterWithRatings", mapMoviesWithRatings(top5HotMoviesTheater));
        model.addAttribute("userId", userId);
        model.addAttribute("profileImage", profileImage);
        return "Home/xemphim"; // Trả về view
    }

    @GetMapping("/phimle/{slug}")
    public String getPhimChieuRapDetails(@AuthenticationPrincipal UserDetails userDetails, @PathVariable("slug") String slug, Model model, Principal principal) {
        Long userId = null;
        String profileImage = null;

        if (userDetails != null) {
            User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            if (user != null) {
                userId = user.getId();
                profileImage = user.getProfileImage();
            }
        }

        Movie movie = movieRepository.findBySlug(slug).orElse(null);
        if (movie == null) {
            return "redirect:/404"; // Hoặc trả về trang lỗi nếu không tìm thấy phim
        }

        model.addAttribute("movie", movie);

        List<Comment> allComments = commentRepository.findByMovieIdSortedByCreatedAt(movie.getMovieId());
        List<Comment> parentComments = allComments.stream()
                .filter(comment -> comment.getParentComment() == null)
                .collect(Collectors.toList());

        long parentCommentsCount = allComments.stream()
                .filter(comment -> comment.getParentComment() == null)
                .count();
        long repliesCount = allComments.stream()
                .filter(comment -> comment.getParentComment() != null)
                .count();

        model.addAttribute("parentCommentsCount", parentCommentsCount);
        model.addAttribute("repliesCount", repliesCount);

        if (principal != null) {
            User user = userRepository.findByUsername(principal.getName()).orElse(null);
            if (user != null) {
                boolean isFavorite = favoritesRepository.existsByUserAndMovie(user, movie);
                model.addAttribute("isFavorite", isFavorite);
            }
        }

        List<Movie> top5HotMoviesSeries = movieService.getTop5HotMoviesSeries();
        List<Movie> top5HotMoviesTheater = movieService.getTop5HotMoviesTheater();

        double averageRating = movie.getRatings()
                .stream()
                .mapToInt(Rating::getRating)
                .average()
                .orElse(0.0);
        List<BannerAds> topBanners = bannerAdsService.getActiveBannersByPosition("top");
        List<BannerAds> bottomBanners = bannerAdsService.getActiveBannersByPosition("bottom");
        model.addAttribute("topBanners", topBanners);
        model.addAttribute("bottomBanners", bottomBanners);
        model.addAttribute("moviesSeriesWithRatings", mapMoviesWithRatings(top5HotMoviesSeries));
        model.addAttribute("moviesTheaterWithRatings", mapMoviesWithRatings(top5HotMoviesTheater));
        List<Movie> moviesInSameGenre = movieRepository.findMoviesByGenres(movie.getGenres(), movie.getSlug());
        model.addAttribute("moviesInSameGenre", moviesInSameGenre);
        model.addAttribute("comments", parentComments);
        model.addAttribute("ratings", movie.getRatings());
        model.addAttribute("averageRating", averageRating);
        model.addAttribute("userId", userId);
        model.addAttribute("profileImage", profileImage);

        return "Home/phimle";
    }

    @GetMapping("/phimbo/{slug}")
    public String getPhimBoDetails(@AuthenticationPrincipal UserDetails userDetails, @PathVariable("slug") String slug, Model model, Principal principal) {
        Long userId = null;
        String profileImage = null;

        if (userDetails != null) {
            User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            if (user != null) {
                userId = user.getId();
                profileImage = user.getProfileImage();
            }
        }

        // Lấy thông tin phim theo slug
        Movie movie = movieRepository.findBySlug(slug).orElse(null);
        if (movie == null) {
            return "redirect:/404"; // Chuyển hướng tới trang lỗi nếu không tìm thấy
        }

        model.addAttribute("movie", movie);

        // Lấy danh sách tất cả bình luận cho phim
        List<Comment> allComments = commentRepository.findByMovieIdSortedByCreatedAt(movie.getMovieId());

        // Tạo danh sách bình luận cha
        List<Comment> parentComments = allComments.stream()
                .filter(comment -> comment.getParentComment() == null)
                .collect(Collectors.toList());

        long parentCommentsCount = allComments.stream()
                .filter(comment -> comment.getParentComment() == null)
                .count();

        long repliesCount = allComments.stream()
                .filter(comment -> comment.getParentComment() != null)
                .count();

        model.addAttribute("parentCommentsCount", parentCommentsCount);
        model.addAttribute("repliesCount", repliesCount);

        // Kiểm tra người dùng đã đăng nhập chưa
        if (principal != null) {
            User user = userRepository.findByUsername(principal.getName()).orElse(null);
            if (user != null) {
                boolean isFavorite = favoritesRepository.existsByUserAndMovie(user, movie);
                model.addAttribute("isFavorite", isFavorite);
            }
        }

        List<Movie> top5HotMoviesSeries = movieService.getTop5HotMoviesSeries();
        List<Movie> top5HotMoviesTheater = movieService.getTop5HotMoviesTheater();

        double averageRating = movie.getRatings()
                .stream()
                .mapToInt(Rating::getRating)
                .average()
                .orElse(0.0);
        List<BannerAds> topBanners = bannerAdsService.getActiveBannersByPosition("top");
        List<BannerAds> bottomBanners = bannerAdsService.getActiveBannersByPosition("bottom");
        model.addAttribute("topBanners", topBanners);
        model.addAttribute("bottomBanners", bottomBanners);
        model.addAttribute("moviesSeriesWithRatings", mapMoviesWithRatings(top5HotMoviesSeries));
        model.addAttribute("moviesTheaterWithRatings", mapMoviesWithRatings(top5HotMoviesTheater));

        List<Movie> moviesInSameGenre = movieRepository.findMoviesByGenres(movie.getGenres(), movie.getSlug());
        model.addAttribute("moviesInSameGenre", moviesInSameGenre);
        model.addAttribute("comments", parentComments);

        model.addAttribute("ratings", movie.getRatings());
        model.addAttribute("averageRating", averageRating);
        model.addAttribute("userId", userId);
        model.addAttribute("profileImage", profileImage);

        return "Home/phimbo";
    }

}
