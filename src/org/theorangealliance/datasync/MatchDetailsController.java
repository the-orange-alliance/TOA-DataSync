package org.theorangealliance.datasync;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class MatchDetailsController {

    @FXML
    public Label matchKey = new Label();

    @FXML
    public Label redLanded = new Label();

    @FXML
    public Label redSample = new Label();

    @FXML
    public Label redClaimed = new Label();

    @FXML
    public Label redParked = new Label();

    @FXML
    public Label redGold = new Label();

    @FXML
    public Label redSilver = new Label();

    @FXML
    public Label redDepot = new Label();

    @FXML
    public Label redLatched = new Label();

    @FXML
    public Label redPart = new Label();

    @FXML
    public Label redFull = new Label();

    @FXML
    public Label blueLanded = new Label();

    @FXML
    public Label blueSample = new Label();

    @FXML
    public Label blueClaimed = new Label();

    @FXML
    public Label blueParked = new Label();

    @FXML
    public Label blueGold = new Label();

    @FXML
    public Label blueSilver = new Label();

    @FXML
    public Label blueDepot = new Label();

    @FXML
    public Label blueLatched = new Label();

    @FXML
    public Label bluePart = new Label();

    @FXML
    public Label blueFull = new Label();

    @FXML
    public void closeWindow(){
        Stage stage = (Stage) blueFull.getScene().getWindow();
        stage.close();
    }

}
