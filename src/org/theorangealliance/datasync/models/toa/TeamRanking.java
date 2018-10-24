package org.theorangealliance.datasync.models.toa;

import javafx.beans.property.SimpleIntegerProperty;

/**
 * Created by Kyle Flynn on 12/1/2017.
 */
public class TeamRanking {

    private SimpleIntegerProperty rank;
    private SimpleIntegerProperty teamKey;
    private SimpleIntegerProperty wins;
    private SimpleIntegerProperty losses;
    private SimpleIntegerProperty ties;
    private SimpleIntegerProperty qualPoints;
    private SimpleIntegerProperty rankPoints;
    private SimpleIntegerProperty highestScore;
    private SimpleIntegerProperty played;

    public TeamRanking(int rank, int teamKey) {
        this.rank = new SimpleIntegerProperty(rank);
        this.teamKey = new SimpleIntegerProperty(teamKey);
        this.wins = new SimpleIntegerProperty(0);
        this.losses = new SimpleIntegerProperty(0);
        this.ties = new SimpleIntegerProperty(0);
        this.qualPoints = new SimpleIntegerProperty(0);
        this.rankPoints = new SimpleIntegerProperty(0);
        this.highestScore = new SimpleIntegerProperty(0);
        this.played = new SimpleIntegerProperty(0);
    }

    public int getRank() {
        return rank.get();
    }

    public void setRank(int rank) {
        this.rank.set(rank);
    }

    public int getTeamKey() {
        return teamKey.get();
    }

    public void setTeamKey(int teamKey) {
        this.teamKey.set(teamKey);
    }

    public int getWins() {
        return wins.get();
    }

    public void setWins(int wins) {
        this.wins.set(wins);
    }

    public int getLosses() {
        return losses.get();
    }

    public void setLosses(int losses) {
        this.losses.set(losses);
    }

    public int getTies() {
        return ties.get();
    }

    public void setTies(int ties) {
        this.ties.set(ties);
    }

    public int getQualPoints() {
        return qualPoints.get();
    }

    public void setQualPoints(int qualPoints) {
        this.qualPoints.set(qualPoints);
    }

    public int getRankPoints() {
        return rankPoints.get();
    }

    public void setRankPoints(int rankPoints) {
        this.rankPoints.set(rankPoints);
    }

    public int getHighestScore() {
        return highestScore.get();
    }

    public void setHighestScore(int highestScore) {
        this.highestScore.set(highestScore);
    }

    public int getPlayed() {
        return played.get();
    }

    public void setPlayed(int played) {
        this.played.set(played);
    }
}
