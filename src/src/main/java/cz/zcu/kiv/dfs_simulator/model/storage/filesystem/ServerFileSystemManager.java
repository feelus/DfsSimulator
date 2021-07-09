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
import cz.zcu.kiv.dfs_simulator.model.ByteSizeUnits;
import cz.zcu.kiv.dfs_simulator.model.ModelServerNode;
import cz.zcu.kiv.dfs_simulator.model.storage.ServerStorage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * File system manager.
 */
public class ServerFileSystemManager implements StatePersistable
{
    /**
     * Persistable identificator
     */
    protected static final String PERSISTABLE_NAME = "mount_table";
    /**
     * Persistable identificator for mount entries
     */
    protected static final String PERSISTABLE_NAME_MOUNT_ENTRY = "mount_entry";
    
    protected final ModelServerNode server;
    
    /**
     * Map of storages for each mounted {@code FsObject} object
     */
    protected final Map<FileSystemObject, ServerStorage> fileSystemObjects = new HashMap<>();
    /**
     * Map of {@code ServerStorageUsedSizeInfo} storage used size info for each storage
     */
    protected final Map<ServerStorage, ServerStorageUsedSizeInfo> storageObjects = new HashMap<>();
    
    /**
     * Construct file system manager for given {@code server} node.
     * 
     * @param server server node
     */
    public ServerFileSystemManager(ModelServerNode server)
    {
        this.server = server;
    }
    
    /**
     * Add a child to directory.
     * 
     * @param parent parent directory
     * @param child child object
     * @return add result
     * @throws NotEnoughSpaceLeftException thrown when parent directory is mounted
     * and child would inherit it's mount device, but wouldnt fit
     */
    public ServerFsAddChildResult addDirectoryChild(FsDirectory parent, FileSystemObject child) throws NotEnoughSpaceLeftException
    {
        ServerStorage storParent = this.getFsObjectMountDevice(parent);
        ServerStorage storChild = this.getFsObjectMountEntry(child);
        
        // check if child with same name already exists
        FileSystemObject existing = parent.getChildObject(child.getRelativePath());
        
        if(storParent != null && storChild == null)
        {
            long removableBytes = (existing != null) ? existing.getMountSize().bytesProperty().get() : 0;
            
            // check if we can fit child onto parent's storage
            ByteSize unused = this.getStorageUnusedSize(storParent);
            if (( (unused.bytesProperty().get() + removableBytes) - child.getMountSize().bytesProperty().get()) < 0)
            {
                throw new NotEnoughSpaceLeftException("Storage " + storParent.idProperty().get() + 
                        " has only " + unused.getHumanReadableFormat() + " available but " + 
                        child.getMountSize().getHumanReadableFormat() + " is required");
            }
        }        
        
        // based on path it should match a type (directory ending with a /)
        if(existing != null && existing.getParent() != null)
        {
            if(child instanceof FsFile && existing instanceof FsFile)
            {
                // no need to alter registry here since they share same path
                this.umount(existing);
                existing.getParent().getChildren().remove(existing);
                
                this.int_addDirectoryChild(parent, child);
                
                return ServerFsAddChildResult.ADDED;
            }
            else if(child instanceof FsDirectory && existing instanceof FsDirectory)
            {
                this.mergeDirectoryChildren((FsDirectory) existing, (FsDirectory) child);
                
                // in case any callee might need the new updated full path
                child.setParent(parent);
                
                return ServerFsAddChildResult.MERGED;
            }
        }
        else
        {
            this.int_addDirectoryChild(parent, child);
            
            return ServerFsAddChildResult.ADDED;
        }
        
        return ServerFsAddChildResult.REFUSED;
    }
    
    /**
     * Merge children from two directories into {@code existing}.
     * 
     * @param existing first directory
     * @param newDir second directory
     */
    private void mergeDirectoryChildren(FsDirectory existing, FsDirectory newDir)
    {
        for(FileSystemObject object : newDir.getChildren())
        {
            FileSystemObject exObject = existing.getChildObject(object.getRelativePath());

            if(exObject != null)
            {
                if(object instanceof FsDirectory && exObject instanceof FsDirectory)
                {
                    this.mergeDirectoryChildren((FsDirectory) exObject, (FsDirectory) object);
                }
                else
                {
                    // adding file
                    this.removeDirectoryChild(exObject);
                    
                    this.int_addDirectoryChild(existing, object);
                }
            }
            else
            {
                // adding directory or file
                this.int_addDirectoryChild(existing, object);
            }
        }
    }
    
