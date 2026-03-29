package com.example.movies.dto;

import lombok.*;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieDTO {
    private Long id;
    private String title;
    private String year;
    private String overview;
    private String posterUrl;
    private Set<String> genres;
    private Set<String> actors;
}