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
 * Allows persisting {@link StatePersistable} objects.
 * @param <T> persisted state output type
 */
public abstract class StatePersistor<T>
{
    /**
     * Persist all elements in the tree given by root {@code root}.
     * 
     * @param root element
     * @return true if successfully persisted
     * @throws Exception thrown when there was an error while persisting elements
     */
    abstract public boolean persist(StatePersistable root) throws Exception;
    /**
     * Returns persisted state as object.
     * 
     * @return persisted state
     */
    abstract public T getPersistedState();
}