    /**
     * Internal method. Add child to directory without any checks.
     * 
     * @param parent parent directory
     * @param child child object
     */
    private void int_addDirectoryChild(FsDirectory parent, FileSystemObject child)
    {
        // register object
        FsGlobalObjectRegistry.addEntry(child, this.server);
        
        // enough space left or parent is not mounted, add child
        parent.getChildren().add(child);
        // set new parent
        child.setParent(parent);
        // notify of possible new storage
        child.onMountDeviceChanged(this.getFsObjectMountDevice(child));
    }
    
    /**
     * Remove child from it's parent.
     * 
     * @param child child object
     */
    public void removeDirectoryChild(FileSystemObject child)
    {
        FsGlobalObjectRegistry.removeEntry(child, this.server);
        
        this.umount(child);
        child.getParent().getChildren().remove(child);
    }
    
    /**
     * Remove child from it's parent.
     * 
     * @param childFullPath child object path
     */
    public void removeDirectoryChild(String childFullPath)
    {
        // find object
        FileSystemObject object = this.server.getRootDir().getChildObject(childFullPath);
        
        if(object != null)
        {
            removeDirectoryChild(object);
        }
    }
    
    /**
     * Get mount device of {@code object}.
     * 
     * @param object object
     * @return mount device or null
     */
    public ServerStorage getFsObjectMountEntry(FileSystemObject object)
    {
        return this.fileSystemObjects.get(object);
    }
    
    /**
     * Returns either direct or indirect mount device of passed {@code FsObject} object.
     * It looks for a direct entry for given object first and if it doesnt find one
     * attempts to get one from hierarchy up its parent tree.
     * 
     * @param object object we want to know the mount storage of
     * @return either storage or null
     */
    public ServerStorage getFsObjectMountDevice(FileSystemObject object)
    {
        ServerStorage stor = this.fileSystemObjects.get(object);
        
        if(stor != null)
        {
            return stor;
        }
        
        // no direct match, try to look for entry of higher level dirs
        FsDirectory pDir = object.getParent();
        
        while(pDir != null)
        {
            stor = this.fileSystemObjects.get(pDir);
            
            if(stor != null)
            {
                return stor;
            }
            
            pDir = pDir.getParent();
        }
        
        // no match
        return null;
    }
    
    /**
     * Returns either direct or indirect mount device of passed {@code FsObject} object.
     * It looks for a direct entry for given object first and if it doesnt find one
     * attempts to get one from hierarchy up its parent tree.
     * 
     * @param object object we want to know the mount storage of
     * @return either storage or null
     */
    public ServerStorage getFsObjectMountDeviceByName(FileSystemObject object)
    {
        for(Entry<FileSystemObject, ServerStorage> fsoE : this.fileSystemObjects.entrySet())
        {
            if(fsoE.getKey().getFullPath().equals(object.getFullPath()))
            {
                return fsoE.getValue();
            }
        }
        
        FsDirectory pDir = object.getParent();
        
        while(pDir != null)
        {
            for(Entry<FileSystemObject, ServerStorage> fsoE : this.fileSystemObjects.entrySet())
            {
                if(fsoE.getKey().getFullPath().equals(pDir.getFullPath()))
                {
                    return fsoE.getValue();
                }
            }
            
            pDir = pDir.getParent();
        }
        
        return null;
    }
    
