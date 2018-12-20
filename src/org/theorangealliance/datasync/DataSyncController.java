package org.theorangealliance.datasync;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.theorangealliance.datasync.json.toa.EventJSON;
import org.theorangealliance.datasync.logging.TOALogger;
import org.theorangealliance.datasync.json.first.Event;
import org.theorangealliance.datasync.models.Award;
import org.theorangealliance.datasync.models.MatchGeneral;
import org.theorangealliance.datasync.models.Team;
import org.theorangealliance.datasync.models.TeamRanking;
import org.theorangealliance.datasync.tabs.*;
import org.theorangealliance.datasync.util.Config;
import org.theorangealliance.datasync.util.FIRSTEndpoint;
import org.theorangealliance.datasync.json.first.Events;
import org.theorangealliance.datasync.util.FIRSTEndpointNonLambda;
import org.theorangealliance.datasync.util.TOAEndpoint;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.util.*;
import java.util.logging.Level;

/**
 * Created by Kyle Flynn on 11/28/2017.
 * Updated For 2018 Scoring System by Soren Zaiser
 * Starting on Oct 23, 2018
 */
public class DataSyncController implements Initializable {

    /* Save file extension */
    private static final String SAVE_FILE_EXTENSION = ".txt";
    /* Default output file */
    private String saveFileName = "Settings" + SAVE_FILE_EXTENSION;

    /* This is our main tab pane */
    @FXML public TabPane tabPaneMain;
    /* This is the left-side of the setup tab. */
    @FXML public Tab tabSetup;
    @FXML public TextField txtSetupKey;
    @FXML public TextField txtSetupID;
    @FXML public Button btnSetupTest;
    @FXML public Label labelSetupTest;
    @FXML public Label txtConsole;

    /* This is the right-side of the setup tab. */
    @FXML public TextField txtSetupDir;
    @FXML public Button btnSetupSelect; //Used to get events from new scoring system
    @FXML public Button btnSetupTestDir; //Used to load selected event from scoring system
    @FXML public RadioButton rbNewScore;
    @FXML public ComboBox<String> cbFirstEvents;
    @FXML public Label labelSetupDir;

    /* This is for our Dashboard Tab */
    @FXML public Tab tabDashboard;
    @FXML public CheckBox cb_connectScore;
    @FXML public Button btn_cb_score;
    @FXML public CheckBox cb_teams;
    @FXML public Button btn_cb_teams;
    @FXML public CheckBox cb_matches;
    @FXML public Button btn_cb_matches;
    @FXML public CheckBox cb_autoSync;
    @FXML public Button btn_cb_autoSync;
    @FXML public CheckBox cb_replay;
    @FXML public Button btn_cb_replay;
    @FXML public CheckBox cb_awards;
    @FXML public Button btn_cb_awards;

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
    @FXML public Button btnTeamsGet;
    @FXML public Button btnTeamsPost;
    @FXML public Button btnTeamsDelete;

    /* This is for our matches tab. */
    @FXML public Tab tabMatches;
    @FXML public Button btnMatchImport;
    @FXML public Button btnMatchScheduleUpload;
    @FXML public Button btnMatchUpload;
    @FXML public Button btnMatchBrowserView;
    @FXML public Button btnMatchOpen;
    @FXML public Button btnSetUrl;
    @FXML public TableView<MatchGeneral> tableMatches;
    @FXML public TableColumn<MatchGeneral, String> colMatchName;
    @FXML public TableColumn<MatchGeneral, Boolean> colMatchDone;
    @FXML public TableColumn<MatchGeneral, Boolean> colMatchPosted;
    @FXML public TextField txtVideoUrl;
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
    @FXML public Label labelVideoUrl;
    @FXML public Label labelCommitTime;

