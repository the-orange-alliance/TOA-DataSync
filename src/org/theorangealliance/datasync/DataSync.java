package org.theorangealliance.datasync;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.theorangealliance.datasync.logging.TOALogger;
import org.theorangealliance.datasync.util.Config;

import java.io.IOException;
import java.util.logging.Level;

/**
 * Created by Kyle Flynn on 11/28/2017.
 */
public class DataSync extends Application {

    private static Stage mainStage;
    public static String[] arguments;

    @Override
    public void start(Stage primaryStage) throws Exception {

        try {
            TOALogger.init();
            TOALogger.log(Level.INFO, Config.VERSION + " initialized.");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DataSync.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            mainStage = primaryStage;
            mainStage.setScene(scene);
            mainStage.setTitle("TOA DataSync " + Config.VERSION);
            mainStage.getIcons().add(new Image(getClass().getResourceAsStream("/app_ico.png")));
            mainStage.setResizable(false);
            mainStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        arguments = args;
        launch(args);
    }

    public static Stage getMainStage() {
        return DataSync.mainStage;
    }

}
