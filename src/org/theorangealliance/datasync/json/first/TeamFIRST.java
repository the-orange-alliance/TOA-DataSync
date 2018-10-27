package org.theorangealliance.datasync.json.first;

import com.google.gson.annotations.SerializedName;

public class TeamFIRST {

    public TeamFIRST(){
        this.teamNumber = 0;
        this.teamNameShort = null;
        this.teamNameLong = null;
        this.teamCity = null;
        this.teamStateProv = null;
        this.teamCountry = null;
        this.teamRookieYear = 0;
    }

    @SerializedName("number")
    private int teamNumber;

    @SerializedName("name")
    private String teamNameShort;

    @SerializedName("school")
    private String teamNameLong;

    @SerializedName("city")
    private String teamCity;

    @SerializedName("state")
    private String teamStateProv;

    @SerializedName("country")
    private String teamCountry;

    @SerializedName("rookie")
    private int teamRookieYear;

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
}
