package com.example.movie.service;

import com.example.movie.elasticsearch.*;
import com.example.movie.model.Movie;
import com.example.movie.repository.MovieRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MovieSynchronizationService {

    private final MovieRepository movieRepository;
    private final MovieDocumentRepository movieDocumentRepository;

    @Autowired
    public MovieSynchronizationService(MovieRepository movieRepository, MovieDocumentRepository movieDocumentRepository) {
        this.movieRepository = movieRepository;
        this.movieDocumentRepository = movieDocumentRepository;
    }

    // Phương thức đồng bộ toàn bộ phim từ cơ sở dữ liệu lên Elasticsearch
    public void synchronizeMovieBySlug(String slug) {
        // Lấy phim từ database theo slug
        Movie movie = movieRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phim với slug: " + slug));

        // Map Movie sang MovieDocument
        MovieDocument movieDocument = MovieDocument.builder()
                .movieId(movie.getMovieId())
                .title(movie.getTitle())
                .name(movie.getName())
                .slug(movie.getSlug())
                .description(movie.getDescription())
                .releaseYear(movie.getReleaseYear())
                .director(movie.getDirector())
                .isSeries(movie.getIsSeries())

                // Map Country
                .country(movie.getCountry() != null ?
                        CountryDocument.builder()
                                .countryId(movie.getCountry().getCountryId())
                                .countryName(movie.getCountry().getCountryName())
                                .build()
                        : null)

                // Map Genres
                .genres(movie.getGenres() != null ?
                        movie.getGenres().stream()
                                .filter(genre -> genre != null)
                                .map(genre -> GenreDocument.builder()
                                        .genreId(genre.getGenreId())
                                        .genreName(genre.getGenreName())
                                        .build())
                                .collect(Collectors.toList())
                        : Collections.emptyList())

                // Map Actors
                .actors(movie.getActors() != null ?
                        movie.getActors().stream()
                                .filter(actor -> actor != null)
                                .map(actor -> ActorDocument.builder()
                                        .actorId(actor.getActorId())
                                        .actorName(actor.getActorName())
                                        .build())
                                .collect(Collectors.toList())
                        : Collections.emptyList())
                .build();

        // Lưu MovieDocument vào Elasticsearch
        movieDocumentRepository.save(movieDocument);
    }

    public void synchronizeAllMovies() {
        List<Movie> movies = movieRepository.findAll();

        List<MovieDocument> movieDocuments = movies.stream().map(movie -> MovieDocument.builder()
                .movieId(movie.getMovieId())
                .title(movie.getTitle())
                .name(movie.getName())
                .slug(movie.getSlug())
                .description(movie.getDescription())
                .releaseYear(movie.getReleaseYear())
                .director(movie.getDirector())
                .isSeries(movie.getIsSeries())

                .country(movie.getCountry() != null ?
                        CountryDocument.builder()
                                .countryId(movie.getCountry().getCountryId())
                                .countryName(movie.getCountry().getCountryName())
                                .build()
                        : null)

                .genres(movie.getGenres() != null ?
                        movie.getGenres().stream()
                                .filter(genre -> genre != null)
                                .map(genre -> GenreDocument.builder()
                                        .genreId(genre.getGenreId())
                                        .genreName(genre.getGenreName())
                                        .build())
                                .collect(Collectors.toList())
                        : Collections.emptyList())

                .actors(movie.getActors() != null ?
                        movie.getActors().stream()
                                .filter(actor -> actor != null)
                                .map(actor -> ActorDocument.builder()
                                        .actorId(actor.getActorId())
                                        .actorName(actor.getActorName())
                                        .build())
                                .collect(Collectors.toList())
                        : Collections.emptyList())
                .build()).collect(Collectors.toList());

        movieDocumentRepository.saveAll(movieDocuments);
    }
    // Gọi phương thức đồng bộ toàn bộ dữ liệu khi khởi động ứng dụng
    @PostConstruct
    public void init() {
        try {
            System.out.println("🚀 Bắt đầu đồng bộ dữ liệu phim...");
            synchronizeAllMovies();
            System.out.println("✅ Đồng bộ xong!");
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi đồng bộ phim: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