    /* This is for our rankings tab. */
    @FXML public Tab tabRankings;
    @FXML public TableView<TeamRanking> tableRankings;
    @FXML public TableColumn<TeamRanking, Integer> colRank;
    @FXML public TableColumn<TeamRanking, Integer> colRankTeam;
    @FXML public TableColumn<TeamRanking, Integer> colRankWins;
    @FXML public TableColumn<TeamRanking, Integer> colRankLosses;
    @FXML public TableColumn<TeamRanking, Integer> colRankTies;
    @FXML public TableColumn<TeamRanking, Integer> colRankRP;
    @FXML public TableColumn<TeamRanking, Integer> colRankTBP;
    @FXML public TableColumn<TeamRanking, Integer> colRankScore;
    @FXML public TableColumn<TeamRanking, Integer> colRankPlayed;
    @FXML public Button btnRankUpload;

    /* This is for our alliances tab */
    @FXML public Tab tabAllianceSelection;
    @FXML public Label labelRedFinals;
    @FXML public Label labelBlueFinals;
    @FXML public Label labelRedFirstSemis;
    @FXML public Label labelBlueFirstSemis;
    @FXML public Label labelRedSecondSemis;
    @FXML public Label labelBlueSecondSemis;
    @FXML public Button btnAllianceImportScoring;
    @FXML public Button btnAllianceImportTOA;
    @FXML public Button btnUploadAlliances;
    @FXML public Button btnPurgeAlliances;

    /* This is for our awards tab */
    @FXML public Tab tabAwards;
    @FXML public TableView<Award> tableAwards;
    @FXML public TableColumn<Award, String>colAwardKey;
    @FXML public TableColumn<Award, String>colAward;
    @FXML public TableColumn<Award, String>colAwardTeamKey;
    @FXML public TableColumn<Award, Boolean>colAwardIsUploaded;
    @FXML public Button btnAwardsPost;
    @FXML public Button btnAwardsDelete;
    @FXML public Button btnAwardsGet;
    @FXML public Button btnAwardsGetTOA;

    /* This is for our advancement tab */
    @FXML public Tab tabAdvancement;

    /* This is for our sync tab. */
    @FXML public Tab tabSync;
    @FXML public Button btnSyncStart;
    @FXML public Button btnSyncStop;
    @FXML public CheckBox btnSyncMatches;

    /* Instances of our tab controllers. */
    public TeamsController teamsController;
    private MatchesController matchesController;
    private RankingsController rankingsController;
    private SyncController syncController;
    private AlliancesController alliancesController;
    private AwardsController awardsController;
    private DashboardController dashboardController;

    public String[] arguments = DataSync.arguments;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.teamsController = new TeamsController(this);
        this.matchesController = new MatchesController(this);
        this.rankingsController = new RankingsController(this);
        this.syncController = new SyncController(this);
        this.alliancesController = new AlliancesController(this);
        this.awardsController = new AwardsController(this);
        this.dashboardController = new DashboardController(this);

        labelSetupTest.setTextFill(Color.RED);
        labelSetupDir.setTextFill(Color.RED);


        if(!Config.BETA_TESTING){

            txtSetupDir.setEditable(false);
            btnSetupSelect.setDisable(true);
            btnSetupTestDir.setDisable(true);

        }else{

            enableAllWindows();

        }

        /* Arguments
         *  -skip-config : Skip Dialog asking for config file
         *  -event-key=<event_key> : Event Key
         *  -api-key=<api_key> : API Key
         *  -scoring-ip=<scoring_ip> : IP address for scoring system
         *  -scoring-event=<first_event_key> : Event Key from FIRST scoring system
         *  -test-toa : attempt connection to TOA with given credentials
         *  -test-first : attempt connection to FIRST with given data
         *  -load-first : attempt to load event from FIRST provided the IP was correct
         */

