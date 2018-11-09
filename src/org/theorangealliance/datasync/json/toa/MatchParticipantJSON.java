package org.theorangealliance.datasync.json.toa;

import com.google.gson.annotations.SerializedName;

public class MatchParticipantJSON {

    @SerializedName("match_participant_key")
    private String matchParticipantKey;

    @SerializedName("match_key")
    private String matchKey;

    @SerializedName("team_key")
    private String teamKey;

    @SerializedName("station")
    private int station;

    @SerializedName("station_status")
    private int stationStatus;

    @SerializedName("ref_status")
    private int refStatus;

    public String getMatchParticipantKey() {
        return matchParticipantKey;
    }

    public void setMatchParticipantKey(String matchParticipantKey) {
        this.matchParticipantKey = matchParticipantKey;
    }

    public String getMatchKey() {
        return matchKey;
    }

    public void setMatchKey(String matchKey) {
        this.matchKey = matchKey;
    }

    public String getTeamKey() {
        return teamKey;
    }

    public void setTeamKey(String teamKey) {
        this.teamKey = teamKey;
    }

    public int getStation() {
        return station;
    }

    public void setStation(int station) {
        this.station = station;
    }

    public int getStationStatus() {
        return stationStatus;
    }

    public void setStationStatus(int stationStatus) {
        this.stationStatus = stationStatus;
    }

    public int getRefStatus() {
        return refStatus;
    }

    public void setRefStatus(int refStatus) {
        this.refStatus = refStatus;
    }
}
