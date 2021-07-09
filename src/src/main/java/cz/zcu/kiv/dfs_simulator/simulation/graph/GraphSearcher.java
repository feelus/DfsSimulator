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

package cz.zcu.kiv.dfs_simulator.simulation.graph;

import cz.zcu.kiv.dfs_simulator.model.ModelNode;
import cz.zcu.kiv.dfs_simulator.model.ModelServerNode;
import cz.zcu.kiv.dfs_simulator.simulation.graph.metric.GraphMetric;
import cz.zcu.kiv.dfs_simulator.model.connection.ModelNodeConnection;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.NotMountedException;
import cz.zcu.kiv.dfs_simulator.simulation.SimulationTask;
import cz.zcu.kiv.dfs_simulator.simulation.SimulationType;
import java.util.List;

/**
 * Graph searcher - finds path.
 */
public interface GraphSearcher
{
    /**
     * Find path from origin node {@code origin} to target node {@code target}
     * with best metric value.
     * 
     * @param origin origin node
     * @param target target node
     * @param task currently simulated task
     * @param sTime simulation time
     * @param path will contain found path
     * @param simType simulation type
     * @return found path metric value
     * @throws NotMountedException thrown when {@code target} does not have
     * object of {@code task} mounted.
     */
    public Long findPath(ModelNode origin, ModelServerNode target, SimulationTask task, long sTime, List<ModelNodeConnection> path, SimulationType simType) throws NotMountedException;
    
    /**
     * Get metric used to find paths.
     * 
     * @return metric
     */
    public GraphMetric getMetric();
}
