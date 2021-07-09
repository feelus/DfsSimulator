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
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FileSystemObject;
import java.util.Comparator;

/**
 * Graph metric used for weighing node connections.
 */
public interface GraphMetric
{
    /**
     * Get best possible edge-weight.
     * 
     * @return best edge-weight
     */
    public long getBestMetricValue();
    /**
     * Get worst possible edge-weight.
     * 
     * @return worst edge-weight
     */
    public long getWorstMetricValue();
    
    /**
     * Get a combined weight from path weight to vertex v1 {@code metricCurrent} 
     * and path weight from v1 to v2 {@code metricEdge}.
     * 
     * @param weightCurrent metric to v1
     * @param weightEdge metric from v1 to v2
     * @param conn {@link ModelNodeConnection} from v1 to v2
     * @return metric value
     */
    public long getCombinedEdgeWeight(long weightCurrent, long weightEdge, ModelNodeConnection conn);
    
    /**
     * Return weight of given connection.
     * 
     * @param connection node connection
     * @param transferSize amount of transfered data
     * @param diskBandwidth maximum possible disk bandwidth
     * @param sTime simulation type
     * @return edge weight
     */
    public long getEdgeWeight(ModelNodeConnection connection, ByteSize transferSize, ByteSpeed diskBandwidth, long sTime);
    
    /**
     * Get comparator for comparing edge weights.
     * 
     * @return comparator
     */
    public Comparator<Long> getComparator();
    
    /**
     * Get maximum possible disk bandwidth for given object {@code targetObject}
     * on a given server {@code server}.
     * 
     * @param server server
     * @param targetObject target object
     * @param type simulation type
     * @return maximum possible disk bandwidth
     */
    public ByteSpeed getPossibleDiskBandwidth(ModelServerNode server, FileSystemObject targetObject, SimulationType type);
}
