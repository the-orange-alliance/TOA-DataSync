package org.theorangealliance.datasync;

import com.sun.corba.se.impl.orbutil.concurrent.Sync;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import org.theorangealliance.datasync.json.EventJSON;
import org.theorangealliance.datasync.logging.TOALogger;
import org.theorangealliance.datasync.models.MatchGeneral;
import org.theorangealliance.datasync.models.Team;
import org.theorangealliance.datasync.models.TeamRanking;
import org.theorangealliance.datasync.tabs.MatchesController;
import org.theorangealliance.datasync.tabs.RankingsController;
import org.theorangealliance.datasync.tabs.SyncController;
import org.theorangealliance.datasync.tabs.TeamsController;
import org.theorangealliance.datasync.util.Config;
import org.theorangealliance.datasync.util.TOAEndpoint;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.logging.Level;

/**
 * Created by Kyle Flynn on 11/28/2017.
 */
public class DataSyncController implements Initializable {

    /* File name for saving the Key/Id/Scoring System Directory */
    private static final String SAVE_FILE_NAME = "Settings.txt";

    /* This is the left-side of the setup tab. */
    @FXML public Tab tabSetup;
    @FXML public TextField txtSetupKey;
    @FXML public TextField txtSetupID;
    @FXML public Button btnSetupTest;
    @FXML public Label labelSetupTest;
    @FXML public Label txtConsole;

    /* This is the right-side of the setup tab. */
    @FXML public TextField txtSetupDir;
    @FXML public Button btnSetupSelect;
    @FXML public Button btnSetupTestDir;
    @FXML public Label labelSetupDir;

    /* This is for our teams tab. */
    @FXML public Tab tabTeams;
    @FXML public TableView<Team> tableTeams;
    @FXML public TableColumn colTeamsTeam;
    @FXML public TableColumn colTeamDiv;
    @FXML public TableColumn colTeamsRegion;
    @FXML public TableColumn colTeamsLeague;
    @FXML public TableColumn colTeamsShort;
    @FXML public TableColumn colTeamsLong;
    @FXML public TableColumn colTeamsLocation;
    @FXML public Button btnTeamsPost;
    @FXML public Button btnTeamsDelete;

    /* This is for our matches tab. */
    @FXML public Tab tabMatches;
    @FXML public Button btnMatchImport;
    @FXML public Button btnMatchScheduleUpload;
    @FXML public Button btnMatchUpload;
    @FXML public Button btnMatchSync;
    @FXML public Button btnMatchOpen;
    @FXML public TableView<MatchGeneral> tableMatches;
    @FXML public TableColumn<MatchGeneral, String> colMatchName;
    @FXML public TableColumn<MatchGeneral, Boolean> colMatchDone;
    @FXML public TableColumn<MatchGeneral, Boolean> colMatchPosted;
    @FXML public Label labelScheduleUploaded;
    @FXML public Label labelMatchLevel;
    @FXML public Label labelMatchField;
    @FXML public Label labelMatchPlay;
    @FXML public Label labelMatchName;
    @FXML public Label labelMatchKey;
    @FXML public Label labelRedAuto;
    @FXML public Label labelRedTele;
    @FXML public Label labelRedEnd;
    @FXML public Label labelRedPenalty;
    @FXML public Label labelRedScore;
    @FXML public Label labelBlueAuto;
    @FXML public Label labelBlueTele;
    @FXML public Label labelBlueEnd;
    @FXML public Label labelBluePenalty;
    @FXML public Label labelBlueScore;
    @FXML public Label labelRedTeams;
    @FXML public Label labelBlueTeams;

