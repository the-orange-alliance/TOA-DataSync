package org.theorangealliance.datasync.tabs;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.theorangealliance.datasync.DataSyncController;
import org.theorangealliance.datasync.json.toa.TeamRankingJSON;
import org.theorangealliance.datasync.logging.TOALogger;
import org.theorangealliance.datasync.json.first.TeamRankingFIRST;
import org.theorangealliance.datasync.json.first.TeamRankingFIRSTArray;
import org.theorangealliance.datasync.models.TeamRanking;
import org.theorangealliance.datasync.util.Config;
import org.theorangealliance.datasync.util.FIRSTEndpoint;
import org.theorangealliance.datasync.util.TOAEndpoint;
import org.theorangealliance.datasync.util.TOARequestBody;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;

/**
 * Created by Kyle Flynn on 12/1/2017.
 */
public class RankingsController {

    private DataSyncController controller;
    private ObservableList<TeamRanking> teamRankings;

    public RankingsController(DataSyncController instance) {
        this.controller = instance;
        this.teamRankings = FXCollections.observableArrayList();
        this.controller.btnRankUpload.setDisable(true);
        this.controller.colRank.setCellValueFactory(new PropertyValueFactory<>("rank"));
        this.controller.colRankTeam.setCellValueFactory(new PropertyValueFactory<>("teamKey"));
        this.controller.colRankWins.setCellValueFactory(new PropertyValueFactory<>("wins"));
        this.controller.colRankLosses.setCellValueFactory(new PropertyValueFactory<>("losses"));
        this.controller.colRankTies.setCellValueFactory(new PropertyValueFactory<>("ties"));
        this.controller.colRankRP.setCellValueFactory(new PropertyValueFactory<>("rankPoints"));
        this.controller.colRankTBP.setCellValueFactory(new PropertyValueFactory<>("tieBreakerPoints"));
        this.controller.colRankScore.setCellValueFactory(new PropertyValueFactory<>("highestScore"));
        this.controller.colRankPlayed.setCellValueFactory(new PropertyValueFactory<>("played"));

        this.controller.tableRankings.setItems(this.teamRankings);
    }

    public void syncRankings() {
        if (teamRankings.size() <= 0) {
            getRankingsByFile();
        } else {
            try {
                File rankReport;
                if (Config.DUAL_DIVISION_EVENT) {
                    rankReport = new File(Config.SCORING_DIR + File.separator + "reports" + File.separator + "Rankings_" + Config.EVENT_NAME.replace(" ", "_") + "_" + Config.DIVISION_NAME.replace(" ", "_") + ".html");
                } else {
                    rankReport = new File(Config.SCORING_DIR + File.separator + "reports" + File.separator + "Rankings_" + Config.EVENT_NAME.replace(" ", "_") + ".html");
                }
                Document rankDoc = Jsoup.parse(rankReport, "UTF-8");
                Element tableBody = rankDoc.body().getElementsByAttribute("cellpadding").first().child(0);
                Elements rankElems = tableBody.getElementsByAttribute("align");
                teamRankings.sort((team1, team2) -> (team1.getRank() > team2.getRank() ? 1 : -1));
                for (Element e : rankElems) {
                    if (e.isBlock() && e.select("th").size() == 0) {
                        int rank = Integer.parseInt(e.child(0).text());
                        int team = Integer.parseInt(e.child(1).text());
                        int qualPoints = Integer.parseInt(e.child(3).text());
                        int rankPoints = Integer.parseInt(e.child(4).text());
                        int highScore = Integer.parseInt(e.child(5).text());
                        int played = Integer.parseInt(e.child(6).text());
                        TeamRanking ranking = teamRankings.get(rank-1);
                        //ranking.setQualPoints(qualPoints);
                        ranking.setRankPoints(rankPoints);
                        ranking.setHighestScore(highScore);
                        ranking.setPlayed(played);
                        int[] results = controller.getTeamWL().get(team);
                        if (results != null) {
                            ranking.setWins(results[0]);
                            ranking.setLosses(results[1]);
                            ranking.setTies(results[2]);
                        }
                    }
                }
                controller.tableRankings.refresh();
                TOALogger.log(Level.INFO, "Rankings sync successful.");
            } catch (Exception e) {
                TOALogger.log(Level.SEVERE, "Error reading rankings file: " + e.getLocalizedMessage());
            }
        }
    }

    public void getRankingsByFile() {
        try {
            File rankReport;
            if (Config.DUAL_DIVISION_EVENT) {
                rankReport = new File(Config.SCORING_DIR + File.separator + "reports" + File.separator + "Rankings_" + Config.EVENT_NAME.replace(" ", "_") + "_" + Config.DIVISION_NAME.replace(" ", "_") + ".html");
            } else {
                rankReport = new File(Config.SCORING_DIR + File.separator + "reports" + File.separator + "Rankings_" + Config.EVENT_NAME.replace(" ", "_") + ".html");
            }
            Document rankDoc = Jsoup.parse(rankReport, "UTF-8");
            Element tableBody = rankDoc.body().getElementsByAttribute("cellpadding").first().child(0);
            Elements rankElems = tableBody.getElementsByAttribute("align");
            teamRankings.clear();
            for (Element e : rankElems) {
                if (e.isBlock() && e.select("th").size() == 0) {
                    int rank = Integer.parseInt(e.child(0).text());
                    int team = Integer.parseInt(e.child(1).text());
                    int qualPoints = Integer.parseInt(e.child(3).text());
                    int rankPoints = Integer.parseInt(e.child(4).text());
                    int highScore = Integer.parseInt(e.child(5).text());
                    int played = Integer.parseInt(e.child(6).text());
                    TeamRanking ranking = new TeamRanking(rank, team);
                    //ranking.setQualPoints(qualPoints);
                    ranking.setRankPoints(rankPoints);
                    ranking.setHighestScore(highScore);
                    ranking.setPlayed(played);
                    int[] results = controller.getTeamWL().get(team);
                    if (results != null) {
                        ranking.setWins(results[0]);
                        ranking.setLosses(results[1]);
                        ranking.setTies(results[2]);
                    }
                    teamRankings.add(ranking);
                }
                this.controller.btnRankUpload.setDisable(false);
            }
            TOALogger.log(Level.INFO, "Rankings import successful.");
        } catch (IOException ex) {
            TOALogger.log(Level.SEVERE, "Error reading rankings file: " + ex.getLocalizedMessage());
        }
    }

