package at.ac.fhcampuswien.fhmdb;

import at.ac.fhcampuswien.fhmdb.api.MovieAPI;
import at.ac.fhcampuswien.fhmdb.db.DatabaseManager;
import at.ac.fhcampuswien.fhmdb.entities.MovieEntity;
import at.ac.fhcampuswien.fhmdb.entities.WatchlistMovieEntity;
import at.ac.fhcampuswien.fhmdb.exceptions.*;
import at.ac.fhcampuswien.fhmdb.models.Genre;
import at.ac.fhcampuswien.fhmdb.models.Movie;
import at.ac.fhcampuswien.fhmdb.models.SortedState;
import at.ac.fhcampuswien.fhmdb.repositories.MovieRepository;
import at.ac.fhcampuswien.fhmdb.repositories.WatchlistRepository;
import at.ac.fhcampuswien.fhmdb.ui.MovieCell;
import com.j256.ormlite.dao.Dao;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXListView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class HomeController implements Initializable {
    @FXML private JFXButton searchBtn;
    @FXML private JFXButton homeBtn;
    @FXML private JFXButton watchlistNavBtn;
    @FXML private JFXButton sortBtn;
    @FXML private TextField searchField;
    @FXML private JFXListView<Movie> movieListView;
    @FXML private JFXComboBox<String> genreComboBox;
    @FXML private JFXComboBox<String> releaseYearComboBox;
    @FXML private JFXComboBox<String> ratingFromComboBox;
    @FXML private VBox sideMenu;

    private List<Movie> allMovies;
    private final ObservableList<Movie> observableMovies = FXCollections.observableArrayList();
    private SortedState sortedState;

    private final MovieRepository movieRepo;
    private final WatchlistRepository watchRepo;

    public HomeController() {
        try {
            DatabaseManager dbm = new DatabaseManager("jdbc:h2:~/fhmdb;AUTO_SERVER=TRUE", "sa", "");
            dbm.createConnectionSource();
            dbm.createTables();

            Dao<MovieEntity, Long> mDao = DatabaseManager.getMovieDao();
            Dao<WatchlistMovieEntity, Long> wDao = DatabaseManager.getWatchlistDao();

            this.movieRepo = new MovieRepository(mDao);
            this.watchRepo = new WatchlistRepository(wDao);

        } catch (Exception e) {
            throw new RuntimeException("DB-Initialization failed", e);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initializeState();
        initializeLayout();
    }

    private void initializeState() {
        try {
            List<Movie> movies = MovieAPI.getAllMovies();
            movieRepo.removeAll();
            movieRepo.addAllMovies(movies);
            setMovies(movies);
            setMovieList(movies);

        } catch (MovieApiException apiEx) {
            showErrorDialog("API-Fehler beim Laden der Filme", apiEx.getMessage());
            try {
                List<MovieEntity> entities = movieRepo.getAllMovies();
                List<Movie> cached = MovieEntity.toMovies(entities);
                setMovies(cached);
                setMovieList(cached);
            } catch (DatabaseException dbEx) {
                showErrorDialog("DB-Fehler beim Lesen des Caches", dbEx.getMessage());
            }
        } catch (DatabaseException dbEx) {
            showErrorDialog("DB-Fehler beim Cachen der Filme", dbEx.getMessage());

        } catch (MovieNotFoundException notFoundEx) {
            showErrorDialog("Kein Film gefunden.", notFoundEx.getMessage());

        } catch (InvalidSearchException searchEX) {
            showErrorDialog("Fehlerhafte Suchparameter!", searchEX.getMessage());

        }

        sortedState = SortedState.NONE;
    }

    private void initializeLayout() {
        movieListView.setItems(observableMovies);

        genreComboBox.getItems().add("No filter");
        for (Genre g : Genre.values()) genreComboBox.getItems().add(g.name());
        genreComboBox.setPromptText("Filter by Genre");

        releaseYearComboBox.getItems().add("No filter");
        for (int y = 1900; y <= 2023; y++) releaseYearComboBox.getItems().add(String.valueOf(y));
        releaseYearComboBox.setPromptText("Filter by Release Year");

        ratingFromComboBox.getItems().add("No filter");
        for (int r = 0; r <= 10; r++) ratingFromComboBox.getItems().add(String.valueOf(r));
        ratingFromComboBox.setPromptText("Filter by Rating");

        movieListView.setCellFactory(lv -> new MovieCell(
                m -> {
                    try { watchRepo.addToWatchlist(new WatchlistMovieEntity(m.getId())); }
                    catch (DatabaseException e) { showErrorDialog("Watchlist-Hinzufügen", e.getMessage()); }
                },
                m -> {
                    try { watchRepo.removeFromWatchlist(m.getId()); }
                    catch (DatabaseException e) { showErrorDialog("Watchlist-Entfernen", e.getMessage()); }
                }
        ));
    }

    @FXML
    private void showAllMovies(ActionEvent ev) {
        setMovieList(allMovies);
        searchField.clear();
        genreComboBox.getSelectionModel().clearSelection();
        releaseYearComboBox.getSelectionModel().clearSelection();
        ratingFromComboBox.getSelectionModel().clearSelection();
        sortedState = SortedState.NONE;
        if (homeBtn != null) homeBtn.setDisable(true);
        if (watchlistNavBtn != null) watchlistNavBtn.setDisable(false);
    }

    @FXML
    private void showWatchlist(ActionEvent ev) {
        List<MovieEntity> entities = watchRepo.getWatchlist().stream()
                .map(WatchlistMovieEntity::getApiId)
                .map(movieRepo::getMovie)
                .collect(Collectors.toList());
        List<Movie> watchlist = MovieEntity.toMovies(entities);
        setMovieList(watchlist);
        sortedState = SortedState.NONE;
        if (homeBtn != null) homeBtn.setDisable(false);
        if (watchlistNavBtn != null) watchlistNavBtn.setDisable(true);
    }

    @FXML
    private void sortBtnClicked(ActionEvent ev) {
        if (sortedState == SortedState.NONE || sortedState == SortedState.DESCENDING) {
            sortMovies(SortedState.ASCENDING);
        } else {
            sortMovies(SortedState.DESCENDING);
        }
    }

    private void sortMovies(SortedState dir) {
        if (dir == SortedState.ASCENDING) {
            observableMovies.sort((a, b) -> a.getTitle().compareToIgnoreCase(b.getTitle()));
        } else {
            observableMovies.sort((a, b) -> b.getTitle().compareToIgnoreCase(a.getTitle()));
        }
        sortedState = dir;
    }

    @FXML
    private void searchBtnClicked(ActionEvent ev) {
        String q = searchField.getText().trim().toLowerCase();
        String yr = validateComboboxValue(releaseYearComboBox.getValue());
        String rt = validateComboboxValue(ratingFromComboBox.getValue());
        String gn = validateComboboxValue(genreComboBox.getValue());
        Genre genre = gn != null ? Genre.valueOf(gn) : null;

        try {
            List<Movie> movies = MovieAPI.getAllMovies(q, genre, yr, rt);
            movieRepo.removeAll();
            movieRepo.addAllMovies(movies);
            setMovies(movies);
            setMovieList(movies);
            if (sortedState != SortedState.NONE) sortMovies(sortedState);
        } catch (MovieApiException ex) {
            showErrorDialog("API-Fehler bei der Suche", ex.getMessage());
        } catch (DatabaseException dbEx) {
            showErrorDialog("DB-Fehler beim Aktualisieren des Caches", dbEx.getMessage());
        }
    }

    private String validateComboboxValue(String val) {
        return "No filter".equals(val) ? null : val;
    }

    private void setMovies(List<Movie> movies) {
        this.allMovies = movies;
    }

    private void setMovieList(List<Movie> movies) {
        observableMovies.setAll(movies);
    }

    private void showErrorDialog(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    @FXML
    private void toggleMenu(ActionEvent event) {
        boolean isVisible = sideMenu.isVisible();
        sideMenu.setVisible(!isVisible);
        sideMenu.setManaged(!isVisible);
    }

    @FXML
    private void showAbout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("FHMDb - Movie Application");
        alert.setContentText("Strommer bester Lektor\nVersion 1.0");
        alert.showAndWait();
    }

    // Zusatzfunktionen für Analyse
    public List<Movie> getMovies(String searchQuery, Genre genre, String releaseYear, String ratingFrom) {
        return MovieAPI.getAllMovies(searchQuery, genre, releaseYear, ratingFrom);
    }

    public String getMostPopularActor(List<Movie> movies) {
        return movies.stream()
                .flatMap(m -> m.getMainCast().stream())
                .collect(Collectors.groupingBy(a -> a, Collectors.counting()))
                .entrySet().stream()
                .max((e1, e2) -> e1.getValue().compareTo(e2.getValue()))
                .map(java.util.Map.Entry::getKey)
                .orElse("");
    }

    public int getLongestMovieTitle(List<Movie> movies) {
        return movies.stream()
                .mapToInt(m -> m.getTitle().length())
                .max()
                .orElse(0);
    }

    public long countMoviesFrom(List<Movie> movies, String director) {
        return movies.stream()
                .filter(m -> m.getDirectors().contains(director))
                .count();
    }

    public List<Movie> getMoviesBetweenYears(List<Movie> movies, int startYear, int endYear) {
        return movies.stream()
                .filter(m -> m.getReleaseYear() >= startYear && m.getReleaseYear() <= endYear)
                .toList();
    }
}