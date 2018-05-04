package org.theorangealliance.datasync.models;

public class Alliance {

    private final int division;
    private final int allianceNumber;
    private final int[] allianceNumbers;

    public Alliance(int division, int allianceNumber, int[] allianceNumbers){
        this.division = division;
        this.allianceNumber = allianceNumber;
        this.allianceNumbers = allianceNumbers;
    }

    public int getDivision(){
        return division;
    }

    public int getAllianceNumber(){
        return allianceNumber;
    }

    public int[] getAllianceNumbers(){
        return  allianceNumbers;
    }

}