    /**
     * Returns a list of FsObjects that will be mounted as well by mounting
     * their root folder.
     * 
     * @param root root folder
     * @return list of FsObjects to be affected by the mount
     */
    public List<FileSystemObject> getMountableHierarchy(FsDirectory root)
    {
        List<FileSystemObject> mountable = new ArrayList<>();
        
        mountable.add(root);
        
        for(FileSystemObject o : root.getChildren())
        {
            ServerStorage stor = this.fileSystemObjects.get(o);
            
            if(stor == null)
            {
                mountable.add(o);
                
                if(o instanceof FsDirectory)
                {
                    mountable.addAll(this.getMountableHierarchy((FsDirectory) o));
                }
            }
            else
            {
                // if its a directory and its already mounted,
                // we do not need to enter it because its children
                // already share its mount
                if(o instanceof FsDirectory)
                {
                    break;
                }
            }
        }
        
        return mountable;
    }
    
    /**
     * Returns the {@code ByteSize} representation of size of passed
     * {@code FsObject} objects. Does not account for children of those objects.
     * 
     * @param objects list of objects
     * @return size of objects
     */
    public ByteSize getObjectListSize(List<FileSystemObject> objects)
    {
        long bytes = 0;
        
        for(FileSystemObject o : objects)
        {
            if(o instanceof FsFile)
            {
                bytes += o.getSize().bytesProperty().get();
            }
        }
        
        return new ByteSize(bytes, ByteSizeUnits.B);
    }
    
    /**
     * Forcefully mount {@code object} onto {@code storage}. If necessary,
     * {@code storage} will be resized to fit {@code object}.
     * 
     * @param storage stroage
     * @param object object
     */
    public void forceMount(ServerStorage storage, FileSystemObject object)
    {
        ByteSize unused = this.getStorageUnusedSize(storage);
        
        long bytesOver = object.getMountSize().bytesProperty().get() - unused.bytesProperty().get();
        if( bytesOver > 0)
        {
            this.resizeStorage(storage, new ByteSize(bytesOver, ByteSizeUnits.B));
        }
        
        this.int_umount(object);
        this.int_mount(storage, object);
    }
    
    /**
     * Mount {@code object} onto {@code storage}.
     * 
     * @param storage storage
     * @param object object
     * @throws NotEnoughSpaceLeftException thrown when {@code storage} does
     * not have enough available space
     */
    public void mount(ServerStorage storage, FileSystemObject object) throws NotEnoughSpaceLeftException
    {
        ByteSize unused = this.getStorageUnusedSize(storage);
        if( (unused.bytesProperty().get() - object.getMountSize().bytesProperty().get()) < 0)
        {
            throw new NotEnoughSpaceLeftException("Storage " + storage.idProperty().get() + 
                    " has only " + unused.getHumanReadableFormat() + " available but " + 
                    object.getMountSize().getHumanReadableFormat() + " is required");
        }
        
        this.forceMount(storage, object);
    }
    
    /**
     * Umount {@code object}.
     * 
     * @param object object
     */
    public void umount(FileSystemObject object)
    {
        this.int_umount(object);
    }
    
    /**
     * Internal mount method, does not do any checks. Mount {@code object}
     * onto {@code storage}.
     * 
     * @param storage storage
     * @param object object
     */
    private void int_mount(ServerStorage storage, FileSystemObject object)
    {
        this.fileSystemObjects.put(object, storage);
        
        // get child container or create a new one
        ServerStorageUsedSizeInfo usedSizeInfo = this.getOrCreateInfo(storage);
        
        usedSizeInfo.addSize(object.getMountSize());
        
        object.inheritedMountDeviceProperty().set(false);
        object.onMountDeviceChanged(storage);
    }
    
    /**
     * Internal umount method.
     * 
     * @param object object
     */
    private void int_umount(FileSystemObject object)
    {
        ServerStorage storage = this.fileSystemObjects.get(object);
        
        if(storage != null)
        {
            this.fileSystemObjects.remove(object);
            
            ServerStorageUsedSizeInfo usedSizeInfo = this.storageObjects.get(storage);

            // should never be null
            if(usedSizeInfo != null)
            {
                usedSizeInfo.removeSize(object.getMountSize());
            }
            
            object.inheritedMountDeviceProperty().set(true);
            object.onMountDeviceChanged(this.getFsObjectMountDevice(object));
        }
    }
    
