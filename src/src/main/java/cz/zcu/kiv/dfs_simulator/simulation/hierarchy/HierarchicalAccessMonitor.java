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

package cz.zcu.kiv.dfs_simulator.simulation.hierarchy;

import cz.zcu.kiv.dfs_simulator.model.ModelServerNode;
import cz.zcu.kiv.dfs_simulator.simulation.DfsSimulatorLogger;
import cz.zcu.kiv.dfs_simulator.simulation.SimulationTask;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FsFile;

/**
 * Monitors file access during simulation of a hierarchical method.
 */
public interface HierarchicalAccessMonitor
{
    /**
     * Will be called before file is accessed during a specific {@code SimulationTask}.
     * File can be accessed MULTIPLE times during single task when dynamic pathing is enabled.
     * 
     * @param file accessed file
     * @param server server, on which the file is present
     * @param task running simulation task
     * @param logger current simulation logger
     * @param sTime current simulation time
     */
    public void onBeforeAccess(FsFile file, ModelServerNode server, SimulationTask task, DfsSimulatorLogger logger, long sTime);
    
    /** 
     * Will be called EXACTLY ONCE after file has been processed during a specific {@code SimulationTask}.
     * 
     * @param file accessed file
     * @param server server, on which the file is present
     * @param task finished simulation task
     * @param logger current simulation logger
     * @param sTime current simulation time
     */
    public void onAfterAccess(FsFile file, ModelServerNode server, SimulationTask task, DfsSimulatorLogger logger, long sTime);
}
