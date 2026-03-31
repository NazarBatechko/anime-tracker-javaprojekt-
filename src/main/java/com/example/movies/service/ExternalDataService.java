package com.example.movies.service;

import com.example.movies.model.*;
import com.example.movies.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;

@Service
public class ExternalDataService {

    private final MovieRepository movieRepository;
    private final ActorRepository actorRepository;
    private final GenreRepository genreRepository;

    public ExternalDataService(MovieRepository movieRepository,
                               ActorRepository actorRepository,
                               GenreRepository genreRepository) {
        this.movieRepository = movieRepository;
        this.actorRepository = actorRepository;
        this.genreRepository = genreRepository;
    }

    // --- Збір трендових Аніме через API ---
    public void fetchMoviesFromApi() throws IOException {
        // Змінюємо URL з movies на anime
        String urlString = "https://data.simkl.in/discover/trending/anime/today_100.json";

        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        connection.setConnectTimeout(10000);

        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException("Помилка запиту API: " + connection.getResponseCode());
        }

        try (var inputStream = connection.getInputStream()) {
            JsonNode root = new ObjectMapper().readTree(inputStream);

            if (root.isArray()) {
                for (JsonNode node : root) {
                    String title = node.path("title").asText(null);
                    if (title == null || title.isBlank()) continue; // Пропускаємо порожні записи

                    Movie anime = new Movie();
                    anime.setTitle(title);
                    anime.setYear(node.path("year").asText("N/A"));
                    anime.setOverview(node.path("overview").asText("Опис аніме відсутній..."));
                    anime.setPosterUrl(node.path("images").path("poster").asText(null));

                    // Зберігаємо акторів озвучки та жанри
                    anime.setActors(parseActors(node));
                    anime.setGenres(parseGenres(node));

                    movieRepository.save(anime);
                }
            }
        } finally {
            connection.disconnect();
        }
    }

    // --- Парсинг Аніме з HTML сторінки (наприклад, MyAnimeList або схожі) ---
    public void fetchMoviesFromHtml(String htmlUrl) throws IOException {
        try {
            var doc = Jsoup.connect(htmlUrl)
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();

            // Змінюємо селектори під стандартні списки аніме
            // Зазвичай аніме йдуть у блоках div або li
            doc.select("div.anime-card, .title").forEach(element -> {
                String title = element.text();
                if (!title.isEmpty()) {
                    Movie anime = new Movie();
                    anime.setTitle(title);
                    anime.setOverview("Парсинг з HTML: " + htmlUrl);
                    anime.setActors(new HashSet<>());
                    anime.setGenres(new HashSet<>());
                    movieRepository.save(anime);
                }
            });
        } catch (Exception e) {
            throw new IOException("Помилка парсингу HTML: " + e.getMessage());
        }
    }

    // --- Обробка персонажів/акторів (без змін, працює універсально) ---
    private HashSet<Actor> parseActors(JsonNode node) {
        HashSet<Actor> actorsSet = new HashSet<>();
        if (node.has("actors")) {
            for (JsonNode a : node.get("actors")) {
                String name = a.path("name").asText(null);
                if (name != null && !name.isBlank()) {
                    Actor actor = actorRepository.findByName(name)
                            .orElseGet(() -> actorRepository.save(new Actor(null, name, null)));
                    actorsSet.add(actor);
                }
            }
        }
        return actorsSet;
    }

    // --- Обробка жанрів ---
    private HashSet<Genre> parseGenres(JsonNode node) {
        HashSet<Genre> genresSet = new HashSet<>();
        if (node.has("genres")) {
            for (JsonNode g : node.get("genres")) {
                String name = g.asText();
                if (name != null && !name.isBlank()) {
                    Genre genre = genreRepository.findByName(name)
                            .orElseGet(() -> genreRepository.save(new Genre(null, name, null)));
                    genresSet.add(genre);
                }
            }
        }
        return genresSet;
    }
}