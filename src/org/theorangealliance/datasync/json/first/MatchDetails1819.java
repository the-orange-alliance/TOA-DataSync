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
    private int redScore;

    @SerializedName("blueScore")
    private int blueScore;

    @SerializedName("red")
    private AllianceMatchDetails1819 redSpecifics;

    @SerializedName("blue")
    private AllianceMatchDetails1819 blueSpecifics;

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

    public AllianceMatchDetails1819 getRedSpecifics() {
        return redSpecifics;
    }

    public void setRedSpecifics(AllianceMatchDetails1819 redSpecifics) {
        this.redSpecifics = redSpecifics;
    }

    public AllianceMatchDetails1819 getBlueSpecifics() {
        return blueSpecifics;
    }

    public void setBlueSpecifics(AllianceMatchDetails1819 blueSpecifics) {
        this.blueSpecifics = blueSpecifics;
    }
}
