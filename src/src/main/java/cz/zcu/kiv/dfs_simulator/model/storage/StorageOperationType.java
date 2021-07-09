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

public enum StorageOperationType
{
    /**
     * Progress managed by {@link StorageOperationManager}
     */
    READ(true, false),
    /**
     * Progress managed by {@link StorageOperationManager}
     */
    WRITE(true, true),
    /**
     * Progress externally managed.
     */
    CLIENT_READ(false, false),
    /**
     * Progress externally managed.
     */
    CLIENT_WRITE(false, true);
    
    /**
     * Managed flag
     */
    private final boolean managed;
    /**
     * Writing flag
     */
    private final boolean writing;
    
    /**
     * Operation type constructor.
     * 
     * @param managed managed flag
     * @param writing writing operation
     */
    private StorageOperationType(boolean managed, boolean writing)
    {
        this.managed = managed;
        this.writing = writing;
    }
    
    /**
     * Operation managed by {@link StorageOperationManager}.
     * 
     * @return managed flag
     */
    public boolean isManaged()
    {
        return this.managed;
    }
    
    /**
     * Operation is writing data.
     * 
     * @return writing flag
     */
    public boolean isWriting()
    {
        return this.writing;
    }
}
