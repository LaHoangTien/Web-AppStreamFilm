package com.example.movie.repository;

import com.example.movie.model.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {
    List<Country> findByCountryIdIn(List<Long> countryIds);
    Country findByCountryName(String countryName);
}
