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
import java.util.List;

/**
 * Simulation throughput sampler.
 */
public interface SimulationThroughputSampler
{
    /**
     * Record (process) throughput sample.
     * 
     * @param time simulation time
     * @param throughput throughput
     * @param lastSample if this sample is the last sample
     */
    public void recordSample(long time, ByteSpeed throughput, boolean lastSample);
    
    /**
     * Get recorded samples.
     * 
     * @return recorded samples
     */
    public List<Pair<Long, Long>> getSamples();
    /**
     * Get sample with highest throughput.
     * 
     * @return sample with highest throughput
     */
    public Pair<Long, Long> getMaxSample();
}
