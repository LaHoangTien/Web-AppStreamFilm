package com.example.movie.controller;

import com.example.movie.StringUtils;
import com.example.movie.elasticsearch.MovieDocument;
import com.example.movie.model.*;
import com.example.movie.repository.*;
import com.example.movie.service.EpisodeService;
import com.example.movie.service.MovieSearchService;
import com.example.movie.service.MovieSynchronizationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/movies")
public class MovieController {
    private final MovieRepository movieRepository; // Sử dụng MovieRepository trực tiếp
    private final ActorRepository actorRepository;  // Repository cho diễn viên
    private final GenreRepository genreRepository;  // Repository cho thể loại
    private final CountryRepository countryRepository;
    private final FavoritesRepository favoritesRepository;
    private final RatingRepository ratingRepository;
    private final WatchHistoryRepository watchHistoryRepository;
    private final CommentRepository commentRepository;
    private final EpisodeRepository episodeRepository;
    private final IUserRepository userRepository;
    @Autowired
    private MovieSearchService movieSearchService;
    @Autowired
    private MovieSynchronizationService movieSynchronizationService;
//    @GetMapping
//    public String listMovies() {
//        return "list"; // Tên file HTML, không cần đuôi .html
//    }
    @GetMapping
    public String listMovies(@RequestParam(required = false) String sort,
                             @RequestParam(required = false) String cat,
                             @RequestParam(required = false) List<Long> genreIds,
                             @RequestParam(required = false) List<Long> actorIds,
                             @RequestParam(required = false) List<Long> countryIds,
                             @RequestParam(required = false) Integer releaseYear,
                             @RequestParam(required = false) String keyword,
                             @RequestParam(required = false) String status,
                             @RequestParam(defaultValue = "0") int page,
                             @AuthenticationPrincipal UserDetails userDetails,
                             Model model) {
        String profileImage = null;
        if (userDetails != null) {
            User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            if (user != null) {
                profileImage = user.getProfileImage(); // Lấy đường dẫn ảnh đại diện
            }
        }
        List<Movie> allMovies = movieRepository.findAll(); // Lấy danh sách tất cả phim
        List<Movie> movies = new ArrayList<>(allMovies); // Khởi tạo danh sách phim dựa trên tất cả phim
        List<Genre> genres = genreRepository.findAll();
        List<Actor> actors = actorRepository.findAll();
        List<Country> countries = countryRepository.findAll();

        for (Genre genre : genres) {
            long count = movies.stream()
                    .filter(movie -> movie.getGenres().stream().anyMatch(g -> g.getGenreId().equals(genre.getGenreId())))
                    .count();
            genre.setMovieCount(count);
        }
        for (Actor actor : actors) {
            long count = movies.stream()
                    .filter(movie -> movie.getActors().stream().anyMatch(a -> a.getActorId().equals(actor.getActorId())))
                    .count();
            actor.setMovieCount(count);
        }
        for (Country country : countries) {
            long count = movies.stream()
                    .filter(movie -> movie.getCountry() != null && movie.getCountry().getCountryId().equals(country.getCountryId()))
                    .count();
            country.setMovieCount(count);
        }
        List<Integer> releaseYears = movies.stream()
                .map(Movie::getReleaseYear)
                .filter(year -> year != null)
                .distinct()
                .sorted((year1, year2) -> Integer.compare(year2, year1)) // Sắp xếp từ lớn đến bé
                .collect(Collectors.toList());



        if (releaseYear != null) {
            movies = movies.stream()
                    .filter(movie -> releaseYear.equals(movie.getReleaseYear()))
                    .collect(Collectors.toList());
        }
        // Áp dụng các bộ lọc
        if (genreIds != null && !genreIds.isEmpty()) {
            movies = movies.stream()
                    .filter(movie -> movie.getGenres().stream()
                            .anyMatch(genre -> genreIds.contains(genre.getGenreId())))
                    .collect(Collectors.toList());
        }
        if ("phimle".equals(cat)) {
            movies = movies.stream().filter(movie -> !movie.isSeries()).collect(Collectors.toList());
        } else if ("phimbo".equals(cat)) {
            movies = movies.stream().filter(Movie::isSeries).collect(Collectors.toList());
        }
        if (actorIds != null && !actorIds.isEmpty()) {
            movies = movies.stream()
                    .filter(movie -> movie.getActors().stream()
                            .anyMatch(actor -> actorIds.contains(actor.getActorId())))
                    .collect(Collectors.toList());
        }
        if (countryIds != null && !countryIds.isEmpty()) {
            movies = movies.stream()
                    .filter(movie -> movie.getCountry() != null && countryIds.contains(movie.getCountry().getCountryId()))
                    .collect(Collectors.toList());
        }
        if ("asc".equals(sort)) {
            movies.sort(Comparator.comparing(Movie::getUpdatedAt));
        } else {
            movies.sort(Comparator.comparing(Movie::getUpdatedAt).reversed());
        }

        if (status != null && !status.isEmpty()) {
            movies = movies.stream()
                    .filter(movie -> status.equals(movie.getStatus()))
                    .collect(Collectors.toList());
        }



        // Phân trang
        int pageSize = 8;
        Pageable pageable = PageRequest.of(page, pageSize);
        int start = Math.min((int) pageable.getOffset(), movies.size());
        int end = Math.min((start + pageable.getPageSize()), movies.size());
        List<Movie> filteredMovies = movies.subList(start, end);
        Page<Movie> moviePage = new PageImpl<>(filteredMovies, pageable, movies.size());




        model.addAttribute("profileImage", profileImage);
        model.addAttribute("moviePage", moviePage);
        model.addAttribute("sort", sort);
        model.addAttribute("cat", cat);
        model.addAttribute("genreIds", genreIds);
        model.addAttribute("actorIds", actorIds);
        model.addAttribute("countryIds", countryIds);
        model.addAttribute("releaseYear", releaseYear);
        model.addAttribute("keyword", keyword);
        model.addAttribute("releaseYears", releaseYears);
        model.addAttribute("genres", genres);
        model.addAttribute("actors", actors);
        model.addAttribute("countries", countries);
        model.addAttribute("status", status);
        return "movies/movie-list"; // Trả về trang locphim.html
    }
    @GetMapping("/adminsearch")
    public String SearchMovies(@AuthenticationPrincipal UserDetails userDetails,
                               @RequestParam(required = false) String sort,
                               @RequestParam(required = false) String cat,
                               @RequestParam(required = false) List<Long> genreIds,
                               @RequestParam(required = false) List<Long> actorIds,
                               @RequestParam(required = false) List<Long> countryIds,
                               @RequestParam(required = false) Integer releaseYear,
                               @RequestParam(required = false) String keyword,
                               @RequestParam(defaultValue = "0") int page,
                               Model model) {

        String profileImage = null;
        if (userDetails != null) {
            User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            if (user != null) {
                profileImage = user.getProfileImage(); // Lấy đường dẫn ảnh đại diện
            }
        }
        model.addAttribute("profileImage", profileImage);

        List<MovieDocument> searchResults = movieSearchService.searchMoviesFuzzy(keyword);

        // Lấy danh sách ID và duy trì thứ tự
        List<Long> orderedMovieIds = searchResults.stream()
                .map(MovieDocument::getMovieId)
                .collect(Collectors.toList());

        // Truy vấn phim từ cơ sở dữ liệu theo ID và duy trì thứ tự
        List<Movie> movies = movieRepository.findAllById(orderedMovieIds).stream()
                .sorted(Comparator.comparingInt(movie -> orderedMovieIds.indexOf(movie.getMovieId())))
                .collect(Collectors.toList());


//        for (Genre genre : genres) {
//            long count = movies.stream()
//                    .filter(movie -> movie.getGenres().stream().anyMatch(g -> g.getGenreId().equals(genre.getGenreId())))
//                    .count();
//            genre.setMovieCount(count);
//        }
//        for (Actor actor : actors) {
//            long count = movies.stream()
//                    .filter(movie -> movie.getActors().stream().anyMatch(a -> a.getActorId().equals(actor.getActorId())))
//                    .count();
//            actor.setMovieCount(count);
//        }
//        for (Country country : countries) {
//            long count = movies.stream()
//                    .filter(movie -> movie.getCountry() != null && movie.getCountry().getCountryId().equals(country.getCountryId()))
//                    .count();
//            country.setMovieCount(count);
//        }
//        List<Integer> releaseYears = movies.stream()
//                .map(Movie::getReleaseYear)
//                .filter(year -> year != null)
//                .distinct()
//                .sorted((year1, year2) -> Integer.compare(year2, year1)) // Sắp xếp từ lớn đến bé
//                .collect(Collectors.toList());
//
//
//
//        if (releaseYear != null) {
//            movies = movies.stream()
//                    .filter(movie -> releaseYear.equals(movie.getReleaseYear()))
//                    .collect(Collectors.toList());
//        }
//        // Áp dụng các bộ lọc
//        if (genreIds != null && !genreIds.isEmpty()) {
//            movies = movies.stream()
//                    .filter(movie -> movie.getGenres().stream()
//                            .anyMatch(genre -> genreIds.contains(genre.getGenreId())))
//                    .collect(Collectors.toList());
//        }
//        if ("phimle".equals(cat)) {
//            movies = movies.stream().filter(movie -> !movie.isSeries()).collect(Collectors.toList());
//        } else if ("phimbo".equals(cat)) {
//            movies = movies.stream().filter(Movie::isSeries).collect(Collectors.toList());
//        }
//        if (actorIds != null && !actorIds.isEmpty()) {
//            movies = movies.stream()
//                    .filter(movie -> movie.getActors().stream()
//                            .anyMatch(actor -> actorIds.contains(actor.getActorId())))
//                    .collect(Collectors.toList());
//        }
//        if (countryIds != null && !countryIds.isEmpty()) {
//            movies = movies.stream()
//                    .filter(movie -> movie.getCountry() != null && countryIds.contains(movie.getCountry().getCountryId()))
//                    .collect(Collectors.toList());
//        }
//


        // Phân trang
        int pageSize = 16;
        Pageable pageable = PageRequest.of(page, pageSize);
        int start = Math.min((int) pageable.getOffset(), movies.size());
        int end = Math.min((start + pageable.getPageSize()), movies.size());
        List<Movie> filteredMovies = movies.subList(start, end);
        Page<Movie> moviePage = new PageImpl<>(filteredMovies, pageable, movies.size());





        model.addAttribute("profileImage", profileImage);
        model.addAttribute("moviePage", moviePage);
        model.addAttribute("sort", sort);
        model.addAttribute("cat", cat);
        model.addAttribute("genreIds", genreIds);
        model.addAttribute("actorIds", actorIds);
        model.addAttribute("countryIds", countryIds);
        model.addAttribute("releaseYear", releaseYear);
        model.addAttribute("keyword", keyword);

        return "movies/movie-search"; // Trả về trang locphim.html
    }
    @GetMapping("/detail/{id}")
    public String showMovieDetail(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id, Model model) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ID phim không hợp lệ: " + id));
        String profileImage = null;
        if (userDetails != null) {
            User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            if (user != null) {
                profileImage = user.getProfileImage(); // Lấy đường dẫn ảnh đại diện
            }
        }
        model.addAttribute("profileImage", profileImage);
        model.addAttribute("movie", movie);
        return "movies/movie-detail"; // tên view cho trang chi tiết phim
    }

    @GetMapping("/add")
    public String showAddMovieForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String profileImage = null;
        if (userDetails != null) {
            User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            if (user != null) {
                profileImage = user.getProfileImage(); // Lấy đường dẫn ảnh đại diện
            }
        }
        model.addAttribute("profileImage", profileImage);
        model.addAttribute("movie", new Movie());
        model.addAttribute("allGenres", genreRepository.findAll());
        model.addAttribute("allActors", actorRepository.findAll());
        model.addAttribute("allCountries", countryRepository.findAll()); // Thêm danh sách quốc gia
        return "movies/add-movie";
    }

    @PostMapping("/add")
    public String addMovie(@ModelAttribute Movie movie,
                           @RequestParam("trailerUrl") String trailerUrl,  // Thay đổi từ MultipartFile thành String
                           @RequestParam("posterUrl") String posterUrl,  // Thay đổi từ MultipartFile thành String
                           @RequestParam("backgroundUrl") String backgroundUrl,  // Thay đổi từ MultipartFile thành String
                           @RequestParam(required = false) List<Long> genreIds,
                           @RequestParam("countryId") Long countryId,
                           @RequestParam(required = false) List<Long> actorIds,
                           @RequestParam String slug) {
        // Thiết lập đường dẫn video, trailer và poster vào movie
        movie.setTrailerUrl(trailerUrl);
        movie.setPosterUrl(posterUrl);
        movie.setBackgroundUrl(backgroundUrl);

        Country country = countryRepository.findById(countryId)
                .orElseThrow(() -> new IllegalArgumentException("Quốc gia không tồn tại"));
        movie.setCountry(country);
        // Thiết lập danh sách thể loại cho phim
        if (genreIds != null && !genreIds.isEmpty()) { // Kiểm tra trước khi tìm kiếm
            List<Genre> genres = genreRepository.findAllById(genreIds);
            movie.setGenres(genres);
        }

        // Thiết lập danh sách diễn viên cho phim
        if (actorIds != null && !actorIds.isEmpty()) { // Kiểm tra trước khi tìm kiếm
            List<Actor> actors = actorRepository.findAllById(actorIds);
            movie.setActors(actors);
        }

        movie.setUpdatedAt(new Date()); // Cập nhật thời gian hiện tại
        movieRepository.save(movie);
        movieSynchronizationService.synchronizeMovieBySlug(slug);
        return "redirect:/movies";
    }



    @GetMapping("/edit/{id}")
    public String showEditMovieForm(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id, Model model) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ID phim không hợp lệ: " + id));
        String profileImage = null;
        if (userDetails != null) {
            User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            if (user != null) {
                profileImage = user.getProfileImage(); // Lấy đường dẫn ảnh đại diện
            }
        }
        // Lấy danh sách các ID diễn viên và thể loại đã chọn
        List<Long> selectedActorIds = movie.getActors().stream()
                .map(Actor::getActorId)
                .collect(Collectors.toList());
        List<Long> selectedGenreIds = movie.getGenres().stream()
                .map(Genre::getGenreId)
                .collect(Collectors.toList());

        // Thêm vào model
        model.addAttribute("profileImage", profileImage);
        model.addAttribute("movie", movie);
        model.addAttribute("allGenres", genreRepository.findAll());
        model.addAttribute("allActors", actorRepository.findAll());
        model.addAttribute("allCountries", countryRepository.findAll());
        model.addAttribute("selectedActorIds", selectedActorIds); // Truyền các diễn viên đã chọn
        model.addAttribute("selectedGenreIds", selectedGenreIds); // Truyền các thể loại đã chọn

        return "movies/edit-movie";
    }


    @PostMapping("/edit/{id}")
    public String updateMovie(@PathVariable Long id,
                              @ModelAttribute Movie movie,
                              @RequestParam(value = "videoUrl", required = false) String videoUrl,  // Thay đổi từ MultipartFile thành String
                              @RequestParam(value = "trailerUrl", required = false) String trailerUrl,  // Thay đổi từ MultipartFile thành String
                              @RequestParam(value = "posterUrl", required = false) String posterUrl,  // Thay đổi từ MultipartFile thành String
                              @RequestParam(value = "backgroundUrl", required = false) String backgroundUrl,  // Thay đổi từ MultipartFile thành String
                              @RequestParam("countryId") Long countryId,
                              @RequestParam List<Long> genreIds,
                              @RequestParam(required = false) List<Long> actorIds) {

        // Lấy thông tin phim hiện tại từ cơ sở dữ liệu
        Movie existingMovie = movieRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ID phim không hợp lệ: " + id));
        // Giữ nguyên giá trị viewCount và weeklyViewCount
        movie.setViewCount(existingMovie.getViewCount());
        movie.setWeeklyViewCount(existingMovie.getWeeklyViewCount());
        movie.setEpisodes(existingMovie.getEpisodes());



        // Cập nhật trailer mới nếu có URL
        if (trailerUrl != null && !trailerUrl.isEmpty()) {
            movie.setTrailerUrl(trailerUrl); // Cập nhật trailer mới
        } else {
            movie.setTrailerUrl(existingMovie.getTrailerUrl()); // Giữ nguyên trailer cũ
        }

        // Cập nhật poster mới nếu có URL
        if (posterUrl != null && !posterUrl.isEmpty()) {
            movie.setPosterUrl(posterUrl); // Cập nhật poster mới
        } else {
            movie.setPosterUrl(existingMovie.getPosterUrl()); // Giữ nguyên poster cũ
        }

        // Cập nhật background mới nếu có URL
        if (backgroundUrl != null && !backgroundUrl.isEmpty()) {
            movie.setBackgroundUrl(backgroundUrl); // Cập nhật background mới
        } else {
            movie.setBackgroundUrl(existingMovie.getBackgroundUrl()); // Giữ nguyên background cũ
        }

        // Thiết lập danh sách thể loại cho phim
        if (genreIds != null && !genreIds.isEmpty()) { // Kiểm tra trước khi tìm kiếm
            List<Genre> genres = genreRepository.findAllById(genreIds);
            movie.setGenres(genres);
        }

        // Thiết lập danh sách diễn viên cho phim
        if (actorIds == null || actorIds.isEmpty()) {
            movie.setActors(null); // Đặt danh sách diễn viên là null nếu không có diễn viên nào được chọn
        } else {
            List<Actor> actors = actorRepository.findAllById(actorIds);
            movie.setActors(actors); // Cập nhật danh sách diễn viên mới
        }

        // Đặt ID cho đối tượng phim
        Country country = countryRepository.findById(countryId)
                .orElseThrow(() -> new IllegalArgumentException("Quốc gia không tồn tại"));
        movie.setCountry(country);
        movie.setMovieId(id);
        movie.setUpdatedAt(new Date()); // Cập nhật thời gian hiện tại
        movieRepository.save(movie); // Cập nhật phim
        return "redirect:/movies"; // Chuyển hướng đến danh sách phim
    }


    @Transactional
    @GetMapping("/delete/{id}")
    public String deleteMovie(@PathVariable Long id) {
        // Kiểm tra sự tồn tại của phim
        if (!movieRepository.existsById(id)) {
            throw new IllegalArgumentException("ID phim không hợp lệ: " + id);
        }

        // Xóa các đánh giá liên quan đến phim
        ratingRepository.deleteByMovieId(id); // Giả định bạn có phương thức này trong reviewRepository

        // Xóa lịch sử xem liên quan đến phim
        watchHistoryRepository.deleteByMovieId(id); // Giả định bạn có phương thức này trong watchHistoryRepository

        // Xóa danh sách yêu thích liên quan đến phim
        favoritesRepository.deleteByMovieId(id); // Giả định bạn có phương thức này trong favoritesRepository

        // Xóa danh sách yêu thích liên quan đến phim
        commentRepository.deleteByMovieId(id); // Giả định bạn có phương thức này trong favoritesRepository

        // Xóa phim
        movieRepository.deleteById(id);

        return "redirect:/movies"; // Chuyển hướng đến danh sách phim
    }

    @Autowired
    private EpisodeService episodeService;



    @PostMapping("/update-episodes")
    public String updateEpisodes(RedirectAttributes redirectAttributes) {
        try {
            List<Movie> movies = movieRepository.findByStatusNot("Hoàn Tất");

            for (Movie movie : movies) {
                episodeService.updateMovieAndEpisodesBySlug(movie.getSlug());
            }

            // Thêm thông báo thành công
            redirectAttributes.addFlashAttribute("message", "Cập nhật tập phim thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            // Thêm thông báo lỗi
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi khi cập nhật tập phim.");
        }

        // Chuyển hướng về danh sách phim
        return "redirect:/movies";
    }

}
