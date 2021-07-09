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
import cz.zcu.kiv.dfs_simulator.model.ByteSize;
import cz.zcu.kiv.dfs_simulator.model.storage.ServerStorage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.ObservableList;

/**
 * Filesystem directory.
 */
public class FsDirectory extends FileSystemObject
{
    /**
     * Persistable identificator
     */
    public static final String PERSISTABLE_NAME = "fs_directory";
    
    /**
     * Character separating directories in path
     */
    public static final String DIR_PATH_SEPARATOR = "/";
    
    /**
     * Directory children
     */
    protected final FsObjectChildContainer childContainer;
    
    /**
     * Construct directory.
     * 
     * @param name directory name
     * @param parent directory parent
     */
    public FsDirectory(String name, FsDirectory parent)
    {
        super(name, parent);
        
        this.childContainer = new FsObjectChildContainer();
    }
    
    /**
     * Construct directory without parent.
     * 
     * @param name directory name
     */
    public FsDirectory(String name)
    {
        this(name, null);
    }
    
    /**
     * Get child objects.
     * 
     * @return child objects
     */
    public ObservableList<FileSystemObject> getChildren()
    {
        return this.childContainer.getFsObjects();
    }
    
    /**
     * Get specific child object by path.
     * 
     * @param path child object path
     * @return if found an instance of {@link FileSystemObject}, else null
     */
    public FileSystemObject getChildObject(String path)
    {
        return this.getChildObject(path, false);
    }
    
    /**
     * Get specific child object by path.
     * 
     * @param path child object path
     * @param createNonExisting specifies if file should be created in given path
     * if not found
     * @return if found an instance of {@link FileSystemObject}, else null
     */
    public FileSystemObject getChildObject(String path, boolean createNonExisting)
    {
        if(path.equals(name.get()))
        {
            return this;
        }
        
        // remove ending separator
        if(path.length() > 1 && path.endsWith(DIR_PATH_SEPARATOR))
        {
            return this.int_getChildObject(path.substring(0, path.length() - 1), true, createNonExisting);
        }
        else
        {
            return this.int_getChildObject(path, false, createNonExisting);
        }
    }
    
