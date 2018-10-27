package org.theorangealliance.datasync.tabs;

import org.theorangealliance.datasync.DataSyncController;
import org.theorangealliance.datasync.json.MatchDetail1718JSON;
import org.theorangealliance.datasync.logging.TOALogger;
import org.theorangealliance.datasync.models.first.AllianceArray;
import org.theorangealliance.datasync.models.first.AllianceFIRST;
import org.theorangealliance.datasync.models.first.TeamFIRST;
import org.theorangealliance.datasync.models.first.Teams;
import org.theorangealliance.datasync.models.toa.Alliance;
import org.theorangealliance.datasync.models.toa.MatchGeneral;
import org.theorangealliance.datasync.models.toa.Team;
import org.theorangealliance.datasync.util.Config;
import org.theorangealliance.datasync.util.FIRSTEndpoint;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.logging.Level;

public class AlliancesController {

    private DataSyncController controller;
    private Alliance[] alliances;

    public AlliancesController(DataSyncController instance){

        this.controller = instance;

    }

    public void importAlliancesScoring(HashMap<MatchGeneral, MatchDetail1718JSON> scores){
        File allianceFile = new File(Config.SCORING_DIR + File.separator + "alliances.txt");
        if (allianceFile.exists()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(allianceFile));
                String line;
                alliances = new Alliance[4];
                while ((line = reader.readLine()) != null) {
                    /** Alliance info */
                    String[] allianceInfo = line.split("\\|");
                    int division = Integer.parseInt(allianceInfo[0]);
                    int allianceNumber = Integer.parseInt(allianceInfo[1]);
                    int[] allianceNumbers = {Integer.parseInt(allianceInfo[3]), Integer.parseInt(allianceInfo[4]), Integer.parseInt(allianceInfo[5])};
                    alliances[allianceNumber-1] = new Alliance(division, allianceNumber, allianceNumbers);
                }
                reader.close();
                /* TODO - Make Upload Alliances so we can uncomment this
                controller.btnUploadAlliances.setDisable(false);*/
                updateAllianceLabels(scores);
                TOALogger.log(Level.INFO, "Alliance import successful.");
            } catch (Exception e) {
                e.printStackTrace();
                controller.sendError("Could not open file. " + e.getLocalizedMessage());
            }
        } else {
            controller.sendError("Could not locate alliances.txt from the Scoring System. Did you generate an elimination bracket?");
        }
    }

    public void importAlliancesFIRSTApi(HashMap<MatchGeneral, MatchDetail1718JSON> scores){
        FIRSTEndpoint firstAlliances = new FIRSTEndpoint("events/" + Config.EVENT_API_KEY + "/elim/alliances/");
        firstAlliances.execute(((response, success) -> {
            if (success && !response.contains("NOT_READY")) {

                alliances = new Alliance[4];

                AllianceArray alls = firstAlliances.getGson().fromJson(response, AllianceArray.class);

                for(AllianceFIRST a : alls.getAlliances()) {
                    int allianceNumber = a.getAllianceNumber();
                    int[] allianceNumbers = {a.getAllianceCaptain(), a.getAlliancePick1(), (a.getAlliancePick2() == -1) ? a.getAlliancePick2() : 0};
                    //TODO: Fix Division info
                    alliances[allianceNumber-1] = new Alliance(0, allianceNumber, allianceNumbers);
                }

                /* TODO - Make Upload Alliances so we can uncomment this
                controller.btnUploadAlliances.setDisable(false);*/
                updateAllianceLabels(scores);
                TOALogger.log(Level.INFO, "Alliance import successful.");
            } else {
                controller.sendError("Connection to FIRST Scoring system unsuccessful, or Alliances were not ready.  " + response);
            }
        }));
    }

    public void importAlliancesTOA(HashMap<MatchGeneral, MatchDetail1718JSON> scores){

        //TODO - This (May need the API updates from below to test)
        updateAllianceLabels(scores);

    }

    public void uploadAlliances(){
        // TODO - Waiting on API updates
    }

    public void purgeAlliances(){
        // TODO - Waiting on API updates
    }

    private void updateAllianceLabels(HashMap<MatchGeneral, MatchDetail1718JSON> scores){

        int redS1 = 0, blueS1 = 0, redS2 = 0, blueS2 = 0;

        for(MatchGeneral match : scores.keySet()){
            String[] slots;
            if((slots = match.getMatchName().split(" ")).length == 4) {
                if (slots[1].equals("1") && match.getRedScore() != match.getBlueScore()) {
                    if (match.getRedScore() > match.getBlueScore()) {
                        redS1++;
                    } else {
                        blueS1++;
                    }
                } else if (slots[1].equals("2") && match.getRedScore() != match.getBlueScore()) {
                    if (match.getRedScore() > match.getBlueScore()) {
                        redS2++;
                    } else {
                        blueS2++;
                    }
                }
            }

        }

        String[] allianceStrings = new String[4];
        for(int i = 0; i < 4; i++){
            allianceStrings[i] = ("" + (i + 1)) + ": " + alliances[i].getAllianceNumbers()[0] + ", "  + alliances[i].getAllianceNumbers()[1] + ((alliances[i].getAllianceNumbers()[2] > 0) ? ", "  + alliances[i].getAllianceNumbers()[2]:"");
        }

        controller.labelRedFirstSemis.setText(allianceStrings[0]);
        controller.labelBlueFirstSemis.setText(allianceStrings[3]);
        controller.labelRedSecondSemis.setText(allianceStrings[1]);
        controller.labelBlueSecondSemis.setText(allianceStrings[2]);

        controller.labelRedFinals.setText(redS1 == 2 ? allianceStrings[0]: blueS1 == 2 ? allianceStrings[3] : "");
        controller.labelBlueFinals.setText(redS2 == 2 ? allianceStrings[1]: blueS2 == 2 ? allianceStrings[2] : "");

    }

}
