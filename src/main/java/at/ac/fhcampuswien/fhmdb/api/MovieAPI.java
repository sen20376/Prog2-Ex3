package at.ac.fhcampuswien.fhmdb.api;

import at.ac.fhcampuswien.fhmdb.exceptions.InvalidSearchException;
import at.ac.fhcampuswien.fhmdb.exceptions.MovieApiException;
import at.ac.fhcampuswien.fhmdb.exceptions.MovieNotFoundException;
import at.ac.fhcampuswien.fhmdb.models.Genre;
import at.ac.fhcampuswien.fhmdb.models.Movie;
import okhttp3.*;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class MovieAPI {
    public static final String DELIMITER = "&";
    private static final String URL = "http://prog2.fh-campuswien.ac.at/movies"; // https if certificates work
    private static final OkHttpClient client = new OkHttpClient();

    private String buildUrl(UUID id) {
        StringBuilder url = new StringBuilder(URL);
        if (id != null) {
            url.append("/").append(id);
        }
        return url.toString();
    }

    private static String buildUrl(String query, Genre genre, String releaseYear, String ratingFrom) {
        StringBuilder url = new StringBuilder(URL);

        if ( (query != null && !query.isEmpty()) ||
                genre != null || releaseYear != null || ratingFrom != null) {

            url.append("?");

            // check all parameters and add them to the url
            if (query != null && !query.isEmpty()) {
                url.append("query=").append(query).append(DELIMITER);
            }
            if (genre != null) {
                url.append("genre=").append(genre).append(DELIMITER);
            }
            if (releaseYear != null) {
                url.append("releaseYear=").append(releaseYear).append(DELIMITER);
            }
            if (ratingFrom != null) {
                url.append("ratingFrom=").append(ratingFrom).append(DELIMITER);
            }
        }

        return url.toString();
    }

    public static List<Movie> getAllMovies() {
        return getAllMovies(null, null, null, null);
    }

    public static List<Movie> getAllMovies(String query, Genre genre, String releaseYear, String ratingFrom) {
        // Optionale Validierung für Release Year und Rating
        try {
            if (releaseYear != null && !releaseYear.matches("\\d{4}")) {
                throw new InvalidSearchException("Ungültiges Jahr: " + releaseYear);
            }
            if (ratingFrom != null) {
                int rating = Integer.parseInt(ratingFrom);
                if (rating < 0 || rating > 10) {
                    throw new InvalidSearchException("Rating muss zwischen 0 und 10 liegen: " + rating);
                }
            }
        } catch (NumberFormatException e) {
            throw new InvalidSearchException("Rating muss eine Zahl sein: " + ratingFrom);
        }

        String url = buildUrl(query, genre, releaseYear, ratingFrom);
        Request request = new Request.Builder()
                .url(url)
                .removeHeader("User-Agent")
                .addHeader("User-Agent", "http.agent")  // needed for the server to accept the request
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new MovieApiException("API-Fehler: HTTP " + response.code(), null);
            }

            String responseBody = response.body().string();
            Gson gson = new Gson();
            Movie[] movies = gson.fromJson(responseBody, Movie[].class);

            if (movies == null || movies.length == 0) {
                throw new MovieNotFoundException("Keine Filme gefunden.");
            }

            List<Movie> result = Arrays.asList(movies);

            if (result.isEmpty()) {
                throw new MovieNotFoundException("Kein Film entspricht den Suchkriterien.");
            }

            return Arrays.asList(movies);
        } catch (IOException e) {
            throw new MovieApiException("Fehler beim Abrufen der Filme von der API", e);
        }
    }


    public Movie requestMovieById(UUID id){
        String url = buildUrl(id);
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            Gson gson = new Gson();
            return gson.fromJson(response.body().string(), Movie.class);
        } catch (Exception e) {
            System.err.println(this.getClass() + ": http status not ok");
        }

        return null;
    }
}
