// File: src/main/java/at/ac/fhcampuswien/fhmdb/entities/MovieEntity.java
package at.ac.fhcampuswien.fhmdb.entities;

import at.ac.fhcampuswien.fhmdb.models.Genre;
import at.ac.fhcampuswien.fhmdb.models.Movie;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@DatabaseTable(tableName = "movies")
public class MovieEntity {
    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField(canBeNull = false)
    private String apiId;

    @DatabaseField(canBeNull = false)
    private String title;

    @DatabaseField(columnDefinition = "TEXT")
    private String description;

    @DatabaseField
    private String genres;

    @DatabaseField
    private int releaseYear;

    @DatabaseField
    private String imgUrl;

    @DatabaseField
    private int lengthInMinutes;

    @DatabaseField
    private double rating;

    public MovieEntity() {}

    public MovieEntity(String apiId,
                       String title,
                       String description,
                       List<Genre> genres,
                       int releaseYear,
                       String imgUrl,
                       int lengthInMinutes,
                       double rating) {
        this.apiId          = apiId;
        this.title          = title;
        this.description    = description;
        this.genres         = genresToString(genres);
        this.releaseYear    = releaseYear;
        this.imgUrl         = imgUrl;
        this.lengthInMinutes= lengthInMinutes;
        this.rating         = rating;
    }


    public static String genresToString(List<Genre> genres) {
        return genres.stream()
                .map(Enum::name)
                .collect(Collectors.joining(","));
    }

    public static List<MovieEntity> fromMovies(List<Movie> movies) {
        return movies.stream()
                .map(m -> new MovieEntity(
                        m.getId(),
                        m.getTitle(),
                        m.getDescription(),
                        m.getGenres(),
                        m.getReleaseYear(),
                        m.getImgUrl(),
                        m.getLengthInMinutes(),
                        m.getRating()))
                .collect(Collectors.toList());
    }

    public static List<Movie> toMovies(List<MovieEntity> entities) {
        return entities.stream()
                .map(e -> new Movie(
                        e.apiId,
                        e.title,
                        e.description,
                        Arrays.stream(e.genres.split(","))
                                .map(Genre::valueOf)
                                .collect(Collectors.toList()),
                        e.releaseYear,
                        e.imgUrl,
                        e.lengthInMinutes,
                        e.rating))
                .collect(Collectors.toList());
    }

    public long getId() { return id; }
    public String getApiId() { return apiId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getGenres() { return genres; }
    public int getReleaseYear() { return releaseYear; }
    public String getImgUrl() { return imgUrl; }
    public int getLengthInMinutes() { return lengthInMinutes; }
    public double getRating() { return rating; }

    public void setApiId(String apiId) { this.apiId = apiId; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setGenres(String genres) { this.genres = genres; }
    public void setReleaseYear(int releaseYear) { this.releaseYear = releaseYear; }
    public void setImgUrl(String imgUrl) { this.imgUrl = imgUrl; }
    public void setLengthInMinutes(int lengthInMinutes) { this.lengthInMinutes = lengthInMinutes; }
    public void setRating(double rating) { this.rating = rating; }
}
