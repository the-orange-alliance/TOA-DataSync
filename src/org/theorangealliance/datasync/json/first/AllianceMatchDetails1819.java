package org.theorangealliance.datasync.json.first;

import com.google.gson.annotations.SerializedName;

public class AllianceMatchDetails1819 {

    public AllianceMatchDetails1819(){
        this.autoLanded = 0;
        this.autoDepot = 0;
        this.autoParking = 0;
        this.autoSample = 0;
        this.teleDepot = 0;
        this.teleGold = 0;
        this.teleSilver = 0;
        this.endLatched = 0;
        this.endParking = 0;
        this.minorPenalties = 0;
        this.majorPenalties = 0;
        this.autoPointTotal = 0;
        this.telePointTotal = 0;
        this.endPointTotal = 0;
        this.penaltyTotal = 0;
    }

    @SerializedName("landed")
    private int autoLanded;

    @SerializedName("claimedDepot")
    private int autoDepot;

    @SerializedName("autoParking")
    private int autoParking;

    @SerializedName("mineralSample")
    private int autoSample;

    @SerializedName("depotMinerals")
    private int teleDepot;

    @SerializedName("landerGold")
    private int teleGold;

    @SerializedName("landerSilver")
    private int teleSilver;

    @SerializedName("latchedLander")
    private int endLatched;

    @SerializedName("endParking")
    private int endParking;

    @SerializedName("minorPenalties")
    private int minorPenalties;

    @SerializedName("majorPenalties")
    private int majorPenalties;

    @SerializedName("auto")
    private int autoPointTotal;

    @SerializedName("teleop")
    private int telePointTotal;

    @SerializedName("end")
    private int endPointTotal;

    @SerializedName("penalty")
    private int penaltyTotal;

    public int getAutoLanded() {
        return autoLanded;
    }

    public void setAutoLanded(int autoLanded) {
        this.autoLanded = autoLanded;
    }

    public int getAutoDepot() {
        return autoDepot;
    }

    public void setAutoDepot(int autoDepot) {
        this.autoDepot = autoDepot;
    }

    public int getAutoParking() {
        return autoParking;
    }

    public void setAutoParking(int autoParking) {
        this.autoParking = autoParking;
    }

    public int getAutoSample() {
        return autoSample;
    }

    public void setAutoSample(int autoSample) {
        this.autoSample = autoSample;
    }

    public int getTeleDepot() {
        return teleDepot;
    }

    public void setTeleDepot(int teleDepot) {
        this.teleDepot = teleDepot;
    }

    public int getTeleGold() {
        return teleGold;
    }

    public void setTeleGold(int teleGold) {
        this.teleGold = teleGold;
    }

    public int getTeleSilver() {
        return teleSilver;
    }

    public void setTeleSilver(int teleSilver) {
        this.teleSilver = teleSilver;
    }

    public int getEndLatched() {
        return endLatched;
    }

    public void setEndLatched(int endLatched) {
        this.endLatched = endLatched;
    }

    public int getEndParking() {
        return endParking;
    }

    public void setEndParking(int endParking) {
        this.endParking = endParking;
    }

    public int getMinorPenalties() {
        return minorPenalties;
    }

    public void setMinorPenalties(int minorPenalties) {
        this.minorPenalties = minorPenalties;
    }

    public int getMajorPenalties() {
        return majorPenalties;
    }

    public void setMajorPenalties(int majorPenalties) {
        this.majorPenalties = majorPenalties;
    }

    public int getAutoPointTotal() {
        return autoPointTotal;
    }

    public void setAutoPointTotal(int autoPointTotal) {
        this.autoPointTotal = autoPointTotal;
    }

    public int getTelePointTotal() {
        return telePointTotal;
    }

    public void setTelePointTotal(int telePointTotal) {
        this.telePointTotal = telePointTotal;
    }

    public int getEndPointTotal() {
        return endPointTotal;
    }

    public void setEndPointTotal(int endPointTotal) {
        this.endPointTotal = endPointTotal;
    }

    public int getPenaltyTotal() {
        return penaltyTotal;
    }

    public void setPenaltyTotal(int penaltyTotal) {
        this.penaltyTotal = penaltyTotal;
    }
}
