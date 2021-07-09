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
import cz.zcu.kiv.dfs_simulator.model.ByteSize;
import cz.zcu.kiv.dfs_simulator.model.ByteSpeed;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;

/**
 * Result of a single simulation run (for one method).
 */
public class DfsSimulatorSimulationResult
{
    /**
     * Simulation type
     */
    protected final SimulationType type;
    /**
     * Results for simulated tasks
     */
    protected final List<DfsSimulatorTaskResult> results;
    
    /**
     * Cumulated results have been built flag
     */
    protected boolean resultsBuilt = false;
    
    /**
     * Total elapsed time (all tasks)
     */
    protected LongProperty totalElapsedTime;
    /**
     * Total average speed (all tasks)
     */
    protected ByteSpeed totalAverageSpeed;
    /**
     * Total amount of downloaded data (all tasks)
     */
    protected ByteSize totalDownloaded;
    /**
     * Total amount of uploaded data (all tasks)
     */
    protected ByteSize totalUploaded;
    /**
     * All throughput samples (all tasks)
     */
    protected List<Pair<Long, Long>> cumThroughputHistory;
    /**
     * Maximum reached transfer speed (of all tasks)
     */
    protected Pair<Long, Long> maximumSpeed;
    
    /**
     * Results of a single simulation run.
     * 
     * @param type simulation type
     * @param results task results
     */
    public DfsSimulatorSimulationResult(SimulationType type, List<DfsSimulatorTaskResult> results)
    {
        this.type = type;
        this.results = results;
    }

    /**
     * Returns used simulation type.
     * 
     * @return simulation type
     */
    public SimulationType getType()
    {
        return type;
    }

    /**
     * Returns results of simulated tasks.
     * 
     * @return results of simulated tasks
     */
    public List<DfsSimulatorTaskResult> getResults()
    {
        return results;
    }
    
    /**
     * Build cumulative stats - total elapsed time, total average speed,
     * total amount of downloaded data, total amount of uploaded data 
     * and cumulative throughput samples. Will be run only once even
     * if called multiple times.
     */
    public void buildCumulativeStats()
    {
        if(!this.resultsBuilt)
        {            
            this.totalElapsedTime = new SimpleLongProperty(0);
            this.totalAverageSpeed = new ByteSpeed(0);
            this.totalDownloaded = new ByteSize(0);
            this.totalUploaded = new ByteSize(0);
            this.cumThroughputHistory = new ArrayList<>();

            long s_totalElapsedTime = 0;
            long s_totalAverageSpeed;
            long s_totalDownloaded = 0;
            long s_totalUploaded = 0;

            for(DfsSimulatorTaskResult result : this.results)
            {
                s_totalElapsedTime += result.getTotalTime();

                if(result.getObject() != null)
                {
                    if(result.getTask() instanceof GetSimulationTask)
                    {
                        s_totalDownloaded += result.getObject().getSize().bytesProperty().get();
                    }
                    else
                    {
                        s_totalUploaded += result.getObject().getSize().bytesProperty().get();
                    }
                }

                this.cumThroughputHistory.addAll(result.getThroughputSamples());
                
                if(result.getSampler() != null && 
                        (this.maximumSpeed == null || 
                        (result.getSampler().getMaxSample() != null && 
                        result.getSampler().getMaxSample().second > this.maximumSpeed.second)))
                {
                    this.maximumSpeed = result.getSampler().getMaxSample();
                }
            }

            s_totalAverageSpeed = ((s_totalDownloaded + s_totalUploaded) / s_totalElapsedTime) * 1000;

            this.totalElapsedTime.set(s_totalElapsedTime);
            this.totalAverageSpeed.setBps(s_totalAverageSpeed);
            this.totalDownloaded.setBytes(s_totalDownloaded);
            this.totalUploaded.setBytes(s_totalUploaded);
            
            this.resultsBuilt = true;
        }
    }

    /**
     * Get total elapsed time. {@link #buildCumulativeStats()} has to be ran
     * beforehand.
     * 
     * @return total elapsed time
     */
    public LongProperty getTotalElapsedTime()
    {
        return totalElapsedTime;
    }

    /**
     * Get total average speed. {@link #buildCumulativeStats()} has to be ran
     * beforehand.
     * 
     * @return total average speed
     */
    public ByteSpeed getTotalAverageSpeed()
    {
        return totalAverageSpeed;
    }

    /**
     * Get total amount of downloaded data. {@link #buildCumulativeStats()} has 
     * to be ran beforehand.
     * 
     * @return total amount of downloaded data
     */
    public ByteSize getTotalDownloaded()
    {
        return totalDownloaded;
    }

    /**
     * Get total amount of uploaded data. {@link #buildCumulativeStats()} has 
     * to be ran beforehand.
     * 
     * @return total amount of uploaded data
     */
    public ByteSize getTotalUploaded()
    {
        return totalUploaded;
    }

    /**
     * Get cumulative throughput samples (of all tasks).
     * 
     * @return throughput samples
     */
    public List<Pair<Long, Long>> getCumThroughputHistory()
    {
        return cumThroughputHistory;
    }
    
    /**
     * Get maximum reached speed of transfer (from samples of all tasks).
     * 
     * @return maximum reached speed
     */
    public Pair<Long, Long> getMaximumSpeed()
    {
        return this.maximumSpeed;
    }
    
}