        //Parse Arguments
        boolean skipConfig = false;
        String toaEventKey = "";
        String toaApiKey = "";
        String firstIP = "";
        String firstEventKey = "";
        boolean testToa = false;
        boolean testFirst = false;
        boolean loadFirst = false;
        for(String a : this.arguments){
            if(a.equalsIgnoreCase("-skip-config")){
                skipConfig = true;
            } else if(a.toLowerCase().startsWith("-event-key=")) {
                toaEventKey = a.substring(11);
            } else if(a.toLowerCase().startsWith("-api-key=")) {
                toaApiKey = a.substring(9);
            } else if(a.toLowerCase().startsWith("-scoring-ip=")) {
                firstIP = a.substring(12);
            } else if(a.toLowerCase().startsWith("-scoring-event=")) {
                firstEventKey = a.substring(15);
            } else if(a.equalsIgnoreCase("-test-toa")) {
                testToa = true;
            } else if(a.equalsIgnoreCase("-test-first")) {
                testFirst = true;
            } else if(a.equalsIgnoreCase("-load-first")) {
                loadFirst = true;
            }
        }
        //Set Text Boxes and Check TOA if requested
        this.txtSetupID.setText(toaEventKey);
        this.txtSetupKey.setText(toaApiKey);
        if(testToa){
            this.testConnection();
        }
        this.txtSetupDir.setText(firstIP);
        if(testToa && testFirst){//  TODO: Add in check for TOA connection successful
            openScoringDialog();

            int i = 0;
            for(String s : cbFirstEvents.getItems()){ //Select the event which was given in the flag
                if(s.toLowerCase().startsWith(firstEventKey.toLowerCase())){
                    cbFirstEvents.getSelectionModel().select(i);
                    if(loadFirst) {
                        this.testDirectory();
                    }
                    break;
                } else {
                    labelSetupDir.setText("Event from launch argument not found");
                    labelSetupDir.setTextFill(Color.RED);
                }
                i++;
            }

        } else {
            labelSetupDir.setText("No TOA connection, did not check FIRST");
            labelSetupDir.setTextFill(Color.RED);
        }



