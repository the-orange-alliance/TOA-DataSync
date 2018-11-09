package org.theorangealliance.datasync.models;

public class MatchGeneralAndMatchParticipant {

    public MatchGeneralAndMatchParticipant () {
        this.MatchGeneral = null;
        this.MatchParticipants = null;
    }

    private MatchGeneral MatchGeneral;

    private MatchParticipant[] MatchParticipants;

    public org.theorangealliance.datasync.models.MatchGeneral getMatchGeneral() {
        return MatchGeneral;
    }

    public void setMatchGeneral(org.theorangealliance.datasync.models.MatchGeneral matchGeneral) {
        MatchGeneral = matchGeneral;
    }

    public MatchParticipant[] getMatchParticipants() {
        return MatchParticipants;
    }

    public void setMatchParticipants(MatchParticipant[] matchParticipants) {
        MatchParticipants = matchParticipants;
    }
}
