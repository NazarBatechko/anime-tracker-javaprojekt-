package com.example.movies.controller;

import com.example.movies.service.ExternalDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class ExternalDataController {

    private final ExternalDataService externalDataService;

    @GetMapping("/api/fetch/api")
    public String fetchFromApi() {
        try {
            externalDataService.fetchMoviesFromApi();
            return "Fetched movies from API successfully";
        } catch (IOException e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    @GetMapping("/api/fetch/html")
    public String fetchFromHtml(@RequestParam String url) {
        try {
            externalDataService.fetchMoviesFromHtml(url);
            return "Fetched movies from HTML successfully";
        } catch (IOException e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}