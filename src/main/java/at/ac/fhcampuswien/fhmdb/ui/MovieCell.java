package at.ac.fhcampuswien.fhmdb.ui;

import at.ac.fhcampuswien.fhmdb.db.DatabaseManager;
import at.ac.fhcampuswien.fhmdb.entities.WatchlistMovieEntity;
import at.ac.fhcampuswien.fhmdb.models.Movie;
import at.ac.fhcampuswien.fhmdb.repositories.WatchlistRepository;
import at.ac.fhcampuswien.fhmdb.util.ClickEventHandler;
import com.jfoenix.controls.JFXButton;
import com.j256.ormlite.dao.Dao;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import java.util.List;
import java.util.stream.Collectors;

public class MovieCell extends ListCell<Movie> {
    private final Label title = new Label();
    private final Label detail = new Label();
    private final Label genre = new Label();
    private final JFXButton detailBtn = new JFXButton("Show Details");
    private final JFXButton watchlistBtn = new JFXButton();
    private final VBox layout = new VBox(title, detail, genre, detailBtn, watchlistBtn);

    private final WatchlistRepository watchRepo;
    private boolean collapsedDetails = true;
    private boolean inWatchlist = false;

    public MovieCell(ClickEventHandler<Movie> addHandler, ClickEventHandler<Movie> removeHandler) {
        super();

        try {
            Dao<WatchlistMovieEntity, Long> wDao = DatabaseManager.getWatchlistDao();
            watchRepo = new WatchlistRepository(wDao);
        } catch (Exception e) {
            throw new RuntimeException("Konnte Watchlist-DAO nicht laden", e);
        }

        detailBtn.setStyle("-fx-background-color: #f5c518;");
        watchlistBtn.setStyle("-fx-background-color: #f5c518;");
        title.getStyleClass().add("text-yellow");
        detail.getStyleClass().add("text-white");
        genre.getStyleClass().add("text-white");
        genre.setStyle("-fx-font-style: italic");
        layout.setBackground(new Background(new BackgroundFill(Color.web("#454545"), null, null)));
        title.setStyle("-fx-font-size: 20px;");
        detail.setWrapText(true);
        layout.setPadding(new Insets(10));
        layout.setSpacing(10);

        // Detail-Button
        detailBtn.setOnMouseClicked(e -> {
            if (collapsedDetails) {
                layout.getChildren().add(getDetails());
                detailBtn.setText("Hide Details");
            } else {
                layout.getChildren().remove(getDetailsIndex());
                detailBtn.setText("Show Details");
            }
            collapsedDetails = !collapsedDetails;
            setGraphic(layout);
        });

        // Watchlist-Button
        watchlistBtn.setOnMouseClicked(e -> {
            Movie m = getItem();
            if (m == null) return;
            if (inWatchlist) {
                removeHandler.onClick(m);
            } else {
                addHandler.onClick(m);
            }
            inWatchlist = !inWatchlist;
            updateWatchlistButton();
        });
    }

    @Override
    protected void updateItem(Movie movie, boolean empty) {
        super.updateItem(movie, empty);

        if (empty || movie == null) {
            setGraphic(null);
            setText(null);
        } else {
            title.setText(movie.getTitle());
            detail.setText(movie.getDescription() != null
                    ? movie.getDescription()
                    : "No description available");
            genre.setText(movie.getGenres().stream()
                    .map(Enum::toString)
                    .collect(Collectors.joining(", ")));
            detail.setMaxWidth(getListView().getWidth() - 30);

            List<WatchlistMovieEntity> all = watchRepo.getWatchlist();
            inWatchlist = all.stream()
                    .map(WatchlistMovieEntity::getApiId)
                    .anyMatch(apiId -> apiId.equals(movie.getId()));
            updateWatchlistButton();

            setGraphic(layout);
        }
    }

    private void updateWatchlistButton() {
        watchlistBtn.setText(
                inWatchlist
                        ? "Remove from Watchlist"
                        : "Add to Watchlist"
        );
    }

    private VBox getDetails() {
        return new VBox(
                new Label("Release Year: " + getItem().getReleaseYear()),
                new Label("Length: " + getItem().getLengthInMinutes() + " minutes"),
                new Label("Rating: " + getItem().getRating() + "/10"),
                new Label("Directors: " + String.join(", ", getItem().getDirectors())),
                new Label("Writers:   " + String.join(", ", getItem().getWriters())),
                new Label("Main Cast: " + String.join(", ", getItem().getMainCast()))
        );
    }

    private int getDetailsIndex() {
        // title(0), detail(1), genre(2), detailBtn(3), watchlistBtn(4)
        // wir fügen Details immer an Position 4 ein
        return 4;
    }
}