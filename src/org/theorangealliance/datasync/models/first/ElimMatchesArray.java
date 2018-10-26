package org.theorangealliance.datasync.models.first;

import com.google.gson.annotations.SerializedName;

public class ElimMatchesArray {

    public ElimMatchesArray() {
        this.elimMatches = null;
    }

    @SerializedName("matchList")
    public ElimMatches[] elimMatches;

    public ElimMatches[] getElimMatches() {
        return elimMatches;
    }

    public void setElimMatches(ElimMatches[] elimMatches) {
        this.elimMatches = elimMatches;
    }
}
