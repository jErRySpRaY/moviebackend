package com.example.moviebackend.logic;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;

import org.json.JSONObject;

public class PosterFetcher {

    private static final String API_KEY = "d8fdfcbf861621a8919a3f58467d705a";

    public static String fetchPoster(String title, int year) {
        try {
            // Encode title for URL
            String query = title.replace(" ", "%20");

            // TMDB search endpoint
            String url = "https://api.themoviedb.org/3/search/movie"
                    + "?api_key=" + API_KEY
                    + "&query=" + query
                    + "&year=" + year;

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            JSONObject json = new JSONObject(response.body());

            // No results?
            if (!json.has("results") || json.getJSONArray("results").isEmpty()) {
                return null;
            }

            JSONObject movie = json.getJSONArray("results").getJSONObject(0);

            // No poster?
            if (!movie.has("poster_path") || movie.isNull("poster_path")) {
                return null;
            }

            String posterPath = movie.getString("poster_path");

            // Build full TMDB image URL
            return "https://image.tmdb.org/t/p/w500" + posterPath;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}