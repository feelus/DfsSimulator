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

package cz.zcu.kiv.dfs_simulator.view.context.server;

import cz.zcu.kiv.dfs_simulator.model.storage.ServerStorage;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.LongSumBinding;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.ServerFileSystemManager;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.LongProperty;
import javafx.collections.ObservableList;

/**
 * Binds multiple properties of storage used size into one sum.
 */
public class UsedSizeBinding extends LongSumBinding<ServerStorage>
{
    /**
     * Server file system manager
     */
    protected final ServerFileSystemManager fsManager;

    /**
     * Storage used size sum binding.
     * 
     * @param children observed storages
     * @param fsManager server fs manager
     */
    public UsedSizeBinding(ObservableList<? extends ServerStorage> children, ServerFileSystemManager fsManager)
    {
        super(children);
        
        this.fsManager = fsManager;
        this.refreshBinding();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override protected void onAfterCreate()
    {
        
    }

    /**
     * {@inheritDoc}
     */
    @Override protected void refreshBinding()
    {
        unbind(observedProperties);
        
        // load new properties
        List<LongProperty> tmp = new ArrayList<>();
        children.stream().map((x) -> fsManager.getStorageUsedSize(x).bytesProperty()).forEach((ip) ->
                {
                    tmp.add(ip);
                });
        
        observedProperties = tmp.toArray(new LongProperty[0]);
        
        super.bind(observedProperties);
        this.invalidate();
    }
    
}
