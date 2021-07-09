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
 * Persistence log message.
 */
public class SimplePersistenceMessage
{
    /**
     * Message
     */
    public final String messsage;
    /**
     * Success flag
     */
    public final boolean success;
    
    /**
     * Persistence log message.
     * 
     * @param message message
     * @param success success flag
     */
    public SimplePersistenceMessage(String message, boolean success)
    {
        this.messsage = message;
        this.success = success;
    }
}
