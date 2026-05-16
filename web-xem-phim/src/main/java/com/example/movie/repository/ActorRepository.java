package com.example.movie.repository;

import com.example.movie.model.Actor;
import com.example.movie.model.Genre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ActorRepository extends JpaRepository<Actor, Long> {
    boolean existsByActorName(String actorName);
    Page<Actor> findByActorNameContainingIgnoreCase(String actorName, Pageable pageable);
//    Optional<Actor> findByActorName(String actorName);
    Actor findByActorName(String actorName);
}
