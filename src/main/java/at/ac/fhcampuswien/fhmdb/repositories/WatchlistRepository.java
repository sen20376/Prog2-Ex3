package at.ac.fhcampuswien.fhmdb.repositories;

import at.ac.fhcampuswien.fhmdb.entities.WatchlistMovieEntity;
import at.ac.fhcampuswien.fhmdb.exceptions.DatabaseException;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;

import java.sql.SQLException;
import java.util.List;

public class WatchlistRepository {
    private final Dao<WatchlistMovieEntity, Long> dao;

    public WatchlistRepository(Dao<WatchlistMovieEntity, Long> dao) {
        this.dao = dao;
    }

    public List<WatchlistMovieEntity> getWatchlist() {
        try {
            return dao.queryForAll();
        } catch (SQLException e) {
            throw new DatabaseException("Fehler beim Auslesen der Watchlist", e);
        }
    }

    public int addToWatchlist(WatchlistMovieEntity movie) {
        try {
            return dao.create(movie);
        } catch (SQLException e) {
            throw new DatabaseException("Fehler beim Hinzufügen zur Watchlist", e);
        }
    }

    public int removeFromWatchlist(String apiId) {
        try {
            DeleteBuilder<WatchlistMovieEntity, Long> db = dao.deleteBuilder();
            db.where().eq("apiId", apiId);
            return db.delete();
        } catch (SQLException e) {
            throw new DatabaseException("Fehler beim Entfernen aus der Watchlist", e);
        }
    }
}