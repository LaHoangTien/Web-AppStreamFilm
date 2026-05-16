package com.example.movie.DTO;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
@Setter
@Getter
public class MovieDTO {
    private Long movieId;
    private String title;
    private String name;
    private String slug;
    private String description;
    private Integer releaseYear;
    private String director;
    private boolean isSeries;
    private Integer duration;
    private String posterUrl;
    private String backgroundUrl;
    private java.util.Date updatedAt;
    private List<GenreDTO> genres;
    public MovieDTO(Long movieId, String title, String name, String slug, String description,
                    Integer releaseYear, String director, boolean isSeries, Integer duration,
                    String posterUrl, String backgroundUrl, java.util.Date updatedAt, List<GenreDTO> genres) {
        this.movieId = movieId;
        this.title = title;
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.releaseYear = releaseYear;
        this.director = director;
        this.isSeries = isSeries;
        this.duration = duration;
        this.posterUrl = posterUrl;
        this.backgroundUrl = backgroundUrl;
        this.updatedAt = updatedAt;
        this.genres = genres;
    }
}
