package com.example.movie.elasticsearch;

import org.springframework.data.elasticsearch.annotations.Query;

import org.springframework.stereotype.Repository;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import java.util.List;

@Repository
public interface MovieDocumentRepository extends ElasticsearchRepository<MovieDocument, Long> {
    @Query("{\"bool\": {"
            + "  \"should\": ["
            + "    { \"match_phrase\": { \"title\": { \"query\": \"?0\", \"boost\": 5 } } },"
            + "    { \"match_phrase\": { \"name\": { \"query\": \"?0\", \"boost\": 4 } } },"
            + "    { \"match_phrase\": { \"slug\": { \"query\": \"?0\", \"boost\": 4 } } },"
            + "    { \"multi_match\": { "
            + "        \"query\": \"?0\", "
            + "        \"fields\": [\"title^5\", \"name^4\", \"slug^4\", \"description^2\", \"director^2\", "
            + "                   \"country.countryName^2\", \"genres.genreName^2\", \"actors.actorName^2\"], "
            + "        \"fuzziness\": \"AUTO\", "
            + "        \"prefix_length\": 1 "
            + "      }"
            + "    }"
            + "  ],"
            + "  \"minimum_should_match\": 1"
            + "}},"
            + "\"sort\": ["
            + "  { \"_score\": { \"order\": \"desc\" } }"
            + "]")
    List<MovieDocument> searchMoviesByTitleSorted(String query);
}
