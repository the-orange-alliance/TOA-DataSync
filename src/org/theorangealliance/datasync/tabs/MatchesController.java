package org.theorangealliance.datasync.tabs;

import com.google.gson.stream.MalformedJsonException;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.theorangealliance.datasync.DataSyncController;
import org.theorangealliance.datasync.MatchDetailsController;
import org.theorangealliance.datasync.json.toa.*;
import org.theorangealliance.datasync.logging.TOALogger;
import org.theorangealliance.datasync.json.first.*;
import org.theorangealliance.datasync.models.MatchGeneral;
import org.theorangealliance.datasync.models.MatchGeneralAndMatchParticipant;
import org.theorangealliance.datasync.models.MatchParticipant;
import org.theorangealliance.datasync.util.*;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;

/**
 * Created by Kyle Flynn on 11/29/2017.
 * Modded By Soren Zaiser for 2018 Scorekeeping System on 11/01/18
 */
public class MatchesController {

    private DataSyncController controller;
    private ObservableList<MatchGeneral> matchList;
    private HashSet<MatchGeneralJSON> uploadedMatches;
    private HashSet<MatchDetail1819JSON> uploadedDetails;
    private HashSet<MatchParticipantJSON> uploadedMatchParticipants;

    private HashMap<MatchGeneral, MatchParticipant[]> matchStations;
    private HashMap<MatchGeneral, MatchDetail1819JSON> matchDetails;
    private HashMap<MatchGeneral, MatchDetail1718JSON> matchDetails1718;
    private MatchGeneral selectedMatch;

    /*For Fixing The Match IDs and Sorting */

    private ArrayList<MatchGeneral> fMatches = new ArrayList<>(); //Finals Matches
    private ArrayList<MatchParticipant[]> fSche = new ArrayList<>(); //Finals Matches
    private ArrayList<MatchDetail1819JSON> fMatchDtl = new ArrayList<>(); //Finals Matches

    private ArrayList<MatchGeneral> sf2Matches = new ArrayList<>();//SF2 Matches
    private ArrayList<MatchParticipant[]> sf2Sche = new ArrayList<>();//SF2 Matches
    private ArrayList<MatchDetail1819JSON> sf2MatchDtl = new ArrayList<>();//SF2 Matches

