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

package cz.zcu.kiv.dfs_simulator.view.simulation;

import cz.zcu.kiv.dfs_simulator.model.ByteSize;
import cz.zcu.kiv.dfs_simulator.model.ByteSpeed;
import cz.zcu.kiv.dfs_simulator.model.connection.ModelNodeConnection;
import cz.zcu.kiv.dfs_simulator.simulation.SimulationType;
import java.util.Collection;
import javafx.beans.property.BooleanProperty;
import javafx.scene.paint.Color;

/**
 * Object that can play-back simulation events.
 */
public interface SimulationLogDisplayable
{
    /**
     * Begin simulation playback.
     */
    public void beginSimulation();
    /**
     * End simulation playback.
     * 
     * @param success simulation status
     */
    public void endSimulation(boolean success);

    /**
     * Switch simulation playback to another run - another model/method.
     * 
     * @param simulationType new simulation type
     * @param id simulation id
     */
    public void switchSimulationDisplay(SimulationType simulationType, int id);
    /**
     * Property indicating that playback should be visualized rather than
     * fast-forwarded.
     * 
     * @return flag indicating playback delay
     */
    public BooleanProperty requestedDelayedDisplay();
    
    /**
     * Playback simulation event.
     * 
     * @param event event
     */
    public void playSimulationEvent(FxSimulationLogEvent event);
    
    /**
     * Highlight used path.
     * 
     * @param path path
     * @param highlight highlight flag 
     */
    public void highlightPath(Collection<ModelNodeConnection> path, boolean highlight);
    
    /**
     * Update playback event.
     * 
     * @param event event
     */
    public void updateSimulationEvent(FxSimulationLogEvent event);
    
    /**
     * Update playback status.
     * 
     * @param status status text
     * @param c status text color
     */
    public void updateSimulationStatus(String status, Color c);
    /**
     * Update playback elapsed time.
     * 
     * @param elapsedTime elapsed time
     */
    public void updateSimulationElapsedTime(long elapsedTime);
    /**
     * Update playback average speed.
     * 
     * @param averageSpeed average speed
     */
    public void updateSimulationAverageSpeed(ByteSpeed averageSpeed);
    /**
     * Update playback amount of downloaded data.
     * 
     * @param downloaded amount of downloaded data
     */
    public void updateSimulationDownloaded(ByteSize downloaded);
    /**
     * Update playback amount of uploaded data.
     * 
     * @param uploaded amount of uploaded data
     */
    public void updateSimulationUploaded(ByteSize uploaded);
    /**
     * Update task counter status (processed/total).
     * 
     * @param counter task counter status
     */
    public void updateTaskCounter(String counter);
}
