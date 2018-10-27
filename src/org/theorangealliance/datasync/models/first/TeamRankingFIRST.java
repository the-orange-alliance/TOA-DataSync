package org.theorangealliance.datasync.models.first;

import com.google.gson.annotations.SerializedName;

public class TeamRankingFIRST {

    public TeamRankingFIRST() {
        teamNumber = 0;
        teamName = "";
        teamRP = 0;
        teamTieBreakerPoints = 0;
        teamMatchesPlayed = 0;
    }

    @SerializedName("team")
    public int teamNumber;

    @SerializedName("teamName")
    public String teamName;

    @SerializedName("ranking")
    public int teamRanking;

    @SerializedName("rankingPoints")
    public int teamRP;

    @SerializedName("tieBreakerPoints")
    public int teamTieBreakerPoints;

    @SerializedName("matchesPlayed")
    public int teamMatchesPlayed;

    public int getTeamNumber() {
        return teamNumber;
    }

    public void setTeamNumber(int teamNumber) {
        this.teamNumber = teamNumber;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public int getTeamRanking() {
        return teamRanking;
    }

    public void setTeamRanking(int teamRanking) {
        this.teamRanking = teamRanking;
    }

    public int getTeamRP() {
        return teamRP;
    }

    public void setTeamRP(int teamRP) {
        this.teamRP = teamRP;
    }

    public int getTeamTieBreakerPoints() {
        return teamTieBreakerPoints;
    }

    public void setTeamTieBreakerPoints(int teamTieBreakerPoints) {
        this.teamTieBreakerPoints = teamTieBreakerPoints;
    }

    public int getTeamMatchesPlayed() {
        return teamMatchesPlayed;
    }

    public void setTeamMatchesPlayed(int teamMatchesPlayed) {
        this.teamMatchesPlayed = teamMatchesPlayed;
    }


}
