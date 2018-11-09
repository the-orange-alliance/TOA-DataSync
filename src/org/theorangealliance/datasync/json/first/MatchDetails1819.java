package org.theorangealliance.datasync.json.first;

import com.google.gson.annotations.SerializedName;

public class MatchDetails1819 {

    @SerializedName("finished")
    private String isFinished;

    @SerializedName("field")
    private int field;

    @SerializedName("time")
    private String startTime;

    @SerializedName("redScore")
    private AllianceMatchDetails1819 redScore;

    @SerializedName("blueScore")
    private AllianceMatchDetails1819 blueScore;

    public String getIsFinished() {
        return isFinished;
    }

    public void setIsFinished(String isFinished) {
        this.isFinished = isFinished;
    }

    public int getField() {
        return field;
    }

    public void setField(int field) {
        this.field = field;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public AllianceMatchDetails1819 getRedScore() {
        return redScore;
    }

    public void setRedScore(AllianceMatchDetails1819 redScore) {
        this.redScore = redScore;
    }

    public AllianceMatchDetails1819 getBlueScore() {
        return blueScore;
    }

    public void setBlueScore(AllianceMatchDetails1819 blueScore) {
        this.blueScore = blueScore;
    }
}
