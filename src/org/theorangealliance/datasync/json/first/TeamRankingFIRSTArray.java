package org.theorangealliance.datasync.json.first;

import com.google.gson.annotations.SerializedName;

public class TeamRankingFIRSTArray {

    public TeamRankingFIRSTArray() {
        this.teamRankings = null;
    }

    @SerializedName("rankingList")
    public TeamRankingFIRST[] teamRankings;

    public TeamRankingFIRST[] getTeamRankings() {
        return teamRankings;
    }

    public void setTeamRankings(TeamRankingFIRST[] teamRanking) {
        this.teamRankings = teamRanking;
    }
}
