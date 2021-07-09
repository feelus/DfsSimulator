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

import cz.zcu.kiv.dfs_simulator.simulation.SimulationType;
import javafx.scene.control.TreeItem;

/**
 * Simulation bar state - context.
 */
class FxSimulationBarState
{
    /**
     * Simulation type
     */
    protected final SimulationType type;
    /**
     * Simulation (context) id
     */
    protected final int id;
    
    /**
     * Task tree root
     */
    protected TreeItem<FxSimulationLogEvent> root = new TreeItem<>();
    
    /* Simulation progress */
    protected String simulationInfoStatus;
    protected String simulationInfoAverageSpeed;
    protected String simulationInfoDownloaded;
    protected String simulationInfoElapsedTime;
    protected String simulationInfoUploaded;
    
    /**
     * Simulation bar state - context.
     * 
     * @param type simulation type
     * @param id simulation (context) id
     */
    public FxSimulationBarState(SimulationType type, int id)
    {
        this.type = type;
        this.id = id;
    }

    /**
     * Get task tree root.
     * 
     * @return root
     */
    public TreeItem<FxSimulationLogEvent> getRoot()
    {
        return root;
    }

    /**
     * Set task tree root.
     * 
     * @param root root
     */
    public void setRoot(TreeItem<FxSimulationLogEvent> root)
    {
        this.root = root;
    }

    /**
     * Get simulation status.
     * 
     * @return status
     */
    public String getSimulationInfoStatus()
    {
        return simulationInfoStatus;
    }
    
    /**
     * Set simulation status.
     * 
     * @param simulationInfoStatus status
     */
    public void setSimulationInfoStatus(String simulationInfoStatus)
    {
        this.simulationInfoStatus = simulationInfoStatus;
    }

    /**
     * Get simulation average speed.
     * 
     * @return simulation avg. speed
     */
    public String getSimulationInfoAverageSpeed()
    {
        return simulationInfoAverageSpeed;
    }

    /**
     * Set simulation average speed.
     * 
     * @param simulationInfoAverageSpeed simulation avg. speed
     */
    public void setSimulationInfoAverageSpeed(String simulationInfoAverageSpeed)
    {
        this.simulationInfoAverageSpeed = simulationInfoAverageSpeed;
    }

    /**
     * Get simulation amount of downloaded data.
     * 
     * @return downloaded
     */
    public String getSimulationInfoDownloaded()
    {
        return simulationInfoDownloaded;
    }

    /**
     * Set simulation amount of downloaded data.
     * 
     * @param simulationInfoDownloaded downloaded
     */
    public void setSimulationInfoDownloaded(String simulationInfoDownloaded)
    {
        this.simulationInfoDownloaded = simulationInfoDownloaded;
    }

    /**
     * Get simulation total time.
     * 
     * @return total time
     */
    public String getSimulationInfoElapsedTime()
    {
        return simulationInfoElapsedTime;
    }

    /**
     * Set simulation total time.
     * 
     * @param simulationInfoElapsedTime total time
     */
    public void setSimulationInfoElapsedTime(String simulationInfoElapsedTime)
    {
        this.simulationInfoElapsedTime = simulationInfoElapsedTime;
    }

    /**
     * Get simulation amount of uploaded data.
     * 
     * @return uploaded
     */
    public String getSimulationInfoUploaded()
    {
        return simulationInfoUploaded;
    }

    /**
     * Set simulation info of uploaded data.
     * 
     * @param simulationInfoUploaded uploaded
     */
    public void setSimulationInfoUploaded(String simulationInfoUploaded)
    {
        this.simulationInfoUploaded = simulationInfoUploaded;
    }
    
    /**
     * Textual representation of context (id. type).
     * 
     * @return string representation
     */
    @Override public String toString()
    {
        return this.id + ". " + this.type.toString();
    }
}
