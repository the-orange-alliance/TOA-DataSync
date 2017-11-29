package org.theorangealliance.datasync.tabs;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import org.theorangealliance.datasync.DataSyncController;
import org.theorangealliance.datasync.json.TeamJSON;
import org.theorangealliance.datasync.models.Team;
import org.theorangealliance.datasync.util.Config;
import org.theorangealliance.datasync.util.TOAEndpoint;

/**
 * Created by Kyle Flynn on 11/29/2017.
 */
public class TeamsController {

    private DataSyncController controller;

    private ObservableList<Team> teamsList;

    public TeamsController(DataSyncController instance) {
        this.controller = instance;
        this.controller.colTeamsTeam.setCellValueFactory(new PropertyValueFactory<Team, Integer>("teamKey"));
        this.controller.colTeamsRegion.setCellValueFactory(new PropertyValueFactory<Team, String>("regionKey"));
        this.controller.colTeamsLeague.setCellValueFactory(new PropertyValueFactory<Team, String>("leagueKey"));
        this.controller.colTeamsShort.setCellValueFactory(new PropertyValueFactory<Team, String>("teamNameShort"));
        this.controller.colTeamsLong.setCellValueFactory(new PropertyValueFactory<Team, String>("teamNameLong"));
        this.controller.colTeamsLocation.setCellValueFactory(new PropertyValueFactory<Team, String>("location"));

        Team team = new Team(5411, "FiM", "", "Geeks, Gears, and Gadgets: Green", "Petoskey Middle School", "Petoskey, MI, USA");

        this.teamsList = FXCollections.observableArrayList();
        this.controller.tableTeams.setItems(this.teamsList);
    }

    public void getTeamsByURL() {
        TOAEndpoint teamsEndpoint = new TOAEndpoint("event/" + Config.EVENT_ID + "/teams");
        teamsEndpoint.setCredentials(Config.EVENT_API_KEY, Config.EVENT_ID);
        teamsEndpoint.execute(((response, success) -> {
           if (success) {
               teamsList.clear();
               controller.sendInfo("Successfully pulled teams for event " + Config.EVENT_ID);
               TeamJSON[] teams = teamsEndpoint.getGson().fromJson(response, TeamJSON[].class);
               for (TeamJSON team : teams) {
                   Team eventTeam = new Team(
                           team.getTeamKey(),
                           team.getRegionKey(),
                           team.getLeagueKey(),
                           team.getTeamNameShort(),
                           team.getTeamNameLong(),
                           team.getLocation());
                   teamsList.add(eventTeam);
               }
           } else {
               controller.sendError("Connection to TOA unsuccessful. " + response);
           }
        }));
    }
}
