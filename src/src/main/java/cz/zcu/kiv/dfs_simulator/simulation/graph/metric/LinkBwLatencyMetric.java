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
import cz.zcu.kiv.dfs_simulator.simulation.path.DfsPath;
import cz.zcu.kiv.dfs_simulator.model.storage.ServerStorage;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FileSystemObject;
import java.util.Comparator;

/**
 * Minimizing transfer time metric
 */
public class LinkBwLatencyMetric implements GraphMetric
{

    /**
     * {@inheritDoc}
     */
    @Override public long getBestMetricValue()
    {
        return 0L;
    }

    /**
     * {@inheritDoc}
     */
    @Override public long getWorstMetricValue()
    {
        return Long.MAX_VALUE;
    }

    /**
     * Get a combined metric from path metric to vertex v1 {@code metricCurrent} 
     * and path metric from v1 to v2 {@code metricEdge}.
     * 
     * For bandwidth and latency, we select the higher number of {@code metricCurrent}
     * and {@code metricEdge} (selecting slower) and then add the latency
     * from {@code NodeConnection}.
     * 
     * @param weightCurrent metric to v1
     * @param weightEdge metric from v1 to v2
     * @param conn {@link ModelNodeConnection} from v1 to v2
     * @return metric value
     */
    @Override public long getCombinedEdgeWeight(long weightCurrent, long weightEdge, ModelNodeConnection conn)
    {
        return ( (weightCurrent > weightEdge) ? weightCurrent : weightEdge) + conn.getLatency();
    }

    /**
     * {@inheritDoc}
     */
    // vracim bez latence protoze vysledny cas je v get combined edge metric
    @Override public long getEdgeWeight(ModelNodeConnection connection, ByteSize transferSize, ByteSpeed diskBandwidth, long sTime)
    {
        long t = DfsPath.getDataTransferTime(sTime, connection.getAverageBandwidth(sTime, 0), transferSize);
        
        if(t >= 0)
        {
            return t;
        }
        
        return this.getWorstMetricValue();
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
