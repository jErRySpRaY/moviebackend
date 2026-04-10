package com.example.moviebackend.logic;

public class Movie {
    private int id;
    private String title;
    private String genre;
    private String plot;
    private int year;
    private String runtime;

    private String rating;
	private String poster;




    public Movie(int id, String title, int year, String genre, String plot, String runtime, String subgenre, String rating, String poster) {
        this.id = id;
        this.title = title;
        this.genre = genre;
        this.plot = plot;
        this.year = year;
        this.runtime = runtime;

        this.rating = rating;
		this.poster = poster;
    }

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getGenre() { return genre; }
    public String getPlot() { return plot; }
    public int getYear() { return year; }
    public String getRuntime() { return runtime; }
    public String getRating() { return rating; }
	public String getPoster() {
    return poster;
}

    @Override
    public String toString() { return title; }
}
