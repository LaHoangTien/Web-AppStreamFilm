package com.example.movie.controller;

//import com.example.movie.elasticsearch.MovieDocument;
import com.example.movie.StringUtils;
import com.example.movie.model.*;
import com.example.movie.repository.*;
//import com.example.movie.service.MovieSearchService;
import com.example.movie.service.BannerAdsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.ResponseBody;

import static com.example.movie.StringUtils.removeDiacritics;

@Controller
public class MovieFilterController {

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
    private BannerAdsService bannerAdsService;

    @GetMapping("/locphim")
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
        List<Movie> allMovies = movieRepository.findAll(); // Lấy danh sách tất cả phim
        List<Movie> movies = new ArrayList<>(allMovies); // Khởi tạo danh sách phim dựa trên tất cả phim
        List<Genre> genres = genreRepository.findAll();
        List<Actor> actors = actorRepository.findAll();
        List<Country> countries = countryRepository.findAll();
        String profileImage = null;
        if (userDetails != null) {
            User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            if (user != null) {
                profileImage = user.getProfileImage(); // Lấy đường dẫn ảnh đại diện
            }
        }
        model.addAttribute("profileImage", profileImage);
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

        return "Home/locphim"; // Trả về trang locphim.html
    }


//    @GetMapping("/livesearch")
//    @ResponseBody
//    public List<Movie> filterMovies(@RequestParam(required = false) String keyword,
//                                    @RequestParam(defaultValue = "0") int page) {
//
//        // Tạo danh sách tất cả phim
//        List<Movie> allMovies = movieRepository.findAll();
//        List<Movie> movies = new ArrayList<>(allMovies);
//        // Tìm kiếm theo từ khóa
//        if (keyword != null && !keyword.isEmpty()) {
//            String normalizedKeyword = StringUtils.removeDiacritics(keyword.toLowerCase());
//
//            // Phương thức kiểm tra tìm kiếm
//            Predicate<Movie> searchPredicate = movie -> {
//                String title = StringUtils.removeDiacritics(movie.getTitle().toLowerCase());
//                String name = StringUtils.removeDiacritics(movie.getName().toLowerCase());
//                String description = StringUtils.removeDiacritics(movie.getDescription().toLowerCase());
//                String director = StringUtils.removeDiacritics(movie.getDirector().toLowerCase());
//                String releaseYearStr = (movie.getReleaseYear() != null) ? movie.getReleaseYear().toString() : "";
//                String isSeries = (movie.getIsSeries() != null) ? movie.getIsSeries().toString() : "";
//                String country = (movie.getCountry() != null) ?
//                        StringUtils.removeDiacritics(movie.getCountry().getCountryName().toLowerCase()) : "";
//                String genress = movie.getGenres().stream()
//                        .map(genre -> StringUtils.removeDiacritics(genre.getGenreName().toLowerCase()))
//                        .collect(Collectors.joining(" "));
//                String actorss = movie.getActors().stream()
//                        .map(actor -> StringUtils.removeDiacritics(actor.getActorName().toLowerCase()))
//                        .collect(Collectors.joining(" "));
//
//                // So khớp chính xác
//                return title.contains(normalizedKeyword) ||
//                        name.contains(normalizedKeyword) ||
//                        description.contains(normalizedKeyword) ||
//                        director.contains(normalizedKeyword) ||
//                        releaseYearStr.contains(normalizedKeyword) ||
//                        isSeries.contains(normalizedKeyword) ||
//                        country.contains(normalizedKeyword) ||
//                        genress.contains(normalizedKeyword) ||
//                        actorss.contains(normalizedKeyword);
//            };
//
//            // Phương thức kiểm tra tìm kiếm gần đúng
//            Predicate<Movie> fuzzySearchPredicate = movie -> {
//                String title = StringUtils.removeDiacritics(movie.getTitle().toLowerCase());
//                String name = StringUtils.removeDiacritics(movie.getName().toLowerCase());
//                String description = StringUtils.removeDiacritics(movie.getDescription().toLowerCase());
//                String director = StringUtils.removeDiacritics(movie.getDirector().toLowerCase());
//                String releaseYearStr = (movie.getReleaseYear() != null) ? movie.getReleaseYear().toString() : "";
//                String isSeries = (movie.getIsSeries() != null) ? movie.getIsSeries().toString() : "";
//                String country = (movie.getCountry() != null) ?
//                        StringUtils.removeDiacritics(movie.getCountry().getCountryName().toLowerCase()) : "";
//                String genress = movie.getGenres().stream()
//                        .map(genre -> StringUtils.removeDiacritics(genre.getGenreName().toLowerCase()))
//                        .collect(Collectors.joining(" "));
//                String actorss = movie.getActors().stream()
//                        .map(actor -> StringUtils.removeDiacritics(actor.getActorName().toLowerCase()))
//                        .collect(Collectors.joining(" "));
//
//                // So khớp gần đúng
//                return StringUtils.isSimilar(title, normalizedKeyword) ||
//                        StringUtils.isSimilar(name, normalizedKeyword) ||
//                        StringUtils.isSimilar(description, normalizedKeyword) ||
//                        StringUtils.isSimilar(director, normalizedKeyword) ||
//                        StringUtils.isSimilar(releaseYearStr, normalizedKeyword) ||
//                        StringUtils.isSimilar(isSeries, normalizedKeyword) ||
//                        StringUtils.isSimilar(country, normalizedKeyword) ||
//                        StringUtils.isSimilar(genress, normalizedKeyword) ||
//                        StringUtils.isSimilar(actorss, normalizedKeyword);
//            };
//
//            // Áp dụng tìm kiếm chính xác
//            List<Movie> filteredMovies = movies.stream()
//                    .filter(searchPredicate)
//                    .collect(Collectors.toList());
//
//            // Nếu không có kết quả, áp dụng tìm kiếm gần đúng
//            if (filteredMovies.isEmpty()) {
//                filteredMovies = movies.stream()
//                        .filter(fuzzySearchPredicate)
//                        .collect(Collectors.toList());
//            }
//
//            // Gán kết quả tìm kiếm
//            movies = filteredMovies;
//        }
//
//        // Trả về danh sách các phim khớp với bộ lọc và từ khóa tìm kiếm
//        return movies;
//    }

}