        if(!skipConfig){
            readSettings();
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
                    Config.TOA_API_KEY = txtSetupKey.getText();
                    Config.EVENT_ID = txtSetupID.getText();

                    EventJSON[] events = testConnection.getGson().fromJson(response, EventJSON[].class);
                    //TODO: Fix Events Returning False Dual Division Info (DATA ISSUE)
                    if (false && events.length > 0 && events[0] != null) {
                        if (false && events[0].getDivisionName() != null || events[0].getDivisionKey() > 0) {
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

                    teamsController.getTeamsByURL();
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
        if (!rbNewScore.isSelected()) {

        } else {
            //New Scoring (ooooo fancy)
            //We want to contact the IP address and GET the events if the IP is correct. If it isn't... The Ip is wrong.
            if (txtSetupDir.getText().length() > 1) {
                Config.FIRST_API_IP = txtSetupDir.getText();
                Events events = null;
                try {
                    events = FIRSTEndpointNonLambda.getGson().fromJson(FIRSTEndpointNonLambda.getResp("events/"), Events.class);
                } catch (Exception e) {
                    sendError("Connection to FIRST Scoring system unsuccessful. ");
                    cbFirstEvents.setDisable(true);
                    btnSetupTestDir.setDisable(true);
                }
                if(events != null) {
                    cbFirstEvents.getItems().clear();
                    sendInfo("Successfully pulled events from FIRST scoring system.");

                    for (String eventName : events.getEventID()) {
                        Event event = null;
                        try{
                            event = FIRSTEndpointNonLambda.getGson().fromJson(FIRSTEndpointNonLambda.getResp("events/" + eventName), Event.class);
                        } catch (Exception e) {
                            sendError("Connection to FIRST Scoring system unsuccessful. ");
                            cbFirstEvents.setDisable(true);
                            btnSetupTestDir.setDisable(true);
                        }
                        if(event != null){
                            cbFirstEvents.getItems().add(event.getEventCode() + " | " + event.getEventName());
                            cbFirstEvents.getSelectionModel().selectFirst();
                        }
                    }

                    if (events.getEventID().length == 0) {
                        //Set Success but 0 events Text
                        labelSetupDir.setText("Found Zero Events. Please create an event within the scoring system and try again.");
                        labelSetupDir.setTextFill(Color.YELLOW);
                        //Disable input devices
                        cbFirstEvents.setDisable(true);
                        btnSetupTestDir.setDisable(true);
                    } else {
                        //Set Success Text
                        labelSetupDir.setText("Connection Successful. Found " + events.getEventID().length + " event(s).");
                        labelSetupDir.setTextFill(Color.GREEN);
                        //Enable Input Devices
                        cbFirstEvents.setDisable(false);
                        cbFirstEvents.getSelectionModel().selectFirst();
                        btnSetupTestDir.setDisable(false);
                        this.saveSettings();
                    }

                } else {
                    sendError("Connection to FIRST Scoring system unsuccessful. ");
                    cbFirstEvents.setDisable(true);
                    btnSetupTestDir.setDisable(true);
                }
            } else {
                //No IP address entered
                labelSetupDir.setText("Please Enter an IP Address");
                labelSetupDir.setTextFill(Color.RED);
            }
        }

    }

    @FXML
    public void testDirectory() {
        if(rbNewScore.isSelected()){
            //2018 Scoring System
            loadEventFromFIRST();
        } else {
            //Archive Scoring File
            testDirOldScoreing();
        }
    }

    @FXML
    public void setVideoUrl(){
        this.matchesController.setSelectedMatchVideo();
    }

    @FXML
    public void btnGoConnect(){
        //This should never get pressed
    }

    @FXML
    public void btnGoTeams(){
        tabPaneMain.getSelectionModel().select(2);
        generateButtonFlash(btnTeamsPost).play();
        generateButtonFlash(btnTeamsGet).play();
    }

    @FXML
    public void btnGoMatches(){
        tabPaneMain.getSelectionModel().select(3);
        generateButtonFlash(btnMatchImport).play();
        generateButtonFlash(btnMatchScheduleUpload).play();
    }

    @FXML
    public void btnGoSync(){
        tabPaneMain.getSelectionModel().select(8);
        generateButtonFlash(btnSyncStart).play();
    }

    @FXML
    public void btnGoReplay(){
        tabPaneMain.getSelectionModel().select(3);
        generateButtonFlash(btnMatchUpload).play();
    }

    @FXML
    public void btnGoAwards(){
        tabPaneMain.getSelectionModel().select(6);
        generateButtonFlash(btnAwardsGet).play();
        generateButtonFlash(btnAwardsPost).play();
    }

    @FXML
    public void cbReplayed(){
        if(cb_replay.isSelected()){
            cb_replay.setTextFill(Color.GREEN);
        } else {
            cb_replay.setTextFill(Color.RED);
        }
    }

    private Timeline generateButtonFlash(Button button){
        Timeline flasher = new Timeline(
                new KeyFrame(Duration.seconds(0.5), e -> {
                    button.setDefaultButton(true);
                }),

                new KeyFrame(Duration.seconds(1.0), e -> {
                    button.setDefaultButton(false);
                }),

                new KeyFrame(Duration.seconds(1.5), e -> {
                    button.setDefaultButton(true);
                }),

                new KeyFrame(Duration.seconds(2.0), e -> {
                    button.setDefaultButton(false);
                }),

                new KeyFrame(Duration.seconds(2.5), e -> {
                    button.setDefaultButton(true);
                }),

                new KeyFrame(Duration.seconds(3.0), e -> {
                    button.setDefaultButton(false);
                })


        );
        return flasher;
    }

    private void loadEventFromFIRST() {
        if (cbFirstEvents.getSelectionModel() != null) {
            String[] eventID = cbFirstEvents.getSelectionModel().getSelectedItem().split("\\|");
            FIRSTEndpoint firstEventData = new FIRSTEndpoint("events/" + eventID[0].substring(0,eventID[0].length() - 1));//remove space at end
            firstEventData.execute(((response, success) -> {
                if (success) {
                    sendInfo("Successfully pulled event info from FIRST scoring system.");

                    //TODO: Fix Division Stuff When Scorekeeping App Is Updated!!!!
                    Event eventData = firstEventData.getGson().fromJson(response, Event.class);
                    Config.DIVISION_NAME = eventData.getEventDivisionId() + "";
                    Config.FIRST_API_EVENT_ID = eventID[0].substring(0,eventID[0].length() - 1);

                    labelSetupDir.setTextFill(Color.GREEN);
                    labelSetupDir.setText("Loaded Event Successfully");

                    sendInfo("Found division id " + Config.DIVISION_NAME);
                    tabTeams.setDisable(false);
                    tabMatches.setDisable(false);
                    tabRankings.setDisable(false);
                    tabSync.setDisable(false);
                    tabAllianceSelection.setDisable(false);
                    tabAwards.setDisable(false);
                    tabDashboard.setDisable(false);
                    tabPaneMain.getSelectionModel().select(1);

                } else {
                    sendError("Connection to FIRST Scoring system unsuccessful. " + response);
                    cbFirstEvents.setDisable(true);
                    btnSetupTestDir.setDisable(true);
                }
            }));
        } else {
            labelSetupDir.setTextFill(Color.RED);
            labelSetupDir.setText("Please select an event");
        }
    }

    private void testDirOldScoreing() {
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
                tabAllianceSelection.setDisable(false);
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

    /* Teams Tab Functions */

    @FXML
    public void getTeamsByURL() {
        this.teamsController.getTeamsByURL();
    }

    @FXML
    public void getTeamsByFile() {
        if(rbNewScore.isSelected()) {
            this.teamsController.getTeamsFromFIRSTApi();
        } else {
            this.teamsController.getTeamsByFile();
        }

    }

    @FXML
    public void postEventTeams() {
        this.teamsController.postEventTeamsAskUser();
    }

    @FXML
    public void deleteEventTeams() {
        this.teamsController.deleteEventTeamsAskUser();
    }

    /* Matches Tab Functions */

    @FXML
    public void getMatchesByFile() {
        if(rbNewScore.isSelected()){
            this.matchesController.getMatchesFromFIRSTApi1819();
        } else{
            this.matchesController.getMatchesByFile1718();
        }

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
    public void viewFromTOA(){
        this.matchesController.viewFromTOA();
    }

    @FXML
    public void openMatchDetails() {
        this.matchesController.openMatchDetails();
    }

    @FXML
    public void deleteMatches(){
        this.matchesController.deleteMatches();
    }

    /* Sync Tab Functions */

    @FXML
    public void startAutoSync() {
        DataSync.getMainStage().setOnCloseRequest(closeEvent -> this.syncController.kill());
        this.syncController.execute((count) -> {
            Platform.runLater(() -> {
                Date date = new Date();
                TOALogger.log(Level.INFO, "Executing update #" + count + " at " + DateFormat.getTimeInstance(DateFormat.SHORT).format(date));
                TOALogger.log(Level.INFO, "There are " + Thread.activeCount() + " threads.");

                if (this.btnSyncMatches.selectedProperty().get()) {
                    this.matchesController.getMatchesFromFIRSTApi1819();
                    this.matchesController.syncMatches();
                }
                this.matchesController.checkMatchSchedule(); //Gets Matches and updates display
                this.matchesController.checkMatchDetails(); //Gets MAtch Details
                this.matchesController.checkMatchParticipants();// Gets Match Participants
                this.rankingsController.getRankingsFIRSTApi();
                this.rankingsController.postRankings();
                /*if (this.btnSyncMatches.selectedProperty().get()) {
                     this.matchesController.postCompletedMatches();
                }*/
            });
        });
    }

    @FXML
    public void stopAutoSync() {
        this.syncController.kill();
    }

    /* Rankings Tab Functions */

    @FXML
    public void getRankingsByFile() {
        if(rbNewScore.isSelected()) {
            this.rankingsController.getRankingsFIRSTApi();
        } else {
            this.rankingsController.getRankingsByFile();
        }

    }

    @FXML
    public void postRankings() {
        this.rankingsController.postRankings();
    }

    @FXML
    public void deleteRankings() {
        this.rankingsController.deleteRankings();
    }

    /* Alliances Tab Functions */

    @FXML
    public void importAlliancesScoring(){
        if(rbNewScore.isSelected()){
            this.alliancesController.importAlliancesFIRSTApi(this.matchesController.getMatchDetails());
        } else {
            //this.alliancesController.importAlliancesScoring(this.matchesController.getMatchDetails());
        }
    }

    @FXML
    public void importAlliancesTOA(){
        this.alliancesController.importAlliancesTOA(this.matchesController.getMatchDetails());
    }

    @FXML
    public void uploadAlliances(){
        this.alliancesController.uploadAlliances();
    }

    @FXML
    public void purgeAlliances(){
        this.alliancesController.purgeAlliances();
    }

    @FXML
    public void uploadAwards() {
        this.awardsController.uploadAwards();
    }

    @FXML
    public void getAwardsFIRST() {
        this.awardsController.getAwardsFIRST();
    }

    @FXML
    public void getAwardsTOA() {
        this.awardsController.loadToaAwards();
    }

    @FXML
    public void purgeAwards() {
        this.awardsController.purgeAwards();
    }

    public HashMap<Integer, int[]> getTeamWL() {
        return this.matchesController.getTeamWL();
    }

    /* Outputs the current settings to the file specified by saveFileName (Default Settings.SAVE_FILE_EXTENSION) */
    private void saveSettings(){

        try (PrintWriter out = new PrintWriter(saveFileName)){

            out.println("Key:" + txtSetupKey.getText());
            out.println("ID:" + txtSetupID.getText());
            out.println("Directory:" + txtSetupDir.getText());

            out.close();

        }catch (FileNotFoundException e){

            TOALogger.log(Level.WARNING, "Error saving settings");

        }

    }

    /* Searches for files in the active directory with the proper extension, and then prompts the user for which one */
    private void readSettings(){

        //Check for saved settings
        try {
            ArrayList<String> files = new ArrayList<>();
            //Looks for files
            for(File file : new File(System.getProperty("user.dir")).listFiles()) {
                String name = file.getName();
                if (file.isFile() //Checks that the file is not a directory
                        && name.substring(name.length() - SAVE_FILE_EXTENSION.length(), name.length()).equals(SAVE_FILE_EXTENSION)) { //Ends with proper file type
                    files.add(name);
                }
            }

            //If it found any files, will prompt the user for which one
            if(files.size() > 0) {

                String file = promptForSaveFile(files);

                if(file != null){

                    saveFileName = file;

                    Scanner scan = new Scanner(new File(saveFileName));
                    while (scan.hasNextLine()) {
                        String[] line = scan.nextLine().split(":", 2);

                        if (line.length > 1) {
                            if (line[0].equalsIgnoreCase("Key")) {

                                txtSetupKey.setText(line[1]);

                            } else if (line[0].equalsIgnoreCase("ID")) {

                                txtSetupID.setText(line[1]);

                            } else if (line[0].equalsIgnoreCase("Directory")) {

                                txtSetupDir.setText(line[1]);
                                txtSetupDir.setEditable(false);

                            }
                        }
                    }
                }
            }

        } catch (FileNotFoundException e){
            /* Error opening file, continue with defaults */
        }
    }

    /* Prompts the user to select a file from the list, or run without a file */
    private String promptForSaveFile(ArrayList<String> files){
        //Create Dialog With list of Files
        Dialog dialog = new ChoiceDialog(files.get(0), files);
        //Set Title
        dialog.setTitle("Select Settings");
        //Set Header Text
        dialog.setHeaderText("Select Settings File");
        //Set Icon because it shows in the task bar
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/app_ico.png")));
        //Show Dialog
        Optional result = dialog.showAndWait();
        //Collect and Return Result
        return (String) result.orElse(null);
    }

    /* Enables all windows, only used when BETA testing */
    private void enableAllWindows(){

        tabAdvancement.setDisable(false);
        tabAllianceSelection.setDisable(false);
        tabAwards.setDisable(false);
        tabMatches.setDisable(false);
        tabRankings.setDisable(false);
        tabSetup.setDisable(false);
        tabSync.setDisable(false);
        tabTeams.setDisable(false);

    }
}
