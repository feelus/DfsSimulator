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

/**
 * Simulation log event status.
 */
public enum FxSimulationLogEventStatus
{
    /**
     * Event done
     */
    DONE("Done"),
    /**
     * Event skipped
     */
    SKIPPED("Skipped"),
    /**
     * Event failed
     */
    FAILED("Failed"),
    /**
     * Event running
     */
    RUNNING("Running");
    
    /**
     * Status text
     */
    protected final String status;
    
    /**
     * Simulation log event status.
     * 
     * @param status status text
     */
    private FxSimulationLogEventStatus(String status)
    {
        this.status = status;
    }
    
    /**
     * Convert status to text - as status text.
     * 
     * @return string representation
     */
    @Override public String toString()
    {
        return this.status;
    }
    
}
