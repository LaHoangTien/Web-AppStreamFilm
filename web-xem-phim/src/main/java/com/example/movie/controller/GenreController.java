package com.example.movie.controller;

import com.example.movie.model.Genre;
import com.example.movie.model.User;
import com.example.movie.repository.GenreRepository;
import com.example.movie.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/genres")
public class GenreController {

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private IUserRepository userRepository;
    @GetMapping
    public String listGenres(@RequestParam(value = "page", defaultValue = "0") int page,
                             @RequestParam(value = "size", defaultValue = "10") int size,
                             @RequestParam(value = "search", defaultValue = "") String search,
                             @AuthenticationPrincipal UserDetails userDetails,
                             Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Genre> genrePage;
        String profileImage = null;
        if (userDetails != null) {
            User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            if (user != null) {
                profileImage = user.getProfileImage(); // Lấy đường dẫn ảnh đại diện
            }
        }
        if (search.isEmpty()) {
            genrePage = genreRepository.findAll(pageable); // Nếu không có từ khóa tìm kiếm, lấy tất cả
        } else {
            genrePage = genreRepository.findByGenreNameContainingIgnoreCase(search, pageable); // Tìm theo tên thể loại
        }
        model.addAttribute("profileImage", profileImage);
        model.addAttribute("genrePage", genrePage);
        model.addAttribute("search", search); // Truyền từ khóa tìm kiếm về view
        return "genres/list"; // Trả về trang danh sách thể loại
    }



    @GetMapping("/add")
    public String showAddGenreForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String profileImage = null;
        if (userDetails != null) {
            User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            if (user != null) {
                profileImage = user.getProfileImage(); // Lấy đường dẫn ảnh đại diện
            }
        }
        model.addAttribute("profileImage", profileImage);
        model.addAttribute("genre", new Genre());
        return "genres/add"; // Trả về trang thêm thể loại
    }

    @PostMapping("/add")
    public String addGenre(@ModelAttribute Genre genre, Model model) {
        // Kiểm tra xem thể loại đã tồn tại trong cơ sở dữ liệu chưa
        if (genreRepository.existsByGenreName(genre.getGenreName())) {
            // Nếu đã tồn tại, hiển thị thông báo lỗi và trả lại trang thêm thể loại
            model.addAttribute("errorMessage", "Thể loại đã tồn tại!");
            return "genres/add";
        }
        // Nếu chưa tồn tại, thực hiện lưu thể loại mới
        genreRepository.save(genre);
        return "redirect:/genres"; // Chuyển hướng về danh sách thể loại
    }


    @GetMapping("/edit/{id}")
    public String showEditGenreForm(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id, Model model) {
        Genre genre = genreRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid genre Id:" + id));
        String profileImage = null;
        if (userDetails != null) {
            User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            if (user != null) {
                profileImage = user.getProfileImage(); // Lấy đường dẫn ảnh đại diện
            }
        }
        model.addAttribute("profileImage", profileImage);
        model.addAttribute("genre", genre);
        return "genres/edit"; // Trả về trang chỉnh sửa thể loại
    }

    @PostMapping("/edit/{id}")
    public String updateGenre(@PathVariable Long id, @ModelAttribute Genre genre) {
        genre.setGenreId(id);
        genreRepository.save(genre);
        return "redirect:/genres"; // Chuyển hướng về danh sách thể loại
    }

    @GetMapping("/delete/{id}")
    public String deleteGenre(@PathVariable Long id) {
        genreRepository.deleteById(id);
        return "redirect:/genres"; // Chuyển hướng về danh sách thể loại
    }

    @PostMapping("/createQuickly")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createGenreQuickly(@RequestBody Genre genre) {
        Genre savedGenre = genreRepository.save(genre);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("genreId", savedGenre.getGenreId());
        response.put("genreName", savedGenre.getGenreName());
        return ResponseEntity.ok(response);
    }
}
