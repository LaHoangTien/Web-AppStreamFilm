package com.example.movie.controller;

import com.example.movie.DTO.GenreDTO;
import com.example.movie.DTO.MovieDTO;
import com.example.movie.StringUtils;
import com.example.movie.elasticsearch.MovieDocument;
import com.example.movie.model.*;
import com.example.movie.repository.*;
import com.example.movie.service.EpisodeService;
import com.example.movie.service.MovieSearchService;
import com.example.movie.service.MovieService;
import com.example.movie.service.MovieSynchronizationService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api")
public class MovieAPIController {

    @Autowired
    private EpisodeService episodeService;

    @Autowired
    private MovieSynchronizationService movieSynchronizationService;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private ActorRepository actorRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private  MovieService movieService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MovieSearchService movieSearchService;

    @Autowired
    private EpisodeRepository episodeRepository;
    @Autowired
    private  FavoritesRepository favoritesRepository;
    @Autowired
    private  RatingRepository ratingRepository;
    @Autowired
    private  WatchHistoryRepository watchHistoryRepository;
    @Autowired
    private CommentRepository commentRepository;
    @GetMapping(value = "/movies", produces = "application/json; charset=UTF-8")
    public ResponseEntity<Map<String, Object>> listMovies(
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String cat,
            @RequestParam(required = false) List<Long> genreIds,
            @RequestParam(required = false) List<Long> actorIds,
            @RequestParam(required = false) List<Long> countryIds,
            @RequestParam(required = false) Integer releaseYear,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            @AuthenticationPrincipal UserDetails userDetails) {

        // Lấy danh sách phim
        List<Movie> allMovies = movieRepository.findAll();
        List<Movie> movies = new ArrayList<>(allMovies);

        // Lọc phim theo các điều kiện
        if (releaseYear != null) {
            movies = movies.stream()
                    .filter(movie -> releaseYear.equals(movie.getReleaseYear()))
                    .collect(Collectors.toList());
        }
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
        if (status != null && !status.isEmpty()) {
            movies = movies.stream()
                    .filter(movie -> status.equals(movie.getStatus()))
                    .collect(Collectors.toList());
        }
        if (keyword != null && !keyword.isEmpty()) {
            String normalizedKeyword = StringUtils.removeDiacritics(keyword.toLowerCase());
            movies = movies.stream()
                    .filter(movie -> StringUtils.removeDiacritics(movie.getTitle().toLowerCase()).contains(normalizedKeyword) ||
                            StringUtils.removeDiacritics(movie.getName().toLowerCase()).contains(normalizedKeyword))
                    .collect(Collectors.toList());
        }

        // Sắp xếp
        if ("asc".equals(sort)) {
            movies.sort(Comparator.comparing(Movie::getUpdatedAt));
        } else {
            movies.sort(Comparator.comparing(Movie::getUpdatedAt).reversed());
        }

        // Phân trang
        Pageable pageable = PageRequest.of(page, size);
        int start = Math.min((int) pageable.getOffset(), movies.size());
        int end = Math.min((start + pageable.getPageSize()), movies.size());
        List<Movie> filteredMovies = movies.subList(start, end);

        Page<Movie> moviePage = new PageImpl<>(filteredMovies, pageable, movies.size());

        // Chuẩn bị dữ liệu trả về
        Map<String, Object> response = new HashMap<>();
        response.put("movies", moviePage.getContent());
        response.put("currentPage", moviePage.getNumber());
        response.put("totalItems", moviePage.getTotalElements());
        response.put("totalPages", moviePage.getTotalPages());

        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/categorized-movies", produces = "application/json; charset=UTF-8")
    public ResponseEntity<Map<String, List<Movie>>> getCategorizedMovies() {
        // Lấy danh sách tất cả phim từ MovieService
        List<Movie> allMovies = movieRepository.findAllByOrderByUpdatedAtDesc();

        // Phân loại phim bằng phương thức categorizeMovies từ MovieService
        Map<String, List<Movie>> categorizedMovies = movieService.categorizeMovies(allMovies);

        // Trả về danh sách phim đã phân loại
        return ResponseEntity.ok(categorizedMovies);
    }

    @GetMapping(value = "/filter-movies", produces = "application/json; charset=UTF-8")
    public ResponseEntity<Map<String, Object>> filterMovies(
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String cat,
            @RequestParam(required = false) List<Long> genreIds,
            @RequestParam(required = false) List<Long> actorIds,
            @RequestParam(required = false) List<Long> countryIds,
            @RequestParam(required = false) Integer releaseYear,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page) {

        // Lấy danh sách tất cả phim
        List<Movie> allMovies = movieRepository.findAll();
        List<Movie> movies = new ArrayList<>(allMovies);

        // Lọc phim theo năm phát hành
        if (releaseYear != null) {
            movies = movies.stream()
                    .filter(movie -> releaseYear.equals(movie.getReleaseYear()))
                    .collect(Collectors.toList());
        }

        // Lọc phim theo thể loại
        if (genreIds != null && !genreIds.isEmpty()) {
            movies = movies.stream()
                    .filter(movie -> movie.getGenres().stream()
                            .anyMatch(genre -> genreIds.contains(genre.getGenreId())))
                    .collect(Collectors.toList());
        }

        // Lọc phim lẻ hoặc phim bộ
        if ("phimle".equals(cat)) {
            movies = movies.stream().filter(movie -> !movie.isSeries()).collect(Collectors.toList());
        } else if ("phimbo".equals(cat)) {
            movies = movies.stream().filter(Movie::isSeries).collect(Collectors.toList());
        }

        // Lọc phim theo diễn viên
        if (actorIds != null && !actorIds.isEmpty()) {
            movies = movies.stream()
                    .filter(movie -> movie.getActors().stream()
                            .anyMatch(actor -> actorIds.contains(actor.getActorId())))
                    .collect(Collectors.toList());
        }

        // Lọc phim theo quốc gia
        if (countryIds != null && !countryIds.isEmpty()) {
            movies = movies.stream()
                    .filter(movie -> movie.getCountry() != null && countryIds.contains(movie.getCountry().getCountryId()))
                    .collect(Collectors.toList());
        }

        // Sắp xếp phim
        if ("asc".equals(sort)) {
            movies.sort(Comparator.comparing(Movie::getUpdatedAt));
        } else {
            movies.sort(Comparator.comparing(Movie::getUpdatedAt).reversed());
        }

        // Phân trang
        int pageSize = 16;
        Pageable pageable = PageRequest.of(page, pageSize);
        int start = Math.min((int) pageable.getOffset(), movies.size());
        int end = Math.min((start + pageable.getPageSize()), movies.size());
        List<Movie> filteredMovies = movies.subList(start, end);

        // Chuẩn bị dữ liệu trả về
        Map<String, Object> response = new HashMap<>();
        response.put("movies", filteredMovies);
        response.put("currentPage", pageable.getPageNumber());
        response.put("totalItems", movies.size());
        response.put("totalPages", (int) Math.ceil((double) movies.size() / pageSize));

        return ResponseEntity.ok(response);
    }
    @GetMapping(value = "/filter-options", produces = "application/json; charset=UTF-8")
    public ResponseEntity<Map<String, Object>> getFilterOptions() {
        // Lấy danh sách tất cả các thể loại
        List<Genre> genres = genreRepository.findAll();

        // Lấy danh sách tất cả các quốc gia
        List<Country> countries = countryRepository.findAll();

        // Lấy danh sách các năm phát hành từ danh sách phim
        List<Movie> allMovies = movieRepository.findAll();
        List<Integer> releaseYears = allMovies.stream()
                .map(Movie::getReleaseYear)
                .filter(Objects::nonNull) // Loại bỏ các năm bị null
                .distinct() // Loại bỏ các năm trùng lặp
                .sorted(Comparator.reverseOrder()) // Sắp xếp theo thứ tự giảm dần
                .collect(Collectors.toList());

        // Chuẩn bị dữ liệu trả về
        Map<String, Object> response = new HashMap<>();
        response.put("genres", genres);
        response.put("countries", countries);
        response.put("releaseYears", releaseYears);

        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/movies/{movieId}/details", produces = "application/json; charset=UTF-8")
    public ResponseEntity<Map<String, Object>> getMovieDetails(@PathVariable Long movieId) {
        // Tìm thông tin phim theo ID
        Optional<Movie> optionalMovie = movieRepository.findById(movieId);
        if (!optionalMovie.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("error", "Movie not found"));
        }

        Movie movie = optionalMovie.get();

        // Lấy danh sách tập của phim (nếu là phim bộ)
        List<Episode> episodes = episodeRepository.findByMovie_MovieId(movieId);
        List<Movie> moviesInSameGenre = movieRepository.findMoviesByGenres(movie.getGenres(), movie.getSlug());

        // Chuẩn bị dữ liệu trả về
        Map<String, Object> response = new HashMap<>();
        response.put("moviesInSameGenre", moviesInSameGenre);
        response.put("movieId", movie.getMovieId());
        response.put("title", movie.getTitle());
        response.put("name", movie.getName());
        response.put("description", movie.getDescription());
        response.put("releaseYear", movie.getReleaseYear());
        response.put("trailerUrl", movie.getTrailerUrl());
        response.put("posterUrl", movie.getPosterUrl());
        response.put("backgroundUrl", movie.getBackgroundUrl());
        response.put("director", movie.getDirector());
        response.put("isSeries", movie.getIsSeries());
        response.put("duration", movie.getDuration());
        response.put("country", Map.of(
                "countryId", movie.getCountry().getCountryId(),
                "countryName", movie.getCountry().getCountryName()
        ));

        // Trả về cả id và name cho thể loại
        response.put("genres", movie.getGenres().stream()
                .map(genre -> Map.of(
                        "genreId", genre.getGenreId(),
                        "genreName", genre.getGenreName()
                ))
                .collect(Collectors.toList()));

        // Trả về cả id và name cho diễn viên
        response.put("actors", movie.getActors().stream()
                .map(actor -> Map.of(
                        "actorId", actor.getActorId(),
                        "actorName", actor.getActorName()
                ))
                .collect(Collectors.toList()));

        response.put("totalEpisodes", movie.getTotalEpisodes());
        response.put("status", movie.getStatus());
        response.put("episodes", episodes);

        return ResponseEntity.ok(response);
    }


    @GetMapping(value = "/live", produces = "application/json; charset=UTF-8")
    public ResponseEntity<List<Movie>> filterMovies(@RequestParam(required = false) String keyword,
                                                    @RequestParam(defaultValue = "0") int page) {

        // Nếu từ khóa trống, trả về toàn bộ danh sách phim
        if (keyword == null || keyword.trim().isEmpty()) {
            List<Movie> allMovies = movieRepository.findAll();
            return ResponseEntity.ok(allMovies);
        }

        // Tìm kiếm fuzzy với Elasticsearch
        List<MovieDocument> searchResults = movieSearchService.searchMoviesFuzzy(keyword);

        // Lấy danh sách ID và duy trì thứ tự
        List<Long> orderedMovieIds = searchResults.stream()
                .map(MovieDocument::getMovieId)
                .collect(Collectors.toList());

        // Truy vấn phim từ cơ sở dữ liệu theo ID và duy trì thứ tự
        List<Movie> movies = movieRepository.findAllById(orderedMovieIds).stream()
                .sorted(Comparator.comparingInt(movie -> orderedMovieIds.indexOf(movie.getMovieId())))
                .collect(Collectors.toList());

        return ResponseEntity.ok(movies);
    }
    @GetMapping(value = "/top-rated", produces = "application/json; charset=UTF-8")
    public ResponseEntity<Map<String, Object>> getTopRatedMovies() {
        // Lấy danh sách top 10 phim đánh giá cao
        List<Movie> top10HotMovies = movieService.getTop10HighestRatedMovies();

        // Chuẩn bị phản hồi JSON
        Map<String, Object> response = new HashMap<>();
        response.put("topRatedMovies", top10HotMovies);

        return ResponseEntity.ok(response);
    }
    @PostMapping("/addMovieAndEpisodes")
    public ResponseEntity<String> addMovieAndEpisodes(@RequestParam String slug) {
        try {
            // Thêm phim và các tập liên quan
            episodeService.addMovieAndEpisodesBySlug(slug);

            // Gọi đồng bộ chỉ cho phim vừa thêm
            movieSynchronizationService.synchronizeMovieBySlug(slug);

            return ResponseEntity.ok("Thêm phim và đồng bộ hóa thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi thêm phim!");
        }
    }
    @GetMapping("/edit/{id}")
    public ResponseEntity<?> getMovieDetails(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Movie movie = movieRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("ID phim không hợp lệ: " + id));

            String profileImage = null;
            if (userDetails != null) {
                User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
                if (user != null) {
                    profileImage = user.getProfileImage(); // Lấy đường dẫn ảnh đại diện
                }
            }

            // Lấy danh sách ID diễn viên và thể loại đã chọn
            List<Long> selectedActorIds = movie.getActors().stream()
                    .map(Actor::getActorId)
                    .collect(Collectors.toList());
            List<Long> selectedGenreIds = movie.getGenres().stream()
                    .map(Genre::getGenreId)
                    .collect(Collectors.toList());

            // Tạo dữ liệu trả về
            Map<String, Object> response = new HashMap<>();
            response.put("movie", movie);
            response.put("profileImage", profileImage);
            response.put("allGenres", genreRepository.findAll());
            response.put("allActors", actorRepository.findAll());
            response.put("allCountries", countryRepository.findAll());
            response.put("selectedActorIds", selectedActorIds);
            response.put("selectedGenreIds", selectedGenreIds);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Lỗi khi lấy thông tin phim: " + e.getMessage());
        }
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<?> updateMovie(@PathVariable Long id,
                                         @RequestBody Map<String, Object> movieData) {
        try {
            // Lấy thông tin phim hiện tại từ cơ sở dữ liệu
            Movie existingMovie = movieRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("ID phim không hợp lệ: " + id));

            // Cập nhật các thuộc tính từ dữ liệu JSON
            existingMovie.setTitle((String) movieData.get("title"));
            existingMovie.setName((String) movieData.get("name"));
            existingMovie.setSlug((String) movieData.get("slug"));
            existingMovie.setDescription((String) movieData.get("description"));
            existingMovie.setReleaseYear((Integer) movieData.get("releaseYear"));
            existingMovie.setDirector((String) movieData.get("director"));
            existingMovie.setTotalEpisodes((String) movieData.get("totalEpisodes"));
            existingMovie.setDuration((Integer) movieData.get("duration"));
            existingMovie.setTrailerUrl((String) movieData.get("trailerUrl"));
            existingMovie.setPosterUrl((String) movieData.get("posterUrl"));
            existingMovie.setBackgroundUrl((String) movieData.get("backgroundUrl"));
            existingMovie.setIsSeries((Boolean) movieData.get("isSeries"));

            // Cập nhật quốc gia
            Long countryId = Long.valueOf((Integer) movieData.get("countryId"));
            Country country = countryRepository.findById(countryId)
                    .orElseThrow(() -> new IllegalArgumentException("Quốc gia không tồn tại"));
            existingMovie.setCountry(country);

            // Cập nhật danh sách thể loại
            List<Long> genreIds = (List<Long>) movieData.get("genreIds");
            if (genreIds != null && !genreIds.isEmpty()) {
                List<Genre> genres = genreRepository.findAllById(genreIds);
                existingMovie.setGenres(genres);
            }

            // Cập nhật danh sách diễn viên
            List<Long> actorIds = (List<Long>) movieData.get("actorIds");
            if (actorIds != null && !actorIds.isEmpty()) {
                List<Actor> actors = actorRepository.findAllById(actorIds);
                existingMovie.setActors(actors);
            } else {
                existingMovie.setActors(null);
            }

            // Lưu phim
            movieRepository.save(existingMovie);

            return ResponseEntity.ok("Phim đã được cập nhật thành công.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Lỗi khi cập nhật phim: " + e.getMessage());
        }
    }

    @Transactional
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteMovie(@PathVariable Long id) {
        // Kiểm tra sự tồn tại của phim
        if (!movieRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("ID phim không hợp lệ: " + id);
        }

        // Xóa các đánh giá liên quan đến phim
        ratingRepository.deleteByMovieId(id); // Giả định phương thức này tồn tại

        // Xóa lịch sử xem liên quan đến phim
        watchHistoryRepository.deleteByMovieId(id); // Giả định phương thức này tồn tại

        // Xóa danh sách yêu thích liên quan đến phim
        favoritesRepository.deleteByMovieId(id); // Giả định phương thức này tồn tại

        // Xóa bình luận liên quan đến phim
        commentRepository.deleteByMovieId(id); // Giả định phương thức này tồn tại

        // Xóa phim
        movieRepository.deleteById(id);

        return ResponseEntity.ok("Phim đã được xóa thành công");
    }
    @PostMapping("/update-episodes")
    public ResponseEntity<?> updateEpisodes() {
        try {
            List<Movie> movies = movieRepository.findByStatusNot("Hoàn Tất");

            for (Movie movie : movies) {
                episodeService.updateMovieAndEpisodesBySlug(movie.getSlug());
            }

            // Trả về thông báo thành công
            return ResponseEntity.ok("Cập nhật tập phim thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            // Trả về thông báo lỗi
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Đã xảy ra lỗi khi cập nhật tập phim.");
        }
    }
    @DeleteMapping("episodes/delete/{id}")
    public ResponseEntity<?> deleteEpisode(@PathVariable("id") Long episodeId) {
        try {
            Episode episode = episodeRepository.findById(episodeId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid episode ID: " + episodeId));

            Movie movie = episode.getMovie();

            // Xóa tập phim khỏi cơ sở dữ liệu
            episodeRepository.delete(episode);

            // Cập nhật thời gian của movie sau khi xóa tập
            movieRepository.save(movie);

            return ResponseEntity.ok("Episode deleted successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while deleting the episode.");
        }
    }
}
