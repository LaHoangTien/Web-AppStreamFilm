package com.example.movie.controller;

import com.example.movie.model.Movie;
import com.example.movie.model.User;
import com.example.movie.model.WatchHistory;
import com.example.movie.repository.MovieRepository;
import com.example.movie.repository.UserRepository;
import com.example.movie.repository.WatchHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.stereotype.Controller;  // Import Controller

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Controller  // Đánh dấu đây là một controller
public class HistoryController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WatchHistoryRepository watchHistoryRepository;

    @Autowired
    private MovieRepository movieRepository; // Repository để lấy thông tin phim

    @GetMapping("/history")
    public String getWatchHistory(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Long userId = null;

        // Lấy thông tin người dùng từ UserDetails
        if (userDetails != null) {
            User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            if (user != null) {
                userId = user.getId(); // Lấy ID người dùng
            }
        }

        // Kiểm tra xem userId có hợp lệ không
        if (userId == null) {
            return "error/403"; // Trả về trang lỗi nếu không tìm thấy người dùng
        }

        // Lấy danh sách lịch sử xem phim theo userId
        List<WatchHistory> historyList = watchHistoryRepository.findByUserId(userId);

        // Loại bỏ các phim đã xem, chỉ lấy phim duy nhất với thời gian xem mới nhất
        Map<Long, WatchHistory> uniqueHistoryMap = new HashMap<>();
        for (WatchHistory history : historyList) {
            Long movieId = history.getMovie().getMovieId();
            if (!uniqueHistoryMap.containsKey(movieId)) {
                uniqueHistoryMap.put(movieId, history);
            } else {
                // Cập nhật thời gian xem cho phim đã tồn tại
                WatchHistory existingHistory = uniqueHistoryMap.get(movieId);
                existingHistory.setWatchedAt(history.getWatchedAt()); // Cập nhật thời gian
            }
        }

        model.addAttribute("historyList", new ArrayList<>(uniqueHistoryMap.values()));
        return "Home/history"; // Đảm bảo rằng đường dẫn này đúng
    }

}