    private ArrayList<MatchGeneral> sf1Matches = new ArrayList<>();//SF1 Matches
    private ArrayList<MatchParticipant[]> sf1Sche = new ArrayList<>();//SF1 Matches
    private ArrayList<MatchDetail1819JSON> sf1MatchDtl = new ArrayList<>();//SF1 Matches


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
        this.controller.btnMatchBrowserView.setVisible(false);
        this.controller.btnMatchBrowserView.setDisable(true);
        this.controller.btnMatchOpen.setVisible(false);
        this.controller.btnMatchOpen.setDisable(true);
    }

    private void openMatchView(MatchGeneral match) {
        this.controller.txtVideoUrl.setText("");
        controller.btnMatchOpen.setDisable(true);
        if (match.isDone()) { //&& uploadedMatches.size() > 0) {
            controller.btnMatchUpload.setDisable(false);
        } else {
            controller.btnMatchUpload.setDisable(true);
        }
        if (match.isUploaded()) {
            controller.btnMatchBrowserView.setDisable(false);
        } else {
            controller.btnMatchBrowserView.setDisable(true);
        }

        controller.btnMatchOpen.setDisable(false);

        MatchParticipant[] teams = matchStations.get(match);
        if (teams != null) {
            String redTeams = teams[0].getTeamKey() + teams[0].getStatusString() + " " + teams[1].getTeamKey() + teams[1].getStatusString();
            String redFinalTeam = (teams[2].getTeamKey() == 0 ? "" : teams[2].getTeamKey() + teams[2].getStatusString() + "");
            String blueTeams = teams[3].getTeamKey() + teams[3].getStatusString() + " " + teams[4].getTeamKey() + teams[4].getStatusString();
            String blueFinalTeam = (teams[5].getTeamKey() == 0 ? "" : teams[5].getTeamKey() + teams[5].getStatusString() + "");
            controller.labelRedTeams.setText(redTeams + " " + redFinalTeam);
            controller.labelBlueTeams.setText(blueTeams + " " + blueFinalTeam);
        }

        controller.labelMatchLevel.setText("Level: " + match.getTournamentLevel());
        controller.labelMatchField.setText("Field: " + match.getFieldNumber());
        controller.labelMatchPlay.setText("Play: " + match.getPlayNumber());
        controller.labelCommitTime.setText(match.getLastCommitTime());
        controller.labelVideoUrl.setText(match.getVideoUrl());
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

    public void setSelectedMatchVideo(){
        int i = 0;
        for(MatchGeneral gen : matchList){
            if(gen.getMatchKey().equals(selectedMatch.getMatchKey())){
                break;
            }
            i++;
        }
        matchList.get(i).setVideoUrl(controller.txtVideoUrl.getText());
    }

    public void openMatchDetails() {
        MatchDetail1819JSON dtl = getSelectedMatchDetails();

        if(dtl != null) {
            try{
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/MatchDetails.fxml"));
                Parent root1 = (Parent) fxmlLoader.load();
                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.initStyle(StageStyle.UNDECORATED);
                stage.setTitle("Match Details for Match " + dtl.getMatchKey());
                stage.setScene(new Scene(root1));
                stage.alwaysOnTopProperty();
                MatchDetailsController dtlCont = fxmlLoader.getController();
                dtlCont.matchKey.setText("Match Details for Match: " + dtl.getMatchKey());
                dtlCont.redLanded.setText("# of Robots Landed: " + dtl.getRedAutoLand());
                dtlCont.redSample.setText("Sample Status: " + dtl.getRedAutoSamp());
                dtlCont.redClaimed.setText("# Claimed: " + dtl.getRedAutoClaim());
                dtlCont.redParked.setText("# of Robots Parked: " + dtl.getRedAutoClaim());
                dtlCont.redGold.setText("# of Gold Particles Scored: " + dtl.getRedDriverGold());
                dtlCont.redSilver.setText("# of Silver Particles Scored: " + dtl.getRedDriverSilver());
                dtlCont.redDepot.setText("# of Particles in Depot: " + dtl.getRedDriverDepot());
                dtlCont.redLatched.setText("# of Robots Latched: " + dtl.getRedEndLatch());
                dtlCont.redPart.setText("# of Robots Partially Parked in Crater: " + dtl.getRedEndIn());
                dtlCont.redFull.setText("# of Robots Fully Parked in Crater: " + dtl.getRedEndComp());
                dtlCont.blueLanded.setText("# of Robots Landed: " + dtl.getBlueAutoLand());
                dtlCont.blueSample.setText("Sample Status: " + dtl.getBlueAutoSamp());
                dtlCont.blueClaimed.setText("# Claimed: " + dtl.getBlueAutoClaim());
                dtlCont.blueParked.setText("# of Robots Parked: " + dtl.getBlueAutoClaim());
                dtlCont.blueGold.setText("# of Gold Particles Scored: " + dtl.getBlueDriverGold());
                dtlCont.blueSilver.setText("# of Silver Particles Scored: " + dtl.getBlueDriverSilver());
                dtlCont.blueDepot.setText("# of Particles in Depot: " + dtl.getBlueDriverDepot());
                dtlCont.blueLatched.setText("# of Robots Latched: " + dtl.getBlueEndLatch());
                dtlCont.bluePart.setText("# of Robots Partially Parked in Crater: " + dtl.getBlueEndIn());
                dtlCont.blueFull.setText("# of Robots Fully Parked in Crater: " + dtl.getBlueEndComp());
                stage.show();
            } catch (Exception e) {
                this.controller.sendError("Failed to Show match Details " + e);
            }
        } else {
            this.controller.sendError("Could Not get Match Details");
        }
    }

    public void viewFromTOA(){

        try {
            Desktop.getDesktop().browse(new URI("https://theorangealliance.org/matches/" + controller.labelMatchKey.getText()));
        }catch (URISyntaxException e){
            TOALogger.log(Level.WARNING, "Error Assembling TOA Match URL");
        }catch (IOException e){
            TOALogger.log(Level.WARNING, "Error Opening Browser");
        }


    }

    public void checkMatchSchedule() {
        this.controller.labelScheduleUploaded.setTextFill(Color.ORANGE);
        this.controller.labelScheduleUploaded.setText("Checking TOA...");
        TOAEndpoint matchesEndpoint = new TOAEndpoint("GET", "event/" + Config.EVENT_ID + "/matches");
        matchesEndpoint.setCredentials(Config.TOA_API_KEY, Config.EVENT_ID);
        matchesEndpoint.execute(((response, success) -> {
            if (success) {
                checkMatchParticipants();
                uploadedMatches = new HashSet<>(Arrays.asList(matchesEndpoint.getGson().fromJson(response, MatchGeneralJSON[].class)));
                if (uploadedMatches.size() > 0) {
                    this.controller.labelScheduleUploaded.setTextFill(Color.GREEN);
                    this.controller.labelScheduleUploaded.setText("Schedule Already Posted");
                    this.controller.sendInfo("Grabbed " + uploadedMatches.size() + " matches from TOA");
                    //Do Dashboard Stuff
                    this.controller.cb_matches.setTextFill(Color.GREEN);
                    this.controller.cb_matches.setSelected(true);
                    this.controller.btn_cb_matches.setDisable(true);
                } else {
                    this.controller.labelScheduleUploaded.setTextFill(Color.RED);
                    this.controller.labelScheduleUploaded.setText("Schedule NOT Posted");
                    //Do Dashboard Stuff
                    this.controller.cb_matches.setTextFill(Color.RED);
                    this.controller.cb_matches.setSelected(false);
                    this.controller.btn_cb_matches.setDisable(false);
                }
            } else {
                this.controller.sendError("Error: " + response);
                //Do Dashboard Stuff
                this.controller.cb_matches.setTextFill(Color.RED);
                this.controller.cb_matches.setSelected(false);
                this.controller.btn_cb_matches.setDisable(false);
            }
        }));
    }

    public void checkMatchDetails() {
        TOAEndpoint matchesEndpoint = new TOAEndpoint("GET", "event/" + Config.EVENT_ID + "/matches/details");
        matchesEndpoint.setCredentials(Config.TOA_API_KEY, Config.EVENT_ID);
        matchesEndpoint.execute(((response, success) -> {
            if (success) {
                //TOALogger.log(Level.INFO, "Grabbed match details for " + uploadedDetails.size() + " matches.");
            } else {
                this.controller.sendError("Error: " + response);
            }
        }));
    }

    public void checkMatchParticipants() {
        TOAEndpoint matchesEndpoint = new TOAEndpoint("GET", "event/" + Config.EVENT_ID + "/matches/participants");
        matchesEndpoint.setCredentials(Config.TOA_API_KEY, Config.EVENT_ID);
        matchesEndpoint.execute(((response, success) -> {
            if (success) {
                uploadedMatchParticipants = new HashSet<>(Arrays.asList(matchesEndpoint.getGson().fromJson(response, MatchParticipantJSON[].class)));
                TOALogger.log(Level.INFO, "Grabbed " + uploadedMatchParticipants.size() + " match participants.");
            } else {
                this.controller.sendError("Error: " + response);
            }
        }));
    }

    private void calculateWL(MatchGeneral match, MatchParticipant[] stations) {
        if (match.isDone() && match.getTournamentLevel() == 1) {
            boolean redWin = match.getRedScore() > match.getBlueScore();
            boolean tie = match.getRedScore() == match.getBlueScore();
            String resultStr = tie ? "TIE" : redWin ? "RED" : "BLUE";
//            System.out.println(match.getMatchKey() + ": "  + resultStr);
            for (MatchParticipant station : stations) {
                if (station.getTeamKey() != 0 && station.getStationStatus() >= 1) {
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
                            result[WINS] = 1;
                            result[LOSS] = 0;
                            result[TIES] = 0;
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
        if(controller.rbNewScore.isSelected()) {
            syncMatches1819();
        } else {
            syncMatches1718();
        }
    }

    private void syncMatches1819(){
        postCompletedMatches();
    }

    private void syncMatches1718(){
        if (matchList.size() <= 0) {
            getMatchesByFile1718();
        } else {
            File matchFile = new File(Config.SCORING_DIR + File.separator + "matches.txt");
            if (matchFile.exists()) {
                try {
                    uploadQueue.clear();
                    teamWinLoss.clear();
                    BufferedReader reader = new BufferedReader(new FileReader(matchFile));
                    String line;
                    int count = 0;
                    while ((line = reader.readLine()) != null && count < matchList.size()) {
                        MatchGeneral match = matchList.get(count);
                        String[] teamInfo = line.split("\\|\\|")[1].split("\\|");
                        // Field 24 will be whether or not score is SAVED.
                        // This is ALSO where the match details section begins.
                        int saved = Integer.parseInt(teamInfo[24]);
                        if (saved == 1) {
                            match.setPlayNumber(1);
                            match.setIsDone(true);
                        }

                        boolean fullyUploaded = false;
/*
                        // Not very efficient, but it is what is is... I hate O(N^2) algorithms.
                        for (MatchDetail1718JSON detail : uploadedDetails) {
                            if (detail.getMatchKey().equals(match.getMatchKey())) {
//                                match.setIsUploaded(true);
                                fullyUploaded = true;
                            }
                        }*/

                        for (MatchGeneralJSON general : uploadedMatches) {
                            if (general.getMatchKey().equals(match.getMatchKey())) {
                                if (general.getPlayNumber() == 0) {
                                    match.setIsUploaded(false);
                                } else if (fullyUploaded) {
                                    match.setIsUploaded(true);
                                }
                            }
                        }

                        MatchDetail1718JSON detailJSON = matchDetails1718.get(match);
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
                            TOALogger.log(Level.INFO, "Added match " + match.getMatchKey() + " to the upload queue.");
                            uploadQueue.add(match);
                        }
//                        else if (!match.isDone() && (match.getTournamentLevel() > 30 || match.getTournamentLevel() == 4)) {
//                            uploadQueue.add(match);
//                        }
                    }
                    controller.tableMatches.refresh();
                    reader.close();

                    TOALogger.log(Level.INFO, "Match sync complete.");

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

    public void getMatchesByFile1718() {
        File matchFile = new File(Config.SCORING_DIR + File.separator + "matches.txt");
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
                            fieldNumber);
                    char qualChar = match.getMatchName().contains("Qual") ? 'Q' : 'E';
                    if (qualChar == 'E') {
                        elimCount++;
                        int elimFieldNum = match.getTournamentLevel() % 2;
                        match.setFieldNumber(match.getTournamentLevel() == 4 ? 1 : (elimFieldNum == 0 ? 2 : 1));
                        match.setMatchKey(Config.EVENT_ID + "-" + qualChar + String.format("%03d", elimCount) + "-1");
                    } else {
                        match.setMatchKey(Config.EVENT_ID + "-" + qualChar + String.format("%03d", match.getCanonicalMatchNumber()) + "-1");
                    }

                    /** TEAM info */
                    String[] teamInfo = line.split("\\|\\|")[1].split("\\|");
                    MatchParticipant[] MatchParticipants = new MatchParticipant[6];
                    MatchParticipants[0] = new MatchParticipant(match.getMatchKey(), 11, Integer.parseInt(teamInfo[0]));
                    MatchParticipants[1] = new MatchParticipant(match.getMatchKey(), 12, Integer.parseInt(teamInfo[1]));
                    MatchParticipants[2] = new MatchParticipant(match.getMatchKey(), 13, Integer.parseInt(teamInfo[2]));
                    MatchParticipants[3] = new MatchParticipant(match.getMatchKey(), 21, Integer.parseInt(teamInfo[3]));
                    MatchParticipants[4] = new MatchParticipant(match.getMatchKey(), 22, Integer.parseInt(teamInfo[4]));
                    MatchParticipants[5] = new MatchParticipant(match.getMatchKey(), 23, Integer.parseInt(teamInfo[5]));

                    /// Check for dq, no show, surrogates, and yellow cards, with that order of precedence
                    MatchParticipants[0].setStationStatus(Integer.parseInt(teamInfo[6]) == 2 ? -2 : Integer.parseInt(teamInfo[6]) == 1 ? -1 : Integer.parseInt(teamInfo[18]) == 1 ? 0 : teamInfo[9].equals("true") ? 2 : 1);
                    MatchParticipants[1].setStationStatus(Integer.parseInt(teamInfo[7]) == 2 ? -2 : Integer.parseInt(teamInfo[7]) == 1 ? -1 : Integer.parseInt(teamInfo[19]) == 1 ? 0 : teamInfo[10].equals("true") ? 2 : 1);
                    MatchParticipants[2].setStationStatus(Integer.parseInt(teamInfo[8]) == 2 ? -2 : Integer.parseInt(teamInfo[8]) == 1 ? -1 : Integer.parseInt(teamInfo[20]) == 1 ? 0 : teamInfo[11].equals("true") ? 2 : 1);
                    MatchParticipants[3].setStationStatus(Integer.parseInt(teamInfo[12]) == 2 ? -2 : Integer.parseInt(teamInfo[12]) == 1 ? -1 : Integer.parseInt(teamInfo[21]) == 1 ? 0 : teamInfo[15].equals("true") ? 2 : 1);
                    MatchParticipants[4].setStationStatus(Integer.parseInt(teamInfo[13]) == 2 ? -2 : Integer.parseInt(teamInfo[13]) == 1 ? -1 : Integer.parseInt(teamInfo[22]) == 1 ? 0 : teamInfo[16].equals("true") ? 2 : 1);
                    MatchParticipants[5].setStationStatus(Integer.parseInt(teamInfo[14]) == 2 ? -2 : Integer.parseInt(teamInfo[14]) == 1 ? -1 : Integer.parseInt(teamInfo[23]) == 1 ? 0 : teamInfo[17].equals("true") ? 2 : 1);

                    // Field 24 will be whether or not score is SAVED.
                    // This is ALSO where the match details section begins.
                    int saved = Integer.parseInt(teamInfo[24]);
                    if (saved == 1) {
                        match.setPlayNumber(1);
                        match.setIsDone(true);
                    }

                    // Not very efficient, but it is what is is... I hate O(N^2) algorithms.
                    for (MatchDetail1819JSON detail : uploadedDetails) {
                        if (detail.getMatchKey().equals(match.getMatchKey())) {
                            match.setIsUploaded(true);
                        }
                    }

                    MatchDetail1718JSON detailJSON = new MatchDetail1718JSON();
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

                    calculateWL(match, MatchParticipants);

                    matchList.add(match);
                    matchStations.put(match, MatchParticipants);
                    matchDetails1718.put(match, detailJSON);
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
                    this.controller.btnMatchBrowserView.setVisible(true);
                    this.controller.btnMatchOpen.setVisible(true);
                }
                TOALogger.log(Level.INFO, "Match import successful.");
            } catch (Exception e) {
                e.printStackTrace();
                controller.sendError("Could not open file. " + e.getLocalizedMessage());
            }
        } else {
            controller.sendError("Could not locate matches.txt from the Scoring System. Did you generate a match schedule?");
        }
    }

    public void getMatchesFromFIRSTApi1819() {
        boolean allQualComplete = true;
        /* Qualifacation Matches*/
        QualMatchesArray matches = null;
        try{
           matches = FIRSTEndpointNonLambda.getGson().fromJson(FIRSTEndpointNonLambda.getResp("events/" + Config.FIRST_API_EVENT_ID + "/matches/"), QualMatchesArray.class);
        } catch (Exception e) {
            controller.sendError("Connection to FIRST Scoring system unsuccessful. " + e);
        }
        if(matches != null) {
            controller.btnMatchUpload.setDisable(true);
            uploadQueue.clear();
            matchList.clear();
            matchStations.clear();
            matchDetails.clear();
            teamWinLoss.clear();

            for (QualMatches m : matches.getQualMatches()) {
                Match qualMatch = null;
                try{
                    qualMatch = FIRSTEndpointNonLambda.getGson().fromJson(FIRSTEndpointNonLambda.getResp("events/" + Config.FIRST_API_EVENT_ID + "/matches/" + m.getMatchNumber()), Match.class);
                } catch (Exception e) {
                    controller.sendError("Connection to FIRST Scoring system unsuccessful. " + e);
                }
                if (qualMatch != null) {

                    MatchGeneralAndMatchParticipant mValues = getMatchGeneralFromFirstAPI(1, m.getMatchNumber(), 0, null, m, qualMatch);

                    MatchGeneral match = mValues.getMatchGeneral();
                    MatchParticipant[] MatchParticipants = mValues.getMatchParticipants();

                    match.setIsUploaded(getUploadedFromMatchKey(match.getMatchKey()));
                    if (match.isDone() && !match.isUploaded()) {
                        TOALogger.log(Level.INFO, "Added match " + match.getMatchKey() + " to the upload queue.");
                        uploadQueue.add(match);
                    } else if (!match.isDone()) {
                        allQualComplete = false;
                    }

                    calculateWL(match, MatchParticipants);

                    matchDetails.put(match, getMatchDetails1819FirstAPI(("matches/" + m.getMatchNumber()), match.getMatchKey(), 1));
                    matchList.add(match);
                    matchStations.put(match, MatchParticipants);



                } else {
                    controller.sendError("Connection to FIRST Scoring system unsuccessful.");
                }
            }

        } else {
            controller.sendError("Connection to FIRST Scoring system unsuccessful.  Did not get Qualifier Matches" );
        }

        /*Elim Matches*/

        /* SF1 */
        int elimMatches = 0;

        //Clear SF1 Lists
        sf1Sche.clear();
        sf1Matches.clear();
        sf1MatchDtl.clear();

        ElimMatchesArray matchesSF1 = null;
        //Try to connect to scoring system
        //If connnection is failed, JSON will not parse and then matchesSF1 will be null
        if(allQualComplete) {
            try{
                matchesSF1 = FIRSTEndpointNonLambda.getGson().fromJson(FIRSTEndpointNonLambda.getResp("events/" + Config.FIRST_API_EVENT_ID + "/elim/sf/1"), ElimMatchesArray.class);
            } catch (Exception e) {
                controller.sendError("Connection to FIRST Scoring system unsuccessful. " + e);
            }
        }


        //Make sure connection was successful
        if (matchesSF1 != null) {
            for(ElimMatches m : matchesSF1.getElimMatches()) {
                Match qualMatch = null;
                try {
                    qualMatch = FIRSTEndpointNonLambda.getGson().fromJson(FIRSTEndpointNonLambda.getResp("events/" + Config.FIRST_API_EVENT_ID + "/elim/sf/1/" + m.getMatchNumber().substring(4)), Match.class);
                } catch (Exception e) {
                    controller.sendError("Connection to FIRST Scoring system unsuccessful. " + e);
                }
                if (qualMatch != null) {
                    elimMatches++;

                    //We Get A string that looks like "SF1-1" We Remove the SF, then Replace the "-" with an empty string.
                    int sfMatchNum = Integer.parseInt(m.matchNumber.substring(2).replace("-", ""));
                    MatchGeneralAndMatchParticipant mValues = getMatchGeneralFromFirstAPI(2, sfMatchNum, elimMatches, m, null, qualMatch);

                    MatchGeneral match = mValues.getMatchGeneral();
                    MatchParticipant[] MatchParticipants = mValues.getMatchParticipants();

                    calculateWL(match, MatchParticipants);

                    sf1MatchDtl.add(getMatchDetails1819FirstAPI(("elim/sf/1/" + (sfMatchNum-10)), match.getMatchKey(), 2));
                    sf1Matches.add(match);
                    sf1Sche.add(MatchParticipants);

                } else {
                    controller.sendError("Unable to get score for " + m.getMatchNumber());
                }
            }//Close For Loop
        }

        /* SF2 */

        //Clear Our Match Lists
        sf2Sche.clear();
        sf2Matches.clear();
        sf2MatchDtl.clear();

        ElimMatchesArray matchesSF2 = null;
        if(allQualComplete) {
            try {
                matchesSF2 = FIRSTEndpointNonLambda.getGson().fromJson(FIRSTEndpointNonLambda.getResp("events/" + Config.FIRST_API_EVENT_ID + "/elim/sf/2"), ElimMatchesArray.class);
            } catch (Exception e) {
                controller.sendError("Connection to FIRST Scoring system unsuccessful. " + e);
            }
        }

        //Make sure conn was successful
        if (matchesSF2 != null) {

            for (ElimMatches m : matchesSF2.getElimMatches()) {

                Match qualMatch = null;
                try {
                    qualMatch = FIRSTEndpointNonLambda.getGson().fromJson(FIRSTEndpointNonLambda.getResp("events/" + Config.FIRST_API_EVENT_ID + "/elim/sf/2/" + m.getMatchNumber().substring(4)), Match.class);
                } catch (Exception e) {
                    controller.sendError("Connection to FIRST Scoring system unsuccessful. " + e);
                }
                if (qualMatch != null) {
                    elimMatches++;

                    //We Get A string that looks like "SF1-2" We Remove the SF, then Replace the "-" with an empty string.
                    int sfMatchNum = Integer.parseInt(m.matchNumber.substring(2).replace("-", ""));
                    MatchGeneralAndMatchParticipant mValues = getMatchGeneralFromFirstAPI(2, sfMatchNum, elimMatches, m, null, qualMatch);

                    MatchGeneral match = mValues.getMatchGeneral();
                    MatchParticipant[] MatchParticipants = mValues.getMatchParticipants();

                    calculateWL(match, MatchParticipants);

                    sf2MatchDtl.add(getMatchDetails1819FirstAPI(("elim/sf/2/" + (sfMatchNum - 20)), match.getMatchKey(), 2));
                    sf2Matches.add(match);
                    sf2Sche.add(MatchParticipants);

                } else {
                    controller.sendError("Unable to get score for " + m.getMatchNumber());
                }
            }//Close For Loop
        }

        /* Finals Matches */
        //Clear Our match lists
        fSche.clear();
        fMatches.clear();
        fMatchDtl.clear();

        ElimMatchesArray matchesF = null;
        if(matchesSF1 != null || matchesSF2 != null) { //If the SF matches are null, then we KNOW there wont be any Finals matches
            try {
                matchesF = FIRSTEndpointNonLambda.getGson().fromJson(FIRSTEndpointNonLambda.getResp("events/" + Config.FIRST_API_EVENT_ID + "/elim/finals/"), ElimMatchesArray.class);
            } catch (Exception e) {
                controller.sendError("Connection to FIRST Scoring system unsuccessful. " + e);
            }
        }
        if (matchesF != null) {

            for(ElimMatches m : matchesF.getElimMatches()) {
                Match qualMatch = null;
                try {
                    qualMatch = FIRSTEndpointNonLambda.getGson().fromJson(FIRSTEndpointNonLambda.getResp("events/" + Config.FIRST_API_EVENT_ID + "/elim/finals/" + m.getMatchNumber().substring(2)), Match.class);
                } catch (Exception e) {
                    controller.sendError("Connection to FIRST Scoring system unsuccessful. " + e);
                }
                if(qualMatch != null) {
                    elimMatches++;

                    //We Get A string that looks like "F-1" We Remove the "F-"
                    int fMatchNum = Integer.parseInt(m.matchNumber.substring(2));
                    MatchGeneralAndMatchParticipant mValues = getMatchGeneralFromFirstAPI(3, fMatchNum, elimMatches, m, null, qualMatch);

                    MatchGeneral match = mValues.getMatchGeneral();
                    MatchParticipant[] MatchParticipants = mValues.getMatchParticipants();

                    calculateWL(match, MatchParticipants);

                    fMatchDtl.add(getMatchDetails1819FirstAPI(("elim/finals/" + fMatchNum), match.getMatchKey(), 3));
                    fMatches.add(match);
                    fSche.add(MatchParticipants);

                } else {
                    controller.sendError("Unable to get score for " + m.getMatchNumber());
                }
            }

        }

        //Now that we parsed ALL of the elim matches, we can fix the matches
        fixTheElimMatches();
        if (uploadedMatches != null) {
            if (uploadedMatches.size() == matchList.size()) {
                controller.sendInfo("Successfully imported " + matchList.size() + " matches from the Scoring System. Online and local schedules are the same.");
            } else {
                controller.sendError("Successfully imported " + matchList.size() + " matches from the Scoring System. However, uploaded schedule and local schedule differ.");
            }
        }
        if(matchList.size() > 0){
            this.controller.btnMatchUpload.setVisible(true);
            this.controller.btnMatchScheduleUpload.setDisable(false);
            this.controller.btnMatchBrowserView.setVisible(true);
            this.controller.btnMatchOpen.setVisible(true);
            this.controller.txtVideoUrl.setDisable(false);
            this.controller.btnSetUrl.setDisable(false);
        } else {
            this.controller.btnMatchUpload.setVisible(false);
            this.controller.btnMatchBrowserView.setVisible(false);
            this.controller.btnMatchOpen.setVisible(false);
            this.controller.txtVideoUrl.setDisable(true);
            this.controller.btnSetUrl.setDisable(true);
            this.controller.btnMatchScheduleUpload.setDisable(true);
        }
        checkMatchDetails();
    }

    private boolean getUploadedFromMatchKey (String matchKey) {
        boolean fullyUploaded = false;
        boolean returnValue = false;

        // Not very efficient, but it is what is is... I hate O(N^2) algorithms.
        if(uploadedDetails != null) {
            for (MatchDetail1819JSON detail : uploadedDetails) {
                if (detail.getMatchKey().equalsIgnoreCase(matchKey)) {
                    returnValue = true;
                    fullyUploaded = true;
                    break;
                }
            }
        }

        if(uploadedMatches != null){
            for (MatchGeneralJSON general : uploadedMatches) {
                if (general.getMatchKey().equalsIgnoreCase(matchKey)) {
                    if (general.getPlayNumber() == 0) {
                        returnValue = false;
                    } else if (fullyUploaded) {
                        returnValue = true;
                    }
                    break;
                }
            }
        }

        return returnValue;
    }

    private MatchDetail1819JSON getMatchDetails1819FirstAPI(String endpoint, String matchKey, int tournLevel) {
        MatchDetail1819JSON matchSpecific = new MatchDetail1819JSON();
        MatchDetails1819 firstMatchSpec = null;
        try {
            firstMatchSpec =  FIRSTEndpointNonLambda.getGson().fromJson(FIRSTEndpointNonLambda.getResp("2019/events/" + Config.FIRST_API_EVENT_ID  + "/" +  endpoint), MatchDetails1819.class);

        } catch (Exception e) {
            controller.sendError("Connection to FIRST Scoring system unsuccessful. Path 2019/events/" + Config.FIRST_API_EVENT_ID  + "/" + endpoint + " Error " +  e);
        }

        if(firstMatchSpec != null) {

            matchSpecific.setMatchDtlKey(matchKey + "-DTL");
            matchSpecific.setMatchKey(matchKey);
            matchSpecific.setRedMinPen(firstMatchSpec.getRedSpecifics().getMinorPenalties());
            matchSpecific.setRedMajPen(firstMatchSpec.getRedSpecifics().getMajorPenalties());
            matchSpecific.setBlueMinPen(firstMatchSpec.getBlueSpecifics().getMinorPenalties());
            matchSpecific.setBlueMajPen(firstMatchSpec.getBlueSpecifics().getMajorPenalties());
            matchSpecific.setRedAutoLand(firstMatchSpec.getRedSpecifics().getAutoLanded() / 30);
            matchSpecific.setBlueAutoLand(firstMatchSpec.getBlueSpecifics().getAutoLanded() / 30);
            matchSpecific.setRedAutoSamp(firstMatchSpec.getRedSpecifics().getAutoSample() / 25);
            matchSpecific.setBlueAutoSamp(firstMatchSpec.getBlueSpecifics().getAutoSample() / 25);
            matchSpecific.setRedAutoClaim(firstMatchSpec.getRedSpecifics().getAutoDepot() / 15);
            matchSpecific.setBlueAutoClaim(firstMatchSpec.getBlueSpecifics().getAutoDepot() / 15);
            matchSpecific.setRedAutoPark(firstMatchSpec.getRedSpecifics().getAutoParking() / 10);
            matchSpecific.setBlueAutoPark(firstMatchSpec.getBlueSpecifics().getAutoParking() / 10);
            matchSpecific.setRedDriverGold(firstMatchSpec.getRedSpecifics().getTeleGold() / 5);
            matchSpecific.setBlueDriverGold(firstMatchSpec.getBlueSpecifics().getTeleGold() / 5);
            matchSpecific.setRedDriverSilver(firstMatchSpec.getRedSpecifics().getTeleSilver() / 5);
            matchSpecific.setBlueDriverSilver(firstMatchSpec.getBlueSpecifics().getTeleSilver() / 5);
            matchSpecific.setRedDriverDepot(firstMatchSpec.getRedSpecifics().getTeleDepot() / 2);
            matchSpecific.setBlueDriverDepot(firstMatchSpec.getBlueSpecifics().getTeleDepot() / 2);
            matchSpecific.setRedEndLatch(firstMatchSpec.getRedSpecifics().getEndLatched() / 50);
            matchSpecific.setBlueEndLatch(firstMatchSpec.getBlueSpecifics().getEndLatched() / 50);
            matchSpecific.setRedEndIn(calcCratePoints(firstMatchSpec.getRedSpecifics().getEndParking())[0]);
            matchSpecific.setBlueEndIn(calcCratePoints(firstMatchSpec.getBlueSpecifics().getEndParking())[0]);
            matchSpecific.setRedEndComp(calcCratePoints(firstMatchSpec.getRedSpecifics().getEndParking())[1]);
            matchSpecific.setBlueEndComp(calcCratePoints(firstMatchSpec.getBlueSpecifics().getEndParking())[1]);
        }

        return matchSpecific;
    }

    private int[] calcCratePoints (int parking){
        int partCrater = 0;
        int fullCrater = 0;
        switch(parking){
            case 15: partCrater = 1; break;
            case 25: fullCrater = 1; break;
            case 30: partCrater = 2; break;
            case 40: partCrater = 1; fullCrater = 1; break;
            case 50: fullCrater = 2; break;
            default: partCrater = parking; break;
        }
        int[] returnValue = {partCrater, fullCrater};
        return returnValue;
    }

    private MatchGeneralAndMatchParticipant getMatchGeneralFromFirstAPI (int tournLevel, int matchNum, int elimNumber, ElimMatches eM, QualMatches qM, Match qualMatch) {
        MatchGeneral match = new MatchGeneral(
                MatchGeneral.buildMatchName(tournLevel, matchNum),
                MatchGeneral.buildTOATournamentLevel(tournLevel, matchNum),
                qualMatch.getFieldNumber());

        //TODO: Update the -1 with the actual play #
        if(elimNumber > 1) {
            match.setMatchKey(Config.EVENT_ID + "-E" + String.format("%03d", elimNumber) + "-1");
        } else {
            match.setMatchKey(Config.EVENT_ID + "-Q" + String.format("%03d", match.getCanonicalMatchNumber()) + "-1");
        }

        MatchParticipant[] MatchParticipants = new MatchParticipant[6];

        /** TEAM info **/

        //TODO: Update with Yellow card, Red Card, No Show, and DQ data when API route is added
        boolean[] noShow =      {false, false, false, false, false, false};
        boolean[] surrogate =   {false, false, false, false, false, false};
        boolean[] yellow =      {false, false, false, false, false, false};
        boolean[] dq =          {false, false, false, false, false, false};
        if(eM != null) {
            AllianceFIRST redAlliance = getAllianceFirstFromTeams(eM.getRedAlliance().getAllianceCaptain(), eM.getRedAlliance().getAlliancePick1(), eM.getRedAlliance().getAlliancePick2());
            AllianceFIRST blueAlliance = getAllianceFirstFromTeams(eM.getBlueAlliance().getAllianceCaptain(), eM.getBlueAlliance().getAlliancePick1(), eM.getBlueAlliance().getAlliancePick2());
            int[] matchTeams = {eM.getRedAlliance().getAllianceCaptain(), eM.getRedAlliance().getAlliancePick1(), eM.getRedAlliance().getAlliancePick2(), eM.getBlueAlliance().getAllianceCaptain(), eM.getBlueAlliance().getAlliancePick1(), eM.getBlueAlliance().getAlliancePick2()};
            if(redAlliance != null && blueAlliance != null) {
                MatchParticipants[0] = new MatchParticipant(match.getMatchKey(), 11, (redAlliance.getAllianceCaptain()));
                MatchParticipants[1] = new MatchParticipant(match.getMatchKey(), 12, (redAlliance.getAlliancePick1()));
                MatchParticipants[2] = new MatchParticipant(match.getMatchKey(), 13, (redAlliance.getAlliancePick2()));
                MatchParticipants[3] = new MatchParticipant(match.getMatchKey(), 21, (blueAlliance.getAllianceCaptain()));
                MatchParticipants[4] = new MatchParticipant(match.getMatchKey(), 22, (blueAlliance.getAlliancePick1()));
                MatchParticipants[5] = new MatchParticipant(match.getMatchKey(), 23, (blueAlliance.getAlliancePick2()));
                //Because these are elims we can calc no shows base on which are -1
                int i = 0;
                for(int t : matchTeams) {
                    if(t < 1){
                        noShow[i] = true;
                    }
                    i++;
                }
            } else { //Something went wrong. We should never get here, but this is just a saftey backup.
                MatchParticipants[0] = new MatchParticipant(match.getMatchKey(), 11, (eM.getRedAlliance().getAllianceCaptain() == -1) ? 0 : eM.getRedAlliance().getAllianceCaptain());
                MatchParticipants[1] = new MatchParticipant(match.getMatchKey(), 12, (eM.getRedAlliance().getAlliancePick1() == -1) ? 0 : eM.getRedAlliance().getAlliancePick1());
                MatchParticipants[2] = new MatchParticipant(match.getMatchKey(), 13, (eM.getRedAlliance().getAlliancePick2() == -1) ? 0 : eM.getRedAlliance().getAlliancePick2());
                MatchParticipants[3] = new MatchParticipant(match.getMatchKey(), 21, (eM.getBlueAlliance().getAllianceCaptain() == -1) ? 0 : eM.getBlueAlliance().getAllianceCaptain());
                MatchParticipants[4] = new MatchParticipant(match.getMatchKey(), 22, (eM.getBlueAlliance().getAlliancePick1() == -1) ? 0 : eM.getBlueAlliance().getAlliancePick1());
                MatchParticipants[5] = new MatchParticipant(match.getMatchKey(), 23, (eM.getBlueAlliance().getAlliancePick2() == -1) ? 0 : eM.getBlueAlliance().getAlliancePick2());
            }

        } else if(qM != null) {
            surrogate = new boolean[]{qM.getRedAlliance().isTeam1Surrogate, qM.getRedAlliance().isTeam2Surrogate, false, qM.getBlueAlliance().isTeam1Surrogate, qM.getBlueAlliance().isTeam2Surrogate, false};
            MatchParticipants[0] = new MatchParticipant(match.getMatchKey(), 11, qM.getRedAlliance().getTeam1());
            MatchParticipants[1] = new MatchParticipant(match.getMatchKey(), 12, qM.getRedAlliance().getTeam2());
            MatchParticipants[2] = new MatchParticipant(match.getMatchKey(), 13, 0);
            MatchParticipants[3] = new MatchParticipant(match.getMatchKey(), 21, qM.getBlueAlliance().getTeam1());
            MatchParticipants[4] = new MatchParticipant(match.getMatchKey(), 22, qM.getBlueAlliance().getTeam2());
            MatchParticipants[5] = new MatchParticipant(match.getMatchKey(), 23, 0);
        }

        ///dq, no show, surrogates, and yellow cards, with that order of precedence
        MatchParticipants[0].setStationStatus(/*DQ*/dq[0] ? -2 : /*NoShow*/noShow[0] ? -1 : /*Surrogate*/surrogate[0] ? 0 : /*YCard*/yellow[0] ? 2 : 1);
        MatchParticipants[1].setStationStatus(/*DQ*/dq[1] ? -2 : /*NoShow*/noShow[1] ? -1 : /*Surrogate*/surrogate[1] ? 0 : /*YCard*/yellow[1] ? 2 : 1);
        MatchParticipants[2].setStationStatus(/*DQ*/dq[2] ? -2 : /*NoShow*/noShow[2] ? -1 : /*Surrogate*/surrogate[2] ? 0 : /*YCard*/yellow[2] ? 2 : 1);
        MatchParticipants[3].setStationStatus(/*DQ*/dq[3] ? -2 : /*NoShow*/noShow[3] ? -1 : /*Surrogate*/surrogate[3] ? 0 : /*YCard*/yellow[3] ? 2 : 1);
        MatchParticipants[4].setStationStatus(/*DQ*/dq[4] ? -2 : /*NoShow*/noShow[4] ? -1 : /*Surrogate*/surrogate[4] ? 0 : /*YCard*/yellow[4] ? 2 : 1);
        MatchParticipants[5].setStationStatus(/*DQ*/dq[5] ? -2 : /*NoShow*/noShow[5] ? -1 : /*Surrogate*/surrogate[5] ? 0 : /*YCard*/yellow[5] ? 2 : 1);

        match.setPlayNumber((qualMatch.isFinished()) ? 1 : 0);
        match.setIsDone(qualMatch.isFinished());

        match.setScheduledTime(null); //TODO: update when avalible in FIRST API
        match.setLastCommitTime(dateFromUnix(qualMatch.getLastCommitTime()));
        match.setRedPenalty(qualMatch.getBlueSpecifics().getPenaltyPoints());//These are backwards, so we have to do it this way
        match.setBluePenalty(qualMatch.getRedSpecifics().getPenaltyPoints());
        match.setRedAutoScore(qualMatch.getRedSpecifics().getAutoPoints());
        match.setBlueAutoScore(qualMatch.getBlueSpecifics().getAutoPoints());
        match.setRedTeleScore(qualMatch.getRedSpecifics().getTeleopPoints());
        match.setBlueTeleScore(qualMatch.getBlueSpecifics().getTeleopPoints());
        match.setRedEndScore(qualMatch.getRedSpecifics().getEndGamePoints());
        match.setBlueEndScore(qualMatch.getBlueSpecifics().getEndGamePoints());
        match.setRedScore(qualMatch.getRedScore());
        match.setBlueScore(qualMatch.getBlueScore());

        MatchGeneralAndMatchParticipant returnValue = new MatchGeneralAndMatchParticipant();
        returnValue.setMatchGeneral(match);
        returnValue.setMatchParticipants(MatchParticipants);

        return returnValue;
    }

    private String dateFromUnix(Long firstDate) {
        Date date = new Date(firstDate);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    private AllianceFIRST getAllianceFirstFromTeams(int t1, int t2, int t3) {
        AllianceArray alls = null;
        try {
            alls =  FIRSTEndpointNonLambda.getGson().fromJson(FIRSTEndpointNonLambda.getResp("events/" + Config.FIRST_API_EVENT_ID + "/elim/alliances/"), AllianceArray.class);
        } catch (Exception e) {
            controller.sendError("Couldn't Get Alliance for Elim Matches");
        }

        if(alls != null) {
            for(AllianceFIRST a : alls.getAlliances()){
                if(a.getAllianceCaptain() == t1 || a.getAlliancePick1() == t2 || (a.getAlliancePick2() == t3 && t3 > 0)) {
                    if(a.getAlliancePick2() < 1){
                        a.setAlliancePick2(0);
                    }
                    return a;
                }
            }
        }
        return null;
    }

    private MatchDetail1819JSON getSelectedMatchDetails(){
        if(selectedMatch != null){
            return matchDetails.get(selectedMatch);
        }
        return null;
    }

    private void fixTheElimMatches(){
        sf1Matches.sort(Comparator.comparing(MatchGeneral::getMatchName));
        sf2Matches.sort(Comparator.comparing(MatchGeneral::getMatchName));
        fMatches.sort(Comparator.comparing(MatchGeneral::getMatchName));
        int elimMatch = 1;
        int sf1Num = 11;
        int sf2Num = 21;
        int fNum = 1;
        boolean loop = true;
        while(loop) {
            if(!sf1Matches.isEmpty()){
                String matchKey =  Config.EVENT_ID + "-E" + String.format("%03d", elimMatch) + "-1";

                int i = 0;
                //Find the schedual station that goes with our match
                for(MatchParticipant[] s : sf1Sche){
                    if(s[0].getMatchKey().equalsIgnoreCase(sf1Matches.get(0).getMatchKey())){
                       break;
                    } else {
                        i++;
                    }
                }

                int ii = 0;
                for(MatchDetail1819JSON m : sf1MatchDtl){
                    if(m.getMatchKey().equalsIgnoreCase(sf1Matches.get(0).getMatchKey())){
                        break;
                    } else {
                        ii++;
                    }
                }

                sf1Matches.get(0).setMatchKey(matchKey);
                sf1Matches.get(0).setTournamentLevel(MatchGeneral.buildTOATournamentLevel(2, sf1Num));

                sf1Sche.get(i)[0].setMatchKey(matchKey);
                sf1Sche.get(i)[1].setMatchKey(matchKey);
                sf1Sche.get(i)[2].setMatchKey(matchKey);
                sf1Sche.get(i)[3].setMatchKey(matchKey);
                sf1Sche.get(i)[4].setMatchKey(matchKey);
                sf1Sche.get(i)[5].setMatchKey(matchKey);
                sf1Sche.get(i)[0].setStationKey(matchKey + "-R1");
                sf1Sche.get(i)[1].setStationKey(matchKey + "-R2");
                sf1Sche.get(i)[2].setStationKey(matchKey + "-R3");
                sf1Sche.get(i)[3].setStationKey(matchKey + "-B1");
                sf1Sche.get(i)[4].setStationKey(matchKey + "-B2");
                sf1Sche.get(i)[5].setStationKey(matchKey + "-B3");


                sf1MatchDtl.get(ii).setMatchKey(matchKey);
                sf1MatchDtl.get(ii).setMatchDtlKey(matchKey + "-DTL");

                sf1Matches.get(0).setIsUploaded(getUploadedFromMatchKey(sf1Matches.get(0).getMatchKey()));
                if (sf1Matches.get(0).isDone() && !sf1Matches.get(0).isUploaded()) {
                    TOALogger.log(Level.INFO, "Added match " + sf1Matches.get(0).getMatchKey() + " to the upload queue.");
                    uploadQueue.add(sf1Matches.get(0));
                }

                matchList.add(sf1Matches.get(0));
                matchStations.put(sf1Matches.get(0), sf1Sche.get(i));
                matchDetails.put(sf1Matches.get(0), sf1MatchDtl.get(i));

                sf1Num++;
                elimMatch++;
                sf1Matches.remove(0);
                sf1Sche.remove(i);
                sf1MatchDtl.remove(ii);
            }

            if(!sf2Matches.isEmpty()){
                String matchKey =  Config.EVENT_ID + "-E" + String.format("%03d", elimMatch) + "-1";

                int i = 0;
                //Find the schedual station that goes with our match
                for(MatchParticipant[] s : sf2Sche){
                    if(s[0].getMatchKey().equalsIgnoreCase(sf2Matches.get(0).getMatchKey())){
                        break;
                    } else {
                        i++;
                    }
                }

                int ii = 0;
                //Find The MatchDetails that goes with out match
                for(MatchDetail1819JSON m : sf2MatchDtl){
                    if(m.getMatchKey().equalsIgnoreCase(sf2Matches.get(0).getMatchKey())){
                        break;
                    } else {
                        ii++;
                    }
                }

                sf2Matches.get(0).setMatchKey(matchKey);
                sf2Matches.get(0).setTournamentLevel(MatchGeneral.buildTOATournamentLevel(2, sf2Num));

                sf2Sche.get(i)[0].setMatchKey(matchKey);
                sf2Sche.get(i)[1].setMatchKey(matchKey);
                sf2Sche.get(i)[2].setMatchKey(matchKey);
                sf2Sche.get(i)[3].setMatchKey(matchKey);
                sf2Sche.get(i)[4].setMatchKey(matchKey);
                sf2Sche.get(i)[5].setMatchKey(matchKey);
                sf2Sche.get(i)[0].setStationKey(matchKey + "-R1");
                sf2Sche.get(i)[1].setStationKey(matchKey + "-R2");
                sf2Sche.get(i)[2].setStationKey(matchKey + "-R3");
                sf2Sche.get(i)[3].setStationKey(matchKey + "-B1");
                sf2Sche.get(i)[4].setStationKey(matchKey + "-B2");
                sf2Sche.get(i)[5].setStationKey(matchKey + "-B3");

                sf2MatchDtl.get(ii).setMatchKey(matchKey);
                sf2MatchDtl.get(ii).setMatchDtlKey(matchKey + "-DTL");

                sf2Matches.get(0).setIsUploaded(getUploadedFromMatchKey(sf2Matches.get(0).getMatchKey()));
                if (sf2Matches.get(0).isDone() && !sf2Matches.get(0).isUploaded()) {
                    TOALogger.log(Level.INFO, "Added match " + sf2Matches.get(0).getMatchKey() + " to the upload queue.");
                    uploadQueue.add(sf2Matches.get(0));
                }

                matchList.add(sf2Matches.get(0));
                matchStations.put(sf2Matches.get(0), sf2Sche.get(i));
                matchDetails.put(sf2Matches.get(0), sf2MatchDtl.get(ii));


                sf2Num++;
                elimMatch++;
                sf2Matches.remove(0);
                sf2Sche.remove(i);
                sf2MatchDtl.remove(ii);
            }
            if(sf1Matches.isEmpty() && sf2Matches.isEmpty() && !fMatches.isEmpty()) {
                String matchKey =  Config.EVENT_ID + "-E" + String.format("%03d", elimMatch) + "-1";

                int i = 0;
                //Find the schedual station that goes with our match
                for(MatchParticipant[] s : fSche){
                    if(s[0].getMatchKey().equalsIgnoreCase(fMatches.get(0).getMatchKey())){
                        break;
                    } else {
                        i++;
                    }
                }

                int ii = 0;
                //Find The MatchDetails that goes with out match
                for(MatchDetail1819JSON m : fMatchDtl){
                    if(m.getMatchKey().equalsIgnoreCase(fMatches.get(0).getMatchKey())){
                        break;
                    } else {
                        ii++;
                    }
                }

                fMatches.get(0).setMatchKey(matchKey);
                fMatches.get(0).setTournamentLevel(MatchGeneral.buildTOATournamentLevel(3, fNum));

                fSche.get(i)[0].setMatchKey(matchKey);
                fSche.get(i)[1].setMatchKey(matchKey);
                fSche.get(i)[2].setMatchKey(matchKey);
                fSche.get(i)[3].setMatchKey(matchKey);
                fSche.get(i)[4].setMatchKey(matchKey);
                fSche.get(i)[5].setMatchKey(matchKey);
                fSche.get(i)[0].setStationKey(matchKey + "-R1");
                fSche.get(i)[1].setStationKey(matchKey + "-R2");
                fSche.get(i)[2].setStationKey(matchKey + "-R3");
                fSche.get(i)[3].setStationKey(matchKey + "-B1");
                fSche.get(i)[4].setStationKey(matchKey + "-B2");
                fSche.get(i)[5].setStationKey(matchKey + "-B3");

                fMatchDtl.get(ii).setMatchKey(matchKey);
                fMatchDtl.get(ii).setMatchDtlKey(matchKey + "-DTL");

                fMatches.get(0).setIsUploaded(getUploadedFromMatchKey(fMatches.get(0).getMatchKey()));
                if (fMatches.get(0).isDone() && !fMatches.get(0).isUploaded()) {
                    TOALogger.log(Level.INFO, "Added match " + fMatches.get(0).getMatchKey() + " to the upload queue.");
                    uploadQueue.add(fMatches.get(0));
                }

                matchList.add(fMatches.get(0));
                matchStations.put(fMatches.get(0), fSche.get(i));
                matchDetails.put(fMatches.get(0), fMatchDtl.get(i));

                fNum++;
                elimMatch++;
                fMatches.remove(0);
                fSche.remove(i);
                fMatchDtl.remove(ii);
            }
            if(sf1Matches.isEmpty() && sf2Matches.isEmpty() && fMatches.isEmpty()) loop = false;
        }
    }

    private void postCompletedMatches() {
        boolean haveParticipants = true;

        if (uploadQueue.size() > 0) {

            for (MatchGeneral completeMatch : uploadQueue) {
                //Parse Data For Match General
                this.controller.sendInfo("Attempting to upload Match " + completeMatch.getMatchKey());
                String methodType = "POST";
                String putRouteExtra = "";
                for (MatchGeneralJSON match : uploadedMatches) {
                    if (match.getMatchKey().equals(completeMatch.getMatchKey())) {
                        methodType = "PUT";
                        putRouteExtra = "/" + match.getMatchKey();
                    }
                }
                TOAEndpoint matchEndpoint = new TOAEndpoint(methodType, "event/" + Config.EVENT_ID + "/matches" + putRouteExtra);
                matchEndpoint.setCredentials(Config.TOA_API_KEY, Config.EVENT_ID);
                TOARequestBody requestBody = new TOARequestBody();
                MatchGeneralJSON matchJSON = new MatchGeneralJSON();
                matchJSON.setMatchKey(completeMatch.getMatchKey());
                matchJSON.setEventKey(Config.EVENT_ID);
                matchJSON.setScheduledTime(completeMatch.getScheduledTime());
                matchJSON.setFieldNumber(completeMatch.getFieldNumber());
                matchJSON.setMatchName(completeMatch.getMatchName());
                matchJSON.setPlayNumber(1);
                matchJSON.setTournamentLevel(completeMatch.getTournamentLevel());
                matchJSON.setLastCommitTime(completeMatch.getLastCommitTime());
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
                matchJSON.setVideoUrl(completeMatch.getVideoUrl());
                requestBody.addValue(matchJSON);
                matchEndpoint.setBody(requestBody);


                //Parse Data for Match Details

                methodType = "POST";
                putRouteExtra = "";
                if (completeMatch.isUploaded()) {
                    methodType = "PUT";
                } else {
                    for (MatchDetail1819JSON detail : uploadedDetails) {
                        if (detail.getMatchKey().equals(completeMatch.getMatchKey())) {
                            methodType = "PUT";
                        }
                    }
                }

                MatchDetail1819JSON detailJSON = null;

                for (MatchDetail1819JSON matchDetails : this.matchDetails.values()) {
                    if (matchDetails.getMatchKey().equals(completeMatch.getMatchKey())) {
                        detailJSON = matchDetails;
                        if(methodType.equals("PUT")){
                            putRouteExtra = matchDetails.getMatchKey() +  "/";
                        }
                        break;
                    }
                }

                TOAEndpoint detailEndpoint = new TOAEndpoint(methodType, "event/" + Config.EVENT_ID + "/matches/" + putRouteExtra + "details");
                detailEndpoint.setCredentials(Config.TOA_API_KEY, Config.EVENT_ID);
                TOARequestBody detailBody = new TOARequestBody();
                detailBody.addValue(detailJSON);
                detailEndpoint.setBody(detailBody);


                //Parese Data for Match Participants
                methodType = "POST";
                putRouteExtra = "";

                if(completeMatch.isUploaded()){
                    methodType = "PUT";
                } else {
                    for (MatchParticipantJSON matchPar : uploadedMatchParticipants) {
                        if (matchPar.getMatchKey().equals(completeMatch.getMatchKey())) {
                            methodType = "PUT";
                        }
                    }
                }

                MatchParticipant[] mPs = null;

                for (MatchParticipant[] mp : this.matchStations.values()) {
                    if (mp[0].getMatchKey().equals(completeMatch.getMatchKey())) {
                        mPs = mp;
                        if(methodType.equals("PUT")){
                            putRouteExtra = mp[0].getMatchKey() +  "/";
                        }
                        break;
                    }
                }

                TOAEndpoint matchParEndpoint = new TOAEndpoint(methodType, "event/" + Config.EVENT_ID + "/matches/" + putRouteExtra + "participants");
                matchParEndpoint.setCredentials(Config.TOA_API_KEY, Config.EVENT_ID);
                TOARequestBody matchParticipantBody = new TOARequestBody();
                if(mPs != null) {
                    for(MatchParticipant m : mPs){ //Todo: I don't know why these aren't stored in JSON by default, Fix at some point
                        MatchParticipantJSON mPJson = new MatchParticipantJSON();
                        mPJson.setMatchParticipantKey(m.getStationKey());
                        mPJson.setMatchKey(m.getMatchKey());
                        mPJson.setStation(m.getStation());
                        mPJson.setStationStatus(m.getStationStatus());
                        mPJson.setTeamKey(m.getTeamKey() + "");
                        mPJson.setRefStatus(0);//TODO: Fix?
                        if(Integer.parseInt(mPJson.getTeamKey()) > 0) {
                            matchParticipantBody.addValue(mPJson);
                        }
                    }
                } else {
                    //Dont Upload Match, because we don't have participants
                    haveParticipants = false;
                }

                //Dont Upload Match, because we don't have participants
                if(matchParticipantBody.getValues().isEmpty()) {
                    haveParticipants = false;
                }

                matchParEndpoint.setBody(matchParticipantBody);

                /* Execute Queries */
                if(haveParticipants) {
                    //Match General Data
                    matchEndpoint.execute(((response, success) -> {
                        if (success) {
                            TOALogger.log(Level.INFO, "Successfully uploaded results to TOA. " + response);
                        }
                    }));
                    //Match Participants
                    matchParEndpoint.execute(((response, success) -> {
                        if (success) {
                            controller.sendInfo("Successfully uploaded match partipants results to TOA. " + response);
                        } else {
                            controller.sendError("Connection to TOA unsuccessful. " + response);
                        }
                    }));
                    //Match Details
                    detailEndpoint.execute(((response, success) -> {
                        if (success) {
                            uploadQueue.remove(completeMatch);
                            matchList.get(completeMatch.getCanonicalMatchNumber()-1).setIsUploaded(true);
                            controller.tableMatches.refresh();
                            TOALogger.log(Level.INFO, "Successfully uploaded match detail results to TOA. " + response);
                        }
                    }));
                } else {
                    TOALogger.log(Level.INFO, "Didn't upload match " + detailJSON.getMatchKey() + " because there were no event participants.");
                }

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

            //Set Icon because it shows in the task bar
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/app_ico.png")));

            alert.getButtonTypes().setAll(okayButton, cancelButton);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == okayButton) {
                String methodType = "POST";
                String putRouteExtra = "";
                if(uploadedMatches != null){
                    for (MatchGeneralJSON match : uploadedMatches) {
                        if (match.getMatchKey().equals(selectedMatch.getMatchKey())) {
                            methodType = "PUT";
                            putRouteExtra = "/" + match.getMatchKey();
                        }
                    }
                }

                TOAEndpoint matchEndpoint = new TOAEndpoint(methodType, "event/" + Config.EVENT_ID + "/matches" + putRouteExtra);
                matchEndpoint.setCredentials(Config.TOA_API_KEY, Config.EVENT_ID);
                TOARequestBody requestBody = new TOARequestBody();
                MatchGeneralJSON matchJSON = new MatchGeneralJSON();
                matchJSON.setMatchKey(selectedMatch.getMatchKey());
                matchJSON.setEventKey(Config.EVENT_ID);
                matchJSON.setScheduledTime(selectedMatch.getScheduledTime());
                matchJSON.setFieldNumber(selectedMatch.getFieldNumber());
                matchJSON.setMatchName(selectedMatch.getMatchName());
                matchJSON.setPlayNumber(1);
                matchJSON.setTournamentLevel(selectedMatch.getTournamentLevel());
                matchJSON.setLastCommitTime(selectedMatch.getLastCommitTime());
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
                matchJSON.setVideoUrl(selectedMatch.getVideoUrl());
                requestBody.addValue(matchJSON);
                matchEndpoint.setBody(requestBody);
                matchEndpoint.execute(((response, success) -> {
                    if (success) {
                        controller.sendInfo("Successfully uploaded results to TOA. " + response);
                        checkMatchSchedule();
                    } else {
                        controller.sendError("Connection to TOA unsuccessful. " + response);
                    }
                }));

                methodType = "POST";
                putRouteExtra = "";
                if(selectedMatch.isUploaded()){
                    methodType = "PUT";
                } else {
                    for (MatchParticipantJSON matchPar : uploadedMatchParticipants) {
                        if (matchPar.getMatchKey().equals(selectedMatch.getMatchKey())) {
                            methodType = "PUT";
                        }
                    }
                }

                MatchParticipant[] mPs = null;

                for (MatchParticipant[] mp : this.matchStations.values()) {
                    if (mp[0].getMatchKey().equals(selectedMatch.getMatchKey())) {
                        mPs = mp;
                        if(methodType.equals("PUT")){
                            putRouteExtra = mp[0].getMatchKey() + "/";
                        }
                        break;
                    }
                }

                TOAEndpoint matchParEndpoint = new TOAEndpoint(methodType, "event/" + Config.EVENT_ID + "/matches/" + putRouteExtra + "participants");
                matchParEndpoint.setCredentials(Config.TOA_API_KEY, Config.EVENT_ID);
                TOARequestBody matchParticipantBody = new TOARequestBody();
                for(MatchParticipant m : mPs){
                    MatchParticipantJSON mPJson = new MatchParticipantJSON();
                    mPJson.setMatchParticipantKey(m.getStationKey());
                    mPJson.setMatchKey(m.getMatchKey());
                    mPJson.setStation(m.getStation());
                    mPJson.setStationStatus(m.getStationStatus());
                    mPJson.setTeamKey(m.getTeamKey() + "");
                    mPJson.setRefStatus(0);//TODO: Fix?
                    if(Integer.parseInt(mPJson.getTeamKey()) > 0) {
                        matchParticipantBody.addValue(mPJson);
                    }

                }
                matchParEndpoint.setBody(matchParticipantBody);
                matchParEndpoint.execute(((response, success) -> {
                    if (success) {
                        controller.sendInfo("Successfully uploaded match partipants results to TOA. " + response);
                        checkMatchParticipants();
                    } else {
                        controller.sendError("Connection to TOA unsuccessful. " + response);
                    }
                }));

                methodType = "POST";
                putRouteExtra = "";
                if (selectedMatch.isUploaded()) {
                    methodType = "PUT";
                } else {
                    for (MatchDetail1819JSON detail : uploadedDetails) {
                        if (detail.getMatchKey().equals(selectedMatch.getMatchKey())) {
                            methodType = "PUT";
                        }
                    }
                }

                MatchDetail1819JSON detailJSON = null;

                for (MatchDetail1819JSON matchDetails : this.matchDetails.values()) {
                    if (matchDetails.getMatchKey().equals(selectedMatch.getMatchKey())) {
                        detailJSON = matchDetails;
                        if(methodType.equals("PUT")){
                            putRouteExtra = matchDetails.getMatchKey() + "/";
                        }
                        break;
                    }
                }

                TOAEndpoint detailEndpoint = new TOAEndpoint(methodType, "event/" + Config.EVENT_ID + "/matches/" + putRouteExtra + "details");
                detailEndpoint.setCredentials(Config.TOA_API_KEY, Config.EVENT_ID);
                TOARequestBody detailBody = new TOARequestBody();
                detailBody.addValue(detailJSON);
                detailEndpoint.setBody(detailBody);
                detailEndpoint.execute(((response, success) -> {
                    if (success) {
                        for(int i = 0; i < matchList.size(); i++){

                            if(matchList.get(i).getMatchName().equals(selectedMatch.getMatchName())){
                                matchList.get(i).setIsUploaded(true);
                            }

                        }
                        controller.tableMatches.refresh();
                        controller.sendInfo("Successfully uploaded detail results to TOA. " + response);
                        checkMatchDetails();
                        controller.btnMatchBrowserView.setDisable(false);
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

        //Set Icon because it shows in the task bar
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/app_ico.png")));

        alert.getButtonTypes().setAll(okayButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == okayButton) {
            postMatchScheduleTeams();
            postMatchScheduleMatches();

            //Now that we know the schedule is generated, the teams list is final.
            //Lets purge and reupload it.  Thanks @CHEER4FTC, this is actually very important.
            this.controller.teamsController.purgeEventTeams();
            this.controller.teamsController.uploadEventTeams();


            //Do Dashboard Things
            this.controller.cb_matches.setTextFill(Color.GREEN);
            this.controller.cb_matches.setSelected(true);
            this.controller.btn_cb_teams.setDisable(true);

            this.controller.btnMatchUpload.setVisible(true);
            this.controller.btnMatchBrowserView.setVisible(true);
//            TOAEndpoint matchDel = new TOAEndpoint("DELETE", "event/" + Config.EVENT_ID + "/schedule/matches");
//            TOARequestBody matchDelBody = new TOARequestBody();
//            matchDel.setCredentials(Config.TOA_API_KEY, Config.EVENT_ID);
//            matchDelBody.setEventKey(Config.EVENT_ID);
//            matchDel.setBody(matchDelBody);
//            matchDel.execute(((response, success) -> {
//                if (success) {
//                    TOAEndpoint teamDel = new TOAEndpoint("DELETE", "event/" + Config.EVENT_ID + "/schedule/teams");
//                    teamDel.setCredentials(Config.TOA_API_KEY, Config.EVENT_ID);
//                    teamDel.setBody(matchDelBody);
//                    teamDel.execute(((response1, success1) -> {
//                        if (success1) {
//                            postMatchScheduleMatches();
//                            postMatchScheduleTeams();
//
//                            this.controller.btnMatchUpload.setVisible(true);
//                            this.controller.btnMatchSync.setVisible(true);
//                        }
//                    }));
//                }
//            }));
        }
    }

    private void postMatchScheduleTeams() {
        controller.sendInfo("Uploading station schedule data from event " + Config.EVENT_ID + "...");
        TOAEndpoint scheduleEndpoint = new TOAEndpoint("POST", "event/" + Config.EVENT_ID + "/matches/participants");
        scheduleEndpoint.setCredentials(Config.TOA_API_KEY, Config.EVENT_ID);
        TOARequestBody requestBody = new TOARequestBody();
        for (MatchParticipant[] matchPars : matchStations.values()) {

            // TODO - Needs testing!

            boolean uploaded = false;
            if(uploadedMatches != null){
                for (MatchGeneralJSON match : uploadedMatches) {
                    if (match.getMatchKey().equals(matchPars[0].getMatchKey())) {
                        // This match has been uploaded, so do NOT proceed.
                        uploaded = true;
                        break;
                    }
                }
            }

            if (!uploaded) {
                for (MatchParticipant matchPar : matchPars) {
                    if (matchPar.getTeamKey() != 0) {
                        MatchParticipantJSON matchParJSON = new MatchParticipantJSON();
                        matchParJSON.setMatchParticipantKey(matchPar.getStationKey());
                        matchParJSON.setMatchKey(matchPar.getMatchKey());
                        matchParJSON.setStation(matchPar.getStation());
                        matchParJSON.setTeamKey(matchPar.getTeamKey() + "");
                        matchParJSON.setStationStatus(matchPar.getStationStatus());
                        if(!matchParJSON.getTeamKey().equalsIgnoreCase("-1")) {
                            requestBody.addValue(matchParJSON);
                        }

                    }
                }
            }
        }
        scheduleEndpoint.setBody(requestBody);
        scheduleEndpoint.execute(((response, success) -> {
            if (success) {
                controller.sendInfo("Successfully uploaded match schedule team list to TOA. " + response);
                TOALogger.log(Level.INFO, "Match schedule team list successfully posted.");
                this.checkMatchSchedule();
            } else {
                controller.sendError("Connection to TOA unsuccessful. " + response);
                TOALogger.log(Level.SEVERE, "Error posting match schedule team list. " + response);
            }
        }));
    }

    private void postMatchScheduleMatches() {
        controller.sendInfo("Uploading match schedule data from event " + Config.EVENT_ID + "...");
        TOAEndpoint scheduleEndpoint = new TOAEndpoint("POST", "event/" + Config.EVENT_ID + "/matches");
        scheduleEndpoint.setCredentials(Config.TOA_API_KEY, Config.EVENT_ID);
        TOARequestBody requestBody = new TOARequestBody();
        for (int i = 0; i < matchList.size(); i++) {

            MatchGeneral match = matchList.get(i);

            boolean matchParExists = false;
            for (MatchParticipantJSON mPj : uploadedMatchParticipants) {
                if (mPj.getMatchKey().equalsIgnoreCase(match.getMatchKey())) {
                    matchParExists = true;
                }
            }
            if (matchParExists) {
                boolean uploaded = false;
                if (uploadedMatches != null) {
                    for (MatchGeneralJSON uploadedMatch : uploadedMatches) {
                        if (match.getMatchKey().equals(uploadedMatch.getMatchKey())) {
                            uploaded = true;
                            break;
                        }
                    }
                }

                if (!uploaded) {
                    MatchGeneralJSON matchJSON = new MatchGeneralJSON();
                    matchJSON.setEventKey(Config.EVENT_ID);
                    matchJSON.setMatchKey(match.getMatchKey());
                    matchJSON.setTournamentLevel(match.getTournamentLevel());
                    matchJSON.setMatchName(match.getMatchName());
                    matchJSON.setPlayNumber(0);
                    matchJSON.setFieldNumber(match.getFieldNumber());
                    matchJSON.setScheduledTime(match.getScheduledTime());
                    matchJSON.setScoreNull();
                    requestBody.addValue(matchJSON);
                }
            }
        }
        scheduleEndpoint.setBody(requestBody);
        scheduleEndpoint.execute(((response, success) -> {
            if (success) {
                controller.labelScheduleUploaded.setText("Schedule Already Posted.");
                controller.sendInfo("Successfully uploaded match schedule to TOA. " + response);
                TOALogger.log(Level.INFO, "Match schedule successfully posted.");
            } else {
                controller.sendError("Connection to TOA unsuccessful. " + response);
                TOALogger.log(Level.INFO, "Error posting match schedule. " + response);
            }
        }));
    }

    public void deleteMatches(){

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Are you sure about this?");
        alert.setHeaderText("This operation cannot be undone.");
        alert.setContentText("You are about to purge all matches in TOA's databases for event " + Config.EVENT_ID + ". Matches will become unavailable until you re-upload.");

        ButtonType okayButton = new ButtonType("Purge Matches");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        //Set Icon because it shows in the task bar
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/app_ico.png")));

        alert.getButtonTypes().setAll(okayButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == okayButton) {



            /*
            deleteMatchData();
            deleteMatchScheduleMatches();
            deleteMatchScheduleTeams();
            */

            TOAEndpoint matchEp = new TOAEndpoint("DELETE", "event/" + Config.EVENT_ID + "/matches/all");
            matchEp.setCredentials(Config.TOA_API_KEY, Config.EVENT_ID);
            TOARequestBody requestBody = new TOARequestBody();
            matchEp.setBody(requestBody);
            matchEp.execute(((response, success) -> {
                if (success) {
                    TOALogger.log(Level.INFO, "Deleted Matches.");
                    //Do Dashboard Stuff
                    this.controller.cb_matches.setTextFill(Color.RED);
                    this.controller.cb_matches.setSelected(false);
                    this.controller.btn_cb_matches.setDisable(false);
                }else{
                    TOALogger.log(Level.SEVERE, "Failed to delete matches from TOA.");
                }
            }));

            for(MatchGeneral match : matchList){
                match.setIsUploaded(false);
            }
            uploadedDetails.clear();
            uploadedMatches.clear();
            uploadedMatchParticipants.clear();
            checkMatchSchedule();
            checkMatchParticipants();
            checkMatchDetails();
            controller.tableMatches.refresh();
        }

    }

    private void deleteMatchScheduleMatches(){

        TOAEndpoint rankingEndpoint = new TOAEndpoint("DELETE", "event/" + Config.EVENT_ID + "/schedule/matches");
        rankingEndpoint.setCredentials(Config.TOA_API_KEY, Config.EVENT_ID);
        TOARequestBody requestBody = new TOARequestBody();
        rankingEndpoint.setBody(requestBody);
        rankingEndpoint.execute(((response, success) -> {
            if (success) {
                TOALogger.log(Level.INFO, "Deleted Matches.");
            }else{
                TOALogger.log(Level.SEVERE, "Failed to delete matches from TOA.");
            }
        }));

    }

    private void deleteMatchScheduleTeams(){

        TOAEndpoint rankingEndpoint = new TOAEndpoint("DELETE", "event/" + Config.EVENT_ID + "/schedule/teams");
        rankingEndpoint.setCredentials(Config.TOA_API_KEY, Config.EVENT_ID);
        TOARequestBody requestBody = new TOARequestBody();
        rankingEndpoint.setBody(requestBody);
        rankingEndpoint.execute(((response, success) -> {
            if (success) {
                TOALogger.log(Level.INFO, "Deleted Match Teams.");
            }else{
                TOALogger.log(Level.SEVERE, "Failed to delete match teams from TOA.");
            }
        }));

    }

    private void deleteMatchData(){

        //Grab list of matches currently on the TOA servers
        TOAEndpoint matchesEndpoint = new TOAEndpoint("event/" + Config.EVENT_ID + "/matches");
        matchesEndpoint.setCredentials(Config.TOA_API_KEY, Config.EVENT_ID);
        matchesEndpoint.execute(((response, success) -> {
            if (success) {

                //Clear local cache
                uploadedDetails.clear();
                uploadQueue.clear();
                uploadedMatches.clear();

                try {
                    //Get the list of matches from the JSON file
                    MatchGeneralJSON[] matches = matchesEndpoint.getGson().fromJson(response, MatchGeneralJSON[].class);
                    for (MatchGeneralJSON match : matches) {

                        //Purge Match Data
                        TOAEndpoint matchEndpoint = new TOAEndpoint("DELETE", "event/" + Config.EVENT_ID + "/matches");
                        matchEndpoint.setCredentials(Config.TOA_API_KEY, Config.EVENT_ID);
                        TOARequestBody requestBody = new TOARequestBody();
                        matchEndpoint.setBody(requestBody);
                        matchEndpoint.execute(((response1, success1) -> {
                            if (!success1) {
                                TOALogger.log(Level.WARNING, "Failed to remove match: " + match.getMatchKey());
                            }
                        }));

                        //Purge Match Details
                        matchEndpoint = new TOAEndpoint("DELETE", "event/" + Config.EVENT_ID + "/matches/details");
                        matchEndpoint.setCredentials(Config.TOA_API_KEY, Config.EVENT_ID);
                        requestBody = new TOARequestBody();
                        matchEndpoint.setBody(requestBody);
                        matchEndpoint.execute(((response1, success1) -> {
                            if (!success1) {
                                TOALogger.log(Level.WARNING, "Failed to remove match details for: " + match.getMatchKey());
                            }
                        }));

                    }
                }catch (NullPointerException e){

                    TOALogger.log(Level.SEVERE, "Error reading HTTP input for match list.");

                }

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

    public HashMap<MatchGeneral, MatchDetail1819JSON> getMatchDetails(){

        return matchDetails;

    }

    public HashMap<MatchGeneral, MatchDetail1718JSON> getMatchDetails1718(){

        return matchDetails1718;

    }

    public HashMap<Integer, int[]> getTeamWL() {
        return teamWinLoss;
    }

}

