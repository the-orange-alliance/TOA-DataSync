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
import org.theorangealliance.datasync.json.MatchDetailRelicJSON;
import org.theorangealliance.datasync.json.MatchGeneralJSON;
import org.theorangealliance.datasync.json.MatchScheduleGeneralJSON;
import org.theorangealliance.datasync.json.MatchScheduleStationJSON;
import org.theorangealliance.datasync.models.MatchGeneral;
import org.theorangealliance.datasync.models.ScheduleStation;
import org.theorangealliance.datasync.util.Config;
import org.theorangealliance.datasync.util.TOAEndpoint;
import org.theorangealliance.datasync.util.TOARequestBody;

import javax.jnlp.IntegrationService;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Kyle Flynn on 11/29/2017.
 */
public class MatchesController {

    private DataSyncController controller;
    private ObservableList<MatchGeneral> matchList;
    private HashSet<MatchGeneralJSON> uploadedMatches;
    private HashSet<MatchDetailRelicJSON> uploadedDetails;

    private HashMap<MatchGeneral, ScheduleStation[]> matchStations;
    private HashMap<MatchGeneral, MatchDetailRelicJSON> matchDetails;
    private MatchGeneral selectedMatch;

    private HashMap<Integer, int[]> teamWinLoss;

    private Queue<MatchGeneral> uploadQueue;

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

