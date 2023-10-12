/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package blueduneDAQ;

import daqGUI.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
//import org.opencv.core.Core;

/**
 *
 * @author Kris
 */
public class BlueDuneDAQ extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/daqGUI/daqGui.fxml"));
            Scene scene = new Scene((Parent) loader.load());
            primaryStage.setMinWidth(525);
            primaryStage.setMinHeight(550);
            primaryStage.setTitle("BlueDune");
                 primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("MAP_Square120.png")));
            DaqGuiController controller = loader.<DaqGuiController>getController();
            controller.initData(primaryStage);
            primaryStage.setScene(scene);
            primaryStage.show();
            primaryStage.setOnCloseRequest((WindowEvent event) -> {
                event.consume();
                controller.close();
                try {
                    Thread.sleep(200); //allow device time to set enabled state to false
                } catch (InterruptedException ex) {
                }
                System.exit(0);
            });
        } catch (IOException ex) {
            Logger.getLogger(BlueDuneDAQ.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
 
     //   System.loadLibrary(Core.NATIVE_LIBRARY_NAME);      
        launch(args);
    }

}
