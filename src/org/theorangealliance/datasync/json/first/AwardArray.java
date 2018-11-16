package org.theorangealliance.datasync.json.first;

public class AwardArray {

    private AwardFIRST[] awards;

    public AwardArray(AwardFIRST[] awards) {
        this.awards = awards;
    }

    public AwardFIRST[] getAwards() {
        return awards;
    }

    public void setAwards(AwardFIRST[] awards) {
        this.awards = awards;
    }
}