    /* This is for our rankings tab. */
    @FXML public Tab tabRankings;
    @FXML public TableView<TeamRanking> tableRankings;
    @FXML public TableColumn<TeamRanking, Integer> colRank;
    @FXML public TableColumn<TeamRanking, Integer> colRankTeam;
    @FXML public TableColumn<TeamRanking, Integer> colRankWins;
    @FXML public TableColumn<TeamRanking, Integer> colRankLosses;
    @FXML public TableColumn<TeamRanking, Integer> colRankTies;
    @FXML public TableColumn<TeamRanking, Integer> colRankQP;
    @FXML public TableColumn<TeamRanking, Integer> colRankRP;
    @FXML public TableColumn<TeamRanking, Integer> colRankScore;
    @FXML public TableColumn<TeamRanking, Integer> colRankPlayed;
    @FXML public Button btnRankUpload;

    /* This is for our sync tab. */
    @FXML public Tab tabSync;
    @FXML public Button btnSyncStart;
    @FXML public Button btnSyncStop;
    @FXML public CheckBox btnSyncMatches;

    /* Instances of our tab controllers. */
    private TeamsController teamsController;
    private MatchesController matchesController;
    private RankingsController rankingsController;
    private SyncController syncController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.teamsController = new TeamsController(this);
        this.matchesController = new MatchesController(this);
        this.rankingsController = new RankingsController(this);
        this.syncController = new SyncController(this);

        labelSetupTest.setTextFill(Color.RED);
        labelSetupDir.setTextFill(Color.RED);

        txtSetupDir.setEditable(false);
        btnSetupSelect.setDisable(true);
        btnSetupTestDir.setDisable(true);

