package org.theorangealliance.datasync.json.first;

import com.google.gson.annotations.SerializedName;

public class AllianceFIRST {

    public AllianceFIRST() {
        allianceNumber = 0;
        allianceCaptain = 0;
        alliancePick1 = 0;
        alliancePick2 = 0;
    }

    @SerializedName("seed")
    public int allianceNumber;

    @SerializedName("captain")
    public int allianceCaptain;

    @SerializedName("pick1")
    public int alliancePick1;

    @SerializedName("pick2")
    public int alliancePick2;

    public int getAllianceNumber() {
        return allianceNumber;
    }

    public void setAllianceNumber(int allianceNumber) {
        this.allianceNumber = allianceNumber;
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
