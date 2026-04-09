package com.example.moviebackend.logic;


import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;
import java.util.function.BiConsumer;


public class MySQLConnector {
	
	public static void main(String[] args) {
    testConnection();

    // Step 1: Check what MySQL user is being used
    try (Connection conn = getConnection();
         PreparedStatement stmt = conn.prepareStatement("SELECT CURRENT_USER()");
         ResultSet rs = stmt.executeQuery()) {

        if (rs.next()) {
            System.out.println("MySQL reports current user: " + rs.getString(1));
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }
}



    private static Connection connection = null;
	
	public static void testConnection() {
    try (Connection conn = getConnection()) {
        if (conn != null && !conn.isClosed()) {
            System.out.println("Connection successful!");
        } else {
            System.out.println("Connection failed!");
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
}

    public static Connection getConnection() throws SQLException {
    try {
        // Load the MySQL JDBC driver
        Class.forName("com.mysql.cj.jdbc.Driver");
    } catch (ClassNotFoundException e) {
        throw new SQLException("MySQL Driver not found", e);
    }

    String url = "jdbc:mysql://localhost:3306/moviedb";




    String dbURL = "jdbc:mysql://localhost:3306/moviedb?useSSL=false&serverTimezone=UTC";
	String dbUser = "root";  // or the 'movieuser' if you created a new user
	String dbPassword = "@lv!n_$!m0n_The0d0re_1987";  // Replace with the correct password

Connection conn = DriverManager.getConnection(dbURL, dbUser, dbPassword);


    System.out.println("Connecting with user: " + dbUser);  // debug print

    return DriverManager.getConnection(dbURL, dbUser, dbPassword);
}


	
	public static Optional<String> getSequelForMovie(String currentMovieId) throws SQLException {
    String sql = "SELECT id FROM movies WHERE sequel_to = ? LIMIT 1";
    try (Connection conn = getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, currentMovieId);
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return Optional.of(rs.getString("id"));
            }
        }
    }
    return Optional.empty();
}


public static Optional<String> getOneUnseenMovie(String genre, String subgenre, List<String> ratingFilters,
                                                String runtime, String era, Set<String> seenIds) throws SQLException {
    System.out.println("ratingFilters param list: " + ratingFilters);

    StringBuilder query = new StringBuilder("SELECT id FROM movies WHERE 1=1");
    List<Object> params = new ArrayList<>();
	
	String normalizedRuntime = switch (runtime) {
    case "Very Short (< 85 min)" -> "very short";
    case "Short (85-99 min)" -> "short";
    case "Standard (100-119 min)" -> "standard";
    case "Long (120-149 min)" -> "long";
    case "Very Long (150+ min)" -> "very long";
    default -> "all";
};

/*String normalizedEra = switch (era) {
    case "Silent (Pre-1929)" -> "silent";
    case "Pre-Code (1929-1933)" -> "pre code";
    case "Golden Age (1934-1947)" -> "golden age";
    case "Post-Studio (1948-1966)" -> "post studio";
    case "New Hollywood (1967-1981)" -> "new hollywood";
    case "Blockbuster (1982-1998)" -> "blockbuster";
    case "Digital (1999-2012)" -> "digital";
    case "Streaming (2013+)" -> "streaming";
    default -> "all";
};*/


    if (genre != null && !genre.isBlank()) {
        query.append(" AND genre = ?");
        params.add(genre.trim());
    }

    if (subgenre != null && !subgenre.isBlank()) {
        query.append(" AND subgenre = ?");
        params.add(subgenre.trim());
    }

    if (ratingFilters != null && !ratingFilters.isEmpty()) {
        query.append(" AND rating IN (")
             .append(String.join(",", Collections.nCopies(ratingFilters.size(), "?")))
             .append(")");
        params.addAll(ratingFilters);
    }
	

    // 1. Convert runtime string to numeric bounds
    int minRuntime, maxRuntime;
    switch (normalizedRuntime) {
        case "very short" -> {
            minRuntime = 0;
            maxRuntime = 84;  // less than 85 minutes
        }
        case "short" -> {
            minRuntime = 85;
            maxRuntime = 99;  // 85-99 minutes
        }
        case "standard" -> {
            minRuntime = 100;
            maxRuntime = 119;  // 100-119 minutes
        }
		case "long" -> {
			minRuntime = 120;
			maxRuntime = 149;
		}
		case "very long" -> {
			minRuntime = 150;
			maxRuntime = 9999;
		}
        default -> {
            minRuntime = 0;
            maxRuntime = 9999;  // no filter (all runtimes)
        }
    }

    if (!(minRuntime == 0 && maxRuntime == 9999)) {
        query.append(" AND CAST(SUBSTRING_INDEX(runtime, ' ', 1) AS UNSIGNED) BETWEEN ? AND ?");
        params.add(minRuntime);
        params.add(maxRuntime);
    }

    // 2. Convert era string to numeric year bounds
    // 2. Convert era string to numeric year bounds (based on user-friendly labels)
int minYear = 0, maxYear = 9999;

String normalizedEra = era != null ? era.toLowerCase().split(" ")[0].replace("-", "").trim() : "";

switch (normalizedEra) {
    case "silent" -> {
        minYear = 1900;
        maxYear = 1928;
    }
    case "precode" -> {
        minYear = 1929;
        maxYear = 1933;
    }
    case "golden" -> {
        minYear = 1934;
        maxYear = 1947;
    }
    case "poststudio" -> {
        minYear = 1948;
        maxYear = 1966;
    }
    case "new" -> {
        minYear = 1967;
        maxYear = 1981;
    }
    case "blockbuster" -> {
        minYear = 1982;
        maxYear = 1998;
    }
    case "digital" -> {
        minYear = 1999;
        maxYear = 2012;
    }
    case "streaming" -> {
        minYear = 2013;
        maxYear = 9999;
    }
    case "all" -> {
        minYear = 0;
        maxYear = 9999;
    }
    default -> {
        minYear = 0;
        maxYear = 9999;
    }
}


    

    if (!(minYear == 0 && maxYear == 9999)) {
        query.append(" AND year >= ? AND year <= ?");
        params.add(minYear);
        params.add(maxYear);
    }

    // Exclude already seen titles (case-insensitive)
    if (seenIds != null && !seenIds.isEmpty()) {
    query.append(" AND id NOT IN (")
         .append(String.join(",", Collections.nCopies(seenIds.size(), "?")))
         .append(")");
    for (String id : seenIds) {
        params.add(Integer.parseInt(id));  // assuming seenIds contains IDs now
    }
}



    // Only select movies with no prequel
    query.append(" AND (sequel_to IS NULL OR sequel_to = 'NULL')");

    // Order by year ASC, then randomize within that
    query.append(" ORDER BY RAND() LIMIT 1");

    System.out.println("Executing query: " + query);
    System.out.println("With params: " + params);

    try (Connection conn = getConnection();
         PreparedStatement stmt = conn.prepareStatement(query.toString())) {

        for (int i = 0; i < params.size(); i++) {
            stmt.setObject(i + 1, params.get(i));
        }

        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return Optional.of(rs.getString("id"));
            }
        }
    }

    return Optional.empty();
}
								





    public static List<String> getUnseenSequelChain(String initialMovieId, Set<String> seenIds) throws SQLException {
    if (initialMovieId == null || initialMovieId.trim().isEmpty()) {
        return Collections.emptyList();
    }

    List<String> unseenSequels = new ArrayList<>();
    String currentMovieId = initialMovieId;
    while (true) {
        String nextId = getNextUnseenSequel(currentMovieId, seenIds);
        if (nextId == null) {
            break;
        }
        unseenSequels.add(nextId);
        seenIds.add(nextId);
        currentMovieId = nextId;
    }
    return unseenSequels;
}


