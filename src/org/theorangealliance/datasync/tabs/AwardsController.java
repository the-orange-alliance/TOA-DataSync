package org.theorangealliance.datasync.tabs;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.theorangealliance.datasync.DataSyncController;
import org.theorangealliance.datasync.json.first.AwardArray;
import org.theorangealliance.datasync.json.first.AwardFIRST;
import org.theorangealliance.datasync.json.toa.AwardTOA;
import org.theorangealliance.datasync.logging.TOALogger;
import org.theorangealliance.datasync.models.Award;
import org.theorangealliance.datasync.util.Config;
import org.theorangealliance.datasync.util.FIRSTEndpointNonLambda;
import org.theorangealliance.datasync.util.TOAEndpoint;
import org.theorangealliance.datasync.util.TOARequestBody;
import java.util.ArrayList;
import java.util.Optional;
import java.util.logging.Level;

public class AwardsController {

    private DataSyncController controller;
    private ObservableList<Award> awardList;
    private ArrayList<AwardTOA> uploadedAwards;

    public AwardsController(DataSyncController instance){

        this.controller = instance;

        //Setup Table
        this.controller.colAward.setCellValueFactory(new PropertyValueFactory<>("awardName"));
        this.controller.colAwardKey.setCellValueFactory(new PropertyValueFactory<>("awardKey"));
        this.controller.colAwardTeamKey.setCellValueFactory(new PropertyValueFactory<>("teamKey"));
        this.controller.colAwardIsUploaded.setCellValueFactory(cellData -> new SimpleBooleanProperty(cellData.getValue().isUploaded()));
        this.controller.colAwardIsUploaded.setCellFactory(col -> new TableCell<Award, Boolean>() {
            @Override
            protected void updateItem(Boolean done, boolean empty) {
                if (!empty && done != null) {
                    if (done) {
                        setTextFill(Color.GREEN);
                        setText("YES");
                    } else {
                        setTextFill(Color.RED);
                        setText("NO");
                    }
                }
            }
        });


        this.awardList = FXCollections.observableArrayList();
        this.controller.tableAwards.setItems(awardList);

        this.uploadedAwards = new ArrayList<>();

        this.controller.btnAwardsPost.setDisable(true);
    }

    private void getAwardsTOA(boolean loadIntoTable){
        uploadedAwards.clear();
        TOAEndpoint matchesEndpoint = new TOAEndpoint("GET", "event/" + Config.EVENT_ID + "/awards");
        matchesEndpoint.setCredentials(Config.TOA_API_KEY, Config.EVENT_ID);
        matchesEndpoint.execute(((response, success) -> {
            if (success) {
                AwardTOA[] awards = matchesEndpoint.getGson().fromJson(response, AwardTOA[].class);
                if(awards.length > 0){
                    for(AwardTOA a : awards){
                        uploadedAwards.add(a);
                    }
                }
                TOALogger.log(Level.INFO, "Grabbed " + uploadedAwards.size() + " awards from TOA.");

                if(loadIntoTable){
                    if(uploadedAwards != null && uploadedAwards.size() > 0){
                        for(AwardTOA a : uploadedAwards) {
                            Award award = new Award();
                            award.setIsUploaded(true);
                            award.setAwardName(a.getAwardName());
                            award.setTeamKey(a.getTeamKey());
                            award.setAwardID(a.getAwardID());
                            award.setAwardKey(a.getAwardKey());
                            awardList.add(award);
                        }
                    }
                    this.controller.tableAwards.refresh();
                }
                this.controller.btnAwardsPost.setDisable(false);
            } else {
                this.controller.sendError("Error: " + response);
                this.controller.btnAwardsPost.setDisable(true);
            }
        }));
    }

    public void uploadAwards() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Are you sure about this?");
        alert.setHeaderText("This operation cannot be undone.");
        String matchesText;

        alert.setContentText("YOU WILL BE PUBLICLY POSTING ALL AWARD RESULTS TO TOA. PLEASE CLICK OK ONLY IF YOU ARE SURE YOU WANT TO CONTINUE?");

