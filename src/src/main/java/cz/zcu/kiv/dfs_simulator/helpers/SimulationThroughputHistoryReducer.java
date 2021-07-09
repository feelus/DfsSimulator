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

package cz.zcu.kiv.dfs_simulator.helpers;

import cz.zcu.kiv.dfs_simulator.model.ByteSpeed;
import java.util.List;

/**
 * An interface for history throughput samples reducers.
 */
public interface SimulationThroughputHistoryReducer
{
    /**
     * Reduce input samples.
     * 
     * @param original measured throughput samples
     * @param totalElapsedTime total simulation time
     * @param totalAverageSpeed total simulation average transfer speed
     * @return reduced list of throughput samples
     */
    public List<Pair<Long, Long>> reduce(List<Pair<Long, Long>> original, long totalElapsedTime, ByteSpeed totalAverageSpeed);
    
    /**
     * Signal reducer that it should stop any active reducing operations.
     */
    public void cancel();
    
    /**
     * Get maximum Y (throughput) value from reduced throughput samples.
     * 
     * @return maximum Y value
     */
    public Long getMaximumX();
    
    /**
     * Get maximum Y (throughput) value from reduced throughput samples.
     * 
     * @return maximum Y value
     */
    public Long getMaximumY();
}
