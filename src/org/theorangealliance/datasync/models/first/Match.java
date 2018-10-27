package org.theorangealliance.datasync.models.first;

import com.google.gson.annotations.SerializedName;

public class Match {

    public Match() {
        this.isFinished = false;
        this.redScore = 0;
        this.blueScore = 0;
        this.redSpecifics = null;
        this.blueSpecifics = null;
    }

    @SerializedName("finished")
    public boolean isFinished;

    @SerializedName("redScore")
    public int redScore;

    @SerializedName("blueScore")
    public int blueScore;

    @SerializedName("red")
    public MatchScore redSpecifics;

    @SerializedName("blue")
    public MatchScore blueSpecifics;


    public boolean isFinished() {
        return isFinished;
    }

    public void setFinished(boolean finished) {
        isFinished = finished;
    }

    public int getRedScore() {
        return redScore;
    }

    public void setRedScore(int redScore) {
        this.redScore = redScore;
    }

    public int getBlueScore() {
        return blueScore;
    }

    public void setBlueScore(int blueScore) {
        this.blueScore = blueScore;
    }

    public MatchScore getRedSpecifics() {
        return redSpecifics;
    }

    public void setRedSpecifics(MatchScore redSpecifics) {
        this.redSpecifics = redSpecifics;
    }

    public MatchScore getBlueSpecifics() {
        return blueSpecifics;
    }

    public void setBlueSpecifics(MatchScore blueSpecifics) {
        this.blueSpecifics = blueSpecifics;
    }
}
