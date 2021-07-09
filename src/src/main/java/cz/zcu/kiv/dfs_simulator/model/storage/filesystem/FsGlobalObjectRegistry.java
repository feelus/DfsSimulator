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

import cz.zcu.kiv.dfs_simulator.model.ModelServerNode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Global {@link FileSystemObject} registry.
 */
public class FsGlobalObjectRegistry
{
    /**
     * Mapping of paths to {@link ObjectRegistryEntry}
     */
    private static final Map<String, ObjectRegistryEntry> OBJECT_SERVER_REGISTRY = new HashMap<>();
    
    /**
     * Add new entry.
     * 
     * @param object object
     * @param node object's server node
     */
    public static void addEntry(FileSystemObject object, ModelServerNode node)
    {
        String fullPath = object.getFullPath();
        ObjectRegistryEntry ore = OBJECT_SERVER_REGISTRY.get(fullPath);
        
        if(ore == null)
        {
            ore = new ObjectRegistryEntry();
            OBJECT_SERVER_REGISTRY.put(fullPath, ore);
        }
        
        ore.servers.add(node);
        ore.fsObjects.add(object);
    }
    
    /**
     * Remove entry.
     * 
     * @param object object
     * @param node object's server node
     */
    public static void removeEntry(FileSystemObject object, ModelServerNode node)
    {
        String fullPath = object.getFullPath();
        ObjectRegistryEntry ore = OBJECT_SERVER_REGISTRY.get(fullPath);
        
        if(ore != null)
        {
            ore.servers.remove(node);
            ore.fsObjects.remove(object);
            
            // both sets should always have the same size
            if(ore.servers.isEmpty() || ore.fsObjects.isEmpty())
            {
                // remove from registry
                OBJECT_SERVER_REGISTRY.remove(fullPath);
            }
        }
    }
    
    /**
     * Remove all object entries with given path.
     * 
     * @param path path
     */
    public static void removePath(String path)
    {
        OBJECT_SERVER_REGISTRY.remove(path);
    }
    
    /**
     * Add {@link ObjectRegistryEntry} with given path {@code path}.
     * 
     * @param path entry path
     * @param entry entry
     */
    public static void addPath(String path, ObjectRegistryEntry entry)
    {
        OBJECT_SERVER_REGISTRY.put(path, entry);
    }
    
    /**
     * Get entry by path.
     * 
     * @param path path
     * @return entry
     */
    public static ObjectRegistryEntry getObjectEntry(String path)
    {
        return OBJECT_SERVER_REGISTRY.get(path);
    }
    
    /**
     * Get entry by object's path.
     * 
     * @param object object
     * @return entry
     */
    public static ObjectRegistryEntry getObjectEntry(FileSystemObject object)
    {
        return getObjectEntry(object.getFullPath());
    }
    
    /**
     * Get servers with mounted objects in path given by {@code path}.
     * 
     * @param object object
     * @return server list
     */
    public static Set<ModelServerNode> getEntryMountedNodeList(FileSystemObject object)
    {
        ObjectRegistryEntry ore = FsGlobalObjectRegistry.getObjectEntry(object);
        Set<ModelServerNode> nodeList = new HashSet<>(ore.servers);
        
        if(nodeList.isEmpty())
        {
            nodeList.removeIf(x -> x.getFsManager().getFsObjectMountDeviceByName(object) == null);
        }
        
        return nodeList;
    }
    
    /**
     * Clean registry.
     */
    public static void purge()
    {
        OBJECT_SERVER_REGISTRY.clear();
    }
    
    /**
     * Reset access counter for all registered files.
     */
    public static void resetAccessCounters()
    {
        OBJECT_SERVER_REGISTRY.entrySet().stream().forEach(e -> {
            e.getValue().fsObjects.stream().filter(o -> o instanceof FsFile).map(o -> (FsFile) o).forEach(o -> {
                o.accessCounterProperty().set(0);
            });
        });
    }
}
