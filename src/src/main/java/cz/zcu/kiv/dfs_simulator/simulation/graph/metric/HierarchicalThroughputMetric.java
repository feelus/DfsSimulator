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

package cz.zcu.kiv.dfs_simulator.simulation.graph.metric;

import cz.zcu.kiv.dfs_simulator.model.ByteSpeed;
import cz.zcu.kiv.dfs_simulator.model.ModelServerNode;
import cz.zcu.kiv.dfs_simulator.simulation.SimulationType;
import cz.zcu.kiv.dfs_simulator.model.storage.ServerStorage;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FileSystemObject;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FsFile;

/**
 * Extends {@link PathThroughputLatencyMetric}. Tries to find the fastest
 * possible storage for simulated task (target object).
 */
public class HierarchicalThroughputMetric extends PathThroughputLatencyMetric
{
    /**
     * {@inheritDoc}
     */
    @Override public ByteSpeed getPossibleDiskBandwidth(ModelServerNode server, FileSystemObject targetObject, SimulationType type)
    {
        ServerStorage stor = null;
        if(targetObject instanceof FsFile && type.isHierarchical() && type.getHierarchicalPlanner()!= null)
        {
            stor = type.getHierarchicalPlanner().getHighestAvailableStorage((FsFile) targetObject, server);
        }
        
        if(stor == null)
        {
            stor = server.getFsManager().getFsObjectMountDeviceByName(targetObject);
        }
        
        if(stor != null)
        {
            return stor.getMaximumSpeed();
        }
        
        return null;
    }
}