        //Check for default settings
        try (Scanner scan = new Scanner(new File(SAVE_FILE_NAME))){

            if(scan.hasNextLine()){
                txtSetupKey.setText(scan.nextLine());
            }
            if(scan.hasNextLine()){
                txtSetupID.setText(scan.nextLine());
            }
            if(scan.hasNextLine()){
                txtSetupDir.setText(scan.nextLine());
            }

        } catch (FileNotFoundException e){
            /* There were no saved strings, continue as is*/
        }

    }

    @FXML
    public void openDocs() {
        try {
            Desktop.getDesktop().browse(new URI("https://drive.google.com/open?id=1fSdBXXRTmZvWZXSr1ieZB4vJNWNUefXYdbUPU6TyhsM"));
        } catch (IOException | URISyntaxException e1) {
            e1.printStackTrace();
        }
    }

    @FXML
    public void testConnection() {
        if (txtSetupKey.getText().length() > 0 && txtSetupID.getText().length() > 0) {
            saveSettings();
            // This will grab the base URL.
            TOAEndpoint testConnection = new TOAEndpoint("GET", "event/" + txtSetupID.getText());
            testConnection.setCredentials(txtSetupKey.getText(), txtSetupID.getText());
            testConnection.execute((response, success) -> {
                if (success) {
                    // Setup our local config
                    Config.EVENT_API_KEY = txtSetupKey.getText();
                    Config.EVENT_ID = txtSetupID.getText();

                    EventJSON[] events = testConnection.getGson().fromJson(response, EventJSON[].class);
                    if (events.length > 0 && events[0] != null) {
                        if (events[0].getDivisionName() != null || events[0].getDivisionKey() > 0) {
                            Config.DUAL_DIVISION_EVENT = true;
                            sendInfo("Connection to TOA was successful. DETECTED DUAL DIVISION EVENT. Proceed to Scoring System setup.");
                        } else {
                            sendInfo("Connection to TOA was successful. Proceed to Scoring System setup.");
                        }
                    } else {
                        sendInfo("Connection to TOA was successful. Proceed to Scoring System setup.");
                    }

                    labelSetupTest.setText("Connection Successful");
                    labelSetupTest.setTextFill(Color.GREEN);

                    txtSetupDir.setEditable(true);
                    btnSetupSelect.setDisable(false);
                    btnSetupTestDir.setDisable(false);

                    matchesController.checkMatchSchedule();
                    matchesController.checkMatchDetails();
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
            saveSettings();
            String root = txtSetupDir.getText();
            File divisionsFile = new File(root + File.separator + "divisions.txt");
            if (divisionsFile.exists()) {
                Config.SCORING_DIR = root;

                try {
                    BufferedReader reader = new BufferedReader(new FileReader(divisionsFile));
                    String line = reader.readLine();
                    line = reader.readLine();
                    Config.EVENT_NAME = line;
                    while ((line = reader.readLine()) != null) {
                        if (line.split("\\|").length > 3) {
                            Config.DIVISION_NAME = line.split("\\|")[1];
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                labelSetupDir.setTextFill(Color.GREEN);
                labelSetupDir.setText("Valid Directory.");

                sendInfo("Found divisions.txt");

                tabTeams.setDisable(false);
                tabMatches.setDisable(false);
                tabRankings.setDisable(false);
                tabSync.setDisable(false);
            } else {
                sendError("Scoring System not setup. Are you using the right directory?");
            }
        } else {
            sendError("You must select a directory for the scoring system root.");
        }
    }

    public void sendInfo(String message) {
        txtConsole.setTextFill(Color.GREEN);
        txtConsole.setText(message);
    }

    public void sendWarning(String message) {
        txtConsole.setTextFill(Color.YELLOW);
        txtConsole.setText(message);
    }

    public void sendError(String message) {
        txtConsole.setTextFill(Color.RED);
        txtConsole.setText(message);
    }

    @FXML
    public void getTeamsByURL() {
        this.teamsController.getTeamsByURL();
    }

    @FXML
    public void getTeamsByFile() {
        this.teamsController.getTeamsByFile();
    }

    @FXML
    public void postEventTeams() {
        this.teamsController.postEventTeams();
    }

    @FXML
    public void deleteEventTeams() {
        this.teamsController.deleteEventTeams();
    }

    @FXML
    public void getMatchesByFile() {
        this.matchesController.getMatchesByFile();
    }

    @FXML
    public void postMatchSchedule() {
        this.matchesController.postMatchSchedule();
    }

    @FXML
    public void postSelectedMatch() {
        this.matchesController.postSelectedMatch();
    }

    @FXML
    public void openMatchDetails() {
        this.matchesController.openMatchDetails();
    }

    @FXML
    public void startAutoSync() {
        DataSync.getMainStage().setOnCloseRequest(closeEvent -> this.syncController.kill());
        this.syncController.execute((count) -> {
            Platform.runLater(() -> {
                Date date = new Date();
                TOALogger.log(Level.INFO, "Executing update #" + count + " at " + DateFormat.getTimeInstance(DateFormat.SHORT).format(date));
                TOALogger.log(Level.INFO, "There are " + Thread.activeCount() + " threads.");
                this.matchesController.syncMatches();
                this.matchesController.checkMatchSchedule();
                this.matchesController.checkMatchDetails();
//                this.rankingsController.syncRankings();
                // We're going to try THIS instead....
                this.rankingsController.getRankingsByFile();
                this.rankingsController.postRankings();
                if (this.btnSyncMatches.selectedProperty().get()) {
                     this.matchesController.postCompletedMatches();
                }
            });
        });
    }

    @FXML
    public void stopAutoSync() {
        this.syncController.kill();
    }

    @FXML
    public void getRankingsByFile() {
        this.rankingsController.getRankingsByFile();
    }

    @FXML
    public void postRankings() {
        this.rankingsController.postRankings();
    }

    @FXML
    public void deleteRankings() {
        this.rankingsController.deleteRankings();
    }

    public HashMap<Integer, int[]> getTeamWL() {
        return this.matchesController.getTeamWL();
    }

    private void saveSettings(){

        try (PrintWriter out = new PrintWriter(SAVE_FILE_NAME)){

            out.println(txtSetupKey.getText());
            out.println(txtSetupID.getText());
            out.println(txtSetupDir.getText());

            out.close();

        }catch (FileNotFoundException e){

            TOALogger.log(Level.WARNING, "Error saving settings");

        }

    }

}
