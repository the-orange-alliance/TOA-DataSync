package org.theorangealliance.datasync.models.first;

import com.google.gson.annotations.SerializedName;

public class QualAlliance {

    public QualAlliance(){
        team1 = 0;
        team2 = 0;
        isTeam1Surrogate = false;
        isTeam2Surrogate = false;
    }

    @SerializedName("team1")
    public int team1;

    @SerializedName("team2")
    public int team2;

    @SerializedName("isTeam1Surrogate")
    public boolean isTeam1Surrogate;

    @SerializedName("isTeam2Surrogate")
    public boolean isTeam2Surrogate;

    public int getTeam1() {
        return team1;
    }

    public void setTeam1(int team1) {
        this.team1 = team1;
    }

    public int getTeam2() {
        return team2;
    }

    public void setTeam2(int team2) {
        this.team2 = team2;
    }

    public boolean getIsTeam1Surrogate() {
        return isTeam1Surrogate;
    }

    public void setTeam1Surrogate(boolean team1Surrogate) {
        isTeam1Surrogate = team1Surrogate;
    }

    public boolean getIsTeam2Surrogate() {
        return isTeam2Surrogate;
    }

    public void setTeam2Surrogate(boolean team2Surrogate) {
        isTeam2Surrogate = team2Surrogate;
    }
}
