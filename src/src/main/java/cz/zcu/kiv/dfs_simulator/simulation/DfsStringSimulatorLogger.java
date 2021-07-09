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
import java.util.ArrayList;
import java.util.List;

/**
 * Simple string logger.
 */
public class DfsStringSimulatorLogger implements DfsSimulatorLogger
{
    
    /**
     * Logged messages
     */
    private final List<String> messages = new ArrayList<>();

    /**
     * {@inheritDoc}
     */
    @Override public void logSimulationStarted(long sTime)
    {
        this.messages.add("[" + sTime + "] Simulation started.");
    }

    /**
     * {@inheritDoc}
     */
    @Override public void logSimulationTaskStarted(SimulationTask task, long sTime)
    {
        StringBuilder sb = new StringBuilder();
        
        sb.append("[");
        sb.append(sTime);
        sb.append("] ");
        
        sb.append("Starting task: ");
        sb.append(task.toString());
        
        this.messages.add(sb.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override public void logPathSelected(DfsPath path, long sTime)
    {
        StringBuilder sb = new StringBuilder();
        
        sb.append("[");
        sb.append(sTime);
        sb.append("] ");
        sb.append("Selecting path [");
        sb.append(path.toString());
        sb.append("] with current maximum throughput ");
        sb.append(path.getCurrentPossibleThroughput(sTime).getHumanReadableFormat());
        sb.append(" and total latency of ");
        sb.append(path.getCumLatency());
        sb.append(" ms.");
        
        this.messages.add(sb.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override public void logSimulationTaskEnded(DfsSimulatorTaskResult result, long sTime)
    {
        StringBuilder sb = new StringBuilder();
        
        sb.append("[");
        sb.append(sTime);
        sb.append("] ");
        sb.append(result.toString());
        
        this.messages.add(sb.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override public void logSimulationEnded(long sTime)
    {
        this.messages.add("[" + sTime + "] Simulation ended.");
    }
    
    /**
     * Get all logged messages.
     * 
     * @return messages
     */
    public List<String> getMessages()
    {
        return this.messages;
    }

    /**
     * {@inheritDoc}
     */
    @Override public void logHierarchicalMigrationStart(FsFile file, ModelServerNode server, ServerStorage originalStorage, ServerStorage targetStorage, long sTime)
    {
        StringBuilder sb = new StringBuilder();
        
        sb.append("[");
        sb.append(sTime);
        sb.append("] ");
        sb.append("START MIGRATION: File migration ");
        sb.append(file.getFullPath());
        sb.append(" at server ");
        sb.append(server.toString());
        sb.append(" ");
        sb.append(originalStorage.toString());
        sb.append(" -> ");
        sb.append(targetStorage.toString());
        
        
        this.messages.add(sb.toString());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public void logHierarchicalMigrationFinish(FsFile file, ModelServerNode server, ServerStorage originalStorage, ServerStorage targetStorage, long sTime)
    {
        StringBuilder sb = new StringBuilder();
        
        sb.append("[");
        sb.append(sTime);
        sb.append("] ");
        sb.append("END MIGRATION: File migration ");
        sb.append(file.getFullPath());
        sb.append(" at server ");
        sb.append(server.toString());
        sb.append(" ");
        sb.append(originalStorage.toString());
        sb.append(" -> ");
        sb.append(targetStorage.toString());
        
        this.messages.add(sb.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override public void logHierarchicalUploadMove(FsFile file, ModelServerNode server, ServerStorage targetStorage, long sTime)
    {
        StringBuilder sb = new StringBuilder();
        
        sb.append("[");
        sb.append(sTime);
        sb.append("] ");
        sb.append("UPLOAD MIGRATION: Uploading file ");
        sb.append(file.getFullPath());
        sb.append(" at server ");
        sb.append(server.toString());
        sb.append(" to highest storage -> ");
        sb.append(targetStorage.toString());
        
        this.messages.add(sb.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override public void logReplicationStart(FsFile file, ModelServerNode origin, ModelServerNode target, long sTime)
    {
        StringBuilder sb = new StringBuilder();
        
        sb.append("[");
        sb.append(sTime);
        sb.append("] ");
        sb.append("REPLICATION START: File replication started ");
        sb.append(file.getFullPath());
        sb.append(" from server ");
        sb.append(origin.toString());
        sb.append(" to server -> ");
        sb.append(target.toString());
        
        this.messages.add(sb.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override public void logReplicationFinish(FsFile file, ModelServerNode origin, ModelServerNode target, long sTime)
    {
        StringBuilder sb = new StringBuilder();
        
        sb.append("[");
        sb.append(sTime);
        sb.append("] ");
        sb.append("REPLICATION FINISHED: File replication finished ");
        sb.append(file.getFullPath());
        sb.append(" from server ");
        sb.append(origin.toString());
        sb.append(" to server -> ");
        sb.append(target.toString());
        
        this.messages.add(sb.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override public void logError(String message, long sTime)
    {
        StringBuilder sb = new StringBuilder();
        
        sb.append("[");
        sb.append(sTime);
        sb.append("] ");
        sb.append("ERROR: ");
        sb.append(message);
        
        this.messages.add(sb.toString());
    }
}
