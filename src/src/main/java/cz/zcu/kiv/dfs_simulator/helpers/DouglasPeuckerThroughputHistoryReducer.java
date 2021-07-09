/**
 * Reducer based on implementation by Lukasz Wiktor
 * 
 * https://github.com/LukaszWiktor/series-reducer
 */

package cz.zcu.kiv.dfs_simulator.helpers;

import cz.zcu.kiv.dfs_simulator.model.ByteSpeed;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Line segment representation.
 */
class LineSegment
{
    /**
     * Line origin
     */
    private final Pair<Long, Long> s;
    /**
     * Line end
     */
    private final Pair<Long, Long> e;
    
    /**
     * Line segment length
     */
    private final double normLength;
    
    /**
     * Constructs line segment with given origin and end.
     
     * @param s line segment origin
     * @param e line segment end
     */
    public LineSegment(Pair<Long, Long> s, Pair<Long, Long> e)
    {
        this.s = s;
        this.e = e;
        
        this.normLength = Math.sqrt(
                (e.first - s.first) * (e.first - s.first) + 
                        (e.second - s.second) * (e.second - s.second));
    }
    
    /**
     * Get perpendicular distance from {@link P} to this line segment.
     * 
     * @param P point
     * @return perpendicular distance
     */
    public double perpendicularDistance(Pair<Long, Long> P)
    {
        return Math.abs( 
                (s.second - e.second) * P.first - (s.first - e.first) * 
                        P.second + (s.first * e.second) - (e.first * s.second)) / 
                this.normLength;
    }
    
    /**
     * Return line segment as a list.
     * 
     * @return line segment list
     */
    public List<Pair<Long, Long>> asList()
    {
        return Arrays.asList(s, e);
    }
}

/**
 * Implementation of {@link SimulationThroughputHistoryReducer} that uses 
 * Ramer-Douglas-Peucker algorithm for reduction 
 * 
 * https://en.wikipedia.org/wiki/Ramer%E2%80%93Douglas%E2%80%93Peucker_algorithm
 */
public class DouglasPeuckerThroughputHistoryReducer implements SimulationThroughputHistoryReducer
{
    /**
     * Distance threshold modifier (greater number means more samples will be filtered out)
     */
    private final static double DIST_TOLERANCE_MODIFIER = 0.0001d;
    
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
     * Internal reduction method that is called recursively, 
     * see {@link #reduce(java.util.List, long, cz.zcu.kiv.dfs_simulator.model.ByteSpeed)}
     * for it's contract.
     * 
     * @param original measured throughput samples
     * @param totalElapsedTime total simulation time
     * @param totalAverageSpeed total simulation average transfer speed
     * @return reduced list of throughput samples
     */
    private List<Pair<Long, Long>> int_reduce(List<Pair<Long, Long>> original, long totalElapsedTime, ByteSpeed totalAverageSpeed)
    {
        double tolerance = totalAverageSpeed.bpsProperty().get() * DIST_TOLERANCE_MODIFIER;
        double furthestDistance = 0;
        int furthestIndex = 0;
        
        LineSegment line = new LineSegment(original.get(0), original.get(original.size() - 1));
        
        for(int i = 1; i < original.size() - 1 && !this.cancelled; i++)
        {
            double distance = line.perpendicularDistance(original.get(i));
            
            if(distance > furthestDistance)
            {
                furthestDistance = distance;
                furthestIndex = i;
            }
        }
        
        if(furthestDistance > tolerance)
        {
            List<Pair<Long, Long>> r1 = reduce(original.subList(0, furthestIndex + 1), totalElapsedTime, totalAverageSpeed);
            List<Pair<Long, Long>> r2 = reduce(original.subList(furthestIndex, original.size()), totalElapsedTime, totalAverageSpeed);
            
            List<Pair<Long, Long>> comb = new ArrayList<>();
            comb.addAll(r1);
            comb.addAll(r2);
            
            return comb;
        }
        else
        {
            return line.asList();
        }
    }
    
    /**
     * Reduce input samples from {@code original} using Ramer-Douglas-Peucker
     * algorithm.
     * 
     * @param original measured throughput samples
     * @param totalElapsedTime total simulation time
     * @param totalAverageSpeed total simulation average transfer speed
     * @return reduced list of throughput samples
     */
    @Override public List<Pair<Long, Long>> reduce(List<Pair<Long, Long>> original, long totalElapsedTime, ByteSpeed totalAverageSpeed)
    {
        List<Pair<Long, Long>> reduced = this.int_reduce(original, totalElapsedTime, totalAverageSpeed);
        this.findMaximums(reduced);
        
        return reduced;        
    }
    
    /**
     * Returns textual representation of this reducer purpose
     * 
     * @return textual representation of reducer's purpose
     */
    @Override public String toString()
    {
        return "Douglas-Peucker";
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