    /**
     * Method that is called recursively to find child object in given path.
     * 
     * @param path child path
     * @param isDirectory if object we are looking for (child) is a directory
     * @param createNonExisting create object if not found
     * @return found (or created) object
     */
    private FileSystemObject int_getChildObject(String path, boolean isDirectory, boolean createNonExisting)
    {
        // remove begining separator if it exists and we are a root directory
        if(path.startsWith(DIR_PATH_SEPARATOR))
        {
            if(this.name.get().equals(DIR_PATH_SEPARATOR))
            {
                path = path.substring(1);
            }
            else
            {
                return null;
            }
        }
        
        if(path.contains(DIR_PATH_SEPARATOR))
        {
            String[] pathParts = path.split(DIR_PATH_SEPARATOR);
            
            // get first object at path
            FileSystemObject object = this.childContainer.getByName(pathParts[0]);
            
            // create if it does not exist
            if(object == null && createNonExisting)
            {
                object = new FsDirectory(pathParts[0], this);
                this.getChildren().add(object);
            }
            
            if(object instanceof FsDirectory)
            {
                FsDirectory directory = (FsDirectory) object;
                String newPath = String.join(DIR_PATH_SEPARATOR, 
                        Arrays.copyOfRange(pathParts, 1, pathParts.length));
                
                if(isDirectory)
                {
                    newPath += DIR_PATH_SEPARATOR;
                }
                
                return directory.getChildObject(newPath, createNonExisting);
            }
        }
        else
        {
            FileSystemObject found = this.childContainer.getByNameAndType(path, (isDirectory) ? FsObjectType.DIRECTORY : FsObjectType.FILE);
            
            if(found == null && createNonExisting)
            {
                if(isDirectory)
                {
                    found = new FsDirectory(path, this);
                }
                else
                {
                    found = new FsFile(path, new ByteSize(0), this);
                }
                this.getChildren().add(found);
            }
            
            return found;
        }
        
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public void onMountDeviceChanged(ServerStorage storage)
    {
        super.onMountDeviceChanged(storage);
        
        for(FileSystemObject o : this.childContainer.getFsObjects())
        {
            // if they share our mount device notify them of change
            if(o.inheritedMountDevice.get())
            {
                o.onMountDeviceChanged(storage);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public ByteSize getMountSize()
    {
        return this.childContainer.getMountSize();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public FsObjectType getType()
    {
        return FsObjectType.DIRECTORY;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public ByteSize getSize()
    {
        return this.childContainer.getTotalSize();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public String getFullPath()
    {
        if(this.parent != null)
        {
            return this.parent.getFullPath() + name.get() + DIR_PATH_SEPARATOR;
        }
        // root
        else if(name.get().equals(DIR_PATH_SEPARATOR))
        {
            return DIR_PATH_SEPARATOR;
        }
        
        return name.get() + DIR_PATH_SEPARATOR;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public String getRelativePath()
    {
        return name.get() + DIR_PATH_SEPARATOR;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public List<StatePersistable> getPersistableChildren()
    {
        List<StatePersistable> l = new ArrayList<>();
        l.addAll(this.getChildren());
        
        return l;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public StatePersistableElement export(StatePersistenceLogger logger)
    {
        StatePersistableElement element = new StatePersistableElement(this.getPersistableName());
        
        element.addAttribute(new StatePersistableAttribute("name", name.get()));
        
        return element;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public void restoreState(StatePersistableElement state, StatePersistenceLogger logger, Object... args) throws InvalidPersistedStateException
    {
        // @TODO not sure if i want to expose fs manager to a fs object
        if(args.length != 1 || !(args[0] instanceof ServerFileSystemManager))
        {
            throw new InvalidPersistedStateException("Expected ServerFileSystemManager as argument to restore state: " + state);
        }
        
        ServerFileSystemManager fsManager = (ServerFileSystemManager) args[0];
        
        if(state != null)
        {
            StatePersistableAttribute nameAttr = state.getAttribute("name");
            
            if(nameAttr == null || !nameAttr.getValue().equals(name.get()))
            {
                throw new InvalidPersistedStateException(
                        "Unexpected name attribute for Directory " + state);
            }
            
            for(StatePersistableElement childElement : state.getElements())
            {
                StatePersistableAttribute childNameAttr = childElement.getAttribute("name");
                
                if(childNameAttr == null)
                {
                    throw new InvalidPersistedStateException("Expected name attribute for file system object: " + childNameAttr);
                }
                
                if(childElement.getName().equals(FsDirectory.PERSISTABLE_NAME))
                {
                    FsDirectory childDir = new FsDirectory(childNameAttr.getValue(), this);
                    childDir.restoreState(childElement, logger, fsManager);
                    
                    try
                    {
                        fsManager.addDirectoryChild(this, childDir);
                    }
                    catch (NotEnoughSpaceLeftException ex)
                    {
                        // @TODO transfer this to other exception since this, in theory, should never
                        // happen because storage is to be restored AFTER fs structure
                        Logger.getLogger(FsDirectory.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                else if(childElement.getName().equals(FsFile.PERSISTABLE_NAME))
                {
                    FsFile childFile = new FsFile(childNameAttr.getValue(), new ByteSize(0), this);
                    childFile.restoreState(childElement, logger);
                    
                    try
                    {
                        fsManager.addDirectoryChild(this, childFile);
                    }
                    catch (NotEnoughSpaceLeftException ex)
                    {
                        throw new InvalidPersistedStateException("Unable to create file in a directory " + name.get() + " because of insufficient space left on mounted device.");
                    }
                }
            }
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
