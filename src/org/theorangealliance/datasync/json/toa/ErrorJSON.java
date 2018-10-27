package org.theorangealliance.datasync.json.toa;

/**
 * Created by Kyle Flynn on 12/5/2017.
 */
public class ErrorJSON {

    private int status;
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
