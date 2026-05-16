package com.example.movie.service;

import com.example.movie.model.*;
import com.example.movie.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class EpisodeService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private EpisodeRepository episodeRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private ActorRepository actorRepository;

    public void addEpisodesFromApi(String apiUrl, Long movieId) {
        // Gửi yêu cầu GET đến API từ URL được cung cấp
        ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.GET, null, Map.class);
        Map<String, Object> responseBody = response.getBody();

        if (responseBody != null) {
            // Lấy thông tin phim từ API
            Map<String, Object> movieData = (Map<String, Object>) responseBody.get("movie");

            // Tìm hoặc tạo Movie dựa trên slug
            Movie movie = movieRepository.findById(movieId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy phim với Id: " + movieId));

            boolean isUpdated = false;


            // Cập nhật thông tin các tập phim
            if (responseBody.containsKey("episodes")) {
                List<Map<String, Object>> episodesData = (List<Map<String, Object>>) responseBody.get("episodes");

                for (Map<String, Object> episode : episodesData) {
                    List<Map<String, String>> serverData = (List<Map<String, String>>) episode.get("server_data");

                    for (Map<String, String> server : serverData) {
                        String episodeNumber = server.get("name"); // Lấy số tập
                        if (episodeNumber == null || episodeNumber.isBlank()) {
                            System.out.println("Bỏ qua tập rỗng hoặc không hợp lệ.");
                            continue;
                        }

                        // Xử lý loại bỏ số `0` ở đầu
                        episodeNumber = episodeNumber.replaceFirst("^0+", "");

                        // Kiểm tra xem tập phim đã tồn tại hay chưa
                        boolean exists = episodeRepository.existsByMovieAndEpisodeNumber(movie, episodeNumber);
                        if (!exists) {
                            Episode newEpisode = new Episode();
                            newEpisode.setMovie(movie);
                            newEpisode.setEpisodeNumber(episodeNumber);
                            newEpisode.setTitle(episodeNumber);
                            newEpisode.setVideoUrl(server.get("link_embed"));

                            episodeRepository.save(newEpisode);

                            isUpdated = true; // Đánh dấu có cập nhật mới
                            System.out.println("Thêm tập mới: " + episodeNumber + " cho phim: " + movie.getTitle());
                        }
                    }
                }
            }

            // Cập nhật `updatedAt` nếu có thay đổi
            if (isUpdated) {
                movie.setUpdatedAt(new Date());
                movieRepository.save(movie);
                System.out.println("Cập nhật thông tin cho phim: " + movie.getTitle());
            } else {
                System.out.println("Không có thay đổi nào cần cập nhật cho phim: " + movie.getTitle());
            }
        } else {
            throw new RuntimeException("Không thể lấy thông tin từ API.");
        }
    }


