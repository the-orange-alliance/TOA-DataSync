package org.theorangealliance.datasync.models;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.Date;

/**
 * Created by Kyle Flynn on 11/29/2017.
 */
public class MatchGeneral {

    private SimpleStringProperty matchName;
    private SimpleBooleanProperty isDone;
    private SimpleBooleanProperty isUploaded;
    private String matchKey;
    private int tournamentLevel;
    private Date scheduledTime;
    private int playNumber;
    private int fieldNumber;
    private int redScore;
    private int blueScore;
    private int redPenalty;
    private int bluePenalty;
    private int redAutoScore;
    private int blueAutoScore;
    private int redTeleScore;
    private int blueTeleScore;
    private int redEndScore;
    private int blueEndScore;

    public MatchGeneral(String matchName, int tournamentLevel, Date scheduledTime, int fieldNumber) {
        this.matchName = new SimpleStringProperty(matchName);
        this.isDone = new SimpleBooleanProperty(false);
        this.isUploaded = new SimpleBooleanProperty(false);
        this.tournamentLevel = tournamentLevel;
        this.scheduledTime = scheduledTime;
        this.fieldNumber = fieldNumber;
    }

    public static int buildTOATournamentLevel(int tournamentLevel, int matchNumber) {
        int level = 0;
        switch (tournamentLevel) {
            case 1:
                level = 1;
                break;
            case 2:
                if (matchNumber < 20) {
                    level = 31;
                } else {
                    level = 32;
                }
                break;
            case 3:
                level = 4;
                break;
            case 4:
                if (matchNumber > 40) {
                    level = 24;
                } else if (matchNumber < 40 && matchNumber > 30) {
                    level = 23;
                } else if (matchNumber < 30 && matchNumber > 20) {
                    level = 22;
                } else {
                    level = 21;
                }
                break;
        }
        return level;
    }

    public static String buildMatchName(int tournamentLevel, int matchNumber) {
        String matchName = "";
        int matchNum = matchNumber;
        switch (tournamentLevel) {
            case 1:
                matchName = "Quals";
                break;
            case 2:
                if (matchNumber < 20) {
                    matchName = "Semis 1 Match";
                    matchNum = matchNumber - 10;
                } else {
                    matchName = "Semis 2 Match";
                    matchNum = matchNumber - 20;
                }
                break;
            case 3:
                matchName = "Finals";
                break;
            case 4:
                if (matchNumber > 40) {
                    matchName = "Quarters 4 Match";
                    matchNum = matchNumber - 40;
                } else if (matchNumber < 40 && matchNumber > 30) {
                    matchName = "Quarters 3 Match";
                    matchNum = matchNumber - 30;
                } else if (matchNumber < 30 && matchNumber > 20) {
                    matchName = "Quarters 2 Match";
                    matchNum = matchNumber - 20;
                } else {
                    matchName = "Quarters 1 Match";
                    matchNum = matchNumber - 10;
                }
                break;
        }
        return matchName + " " + matchNum;
    }

    public int getCanonicalMatchNumber() {
        String[] matchArgs = getMatchName().split(" ");
        String matchNumber = matchArgs[matchArgs.length-1];
        return Integer.parseInt(matchNumber);
    }

    public int getPlayNumber() {
        return playNumber;
    }

    public void setPlayNumber(int playNumber) {
        this.playNumber = playNumber;
    }

    public void setMatchKey(String matchKey) {
        this.matchKey = matchKey;
    }

    public void setMatchName(String matchName) {
         this.matchName.set(matchName);
    }

    public void setIsDone(boolean done) {
        this.isDone.set(done);
    }

    public void setIsUploaded(boolean isUploaded) {
        this.isUploaded.set(isUploaded);
    }

    public String getMatchKey() {
        return matchKey;
    }

    public String getMatchName() {
        return this.matchName.get();
    }

    public Boolean isDone() {
        return isDone.get();
    }

    public Boolean isUploaded() {
        return isUploaded.get();
    }

    public int getTournamentLevel() {
        return tournamentLevel;
    }

    public void setTournamentLevel(int tournamentLevel) {
        this.tournamentLevel = tournamentLevel;
    }

    public Date getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(Date scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public int getFieldNumber() {
        return fieldNumber;
    }

    public void setFieldNumber(int fieldNumber) {
        this.fieldNumber = fieldNumber;
    }

    public int getRedScore() {
        return redScore;
    }

    public void setRedScore(int redScore) {
        this.redScore = redScore;
    }

    public int getBlueScore() {
        return blueScore;
    }

    public void setBlueScore(int blueScore) {
        this.blueScore = blueScore;
    }

    public int getRedTeleScore() {
        return redTeleScore;
    }

    public void setRedTeleScore(int redTeleScore) {
        this.redTeleScore = redTeleScore;
    }

    public int getBlueTeleScore() {
        return blueTeleScore;
    }

    public void setBlueTeleScore(int blueTeleScore) {
        this.blueTeleScore = blueTeleScore;
    }

    public int getRedPenalty() {
        return redPenalty;
    }

    public void setRedPenalty(int redPenalty) {
        this.redPenalty = redPenalty;
    }

    public int getBluePenalty() {
        return bluePenalty;
    }

    public void setBluePenalty(int bluePenalty) {
        this.bluePenalty = bluePenalty;
    }

    public int getRedAutoScore() {
        return redAutoScore;
    }

    public void setRedAutoScore(int redAutoScore) {
        this.redAutoScore = redAutoScore;
    }

    public int getBlueAutoScore() {
        return blueAutoScore;
    }

    public void setBlueAutoScore(int blueAutoScore) {
        this.blueAutoScore = blueAutoScore;
    }

    public int getRedEndScore() {
        return redEndScore;
    }

    public void setRedEndScore(int redEndScore) {
        this.redEndScore = redEndScore;
    }

    public int getBlueEndScore() {
        return blueEndScore;
    }

    public void setBlueEndScore(int blueEndScore) {
        this.blueEndScore = blueEndScore;
    }
}
