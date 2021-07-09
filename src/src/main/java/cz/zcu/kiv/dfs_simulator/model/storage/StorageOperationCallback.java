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

package cz.zcu.kiv.dfs_simulator.model.storage;

/**
 * Storage operation callback.
 */
public abstract class StorageOperationCallback
{
    /**
     * Action performed before switching operation to running state.
     * 
     * @param sTime simulation time
     */
    public abstract void onOperationStarted(long sTime);
    
    /**
     * Action performed after operation has finished.
     * 
     * @param sTime simulation time
     */
    public abstract void onOperationFinished(long sTime);
}
