package org.theorangealliance.datasync.util;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kyle Flynn on 11/29/2017.
 */
public class TOARequestBody {

    @SerializedName("event_key")
    private String eventKey;

    @SerializedName("match_key")
    private String matchKey;

    @SerializedName("team_rank_key")
    private String rankKey;

    private List<Object> values;

    public TOARequestBody() {
        this.matchKey = null;
        this.eventKey = null;
        this.rankKey = null;
        this.values = new ArrayList<>();
    }

    public void setEventKey(String eventKey) {
        this.eventKey = eventKey;
    }

    public void setMatchKey(String matchKey) {
        this.matchKey = matchKey;
    }

    public void setRankKey(String rankKey) {
        this.rankKey = rankKey;
    }

    public void addValue(Object value) {
        this.values.add(value);
    }

    public String getEventKey() {
        return this.eventKey;
    }

    public String getMatchKey() {
        return this.matchKey;
    }

    public String getRankKey() {
        return this.rankKey;
    }

    public List<Object> getValues() {
        return this.values;
    }

}
