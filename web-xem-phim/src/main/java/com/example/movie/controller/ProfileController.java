package com.example.movie.controller;

import com.example.movie.model.*;
import com.example.movie.repository.FavoritesRepository;
import com.example.movie.repository.IUserRepository;
import com.example.movie.repository.MovieRepository;
import com.example.movie.repository.WatchHistoryRepository;

import com.example.movie.service.BannerAdsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.*;

@Controller
public class ProfileController {

    private final IUserRepository userRepository;
    private final String PROFILE_IMAGE_DIRECTORY = "src/main/resources/static/profile_images/";
    @Autowired
    private WatchHistoryRepository watchHistoryRepository;
    @Autowired
    private FavoritesRepository favoritesRepository;
    @Autowired
    private MovieRepository movieRepository; // Repository để lấy thông tin phim
    @Autowired
    public ProfileController(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private BannerAdsService bannerAdsService;
    @GetMapping("/profile")
    public String viewProfile(@AuthenticationPrincipal UserDetails userDetails, Principal principal, Model model) {

        String profileImage = null;
        if (userDetails != null) {
            User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            if (user != null) {
                profileImage = user.getProfileImage(); // Lấy đường dẫn ảnh đại diện
            }
        }

        // Tìm kiếm người dùng trong cơ sở dữ liệu
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Chuyển đổi User thành UpdateUserDTO
        UpdateUserDTO updateUserDTO = new UpdateUserDTO();
        updateUserDTO.setFullName(user.getFullName());
        updateUserDTO.setProfileImage(null); // Không load ảnh gốc vào form vì ảnh là file

        // Lấy danh sách lịch sử xem phim theo userId
        List<WatchHistory> historyList = watchHistoryRepository.findByUserId(user.getId());

        // Loại bỏ các phim đã xem, chỉ lấy phim duy nhất với thời gian xem mới nhất
        Map<Long, WatchHistory> uniqueHistoryMap = new HashMap<>();
        for (WatchHistory history : historyList) {
            Long movieId = history.getMovie().getMovieId();
            if (!uniqueHistoryMap.containsKey(movieId)) {
                uniqueHistoryMap.put(movieId, history);
            } else {
                // Chỉ cập nhật thời gian xem nếu thời gian mới hơn
                WatchHistory existingHistory = uniqueHistoryMap.get(movieId);
                if (existingHistory.getWatchedAt().before(history.getWatchedAt())) {
                    uniqueHistoryMap.put(movieId, history); // Cập nhật nếu thời gian mới hơn
                }
            }
        }

        // Sắp xếp danh sách theo thời gian xem mới nhất
        List<WatchHistory> sortedHistoryList = new ArrayList<>(uniqueHistoryMap.values());
        sortedHistoryList.sort((h1, h2) -> h2.getWatchedAt().compareTo(h1.getWatchedAt()));
        // Lấy danh sách phim yêu thích theo userId, ưu tiên hiển thị phim mới
        List<Favorites> favoritesList = favoritesRepository.findByUserIdOrderByAddedAtDesc(user.getId());
        List<BannerAds> topBanners = bannerAdsService.getActiveBannersByPosition("top");
        List<BannerAds> bottomBanners = bannerAdsService.getActiveBannersByPosition("bottom");
        model.addAttribute("topBanners", topBanners);
        model.addAttribute("bottomBanners", bottomBanners);
        model.addAttribute("favoritesList", favoritesList);
        model.addAttribute("profileImage", profileImage);
        // Thêm dữ liệu vào model để hiển thị trên trang profile
        model.addAttribute("updateUserDTO", updateUserDTO);
        model.addAttribute("user", user); // Thông tin người dùng không chỉnh sửa như username và email
        model.addAttribute("historyList", sortedHistoryList); // Lịch sử xem phim duy nhất và đã sắp xếp

        return "users/profile"; // Đảm bảo rằng đường dẫn tới file HTML là đúng
    }



    @PostMapping("/profile/update")
    public String updateProfile(
            Principal principal,
            @Validated @ModelAttribute UpdateUserDTO updateUserDTO,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Kiểm tra lỗi nếu có trong form
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("updateUserDTO", updateUserDTO);
            return "redirect:/profile"; // Trả về trang profile với thông báo lỗi
        }

        // Tìm người dùng qua tên đăng nhập
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Kiểm tra nếu người dùng muốn thay đổi mật khẩu
        if (updateUserDTO.getCurrentPassword() != null && !updateUserDTO.getCurrentPassword().isEmpty()) {
            // Xác thực mật khẩu hiện tại
            if (!passwordEncoder.matches(updateUserDTO.getCurrentPassword(), user.getPassword())) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu hiện tại không đúng."); // Thêm thông báo lỗi vào FlashAttributes
                return "redirect:/profile"; // Trả về trang profile với thông báo lỗi
            }

            // Kiểm tra mật khẩu mới và mật khẩu xác nhận
            if (!updateUserDTO.getNewPassword().equals(updateUserDTO.getConfirmPassword())) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu mới và mật khẩu xác nhận không khớp."); // Thêm thông báo lỗi vào FlashAttributes
                return "redirect:/profile"; // Trả về trang profile với thông báo lỗi
            }

            // Cập nhật mật khẩu mới
            user.setPassword(passwordEncoder.encode(updateUserDTO.getNewPassword()));
        }

        // Cập nhật các thông tin khác từ DTO
        user.setFullName(updateUserDTO.getFullName());

        // Xử lý upload hình ảnh nếu có
        MultipartFile profileImage = updateUserDTO.getProfileImage();
        if (profileImage != null && !profileImage.isEmpty()) {
            try {
                File directory = new File(PROFILE_IMAGE_DIRECTORY);
                if (!directory.exists()) {
                    directory.mkdirs();
                }
                String fileName = System.currentTimeMillis() + "_" + profileImage.getOriginalFilename();
                Path path = Paths.get(PROFILE_IMAGE_DIRECTORY, fileName);
                Files.write(path, profileImage.getBytes());
                user.setProfileImage(fileName);
            } catch (IOException e) {
                e.printStackTrace();
                redirectAttributes.addFlashAttribute("error", "Lỗi tải lên hình ảnh."); // Thêm thông báo lỗi vào FlashAttributes
                return "redirect:/profile"; // Trả về trang profile với thông báo lỗi
            }
        }

        // Lưu thông tin người dùng đã cập nhật
        try {
            userRepository.save(user);
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi cơ sở dữ liệu."); // Thêm thông báo lỗi vào FlashAttributes
            return "redirect:/profile"; // Trả về trang profile với thông báo lỗi
        }

        // Thêm thông báo thành công vào FlashAttributes
        redirectAttributes.addFlashAttribute("message", "Thông tin cá nhân đã được cập nhật thành công.");

        return "redirect:/profile"; // Trả về trang profile sau khi cập nhật thành công
    }


}