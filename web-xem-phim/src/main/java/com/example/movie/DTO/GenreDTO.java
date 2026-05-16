package com.example.movie.DTO;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GenreDTO {
    private Long genreId;
    private String genreName;
    public GenreDTO(Long genreId, String genreName) {
        this.genreId = genreId;
        this.genreName = genreName;
    }
}
