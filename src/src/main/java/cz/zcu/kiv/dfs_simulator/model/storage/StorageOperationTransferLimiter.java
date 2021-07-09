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

import cz.zcu.kiv.dfs_simulator.model.ByteSpeed;

/**
 * Storage operation transfer limiter.
 */
public abstract class StorageOperationTransferLimiter
{
    /**
     * Returns maximum available transfer speed at time {@code sTime}.
     * 
     * @param sTime simulation time
     * @return maximum available transfer speed
     */
    abstract public ByteSpeed getTransferLimit(long sTime);
}
