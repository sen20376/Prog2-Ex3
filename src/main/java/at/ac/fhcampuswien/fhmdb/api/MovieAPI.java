package at.ac.fhcampuswien.fhmdb.api;

import at.ac.fhcampuswien.fhmdb.exceptions.MovieApiException;
import at.ac.fhcampuswien.fhmdb.models.Genre;
import at.ac.fhcampuswien.fhmdb.models.Movie;
import okhttp3.*;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class MovieAPI {
    public static final String DELIMITER = "&";
    private static final String BASE_URL = "http://prog2.fh-campuswien.ac.at/movies"; // https if certificates work
    private static final OkHttpClient client = new OkHttpClient();

    private String buildUrl(UUID id) {
        StringBuilder url = new StringBuilder(BASE_URL);
        if (id != null) {
            url.append("/").append(id);
        }
        return url.toString();
    }

    private static String buildUrl(String query, Genre genre, String releaseYear, String ratingFrom) {
        StringBuilder url = new StringBuilder(BASE_URL);
        if ((query != null && !query.isEmpty()) ||
                genre != null || releaseYear != null || ratingFrom != null) {
            url.append("?");
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
        String url = buildUrl(query, genre, releaseYear, ratingFrom);
        Request request = new Request.Builder()
                .url(url)
                .removeHeader("User-Agent")
                .addHeader("User-Agent", "http.agent")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new MovieApiException("API-Fehler: HTTP " + response.code(), null);
            }
            String body = response.body().string();
            Movie[] arr = new Gson().fromJson(body, Movie[].class);
            return Arrays.asList(arr);
        } catch (MovieApiException e) {
            throw e;
        } catch (Exception e) {
            throw new MovieApiException("Fehler beim Laden der Filme von der API", e);
        }
    }

    public Movie requestMovieById(UUID id) {
        String url = BASE_URL + "/" + id;
        Request request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new MovieApiException("API-Fehler: HTTP " + response.code(), null);
            }
            return new Gson().fromJson(response.body().string(), Movie.class);
        } catch (MovieApiException e) {
            throw e;
        } catch (Exception e) {
            throw new MovieApiException("Fehler beim Laden des Films mit ID " + id, e);
        }
    }
}
