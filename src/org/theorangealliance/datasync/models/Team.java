package org.theorangealliance.datasync.models;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Created by Kyle Flynn on 11/28/2017.
 */
public class Team {

    private final SimpleStringProperty teamKey;
    private final SimpleIntegerProperty teamDivKey;
    private final SimpleStringProperty regionKey;
    private final SimpleStringProperty leagueKey;
    private final SimpleStringProperty teamNameShort;
    private final SimpleStringProperty teamNameLong;
    private final SimpleStringProperty location;

    public Team(String teamKey, int teamDivKey, String regionKey, String leagueKey, String teamNameShort, String teamNameLong, String location) {
        this.teamKey = new SimpleStringProperty(teamKey);
        this.teamDivKey = new SimpleIntegerProperty(teamDivKey);
        this.regionKey = new SimpleStringProperty(regionKey);
        this.leagueKey = new SimpleStringProperty(leagueKey);
        this.teamNameShort = new SimpleStringProperty(teamNameShort);
        this.teamNameLong = new SimpleStringProperty(teamNameLong);
        this.location = new SimpleStringProperty(location);
    }



    public void setTeamKey(String teamKey) {
        this.teamKey.set(teamKey);
    }

    public void setTeamDivKey(int teamDivKey) {
        this.teamDivKey.set(teamDivKey);
    }

    public void setRegionKey(String regionKey) {
        this.regionKey.set(regionKey);
    }

    public void setLeagueKey(String leagueKey) {
        this.leagueKey.set(leagueKey);
    }

    public void setTeamNameShort(String teamNameShort) {
        this.teamNameShort.set(teamNameShort);
    }

    public void setTeamNameLong(String teamNameLong) {
        this.teamNameLong.set(teamNameLong);
    }

    public void setLocation(String location) {
        this.location.set(location);
    }

    public String getTeamKey() {
        return this.teamKey.get();
    }

    public int getTeamDivKey() {
        return this.teamDivKey.get();
    }

    public String getRegionKey() {
        return this.regionKey.get();
    }

    public String getLeagueKey() {
        return this.leagueKey.get();
    }

    public String getTeamNameShort() {
        return this.teamNameShort.get();
    }

    public String getTeamNameLong() {
        return this.teamNameLong.get();
    }

    public String getLocation() {
        return this.location.get();
    }

}
