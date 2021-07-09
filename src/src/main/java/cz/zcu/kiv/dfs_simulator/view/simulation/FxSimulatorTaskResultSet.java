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

package cz.zcu.kiv.dfs_simulator.view.simulation;

import cz.zcu.kiv.dfs_simulator.simulation.DfsSimulatorSimulationResult;
import cz.zcu.kiv.dfs_simulator.simulation.DfsStringSimulatorLogger;

/**
 * Result of a single simulation run and it's logger.
 */
public class FxSimulatorTaskResultSet
{
    /**
     * Result
     */
    private final DfsSimulatorSimulationResult resultSet;
    /**
     * Logger
     */
    private final DfsStringSimulatorLogger logger;
    
    /**
     * Simulator result and it's logger.
     * 
     * @param resultSet result
     * @param logger logger
     */
    public FxSimulatorTaskResultSet(DfsSimulatorSimulationResult resultSet, DfsStringSimulatorLogger logger)
    {
        this.resultSet = resultSet;
        this.logger = logger;
    }

    /**
     * Get result.
     * 
     * @return result
     */
    public DfsSimulatorSimulationResult getSimulationResult()
    {
        return resultSet;
    }

    /**
     * Get logger.
     * 
     * @return logger
     */
    public DfsStringSimulatorLogger getLogger()
    {
        return logger;
    }
    
    /**
     * Returns text representation - if result is not null, returns
     * result type, otherwise empty string.
     * 
     * @return string representation
     */
    @Override public String toString()
    {
        if(this.resultSet != null)
        {
            return this.resultSet.getType().toString();
        }
        
        return "";
    }
    
}
