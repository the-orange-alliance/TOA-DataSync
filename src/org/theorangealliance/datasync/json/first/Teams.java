package org.theorangealliance.datasync.json.first;

import com.google.gson.annotations.SerializedName;

public class Teams {

    public Teams(){
        this.teamNumber = null;
    }

    @SerializedName("teamNumbers")
    private int[] teamNumber;

    public int[] getTeamNumber() {
        return teamNumber;
    }

    public void setTeamNumber(int[] eventName) {
        this.teamNumber = eventName;
    }
}
