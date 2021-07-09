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

package cz.zcu.kiv.dfs_simulator.simulation;

import java.util.List;

/**
 * Distributed file system simulator.
 */
public interface DfsSimulator
{
    /**
     * Run simulator.
     * 
     * @param logger simulator logger
     */
    public void run(DfsSimulatorLogger logger);
    /**
     * Get results of simulation.
     * 
     * @return simulation results
     */
    public List<DfsSimulatorTaskResult> getResults();
}
