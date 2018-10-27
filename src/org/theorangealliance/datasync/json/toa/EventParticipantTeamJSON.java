package org.theorangealliance.datasync.json.toa;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Kyle Flynn on 11/29/2017.
 */
public class EventParticipantTeamJSON {

    @SerializedName("event_participant_key")
    private String participantKey;

    @SerializedName("event_key")
    private String eventKey;

    @SerializedName("team_key")
    private String teamKey;

    @SerializedName("team_number")
    private int teamNumber;

    @SerializedName("is_active")
    private boolean teamIsActive;

    @SerializedName("card_status")
    private String teamCardStatus;

    @SerializedName("team")
    private TeamJSON teamSpecifics;

    public String getParticipantKey() {
        return participantKey;
    }

    public void setParticipantKey(String participantKey) {
        this.participantKey = participantKey;
    }

    public String getEventKey() {
        return eventKey;
    }

    public void setEventKey(String eventKey) {
        this.eventKey = eventKey;
    }

    public String getTeamKey() {
        return teamKey;
    }

    public void setTeamKey(String teamKey) {
        this.teamKey = teamKey;
    }

    public int getTeamNumber() {
        return teamNumber;
    }

    public void setTeamNumber(int teamNumber) {
        this.teamNumber = teamNumber;
    }

    public boolean isTeamIsActive() {
        return teamIsActive;
    }

    public void setTeamIsActive(boolean teamIsActive) {
        this.teamIsActive = teamIsActive;
    }

    public String getTeamCardStatus() {
        return teamCardStatus;
    }

    public void setTeamCardStatus(String teamCardStatus) {
        this.teamCardStatus = teamCardStatus;
    }

    public TeamJSON getTeamSpecifics() {
        return teamSpecifics;
    }

    public void setTeamSpecifics(TeamJSON teamSpecifics) {
        this.teamSpecifics = teamSpecifics;
    }
}
