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

import java.util.List;

/**
 * State persistable object.
 */
public interface StatePersistable
{
    /**
     * Returns persistable identifier.
     * 
     * @return persistable identifier
     */
    public String getPersistableName();
    
    /**
     * Returns a list of persistable children (objects, that will be included during export).
     * 
     * @return persistable children
     */
    public List<? extends StatePersistable> getPersistableChildren();
    
    /**
     * Export this object.
     * 
     * @param logger information logger
     * @return exported object state
     */
    public StatePersistableElement export(StatePersistenceLogger logger);
    
    /**
     * Restore this object's state.
     * 
     * @param state imported object state
     * @param logger information logger
     * @param args additional arguments
     * @throws InvalidPersistedStateException can be thrown when {@code state}
     * has invalid values.
     */
    public void restoreState(StatePersistableElement state, StatePersistenceLogger logger, Object... args) throws InvalidPersistedStateException;
}
