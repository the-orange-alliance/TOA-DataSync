package org.theorangealliance.datasync.models;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

public class Award {

    private SimpleStringProperty awardKey;
    private SimpleStringProperty awardName;
    private SimpleStringProperty teamKey;
    private SimpleBooleanProperty isUploaded;
    private String awardID;
    private String reciepientId;

    public Award(SimpleStringProperty awardKey, SimpleStringProperty awardName, SimpleStringProperty teamKey, SimpleBooleanProperty isUploaded) {
        this.awardKey = awardKey;
        this.awardName = awardName;
        this.teamKey = teamKey;
        this.isUploaded = isUploaded;
        this.reciepientId = null;
    }

    public Award() {
        this.awardKey = new SimpleStringProperty();
        this.awardName = new SimpleStringProperty();
        this.teamKey = new SimpleStringProperty();
        this.isUploaded = new SimpleBooleanProperty();
        this.reciepientId = null;
    }

    public String getAwardKey() {
        return awardKey.get();
    }

    public SimpleStringProperty awardKeyProperty() {
        return awardKey;
    }

    public void setAwardKey(String awardKey) {
        this.awardKey.set(awardKey);
    }

    public String getAwardName() {
        return awardName.get();
    }

    public SimpleStringProperty awardNameProperty() {
        return awardName;
    }

    public void setAwardName(String awardName) {
        this.awardName.set(awardName);
    }

    public String getTeamKey() {
        return teamKey.get();
    }

    public SimpleStringProperty teamKeyProperty() {
        return teamKey;
    }

    public void setTeamKey(String teamKey) {
        this.teamKey.set(teamKey);
    }

    public boolean isUploaded() {
        return isUploaded.get();
    }

    public SimpleBooleanProperty isUploadedProperty() {
        return isUploaded;
    }

    public void setIsUploaded(boolean isUploaded) {
        this.isUploaded.set(isUploaded);
    }

    public String getAwardID() {
        return awardID;
    }

    public void setAwardID(String awardID) {
        this.awardID = awardID;
    }

    public boolean isIsUploaded() {
        return isUploaded.get();
    }

    public String getReciepientId() {
        return reciepientId;
    }

    public void setReciepientId(String reciepientId) {
        this.reciepientId = reciepientId;
    }
}

