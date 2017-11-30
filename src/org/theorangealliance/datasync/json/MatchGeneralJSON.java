package org.theorangealliance.datasync.json;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Kyle Flynn on 11/29/2017.
 */
public class MatchGeneralJSON {

    @SerializedName("match_key")
    private String matchKey;

    @SerializedName("event_key")
    private String eventKey;

    @SerializedName("tournament_level")
    private int tournamentLevel;

    @SerializedName("schedule_time")
    private String scheduledTime;

    @SerializedName("match_name")
    private String matchName;

    @SerializedName("play_number")
    private int playNumber;

    @SerializedName("field_number")
    private int fieldNumber;

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
