package com.example.movie.controller;

import com.example.movie.model.Episode;
import com.example.movie.model.Movie;
import com.example.movie.model.User;
import com.example.movie.repository.EpisodeRepository;
import com.example.movie.repository.IUserRepository;
import com.example.movie.repository.MovieRepository;
import com.example.movie.service.EpisodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

@Controller
public class EpisodeController {


    @Autowired
    private EpisodeRepository episodeRepository;

    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private IUserRepository userRepository;
    // Đường dẫn lưu video
    private final String SERIES_DIRECTORY = "src/main/resources/static/series/";
    // Hiển thị trang thêm tập phim
    @GetMapping("/episodes/add")
    public String showAddEpisodeForm(@AuthenticationPrincipal UserDetails userDetails, @RequestParam("movieId") Long movieId, Model model) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid movie ID: " + movieId));
        String profileImage = null;
        if (userDetails != null) {
            User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            if (user != null) {
                profileImage = user.getProfileImage(); // Lấy đường dẫn ảnh đại diện
            }
        }
        model.addAttribute("profileImage", profileImage);
        model.addAttribute("movie", movie);
        model.addAttribute("episode", new Episode());
        return "episodes/add-episode"; // Tên file HTML sẽ hiển thị form thêm tập phim
    }

    @PostMapping("/episodes/add")
    public String addEpisode(@RequestParam("movieId") Long movieId,
                             @RequestParam(value = "videoUrl", required = false) String videoUrl,
                             Episode episode) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid movie ID: " + movieId));


        episode.setMovie(movie); // Gán phim cho tập phim
        episodeRepository.save(episode); // Lưu tập phim vào cơ sở dữ liệu
        // Cập nhật thời gian cho movie
        movie.setUpdatedAt(new Date()); // Cập nhật thời gian hiện tại
        movieRepository.save(movie); // Lưu movie để cập nhật thời gian

        return "redirect:/movies"; // Chuyển hướng về trang thông tin phim sau khi thêm tập
    }
    // Hiển thị trang chỉnh sửa tập phim
    @GetMapping("/episodes/edit/{id}")
    public String showEditEpisodeForm(@AuthenticationPrincipal UserDetails userDetails, @PathVariable("id") Long episodeId, Model model) {
        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid episode ID: " + episodeId));
        String profileImage = null;
        if (userDetails != null) {
            User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            if (user != null) {
                profileImage = user.getProfileImage(); // Lấy đường dẫn ảnh đại diện
            }
        }
        model.addAttribute("profileImage", profileImage);
        model.addAttribute("episode", episode);
        model.addAttribute("movie", episode.getMovie());
        return "episodes/edit-episode"; // Tên file HTML sẽ hiển thị form chỉnh sửa tập phim
    }

    @PostMapping("/episodes/edit/{id}")
    public String editEpisode(@PathVariable("id") Long episodeId,
                              @RequestParam(value = "videoUrl", required = false) String videoUrl,
                              Episode updatedEpisode) {
        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid episode ID: " + episodeId));


        // Cập nhật thông tin tập phim
        episode.setEpisodeNumber(updatedEpisode.getEpisodeNumber());
        episode.setTitle(updatedEpisode.getTitle());


        // Lưu video mới nếu có
        if (videoUrl != null && !videoUrl.isEmpty()) {
            episode.setVideoUrl(videoUrl); // Cập nhật video mới
        } else {
            episode.setVideoUrl(episode.getVideoUrl()); // Giữ nguyên video cũ
        }

        episodeRepository.save(episode); // Lưu tập phim đã chỉnh sửa vào cơ sở dữ liệu
// Cập nhật thời gian cho movie
        Movie movie = episode.getMovie();
        movie.setUpdatedAt(new Date()); // Cập nhật thời gian hiện tại
        movieRepository.save(movie); // Lưu movie để cập nhật thời gian
        return "redirect:/movies"; // Chuyển hướng về trang thông tin phim sau khi chỉnh sửa tập
    }


    // Hiển thị trang chi tiết tập phim
    @GetMapping("/episodes/detail/{id}")
    public String showEpisodeDetail(@AuthenticationPrincipal UserDetails userDetails, @PathVariable("id") Long episodeId, Model model) {
        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid episode ID: " + episodeId));
        String profileImage = null;
        if (userDetails != null) {
            User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            if (user != null) {
                profileImage = user.getProfileImage(); // Lấy đường dẫn ảnh đại diện
            }
        }
        model.addAttribute("profileImage", profileImage);
        model.addAttribute("episode", episode);
        return "episodes/episode-detail"; // Tên file HTML của trang chi tiết tập phim
    }
    // Xóa tập phim
    @GetMapping("/episodes/delete/{id}")
    public String deleteEpisode(@PathVariable("id") Long episodeId, RedirectAttributes redirectAttributes) {
        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid episode ID: " + episodeId));

        Movie movie = episode.getMovie();

        // Xóa tập phim khỏi cơ sở dữ liệu
        episodeRepository.delete(episode);

        // Cập nhật thời gian của movie sau khi xóa tập
        movieRepository.save(movie);

        // Thông báo thành công
        redirectAttributes.addFlashAttribute("successMessage", "Episode deleted successfully.");

        return "redirect:/movies"; // Chuyển hướng về danh sách phim
    }

    @Autowired
    private EpisodeService episodeService;
    @PostMapping("/add-episodes")
    public String addEpisodesFromApi(
            @RequestParam(value = "apiUrl", required = true) String apiUrl,
            @RequestParam(value = "movieId", required = true) Long movieId) {
        System.out.println("API URL: " + apiUrl);
        System.out.println("Movie ID: " + movieId);
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid movie ID: " + movieId));
        movie.setUpdatedAt(new Date());
        try {
            episodeService.addEpisodesFromApi(apiUrl, movieId);
            return "redirect:/movies?success"; // Thông báo thành công
        } catch (Exception e) {
            return "redirect:/movies?error"; // Thông báo lỗi
        }
    }
}
