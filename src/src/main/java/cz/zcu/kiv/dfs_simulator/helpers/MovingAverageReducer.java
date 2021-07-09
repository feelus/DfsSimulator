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
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link SimulationThroughputHistoryReducer} that reduces
 * multiple samples into one using average of those samples.
 */
public class MovingAverageReducer implements SimulationThroughputHistoryReducer
{ 
    /**
     * Rough-estimate of reduced time periods.
     */
    private static final int TARGET_TIME_PERIODS = 500;
    
    /**
     * Maximum X value from filtered data
     */
    private Long maxX;
    /**
     * Maximum Y value from filtered data
     */
    private Long maxY;
    
    /**
     * Flag signaling that reduction should stop.
     */
    private boolean cancelled = false;
    
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
     * Reduce input samples from {@code original} replacing groups of samples
     * with their average.
     * 
     * @param original measured throughput samples
     * @param totalElapsedTime total simulation time
     * @param totalAverageSpeed total simulation average transfer speed
     * @return reduced list of throughput samples
     */
    @Override public List<Pair<Long, Long>> reduce(List<Pair<Long, Long>> original, long totalElapsedTime, ByteSpeed totalAverageSpeed)
    {
        List<Pair<Long, Long>> reduced = new ArrayList<>();
        
        // check if we can actually reduce
        if(original.size() > TARGET_TIME_PERIODS)
        {
            // calculate number of data points per time periods
            int pT = (int) Math.ceil(original.size() / TARGET_TIME_PERIODS);
            
            for(int i = 0; i < TARGET_TIME_PERIODS && !this.cancelled; i++)
            {
                int xi = i * pT;
                int limit = xi + pT;

                long timeSeriesSum = 0;
                int timeSeriesCount = 0;
                for(int x = xi; x < limit && x < original.size() && !this.cancelled; x++)
                {
                    timeSeriesSum += original.get(x).second;
                    timeSeriesCount++;
                }
                
                if(timeSeriesCount > 0)
                {
                    long timeSeriesTime = original.get(xi + (timeSeriesCount - 1)).first;
                    long timeSeriesAvg = Math.round(timeSeriesSum / timeSeriesCount);
                    
                    reduced.add(new Pair<>(timeSeriesTime, timeSeriesAvg));
                }
            }
            
            this.findMaximums(reduced);
            
            return reduced;
        }
        
        this.findMaximums(original);
        
        return original;
    }
    
    /**
     * Returns textual representation of this reducer purpose
     * 
     * @return textual representation of reducer's purpose
     */
    @Override public String toString()
    {
        return "Moving average";
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
        this.cancelled = true;
    }
}
