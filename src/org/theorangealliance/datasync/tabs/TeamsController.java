package org.theorangealliance.datasync.tabs;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.theorangealliance.datasync.DataSyncController;
import org.theorangealliance.datasync.json.toa.EventParticipantTeamJSON;
import org.theorangealliance.datasync.json.first.TeamFIRST;
import org.theorangealliance.datasync.json.first.Teams;
import org.theorangealliance.datasync.json.toa.EventParticipantTeamJSONPost;
import org.theorangealliance.datasync.json.toa.TeamJSON;
import org.theorangealliance.datasync.models.Team;
import org.theorangealliance.datasync.util.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Comparator;
import java.util.Optional;

/**
 * Created by Kyle Flynn on 11/29/2017.
 * Modded By Soren for 2018 FIRST Scoring System 11/11/18
 */
public class TeamsController {

    private DataSyncController controller;
    private ObservableList<Team> teamsList;

    public TeamsController(DataSyncController instance) {
        this.controller = instance;
        this.controller.colTeamsTeam.setCellValueFactory(new PropertyValueFactory<Team, Integer>("teamKey"));
        this.controller.colTeamDiv.setCellValueFactory(new PropertyValueFactory<Team, Integer>("teamDivKey"));
        this.controller.colTeamsRegion.setCellValueFactory(new PropertyValueFactory<Team, String>("regionKey"));
        this.controller.colTeamsLeague.setCellValueFactory(new PropertyValueFactory<Team, String>("leagueKey"));
        this.controller.colTeamsShort.setCellValueFactory(new PropertyValueFactory<Team, String>("teamNameShort"));
        this.controller.colTeamsLong.setCellValueFactory(new PropertyValueFactory<Team, String>("teamNameLong"));
        this.controller.colTeamsLocation.setCellValueFactory(new PropertyValueFactory<Team, String>("location"));

        this.teamsList = FXCollections.observableArrayList();
        this.controller.tableTeams.setItems(this.teamsList);

        this.controller.btnTeamsPost.setDisable(true);
        this.controller.btnTeamsDelete.setDisable(true);
    }

    //Get Teams from TOA
    public void getTeamsFromTOA() {
        TOAEndpoint teamsEndpoint = new TOAEndpoint("event/" + Config.EVENT_ID + "/teams");
        teamsEndpoint.setCredentials(Config.TOA_API_KEY, Config.EVENT_ID);
        teamsEndpoint.execute(((response, success) -> {

           if (success) {
               teamsList.clear();
               controller.sendInfo("Successfully pulled teams for event " + Config.EVENT_ID);
               EventParticipantTeamJSON[] teams = teamsEndpoint.getGson().fromJson(response, EventParticipantTeamJSON[].class);
               for (EventParticipantTeamJSON team : teams) {
                   int divKey = 1;
                   if (Config.DUAL_DIVISION_EVENT) {
                       String[] args = team.getParticipantKey().split("-");
                       String eventKey = args[2];
                       try {
                           divKey = Integer.parseInt(eventKey.substring(eventKey.length()-1, eventKey.length()));
                       } catch (Exception e) {
                           divKey = 1;
                       }
                   }
                   Team eventTeam = new Team(
                           team.getTeamKey(),
                           divKey,
                           team.getTeamSpecifics().getTeamRegionKey(),
                           team.getTeamSpecifics().getTeamLeagueKey(),
                           team.getTeamSpecifics().getTeamNameShort(),
                           team.getTeamSpecifics().getTeamNameLong(),
                           team.getTeamSpecifics().getTeamCity() + ", " + team.getTeamSpecifics().getTeamCity() + ", " + team.getTeamSpecifics().getTeamCountry());
                   teamsList.add(eventTeam);
               }

               teamsList.sorted().setComparator(Comparator.comparing(Team::getTeamKey));


               controller.btnTeamsPost.setDisable(false);
               controller.btnTeamsDelete.setDisable(false);

               //Do Dashboard Stuff
               this.controller.cb_teams.setTextFill(Color.GREEN);
               this.controller.cb_teams.setSelected(true);
               this.controller.btn_cb_teams.setDisable(true);
           } else {
               controller.sendError("Connection to TOA unsuccessful. " + response);
               controller.btnTeamsPost.setDisable(true);
               controller.btnTeamsDelete.setDisable(true);
               //Do Dashboard Stuff
               this.controller.cb_teams.setTextFill(Color.RED);
               this.controller.cb_teams.setSelected(false);
               this.controller.btn_cb_teams.setDisable(false);
           }
        }));
    }

