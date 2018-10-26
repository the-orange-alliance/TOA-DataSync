package org.theorangealliance.datasync.models.first;

import com.google.gson.annotations.SerializedName;

public class ElimMatches {

    public ElimMatches() {
        matchNumber = "";
        redAlliance = null;
        blueAlliance = null;
    }

    @SerializedName("match")
    public String matchNumber;

    @SerializedName("red")
    public ElimAlliance redAlliance;

    @SerializedName("blue")
    public ElimAlliance blueAlliance;

    public String getMatchNumber() {
        return matchNumber;
    }

    public void setMatchNumber(String matchNumber) {
        this.matchNumber = matchNumber;
    }

    public ElimAlliance getRedAlliance() {
        return redAlliance;
    }

    public void setRedAlliance(ElimAlliance redAlliance) {
        this.redAlliance = redAlliance;
    }

    public ElimAlliance getBlueAlliance() {
        return blueAlliance;
    }

    public void setBlueAlliance(ElimAlliance blueAlliance) {
        this.blueAlliance = blueAlliance;
    }
}
