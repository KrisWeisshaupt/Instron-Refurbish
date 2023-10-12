package daqGUI;

import com.phidget22.*;
import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import blueduneDAQ.BlueDuneDAQ;
import javafx.scene.control.RadioButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
//import org.opencv.core.Mat;
//import org.opencv.imgproc.Imgproc;
//import org.opencv.videoio.VideoCapture;
import settingsGUI.SettingsGUIController;

public class DaqGuiController implements Initializable {

    @FXML
    private GridPane mainGrid;
    @FXML
    private TableColumn<SensorData, Double> forceCol;
    @FXML
    private TableColumn<SensorData, String> unitsForceCol;
    @FXML
    private TableColumn<SensorData, String> dateCol;
    @FXML
    private TableColumn<SensorData, Double> displacementCol;
    @FXML
    private TableColumn<SensorData, Double> unitsDisplacementCol;
    @FXML
    private TableColumn<SensorData, Double> timeCol;
    @FXML
    private TableView<SensorData> table;
    @FXML
    private Button startButton;
    @FXML
    private Button stopButton;
    @FXML
    private Button graphClearButton;
    @FXML
    private Button tableClearButton;
    @FXML
    private MenuItem menuClose;
    @FXML
    private MenuItem menuCalibrate;
    @FXML
    private MenuItem menuExport;
    @FXML
    private TextField forceField;
    @FXML
    private TextField displacementField;
    @FXML
    private RadioButton limitStatus;
    @FXML
    private RadioButton emergencyStatus;
    @FXML
    private RadioButton overloadStatus;
    @FXML
    private RadioButton downStatus;
    @FXML
    private RadioButton upStatus;
    @FXML
    private Button jogUpButton;
    @FXML
    private Button jogDownButton;
    @FXML
    private Button fineUpButton;
    @FXML
    private Button fineDownButton;
    @FXML
    private Button returnButton;
    @FXML
    private Button zeroForceButton;
    @FXML
    private Button zeroDisplacementButton;
    @FXML
    private ImageView cameraView;

    private DigitalInput limitCheck;
    private DigitalInput upCheck;
    private DigitalInput downCheck;
    private DigitalInput emergencyCheck;

    SimpleBooleanProperty sampling = new SimpleBooleanProperty(false);

    private VoltageRatioInput loadCell;
    private Encoder encoder;
    private MotorPositionController motorController;

    private Stage stage;
    private XYChart.Series<Number, Number> series1;
    private PollingService polling;
    private Stage settingsStage;
    private double lastSave = 0;
    //private VideoCapture capture = new VideoCapture();
    private boolean cameraActive = false;
    private static int cameraId = 0;