    public void getRankingsFIRSTApi(){
        //TODO: Add Division Support
        FIRSTEndpoint firstRankings = new FIRSTEndpoint("events/" + Config.FIRST_API_EVENT_ID + "/rankings/");
        firstRankings.execute(((response, success) -> {
            if (success) {
                TeamRankingFIRSTArray ranks = firstRankings.getGson().fromJson(response, TeamRankingFIRSTArray.class);
                teamRankings.clear();

                for(TeamRankingFIRST tr : ranks.getTeamRankings()) {
                    TeamRanking ranking = new TeamRanking(tr.getTeamRanking(), tr.getTeamNumber());
                    int[] results = controller.getTeamWL().get(tr.getTeamNumber());
                    //TODO: Update when more data is added to API
                    ranking.setTieBreakerPoints(tr.getTeamTieBreakerPoints());
                    ranking.setRankPoints(tr.getTeamRP());
                    //ranking.setHighestScore(highScore);
                    ranking.setPlayed(tr.getTeamMatchesPlayed());
                    if (results != null) {
                        ranking.setWins(results[0]);
                        ranking.setLosses(results[1]);
                        ranking.setTies(results[2]);
                    }
                    teamRankings.add(ranking);
                }
                this.controller.btnRankUpload.setDisable(false);
            } else {
                controller.sendError("Connection to FIRST Scoring system unsuccessful.  " + response);
            }
        }));
    }

    public void postRankings() {
        if (teamRankings.size() > 0) {
            /* We HAVE to make a DELETE request first
               This is because MySQL UPDATE query does not
               allow multiple row updates...*/
            TOAEndpoint deleteEndpoint = new TOAEndpoint("DELETE", "event/" + Config.EVENT_ID + "/rankings");
            deleteEndpoint.setCredentials(Config.TOA_API_KEY, Config.EVENT_ID);
            TOARequestBody deleteBody = new TOARequestBody();
            deleteEndpoint.setBody(deleteBody);
            deleteEndpoint.execute(((response, success) -> {
                if (success) {
                    TOALogger.log(Level.INFO, "Deleted rankings. Now posting rankings.");
                    TOAEndpoint postEndpoint = new TOAEndpoint("POST", "event/" + Config.EVENT_ID + "/rankings");
                    postEndpoint.setCredentials(Config.TOA_API_KEY, Config.EVENT_ID);
                    TOARequestBody postBody = new TOARequestBody();
                    for (TeamRanking ranking : teamRankings) {
                        TeamRankingJSON rankJSON = new TeamRankingJSON();
                        rankJSON.setTeamRankKey(Config.EVENT_ID + "-R" + ranking.getTeamKey());
                        rankJSON.setEventKey(Config.EVENT_ID);
                        rankJSON.setTeamKey(ranking.getTeamKey());
                        rankJSON.setHighestScore(ranking.getHighestScore());
                        rankJSON.setQualPoints(0);
                        rankJSON.setRankPoints(ranking.getRankPoints());
                        rankJSON.setTieBreakerPoints(ranking.getTieBreakerPoints());
                        rankJSON.setDisqualified(0);
                        rankJSON.setPlayed(ranking.getPlayed());
                        rankJSON.setWins(ranking.getWins());
                        rankJSON.setLosses(ranking.getLosses());
                        rankJSON.setTies(ranking.getTies());
                        rankJSON.setRank(ranking.getRank());
                        rankJSON.setRankChange(0);
                        postBody.addValue(rankJSON);
                    }
                    postEndpoint.setBody(postBody);
                    postEndpoint.execute(((response1, success1) -> {
                        if (success1) {
                            TOALogger.log(Level.INFO, "Successfully posted rankings.");
                        }
                    }));
                }
            }));
        } else {
            TOALogger.log(Level.WARNING, "There are no team rankings to upload.");
        }
    }

    public void deleteRankings() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Are you sure about this?");
        alert.setHeaderText("This operation cannot be undone.");
        alert.setContentText("You are about to purge team standings in TOA's databases for event " + Config.EVENT_ID + ". Rankings will become unavailable until you re-upload.");

        ButtonType okayButton = new ButtonType("Purge Rankings");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        //Set Icon because it shows in the task bar
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/app_ico.png")));

        alert.getButtonTypes().setAll(okayButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == okayButton) {
            TOAEndpoint rankingEndpoint = new TOAEndpoint("DELETE", "event/" + Config.EVENT_ID + "/rankings");
            rankingEndpoint.setCredentials(Config.TOA_API_KEY, Config.EVENT_ID);
            TOARequestBody requestBody = new TOARequestBody();
            //requestBody.setEventKey(Config.EVENT_ID);
            rankingEndpoint.setBody(requestBody);
            rankingEndpoint.execute(((response, success) -> {
                if (success) {
                    TOALogger.log(Level.INFO, "Deleted rankings.");
                }
            }));
        }
    }

}
