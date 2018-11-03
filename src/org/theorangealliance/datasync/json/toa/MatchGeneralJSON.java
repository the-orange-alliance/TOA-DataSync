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

    @SerializedName("tournament_level")
    private int tournamentLevel;

    @SerializedName("scheduled_time")
    private String scheduledTime;

    @SerializedName("match_name")
    private String matchName;

    @SerializedName("play_number")
    private int playNumber;

    @SerializedName("field_number")
    private int fieldNumber;

    @SerializedName("prestart_time")
    private String prestartTime;

    @SerializedName("prestart_count")
    private String prestartCount;

    @SerializedName("cycle_time")
    private String cycleTime;

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

    /* Dont know if these will ever get added back
    @SerializedName("video_url")
    private String videoURL;

    @SerializedName("participants")
    private MatchParticipantJSON[] participant;*/

    public void setScoreNull() {
        this.setRedScore(-1);
        this.setBlueScore(-1);
        this.setRedAutoScore(-1);
        this.setBlueAutoScore(-1);
        this.setRedTeleScore(-1);
        this.setBlueTeleScore(-1);
        this.setRedEndScore(-1);
        this.setBlueEndScore(-1);
        this.setRedPenalty(-1);
        this.setBluePenalty(-1);
    }

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

    public int getTournamentLevel() {
        return tournamentLevel;
    }

    public void setTournamentLevel(int tournamentLevel) {
        this.tournamentLevel = tournamentLevel;
    }

    public String getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(String scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public String getMatchName() {
        return matchName;
    }

    public void setMatchName(String matchName) {
        this.matchName = matchName;
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

    public String getPrestartTime() {
        return prestartTime;
    }

    public void setPrestartTime(String prestartTime) {
        this.prestartTime = prestartTime;
    }

    public String getPrestartCount() {
        return prestartCount;
    }

    public void setPrestartCount(String prestartCount) {
        this.prestartCount = prestartCount;
    }

    public String getCycleTime() {
        return cycleTime;
    }

    public void setCycleTime(String cycleTime) {
        this.cycleTime = cycleTime;
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

    /* Dont Know if these will ever get added back
    public String getVideoURL() {
        return videoURL;
    }

    public void setVideoURL(String videoURL) {
        this.videoURL = videoURL;
    }

    public MatchParticipantJSON[] getParticipant() {
        return participant;
    }

    public void setParticipant(MatchParticipantJSON[] participant) {
        this.participant = participant;
    }*/

}