public static Optional<Movie> getRandomMovieByGenre(String genre) {
    String sql = "SELECT * FROM movies WHERE sequel_to IS NULL AND LOWER(genre) LIKE ? ORDER BY RAND() LIMIT 1";

    try (Connection conn = getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setString(1, "%" + genre.toLowerCase() + "%");

        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
    int id = rs.getInt("id");
    String title = rs.getString("title");
    int year = rs.getInt("year");
    genre = rs.getString("genre");
    String plot = rs.getString("plot");
    String runtime = rs.getString("runtime");
    String subgenre = rs.getString("subgenre");
    String rating = rs.getString("rating");

    String poster = PosterFetcher.fetchPoster(title, year);

    return Optional.of(
    new Movie(
        id,
        title,
        year,
        genre,
        plot,
        runtime,
        subgenre,
        rating,
        poster
    )
);
}
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return Optional.empty();
}






    public static String getNextUnseenSequel(String currentMovieId, Set<String> seenIds) throws SQLException {
    String sql = "SELECT id FROM movies WHERE sequel_to = ?";
    try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
        stmt.setString(1, currentMovieId);
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String candidateId = rs.getString("id");
                if (!seenIds.contains(candidateId)) {
                    return candidateId;
                }
            }
        }
    }
    return null;
}



    public static void printMovieDetails(String title) throws SQLException {
        String sql = "SELECT title, year, runtime, director, rating FROM movies WHERE title = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, title);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("\n--- Movie Details ---");
                    System.out.println("Title: " + rs.getString("title"));
                    System.out.println("Year: " + rs.getInt("year"));
                    System.out.println("Runtime: " + rs.getString("runtime"));
                    System.out.println("Director: " + rs.getString("director"));
                    System.out.println("Rating: " + rs.getString("rating"));
                    System.out.println("----------------------\n");
                } else {
                    System.out.println("Movie not found: " + title);
                }
            }
        }
    }
	
	public static int getMovieIdByTitle(String title) throws SQLException {
    String sql = "SELECT id FROM movies WHERE title = ?";
    try (Connection conn = getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, title);
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("id");
            } else {
                throw new IllegalArgumentException("Movie not found: " + title);
            }
        }
    }
}


	public static Optional<String> getTitleById(String movieId) throws SQLException {
    String sql = "SELECT title FROM movies WHERE id = ?";
    try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
        stmt.setString(1, movieId);
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return Optional.of(rs.getString("title"));
            }
        }
    }
    return Optional.empty();
}

	public static Optional<Movie> getMovieByIdOptional(String movieId) {
    String sql = "SELECT id, title, year, genre, plot, runtime, subgenre, rating FROM movies WHERE id = ?";
    try (Connection conn = MySQLConnector.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setString(1, movieId);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            int id = rs.getInt("id");
            String title = rs.getString("title");
			int year = rs.getInt("year");
            String genre = rs.getString("genre");
            String plot = rs.getString("plot");
			String runtime = rs.getString("runtime");
			String subgenre = rs.getString("subgenre");
			String rating = rs.getString("rating");

            String poster = PosterFetcher.fetchPoster(title, year);

			Movie movie = new Movie(id, title, year, genre, plot, runtime, subgenre, rating, poster);
            return Optional.of(movie);
        } else {
            return Optional.empty(); // No movie found with that ID
        }

    } catch (SQLException e) {
        e.printStackTrace();
        return Optional.empty();
    }
}



    public static int[] getYearRangeForEra(String era) {
        if (era == null) return null;
        return switch (era.toLowerCase()) {
            case "silent" -> new int[]{0, 1928};
            case "pre code" -> new int[]{1929, 1933};
            case "golden age" -> new int[]{1934, 1947};
            case "post studio" -> new int[]{1948, 1966};
            case "new hollywood" -> new int[]{1967, 1981};
            case "blockbuster" -> new int[]{1982, 1998};
            case "digital" -> new int[]{1999, 2012};
            case "streaming" -> new int[]{2013, Integer.MAX_VALUE};
            default -> null;
        };
    }
	
	private static List<String> formatParamsForDisplay(String genre, String subgenre, List<String> ratingFilters, String runtime, String era) {
    List<String> display = new ArrayList<>();
    if (genre != null && !genre.isBlank()) display.add(genre);
    if (subgenre != null && !subgenre.isBlank()) display.add(subgenre);
    if (ratingFilters != null && !ratingFilters.isEmpty()) {
    display.addAll(ratingFilters);
}

    if (runtime != null && !runtime.isBlank()) display.add(runtime);
    if (era != null && !era.isBlank()) display.add(era);
    return display;
}
}






	









    














    







