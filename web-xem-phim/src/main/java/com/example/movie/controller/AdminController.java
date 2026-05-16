package com.example.movie.controller;

import com.example.movie.model.*;
import com.example.movie.repository.*;
import com.example.movie.service.MovieService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.example.movie.service.UserService;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class AdminController {

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IRoleRepository roleRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private MovieService movieService;

    @Autowired
    private UserService userService;

    @GetMapping("/admin")
    public String getManagementPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        // Lấy danh sách bình luận từ cơ sở dữ liệu, sắp xếp theo thời gian tạo giảm dần
        List<Comment> comments = commentRepository.findAll(Sort.by(Sort.Order.desc("createdAt")));

        // Lấy danh sách các đánh giá từ cơ sở dữ liệu
        List<Rating> ratings = ratingRepository.findAll(Sort.by(Sort.Order.desc("createdAt")));

        // Lấy danh sách phim từ cơ sở dữ liệu
        List<Movie> movies = movieRepository.findAll();


        String profileImage = null;
        if (userDetails != null) {
            User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            if (user != null) {
                profileImage = user.getProfileImage(); // Lấy đường dẫn ảnh đại diện
            }
        }

        long totalMovies = movies.size();

        // Đếm số lượng phim bộ
        long seriesCount = movies.stream().filter(Movie::getIsSeries).count();

        // Đếm số lượng phim chiếu rạp
        long cinemaMoviesCount = movies.stream().filter(movie -> !movie.getIsSeries()).count();

        List<Movie> top5HotMoviesSeries = movieService.getTop5HotMoviesSeries();
        List<Movie> top5HotMoviesTheater = movieService.getTop5HotMoviesTheater();

        long totalUsers = userService.getTotalUsers();
        long newUsersThisWeek = userService.getNewUsersThisWeek();
        List<Long> newUsersPerDay = userService.getNewUsersPerDayInWeek();

        List<Movie> top6HighestRatedMovies = movieService.getTop6HighestRatedMovies();

        // Tính điểm trung bình cho mỗi phim
        List<Map<String, Object>> moviesWithRatings = top6HighestRatedMovies.stream()
                .map(movie -> {
                    Double averageRating = ratingRepository.findAverageRatingByMovie(movie);
                    Map<String, Object> movieData = new HashMap<>();
                    movieData.put("movie", movie);
                    movieData.put("averageRating", averageRating != null ? averageRating : 0.0);
                    return movieData;
                })
                .collect(Collectors.toList());

        // Thêm danh sách phim và điểm trung bình vào model
        model.addAttribute("moviesWithRatings", moviesWithRatings);
        model.addAttribute("newUsersPerDay", newUsersPerDay);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("newUsersThisWeek", newUsersThisWeek);
        model.addAttribute("comments", comments);
        model.addAttribute("ratings", ratings);
        model.addAttribute("movies", movies);
        model.addAttribute("totalMovies", totalMovies);
        model.addAttribute("seriesCount", seriesCount);
        model.addAttribute("cinemaMoviesCount", cinemaMoviesCount);
        model.addAttribute("top5HotMoviesSeries", top5HotMoviesSeries);
        model.addAttribute("top5HotMoviesTheater", top5HotMoviesTheater);
        model.addAttribute("profileImage", profileImage);
        // Tính tổng lượt xem cho từng thể loại
        List<Genre> genres = genreRepository.findAll();

// Cập nhật tổng lượt xem cho từng thể loại
        for (Genre genre : genres) {
            long totalViewCount = genre.getMovies().stream()
                    .mapToLong(Movie::getViewCount)
                    .sum();
            genre.setMovieCount(genre.getMovies().size()); // Cập nhật số lượng phim cho thể loại
            genre.setTotalViewCount(totalViewCount); // Cập nhật tổng lượt xem cho thể loại
        }

// Sắp xếp các thể loại theo tổng lượt xem giảm dần
        List<Genre> sortedGenres = genres.stream()
                .sorted((g1, g2) -> Long.compare(g2.getTotalViewCount(), g1.getTotalViewCount())) // Sắp xếp giảm dần
                .collect(Collectors.toList());

// Lấy 5 thể loại có lượt xem cao nhất
        List<Genre> topGenres = sortedGenres.stream()
                .limit(5) // Giới hạn số lượng thể loại hiển thị
                .collect(Collectors.toList());

// Thêm tất cả thể loại và các thể loại top vào model
        model.addAttribute("allGenres", genres);
        model.addAttribute("topGenres", topGenres);

        return "Admin/management";
    }



    // Hiển thị danh sách người dùng
    @GetMapping("/users")
    public String getAllUsers(@AuthenticationPrincipal UserDetails userDetails,
                              @RequestParam(value = "page", defaultValue = "0") int page,
                              @RequestParam(value = "size", defaultValue = "10") int size,
                              @RequestParam(value = "query", defaultValue = "") String query,
                              Model model) {
        Pageable pageable = PageRequest.of(page, size);
        String profileImage = null;
        if (userDetails != null) {
            User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            if (user != null) {
                profileImage = user.getProfileImage(); // Lấy đường dẫn ảnh đại diện
            }
        }
        model.addAttribute("profileImage", profileImage);
        Page<User> userPage;
        if (query.isEmpty()) {
            userPage = userRepository.findAll(pageable); // Nếu không có tìm kiếm
        } else {
            userPage = userRepository.searchByUsernameOrEmailOrFullName(query, pageable); // Nếu có tìm kiếm
        }

        List<Role> roles = roleRepository.findAll();

        model.addAttribute("userPage", userPage);
        model.addAttribute("roles", roles);
        model.addAttribute("query", query);

        return "Admin/users";  // Trả về view để hiển thị danh sách người dùng
    }



    @PostMapping("/user/updateRole")
    public String updateUserRole(@RequestParam("userId") Long userId, @RequestParam("roleId") Long roleId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            Optional<Role> roleOptional = roleRepository.findById(roleId);
            if (roleOptional.isPresent()) {
                Role role = roleOptional.get();
                user.getRoles().clear();  // Clear all roles before adding new one
                user.getRoles().add(role);  // Add the new role
                userRepository.save(user);
            }
        }
        return "redirect:/users";  // Redirect to the users list page after update
    }
    @PostMapping("/users/delete")
    public String deleteUser(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // Xóa tất cả các vai trò liên kết với người dùng
            user.getRoles().clear();
            userRepository.save(user); // Cập nhật để xóa liên kết trong bảng trung gian

            // Sau khi liên kết bị xóa, tiến hành xóa người dùng
            userRepository.deleteById(userId);
        }
        return "redirect:/users"; // Chuyển hướng về trang danh sách người dùng
    }
}
