package org.theorangealliance.datasync.json.toa;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Kyle Flynn on 12/5/2017.
 */
public class ErrorJSON {

    @SerializedName("_status")
    private int status;
    @SerializedName("_message")
    private String code;
    private String url;

    public int getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getUrl() {
        return url;
    }
}