    private static double MAXFORCE = 50;

    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/settingsGUI/settingsGUI.fxml"));
            Scene scene = new Scene((Parent) loader.load());
            settingsStage = new Stage();
            //     settingsStage.getIcons().add(new Image(getClass().getResourceAsStream("exportIcon.png")));
            SettingsGUIController controller = loader.<SettingsGUIController>getController();
            controller.initData(settingsStage, this);
            settingsStage.setScene(scene);
            settingsStage.setTitle("Settings/Calibration");
            settingsStage.setMinWidth(692);
            settingsStage.setMinHeight(524);
            settingsStage.setResizable(false);
        } catch (IOException ex) {
            Logger.getLogger(BlueDuneDAQ.class.getName()).log(Level.SEVERE, null, ex);
        }

        forceCol.setCellValueFactory(new PropertyValueFactory<>("force"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        displacementCol.setCellValueFactory(new PropertyValueFactory<>("displacement"));
        timeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        //unitsForceCol.setCellValueFactory(new PropertyValueFactory<>("unitF"));
        //unitsDisplacementCol.setCellValueFactory(new PropertyValueFactory<>("unitD"));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        series1 = new XYChart.Series<>();
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Time (s)");
        yAxis.setLabel("Force (N)");

        final ScatterChart<Number, Number> scatterChart = new ScatterChart<>(xAxis, yAxis);

        scatterChart.getData().addAll(series1);
        scatterChart.setLegendVisible(false);
        scatterChart.getStyleClass().add("custom-chart");
        scatterChart.setAnimated(false);
        mainGrid.add(scatterChart, 0, 0);

        try {
            System.out.println("Creating Phidget...");
            loadCell = new VoltageRatioInput();
            loadCell.setHubPort(1);
            loadCell.addAttachListener((AttachEvent ae) -> {
                System.out.println(ae.getSource());
            });
            loadCell.open(5000);
            loadCell.setChannel(1);
            loadCell.setBridgeEnabled(false);
            loadCell.setChannel(0);
            loadCell.setBridgeEnabled(true);
            loadCell.setBridgeGain(com.phidget22.BridgeGain.GAIN_128X);
            loadCell.setDataInterval(150);

            limitCheck = new DigitalInput();
            upCheck = new DigitalInput();
            downCheck = new DigitalInput();
            emergencyCheck = new DigitalInput();

            limitCheck.setHubPort(2);
            limitCheck.setChannel(0);
            upCheck.setHubPort(2);
            upCheck.setChannel(1);
            downCheck.setHubPort(2);
            downCheck.setChannel(2);
            emergencyCheck.setHubPort(2);
            emergencyCheck.setChannel(3);

            limitCheck.addStateChangeListener(limitCheck_StateChange);
            upCheck.addStateChangeListener(upCheck_StateChange);
            downCheck.addStateChangeListener(downCheck_StateChange);
            emergencyCheck.addStateChangeListener(emergencyCheck_StateChange);
            loadCell.addVoltageRatioChangeListener(loadCell_Change);

            limitCheck.open(5000);
            upCheck.open(5000);
            downCheck.open(5000);
            emergencyCheck.open(5000);

            encoder = new Encoder();
            motorController = new MotorPositionController();
            encoder.setHubPort(0);
            motorController.setHubPort(0);

            encoder.open(5000);
            encoder.setPosition(0);

            motorController.open(5000);
            motorController.addPositionOffset(-1*motorController.getPosition());
            motorController.setTargetPosition(0);
            motorController.setRescaleFactor(0.0001562776);
            motorController.setAcceleration(100);
            motorController.setVelocityLimit(15);
            motorController.setKp(-10000);
            motorController.setKi(-300);
            motorController.setKd(-15000);
            motorController.setDeadBand(0.005);
            motorController.setCurrentLimit(3.5);
            
            motorController.setEngaged(true);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(DaqGuiController.class.getName()).log(Level.SEVERE, null, ex);
            }

            polling = new PollingService(loadCell, motorController);
            polling.setPeriod(Duration.millis(150));
            //capture.open(1);
           

//            polling.setOnSucceeded((final WorkerStateEvent workerStateEvent) -> {
//                SensorData bridgeData = (SensorData) workerStateEvent.getSource().getValue();
//                forceField.setText(String.format("%f", bridgeData.getForce()));
//                displacementField.setText(String.format("%f", bridgeData.getDisplacement()));
//                if (sampling.get()) {
//                    series1.getData().add(new XYChart.Data<>(bridgeData.getTime()/3600.00, bridgeData.getForce()));
//                    table.getItems().add(bridgeData);
//                    table.scrollTo(table.getItems().size());
//                }
//            });
//            polling.start();
//            capture = new VideoCapture();
            polling.setOnSucceeded((final WorkerStateEvent workerStateEvent) -> {
                SensorData bridgeData = (SensorData) workerStateEvent.getSource().getValue();
                forceField.setText(String.format("%f", bridgeData.getForce()));
                displacementField.setText(String.format("%f", bridgeData.getDisplacement()));
                if (sampling.get()) {
                    series1.getData().add(new XYChart.Data<>(bridgeData.getTime() / 3600.00, bridgeData.getForce()));
                    table.getItems().add(bridgeData);
                    table.scrollTo(table.getItems().size());
                    //Mat frame = grabFrame();
                    // convert and show the frame
                    //Image imageToShow = Utils.mat2Image(frame);
                    //updateImageView(cameraView, imageToShow);
                    if (bridgeData.getTime() - lastSave > 1800 || lastSave == 0) {
                        lastSave = bridgeData.getTime();
                        File file = new File("autosave.csv");
                        if (!file.getName().endsWith(".csv")) {
                            file = new File(file.getAbsolutePath() + ".csv");
                        }
                        try {
                            if (!file.exists()) {
                                file.createNewFile();
                            }
                        } catch (IOException ex) {
                            return;
                        }

                        try (PrintWriter pw = new PrintWriter(file)) {
                            pw.printf("Date, deltaTime(s), Force, Units, Displacement, Units");
                            table.getItems().stream().forEach((meter) -> {
                                pw.printf("%n%s,%s,%s,%s", escapeCSV(meter.getDate()), escapeCSV(meter.getTime() + ""),
                                        escapeCSV(meter.getDisplacement() + ""), escapeCSV(meter.getForce() + ""));
                            });
                            pw.flush();
                            pw.close();
                        } catch (FileNotFoundException ex) {
                            Logger.getLogger(DaqGuiController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            });
            polling.start();

//                        valueField.textProperty().bind(polling.valueProperty());
//                        valueField.textProperty().bind(polling.valueProperty());
        } catch (PhidgetException ex) {
            System.out.println(ex);

            Logger.getLogger(DaqGuiController.class.getName()).log(Level.SEVERE, null, ex);

        }

    }

    private void updateImageView(ImageView view, Image image) {
        //Utils.onFXThread(view.imageProperty(), image);
    }

//    private Mat grabFrame() {
//        // init everything
//        Mat frame = new Mat();
//
//        // check if the capture is open
//        if (this.capture.isOpened()) {
//            try {
//                // read the current frame
//                this.capture.read(frame);
//
//                // if the frame is not empty, process it
//                if (!frame.empty()) {
//                    Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY);
//                }
//
//            } catch (Exception e) {
//                // log the error
//                System.err.println("Exception during the image elaboration: " + e);
//            }
//        }
//
//        return frame;
//    }

    public void close() {
        try {
            loadCell.close();
            upCheck.close();
            downCheck.close();
            emergencyCheck.close();
            limitCheck.close();
            motorController.close();
            loadCell = null;
        } catch (PhidgetException ex) {
            Logger.getLogger(DaqGuiController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    void menuCalibrateFired(ActionEvent event) {
        settingsStage.show();
    }

    @FXML
    void menuCloseFired(ActionEvent event) {
        stage.hide();
        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    @FXML
    void menuExportFired(ActionEvent event) {
        Platform.runLater(() -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Comma Delimited (*.csv)", "*.csv"));
            fileChooser.setTitle("Table Export");
            File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                if (!file.getName().endsWith(".csv")) {
                    file = new File(file.getAbsolutePath() + ".csv");
                }
                try {
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                } catch (IOException ex) {
                    return;
                }

                try (PrintWriter pw = new PrintWriter(file)) {
                    pw.printf("Date, deltaTime(s), Force, Units, Displacement, Units");
                    table.getItems().stream().forEach((meter) -> {
                        pw.printf("%n%s,%s,%s,%s,%s, %s", escapeCSV(meter.getDate()), escapeCSV(meter.getTime() + ""),
                                escapeCSV(meter.getForce() + ""), escapeCSV(meter.getUnitF()),
                                escapeCSV(meter.getDisplacement() + ""), escapeCSV(meter.getUnitD()));
                    });
                    pw.flush();
                    pw.close();
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().open(file);
                    }
                    pw.flush();
                    pw.close();
                } catch (IOException ex) {
                }
            }
        });
    }

    @FXML
    void startPressed(ActionEvent event) {
        
        sampling.setValue(true);
        polling.cancel();
        polling.setZero();
        // polling.setPeriod(Duration.millis(100));
        polling.restart();
        //System.out.println(limitStatus.isSelected());

    }

    @FXML
    void stopPressed(ActionEvent event) {
        sampling.setValue(false);

    }

    public void initData(Stage primaryStage) {
        stage = primaryStage;
    }

    @FXML
    void clearGraphPressed(ActionEvent event) {
        series1.getData().clear();
    }

    @FXML
    void clearTablePressed(ActionEvent event) {
        table.getItems().clear();
    }

    private final String QUOTE = "\"";
    private final String ESCAPED_QUOTE = "\"\"";
    private static final char[] CHARACTERS_THAT_MUST_BE_QUOTED = {',', '"', '\n'};

    private String escapeCSV(String s) {
        if (s.contains(QUOTE)) {
            s = s.replace(QUOTE, ESCAPED_QUOTE);
        }

        if ((s.indexOf(CHARACTERS_THAT_MUST_BE_QUOTED[2]) != -1) || (s.indexOf(CHARACTERS_THAT_MUST_BE_QUOTED[1]) != -1) || (s.indexOf(CHARACTERS_THAT_MUST_BE_QUOTED[0]) != -1)) {
            s = QUOTE + s + QUOTE;
        }
        return s;
    }

    public void settings(String pollingRate, String forceSlope, String forceOffset, String dispSlope, String dispOffset) {
        polling.setParameters((Integer.parseInt(pollingRate)), Double.parseDouble(forceSlope), Double.parseDouble(forceOffset));
    }

    @FXML
    void jogUpPressed(ActionEvent event) {
        try {
            motorController.setEngaged(true);
            motorController.setVelocityLimit(15);
            motorController.setTargetPosition(motorController.getPosition() + 5);
        } catch (PhidgetException ex) {
            System.out.println(ex);
            Logger.getLogger(DaqGuiController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    void fineUpPressed(ActionEvent event) {
        try {
            motorController.setVelocityLimit(0.1);
            motorController.setEngaged(true);
            motorController.setTargetPosition(motorController.getPosition() + 0.1);
        } catch (PhidgetException ex) {
            System.out.println(ex);
            Logger.getLogger(DaqGuiController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    void fineDownPressed(ActionEvent event) {
        try {
            motorController.setEngaged(true);
            motorController.setVelocityLimit(.1);
            motorController.setTargetPosition(motorController.getPosition() - 0.1);
        } catch (PhidgetException ex) {
            System.out.println(ex);
            Logger.getLogger(DaqGuiController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    void jogDownPressed(ActionEvent event) {
        try {
            motorController.setEngaged(true);
            motorController.setVelocityLimit(15);
            motorController.setTargetPosition(motorController.getPosition() - 5);
        } catch (PhidgetException ex) {
            System.out.println(ex);
            Logger.getLogger(DaqGuiController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    void returnPressed(ActionEvent event) {

        try {
            motorController.setEngaged(true);
            motorController.setTargetPosition(0);
        } catch (PhidgetException ex) {
            System.out.println(ex);
            Logger.getLogger(DaqGuiController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @FXML
    void zeroForcePressed(ActionEvent event) {
        try {
            polling.setForceOffset(-1 * loadCell.getVoltageRatio() * polling.getForceSlope());
        } catch (PhidgetException ex) {
            Logger.getLogger(DaqGuiController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    void zeroDisplacementPressed(ActionEvent event) {
        try {
            motorController.addPositionOffset(-1*motorController.getPosition());

        } catch (PhidgetException ex) {
            Logger.getLogger(DaqGuiController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public DigitalInputStateChangeListener limitCheck_StateChange
            = (DigitalInputStateChangeEvent e) -> {
                limitStatus.setSelected(e.getState());
                if (!e.getState()) {
                    try {
                        motorController.setEngaged(false);
                    } catch (PhidgetException ex) {
                        System.out.println(ex);
                        Logger.getLogger(DaqGuiController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };

    public DigitalInputStateChangeListener upCheck_StateChange
            = (DigitalInputStateChangeEvent e) -> {
                upStatus.setSelected(e.getState());
                if (e.getState()) {
                    try {
                        motorController.setEngaged(false);
                        sampling.setValue(false);
                    } catch (PhidgetException ex) {
                        System.out.println(ex);
                        Logger.getLogger(DaqGuiController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };

    public DigitalInputStateChangeListener downCheck_StateChange
            = (DigitalInputStateChangeEvent e) -> {
                downStatus.setSelected(e.getState());
                if (e.getState()) {
                    try {
                        motorController.setEngaged(false);
                    } catch (PhidgetException ex) {
                        System.out.println(ex);
                        Logger.getLogger(DaqGuiController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };

    public DigitalInputStateChangeListener emergencyCheck_StateChange
            = (DigitalInputStateChangeEvent e) -> {
                emergencyStatus.setSelected(!e.getState());
                if (!e.getState()) {
                    try {
                        motorController.setEngaged(false);
                    } catch (PhidgetException ex) {
                        System.out.println(ex);
                        Logger.getLogger(DaqGuiController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };

    public VoltageRatioInputVoltageRatioChangeListener loadCell_Change
            = (VoltageRatioInputVoltageRatioChangeEvent e) -> {
                if (e.getVoltageRatio() * polling.getForceSlope() + polling.getForceOffset() > MAXFORCE) {
                    overloadStatus.setSelected(true);
                    sampling.setValue(false);
                    try {
                        motorController.setEngaged(false);
                    } catch (PhidgetException ex) {
                        System.out.println(ex);
                        Logger.getLogger(DaqGuiController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    overloadStatus.setSelected(false);
                }
            };
}
