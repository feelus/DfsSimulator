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
 * Implementation of {@link SimulationThroughputHistoryReducer} that actually 
 * doesn't do any reducing, method {@link reduce} returns original data.
 */
public class AllInclusiveThroughputHistoryReducer implements SimulationThroughputHistoryReducer
{
    /**
     * Maximum X value from filtered data
     */
    private Long maxX;
    /**
     * Maximum Y value from filtered data
     */
    private Long maxY;
    
    /**
     * Find maximum X and Y values from {@link data}.
     * 
     * @param data input data
     */
    private void findMaximums(List<Pair<Long, Long>> data)
    {
        data.stream().forEach(p -> {
            if(this.maxX == null || p.first > this.maxX)
            {
                this.maxX = p.first;
            }

            if(this.maxY == null || p.second > this.maxY)
            {
                this.maxY = p.second;
            }
        });
    }
    
    /**
     * Returns textual representation of this reducer purpose
     * 
     * @return textual representation of reducer's purpose
     */
    @Override public String toString()
    {
        return "No filter (CPU Intensive)";
    }

    /**
     * Returns unmodified input data.
     * 
     * @param original measured throughput samples
     * @param totalElapsedTime total simulation time
     * @param totalAverageSpeed total simulation average transfer speed
     * @return unmodified {@code original}
     */
    @Override public List<Pair<Long, Long>> reduce(List<Pair<Long, Long>> original, long totalElapsedTime, ByteSpeed totalAverageSpeed)
    {
        this.findMaximums(original);
        
        return original;
    }

    /**
     * Get maximum X (time) value from reduced throughput samples.
     * 
     * @return maximum X value
     */
    @Override public Long getMaximumX()
    {
        return this.maxX;
    }

    /**
     * Get maximum Y (throughput) value from reduced throughput samples.
     * 
     * @return maximum Y value
     */
    @Override public Long getMaximumY()
    {
        return this.maxY;
    }

    /**
     * Signal reducer that it should stop any active reducing operations.
     */
    @Override public void cancel()
    {
        
    }
    
}