        this.uploadQueue = new LinkedList<>();
        this.matchStations = new HashMap<>();
        this.matchDetails = new HashMap<>();
        this.teamWinLoss = new HashMap<>();
        this.matchList = FXCollections.observableArrayList();
        this.controller.tableMatches.setItems(this.matchList);
        this.controller.tableMatches.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue != null) {
                selectedMatch = newValue;
                openMatchView(newValue);
            } else if (oldValue != null) {
                selectedMatch = oldValue;
            } else {
                selectedMatch = null;
            }
        }));

        this.controller.btnMatchScheduleUpload.setDisable(true);
        this.controller.btnMatchUpload.setVisible(false);
        this.controller.btnMatchUpload.setDisable(true);
        this.controller.btnMatchSync.setVisible(false);
        this.controller.btnMatchSync.setDisable(true);
        this.controller.btnMatchOpen.setVisible(false);
        this.controller.btnMatchOpen.setDisable(true);
    }

    private void openMatchView(MatchGeneral match) {
        controller.btnMatchOpen.setDisable(false);
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

        controller.labelMatchLevel.setText("Level: " + match.getTournamentLevel());
        controller.labelMatchField.setText("Field: " + match.getFieldNumber());
        controller.labelMatchPlay.setText("Play: " + match.getPlayNumber());
        controller.labelMatchName.setText(match.getMatchName());
        controller.labelMatchKey.setText(match.getMatchKey());

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

    public void openMatchDetails() {
        // TODO - Actually make
    }

    public void checkMatchSchedule() {
        this.controller.labelScheduleUploaded.setTextFill(Color.YELLOW);
        this.controller.labelScheduleUploaded.setText("Checking TOA...");
        TOAEndpoint matchesEndpoint = new TOAEndpoint("GET", "event/" + Config.EVENT_ID + "/matches");
        matchesEndpoint.setCredentials(Config.EVENT_API_KEY, Config.EVENT_ID);
        matchesEndpoint.execute(((response, success) -> {
            if (success) {
                uploadedMatches = new HashSet<>(Arrays.asList(matchesEndpoint.getGson().fromJson(response, MatchGeneralJSON[].class)));
                if (uploadedMatches.size() > 0) {
                    this.controller.labelScheduleUploaded.setTextFill(Color.GREEN);
                    this.controller.labelScheduleUploaded.setText("Schedule Already Posted");
//                    this.controller.sendInfo("Match Schedule Already Posted");
                } else {
                    this.controller.labelScheduleUploaded.setTextFill(Color.GREEN);
                    this.controller.labelScheduleUploaded.setText("Schedule NOT Posted");
//                    this.controller.sendInfo("Match Schedule NOT Posted");
                }
            } else {
                this.controller.sendError("Error: " + response);
            }
        }));
    }

    public void checkMatchDetails() {
        TOAEndpoint matchesEndpoint = new TOAEndpoint("GET", "event/" + Config.EVENT_ID + "/matches/details");
        matchesEndpoint.setCredentials(Config.EVENT_API_KEY, Config.EVENT_ID);
        matchesEndpoint.execute(((response, success) -> {
            if (success) {
                uploadedDetails = new HashSet<>(Arrays.asList(matchesEndpoint.getGson().fromJson(response, MatchDetailRelicJSON[].class)));
            } else {
                this.controller.sendError("Error: " + response);
            }
        }));
    }

    private void calculateWL(MatchGeneral match, ScheduleStation[] stations) {
        if (match.isDone()) {
            boolean redWin = match.getRedScore() > match.getBlueScore();
            boolean tie = match.getRedScore() == match.getBlueScore();
            String resultStr = tie ? "TIE" : redWin ? "RED" : "BLUE";
//            System.out.println(match.getMatchKey() + ": "  + resultStr);
            for (ScheduleStation station : stations) {
                if (station.getTeamKey() != 0) {
                    int WINS = 0;
                    int LOSS = 1;
                    int TIES = 2;
                    int[] result = teamWinLoss.get(station.getTeamKey());
                    if (result != null) {
                        if (tie) {
                            result[TIES]++;
                        } else if (redWin && station.getStation() < 20) {
                            // Red win and this team is on red
                            result[WINS]++;
                        } else if (!redWin && station.getStation() > 20) {
                            result[WINS]++;
                        } else {
                            result[LOSS]++;
                        }
                    } else {
                        result = new int[3];
                        if (tie) {
                            result[WINS] = 0;
                            result[LOSS] = 0;
                            result[TIES] = 1;
                        } else if (redWin && station.getStation() < 20) {
                            // Red win and this team is on red
                            result[WINS] = 1;
                            result[LOSS] = 0;
                            result[TIES] = 0;
                        } else if (!redWin && station.getStation() > 20) {
                            result[WINS]++;
                        } else {
                            result[WINS] = 0;
                            result[LOSS] = 1;
                            result[TIES] = 0;
                        }
                    }
                    teamWinLoss.put(station.getTeamKey(), result);
                }
            }
        }
    }

    public void syncMatches() {
        if (matchList.size() <= 0) {
            getMatchesByFile();
        } else {
            File matchFile = new File(Config.SCORING_DIR + "\\matches.txt");
            if (matchFile.exists()) {
                try {
                    uploadQueue.clear();
                    teamWinLoss.clear();
                    BufferedReader reader = new BufferedReader(new FileReader(matchFile));
                    String line;
                    int count = 0;
                    while ((line = reader.readLine()) != null) {
                        MatchGeneral match = matchList.get(count);
                        String[] teamInfo = line.split("\\|\\|")[1].split("\\|");
                        // Field 24 will be whether or not score is SAVED.
                        // This is ALSO where the match details section begins.
                        int saved = Integer.parseInt(teamInfo[24]);
                        if (saved == 1) {
                            match.setPlayNumber(1);
                            match.setIsDone(true);
                        }

                        // Not very efficient, but it is what is is... I hate O(N^2) algorithms.
                        for (MatchDetailRelicJSON detail : uploadedDetails) {
                            if (detail.getMatchKey().equals(match.getMatchKey())) {
                                match.setIsUploaded(true);
                            }
                        }

                        MatchDetailRelicJSON detailJSON = matchDetails.get(match);
                        detailJSON.setRedAutoJewel(Integer.parseInt(teamInfo[25]));
                        detailJSON.setRedAutoGlyphs(Integer.parseInt(teamInfo[26]));
                        detailJSON.setRedAutoKeys(Integer.parseInt(teamInfo[27]));
                        detailJSON.setRedAutoPark(Integer.parseInt(teamInfo[28]));
                        detailJSON.setRedTeleGlyphs(Integer.parseInt(teamInfo[29]));
                        detailJSON.setRedTeleRows(Integer.parseInt(teamInfo[30]));
                        detailJSON.setRedTeleColumns(Integer.parseInt(teamInfo[31]));
                        detailJSON.setRedTeleCypher(Integer.parseInt(teamInfo[32]));
                        detailJSON.setRedEndRelic1(Integer.parseInt(teamInfo[33]));
                        detailJSON.setRedEndRelic2(Integer.parseInt(teamInfo[34]));
                        detailJSON.setRedEndRelic3(Integer.parseInt(teamInfo[35]));
                        detailJSON.setRedEndRelicUp(Integer.parseInt(teamInfo[36]));
                        detailJSON.setRedEndRobotBal(Integer.parseInt(teamInfo[37]));
                        detailJSON.setRedMinPen(Integer.parseInt(teamInfo[38]));
                        detailJSON.setRedMajPen(Integer.parseInt(teamInfo[39]));
                        detailJSON.setBlueAutoJewel(Integer.parseInt(teamInfo[42]));
                        detailJSON.setBlueAutoGlyphs(Integer.parseInt(teamInfo[43]));
                        detailJSON.setBlueAutoKeys(Integer.parseInt(teamInfo[44]));
                        detailJSON.setBlueAutoPark(Integer.parseInt(teamInfo[45]));
                        detailJSON.setBlueTeleGlyphs(Integer.parseInt(teamInfo[46]));
                        detailJSON.setBlueTeleRows(Integer.parseInt(teamInfo[47]));
                        detailJSON.setBlueTeleColumns(Integer.parseInt(teamInfo[48]));
                        detailJSON.setBlueTeleCypher(Integer.parseInt(teamInfo[49]));
                        detailJSON.setBlueEndRelic1(Integer.parseInt(teamInfo[50]));
                        detailJSON.setBlueEndRelic2(Integer.parseInt(teamInfo[51]));
                        detailJSON.setBlueEndRelic3(Integer.parseInt(teamInfo[52]));
                        detailJSON.setBlueEndRelicUp(Integer.parseInt(teamInfo[53]));
                        detailJSON.setBlueEndRobotBal(Integer.parseInt(teamInfo[54]));
                        detailJSON.setBlueMinPen(Integer.parseInt(teamInfo[55]));
                        detailJSON.setBlueMajPen(Integer.parseInt(teamInfo[56]));
                        detailJSON.setMatchKey(match.getMatchKey());
                        detailJSON.setMatchDtlKey(match.getMatchKey() + "-DTL");

                        match.setRedPenalty((detailJSON.getBlueMinPen() * 10) + (detailJSON.getBlueMajPen() * 40));
                        match.setBluePenalty((detailJSON.getRedMinPen() * 10) + (detailJSON.getRedMajPen() * 40));
                        match.setRedAutoScore((detailJSON.getRedAutoGlyphs()*15) + (detailJSON.getRedAutoPark()*10) + (detailJSON.getRedAutoKeys()*30) + (detailJSON.getRedAutoJewel()*30));
                        match.setBlueAutoScore((detailJSON.getBlueAutoGlyphs()*15) + (detailJSON.getBlueAutoPark()*10) + (detailJSON.getBlueAutoKeys()*30) + (detailJSON.getBlueAutoJewel()*30));
                        match.setRedTeleScore((detailJSON.getRedTeleGlyphs()*2) + (detailJSON.getRedTeleRows()*10) + (detailJSON.getRedTeleColumns()*20) + (detailJSON.getRedTeleCypher()*30));
                        match.setBlueTeleScore((detailJSON.getBlueTeleGlyphs()*2) + (detailJSON.getBlueTeleRows()*10) + (detailJSON.getBlueTeleColumns()*20) + (detailJSON.getBlueTeleCypher()*30));
                        match.setRedEndScore((detailJSON.getRedEndRelic1()*10) + (detailJSON.getRedEndRelic2()*20) + (detailJSON.getRedEndRelic3()*40) + (detailJSON.getRedEndRelicUp()*15) + (detailJSON.getRedEndRobotBal()*20));
                        match.setBlueEndScore((detailJSON.getBlueEndRelic1()*10) + (detailJSON.getBlueEndRelic2()*20) + (detailJSON.getBlueEndRelic3()*40) + (detailJSON.getBlueEndRelicUp()*15) + (detailJSON.getBlueEndRobotBal()*20));
                        match.setRedScore(match.getRedAutoScore()+match.getRedTeleScore()+match.getRedEndScore()+match.getRedPenalty());
                        match.setBlueScore(match.getBlueAutoScore()+match.getBlueTeleScore()+match.getBlueEndScore()+match.getBluePenalty());
                        calculateWL(match, matchStations.get(match));
                        count++;

                        if (match.isDone() && !match.isUploaded()) {
                            uploadQueue.add(match);
                        }
                    }
                    controller.tableMatches.refresh();
                    reader.close();

                    if (selectedMatch != null) {
                        selectedMatch = matchList.get(selectedMatch.getCanonicalMatchNumber()-1);
                        openMatchView(selectedMatch);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void getMatchesByFile() {
        File matchFile = new File(Config.SCORING_DIR + "\\matches.txt");
        if (matchFile.exists()) {
            try {
                controller.btnMatchUpload.setDisable(true);
                matchList.clear();
                matchStations.clear();
                matchDetails.clear();
                teamWinLoss.clear();
                BufferedReader reader = new BufferedReader(new FileReader(matchFile));
                String line;
                int elimCount = 0;
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
                    if (qualChar == 'E') {
                        elimCount++;
                        match.setFieldNumber(match.getTournamentLevel() == 4 ? 1 : (match.getTournamentLevel() == 31 ? 1 : 2));
                        match.setMatchKey(Config.EVENT_ID + "-" + qualChar + String.format("%03d", elimCount) + "-1");
                    } else {
                        match.setMatchKey(Config.EVENT_ID + "-" + qualChar + String.format("%03d", match.getCanonicalMatchNumber()) + "-1");
                    }

                    /** TEAM info */
                    String[] teamInfo = line.split("\\|\\|")[1].split("\\|");
                    ScheduleStation[] scheduleStations = new ScheduleStation[6];
                    scheduleStations[0] = new ScheduleStation(match.getMatchKey(), 11, Integer.parseInt(teamInfo[0]));
                    scheduleStations[1] = new ScheduleStation(match.getMatchKey(), 12, Integer.parseInt(teamInfo[1]));
                    scheduleStations[2] = new ScheduleStation(match.getMatchKey(), 13, Integer.parseInt(teamInfo[2]));
                    scheduleStations[3] = new ScheduleStation(match.getMatchKey(), 21, Integer.parseInt(teamInfo[3]));
                    scheduleStations[4] = new ScheduleStation(match.getMatchKey(), 22, Integer.parseInt(teamInfo[4]));
                    scheduleStations[5] = new ScheduleStation(match.getMatchKey(), 23, Integer.parseInt(teamInfo[5]));

                    // Field 24 will be whether or not score is SAVED.
                    // This is ALSO where the match details section begins.
                    int saved = Integer.parseInt(teamInfo[24]);
                    if (saved == 1) {
                        match.setPlayNumber(1);
                        match.setIsDone(true);
                    }

                    // Not very efficient, but it is what is is... I hate O(N^2) algorithms.
                    for (MatchDetailRelicJSON detail : uploadedDetails) {
                        if (detail.getMatchKey().equals(match.getMatchKey())) {
                            match.setIsUploaded(true);
                        }
                    }

                    MatchDetailRelicJSON detailJSON = new MatchDetailRelicJSON();
                    detailJSON.setRedAutoJewel(Integer.parseInt(teamInfo[25]));
                    detailJSON.setRedAutoGlyphs(Integer.parseInt(teamInfo[26]));
                    detailJSON.setRedAutoKeys(Integer.parseInt(teamInfo[27]));
                    detailJSON.setRedAutoPark(Integer.parseInt(teamInfo[28]));
                    detailJSON.setRedTeleGlyphs(Integer.parseInt(teamInfo[29]));
                    detailJSON.setRedTeleRows(Integer.parseInt(teamInfo[30]));
                    detailJSON.setRedTeleColumns(Integer.parseInt(teamInfo[31]));
                    detailJSON.setRedTeleCypher(Integer.parseInt(teamInfo[32]));
                    detailJSON.setRedEndRelic1(Integer.parseInt(teamInfo[33]));
                    detailJSON.setRedEndRelic2(Integer.parseInt(teamInfo[34]));
                    detailJSON.setRedEndRelic3(Integer.parseInt(teamInfo[35]));
                    detailJSON.setRedEndRelicUp(Integer.parseInt(teamInfo[36]));
                    detailJSON.setRedEndRobotBal(Integer.parseInt(teamInfo[37]));
                    detailJSON.setRedMinPen(Integer.parseInt(teamInfo[38]));
                    detailJSON.setRedMajPen(Integer.parseInt(teamInfo[39]));
                    detailJSON.setBlueAutoJewel(Integer.parseInt(teamInfo[42]));
                    detailJSON.setBlueAutoGlyphs(Integer.parseInt(teamInfo[43]));
                    detailJSON.setBlueAutoKeys(Integer.parseInt(teamInfo[44]));
                    detailJSON.setBlueAutoPark(Integer.parseInt(teamInfo[45]));
                    detailJSON.setBlueTeleGlyphs(Integer.parseInt(teamInfo[46]));
                    detailJSON.setBlueTeleRows(Integer.parseInt(teamInfo[47]));
                    detailJSON.setBlueTeleColumns(Integer.parseInt(teamInfo[48]));
                    detailJSON.setBlueTeleCypher(Integer.parseInt(teamInfo[49]));
                    detailJSON.setBlueEndRelic1(Integer.parseInt(teamInfo[50]));
                    detailJSON.setBlueEndRelic2(Integer.parseInt(teamInfo[51]));
                    detailJSON.setBlueEndRelic3(Integer.parseInt(teamInfo[52]));
                    detailJSON.setBlueEndRelicUp(Integer.parseInt(teamInfo[53]));
                    detailJSON.setBlueEndRobotBal(Integer.parseInt(teamInfo[54]));
                    detailJSON.setBlueMinPen(Integer.parseInt(teamInfo[55]));
                    detailJSON.setBlueMajPen(Integer.parseInt(teamInfo[56]));
                    detailJSON.setMatchKey(match.getMatchKey());
                    detailJSON.setMatchDtlKey(match.getMatchKey() + "-DTL");

                    match.setRedPenalty((detailJSON.getBlueMinPen() * 10) + (detailJSON.getBlueMajPen() * 40));
                    match.setBluePenalty((detailJSON.getRedMinPen() * 10) + (detailJSON.getRedMajPen() * 40));
                    match.setRedAutoScore((detailJSON.getRedAutoGlyphs()*15) + (detailJSON.getRedAutoPark()*10) + (detailJSON.getRedAutoKeys()*30) + (detailJSON.getRedAutoJewel()*30));
                    match.setBlueAutoScore((detailJSON.getBlueAutoGlyphs()*15) + (detailJSON.getBlueAutoPark()*10) + (detailJSON.getBlueAutoKeys()*30) + (detailJSON.getBlueAutoJewel()*30));
                    match.setRedTeleScore((detailJSON.getRedTeleGlyphs()*2) + (detailJSON.getRedTeleRows()*10) + (detailJSON.getRedTeleColumns()*20) + (detailJSON.getRedTeleCypher()*30));
                    match.setBlueTeleScore((detailJSON.getBlueTeleGlyphs()*2) + (detailJSON.getBlueTeleRows()*10) + (detailJSON.getBlueTeleColumns()*20) + (detailJSON.getBlueTeleCypher()*30));
                    match.setRedEndScore((detailJSON.getRedEndRelic1()*10) + (detailJSON.getRedEndRelic2()*20) + (detailJSON.getRedEndRelic3()*40) + (detailJSON.getRedEndRelicUp()*15) + (detailJSON.getRedEndRobotBal()*20));
                    match.setBlueEndScore((detailJSON.getBlueEndRelic1()*10) + (detailJSON.getBlueEndRelic2()*20) + (detailJSON.getBlueEndRelic3()*40) + (detailJSON.getBlueEndRelicUp()*15) + (detailJSON.getBlueEndRobotBal()*20));
                    match.setRedScore(match.getRedAutoScore()+match.getRedTeleScore()+match.getRedEndScore()+match.getRedPenalty());
                    match.setBlueScore(match.getBlueAutoScore()+match.getBlueTeleScore()+match.getBlueEndScore()+match.getBluePenalty());

                    calculateWL(match, scheduleStations);

                    matchList.add(match);
                    matchStations.put(match, scheduleStations);
                    matchDetails.put(match, detailJSON);
                }
                reader.close();
                controller.btnMatchScheduleUpload.setDisable(false);
                controller.btnMatchImport.setText("Re-Sync Match Schedule");
                if (uploadedMatches != null) {
                    if (uploadedMatches.size() == matchList.size()) {
                        controller.sendInfo("Successfully imported " + matchList.size() + " matches from the Scoring System. Online and local schedules are the same.");
                    } else {
                        controller.sendError("Successfully imported " + matchList.size() + " matches from the Scoring System. However, uploaded schedule and local schedule differ.");
                    }
                    this.controller.btnMatchUpload.setVisible(true);
                    this.controller.btnMatchSync.setVisible(true);
                    this.controller.btnMatchOpen.setVisible(true);
                }

            } catch (Exception e) {
                e.printStackTrace();
                controller.sendError("Could not open file. " + e.getLocalizedMessage());
            }
        } else {
            controller.sendError("Could not locate matches.txt from the Scoring System. Did you generate a match schedule?");
        }
    }

    public void postCompletedMatches() {
        if (uploadQueue.size() > 0) {
            for (MatchGeneral completeMatch : uploadQueue) {
                String methodType = "POST";
                for (MatchGeneralJSON match : uploadedMatches) {
                    if (match.getMatchKey().equals(completeMatch.getMatchKey())) {
                        methodType = "PUT";
                    }
                }
                TOAEndpoint matchEndpoint = new TOAEndpoint(methodType, "upload/event/match");
                matchEndpoint.setCredentials(Config.EVENT_API_KEY, Config.EVENT_ID);
                TOARequestBody requestBody = new TOARequestBody();
                requestBody.setEventKey(Config.EVENT_ID);
                requestBody.setMatchKey(completeMatch.getMatchKey());
                MatchGeneralJSON matchJSON = new MatchGeneralJSON();
                matchJSON.setMatchKey(completeMatch.getMatchKey());
                matchJSON.setCreatedBy("TOA-DataSync");
                matchJSON.setCreatedOn(getCurrentTime());
                matchJSON.setEventKey(Config.EVENT_ID);
                matchJSON.setFieldNumber(completeMatch.getFieldNumber());
                matchJSON.setMatchName(completeMatch.getMatchName());
                matchJSON.setPlayNumber(1);
                matchJSON.setTournamentLevel(completeMatch.getTournamentLevel());
                matchJSON.setRedAutoScore(completeMatch.getRedAutoScore());
                matchJSON.setRedTeleScore(completeMatch.getRedTeleScore());
                matchJSON.setRedEndScore(completeMatch.getRedEndScore());
                matchJSON.setRedPenalty(completeMatch.getRedPenalty());
                matchJSON.setRedScore(completeMatch.getRedScore());
                matchJSON.setBlueAutoScore(completeMatch.getBlueAutoScore());
                matchJSON.setBlueTeleScore(completeMatch.getBlueTeleScore());
                matchJSON.setBlueEndScore(completeMatch.getBlueEndScore());
                matchJSON.setBluePenalty(completeMatch.getBluePenalty());
                matchJSON.setBlueScore(completeMatch.getBlueScore());
                requestBody.addValue(matchJSON);
                matchEndpoint.setBody(requestBody);
                matchEndpoint.execute(((response, success) -> {
                    if (success) {
                        controller.sendInfo("Successfully uploaded results to TOA. " + response);
                    } else {
                        controller.sendError("Connection to TOA unsuccessful. " + response);
                    }
                }));

                methodType = "POST";
                if (completeMatch.isUploaded()) {
                    methodType = "PUT";
                } else {
                    for (MatchDetailRelicJSON detail : uploadedDetails) {
                        if (detail.getMatchKey().equals(completeMatch.getMatchKey())) {
                            methodType = "PUT";
                        }
                    }
                }

                MatchDetailRelicJSON detailJSON = null;

                for (MatchDetailRelicJSON matchDetails : matchDetails.values()) {
                    if (matchDetails.getMatchKey().equals(completeMatch.getMatchKey())) {
                        detailJSON = matchDetails;
                        break;
                    }
                }

                TOAEndpoint detailEndpoint = new TOAEndpoint(methodType, "upload/event/match/detail");
                detailEndpoint.setCredentials(Config.EVENT_API_KEY, Config.EVENT_ID);
                TOARequestBody detailBody = new TOARequestBody();
                detailBody.setEventKey(Config.EVENT_ID);
                detailBody.setMatchKey(completeMatch.getMatchKey());
                detailBody.addValue(detailJSON);
                detailEndpoint.setBody(detailBody);
                detailEndpoint.execute(((response, success) -> {
                    if (success) {
                        uploadQueue.remove(completeMatch);
                        matchList.get(completeMatch.getCanonicalMatchNumber()-1).setIsUploaded(true);
                        controller.tableMatches.refresh();
                        controller.sendInfo("Successfully uploaded detail results to TOA. " + response);
                        checkMatchDetails();
                    } else {
                        controller.sendError("Connection to TOA unsuccessful. " + response);
                    }
                }));
            }
        }
    }

    public void postSelectedMatch() {
        if (selectedMatch != null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Are you sure about this?");
            alert.setHeaderText("This operation cannot be undone.");
            alert.setContentText("You are about to POST match " + selectedMatch.getMatchKey() + " to TOA's databases. Are you sure this information is ready to be public?");

            ButtonType okayButton = new ButtonType("Upload Results");
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(okayButton, cancelButton);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == okayButton) {
                String methodType = "POST";
                for (MatchGeneralJSON match : uploadedMatches) {
                    if (match.getMatchKey().equals(selectedMatch.getMatchKey())) {
                        methodType = "PUT";
                    }
                }
                TOAEndpoint matchEndpoint = new TOAEndpoint(methodType, "upload/event/match");
                matchEndpoint.setCredentials(Config.EVENT_API_KEY, Config.EVENT_ID);
                TOARequestBody requestBody = new TOARequestBody();
                requestBody.setEventKey(Config.EVENT_ID);
                requestBody.setMatchKey(selectedMatch.getMatchKey());
                MatchGeneralJSON matchJSON = new MatchGeneralJSON();
                matchJSON.setMatchKey(selectedMatch.getMatchKey());
                matchJSON.setCreatedBy("TOA-DataSync");
                matchJSON.setCreatedOn(getCurrentTime());
                matchJSON.setEventKey(Config.EVENT_ID);
                matchJSON.setFieldNumber(selectedMatch.getFieldNumber());
                matchJSON.setMatchName(selectedMatch.getMatchName());
                matchJSON.setPlayNumber(1);
                matchJSON.setTournamentLevel(selectedMatch.getTournamentLevel());
                matchJSON.setRedAutoScore(selectedMatch.getRedAutoScore());
                matchJSON.setRedTeleScore(selectedMatch.getRedTeleScore());
                matchJSON.setRedEndScore(selectedMatch.getRedEndScore());
                matchJSON.setRedPenalty(selectedMatch.getRedPenalty());
                matchJSON.setRedScore(selectedMatch.getRedScore());
                matchJSON.setBlueAutoScore(selectedMatch.getBlueAutoScore());
                matchJSON.setBlueTeleScore(selectedMatch.getBlueTeleScore());
                matchJSON.setBlueEndScore(selectedMatch.getBlueEndScore());
                matchJSON.setBluePenalty(selectedMatch.getBluePenalty());
                matchJSON.setBlueScore(selectedMatch.getBlueScore());
                requestBody.addValue(matchJSON);
                matchEndpoint.setBody(requestBody);
                matchEndpoint.execute(((response, success) -> {
                    if (success) {
                        controller.sendInfo("Successfully uploaded results to TOA. " + response);
                    } else {
                        controller.sendError("Connection to TOA unsuccessful. " + response);
                    }
                }));

                methodType = "POST";
                if (selectedMatch.isUploaded()) {
                    methodType = "PUT";
                } else {
                    for (MatchDetailRelicJSON detail : uploadedDetails) {
                        if (detail.getMatchKey().equals(selectedMatch.getMatchKey())) {
                            methodType = "PUT";
                        }
                    }
                }

                MatchDetailRelicJSON detailJSON = null;

                for (MatchDetailRelicJSON matchDetails : matchDetails.values()) {
                    if (matchDetails.getMatchKey().equals(selectedMatch.getMatchKey())) {
                        detailJSON = matchDetails;
                        break;
                    }
                }

                TOAEndpoint detailEndpoint = new TOAEndpoint(methodType, "upload/event/match/detail");
                detailEndpoint.setCredentials(Config.EVENT_API_KEY, Config.EVENT_ID);
                TOARequestBody detailBody = new TOARequestBody();
                detailBody.setEventKey(Config.EVENT_ID);
                detailBody.setMatchKey(selectedMatch.getMatchKey());
                detailBody.addValue(detailJSON);
                detailEndpoint.setBody(detailBody);
                detailEndpoint.execute(((response, success) -> {
                    if (success) {
                        matchList.get(selectedMatch.getCanonicalMatchNumber()-1).setIsUploaded(true);
                        controller.tableMatches.refresh();
                        controller.sendInfo("Successfully uploaded detail results to TOA. " + response);
                        checkMatchDetails();
                    } else {
                        controller.sendError("Connection to TOA unsuccessful. " + response);
                    }
                }));
            }

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
            this.checkMatchSchedule();
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
                controller.labelScheduleUploaded.setText("Schedule Already Posted.");
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

    public HashMap<Integer, int[]> getTeamWL() {
        return teamWinLoss;
    }

}