//    @Scheduled(cron = "0 * * * * *")
    public void scheduleEpisodeUpdates() {
        List<Movie> movies = movieRepository.findByStatusNot("Hoàn Tất");

        for (Movie movie : movies) {
            try {
                updateMovieAndEpisodesBySlug(movie.getSlug());
            } catch (Exception e) {
                // Ghi log hoặc xử lý lỗi nếu cần
                System.err.println("Lỗi khi cập nhật tập phim cho: " + movie.getTitle());
                e.printStackTrace();
            }
        }
    }
    public void updateMovieAndEpisodesBySlug(String slug) {
        // URL API dựa trên slug
        String apiUrl = "https://ophim1.com/phim/" + slug;

        // Gửi yêu cầu GET đến API
        ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.GET, null, Map.class);
        Map<String, Object> responseBody = response.getBody();

        if (responseBody != null) {
            // Lấy thông tin phim từ API
            Map<String, Object> movieData = (Map<String, Object>) responseBody.get("movie");

            // Tìm hoặc tạo Movie dựa trên slug
            Movie movie = movieRepository.findBySlug(slug)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy phim với slug: " + slug));

            boolean isUpdated = false;

            // Cập nhật thông tin cơ bản của phim
            String title = (String) movieData.get("name");
            if (!title.equals(movie.getTitle())) {
                movie.setTitle(title);
                isUpdated = true;
            }

            String posterUrl = (String) movieData.get("thumb_url");
            if (!posterUrl.equals(movie.getPosterUrl())) {
                movie.setPosterUrl(posterUrl);
                isUpdated = true;
            }

            String backgroundUrl = (String) movieData.get("poster_url");
            if (!backgroundUrl.equals(movie.getBackgroundUrl())) {
                movie.setBackgroundUrl(backgroundUrl);
                isUpdated = true;
            }

            String apiStatus = (String) movieData.get("status");
            String mappedStatus = mapStatus(apiStatus); // Ánh xạ status từ API
            if (!mappedStatus.equals(movie.getStatus())) {
                movie.setStatus(mappedStatus);
                isUpdated = true;
            }

            // Cập nhật thông tin các tập phim
            if (responseBody.containsKey("episodes")) {
                List<Map<String, Object>> episodesData = (List<Map<String, Object>>) responseBody.get("episodes");

                for (Map<String, Object> episode : episodesData) {
                    List<Map<String, String>> serverData = (List<Map<String, String>>) episode.get("server_data");

                    for (Map<String, String> server : serverData) {
                        String episodeNumber = server.get("name"); // Lấy số tập
                        if (episodeNumber == null || episodeNumber.isBlank()) {
                            System.out.println("Bỏ qua tập rỗng hoặc không hợp lệ.");
                            continue;
                        }

                        // Xử lý loại bỏ số `0` ở đầu
                        episodeNumber = episodeNumber.replaceFirst("^0+", "");

                        // Kiểm tra xem tập phim đã tồn tại hay chưa
                        boolean exists = episodeRepository.existsByMovieAndEpisodeNumber(movie, episodeNumber);
                        if (!exists) {
                            Episode newEpisode = new Episode();
                            newEpisode.setMovie(movie);
                            newEpisode.setEpisodeNumber(episodeNumber);
                            newEpisode.setTitle(episodeNumber);
                            newEpisode.setVideoUrl(server.get("link_embed"));

                            episodeRepository.save(newEpisode);

                            isUpdated = true; // Đánh dấu có cập nhật mới
                            System.out.println("Thêm tập mới: " + episodeNumber + " cho phim: " + movie.getTitle());
                        }
                    }
                }
            }

            // Cập nhật `updatedAt` nếu có thay đổi
            if (isUpdated) {
                movie.setUpdatedAt(new Date());
                movieRepository.save(movie);
                System.out.println("Cập nhật thông tin cho phim: " + movie.getTitle());
            } else {
                System.out.println("Không có thay đổi nào cần cập nhật cho phim: " + movie.getTitle());
            }
        } else {
            throw new RuntimeException("Không thể lấy thông tin từ API.");
        }
    }


    private static final Map<String, String> STATUS_MAP = Map.of(
            "trailer", "Sắp Chiếu",
            "ongoing", "Đang Chiếu",
            "completed", "Hoàn Tất"
    );

    private String mapStatus(String apiStatus) {
        return STATUS_MAP.getOrDefault(apiStatus, "??");
    }
    public void addMovieAndEpisodesBySlug(String slug) {
        // URL API dựa trên slug
        String apiUrl = "https://ophim1.com/phim/" + slug;

        // Gửi yêu cầu GET đến API
        ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.GET, null, Map.class);
        Map<String, Object> responseBody = response.getBody();

        if (responseBody != null) {
            // Lấy thông tin phim từ API
            Map<String, Object> movieData = (Map<String, Object>) responseBody.get("movie");

            Optional<Movie> optionalMovie = movieRepository.findBySlug(slug);
            Movie movie;
            if (optionalMovie.isPresent()) {
                // Nếu phim đã tồn tại, lấy đối tượng phim để cập nhật
                movie = optionalMovie.get();
                System.out.println("Phim đã tồn tại, cập nhật thông tin: " + movie.getTitle());
            } else {
                // Nếu phim chưa tồn tại, tạo đối tượng mới
                movie = new Movie();
                movie.setSlug(slug);
                System.out.println("Phim mới, thêm vào cơ sở dữ liệu: " + slug);
            }

            // Lấy thông tin cơ bản của phim từ API
            String title = (String) movieData.get("name");
            movie.setTitle(title);

            String Name = (String) movieData.get("origin_name");
            movie.setName(Name);

            String description = (String) movieData.get("content");
            movie.setDescription(description);

            String posterUrl = (String) movieData.get("thumb_url");
            movie.setPosterUrl(posterUrl);

            String backgroundUrl = (String) movieData.get("poster_url");
            movie.setBackgroundUrl(backgroundUrl);

            // Trailer URL
            String trailerUrl = (String) movieData.get("trailer_url");
            movie.setTrailerUrl(trailerUrl != null ? convertToEmbedUrl(trailerUrl) : "");

            String apiStatus = (String) movieData.get("status");
            String mappedStatus = mapStatus(apiStatus); // Ánh xạ status từ API
            movie.setStatus(mappedStatus);

            Integer year = (Integer) movieData.get("year");
            movie.setReleaseYear(year != null ? year : 0);

            String episodeTotal = (String) movieData.get("episode_total");
            if (episodeTotal != null) {
                // Sử dụng regex để lấy số từ chuỗi
                Matcher matcher = Pattern.compile("\\d+").matcher(episodeTotal);
                if (matcher.find()) {
                    // Loại bỏ số 0 ở đầu chuỗi số (nếu có)
                    String episodeNumber = matcher.group().replaceFirst("^0+(?!$)", ""); // Xóa số 0 đầu chuỗi, trừ trường hợp toàn bộ là "0"
                    movie.setTotalEpisodes(episodeNumber); // Gán số đã xử lý vào totalEpisodes
                } else {
                    movie.setTotalEpisodes("??"); // Giá trị mặc định nếu episode_total là null
                }
            }


            if (episodeTotal != null) {
                // Loại bỏ các số 0 ở đầu chuỗi và kiểm tra nếu bằng 1
                String cleanedEpisodeTotal = episodeTotal.replaceFirst("^0+(?!$)", ""); // Xóa số 0 đầu chuỗi
                movie.setIsSeries(!cleanedEpisodeTotal.equals("1")); // Nếu số tập là 1 thì isSeries = false
            } else {
                movie.setIsSeries(false); // Giá trị mặc định nếu không có số tập
            }



            String time = (String) movieData.get("time");
            if (time != null) {
                Matcher timeMatcher = Pattern.compile("\\d+").matcher(time);
                if (timeMatcher.find()) {
                    movie.setDuration(Integer.parseInt(timeMatcher.group()));
                }
            }
            List<String> directors = (List<String>) movieData.get("director");
            movie.setDirector(directors != null ? String.join(", ", directors) : "Đang cập nhật");

            String type = (String) movieData.get("type");


            List<Genre> movieGenres = new ArrayList<>();

            if ("hoathinh".equalsIgnoreCase(type)) {
                Genre animeGenre = genreRepository.findByGenreName("Hoạt Hình");
                if (animeGenre != null) {
                    movieGenres.add(animeGenre);
                }
            }

            if ("tvshows".equalsIgnoreCase(type)) {
                Genre tvShowsGenre = genreRepository.findByGenreName("TV Shows");
                if (tvShowsGenre != null) {
                    movieGenres.add(tvShowsGenre);
                }
            }


            List<Map<String, String>> categories = (List<Map<String, String>>) movieData.get("category");
            if (categories != null) {

                for (Map<String, String> category : categories) {
                    String genreName = category.get("name");
                    if (genreName != null) {
                        // Truy vấn thể loại từ cơ sở dữ liệu
                        Genre genre = genreRepository.findByGenreName(genreName);
                        if (genre != null) {
                            movieGenres.add(genre);
                        } else {
                            System.out.println("Thể loại không tìm thấy: " + genreName);
                        }
                    }
                }

                // Gắn danh sách thể loại vào phim
                movie.setGenres(movieGenres);
            }
            List<Map<String, String>> countries = (List<Map<String, String>>) movieData.get("country");
            if (countries != null && !countries.isEmpty()) {
                String countryName = countries.get(0).get("name"); // Lấy tên quốc gia đầu tiên
                Country country = countryRepository.findByCountryName(countryName);
                if (country == null) {
                    // Nếu không tìm thấy, tạo mới
                    country = new Country();
                    country.setCountryName(countryName);
                    country = countryRepository.save(country); // Lưu quốc gia mới vào cơ sở dữ liệu
                    System.out.println("Thêm quốc gia mới: " + countryName);
                }
                movie.setCountry(country); // Gán quốc gia vào phim
            } else {
                System.out.println("Không có thông tin quốc gia trong API.");
            }
            List<String> actorsFromApi = (List<String>) movieData.get("actor");
            if (actorsFromApi != null && !actorsFromApi.isEmpty()) {
                List<Actor> movieActors = new ArrayList<>();
                for (String actorName : actorsFromApi) {
                    Actor actor = actorRepository.findByActorName(actorName);
                    if (actor == null) {
                        // Nếu diễn viên không tồn tại, tạo mới
                        actor = new Actor();
                        actor.setActorName(actorName);
                        actor = actorRepository.save(actor); // Lưu diễn viên mới vào cơ sở dữ liệu
                        System.out.println("Thêm diễn viên mới: " + actorName);
                    }
                    movieActors.add(actor);
                }
                movie.setActors(movieActors); // Gắn danh sách diễn viên vào phim
            } else {
                System.out.println("Không có thông tin diễn viên trong API.");
            }
            // Lưu thông tin phim vào cơ sở dữ liệu
            movie.setUpdatedAt(new Date());
            movieRepository.save(movie);
            System.out.println("Thêm phim mới: " + movie.getTitle());

//             Cập nhật các tập phim (nếu có)
            if (responseBody.containsKey("episodes")) {
                List<Map<String, Object>> episodesData = (List<Map<String, Object>>) responseBody.get("episodes");

                for (Map<String, Object> episode : episodesData) {
                    List<Map<String, String>> serverData = (List<Map<String, String>>) episode.get("server_data");

                    for (Map<String, String> server : serverData) {
                        String episodeNumber = server.get("name"); // Lấy số tập
                        if (episodeNumber == null || episodeNumber.isBlank()) {
                            System.out.println("Bỏ qua tập rỗng hoặc không hợp lệ.");
                            continue;
                        }

                        // Xử lý loại bỏ số `0` ở đầu
                        episodeNumber = episodeNumber.replaceFirst("^0+", "");

                        // Kiểm tra xem tập phim đã tồn tại hay chưa
                        boolean exists = episodeRepository.existsByMovieAndEpisodeNumber(movie, episodeNumber);
                        if (!exists) {
                            Episode newEpisode = new Episode();
                            newEpisode.setMovie(movie);
                            newEpisode.setEpisodeNumber(episodeNumber);
                            newEpisode.setTitle(episodeNumber);
                            newEpisode.setVideoUrl(server.get("link_embed"));

                            episodeRepository.save(newEpisode);
                            System.out.println("Thêm tập mới: " + episodeNumber + " cho phim: " + movie.getTitle());
                        }
                    }
                }
            }
        } else {
            throw new RuntimeException("Không thể lấy thông tin từ API.");
        }
    }

    private String convertToEmbedUrl(String trailerUrl) {
        if (trailerUrl.contains("youtube.com") || trailerUrl.contains("youtu.be")) {
            return trailerUrl.replace("watch?v=", "embed/");
        }
        return trailerUrl; // Giữ nguyên nếu không phải YouTube
    }


}
