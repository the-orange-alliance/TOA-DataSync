package org.theorangealliance.datasync.tabs;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import org.theorangealliance.datasync.DataSyncController;
import org.theorangealliance.datasync.json.MatchScheduleGeneralJSON;
import org.theorangealliance.datasync.json.MatchScheduleStationJSON;
import org.theorangealliance.datasync.models.MatchGeneral;
import org.theorangealliance.datasync.models.ScheduleStation;
import org.theorangealliance.datasync.util.Config;
import org.theorangealliance.datasync.util.TOAEndpoint;
import org.theorangealliance.datasync.util.TOARequestBody;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;

/**
 * Created by Kyle Flynn on 11/29/2017.
 */
public class MatchesController {

    private DataSyncController controller;
    private ObservableList<MatchGeneral> matchList;
    private MatchGeneral[] uploadedMatches;

    private HashMap<MatchGeneral, ScheduleStation[]> matchStations;

    public MatchesController(DataSyncController instance) {
        this.controller = instance;

        this.controller.colMatchName.setCellValueFactory(new PropertyValueFactory<>("matchName"));
        this.controller.colMatchDone.setCellValueFactory(cellData -> new SimpleBooleanProperty(cellData.getValue().isDone()));
        this.controller.colMatchPosted.setCellValueFactory(cellData -> new SimpleBooleanProperty(cellData.getValue().isUploaded()));
        this.controller.colMatchDone.setCellFactory(col -> new TableCell<MatchGeneral, Boolean>() {
            @Override
            protected void updateItem(Boolean done, boolean empty) {
                if (!empty && done != null) {
                    if (done) {
                        setTextFill(Color.GREEN);
                        setText("YES");
                    } else {
                        setTextFill(Color.RED);
                        setText("NO");
                    }
                }
            }
        });
        this.controller.colMatchPosted.setCellFactory(col -> new TableCell<MatchGeneral, Boolean>() {
            @Override
            protected void updateItem(Boolean done, boolean empty) {
                if (!empty && done != null) {
                    if (done) {
                        setTextFill(Color.GREEN);
                        setText("YES");
                    } else {
                        setTextFill(Color.RED);
                        setText("NO");
                    }
                }
            }
        });

        this.matchStations = new HashMap<>();
        this.matchList = FXCollections.observableArrayList();
        this.controller.tableMatches.setItems(this.matchList);
        this.controller.tableMatches.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
            openMatchView(newValue);
        }));

        this.controller.btnMatchScheduleUpload.setDisable(true);
        this.controller.btnMatchUpload.setVisible(false);
        this.controller.btnMatchUpload.setDisable(true);
        this.controller.btnMatchSync.setVisible(false);
        this.controller.btnMatchSync.setDisable(true);
    }

    private void openMatchView(MatchGeneral match) {
        if (match.isDone()) {
            controller.btnMatchUpload.setDisable(false);
        } else {
            controller.btnMatchUpload.setDisable(true);
        }
        ScheduleStation[] teams = matchStations.get(match);
        if (teams != null) {
            String redTeams = teams[0].getTeamKey() + " " + teams[1].getTeamKey();
            String redFinalTeam = (teams[2].getTeamKey() == 0 ? "" : teams[2].getTeamKey() + "");
            String blueTeams = teams[3].getTeamKey() + " " + teams[4].getTeamKey();
            String blueFinalTeam = (teams[5].getTeamKey() == 0 ? "" : teams[5].getTeamKey() + "");
            controller.labelRedTeams.setText(redTeams + " " + redFinalTeam);
            controller.labelBlueTeams.setText(blueTeams + " " + blueFinalTeam);
        }

        controller.labelRedAuto.setUnderline(match.getRedAutoScore() >= match.getBlueAutoScore());
        controller.labelBlueAuto.setUnderline(match.getRedAutoScore() <= match.getBlueAutoScore());
        controller.labelRedAuto.setText("" + match.getRedAutoScore());
        controller.labelBlueAuto.setText("" + match.getBlueAutoScore());

        controller.labelRedTele.setUnderline(match.getRedTeleScore() >= match.getBlueTeleScore());
        controller.labelBlueTele.setUnderline(match.getRedTeleScore() <= match.getBlueTeleScore());
        controller.labelRedTele.setText("" + match.getRedTeleScore());
        controller.labelBlueTele.setText("" + match.getBlueTeleScore());

        controller.labelRedEnd.setUnderline(match.getRedEndScore() >= match.getBlueEndScore());
        controller.labelBlueEnd.setUnderline(match.getRedEndScore() <= match.getBlueEndScore());
        controller.labelRedEnd.setText("" + match.getRedEndScore());
        controller.labelBlueEnd.setText("" + match.getBlueEndScore());

        controller.labelRedPenalty.setUnderline(match.getRedPenalty() >= match.getBluePenalty());
        controller.labelBluePenalty.setUnderline(match.getRedPenalty() <= match.getBluePenalty());
        controller.labelRedPenalty.setText("" + match.getRedPenalty());
        controller.labelBluePenalty.setText("" + match.getBluePenalty());

        controller.labelRedScore.setUnderline(match.getRedScore() >= match.getBlueScore());
        controller.labelBlueScore.setUnderline(match.getRedScore() <= match.getBlueScore());
        controller.labelRedScore.setText("" + match.getRedScore());
        controller.labelBlueScore.setText("" + match.getBlueScore());
    }

    private void updateMatchDetails() {

    }

    public void checkMatchSchedule() {
        this.controller.labelScheduleUploaded.setTextFill(Color.YELLOW);
        this.controller.labelScheduleUploaded.setText("Checking TOA...");
        TOAEndpoint matchesEndpoint = new TOAEndpoint("GET", "event/" + Config.EVENT_ID + "/matches");
        matchesEndpoint.setCredentials(Config.EVENT_API_KEY, Config.EVENT_ID);
        matchesEndpoint.execute(((response, success) -> {
            if (success) {
                uploadedMatches = matchesEndpoint.getGson().fromJson(response, MatchGeneral[].class);
                if (uploadedMatches.length > 0) {
                    this.controller.labelScheduleUploaded.setTextFill(Color.GREEN);
                    this.controller.labelScheduleUploaded.setText("Schedule Already Posted");
                    this.controller.sendInfo("Match Schedule Already Posted");
                } else {
                    this.controller.labelScheduleUploaded.setTextFill(Color.GREEN);
                    this.controller.labelScheduleUploaded.setText("Schedule NOT Posted");
                    this.controller.sendInfo("Match Schedule NOT Posted");
                }
            } else {
                this.controller.sendError("Error: " + response);
            }
        }));
    }

    public void getMatchesByFile() {
        File matchFile = new File(Config.SCORING_DIR + "\\matches.txt");
        if (matchFile.exists()) {
            try {
                matchList.clear();
                BufferedReader reader = new BufferedReader(new FileReader(matchFile));
                String line;
                while ((line = reader.readLine()) != null) {
                    /** MATCH info */
                    String[] matchInfo = line.split("\\|\\|")[0].split("\\|");
                    int tournamentLevel = Integer.parseInt(matchInfo[1]);
                    int matchNumber = Integer.parseInt(matchInfo[2]);
                    int fieldNumber = matchNumber % 2 == 0 ? 2 : 1;
                    int playNumber = Integer.parseInt(matchInfo[3]);
                    MatchGeneral match = new MatchGeneral(
                            MatchGeneral.buildMatchName(tournamentLevel, matchNumber),
                            MatchGeneral.buildTOATournamentLevel(tournamentLevel, matchNumber),
                            null,
                            fieldNumber);
                    char qualChar = match.getMatchName().contains("Qual") ? 'Q' : 'E';
                    match.setMatchKey(Config.EVENT_ID + "-" + qualChar + String.format("%03d", match.getCanonicalMatchNumber()) + "-1");

                    if (playNumber > 0) {
                        match.setIsDone(true);
                    }

                    matchList.add(match);

                    /** TEAM info */
                    String[] teamInfo = line.split("\\|\\|")[1].split("\\|");
                    ScheduleStation[] scheduleStations = new ScheduleStation[6];
                    scheduleStations[0] = new ScheduleStation(match.getMatchKey(), 11, Integer.parseInt(teamInfo[0]));
                    scheduleStations[1] = new ScheduleStation(match.getMatchKey(), 12, Integer.parseInt(teamInfo[1]));
                    scheduleStations[2] = new ScheduleStation(match.getMatchKey(), 13, Integer.parseInt(teamInfo[2]));
                    scheduleStations[3] = new ScheduleStation(match.getMatchKey(), 21, Integer.parseInt(teamInfo[3]));
                    scheduleStations[4] = new ScheduleStation(match.getMatchKey(), 22, Integer.parseInt(teamInfo[4]));
                    scheduleStations[5] = new ScheduleStation(match.getMatchKey(), 23, Integer.parseInt(teamInfo[5]));
                    matchStations.put(match, scheduleStations);
                }
                reader.close();
                controller.btnMatchScheduleUpload.setDisable(false);
                if (uploadedMatches != null) {
                    if (uploadedMatches.length == matchList.size()) {
                        controller.sendInfo("Successfully imported " + matchList.size() + " matches from the Scoring System. Online and local schedules are the same.");
                    } else {
                        controller.sendError("Successfully imported " + matchList.size() + " matches from the Scoring System. However, uploaded schedule and local schedule differ.");
                    }
                    this.controller.btnMatchUpload.setVisible(true);
                    this.controller.btnMatchSync.setVisible(true);
                }

            } catch (Exception e) {
                e.printStackTrace();
                controller.sendError("Could not open file. " + e.getLocalizedMessage());
            }
        } else {
            controller.sendError("Could not locate matches.txt from the Scoring System. Did you generate a match schedule?");
        }
    }

    public void postMatchSchedule() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Are you sure about this?");
        alert.setHeaderText("This operation cannot be undone.");
        alert.setContentText("You are about to POST " + matchList.size() + " matches to TOA's databases. This schedule will be made available to the public. Make sure this schedule is correct before you upload.");

        ButtonType okayButton = new ButtonType("Upload Matches");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(okayButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == okayButton) {
            postMatchScheduleMatches();
            postMatchScheduleTeams();

            this.controller.btnMatchUpload.setVisible(true);
            this.controller.btnMatchSync.setVisible(true);
        }
    }

    private void postMatchScheduleTeams() {
        controller.sendInfo("Uploading station schedule data from event " + Config.EVENT_ID + "...");
        TOAEndpoint scheduleEndpoint = new TOAEndpoint("POST", "upload/event/schedule/teams");
        scheduleEndpoint.setCredentials(Config.EVENT_API_KEY, Config.EVENT_ID);
        TOARequestBody requestBody = new TOARequestBody();
        requestBody.setEventKey(Config.EVENT_ID);
        for (ScheduleStation[] stations : matchStations.values()) {
            for (ScheduleStation station : stations) {
                if (station.getTeamKey() != 0) {
                    MatchScheduleStationJSON stationJSON = new MatchScheduleStationJSON();
                    stationJSON.setMatchKey(station.getMatchKey());
                    stationJSON.setStation(station.getStation());
                    stationJSON.setStationKey(station.getStationKey());
                    stationJSON.setTeamKey(station.getTeamKey());
                    stationJSON.setStationStatus(1);
                    requestBody.addValue(stationJSON);
                }
            }
        }
        scheduleEndpoint.setBody(requestBody);
        scheduleEndpoint.execute(((response, success) -> {
            if (success) {
                controller.sendInfo("Successfully uploaded data to TOA. " + response);
            } else {
                controller.sendError("Connection to TOA unsuccessful. " + response);
            }
        }));
    }

    private void postMatchScheduleMatches() {
        controller.sendInfo("Uploading match schedule data from event " + Config.EVENT_ID + "...");
        TOAEndpoint scheduleEndpoint = new TOAEndpoint("POST", "upload/event/schedule/matches");
        scheduleEndpoint.setCredentials(Config.EVENT_API_KEY, Config.EVENT_ID);
        TOARequestBody requestBody = new TOARequestBody();
        requestBody.setEventKey(Config.EVENT_ID);
        for (int i = 0; i < matchList.size(); i++) {
            MatchGeneral match = matchList.get(i);
            MatchScheduleGeneralJSON matchJSON = new MatchScheduleGeneralJSON();
            matchJSON.setEventKey(Config.EVENT_ID);
            matchJSON.setMatchKey(match.getMatchKey());
            matchJSON.setTournamentLevel(match.getTournamentLevel());
            matchJSON.setMatchName(match.getMatchName());
            matchJSON.setPlayNumber(0);
            matchJSON.setFieldNumber(match.getFieldNumber());
            matchJSON.setCreatedBy("TOA-DataSync");
            matchJSON.setCreatedOn(getCurrentTime());
            requestBody.addValue(matchJSON);
        }
        scheduleEndpoint.setBody(requestBody);
        scheduleEndpoint.execute(((response, success) -> {
            if (success) {
                controller.sendInfo("Successfully uploaded data to TOA. " + response);
            } else {
                controller.sendError("Connection to TOA unsuccessful. " + response);
            }
        }));
    }

    private String getCurrentTime() {
        Date dt = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        return sdf.format(dt);
    }

}

