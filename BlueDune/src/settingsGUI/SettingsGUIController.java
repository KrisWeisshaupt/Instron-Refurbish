/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package settingsGUI;

import daqGUI.DaqGuiController;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class SettingsGUIController implements Initializable {

    @FXML
    private TextField displacementCal1;

    @FXML
    private TextField displacementOffsetField;

    @FXML
    private TextField displacementCal2;

    @FXML
    private TextField forceCal1;

    @FXML
    private TextField forceCal2;

    @FXML
    private TextField displacementSlopeField;

    @FXML
    private TextField forceOffsetField;

    @FXML
    private TextField displacementReading2;

    @FXML
    private TextField displacementRead1;

    @FXML
    private Button applyButton;

    @FXML
    private Button displacementFitButton;

    @FXML
    private TextField forceSlopeField;

    @FXML
    private Button forceFitButton;

    @FXML
    private TextField displacementUnit;

    @FXML
    private TextField pollingRateField;

    @FXML
    private TextField forceRead1;

    @FXML
    private TextField forceRead2;

    @FXML
    private TextField forceUnit;

    @FXML
    private Slider forceGainSlider;

    @FXML
    private TextField deviceRateField;

    @FXML
    private Slider displacementGainsSlider;
    private DaqGuiController daqGUI;
    private Stage stage;

    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {

    }

    @FXML
    void applySettingsPressed(ActionEvent event) {
        daqGUI.settings(pollingRateField.getText(), forceSlopeField.getText(), forceOffsetField.getText(), displacementSlopeField.getText(), displacementOffsetField.getText());
    }

    @FXML
    void displacementFitPressed(ActionEvent event) {

    }

    @FXML
    void forrceFitPressed(ActionEvent event) {

    }

    public void initData(Stage settingsStage, DaqGuiController daqGUI) {
        this.daqGUI = daqGUI;
        this.stage = settingsStage;
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                stage.hide();
            }
        });
    }
}