    //Get Teams from scoring file
    public void getTeamsByFile() {
        File teamsFile = new File(Config.SCORING_DIR + File.separator + "teams.txt");
        if (teamsFile.exists()) {
            try {
                teamsList.clear();
                BufferedReader reader = new BufferedReader(new FileReader(teamsFile));
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] values = line.split("\\|");
                    Team team = new Team(
                            convertTeamNumToTOA(Integer.parseInt(values[1]), values[6]),
                            Integer.parseInt(values[0]),
                            "",
                            "",
                            values[2],
                            values[3],
                            values[4] + ", " + values[5] + ", " + values[6]
                            );
                    teamsList.add(team);
                }
                reader.close();

                controller.btnTeamsPost.setDisable(false);
                controller.btnTeamsDelete.setDisable(false);
                controller.sendInfo("Successfully imported " + teamsList.size() + " teams from the Scoring System.");
            } catch (Exception e) {
                e.printStackTrace();
                controller.sendError("Could not open file. " + e.getLocalizedMessage());
            }
        } else {
            controller.sendError("Could not locate teams.txt from the Scoring System. Did you setup your event properly?");
        }
    }

    //Get Teams from FIRST API
    public void getTeamsFromFIRSTApi() {
        Teams teams = null;
        try {
            teams = FIRSTEndpointNonLambda.getGson().fromJson(FIRSTEndpointNonLambda.getResp("events/" + Config.FIRST_API_EVENT_ID + "/teams/"), Teams.class);
        } catch(Exception E) {}

        if (teams != null) {

            teamsList.clear();
            controller.sendInfo("Successfully pulled teams from FIRST scoring system.");


            //Now get team data
            for (int tN : teams.getTeamNumber()) {
                TeamFIRST t = null;
                try{
                    t = FIRSTEndpointNonLambda.getGson().fromJson(FIRSTEndpointNonLambda.getResp("teams/" + tN + "/"), TeamFIRST.class);
                }catch(Exception e) {}

                if(t != null) {
                    //TODO: FIX DIVISION INFO
                    Team team = new Team(
                            convertTeamNumToTOA(t.getTeamNumber(), t.getTeamCountry()),
                            0,
                            getRegion(t.getTeamStateProv(), t.getTeamCountry()),
                            null,
                            t.getTeamNameShort(),
                            t.getTeamNameLong(),
                            t.getTeamCity() + ", " + t.getTeamStateProv() + ", " + t.getTeamCountry());
                    teamsList.add(team);
                } else { //We will ask the user for the TOA equivalent of this team #
                    TextInputDialog dialog = new TextInputDialog("");
                    dialog.setTitle("Custom Team Found");
                    dialog.setHeaderText("Team " + tN  + " is a custom team. What is their Team Key on TheOrangeAlliance? (If you can't find it, type \"Not Found\")");
                    dialog.setContentText("Example: ISR11056");

                    Optional<String> result = dialog.showAndWait();
                    result.ifPresent(s1 -> {
                        TOAEndpoint teamsEndpoint = new TOAEndpoint("team/" + result.get());
                        teamsEndpoint.setCredentials(Config.TOA_API_KEY, Config.EVENT_ID);
                        teamsEndpoint.execute(((toaR, toaS) -> {
                            if (toaS) {
                                TeamJSON[] team = teamsEndpoint.getGson().fromJson(toaR, TeamJSON[].class);
                                if(team.length == 1){
                                    Team eventTeam = new Team(
                                            team[0].getTeamKey(),
                                            1,
                                            team[0].getTeamRegionKey(),
                                            team[0].getTeamLeagueKey(),
                                            team[0].getTeamNameShort(),
                                            team[0].getTeamNameLong(),
                                            team[0].getTeamCity() + ", " + team[0].getTeamCity() + ", " + team[0].getTeamCountry());
                                    teamsList.add(eventTeam);
                                } else {
                                    Alert alert = new Alert(Alert.AlertType.ERROR);
                                    alert.setTitle("Error");
                                    alert.setHeaderText("Non-Existant Team");
                                    alert.setContentText("This team is not on TOA, and therefore, must not have been registered in TIMS. Please contact contact@theorangealliance.org soon so we can manually add the team.");

                                    alert.showAndWait();
                                }
                            } else {
                                Alert alert = new Alert(Alert.AlertType.ERROR);
                                alert.setTitle("Error");
                                alert.setHeaderText("Non-Existant Team");
                                alert.setContentText("This team is not on TOA, and therefore, must not have been registered in TIMS. Please contact contact@theorangealliance.org soon so we can manually add the team.");

                                alert.showAndWait();
                                //controller.sendError("Custom Team " + tN + " not found on TOA as " + result.get() + ". Please Reimport teams and try again.");
                            }
                        }));
                    });
                }
            }

            controller.btnTeamsPost.setDisable(false);
            controller.btnTeamsDelete.setDisable(false);
            controller.sendInfo("Successfully imported " + teamsList.size() + " teams from the Scoring System.");

        } else {
            controller.sendError("Connection to FIRST Scoring system unsuccessful.");
        }
    }

    //This will ask the user if they want to upload the teams
    public void postEventTeamsAskUser() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Are you sure about this?");
        alert.setHeaderText("This operation cannot be undone.");
        alert.setContentText("You are about to POST " + teamsList.size() + " teams to TOA's databases. Make sure your team list is correct before uploading.");

        ButtonType okayButton = new ButtonType("Upload Teams");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        //Set Icon because it shows in the task bar
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/app_ico.png")));

        alert.getButtonTypes().setAll(okayButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == okayButton) {
            uploadEventTeams();
        }
    }

    //This will upload all of the event participants
    public void uploadEventTeams(){
        teamsList.sort((team1, team2) -> (Integer.parseInt(team1.getTeamKey().replaceAll("[^\\d.]", "")) > Integer.parseInt(team2.getTeamKey().replaceAll("[^\\d.]", "")) ? 1 : -1));
        controller.sendInfo("Uploading data from event " + Config.EVENT_ID + "...");
        TOAEndpoint postEndpoint = new TOAEndpoint("POST", "event/" + Config.EVENT_ID + "/teams");
        postEndpoint.setCredentials(Config.TOA_API_KEY, Config.EVENT_ID);
        TOARequestBody requestBody = new TOARequestBody();
        int div1 = 0;
        int div2 = 0;
        for (int i = 0; i < teamsList.size(); i++) {
            Team team = teamsList.get(i);
            EventParticipantTeamJSONPost eventTeam = new EventParticipantTeamJSONPost();

            if (Config.DUAL_DIVISION_EVENT) {
                String newEventID = Config.EVENT_ID.substring(0, Config.EVENT_ID.length()-1);
                if (team.getTeamDivKey() == 1) {
                    eventTeam.setParticipantKey(newEventID + team.getTeamDivKey() + "-T" + (div1+1));
                    eventTeam.setEventKey(newEventID + team.getTeamDivKey());
                    div1++;
                } else {
                    eventTeam.setParticipantKey(newEventID + team.getTeamDivKey() + "-T" + (div2+1));
                    eventTeam.setEventKey(newEventID + team.getTeamDivKey());
                    div2++;
                }
            } else {
                eventTeam.setParticipantKey(Config.EVENT_ID + "-T" + (i+1));
                eventTeam.setEventKey(Config.EVENT_ID);
            }
            eventTeam.setTeamKey(team.getTeamKey());
            eventTeam.setTeamIsActive(true);
            requestBody.addValue(eventTeam);
        }
        postEndpoint.setBody(requestBody);
        postEndpoint.execute((response, success) -> {
            if (success) {
                controller.sendInfo("Successfully uploaded data to TOA. " + response);
                //Do Checklist stuff
                controller.cb_teams.setSelected(true);
                controller.cb_teams.setTextFill(Color.GREEN);
                controller.cb_teams.setDisable(true);
                controller.btn_cb_teams.setDisable(true);
                controller.btnTeamsPost.setDisable(true);
            } else {
                controller.sendError("Connection to TOA unsuccessful. " + response);
            }
        });
    }

    //This will ask the user if they want to delete the teams
    public void deleteEventTeamsAskUser() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Are you sure about this?");
        alert.setHeaderText("This operation cannot be undone.");
        alert.setContentText("You are about to purge TOA's EventParticipant data for EventID " + Config.EVENT_ID + ". We don't recommend doing this unless our databases are wrong and you have teams present/not present at your event.");

        ButtonType okayButton = new ButtonType("Purge Anyway");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        //Set Icon because it shows in the task bar
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/app_ico.png")));

        alert.getButtonTypes().setAll(okayButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == okayButton) {
            purgeEventTeams();
        }
    }

    //This will delete all EventParticipants on TOA
    public void purgeEventTeams(){
        // Begin the purging of the data table...
        controller.sendInfo("Purging EventParticipant data from event " + Config.EVENT_ID + "...");
        TOAEndpoint deleteEndpoint = new TOAEndpoint("DELETE", "event/" + Config.EVENT_ID + "/teams");
        deleteEndpoint.setCredentials(Config.TOA_API_KEY, Config.EVENT_ID);
        TOARequestBody requestBody = new TOARequestBody();
        deleteEndpoint.setBody(requestBody);
        deleteEndpoint.execute((response, success) -> {
            if (success) {
                controller.sendInfo("Successfully purged data from TOA. " + response);
                //Do Checklist stuff
                controller.cb_teams.setSelected(false);
                controller.cb_teams.setTextFill(Color.RED);
                controller.btn_cb_teams.setDisable(false);
                controller.btnTeamsPost.setDisable(false);
            } else {
                controller.sendError("Connection to TOA unsuccessful. " + response);
            }
        });
    }

    //This will get convert a state to a region
    private String getRegion(String state, String country){
        if(country.equalsIgnoreCase("usa") || country.equalsIgnoreCase("canada")) {
            if (state.equalsIgnoreCase("MI")) return "FIM";
            else return state;
        } else {
            return countryCodeConvert(country);
        }
    }

    //This will convert a team number to a TOA team number
    private String convertTeamNumToTOA(int teamNum, String country){
        if(country.equalsIgnoreCase("usa") || (country.equalsIgnoreCase("canada"))){
            return teamNum + "";
        } else {
            return countryCodeConvert(country) + teamNum;
        }
    }

    // Convert a country name to it's 3 digit UN identifier.
    private String countryCodeConvert(String country) {
        switch (country) {
            case "Albania"              : return "ALB";
            case "Australia"            : return "AUS";
            case "Austria"              : return "AUT";
            case "Bulgaria"             : return "BGR";
            case "Cayman Islands"       : return "CYM";
            case "Chile"                : return "CHL";
            case "China"                : return "CHT";
            case "Chinese Taipei"       : return "TWN";
            case "Costa Rica"           : return "CRI";
            case "Croatia"              : return "HRV";
            case "Cyprus"               : return "CYP";
            case "Czech Republic"       : return "CZE";
            case "Egypt"                : return "EGY";
            case "France"               : return "FRA";
            case "Germany"              : return "DEU";
            case "India"                : return "IND";
            case "Israel"               : return "ISR";
            case "Italy"                : return "ITA";
            case "Latvia"               : return "LVA";
            case "Lebanon"              : return "LBN";
            case "Jamaica"              : return "JAM";
            case "Japan"                : return "JPN";
            case "Mali"                 : return "MLI";
            case "Mexico"               : return "MEX";
            case "Netherlands"          : return "NLD";
            case "New Zealand"          : return "NZL";
            case "Nigeria"              : return "NGA";
            case "Norway"               : return "NOR";
            case "Poland"               : return "POL";
            case "Portugal"             : return "PRT";
            case "Romania"              : return "ROU";
            case "Russia"               : return "RUS";
            case "Saudi Arabia"         : return "SAU";
            case "South Korea"          : return "SKR";
            case "South Africa"         : return "ZAF";
            case "Serbia"               : return "SRB";
            case "Singapore"            : return "SGP";
            case "Slovenia"             : return "SVN";
            case "Spain"                : return "ESP";
            case "Sweden"               : return "SWE";
            case "Taiwan"               : return "TWN";
            case "Thailand"             : return "THA";
            case "Tonga"                : return "TON";
            case "Turkey"               : return "TUR";
            case "Tunisia"              : return "TUN";
            case "Uganda"               : return "UGA";
            case "United Kingdom"       : return "GBR";
            case "Ukraine"              : return "UKR";
            case "Vietnam"              : return "VMN";
            case "Zimbabwe"             : return "ZWE";
            default:
                //If we don't have a country code for right now, we'll print it out and then we'll take the first 3 digits of the name of the country
                String threeDigitCode = country.substring(0, Math.min(country.length(), 3)).toUpperCase();
                //Then We'll print it out, so this never happens again.
                System.out.println("Didn't Have country code for '" + country + "'. Instead, went with '" + threeDigitCode + "'.");
                return threeDigitCode;

        }
    }

}
