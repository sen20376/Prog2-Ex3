package at.ac.fhcampuswien.fhmdb.db;

import at.ac.fhcampuswien.fhmdb.entities.MovieEntity;
import at.ac.fhcampuswien.fhmdb.entities.WatchlistMovieEntity;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.j256.ormlite.dao.DaoManager;

public class DatabaseManager {
    private final String DB_URL;
    private final String username;
    private final String password;

    private static ConnectionSource conn;
    private static Dao<MovieEntity, Long> movieDao;
    private static Dao<WatchlistMovieEntity, Long> watchlistDao;

    public DatabaseManager(String DB_URL, String username, String password) {
        this.DB_URL   = DB_URL;
        this.username = username;
        this.password = password;
    }

    public void createConnectionSource() throws Exception {
        conn = new JdbcConnectionSource(DB_URL, username, password);
    }

    public void createTables() throws Exception {
        TableUtils.createTableIfNotExists(conn, MovieEntity.class);
        TableUtils.createTableIfNotExists(conn, WatchlistMovieEntity.class);
    }

    public static ConnectionSource getConnectionSource() {
        return conn;
    }

    public static Dao<MovieEntity, Long> getMovieDao() throws Exception {
        if (movieDao == null) {
            movieDao = DaoManager.createDao(conn, MovieEntity.class);
        }
        return movieDao;
    }

    public static Dao<WatchlistMovieEntity, Long> getWatchlistDao() throws Exception {
        if (watchlistDao == null) {
            watchlistDao = DaoManager.createDao(conn, WatchlistMovieEntity.class);
        }
        return watchlistDao;
    }
}
