package org.theorangealliance.datasync.json.toa;

import com.google.gson.annotations.SerializedName;

public class TeamJSON {

    @SerializedName("team_key")
    private String teamKey;

    @SerializedName("region_key")
    private String teamRegionKey;

    @SerializedName("league_key")
    private String teamLeagueKey;

    @SerializedName("team_number")
    private int teamNumber;

    @SerializedName("team_name_short")
    private String teamNameShort;

    @SerializedName("team_name_long")
    private String teamNameLong;

    @SerializedName("robot_name")
    private String teamRobotName;

    @SerializedName("last_active")
    private String teamLastActive;

    @SerializedName("city")
    private String teamCity;

    @SerializedName("state_prov")
    private String teamStateProv;

    @SerializedName("zip_code")
    private String teamZipCode;

    @SerializedName("country")
    private String teamCountry;

    @SerializedName("rookie_year")
    private int teamRookieYear;

    @SerializedName("website")
    private String teamWebsite;

    public String getTeamKey() {
        return teamKey;
    }

    public void setTeamKey(String teamKey) {
        this.teamKey = teamKey;
    }

    public String getTeamRegionKey() {
        return teamRegionKey;
    }

    public void setTeamRegionKey(String teamRegionKey) {
        this.teamRegionKey = teamRegionKey;
    }

    public String getTeamLeagueKey() {
        return teamLeagueKey;
    }

    public void setTeamLeagueKey(String teamLeagueKey) {
        this.teamLeagueKey = teamLeagueKey;
    }

    public int getTeamNumber() {
        return teamNumber;
    }

    public void setTeamNumber(int teamNumber) {
        this.teamNumber = teamNumber;
    }

    public String getTeamNameShort() {
        return teamNameShort;
    }

    public void setTeamNameShort(String teamNameShort) {
        this.teamNameShort = teamNameShort;
    }

    public String getTeamNameLong() {
        return teamNameLong;
    }

    public void setTeamNameLong(String teamNameLong) {
        this.teamNameLong = teamNameLong;
    }

    public String getTeamRobotName() {
        return teamRobotName;
    }

    public void setTeamRobotName(String teamRobotName) {
        this.teamRobotName = teamRobotName;
    }

    public String getTeamLastActive() {
        return teamLastActive;
    }

    public void setTeamLastActive(String teamLastActive) {
        this.teamLastActive = teamLastActive;
    }

    public String getTeamCity() {
        return teamCity;
    }

    public void setTeamCity(String teamCity) {
        this.teamCity = teamCity;
    }

    public String getTeamStateProv() {
        return teamStateProv;
    }

    public void setTeamStateProv(String teamStateProv) {
        this.teamStateProv = teamStateProv;
    }

    public String getTeamZipCode() {
        return teamZipCode;
    }

    public void setTeamZipCode(String teamZipCode) {
        this.teamZipCode = teamZipCode;
    }

    public String getTeamCountry() {
        return teamCountry;
    }

    public void setTeamCountry(String teamCountry) {
        this.teamCountry = teamCountry;
    }

    public int getTeamRookieYear() {
        return teamRookieYear;
    }

    public void setTeamRookieYear(int teamRookieYear) {
        this.teamRookieYear = teamRookieYear;
    }

    public String getTeamWebsite() {
        return teamWebsite;
    }

    public void setTeamWebsite(String teamWebsite) {
        this.teamWebsite = teamWebsite;
    }
}
