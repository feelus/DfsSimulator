package cz.zcu.kiv.dfs_simulator.helpers;

import cz.zcu.kiv.dfs_simulator.model.ByteSpeed;
import java.util.ArrayList;
import java.util.List;

public class RadialDistanceThroughputHistoryReducer implements SimulationThroughputHistoryReducer
{
    private final static double DIST_TOLERANCE_MOD = 0.002;
    private final static double TIME_TOLERANCE_MOD = 0.004;
    
    private Long maxX;
    private Long maxY;
    
    private boolean cancelled = false;
    
    private double throughputSampleDistance(Pair<Long, Long> a, Pair<Long, Long> b)
    {
        return Math.sqrt( ((a.first - b.first) * (a.first - b.first)) + 
                ( (a.second - b.second) * (a.second - b.second)) );
    }
    
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

    @Override public List<Pair<Long, Long>> reduce(List<Pair<Long, Long>> original, long totalElapsedTime, ByteSpeed totalAverageSpeed)
    {
        double distTolerance = 
                totalElapsedTime * DIST_TOLERANCE_MOD + 
                totalAverageSpeed.bpsProperty().get() * DIST_TOLERANCE_MOD;
        double timeTolerance = totalElapsedTime * TIME_TOLERANCE_MOD;
        
        List<Pair<Long, Long>> filtered = new ArrayList<>(original);
        
        int key = 0;

        while(key < filtered.size() && !this.cancelled)
        {
            int test = key + 1;
            
            while(!this.cancelled && test < (filtered.size() - 1) && 
                    (this.throughputSampleDistance(filtered.get(test), filtered.get(key)) < distTolerance || 
                    Math.abs(filtered.get(test).first - filtered.get(key).first) < timeTolerance))
            {
                filtered.remove(test);
            }
            
            key++;
        }
        
        this.findMaximums(filtered);
        
        return filtered;
    }
    
    @Override public String toString()
    {
        return "Radial distance";
    }

    @Override public Long getMaximumX()
    {
        return this.maxX;
    }

    @Override public Long getMaximumY()
    {
        return this.maxY;
    }

    @Override public void cancel()
    {
        this.cancelled = true;
    }
    
}
