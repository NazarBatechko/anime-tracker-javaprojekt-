package com.example.movies.controller;

import com.example.movies.model.Movie;
import com.example.movies.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieRepository movieRepository;

    @GetMapping
    public List<Movie> all() {
        return movieRepository.findAll();
    }

    @GetMapping("/{id}")
    public Movie get(@PathVariable Long id) {
        return movieRepository.findById(id).orElse(null);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        movieRepository.deleteById(id);
    }
}