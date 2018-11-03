package org.theorangealliance.datasync.models;

/**
 * Created by Kyle Flynn on 11/29/2017.
 */
public class MatchParticipant {

    private String stationKey;
    private String matchKey;
    private int station;
    //-2:Disqualified -1:No Show 0:Surrogate 1:Nothing 2: Yellow Card
    private int stationStatus;
    private int teamKey;

    public MatchParticipant(String matchKey, int station, int teamKey) {
        this.matchKey = matchKey;
        this.station = station;
        this.teamKey = teamKey;
        this.stationKey = this.matchKey + "-" + getStationSuffix();
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

    public String getStatusString(){

        switch(stationStatus){

            case -2:
                return "(Disqualified)";
            case -1:
                return "(No Show)";
            case 0:
                return "*";
            case 2:
                return "(Yellow Card)";
            default:
                return "";

        }

    }

    public String getStationKey() {
        return stationKey;
    }

    public void setStationKey(String stationKey) {
        this.stationKey = stationKey;
    }

    public String getMatchKey() {
        return matchKey;
    }

    public void setMatchKey(String matchKey) {
        this.matchKey = matchKey;
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

    public int getTeamKey() {
        return teamKey;
    }

    public void setTeamKey(int teamKey) {
        this.teamKey = teamKey;
    }
}
