package com.example.moviebackend;

import org.springframework.web.bind.annotation.*;
import java.sql.SQLException;
import java.util.*;

import com.example.moviebackend.logic.MySQLConnector;
import com.example.moviebackend.logic.Movie;

@RestController
@CrossOrigin(origins = {
    "http://localhost:5173",
    "https://moviebackend-production-b190.up.railway.app"
})
public class MovieController {

    @GetMapping("/pickMovie")
    public Object pickMovie(
            @RequestParam String genre,
            @RequestParam String subgenre,
            @RequestParam String runtime,
            @RequestParam String era,
            @RequestParam String rating
    ) {
        try {
            List<String> ratingFilters = new ArrayList<>();
            if (!rating.equalsIgnoreCase("Any")) {
                ratingFilters.add(rating);
            }

            Set<String> seenIds = new HashSet<>();

            // Step 1: Pick a movie ID
            Optional<String> result = MySQLConnector.getOneUnseenMovie(
                    genre,
                    subgenre,
                    ratingFilters,
                    runtime,
                    era,
                    seenIds
            );

            if (result.isEmpty()) {
                return Map.of("error", "No movie found with these filters.");
            }

            String movieId = result.get();

            // Step 2: Fetch full movie details
            Optional<Movie> movieOpt = MySQLConnector.getMovieByIdOptional(movieId);

            if (movieOpt.isEmpty()) {
                return Map.of("error", "Movie not found in database.");
            }

            return movieOpt.get();  // Spring Boot auto-converts Movie → JSON

        } catch (SQLException e) {
            e.printStackTrace();
            return Map.of("error", e.getMessage());
        }
    }
}