package org.theorangealliance.datasync.json.toa;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Kyle Flynn on 12/1/2017.
 * Modded By Soren Zaiser for 2018 Scoring System 11/11/18
 */
public class TeamRankingJSON {

    @SerializedName("rank_key")
    private String teamRankKey;

    @SerializedName("event_key")
    private String eventKey;

    @SerializedName("team_key")
    private int teamKey;

    private int rank;

    @SerializedName("rank_change")
    private int rankChange;

    private int wins;
    private int losses;
    private int ties;

    @SerializedName("highest_qual_score")
    private int highestScore;

    @SerializedName("ranking_points")
    private int rankPoints;

    @SerializedName("qualifying_points")
    private int qualPoints;

    @SerializedName("tie_breaker_points")
    private int tieBreakerPoints;

    private int played;

    private int disqualified;

    public String getTeamRankKey() {
        return teamRankKey;
    }

    public void setTeamRankKey(String teamRankKey) {
        this.teamRankKey = teamRankKey;
    }

    public String getEventKey() {
        return eventKey;
    }

    public void setEventKey(String eventKey) {
        this.eventKey = eventKey;
    }

    public int getTeamKey() {
        return teamKey;
    }

    public void setTeamKey(int teamKey) {
        this.teamKey = teamKey;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getRankChange() {
        return rankChange;
    }

    public void setRankChange(int rankChange) {
        this.rankChange = rankChange;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public int getLosses() {
        return losses;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    public int getTies() {
        return ties;
    }

    public void setTies(int ties) {
        this.ties = ties;
    }

    public int getHighestScore() {
        return highestScore;
    }

    public void setHighestScore(int highestScore) {
        this.highestScore = highestScore;
    }

    public int getRankPoints() {
        return rankPoints;
    }

    public void setRankPoints(int rankPoints) {
        this.rankPoints = rankPoints;
    }

    public int getQualPoints() {
        return qualPoints;
    }

    public void setQualPoints(int qualPoints) {
        this.qualPoints = qualPoints;
    }

    public int getTieBreakerPoints() {
        return tieBreakerPoints;
    }

    public void setTieBreakerPoints(int tieBreakerPoints) {
        this.tieBreakerPoints = tieBreakerPoints;
    }

    public int getPlayed() {
        return played;
    }

    public void setPlayed(int played) {
        this.played = played;
    }

    public int getDisqualified() {
        return disqualified;
    }

    public void setDisqualified(int disqualified) {
        this.disqualified = disqualified;
    }
}