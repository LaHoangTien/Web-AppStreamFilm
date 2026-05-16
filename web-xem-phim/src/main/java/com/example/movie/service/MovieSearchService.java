package com.example.movie.service;

import com.example.movie.elasticsearch.MovieDocument;
import com.example.movie.elasticsearch.MovieDocumentRepository;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class MovieSearchService {


    private final MovieDocumentRepository movieDocumentRepository;

    @Autowired
    public MovieSearchService(MovieDocumentRepository movieDocumentRepository) {
        this.movieDocumentRepository = movieDocumentRepository;
    }

    public List<MovieDocument> searchMoviesFuzzy(String query) {
        return movieDocumentRepository.searchMoviesByTitleSorted(query);
    }

}
