package org.theorangealliance.datasync.models.first;

import com.google.gson.annotations.SerializedName;

public class MatchScore {

    public MatchScore(){
        this.autoPoints = 0;
        this.teleopPoints = 0;
        this.endGamePoints = 0;
        this.penaltyPoints = 0;
    }

    @SerializedName("auto")
    public int autoPoints;

    @SerializedName("teleop")
    public int teleopPoints;

    @SerializedName("end")
    public int endGamePoints;

    @SerializedName("penalty")
    public int penaltyPoints;

    public int getAutoPoints() {
        return autoPoints;
    }

    public void setAutoPoints(int autoPoints) {
        this.autoPoints = autoPoints;
    }

    public int getTeleopPoints() {
        return teleopPoints;
    }

    public void setTeleopPoints(int teleopPoints) {
        this.teleopPoints = teleopPoints;
    }

    public int getEndGamePoints() {
        return endGamePoints;
    }

    public void setEndGamePoints(int endGamePoints) {
        this.endGamePoints = endGamePoints;
    }

    public int getPenaltyPoints() {
        return penaltyPoints;
    }

    public void setPenaltyPoints(int penaltyPoints) {
        this.penaltyPoints = penaltyPoints;
    }
}
