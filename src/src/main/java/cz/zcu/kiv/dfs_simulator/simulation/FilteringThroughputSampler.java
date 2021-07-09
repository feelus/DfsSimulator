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
import cz.zcu.kiv.dfs_simulator.model.ByteSpeed;
import java.util.ArrayList;
import java.util.List;

/**
 * Filters throughput samples - does not record subsequent samples with same
 * throughput value.
 */
public class FilteringThroughputSampler implements SimulationThroughputSampler
{
    /**
     * Recorded samples
     */
    private final List<Pair<Long, Long>> samples = new ArrayList<>();
    /**
     * Sample with maximum throughput value
     */
    private Pair<Long, Long> maxSample = new Pair<>(0L, 0L);
    
    /**
     * Last recorded sample
     */
    private long prevSampleValue = -1;
    
    /**
     * {@inheritDoc}
     */
    @Override public void recordSample(long time, ByteSpeed throughput, boolean lastSample)
    {
        if(this.prevSampleValue != throughput.bpsProperty().get() || lastSample)
        {
            this.prevSampleValue = throughput.bpsProperty().get();
            samples.add(new Pair(time, this.prevSampleValue));
            
            if(this.maxSample == null || this.prevSampleValue > this.maxSample.second)
            {
                this.maxSample = new Pair(time, this.prevSampleValue);
            }
        }
    }
        
    /**
     * {@inheritDoc}
     */
    @Override public List<Pair<Long, Long>> getSamples()
    {
        return this.samples;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public Pair<Long, Long> getMaxSample()
    {
        return this.maxSample;
    }
    
}
