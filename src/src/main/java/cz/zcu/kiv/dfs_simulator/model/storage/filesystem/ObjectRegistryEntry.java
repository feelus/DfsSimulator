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
import java.util.ArrayList;
import java.util.List;

/**
 * Entry with list of all servers and instances of objects for given path.
 */
public class ObjectRegistryEntry
{
    /**
     * List of all servers
     */
    public List<ModelServerNode> servers = new ArrayList<>();
    
    /**
     * List of all {@link FileSystemObject} instances
     */
    public List<FileSystemObject> fsObjects = new ArrayList<>();
}