    /**
     * Get {@link ServerStorageUsedSizeInfo} for {@code storage}. If info 
     * does not exist, create new one.
     * 
     * @param storage storage
     * @return info
     */
    private ServerStorageUsedSizeInfo getOrCreateInfo(ServerStorage storage)
    {
        ServerStorageUsedSizeInfo usedSizeInfo = this.storageObjects.get(storage);
        
        if(usedSizeInfo == null)
        {
            usedSizeInfo = new ServerStorageUsedSizeInfo();
            this.storageObjects.put(storage, usedSizeInfo);
        }
        
        return usedSizeInfo;
    }
    
    /**
     * Checks whether any files are mounted on {@code storage}.
     * 
     * @param storage storage
     * @return true if used, false otherwise
     */
    public boolean isStorageUsed(ServerStorage storage)
    {
        return this.fileSystemObjects.entrySet().stream().anyMatch(e -> e.getValue() == storage);
    }
    
    /**
     * Get amount of used size of {@code storage}.
     * 
     * @param storage storage
     * @return used size
     */
    public ByteSize getStorageUsedSize(ServerStorage storage)
    {
        return this.getOrCreateInfo(storage).getUsedSize();
    }
    
    /**
     * Get amount of unused size of {@code storage}.
     * 
     * @param storage storage
     * @return unused size
     */
    public ByteSize getStorageUnusedSize(ServerStorage storage)
    {
        ByteSize used = this.getStorageUsedSize(storage);
        
        return new ByteSize(storage.getSize().bytesProperty().get() - used.bytesProperty().get(), ByteSizeUnits.B);
    }
    
    /**
     * Remove storage from managed list.
     * 
     * @param storage storage
     */
    public void removeStorage(ServerStorage storage)
    {
        this.storageObjects.remove(storage);
        
        for(Iterator<Entry<FileSystemObject, ServerStorage>> it = this.fileSystemObjects.entrySet().iterator(); it.hasNext();)
        {
            Entry<FileSystemObject, ServerStorage> e = it.next();
            
            if(e.getValue() == storage)
            {
                e.getKey().inheritedMountDeviceProperty().set(true);
                e.getKey().onMountDeviceChanged(null);
                
                it.remove();
            }
        }
    }
    
    /**
     * Checks whether {@code file} can fit with new size of {@code newSize}
     * onto it's current storage.
     * 
     * @param file file
     * @param newSize new file size
     * @return true if can fit, false otherwise
     */
    public boolean canFileFitStorage(FsFile file, ByteSize newSize)
    {
        if(newSize.bytesProperty().get() > file.getSize().bytesProperty().get())
        {
            ServerStorage stor = this.getFsObjectMountDevice(file);

            // either storage is null or its unused size is atleast as big 
            // as newSize - oldSize
            return (stor == null || 
                    this.getStorageUnusedSize(stor).bytesProperty().get() >= (newSize.bytesProperty().get() - file.getSize().bytesProperty().get()));
        }
        
        return true;
        
    }
    
    /**
     * Resizes storage by {@code incBytes}.
     * 
     * @param storage storage
     * @param incBytes increment size
     */
    public void resizeStorage(ServerStorage storage, ByteSize incBytes)
    {
        storage.getSize().setBytes(storage.getSize().bytesProperty().get() + incBytes.bytesProperty().get());
    }
    
    /**
     * Resizes storage of {@code file} so that the file can fit with ne size of {@code newSize}.
     * 
     * @param file file
     * @param newSize new file size
     */
    public void resizeStorageToFit(FsFile file, ByteSize newSize)
    {
        if(newSize.bytesProperty().get() > file.getSize().bytesProperty().get())
        {
            ServerStorage stor = this.getFsObjectMountDevice(file);
            ByteSize reqBytes = new ByteSize(newSize.bytesProperty().get() - file.getSize().bytesProperty().get(), ByteSizeUnits.B);
            ByteSize avalBytes = this.getStorageUnusedSize(stor);

            if(stor != null)
            {
                this.resizeStorage(stor, 
                        new ByteSize(reqBytes.bytesProperty().get() - avalBytes.bytesProperty().get(), ByteSizeUnits.B));
            }
        }
    }
    
