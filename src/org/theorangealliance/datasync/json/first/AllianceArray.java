package org.theorangealliance.datasync.json.first;

import com.google.gson.annotations.SerializedName;

public class AllianceArray {

    public AllianceArray() {
        alliances = null;
    }

    @SerializedName("alliances")
    public AllianceFIRST[] alliances;

    public AllianceFIRST[] getAlliances() {
        return alliances;
    }

    public void setAlliances(AllianceFIRST[] alliances) {
        this.alliances = alliances;
    }
}
