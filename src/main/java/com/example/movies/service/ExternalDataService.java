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

    // API-ключ потрібен тільки для захищених ендпоінтів api.simkl.com
    // Для публічних trending-файлів він не потрібен
    private static final String SIMKL_API_KEY = "d5bbb687eafb0ad0565de52972ccbee56338acb698946b55a579fca01a87d92a";

    public ExternalDataService(MovieRepository movieRepository,
                               ActorRepository actorRepository,
                               GenreRepository genreRepository) {
        this.movieRepository = movieRepository;
        this.actorRepository = actorRepository;
        this.genreRepository = genreRepository;
    }

    // --- Фетч trending movies (виправлений URL) ---
    public void fetchMoviesFromApi() throws IOException {
        // Правильний публічний URL (без потреби в API-ключі)
        String urlString = "https://data.simkl.in/discover/trending/movies/today_100.json";

        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(15000);

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("Failed to fetch trending movies. HTTP code: " + responseCode);
        }

        try (var inputStream = connection.getInputStream()) {
            JsonNode root = new ObjectMapper().readTree(inputStream);

            if (root.isArray()) {
                for (JsonNode node : root) {
                    Movie movie = new Movie();
                    movie.setTitle(node.path("title").asText(null));
                    movie.setYear(node.path("year").asText(null));
                    movie.setOverview(node.path("overview").asText(null));
                    movie.setPosterUrl(node.path("images").path("poster").asText(null));

                    // Якщо в JSON є поля actors/genres — парсинг працюватиме
                    movie.setActors(parseActors(node));
                    movie.setGenres(parseGenres(node));

                    movieRepository.save(movie);
                }
            } else {
                System.out.println("Response is not an array: " + root);
            }
        } finally {
            connection.disconnect();
        }
    }

    // --- Парс HTML сторінки (виправлений вивід помилки) ---
    public void fetchMoviesFromHtml(String htmlUrl) throws IOException {
        try {
            var doc = Jsoup.connect(htmlUrl)
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();

            doc.select("div.movie").forEach(element -> {
                Movie movie = new Movie();
                movie.setTitle(element.select("h2.title").text());
                movie.setOverview(element.select("p.overview").text());

                movie.setActors(new HashSet<>());
                movie.setGenres(new HashSet<>());

                movieRepository.save(movie);
            });
        } catch (org.jsoup.HttpStatusException e) {
            throw new IOException("Failed to fetch HTML. Status=" + e.getStatusCode() + ", URL=" + htmlUrl, e);
        }
    }

    // --- Допоміжні методи (без змін) ---
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