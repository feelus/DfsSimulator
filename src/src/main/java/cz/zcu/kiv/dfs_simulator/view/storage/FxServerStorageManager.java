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

package cz.zcu.kiv.dfs_simulator.view.storage;

import cz.zcu.kiv.dfs_simulator.model.ByteSize;
import cz.zcu.kiv.dfs_simulator.model.ByteSpeed;
import cz.zcu.kiv.dfs_simulator.model.storage.ServerStorage;
import cz.zcu.kiv.dfs_simulator.model.storage.ServerStorageManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Graphical storage manager. 
 */
public class FxServerStorageManager
{
    /**
     * Observable storages
     */
    protected final ObservableList<ServerStorage> storage;

    /**
     * Graphical storage manager.
     * 
     * @param manager underlying manager
     */
    public FxServerStorageManager(ServerStorageManager manager)
    {
        // do not copy storage but build on top of it
        this.storage = FXCollections.observableList(manager.getStorage());
    }

    /**
     * Get observable storage.
     * 
     * @return storage
     */
    public ObservableList<ServerStorage> getStorage()
    {
        return this.storage;
    }

    /**
     * Add storage.
     * 
     * @param capacity storage capacity
     * @param speed storage speed
     */
    public void addStorage(ByteSize capacity, ByteSpeed speed)
    {
        this.storage.add(new ServerStorage(capacity, speed));
    }
}
