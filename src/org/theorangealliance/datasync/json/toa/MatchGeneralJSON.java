package org.theorangealliance.datasync.json.toa;

import com.google.gson.annotations.SerializedName;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.Date;

/**
 * Created by Kyle Flynn on 11/30/2017.
 */
public class MatchGeneralJSON {

    @SerializedName("match_key")
    private String matchKey;

    @SerializedName("event_key")
    private String eventKey;

    @SerializedName("match_name")
    private String matchName;

    @SerializedName("tournament_level")
    private int tournamentLevel;

    @SerializedName("scheduled_time")
    private Date scheduledTime;

    @SerializedName("play_number")
    private int playNumber;

    @SerializedName("field_number")
    private int fieldNumber;

    @SerializedName("red_score")
    private int redScore;

    @SerializedName("blue_score")
    private int blueScore;

    @SerializedName("red_penalty")
    private int redPenalty;

    @SerializedName("blue_penalty")
    private int bluePenalty;

    @SerializedName("red_auto_score")
    private int redAutoScore;

    @SerializedName("blue_auto_score")
    private int blueAutoScore;

    @SerializedName("red_tele_score")
    private int redTeleScore;

    @SerializedName("blue_tele_score")
    private int blueTeleScore;

    @SerializedName("red_end_score")
    private int redEndScore;

    @SerializedName("blue_end_score")
    private int blueEndScore;

    @SerializedName("created_by")
    private String createdBy;

    @SerializedName("created_on")
    private String createdOn;

    public String getMatchKey() {
        return matchKey;
    }

    public void setMatchKey(String matchKey) {
        this.matchKey = matchKey;
    }

    public String getEventKey() {
        return eventKey;
    }

    public void setEventKey(String eventKey) {
        this.eventKey = eventKey;
    }

    public String getMatchName() {
        return matchName;
    }

    public void setMatchName(String matchName) {
        this.matchName = matchName;
    }

    public int getTournamentLevel() {
        return tournamentLevel;
    }

    public void setTournamentLevel(int tournamentLevel) {
        this.tournamentLevel = tournamentLevel;
    }

    public Date getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(Date scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public int getPlayNumber() {
        return playNumber;
    }

    public void setPlayNumber(int playNumber) {
        this.playNumber = playNumber;
    }

    public int getFieldNumber() {
        return fieldNumber;
    }

    public void setFieldNumber(int fieldNumber) {
        this.fieldNumber = fieldNumber;
    }

    public int getRedScore() {
        return redScore;
    }

    public void setRedScore(int redScore) {
        this.redScore = redScore;
    }

    public int getBlueScore() {
        return blueScore;
    }

    public void setBlueScore(int blueScore) {
        this.blueScore = blueScore;
    }

    public int getRedPenalty() {
        return redPenalty;
    }

    public void setRedPenalty(int redPenalty) {
        this.redPenalty = redPenalty;
    }

    public int getBluePenalty() {
        return bluePenalty;
    }

    public void setBluePenalty(int bluePenalty) {
        this.bluePenalty = bluePenalty;
    }

    public int getRedAutoScore() {
        return redAutoScore;
    }

    public void setRedAutoScore(int redAutoScore) {
        this.redAutoScore = redAutoScore;
    }

    public int getBlueAutoScore() {
        return blueAutoScore;
    }

    public void setBlueAutoScore(int blueAutoScore) {
        this.blueAutoScore = blueAutoScore;
    }

    public int getRedTeleScore() {
        return redTeleScore;
    }

    public void setRedTeleScore(int redTeleScore) {
        this.redTeleScore = redTeleScore;
    }

    public int getBlueTeleScore() {
        return blueTeleScore;
    }

    public void setBlueTeleScore(int blueTeleScore) {
        this.blueTeleScore = blueTeleScore;
    }

    public int getRedEndScore() {
        return redEndScore;
    }

    public void setRedEndScore(int redEndScore) {
        this.redEndScore = redEndScore;
    }

    public int getBlueEndScore() {
        return blueEndScore;
    }

    public void setBlueEndScore(int blueEndScore) {
        this.blueEndScore = blueEndScore;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }
}
