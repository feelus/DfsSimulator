/**
 * This program is part of master's thesis "Distributed file system simulator"
 * at University of West Bohemia
 * ---------------------------------------------------------------------------
 * Discrete simulation of distributed file systems.
 * 
 * Author: Martin Kucera
 * Date: April, 2017
 * Version: 1.0
 */

package cz.zcu.kiv.dfs_simulator;

import cz.zcu.kiv.dfs_simulator.view.SimulatorLayoutPane;

import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * Application entry point.
 */
public class MainApp extends Application
{
    /**
     * Window title
     */
    private static final String WINDOW_TITLE = "ZCU FAV/KIV: Distributed file system simulator";

    /**
     * Main window stage
     */
    private Stage primaryStage;
    
    /**
     * Application layout pane.
     */
    private BorderPane rootLayout;
    
    /**
     * Start application.
     * 
     * @param primaryStage application stage
     */
    @Override public void start(Stage primaryStage)
    {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle(WINDOW_TITLE);

        this.initRootLayout();
    }

    /**
     * Initiate simulator's layout.
     */
    protected void initRootLayout()
    {
        this.rootLayout = new SimulatorLayoutPane();
        
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        
        this.primaryStage.setScene(new Scene(this.rootLayout));
        this.primaryStage.setWidth(primaryScreenBounds.getWidth() * 0.7);
        this.primaryStage.setHeight(primaryScreenBounds.getHeight() * 0.8);
        this.primaryStage.show();
    }
    
    /**
     * Entry method.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args)
    {
        launch(args);
    }
}
