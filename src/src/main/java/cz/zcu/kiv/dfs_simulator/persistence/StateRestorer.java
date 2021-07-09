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
 * Allows restoring {@link StatePersistable} objects.
 * @param <T> persisted state input type
 */
public abstract class StateRestorer<T>
{
    /**
     * Restores persisted tree from root {@code root} element.
     * 
     * @param state persisted state input object
     * @param root root element
     * @return true if successful, else false
     * @throws InvalidPersistedStateException thrown when the persisted 
     * state is invalid.
     */
    abstract public boolean restore(T state, StatePersistable root) throws InvalidPersistedStateException;
}
