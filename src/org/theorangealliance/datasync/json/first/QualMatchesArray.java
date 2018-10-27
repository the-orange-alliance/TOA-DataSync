package org.theorangealliance.datasync.json.first;

import com.google.gson.annotations.SerializedName;

public class QualMatchesArray {

    public QualMatchesArray() {
        this.qualMatches = null;
    }

    @SerializedName("matches")
    public QualMatches[] qualMatches;

    public QualMatches[] getQualMatches() {
        return qualMatches;
    }

    public void setQualMatches(QualMatches[] qualMatches) {
        this.qualMatches = qualMatches;
    }
}
