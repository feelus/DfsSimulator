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

import cz.zcu.kiv.dfs_simulator.persistence.InvalidPersistedStateException;
import cz.zcu.kiv.dfs_simulator.persistence.StatePersistable;
import cz.zcu.kiv.dfs_simulator.persistence.StatePersistableAttribute;
import cz.zcu.kiv.dfs_simulator.persistence.StatePersistableElement;
import cz.zcu.kiv.dfs_simulator.persistence.StatePersistenceLogger;
import cz.zcu.kiv.dfs_simulator.helpers.Helper;
import cz.zcu.kiv.dfs_simulator.model.ByteSize;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Filesystem file.
 */
public class FsFile extends FileSystemObject
{
    /**
     * Persistable identificator
     */
    public static final String PERSISTABLE_NAME = "fs_file";
    
    /**
     * Minimum name length
     */
    public static final int NAME_MIN_LENGTH = 1;
    /**
     * Maximum name length
     */
    public static final int NAME_MAX_LENGTH = 20;
    
    /**
     * Access counter
     */
    private final IntegerProperty accessCounter = new SimpleIntegerProperty(0);
    
    /**
     * File size
     */
    protected final ByteSize size;
    
    /**
     * File constructor.
     * 
     * @param name file name
     * @param size file size
     * @param directory parent directory
     */
    public FsFile(String name, ByteSize size, FsDirectory directory)
    {
        super(name, directory);
        
        this.size = size;
    }
    
    /**
     * Set file size.
     * 
     * @param size size
     */
    public void setSize(ByteSize size)
    {
        this.size.bytesProperty().set(size.bytesProperty().get());
    }
    
    /**
     * Increment access counter by 1.
     */
    public void incrementAccessCounter()
    {
        this.accessCounter.set(this.accessCounter.get() + 1);
    }
    
    /**
     * Get access counter.
     * 
     * @return access counter
     */
    public int getAccessCount()
    {
        return this.accessCounter.get();
    }
    
    /**
     * Get writable {@link IntegerProperty} access counter.
     * 
     * @return access counter
     */
    public IntegerProperty accessCounterProperty()
    {
        return this.accessCounter;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public ByteSize getSize()
    {
        return this.size;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public FsObjectType getType()
    {
        return FsObjectType.FILE;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public ByteSize getMountSize()
    {
        return this.getSize();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public String getFullPath()
    {
        return parent.getFullPath() + name.get();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public String getRelativePath()
    {
        return name.get();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public List<StatePersistable> getPersistableChildren()
    {
        return new ArrayList<>();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public StatePersistableElement export(StatePersistenceLogger logger)
    {
        StatePersistableElement element = new StatePersistableElement(this.getPersistableName());
        
        element.addAttribute(new StatePersistableAttribute("name", name.get()));
        element.addAttribute(new StatePersistableAttribute("size", "" + this.size.bytesProperty().get()));
        
        return element;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public void restoreState(StatePersistableElement state, StatePersistenceLogger logger, Object... args) throws InvalidPersistedStateException
    {
        if(state != null)
        {
            StatePersistableAttribute nameAttr = state.getAttribute("name");
            StatePersistableAttribute sizeAttr = state.getAttribute("size");
            
            if(nameAttr == null || sizeAttr == null || !nameAttr.getValue().equals(name.get()) || !Helper.isLong(sizeAttr.getValue()))
            {
                throw new InvalidPersistedStateException("Invalid attributes for file, expected name and size: " + state);
            }
            
            this.size.setBytes(Long.parseLong(sizeAttr.getValue()));
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public String getPersistableName()
    {
        return PERSISTABLE_NAME;
    }
}
