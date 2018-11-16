package org.theorangealliance.datasync.json.toa;

import com.google.gson.annotations.SerializedName;

public class AwardTOA {

    @SerializedName("awards_key")
    private String awardKey;

    @SerializedName("event_key")
    private String eventKey;

    @SerializedName("award_key")
    private String awardID;

    @SerializedName("team_key")
    private String teamKey;

    @SerializedName("receiver_name")
    private String recieverName;

    @SerializedName("award_name")
    private String awardName;

    public AwardTOA(String awardKey, String eventKey, String awardID, String teamKey, String recieverName, String awardName) {
        this.awardKey = awardKey;
        this.eventKey = eventKey;
        this.awardID = awardID;
        this.teamKey = teamKey;
        this.recieverName = recieverName;
        this.awardName = awardName;
    }

    public String getAwardKey() {
        return awardKey;
    }

    public void setAwardKey(String awardKey) {
        this.awardKey = awardKey;
    }

    public String getEventKey() {
        return eventKey;
    }

    public void setEventKey(String eventKey) {
        this.eventKey = eventKey;
    }

    public String getAwardID() {
        return awardID;
    }

    public void setAwardID(String awardID) {
        this.awardID = awardID;
    }

    public String getTeamKey() {
        return teamKey;
    }

    public void setTeamKey(String teamKey) {
        this.teamKey = teamKey;
    }

    public String getRecieverName() {
        return recieverName;
    }

    public void setRecieverName(String recieverName) {
        this.recieverName = recieverName;
    }

    public String getAwardName() {
        return awardName;
    }

    public void setAwardName(String awardName) {
        this.awardName = awardName;
    }
}
