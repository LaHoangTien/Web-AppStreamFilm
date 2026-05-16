package com.example.movie.elasticsearch;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenreDocument {


    private Long genreId;


    private String genreName; // Tên quốc gia
}
