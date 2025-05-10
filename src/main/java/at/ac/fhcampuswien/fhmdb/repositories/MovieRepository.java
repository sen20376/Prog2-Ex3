package at.ac.fhcampuswien.fhmdb.repositories;

import at.ac.fhcampuswien.fhmdb.entities.MovieEntity;
import at.ac.fhcampuswien.fhmdb.exceptions.DatabaseException;
import at.ac.fhcampuswien.fhmdb.models.Movie;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;

import java.sql.SQLException;
import java.util.List;

public class MovieRepository {
    private final Dao<MovieEntity, Long> dao;

    public MovieRepository(Dao<MovieEntity, Long> dao) {
        this.dao = dao;
    }

    public List<MovieEntity> getAllMovies() {
        try {
            return dao.queryForAll();
        } catch (SQLException e) {
            throw new DatabaseException("Fehler beim Auslesen aller Filme", e);
        }
    }

    public int removeAll() {
        try {
            DeleteBuilder<MovieEntity, Long> db = dao.deleteBuilder();
            return db.delete();
        } catch (SQLException e) {
            throw new DatabaseException("Fehler beim Löschen aller Filme", e);
        }
    }

    public MovieEntity getMovie(String apiId) {
        try {
            return dao.queryBuilder()
                    .where()
                    .eq("apiId", apiId)
                    .queryForFirst();
        } catch (SQLException e) {
            throw new DatabaseException("Fehler beim Laden des Films mit apiId=" + apiId, e);
        }
    }

    public int addAllMovies(List<Movie> movies) {
        try {
            int count = 0;
            for (Movie m : movies) {
                MovieEntity e = new MovieEntity(
                        m.getId(),
                        m.getTitle(),
                        m.getDescription(),
                        m.getGenres(),
                        m.getReleaseYear(),
                        m.getImgUrl(),
                        m.getLengthInMinutes(),
                        m.getRating()
                );
                count += dao.create(e);
            }
            return count;
        } catch (SQLException e) {
            throw new DatabaseException("Fehler beim Einfügen mehrerer Filme", e);
        }
    }
}