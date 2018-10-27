package org.theorangealliance.datasync.json.toa;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Kyle Flynn on 11/30/2017.
 */
public class MatchScheduleStationJSON {

    @SerializedName("station_key")
    private String stationKey;

    @SerializedName("match_key")
    private String matchKey;

    @SerializedName("station")
    private int station;

    @SerializedName("station_status")
    private int stationStatus;

    @SerializedName("ref_status")
    private int refStatus;

    @SerializedName("team_key")
    private int teamKey;

    public String getStationKey() {
        return stationKey;
    }

    public String getMatchKey() {
        return matchKey;
    }

    public int getStation() {
        return station;
    }

    public int getStationStatus() {
        return stationStatus;
    }

    public int getRefStatus() {
        return refStatus;
    }

    public int getTeamKey() {
        return teamKey;
    }

    public void setStationKey(String stationKey) {
        this.stationKey = stationKey;
    }

    public void setMatchKey(String matchKey) {
        this.matchKey = matchKey;
    }

    public void setStation(int station) {
        this.station = station;
    }

    public void setStationStatus(int stationStatus) {
        this.stationStatus = stationStatus;
    }

    public void setRefStatus(int refStatus) {
        this.refStatus = refStatus;
    }

    public void setTeamKey(int teamKey) {
        this.teamKey = teamKey;
    }
}
