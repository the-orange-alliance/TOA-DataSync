package org.theorangealliance.datasync.json.first;

import com.google.gson.annotations.SerializedName;

public class Event {

    public Event() {
        this.eventCode = null;
        this.eventName = null;
        this.eventType = null;
        this.eventStatus = null;
        this.isFinals = false;
        this.eventDivisionId = 0;
        this.eventStartTime = 0;
        this.eventEndTime = 0;
    }

    @SerializedName("eventCode")
    private String eventCode;

    @SerializedName("name")
    private String eventName;

    @SerializedName("type")
    private String eventType;

    @SerializedName("status")
    private String eventStatus;

    @SerializedName("finals")
    private boolean isFinals;

    @SerializedName("division")
    private int eventDivisionId;

    @SerializedName("start")
    private long eventStartTime;

    @SerializedName("end")
    private long eventEndTime;

    public String getEventCode() {
        return eventCode;
    }

    public void setEventCode(String eventCode) {
        this.eventCode = eventCode;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getEventStatus() {
        return eventStatus;
    }

    public void setEventStatus(String eventStatus) {
        this.eventStatus = eventStatus;
    }

    public boolean isFinals() {
        return isFinals;
    }

    public void setFinals(boolean finals) {
        isFinals = finals;
    }

    public int getEventDivisionId() {
        return eventDivisionId;
    }

    public void setEventDivisionId(int eventDivisionId) {
        this.eventDivisionId = eventDivisionId;
    }

    public long getEventStartTime() {
        return eventStartTime;
    }

    public void setEventStartTime(long eventStartTime) {
        this.eventStartTime = eventStartTime;
    }

    public long getEventEndTime() {
        return eventEndTime;
    }

    public void setEventEndTime(long eventEndTime) {
        this.eventEndTime = eventEndTime;
    }
}
