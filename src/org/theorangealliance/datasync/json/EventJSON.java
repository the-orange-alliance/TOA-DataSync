package org.theorangealliance.datasync.json;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Kyle Flynn on 12/1/2017.
 */
public class EventJSON {

    @SerializedName("event_key")
    private String eventKey;

    @SerializedName("season_key")
    private String seasonKey;

    @SerializedName("region_key")
    private String regionKey;

    @SerializedName("division_key")
    private int divisionKey;

    @SerializedName("division_name")
    private String divisionName;

    public String getEventKey() {
        return eventKey;
    }

    public void setEventKey(String eventKey) {
        this.eventKey = eventKey;
    }

    public String getSeasonKey() {
        return seasonKey;
    }

    public void setSeasonKey(String seasonKey) {
        this.seasonKey = seasonKey;
    }

    public String getRegionKey() {
        return regionKey;
    }

    public void setRegionKey(String regionKey) {
        this.regionKey = regionKey;
    }

    public int getDivisionKey() {
        return divisionKey;
    }

    public void setDivisionKey(int divisionKey) {
        this.divisionKey = divisionKey;
    }

    public String getDivisionName() {
        return divisionName;
    }

    public void setDivisionName(String divisionName) {
        this.divisionName = divisionName;
    }
}
