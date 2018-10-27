package org.theorangealliance.datasync.json.toa;

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

    @SerializedName("league_key")
    private String leagueKey;

    @SerializedName("event_code")
    private String eventCode;

    @SerializedName("event_type_key")
    private String eventTypeKey;

    @SerializedName("division_key")
    private int divisionKey;

    @SerializedName("division_name")
    private String divisionName;

    @SerializedName("event_name")
    private String eventName;

    @SerializedName("start_date")
    private String startDate;

    @SerializedName("end_date")
    private String endDate;

    @SerializedName("week_key")
    private String weekKey;

    @SerializedName("city")
    private String city;

    @SerializedName("state_prov")
    private String stateProv;

    @SerializedName("country")
    private String country;

    @SerializedName("venue")
    private String venue;

    @SerializedName("website")
    private String website;

    @SerializedName("time_zone")
    private String timeZone;

    @SerializedName("is_active")
    private String isActive;

    @SerializedName("is_public")
    private String isPublic;

    @SerializedName("active_tournament_level")
    private String activeTournamentLevel;

    @SerializedName("alliance_count")
    private String allianceCount;

    @SerializedName("field_count")
    private String fieldCount;

    @SerializedName("advance_spots")
    private String advancementSpots;

    @SerializedName("advance_event")
    private String advasncementEvent;

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
