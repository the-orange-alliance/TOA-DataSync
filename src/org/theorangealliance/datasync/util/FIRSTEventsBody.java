package org.theorangealliance.datasync.util;

import com.google.gson.annotations.SerializedName;

public class FIRSTEventsBody {

    public FIRSTEventsBody(){
        this.eventName = null;
    }

    @SerializedName("eventCodes")
    private String[] eventName;

    public String[] getEventName() {
        return eventName;
    }

    public void setEventName(String[] eventName) {
        this.eventName = eventName;
    }
}
