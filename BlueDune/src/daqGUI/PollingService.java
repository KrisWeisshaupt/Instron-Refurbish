/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package daqGUI;

import com.phidget22.MotorPositionController;
import com.phidget22.PhidgetException;
import com.phidget22.VoltageRatioInput;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.util.Duration;

/**
 *
 * @author Kris
 */
public class PollingService extends ScheduledService<SensorData> {

    private VoltageRatioInput loadCell;
    private MotorPositionController motorController;
    private SensorData data;
    private Calendar cal;
    private Date zeroTime;
    private final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd  HH:mm:ss.SSSS");
    private double forceO = 0;
    private double forceS = 1;

    public PollingService(VoltageRatioInput loadCell, MotorPositionController motorController) {
        this.loadCell = loadCell;
        this.motorController = motorController;
        cal = Calendar.getInstance();
        zeroTime = cal.getTime();
    }

    public void setZero() {
        cal = Calendar.getInstance();
        zeroTime = cal.getTime();
        try {
            motorController.addPositionOffset(motorController.getPosition());
        } catch (PhidgetException ex) {
            System.out.println(ex);
            Logger.getLogger(DaqGuiController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
   public double getForceOffset() {
        return forceO;
    }
   
    public double getForceSlope() {
        return forceS;
    }
    
   public void setForceOffset(double forceO) {
        this.forceO=forceO;
    }
   
    public void setForceSlope(double forceS) {
        this.forceS=forceS;
    }

    //Computer Driven DAQ.  Will request phidget bridge data at set intervals
    @Override
    protected Task<SensorData> createTask() {
        final Task<SensorData> voidTask = new Task() {
            @Override
            protected SensorData call() throws Exception {
                cal = Calendar.getInstance();
                Date time = cal.getTime();
                double deltaTime = (time.getTime() - zeroTime.getTime()) / 1000.0;
                data = new SensorData(sdf.format(time), deltaTime, loadCell.getVoltageRatio() * forceS + forceO, motorController.getPosition(), "?", "?");
                return data;
            }
        };
        return voidTask;
    }

    void setParameters(int i, double forceS, double forceO) {
        this.setPeriod(Duration.millis(i));
        this.forceS = forceS;
        this.forceO = forceO;
    }
// Event Driven Data Acquisition
// Phidget bridge will alert when a new data point is available
//
//            loadCell.addBridgeDataListener((BridgeDataEvent bde) -> {
//                Platform.runLater(() -> {
//                    Double value = 0.0;
//                    try {
//                        value = loadCell.getBridgeValue(0);
//                    } catch (Exception ex) {
//                    }
//                    valueField.setText(value.toString());
//                    if (sampling.get()) {
//                        cal = Calendar.getInstance();
//                        Date time = cal.getTime();
//                        Double deltaTime = (time.getTime() - zeroTime.getTime()) / 1000.0;
//                        series1.getData().add(new XYChart.Data<>(deltaTime, value));
//                        table.getItems().add(new SensorData(sdf.format(time), deltaTime.toString(), value.toString(), "", ""));
//                        table.scrollTo(table.getItems().size());
//                    }
//                });
//            });

}
