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

import cz.zcu.kiv.dfs_simulator.model.ModelServerNode;
import cz.zcu.kiv.dfs_simulator.simulation.path.DfsPath;
import cz.zcu.kiv.dfs_simulator.model.storage.ServerStorage;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FsFile;

/**
 * Simulation logger.
 */
public interface DfsSimulatorLogger
{
    /**
     * Log simulation started event.
     * 
     * @param sTime simulation time
     */
    public void logSimulationStarted(long sTime);
    /**
     * Log simulation task started event.
     * 
     * @param task simulation task
     * @param sTime simulation time
     */
    public void logSimulationTaskStarted(SimulationTask task, long sTime);
    /**
     * Log path has been selected event.
     * 
     * @param path simulation path
     * @param sTime simulation time
     */
    public void logPathSelected(DfsPath path, long sTime);
    /**
     * Log simulation of task ended event.
     * 
     * @param result simulation task result
     * @param sTime simulation time
     */
    public void logSimulationTaskEnded(DfsSimulatorTaskResult result, long sTime);
    /**
     * Log simulation ended event.
     * 
     * @param sTime simulation time
     */
    public void logSimulationEnded(long sTime);
    
    /**
     * Log hierarchical migration process started event.
     * 
     * @param file migrated file
     * @param server server
     * @param originalStorage source storage
     * @param targetStorage target storage
     * @param sTime simulation time
     */
    public void logHierarchicalMigrationStart(FsFile file, ModelServerNode server, ServerStorage originalStorage, ServerStorage targetStorage, long sTime);
    /**
     * Log hierarchical migration process has ended event.
     * 
     * @param file migrated file
     * @param server server
     * @param originalStorage source storage
     * @param targetStorage target storage
     * @param sTime simulation time
     */
    public void logHierarchicalMigrationFinish(FsFile file, ModelServerNode server, ServerStorage originalStorage, ServerStorage targetStorage, long sTime);
    
    /**
     * Log hierarchical file upload move event - uploaded file is being
     * migrated onto different storage.
     * 
     * @param file migrated file
     * @param server server
     * @param targetStorage target storage
     * @param sTime simulation time
     */
    public void logHierarchicalUploadMove(FsFile file, ModelServerNode server, ServerStorage targetStorage, long sTime);
    
    /**
     * Log file replication started event.
     * 
     * @param file replicated file
     * @param origin replica origin (most up-to date server)
     * @param target replica target (replica is being updated)
     * @param sTime simulation time
     */
    public void logReplicationStart(FsFile file, ModelServerNode origin, ModelServerNode target, long sTime);
    /**
     * Log file replication ended event.
     * 
     * @param file replicated file
     * @param origin replica origin (most up-to date server)
     * @param target replica target (replica is being updated)
     * @param sTime simulation time
     */
    public void logReplicationFinish(FsFile file, ModelServerNode origin, ModelServerNode target, long sTime);
    
    /**
     * Log error event.
     * 
     * @param message error message
     * @param sTime simulation time
     */
    public void logError(String message, long sTime);
}
