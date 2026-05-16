package com.example.movie.controller;

import com.example.movie.StringUtils;
import com.example.movie.elasticsearch.MovieDocument;
import com.example.movie.elasticsearch.MovieDocumentRepository;
import com.example.movie.model.*;
import com.example.movie.repository.*;
import com.example.movie.service.BannerAdsService;
import com.example.movie.service.MovieSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Controller
public class ElasticsearchController {
    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private ActorRepository actorRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private MovieDocumentRepository movieDocumentRepository;

    @Autowired
    private MovieSearchService movieSearchService;

    @Autowired
    private BannerAdsService bannerAdsService;
    @GetMapping("/search")
    public String filterMovies(@AuthenticationPrincipal UserDetails userDetails,
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
        List<Movie> allMovies = movieRepository.findAll(); // Lấy danh sách tất cả phim
        List<Movie> moviess = new ArrayList<>(allMovies);
        List<Genre> genres = genreRepository.findAll();
        List<Actor> actors = actorRepository.findAll();
        List<Country> countries = countryRepository.findAll();

        for (Genre genre : genres) {
            long count = moviess.stream()
                    .filter(movie -> movie.getGenres().stream().anyMatch(g -> g.getGenreId().equals(genre.getGenreId())))
                    .count();
            genre.setMovieCount(count);
        }
        for (Actor actor : actors) {
            long count = moviess.stream()
                    .filter(movie -> movie.getActors().stream().anyMatch(a -> a.getActorId().equals(actor.getActorId())))
                    .count();
            actor.setMovieCount(count);
        }
        for (Country country : countries) {
            long count = moviess.stream()
                    .filter(movie -> movie.getCountry() != null && movie.getCountry().getCountryId().equals(country.getCountryId()))
                    .count();
            country.setMovieCount(count);
        }
        List<Integer> releaseYears = moviess.stream()
                .map(Movie::getReleaseYear)
                .filter(year -> year != null)
                .distinct()
                .sorted((year1, year2) -> Integer.compare(year2, year1)) // Sắp xếp từ lớn đến bé
                .collect(Collectors.toList());



        if (releaseYear != null) {
            movies = moviess.stream()
                    .filter(movie -> releaseYear.equals(movie.getReleaseYear()))
                    .collect(Collectors.toList());
        }
        // Áp dụng các bộ lọc
        if (genreIds != null && !genreIds.isEmpty()) {
            movies = moviess.stream()
                    .filter(movie -> movie.getGenres().stream()
                            .anyMatch(genre -> genreIds.contains(genre.getGenreId())))
                    .collect(Collectors.toList());
        }
        if ("phimle".equals(cat)) {
            movies = moviess.stream().filter(movie -> !movie.isSeries()).collect(Collectors.toList());
        } else if ("phimbo".equals(cat)) {
            movies = moviess.stream().filter(Movie::isSeries).collect(Collectors.toList());
        }
        if (actorIds != null && !actorIds.isEmpty()) {
            movies = moviess.stream()
                    .filter(movie -> movie.getActors().stream()
                            .anyMatch(actor -> actorIds.contains(actor.getActorId())))
                    .collect(Collectors.toList());
        }
        if (countryIds != null && !countryIds.isEmpty()) {
            movies = moviess.stream()
                    .filter(movie -> movie.getCountry() != null && countryIds.contains(movie.getCountry().getCountryId()))
                    .collect(Collectors.toList());
        }

        // Phân trang
        int pageSize = 16;
        Pageable pageable = PageRequest.of(page, pageSize);
        int start = Math.min((int) pageable.getOffset(), movies.size());
        int end = Math.min((start + pageable.getPageSize()), movies.size());
        List<Movie> filteredMovies = movies.subList(start, end);
        Page<Movie> moviePage = new PageImpl<>(filteredMovies, pageable, movies.size());




        List<BannerAds> topBanners = bannerAdsService.getActiveBannersByPosition("top");
        List<BannerAds> bottomBanners = bannerAdsService.getActiveBannersByPosition("bottom");
        model.addAttribute("topBanners", topBanners);
        model.addAttribute("bottomBanners", bottomBanners);
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

        return "Home/search"; // Trả về trang locphim.html
    }
    @GetMapping("/live")
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

}
