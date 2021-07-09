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

package cz.zcu.kiv.dfs_simulator.persistence;

/**
 * State persistence logger - logging information during state persisting or restoring.
 */
public abstract class StatePersistenceLogger
{
    /**
     * Log persistence operation.
     * 
     * @param elementName element id
     * @param message message
     * @param success success flag
     */
    abstract public void logOperation(String elementName, String message, boolean success);
}
