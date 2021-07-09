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

import cz.zcu.kiv.dfs_simulator.persistence.InvalidPersistedStateException;
import cz.zcu.kiv.dfs_simulator.persistence.StatePersistable;
import cz.zcu.kiv.dfs_simulator.persistence.StatePersistableAttribute;
import cz.zcu.kiv.dfs_simulator.persistence.StatePersistableElement;
import cz.zcu.kiv.dfs_simulator.persistence.StatePersistenceLogger;
import cz.zcu.kiv.dfs_simulator.helpers.Helper;
import cz.zcu.kiv.dfs_simulator.model.ByteSize;
import cz.zcu.kiv.dfs_simulator.model.ByteSizeUnits;
import cz.zcu.kiv.dfs_simulator.model.ByteSpeed;
import cz.zcu.kiv.dfs_simulator.model.ByteSpeedUnits;
import cz.zcu.kiv.dfs_simulator.model.SizeableObject;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Server storage
 */
public class ServerStorage implements SizeableObject, StatePersistable
{
    /**
     * Persistable identificator
     */
    public static final String PERSISTABLE_NAME = "storage_device";
    
    /**
     * Storage id  prefix
     */
    public static final String ID_PREFIX = "stor";

    /**
     * Maximum storage capacity
     */
    public static final ByteSize MAX_CAPACITY = new ByteSize(100000, ByteSizeUnits.GB);
    /**
     * Maximum storage speed
     */
    public static final ByteSpeed MAX_SPEED = new ByteSpeed(100000, ByteSpeedUnits.MBPS);

    /**
     * Storage identifier counter
     */
    protected static int storageIdCounter = 1;

    /**
     * Storage id
     */
    private final StringProperty id;
    /**
     * Storage size
     */
    private final ByteSize size;
    /**
     * Storage speed
     */
    private final ByteSpeed speed;
    
    /**
     * Storage operations (I/O) manager
     */
    private final StorageOperationManager operationManager;
    
    /**
     * Constructs server storage with capacity {@code capacity} and speed
     * {@code speed}.
     * 
     * @param capacity storage capacity
     * @param speed storage speed
     */
    public ServerStorage(ByteSize capacity, ByteSpeed speed)
    {
        this.id = new SimpleStringProperty(ID_PREFIX + storageIdCounter++);
        this.size = capacity;
        this.speed = speed;
        
        this.operationManager = new StorageOperationManager(this);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public ByteSize getSize()
    {
        return this.size;
    }
    
    /**
     * Get maximum storage speed.
     * 
     * @return maximum speed
     */
    public ByteSpeed getMaximumSpeed()
    {
        return this.speed;
    }
    
    /**
     * Get storage operations manager.
     * 
     * @return storage op. manager
     */
    public StorageOperationManager getOperationManager()
    {
        return this.operationManager;
    }
    
    /**
     * Return writable {@link StringProperty} id.
     * 
     * @return id
     */
    public StringProperty idProperty()
    {
        return this.id;
    }
    
    /**
     * String representation of storage.
     * 
     * @return string representation
     */
    @Override public String toString()
    {
        return this.id.get() + 
                " [" + this.getSize().getHumanReadableFormat() + 
                ", " + this.getMaximumSpeed().getHumanReadableFormat() + "]";
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
        
        element.addAttribute(new StatePersistableAttribute("id", this.id.get()));
        element.addAttribute(new StatePersistableAttribute("size", "" + this.size.bytesProperty().get()));
        element.addAttribute(new StatePersistableAttribute("speed", "" + this.speed.bpsProperty().get()));
        
        return element;
    }

    /**
     * {@inheritDoc}
     */
    @Override public void restoreState(StatePersistableElement state, StatePersistenceLogger logger, Object... args) throws InvalidPersistedStateException
    {
        if(state != null)
        {
            StatePersistableAttribute idAttr = state.getAttribute("id");
            StatePersistableAttribute sizeAttr = state.getAttribute("size");
            StatePersistableAttribute speedAttr = state.getAttribute("speed");
            
            if(idAttr == null || sizeAttr == null || speedAttr == null || 
                    !(Helper.isLong(sizeAttr.getValue())) || 
                    !(Helper.isLong(speedAttr.getValue())))
            {
                throw new InvalidPersistedStateException("Expected id, size and speed attributes (size and speed of numeric type): " + state);
            }
            
            this.id.set(idAttr.getValue());
            this.size.setBytes(Long.parseLong(sizeAttr.getValue()));
            this.speed.setBps(Long.parseLong(speedAttr.getValue()));
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
