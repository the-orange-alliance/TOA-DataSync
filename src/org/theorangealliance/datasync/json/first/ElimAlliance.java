package org.theorangealliance.datasync.json.first;

import com.google.gson.annotations.SerializedName;

public class ElimAlliance {

    public ElimAlliance() {
        this.allianceSeed = 0;
        this.allianceCaptain = 0;
        this.alliancePick1 = 0;
        this.alliancePick2 = 0;
    }

    @SerializedName("seed")
    public int allianceSeed;

    @SerializedName("captain")
    public int allianceCaptain;

    @SerializedName("pick1")
    public int alliancePick1;

    @SerializedName("pick2")
    public int alliancePick2;

    public int getAllianceSeed() {
        return allianceSeed;
    }

    public void setAllianceSeed(int allianceSeed) {
        this.allianceSeed = allianceSeed;
    }

    public int getAllianceCaptain() {
        return allianceCaptain;
    }

    public void setAllianceCaptain(int allianceCaptain) {
        this.allianceCaptain = allianceCaptain;
    }

    public int getAlliancePick1() {
        return alliancePick1;
    }

    public void setAlliancePick1(int alliancePick1) {
        this.alliancePick1 = alliancePick1;
    }

    public int getAlliancePick2() {
        return alliancePick2;
    }

    public void setAlliancePick2(int alliancePick2) {
        this.alliancePick2 = alliancePick2;
    }
}
