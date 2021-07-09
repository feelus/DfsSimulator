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

package cz.zcu.kiv.dfs_simulator.simulation;

/**
 * Simulation task type.
 */
public enum SimulationTaskType
{
    /**
     * Put (upload) task.
     */
    PUT("Upload"),
    /**
     * Get (download) task.
     */
    GET("Download");
    
    /**
     * Textual representation
     */
    protected String name;
    
    /**
     * Simulation task type.
     * 
     * @param name textual representation of type
     */
    private SimulationTaskType(String name)
    {
        this.name = name;
    }
    
    /**
     * Returns textual representation of type.
     * 
     * @return string representation
     */
    @Override public String toString()
    {
        return this.name;
    }
}
