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

package cz.zcu.kiv.dfs_simulator.model.storage.filesystem;

import cz.zcu.kiv.dfs_simulator.model.ByteSize;
import cz.zcu.kiv.dfs_simulator.model.storage.ServerStorage;
import javafx.beans.binding.NumberBinding;

/**
 * Information about used size of {@link ServerStorage}.
 */
public class ServerStorageUsedSizeInfo
{
    /**
     * Used size
     */
    protected final ByteSize usedSize = new ByteSize(0);
    
    /**
     * Used size binding
     */
    protected NumberBinding usedSizeBinding;
    
    /**
     * Add {@code size} to {@code usedSizeBinding}.
     * 
     * @param size size
     */
    public void addSize(ByteSize size)
    {
        if(this.usedSizeBinding == null)
        {
            this.usedSizeBinding = size.bytesProperty().add(0);
        }
        else
        {
            this.usedSizeBinding = this.usedSizeBinding.add(size.bytesProperty());
        }
        
        this.usedSize.bytesProperty().bind(this.usedSizeBinding);
    }
    
    /**
     * Remove {@code size} from {@code usedSizeBinding}.
     * 
     * @param size size
     */
    public void removeSize(ByteSize size)
    {
        this.usedSizeBinding = this.usedSizeBinding.subtract(size.bytesProperty());
        
        this.usedSize.bytesProperty().bind(this.usedSizeBinding);
    }
    
    /**
     * Get used size.
     * 
     * @return used size
     */
    public ByteSize getUsedSize()
    {
        return this.usedSize;
    }
}
