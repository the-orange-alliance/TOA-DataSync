package org.theorangealliance.datasync.json.first;

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
    private boolean isFinished;

    @SerializedName("field")
    private int fieldNumber;

    @SerializedName("time")
    private Long lastCommitTime;

    @SerializedName("redScore")
    private int redScore;

    @SerializedName("blueScore")
    private int blueScore;

    @SerializedName("red")
    private MatchScore redSpecifics;

    @SerializedName("blue")
    private MatchScore blueSpecifics;


    public boolean isFinished() {
        return isFinished;
    }

    public void setFinished(boolean finished) {
        isFinished = finished;
    }

    public int getFieldNumber() {
        return fieldNumber;
    }

    public void setFieldNumber(int fieldNumber) {
        this.fieldNumber = fieldNumber;
    }

    public Long getLastCommitTime() {
        return lastCommitTime;
    }

    public void setLastCommitTime(Long lastCommitTime) {
        this.lastCommitTime = lastCommitTime;
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
