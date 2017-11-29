package org.theorangealliance.datasync;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import org.theorangealliance.datasync.util.TOAEndpoint;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Kyle Flynn on 11/28/2017.
 */
public class DataSyncController implements Initializable {

    /* This is the left-side of the setup tab. */
    @FXML Tab tabSetup;
    @FXML TextField txtSetupKey;
    @FXML TextField txtSetupID;
    @FXML Button btnSetupTest;
    @FXML Label labelSetupTest;
    @FXML Label txtConsole;

    /* This is the right-side of the setup tab. */
    @FXML TextField txtSetupDir;
    @FXML Button btnSetupSelect;
    @FXML Button btnSetupTestDir;
    @FXML Label labelSetupDir;

    @FXML Tab tabTeams;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        labelSetupTest.setTextFill(Color.RED);
        labelSetupDir.setTextFill(Color.RED);

        txtSetupDir.setEditable(false);
        btnSetupSelect.setDisable(true);
        btnSetupTestDir.setDisable(true);
    }

    @FXML
    public void testConnection() {
        if (txtSetupKey.getText().length() > 0 && txtSetupKey.getText().length() > 0) {
            // This will grab the base URL.
            TOAEndpoint testConnection = new TOAEndpoint("POST", "upload");
            testConnection.setCredentials(txtSetupKey.getText(), txtSetupID.getText());
            testConnection.execute((response, success) -> {
                if (success) {
                    sendInfo("Connection to TOA was successful. Proceed to Scoring System setup.");

                    labelSetupTest.setText("Connection Successful");
                    labelSetupTest.setTextFill(Color.GREEN);

                    txtSetupDir.setEditable(true);
                    btnSetupSelect.setDisable(false);
                    btnSetupTestDir.setDisable(false);
                } else {
                    sendError("Connection to TOA unsuccessful. " + response);

                    labelSetupTest.setText("Connection Unsuccessful.");
                    labelSetupTest.setTextFill(Color.RED);
                }
            });
        } else {
            sendError("You must enter the two fields described.");
        }
    }

    @FXML
    public void openScoringDialog() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Scoring System Directory");
        File file = directoryChooser.showDialog(DataSync.getMainStage());

        if (file != null && file.isDirectory()) {
            txtSetupDir.setText(file.getAbsolutePath());
        } else {
            sendError("Error in selecting scoring system directory.");
        }
    }

    @FXML
    public void testDirectory() {
        if (txtSetupDir.getText().length() > 0) {
            String root = txtSetupDir.getText();
            File divionsFile = new File(root + "\\divisions.txt");
            if (divionsFile.exists()) {
                labelSetupDir.setTextFill(Color.GREEN);
                labelSetupDir.setText("Valid Directory.");

                sendInfo("Found divisions.txt");

                tabTeams.setDisable(false);
            } else {
                sendError("Scoring System not setup. Are you using the right directory?");
            }
        } else {
            sendError("You must select a directory for the scoring system root.");
        }
    }

    private void sendInfo(String message) {
        txtConsole.setTextFill(Color.GREEN);
        txtConsole.setText(message);
    }

    private void sendWarning(String message) {
        txtConsole.setTextFill(Color.YELLOW);
        txtConsole.setText(message);
    }

    private void sendError(String message) {
        txtConsole.setTextFill(Color.RED);
        txtConsole.setText(message);
    }

}
