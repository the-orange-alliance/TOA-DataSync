package org.theorangealliance.datasync.tabs;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.cell.PropertyValueFactory;
import org.theorangealliance.datasync.DataSyncController;
import org.theorangealliance.datasync.json.toa.EventParticipantTeamJSON;
import org.theorangealliance.datasync.json.first.TeamFIRST;
import org.theorangealliance.datasync.json.first.Teams;
import org.theorangealliance.datasync.json.toa.EventParticipantTeamJSONPost;
import org.theorangealliance.datasync.models.Team;
import org.theorangealliance.datasync.util.Config;
import org.theorangealliance.datasync.util.FIRSTEndpoint;
import org.theorangealliance.datasync.util.TOAEndpoint;
import org.theorangealliance.datasync.util.TOARequestBody;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

/**
 * Created by Kyle Flynn on 11/29/2017.
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

    public void getTeamsByURL() {
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
                //TODO: Fix for International Team
               teamsList.sort((team1, team2) -> (Integer.parseInt(team1.getTeamKey()) > Integer.parseInt(team2.getTeamKey()) ? 1 : -1));

               controller.btnTeamsPost.setDisable(false);
               controller.btnTeamsDelete.setDisable(false);
           } else {
               controller.sendError("Connection to TOA unsuccessful. " + response);
               controller.btnTeamsPost.setDisable(true);
               controller.btnTeamsDelete.setDisable(true);
           }
        }));
    }

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

    public void getTeamsFromFIRSTApi() {
        FIRSTEndpoint firstEventTeams = new FIRSTEndpoint("events/" + Config.FIRST_API_EVENT_ID + "/teams/");
        firstEventTeams.execute(((response, success) -> {
            if (success) {
                teamsList.clear();
                controller.sendInfo("Successfully pulled teams from FIRST scoring system.");

                //Get Team list from score system
                Teams teams = firstEventTeams.getGson().fromJson(response, Teams.class);

                //Now get team data
                for (int tN : teams.getTeamNumber()) {
                    FIRSTEndpoint firstTeamData = new FIRSTEndpoint("teams/" + tN + "/");
                    firstTeamData.execute(((r, s) -> {
                        if(s) {
                            TeamFIRST t = firstTeamData.getGson().fromJson(r, TeamFIRST.class);
                            //TODO: FIX DIVISION INFO
                            Team team = new Team(
                                    convertTeamNumToTOA(t.getTeamNumber(),
                                    t.getTeamCountry()),
                                    0,
                                    getRegion(t.getTeamStateProv(), t.getTeamCountry()),
                                    null,
                                    t.getTeamNameShort(),
                                    t.getTeamNameLong(),
                                    t.getTeamCity() + ", " + t.getTeamStateProv() + ", " + t.getTeamCountry());
                            teamsList.add(team);
                        } //We have bigger problems if this fails
                    }));
                }

                controller.btnTeamsPost.setDisable(false);
                controller.btnTeamsDelete.setDisable(false);
                controller.sendInfo("Successfully imported " + teamsList.size() + " teams from the Scoring System.");

            } else {
                controller.sendError("Connection to FIRST Scoring system unsuccessful. " + response);
            }
        }));
    }

    public void getTeamsFromDB(ResultSet rs){
        int i = 0;
        try {
            while (rs.next()) {
                int teamNum = rs.getInt(1);
                System.out.println(teamNum);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void postEventTeams() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Are you sure about this?");
        alert.setHeaderText("This operation cannot be undone.");
        alert.setContentText("You are about to POST " + teamsList.size() + " teams to TOA's databases. Make sure your team list is correct before uploading.");

        ButtonType okayButton = new ButtonType("Upload Teams");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(okayButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == okayButton) {
            //TODO: Fix For international teams at some point
            teamsList.sort((team1, team2) -> (Integer.parseInt(team1.getTeamKey()) > Integer.parseInt(team2.getTeamKey()) ? 1 : -1));
            controller.sendInfo("Uploading data from event " + Config.EVENT_ID + "...");
            TOAEndpoint deleteEndpoint = new TOAEndpoint("POST", "event/" + Config.EVENT_ID + "/teams");
            deleteEndpoint.setCredentials(Config.TOA_API_KEY, Config.EVENT_ID);
            TOARequestBody requestBody = new TOARequestBody();
            //requestBody.setEventKey(Config.EVENT_ID);
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
                eventTeam.setTeamNumber(Integer.parseInt(team.getTeamKey()));
                eventTeam.setTeamIsActive(true);
                requestBody.addValue(eventTeam);
            }
            deleteEndpoint.setBody(requestBody);
            deleteEndpoint.execute((response, success) -> {
                if (success) {
                    controller.sendInfo("Successfully uploaded data to TOA. " + response);
                } else {
                    controller.sendError("Connection to TOA unsuccessful. " + response);
                }
            });
        }
    }

    public void deleteEventTeams() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Are you sure about this?");
        alert.setHeaderText("This operation cannot be undone.");
        alert.setContentText("You are about to purge TOA's EventParticipant data for EventID " + Config.EVENT_ID + ". We don't recommend doing this unless our databases are wrong and you have teams present/not present at your event.");

        ButtonType okayButton = new ButtonType("Purge Anyway");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(okayButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == okayButton) {
            // Begin the purging of the data table...
            controller.sendInfo("Purging data from event " + Config.EVENT_ID + "...");
            TOAEndpoint deleteEndpoint = new TOAEndpoint("DELETE", "event/" + Config.EVENT_ID + "/teams");
            deleteEndpoint.setCredentials(Config.TOA_API_KEY, Config.EVENT_ID);
            TOARequestBody requestBody = new TOARequestBody();
            //requestBody.setEventKey(Config.EVENT_ID);
            deleteEndpoint.setBody(requestBody);
            deleteEndpoint.execute((response, success) -> {
                if (success) {
                    controller.sendInfo("Successfully purged data from TOA. " + response);
                } else {
                    controller.sendError("Connection to TOA unsuccessful. " + response);
                }
            });
        }
    }

    private String getCurrentTime() {
        Date dt = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        return sdf.format(dt);
    }

    private String getRegion(String state, String country){
        if(country.equalsIgnoreCase("usa") || country.equalsIgnoreCase("canada")) {
            if (state.equalsIgnoreCase("MI")) return "FIM";
            else return state;
        } else {
            return countryCodeConvert(country);
        }
    }

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
