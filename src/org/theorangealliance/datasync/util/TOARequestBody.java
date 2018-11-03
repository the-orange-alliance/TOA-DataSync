package org.theorangealliance.datasync.util;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kyle Flynn on 11/29/2017.
 */
public class TOARequestBody {

    @SerializedName("")
    private List<Object> values;

    public TOARequestBody() {
        this.values = new ArrayList<>();
    }


    public void addValue(Object value) {
        this.values.add(value);
    }

    public List<Object> getValues() {
        return this.values;
    }

}
