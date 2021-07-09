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

import cz.zcu.kiv.dfs_simulator.persistence.StatePersistable;
import cz.zcu.kiv.dfs_simulator.model.ByteSize;
import cz.zcu.kiv.dfs_simulator.model.SizeableObject;
import cz.zcu.kiv.dfs_simulator.model.storage.ServerStorage;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * File system object.
 */
public abstract class FileSystemObject implements SizeableObject, StatePersistable
{
    // used only for textual representation of this object
    protected static final StringProperty MOUNT_DEVICE_ID_PREFIX = new SimpleStringProperty("[");
    // used only for textual representation of this object
    protected static final StringProperty MOUNT_DEVICE_ID_SUFIX = new SimpleStringProperty("]");
    
    /**
     * Object name
     */
    protected StringProperty name;
    /**
     * Object parent directory
     */
    protected FsDirectory parent;
    /**
     * If this object inherits it's mount device from any parent up the tree (this
     * object is not directly mounted)
     */
    protected final BooleanProperty inheritedMountDevice = new SimpleBooleanProperty(true);
    /**
     * ID of device this object is mounted on
     */
    protected final StringProperty mountDeviceId = new SimpleStringProperty();
    /**
     * Whether file is in the process of migration
     */
    protected final BooleanProperty migrating = new SimpleBooleanProperty(false);
    
    /**
     * Construct new file object.
     * 
     * @param name object name
     * @param parent object parent directory
     */
    public FileSystemObject(String name, FsDirectory parent)
    {
        this.name = new SimpleStringProperty(name);
        this.parent = parent;
    }
    
    /**
     * Get writable {@link StringProperty} name.
     * 
     * @return name
     */
    public StringProperty nameProperty()
    {
        return this.name;
    }
    
    /**
     * Get writable {@link BooleanProperty} inherited mount flag.
     * 
     * @return inherited mount flag
     */
    public BooleanProperty inheritedMountDeviceProperty()
    {
        return this.inheritedMountDevice;
    }
    
    /**
     * Get writable {@link StringProperty} device ID of this
     * object's mount device.
     * 
     * @return mount device ID
     */
    public StringProperty mountDeviceIdProperty()
    {
        return this.mountDeviceId;
    }
    
    /**
     * Signal object that it's mount device has changed.
     * 
     * @param storage new mount device
     */
    public void onMountDeviceChanged(ServerStorage storage)
    {
        if (storage != null)
        {
            if(this.inheritedMountDevice.get())
            {
                this.mountDeviceId.bind(
                        Bindings.concat(MOUNT_DEVICE_ID_PREFIX, storage.idProperty(), MOUNT_DEVICE_ID_SUFIX));
            }
            else
            {
                this.mountDeviceId.bind(storage.idProperty());
            }
        }
        else
        {
            this.mountDeviceId.unbind();
            this.mountDeviceId.set("");
        }
    }
    
    /**
     * Set parent directory
     * 
     * @param parent parent directory
     */
    public void setParent(FsDirectory parent)
    {
        this.parent = parent;
    }
    
    /**
     * Get parent directory
     * 
     * @return parent directory
     */
    public FsDirectory getParent()
    {
        return this.parent;
    }
    
    /**
     * Get writable {@link BooleanProperty} flag signaling if file is in the
     * process of migration.
     * 
     * @return migration flag
     */
    public BooleanProperty migratingProperty()
    {
        return this.migrating;
    }
    
    /**
     * Checks if file is currently in the process of migration.
     * 
     * @return migration flag
     */
    public boolean isMigrating()
    {
        return this.migrating.get();
    }
    
    /**
     * Get absolute object path.
     * 
     * @return absolute path
     */
    abstract public String getFullPath();
    
    /**
     * Get relative object path.
     * 
     * @return relative path
     */
    abstract public String getRelativePath();
    
    /**
     * Get object type.
     * 
     * @return object type
     */
    abstract public FsObjectType getType();
    
    /**
     * Get amount of space this object will take up on a device
     * when mounted (includes it's children - if any). Only counts those
     * objects that don't have explicit mount device and will inherit
     * one from this object.
     * 
     * @return mounted size
     */
    abstract public ByteSize getMountSize();
    
}
