package com.example.movie.elasticsearch;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "movies") // Index name in Elasticsearch
public class MovieDocument {

    @Id
    private Long movieId;

    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Text)
    private String name; // Tiêu đề phim

    @Field(type = FieldType.Text)
    private String slug;

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Integer)
    private Integer releaseYear;

    @Field(type = FieldType.Text)
    private String director;

    @Field(type = FieldType.Object)
    private CountryDocument country; // Quốc gia

    @Field(type = FieldType.Nested)
    private List<GenreDocument> genres; // Thể loại

    @Field(type = FieldType.Nested)
    private List<ActorDocument> actors; // Diễn viên

    @Field(type = FieldType.Boolean)
    private Boolean isSeries;

    // Additional fields as required for searching
}
