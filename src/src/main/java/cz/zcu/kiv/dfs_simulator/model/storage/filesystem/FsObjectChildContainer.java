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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Container for children of {@link FsDirectory}.
 */
public class FsObjectChildContainer
{
    /**
     * List of children
     */
    protected final ObservableList<FileSystemObject> children = FXCollections.observableArrayList();
    
    /**
     * Total size of all children
     */
    protected final ByteSize totalSize;
    
    /**
     * Total mount size of all children
     */
    protected final ByteSize mountSize;
    
    /**
     * Total size binding
     */
    protected TotalSizeBinding totalSizeBinding;
    /**
     * Mount size binding
     */
    protected MountChildSizeBinding mountSizeBinding;
    
    /**
     * Construct new container with initial sizes set to 0.
     */
    public FsObjectChildContainer()
    {
        this.totalSizeBinding = new TotalSizeBinding(this.children);
        this.mountSizeBinding = new MountChildSizeBinding(this.children);
        
        this.totalSize = new ByteSize(0);
        this.mountSize = new ByteSize(0);
        
        this.totalSize.bytesProperty().bind(this.totalSizeBinding);
        this.mountSize.bytesProperty().bind(this.mountSizeBinding);
    }
    
    /**
     * Get all child objects.
     * 
     * @return child objects
     */
    public ObservableList<FileSystemObject> getFsObjects()
    {
        return this.children;
    }
    
    /**
     * Get total size of all child objects.
     * 
     * @return total size
     */
    public ByteSize getTotalSize()
    {
        return this.totalSize;
    }
    
    /**
     * Get total mount size of all child objects.
     * 
     * @return total mount size
     */
    public ByteSize getMountSize()
    {
        return this.mountSize;
    }
    
    /**
     * Get child object by name.
     * 
     * @param name name
     * @return child object if found, null otherwise
     */
    public FileSystemObject getByName(String name)
    {
        for(FileSystemObject object : this.children)
        {
            if(object.nameProperty().get().equals(name))
            {
                return object;
            }
        }
        
        return null;
    }
    
    /**
     * Get child object by name and type.
     * 
     * @param name name
     * @param type type
     * @return child object if found, null otherwise
     */
    public FileSystemObject getByNameAndType(String name, FsObjectType type)
    {
        for(FileSystemObject object : this.children)
        {
            if(object.nameProperty().get().equals(name) && object.getType() == type)
            {
                return object;
            }
        }
        
        return null;
    }
}
