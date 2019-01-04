package org.theorangealliance.datasync.models;

import org.theorangealliance.datasync.json.toa.MatchParticipantJSON;

public class MatchGeneralAndMatchParticipant {

    public MatchGeneralAndMatchParticipant () {
        this.MatchGeneral = null;
        this.MatchParticipants = null;
    }

    private MatchGeneral MatchGeneral;

    private MatchParticipantJSON[] MatchParticipants;

    public org.theorangealliance.datasync.models.MatchGeneral getMatchGeneral() {
        return MatchGeneral;
    }

    public void setMatchGeneral(org.theorangealliance.datasync.models.MatchGeneral matchGeneral) {
        MatchGeneral = matchGeneral;
    }

    public MatchParticipantJSON[] getMatchParticipants() {
        return MatchParticipants;
    }

    public void setMatchParticipants(MatchParticipantJSON[] matchParticipants) {
        MatchParticipants = matchParticipants;
    }
}
