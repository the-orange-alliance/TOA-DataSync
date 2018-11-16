package org.theorangealliance.datasync.json.first;

public class AwardFIRST {


    private boolean officialAward;
    private String awardName;
    private String firstPlace;
    private String secondPlace;
    private String thirdPlace;

    public AwardFIRST(boolean officialAward, String awardName, String firstPlace, String secondPlace, String thirdPlace) {
        this.officialAward = officialAward;
        this.awardName = awardName;
        this.firstPlace = firstPlace;
        this.secondPlace = secondPlace;
        this.thirdPlace = thirdPlace;
    }

    public boolean isOfficialAward() {
        return officialAward;
    }

    public void setOfficialAward(boolean officialAward) {
        this.officialAward = officialAward;
    }

    public String getAwardName() {
        return awardName;
    }

    public void setAwardName(String awardName) {
        this.awardName = awardName;
    }

    public String getFirstPlace() {
        return firstPlace;
    }

    public void setFirstPlace(String firstPlace) {
        this.firstPlace = firstPlace;
    }

    public String getSecondPlace() {
        return secondPlace;
    }

    public void setSecondPlace(String secondPlace) {
        this.secondPlace = secondPlace;
    }

    public String getThirdPlace() {
        return thirdPlace;
    }

    public void setThirdPlace(String thirdPlace) {
        this.thirdPlace = thirdPlace;
    }
}
