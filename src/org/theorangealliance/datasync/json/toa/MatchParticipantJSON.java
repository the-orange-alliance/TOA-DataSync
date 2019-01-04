package org.theorangealliance.datasync.json.toa;

import com.google.gson.annotations.SerializedName;

public class MatchParticipantJSON {

    public MatchParticipantJSON() {
        this.matchParticipantKey = "";
        this.matchKey = "";
        this.teamKey = 0;
        this.station = 0;
        this.stationStatus = 0;
        this.refStatus = 0;
    }

    public MatchParticipantJSON(String matchKey, int station, int teamKey) {
        this.matchKey = matchKey;
        this.station = station;
        this.teamKey = teamKey;
        this.matchParticipantKey = this.matchKey + "-" + getStationSuffix();
        this.stationStatus = 1;
    }

    private String getStationSuffix() {
        String suffix = "";
        switch (station) {
            case 11:
                suffix = "R1";
                break;
            case 12:
                suffix = "R2";
                break;
            case 13:
                suffix = "R3";
                break;
            case 21:
                suffix = "B1";
                break;
            case 22:
                suffix = "B2";
                break;
            case 23:
                suffix = "B3";
                break;
        }
        return suffix;
    }

    @SerializedName("match_participant_key")
    private String matchParticipantKey;

    @SerializedName("match_key")
    private String matchKey;

    @SerializedName("team_key")
    private int teamKey;

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

    public int getTeamKey() {
        return teamKey;
    }

    public void setTeamKey(int teamKey) {
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
