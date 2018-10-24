package org.theorangealliance.datasync.models.first;

import com.google.gson.annotations.SerializedName;

public class Events {

    public Events(){
        this.eventID = null;
    }

    @SerializedName("eventCodes")
    private String[] eventID;

    public String[] getEventID() {
        return eventID;
    }

    public void setEventID(String[] eventName) {
        this.eventID = eventName;
    }
}
