package org.theorangealliance.datasync.json.toa;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Kyle Flynn on 11/29/2017.
 */
public class EventParticipantJSON {

    @SerializedName("participant_id")
    private String participantKey;

    @SerializedName("event_key")
    private String eventKey;

    @SerializedName("team_key")
    private String teamKey;

    @SerializedName("is_active")
    private int isActive;

    @SerializedName("added_from_ui")
    private int addedFromUI;

    @SerializedName("card_carry_qualification")
    private String cardCarryQual;

    @SerializedName("created_by")
    private String createdBy;

    @SerializedName("created_on")
    private String createdOn;

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

    public int getIsActive() {
        return isActive;
    }

    public void setIsActive(int isActive) {
        this.isActive = isActive;
    }

    public int getAddedFromUI() {
        return addedFromUI;
    }

    public void setAddedFromUI(int addedFromUI) {
        this.addedFromUI = addedFromUI;
    }

    public String getCardCarryQual() {
        return cardCarryQual;
    }

    public void setCardCarryQual(String cardCarryQual) {
        this.cardCarryQual = cardCarryQual;
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
