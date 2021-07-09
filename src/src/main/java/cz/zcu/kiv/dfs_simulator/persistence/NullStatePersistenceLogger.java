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
 * Dummy implementation of {@link StatePersistenceLogger}. Does not do any 
 * logging.
 */
public class NullStatePersistenceLogger extends StatePersistenceLogger
{

    /**
     * Does not do anything.
     * 
     * @param elementName element identificator
     * @param message message
     * @param success operation state
     */
    @Override public void logOperation(String elementName, String message, boolean success) {}
    
}
