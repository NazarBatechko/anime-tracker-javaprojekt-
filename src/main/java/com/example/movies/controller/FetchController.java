//package com.example.library.controller;
//
//import com.example.library.service.ExternalDataService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.io.IOException;
//
//@RestController
//@RequiredArgsConstructor
//public class FetchController {
//
//    private final ExternalDataService externalDataService;
//
//    @GetMapping("/api/fetch/movies")
//    public String fetchMovies() {
//        try {
//            externalDataService.fetchMoviesFromSimkl();
//            return "Movies fetched";
//        } catch (IOException e) {
//            return "Error: " + e.getMessage();
//        }
//    }
//}