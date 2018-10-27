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
    private int redStatus;


}
