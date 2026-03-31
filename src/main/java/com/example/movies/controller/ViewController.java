package com.example.movies.controller;

import com.example.movies.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class ViewController {

    private final MovieRepository movieRepository;

    @GetMapping("/")
    public String index(Model model) {
        // Ми дістаємо всі записи з таблиці MOVIE
        // і передаємо їх у HTML-шаблон під ім'ям "movies"
        model.addAttribute("movies", movieRepository.findAll());

        // Повертаємо назву файлу src/main/resources/templates/index.html
        return "index";
    }
}