    /**
     * Internal method. Add all files that share mount device from {@code directory}.
     * 
     * @param directory parent directory
     * @param files files
     */
    private void int_addFilesDirectlySharingMount(FsDirectory directory, List<FsFile> files)
    {
        for(FileSystemObject childObject : directory.getChildren())
        {
            ServerStorage storage = this.getFsObjectMountEntry(childObject);
            
            // we do not need to recursively look through all directory children
            // if the directory itself is mounted since the directory
            // would have its own mount entry
            if(storage == null)
            {
                if(childObject instanceof FsFile)
                {
                    files.add((FsFile) childObject);
                }
                else
                {
                    this.int_addFilesDirectlySharingMount((FsDirectory) childObject, files);
                }
            }
        }
    }
    
    /**
     * Get all files mounted on {@code storage}.
     * 
     * @param storage storage
     * @return mounted files
     */
    public List<FsFile> getStorageMountedFiles(ServerStorage storage)
    {
        List<FileSystemObject> mountedObjects = 
                this.fileSystemObjects.entrySet().stream().filter(entry -> entry.getValue() == storage).map(entry -> entry.getKey()).collect(Collectors.toList());
        
        List<FsFile> mountedFiles = new ArrayList<>();
        mountedObjects.forEach((mountedObject) ->
        {
            if(mountedObject instanceof FsFile)
            {
                mountedFiles.add((FsFile) mountedObject);
            }
            else
            {
                this.int_addFilesDirectlySharingMount((FsDirectory) mountedObject, mountedFiles);
            }
        });
        
        return mountedFiles;
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
        
        for(Entry<FileSystemObject, ServerStorage> e : this.fileSystemObjects.entrySet())
        {
            StatePersistableElement mountEntry = new StatePersistableElement(PERSISTABLE_NAME_MOUNT_ENTRY);
            mountEntry.addAttribute(new StatePersistableAttribute("path", e.getKey().getFullPath()));
            mountEntry.addAttribute(new StatePersistableAttribute("device", e.getValue().idProperty().get()));
            
            element.addElement(mountEntry);
        }
        
        return element;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public void restoreState(StatePersistableElement state, StatePersistenceLogger logger, Object... args) throws InvalidPersistedStateException
    {
        if(state != null)
        {
            for(StatePersistableElement childElem : state.getElements())
            {
                if(childElem.getName().equals(PERSISTABLE_NAME_MOUNT_ENTRY))
                {
                    this.restoreMountEntry(childElem);
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
    
    /**
     * Restore mount entry from persisted state {@code entry}.
     * 
     * @param entry state
     * @throws InvalidPersistedStateException thrown when {@entry} is invalid.
     */
    private void restoreMountEntry(StatePersistableElement entry) throws InvalidPersistedStateException
    {
        StatePersistableAttribute pathAttr = entry.getAttribute("path");
        StatePersistableAttribute deviceAttr = entry.getAttribute("device");
        
        if(pathAttr == null || deviceAttr == null)
        {
            throw new InvalidPersistedStateException("Invalid attributes for mount entry, expected path and device: " + entry);
        }
        
        // attempt to find target device
        ServerStorage stor = this.server.getStorageManager().getStorageByID(deviceAttr.getValue());
        
        if(stor != null)
        {
            // attempt to find associated file
            FileSystemObject object = this.server.getRootDir().getChildObject(pathAttr.getValue());
            
            if(object != null)
            {
                try
                {
                    this.mount(stor, object);
                }
                catch (NotEnoughSpaceLeftException ex)
                {
                    throw new InvalidPersistedStateException("Unable to mount " + object + " to storage " + stor + " due to insufficient space left on device.");
                }
            }
            else
            {
                throw new InvalidPersistedStateException("Unable to mount " + pathAttr.getValue() + " to storage ID " + deviceAttr.getValue() + ", cannot find target file system objec: " + entry);
            }
        }
        else
        {
            throw new InvalidPersistedStateException("Unable to mount " + pathAttr.getValue() + " to storage ID " + deviceAttr.getValue() + ", cannot find target storage: " + entry);
        }
    }
    
}
