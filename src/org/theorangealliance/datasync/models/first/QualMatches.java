package org.theorangealliance.datasync.models.first;

import com.google.gson.annotations.SerializedName;

public class QualMatches {

    public QualMatches() {
        this.matchNumber = null;
        this.redAlliance = null;
        this.blueAlliance = null;
        this.isFinished = false;
    }

    @SerializedName("matchNumber")
    public String matchNumber;

    @SerializedName("red")
    public QualAlliance redAlliance;

    @SerializedName("blue")
    public QualAlliance blueAlliance;

    @SerializedName("finished")
    public boolean isFinished;

    public String getMatchNumber() {
        return matchNumber;
    }

    public void setMatchNumber(String matchNumber) {
        this.matchNumber = matchNumber;
    }

    public QualAlliance getRedAlliance() {
        return redAlliance;
    }

    public void setRedAlliance(QualAlliance redAlliance) {
        this.redAlliance = redAlliance;
    }

    public QualAlliance getBlueAlliance() {
        return blueAlliance;
    }

    public void setBlueAlliance(QualAlliance blueAlliance) {
        this.blueAlliance = blueAlliance;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void setFinished(boolean finished) {
        isFinished = finished;
    }
}
