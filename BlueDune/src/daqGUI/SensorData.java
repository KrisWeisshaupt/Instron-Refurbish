/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package daqGUI;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Kris
 */
public class SensorData {

    /**
     * @author Kris Weisshaupt http://nooleanbot.blogspot.com/
     *
     * If you copy my code please cite me.
     */
    private final StringProperty date, unitF, unitD;
    private final DoubleProperty time, force, displacement;

    public SensorData(String date, double time, double force, double displacement, String unitF, String unitD) {
        this.date = new SimpleStringProperty((date == null ? "null" : date));
        this.time = new SimpleDoubleProperty(time);
        this.force = new SimpleDoubleProperty(force);
        this.displacement = new SimpleDoubleProperty(displacement);
        this.unitF = new SimpleStringProperty((unitF == null ? "null" : unitF));
        this.unitD = new SimpleStringProperty((unitD == null ? "null" : unitD));
    }

    public String getDate() {
        return date.get();
    }

    public String getUnitF() {
        return unitF.get();
    }

    public String getUnitD() {
        return unitD.get();
    }

    public double getTime() {
        return time.get();
    }

    public double getForce() {
        return force.get();
    }

    public double getDisplacement() {
        return displacement.get();
    }
}
