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
 * Simulation task result - state.
 */
public enum DfsSimulatorTaskResultState
{
    /**
     * Success
     */
    SUCCESS("Success"),
    /**
     * No neighbours were available (no connections)
     */
    NO_NEIGHBOURS_AVAILABLE("No neighbours available"),
    /**
     * No path to target 
     */
    NO_PATH_AVAILABLE("No path available"),
    /**
     * Not enough space for upload
     */
    NOT_ENOUGH_SPACE_ON_DEVICE("Not enough space on device"),
    /**
     * Target not found
     */
    OBJECT_NOT_FOUND("Object not found"),
    /**
     * Target object was not mounted
     */
    OBJECT_NOT_MOUNTED("Object is not mounted on any device"),
    /**
     * Couldn't resize replica on all target storages
     */
    NOT_ENOUGH_SPACE_FOR_REPLICA("Not enough space to resize replicas");
    
    /**
     * Textual representation
     */
    private final String name;
    
    /**
     * Simulation task result state.
     * 
     * @param name textual representation
     */
    private DfsSimulatorTaskResultState(String name)
    {
        this.name = name;
    }
    
    /**
     * Textual representation of result state.
     * 
     * @return string representation
     */
    @Override public String toString()
    {
        return this.name;
    }
}
