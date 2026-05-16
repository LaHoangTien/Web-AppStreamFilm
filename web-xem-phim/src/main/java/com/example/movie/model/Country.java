package com.example.movie.model;

import jakarta.persistence.*;
import lombok.*;



@Setter
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "countries")
public class Country {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long countryId;

    @Column(nullable = false, unique = true)
    private String countryName;

    private long movieCount; // Thêm thuộc tính này nếu chưa có
}
