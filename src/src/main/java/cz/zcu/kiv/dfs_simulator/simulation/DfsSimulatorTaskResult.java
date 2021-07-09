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

import cz.zcu.kiv.dfs_simulator.helpers.Pair;
import cz.zcu.kiv.dfs_simulator.simulation.path.DfsPath;
import cz.zcu.kiv.dfs_simulator.model.ByteSpeed;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FileSystemObject;
import java.util.List;

/**
 * Simulation result of a single task.
 */
public class DfsSimulatorTaskResult
{
    /**
     * Simulated task
     */
    protected final SimulationTask task;
    
    /**
     * Task simulation result
     */
    protected final DfsSimulatorTaskResultState state;
    
    /**
     * Task time
     */
    protected final long totalTime;
    /**
     * Task average speed
     */
    protected final ByteSpeed averageSpeed;
    
    /**
     * Path history - which paths were used to transfer data
     */
    protected final List<DfsPath> pathHistory;
    /**
     * Throughput samples
     */
    protected final SimulationThroughputSampler sampler;
    
    /**
     * Simulation result of a single task.
     * 
     * @param task task
     * @param state result state
     * @param totalTime task time
     * @param averageSpeed task average speed
     * @param pathHistory task path history
     * @param sampler task throughput sampler
     */
    public DfsSimulatorTaskResult(SimulationTask task, DfsSimulatorTaskResultState state, 
            long totalTime, ByteSpeed averageSpeed, List<DfsPath> pathHistory, SimulationThroughputSampler sampler)
    {
        this.task = task;
        this.state = state;
        this.totalTime = totalTime;
        this.averageSpeed = averageSpeed;
        this.pathHistory = pathHistory;
        this.sampler = sampler;
    }

    /**
     * Get simulated task.
     * 
     * @return task
     */
    public SimulationTask getTask()
    {
        return this.task;
    }

    /**
     * Get total time.
     * 
     * @return total time
     */
    public long getTotalTime()
    {
        return this.totalTime;
    }

    /**
     * Get average speed.
     * 
     * @return average speed
     */
    public ByteSpeed getAverageSpeed()
    {
        return this.averageSpeed;
    }

    /**
     * Get task path history.
     * 
     * @return path history
     */
    public List<DfsPath> getPathHistory()
    {
        return this.pathHistory;
    }
    
    /**
     * Get task object.
     * 
     * @return task object
     */
    public FileSystemObject getObject()
    {
        if(this.pathHistory != null && !this.pathHistory.isEmpty())
        {
            return this.pathHistory.get(0).getOrCreateTargetFile();
        }
        
        return null;
    }

    /**
     * Get task result state.
     * 
     * @return result state
     */
    public DfsSimulatorTaskResultState getState()
    {
        return state;
    }
    
    /**
     * Get task throughput samples.
     * 
     * @return throughput samples
     */
    public List<Pair<Long, Long>> getThroughputSamples()
    {
        if(this.sampler != null)
        {
            return this.sampler.getSamples();
        }
        
        return null;
    }
    
    /**
     * Get task throughput sampler.
     * 
     * @return throughput sampler
     */
    public SimulationThroughputSampler getSampler()
    {
        return this.sampler;
    }
    
    /**
     * Textual representation of task result.
     * 
     * @return string representation
     */
    @Override public String toString()
    {
        StringBuilder sb = new StringBuilder();
        
        sb.append("Simulation of task [");
        sb.append(this.task.toString());
        sb.append("] ended with result [");
        sb.append(this.state.toString());
        sb.append("], total time ");
        sb.append(totalTime);
        sb.append(" ms and with average speed ");
        sb.append(this.averageSpeed.getHumanReadableFormat());
        sb.append(".");
        
        return sb.toString();
    }
}
