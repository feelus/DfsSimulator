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

package cz.zcu.kiv.dfs_simulator.simulation.path;

import cz.zcu.kiv.dfs_simulator.model.ModelClientNode;
import cz.zcu.kiv.dfs_simulator.model.ModelNode;
import cz.zcu.kiv.dfs_simulator.model.ModelServerNode;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.NotMountedException;
import cz.zcu.kiv.dfs_simulator.simulation.SimulationTask;
import cz.zcu.kiv.dfs_simulator.simulation.SimulationType;

/**
 * Selecting (picking) path for data transfer.
 */
public interface DfsPathPicker
{
    /**
     * Select path from {@code origin} to any destination server, based on
     * simulation task {@code task}.
     * 
     * @param origin origin node
     * @param task simulation task associated with this path select
     * @param sTime simulation time
     * @param forceTarget if we want the path to end at this target server 
     * (if null, best target will be selected)
     * @param simType simulation type
     * @return selected path
     * @throws NoPathAvailableException when there is no path from {@code origin} to
     * a target object given by {@code task}
     * @throws FsObjectNotFoundException when the file associated with {@code task}
     * cannot be found
     * @throws NotMountedException when the file associated with {@code task}
     * is not mounted
     */
    public DfsPath selectPath(ModelNode origin, SimulationTask task, long sTime, ModelServerNode forceTarget, SimulationType simType) throws NoPathAvailableException, FsObjectNotFoundException, NotMountedException;
    
    /**
     * Get the amount of the required to query any registry (metadata server).
     * Usually used when we are asking if some file exists or not - this
     * will give us the time (latency) required to contact registry.
     * 
     * @param origin origin node
     * @return amount of time
     * @throws NoNeighboursAvailableException when {@code origin} has no
     * server neighbours
     */
    public long getObjectRegistryQueryTime(ModelClientNode origin) throws NoNeighboursAvailableException;
}
