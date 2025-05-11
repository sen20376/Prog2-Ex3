package at.ac.fhcampuswien.fhmdb;

import at.ac.fhcampuswien.fhmdb.db.DatabaseManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class FhmdbApplication extends Application {
    private static final String DB_URL   = "jdbc:h2:~/fhmdb;AUTO_SERVER=TRUE";
    private static final String USERNAME = "sa";
    private static final String PASSWORD = "";

    @Override
    public void init() throws Exception {
        super.init();
        DatabaseManager dbm = new DatabaseManager(DB_URL, USERNAME, PASSWORD);
        dbm.createConnectionSource();
        dbm.createTables();
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(FhmdbApplication.class.getResource("home-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 890, 620);
        scene.getStylesheets().add(
                FhmdbApplication.class.getResource("styles.css").toExternalForm()
        );
        stage.setTitle("FHMDb!");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        try {
            if (DatabaseManager.getConnectionSource() != null) {
                DatabaseManager.getConnectionSource().closeQuietly();
            }
        } catch (Exception e) {
            System.err.println("Fehler beim Schließen der DB-Verbindung: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
