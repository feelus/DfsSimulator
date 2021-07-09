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

import cz.zcu.kiv.dfs_simulator.model.ByteSize;
import cz.zcu.kiv.dfs_simulator.model.ByteSpeed;
import cz.zcu.kiv.dfs_simulator.model.ModelServerNode;
import cz.zcu.kiv.dfs_simulator.model.connection.ModelNodeConnection;
import cz.zcu.kiv.dfs_simulator.simulation.SimulationType;
import cz.zcu.kiv.dfs_simulator.model.storage.ServerStorage;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FileSystemObject;
import java.util.Comparator;

/**
 * Min distance metric (shortest path)
 */
public class DistanceMetric implements GraphMetric
{

    /**
     * {@inheritDoc}
     */
    @Override public long getEdgeWeight(ModelNodeConnection connection, ByteSize transferSize, ByteSpeed diskBandwidth, long sTime)
    {
        return 1L;
    }

    /**
     * {@inheritDoc}
     */
    @Override public Comparator<Long> getComparator()
    {
        return (Long o1, Long o2) -> (o1.compareTo(o2));
    }

    /**
     * {@inheritDoc}
     */
    @Override public long getWorstMetricValue()
    {
        return Long.MAX_VALUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override public long getBestMetricValue()
    {
        return 1L;
    }

    /**
     * {@inheritDoc}
     */
    @Override public long getCombinedEdgeWeight(long weightCurrent, long weightEdge, ModelNodeConnection conn)
    {
        return (weightCurrent + weightEdge);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public ByteSpeed getPossibleDiskBandwidth(ModelServerNode server, FileSystemObject targetObject, SimulationType type)
    {
        ServerStorage stor = server.getFsManager().getFsObjectMountDeviceByName(targetObject);
        
        if(stor != null)
        {
            return stor.getMaximumSpeed();
        }
        
        return null;
    }
    
}
