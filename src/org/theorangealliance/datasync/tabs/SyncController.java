package org.theorangealliance.datasync.tabs;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.theorangealliance.datasync.DataSyncController;
import org.theorangealliance.datasync.util.Config;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Kyle Flynn on 11/30/2017.
 */
public class SyncController implements Runnable {

    private DataSyncController controller;
    private TOAExecuteAdapter executeAdapter;
    private ScheduledExecutorService service;

    private int totalSyncs;

    public SyncController(DataSyncController instance) {
        this.controller = instance;
        this.controller.btnSyncStart.setDisable(false);
        this.controller.btnSyncStop.setDisable(true);
        this.controller.btnSyncMatches.setDisable(false);
    }

    public void execute(TOAExecuteAdapter toaExecuteAdapter) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Are you sure about this?");
        alert.setHeaderText("This operation cannot be undone.");
        String matchesText;
        if (this.controller.btnSyncMatches.selectedProperty().get()) {
            matchesText = "Match results WILL AUTOMATICALLY BE UPLOADED ON COMPLETED MATCH.";
        } else {
            matchesText = "Match results, however, MUST be uploaded separately in the matches tab.";
        }
        alert.setContentText("You are about to start AutoSync. This will automatically stage changes from the scoring system and make them ready to be uploaded. " +
                "Rankings WILL AUTOMATICALLY BE UPLOADED ON COMPLETED MATCH. " + matchesText + " Continue?");

        ButtonType okayButton = new ButtonType("Sure?");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        //Set Icon because it shows in the task bar
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/app_ico.png")));

        alert.getButtonTypes().setAll(okayButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == okayButton) {
            this.service = Executors.newSingleThreadScheduledExecutor();
            this.controller.btnSyncStart.setDisable(true);
            this.totalSyncs = 0;
            controller.btnSyncStop.setDisable(false);
            controller.btnSyncMatches.setDisable(true);
            //Do Dashboard Stuff
            controller.cb_autoSync.setSelected(true);
            controller.cb_autoSync.setTextFill(Color.GREEN);
            controller.btn_cb_autoSync.setDisable(true);

            service.scheduleAtFixedRate(this, 0, 40, TimeUnit.SECONDS);
            executeAdapter = toaExecuteAdapter;
        }
    }

    public void kill() {
        controller.btnSyncStart.setDisable(false);
        controller.btnSyncStop.setDisable(true);
        controller.btnSyncMatches.setDisable(false);
        //Do Dashboard Stuff
        controller.cb_autoSync.setSelected(false);
        controller.cb_autoSync.setTextFill(Color.RED);
        controller.btn_cb_autoSync.setDisable(false);
        if (service != null) {
            service.shutdownNow();
        }
    }

    @Override
    public void run() {
        if (executeAdapter != null) {
            totalSyncs++;
            executeAdapter.onExecute(totalSyncs);
        }
    }

    public interface TOAExecuteAdapter {
        void onExecute(int totalSyncs);
    }

}
