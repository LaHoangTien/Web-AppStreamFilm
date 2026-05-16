package com.example.movie.controller;

import com.example.movie.model.Favorites;
import com.example.movie.model.Movie;
import com.example.movie.model.User;
import com.example.movie.repository.FavoritesRepository;
import com.example.movie.repository.MovieRepository;
import com.example.movie.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/favorites")
public class FavoritesController {

    @Autowired
    private FavoritesRepository favoritesRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MovieRepository movieRepository;

    // Hiển thị danh sách phim yêu thích
    @GetMapping
    public String getFavorites(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Long userId = null;

        // Lấy thông tin người dùng từ UserDetails
        if (userDetails != null) {
            User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            if (user != null) {
                userId = user.getId(); // Lấy ID người dùng
            }
        }

        // Lấy danh sách phim yêu thích theo userId
        List<Favorites> favoritesList = favoritesRepository.findByUserId(userId);
        model.addAttribute("favoritesList", favoritesList);
        return "Home/favorites"; // Đảm bảo rằng đường dẫn này đúng
    }

    // Thêm phim vào danh sách yêu thích
    @PostMapping("/add")
    public String addFavorite(@RequestParam Long movieId, Principal principal, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        // Lưu đường dẫn cũ để trả về
        String referer = request.getHeader("Referer");

        if (principal == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần đăng nhập để bình luận.");
            return "redirect:/login"; // Chuyển hướng tới trang đăng nhập
        }

        String username = principal.getName();
        User user = userRepository.findByUsername(username).orElse(null);;
        if (user != null) {
            Movie movie = movieRepository.findById(movieId).orElse(null);
            if (movie != null) {
                // Kiểm tra xem phim đã có trong danh sách yêu thích chưa
                Favorites existingFavorite = favoritesRepository.findByUserAndMovie(user, movie).orElse(null);
                if (existingFavorite != null) {
                    // Thông báo nếu phim đã có trong danh sách yêu thích
                    redirectAttributes.addFlashAttribute("errorMessage", "Phim đã có trong danh sách yêu thích!");
                } else {
                    // Tạo và lưu vào danh sách yêu thích
                    Favorites favorites = Favorites.builder()
                            .user(user)
                            .movie(movie)
                            .build();
                    favoritesRepository.save(favorites);
                    redirectAttributes.addFlashAttribute("successMessage", "Thêm phim vào danh sách yêu thích thành công!");
                }
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Phim không tồn tại!");
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Người dùng không tồn tại!");
        }

        return "redirect:" + referer; // Quay lại đường dẫn cũ
    }

    // Xóa phim khỏi danh sách yêu thích
    @PostMapping("/remove")
    public String removeFavorite(@RequestParam Long favoriteId) {
        favoritesRepository.deleteById(favoriteId); // Xóa theo ID của danh sách yêu thích
        return "redirect:/profile"; // Quay lại trang danh sách yêu thích
    }
}