        ButtonType okayButton = new ButtonType("Sure?");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        //Set Icon because it shows in the task bar
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/app_ico.png")));

        alert.getButtonTypes().setAll(okayButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == okayButton) {
            Alert a = new Alert(Alert.AlertType.WARNING);
            a.setTitle("Are you sure about this?");
            a.setHeaderText("This operation STILL cannot be undone.");

            a.setContentText("YOU WILL BE PUBLICLY POSTING ALL AWARD RESULTS TO TOA. ARE YOU 100% SURE YOU ARE NOT GOING TO LEAK CONFIDENTIAL DATA?");

            ButtonType o = new ButtonType("Upload");
            ButtonType c = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

            //Set Icon because it shows in the task bar
            Stage s = (Stage) a.getDialogPane().getScene().getWindow();
            s.getIcons().add(new Image(getClass().getResourceAsStream("/app_ico.png")));

            alert.getButtonTypes().setAll(o, c);

            Optional<ButtonType> r = alert.showAndWait();

            if(r.get() == o) {
                uploadAwardsNoWarning();
            }
        }
    }

    private void uploadAwardsNoWarning() {
        for(Award a : awardList){
            String methodType = "POST";
            String putRouteExtra = "";

            if(a.isUploaded()){
                methodType = "PUT";
            } else {
                if(uploadedAwards != null && uploadedAwards.size() > 0){
                    for(AwardTOA aTOA : uploadedAwards) {
                        if(aTOA.getAwardKey().equals(a.getAwardKey())){
                            methodType = "PUT";
                        }
                    }
                }

            }

            TOAEndpoint detailEndpoint = new TOAEndpoint(methodType, "event/" + Config.EVENT_ID + "/awards/" + putRouteExtra);
            detailEndpoint.setCredentials(Config.TOA_API_KEY, Config.EVENT_ID);
            TOARequestBody awardBody = new TOARequestBody();
            AwardTOA award = new AwardTOA(a.getAwardKey(), Config.EVENT_ID, a.getAwardID(), a.getTeamKey(), null, a.getAwardName());
            awardBody.addValue(award);
            detailEndpoint.setBody(awardBody);
            detailEndpoint.execute(((response, success) -> {
                if (success) {
                    int i = 0;
                    for(Award award1 : awardList){
                        if(a.getAwardKey().equals(award1.getAwardKey())){
                            awardList.get(i).setIsUploaded(true);
                            break;
                        }
                        i++;
                    }
                    uploadedAwards.add(award);
                    controller.tableAwards.refresh();
                    TOALogger.log(Level.INFO, "Successfully uploaded Award " + a.getAwardKey() + ". " + response);
                    //Do Dashboard Stuff
                    controller.cb_awards.setSelected(false);
                    controller.cb_awards.setTextFill(Color.RED);
                    controller.btn_cb_awards.setDisable(false);
                }
            }));
        }
    }

    public void getAwardsFIRST() {
        AwardArray awards = null;
        try {
            awards = FIRSTEndpointNonLambda.getGson().fromJson(FIRSTEndpointNonLambda.getResp("events/" + Config.FIRST_API_EVENT_ID  + "/awards"), AwardArray.class);
        } catch (Exception e) {
            this.controller.sendInfo("Unable to get awards " + e);
        }

        if(awards != null) {
            awardList.clear();
            for(AwardFIRST a : awards.getAwards()) {
                int recipNum = 1;
                String recipients[] = {a.getFirstPlace(), a.getSecondPlace(), a.getThirdPlace()};
                //Go Through each recipient and generate an awardID for them
                for(String r : recipients) {
                    if(!r.equals("-1") && !r.equals("") && !r.equals("(none)")) {
                        Award award = new Award();
                        String awardID = getAwardIDFromName(a.getAwardName());
                        if(awardID != null) {
                            award.setAwardID(awardID + recipNum);
                            award.setAwardName(getAwardNameFromID(award.getAwardID()));
                            award.setAwardKey(Config.EVENT_ID + "-" + awardID + recipNum);
                            award.setIsUploaded(false);
                            //Check If Award is uploaded
                            if(uploadedAwards != null && uploadedAwards.size() > 0) {
                                for(AwardTOA toaA : uploadedAwards) {
                                    if(toaA.getAwardKey().equals(award.getAwardKey())){
                                        award.setIsUploaded(true);
                                    }
                                }
                            }

                            award.setTeamKey(r + "");
                            awardList.add(award);
                            recipNum++;
                            this.controller.btnAwardsPost.setDisable(false);
                        } else {
                            this.controller.sendError("Could Not Get Award ID from " + a.getAwardName());
                            TOALogger.log(Level.INFO, "Could Not Get Award ID from " + a.getAwardName());
                            this.controller.btnAwardsPost.setDisable(true);
                        }
                    }
                }

            }
        }
        this.controller.tableAwards.refresh();
    }


    public void loadToaAwards() {
        awardList.clear();
        getAwardsTOA(true);
    }


    public void purgeAwards() {
        //TODO: When route becomes avaliable
    }

    private String getAwardIDFromName(String name) {
        switch (name) {
            case "Winning Alliance Award":
                return "FIN";
            case "Finalist Alliance Award":
                return "WIN";
            case "Inspire Award":
                return "INS";
            case "Think Award":
                return "THK";
            case "Connect Award":
                return "CNT";
            case "Rockwell Collins Innovate Award":
                return "INV";
            case "Design Award":
                return "DSN";
            case "Motivate Award":
                return "MOT";
            case "Control Award":
                return "CTL";
            case "Promote Award":
                return "PRM";
            case "Compass Award":
                return "CMP";
            case "Judges\u0027 Award":
            case "Judge\u0027s Award":
                return "JUD";
            default:
                return null;
        }
    }

    private String getAwardNameFromID(String key){
        if (key.startsWith("INS")) {
            if(key.substring(key.length() - 1).equals("1")){
                return "Inspire Award Winner";
            } else {
                return "Inspire Award Finalist";
            }
        } else if (key.startsWith("THK")) {
            if(key.substring(key.length() - 1).equals("1")){
                return "Think Award Winner";
            } else {
                return "Think Award Finalist";
            }
        } else if (key.startsWith("CNT")) {
            if(key.substring(key.length() - 1).equals("1")){
                return "Connect Award Winner";
            } else {
                return "Connect Award Finalist";
            }
        } else if (key.startsWith("INV")) {
            if(key.substring(key.length() - 1).equals("1")){
                return "Rockwell Collins Innovate Award Winner";
            } else {
                return "Rockwell Collins Innovate Award Finalist";
            }
        } else if (key.startsWith("DSN")) {
            if(key.substring(key.length() - 1).equals("1")){
                return "Design Award Winner";
            } else {
                return "Design Award Finalist";
            }
        } else if (key.startsWith("MOT")) {
            if(key.substring(key.length() - 1).equals("1")){
                return "Motivate Award Winner";
            }else {
                return "Motivate Award Finalist";
            }
        } else if (key.startsWith("CTL")) {
            if(key.substring(key.length() - 1).equals("1")){
                return "Control Award Winner";
            } else {
                return "Control Award Finalist";
            }
        } else if (key.startsWith("PRM")) {
            if(key.substring(key.length() - 1).equals("1")){
                return "Promote Award Winner";
            } else {
                return "Promote Award Finalist";
            }
        } else if (key.startsWith("CMP")) {
            if(key.substring(key.length() - 1).equals("1")){
                return "Compass Award Winner";
            } else {
                return "Compass Award Finalist";
            }
        } else if (key.startsWith("JUD")) {
            if(key.substring(key.length() - 1).equals("1")){
                return "Judges Award Winner";
            } else {
                return "Judges Award Finalist";
            }
        } else if (key.startsWith("WIN")) {
            return "Winning Alliance Award Winners";
        } else if (key.startsWith("FIN")) {
            return "Finalist Alliance Award Winners";
        }
        return null;
    }

}
