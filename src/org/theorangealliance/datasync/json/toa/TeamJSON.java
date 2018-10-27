package org.theorangealliance.datasync.json.toa;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Kyle Flynn on 11/29/2017.
 */
public class TeamJSON {

    @SerializedName("event_participant_key")
    private String participantKey;

    @SerializedName("event_key")
    private String eventKey;

    @SerializedName("team_key")
    private String teamKey;

    @SerializedName("region_key")
    private String regionKey;

    @SerializedName("league_key")
    private String leagueKey;

    @SerializedName("has_card")
    private String hasCard;

    @SerializedName("city")
    private String city;

    @SerializedName("state_prov")
    private String stateProv;

    @SerializedName("country")
    private String country;

    @SerializedName("team_name_short")
    private String teamNameShort;

    @SerializedName("team_name_long")
    private String teamNameLong;

    public String getParticipantKey() {
        return participantKey;
    }

    public String getEventKey() {
        return eventKey;
    }

    public String getTeamKey() {
        return teamKey;
    }

    public String getRegionKey() {
        return regionKey;
    }

    public String getLeagueKey() {
        return leagueKey;
    }

    public int hasCard() {
        return Integer.parseInt(hasCard);
    }

    public String getLocation() {
        return city + ", " + stateProv + ", " + country;
    }

    public String getTeamNameShort() {
        return teamNameShort;
    }

    public String getTeamNameLong() {
        return teamNameLong;
    }

}
