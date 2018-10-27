package org.theorangealliance.datasync.json.first;

import com.google.gson.annotations.SerializedName;

public class QualMatches {

    public QualMatches() {
        this.matchNumber = 0;
        this.redAlliance = null;
        this.blueAlliance = null;
        this.isFinished = false;
    }

    @SerializedName("matchNumber")
    public int matchNumber;

    @SerializedName("red")
    public QualAlliance redAlliance;

    @SerializedName("blue")
    public QualAlliance blueAlliance;

    @SerializedName("finished")
    public boolean isFinished;

    public int getMatchNumber() {
        return matchNumber;
    }

    public void setMatchNumber(int matchNumber) {
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
