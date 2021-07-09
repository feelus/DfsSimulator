package cz.zcu.kiv.dfs_simulator.model.connection;

import cz.zcu.kiv.dfs_simulator.persistence.InvalidPersistedStateException;
import cz.zcu.kiv.dfs_simulator.persistence.StatePersistable;
import cz.zcu.kiv.dfs_simulator.persistence.StatePersistableAttribute;
import cz.zcu.kiv.dfs_simulator.persistence.StatePersistableElement;
import cz.zcu.kiv.dfs_simulator.persistence.StatePersistenceLogger;
import cz.zcu.kiv.dfs_simulator.helpers.Helper;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;

public class LineConnectionCharacteristic implements ConnectionCharacteristic
{
    public static final String PERSISTABLE_NAME = "line_connection_characteristic";
    
    public static final String PERSISTABLE_NAME_INTERVAL_MS = "period_interval_ms";
    
    private static final int NUM_POINTS = 21;
    
    private static final double Y_LOWER_BOUND = 0.01;
    private static final double Y_UPPER_BOUND = 1d;
    
    private static final double X_LOWER_BOUND = 0d;
    private static final double X_UPPER_BOUND = 1d;
    
    private final List<ConnectionCharacteristicPoint> dataPoints = new ArrayList<>();
    // default period is 1 minute
    private final LongProperty periodIntervalMs = new SimpleLongProperty(60 * 1000);
    
    public LineConnectionCharacteristic()
    {
        this.constructDataPoints();
    }
    
    private long getPeriodIntervalStep()
    {
        int dpSize = this.dataPoints.size();
        long periodIntervalStep = 0;
        
        if(dpSize > 0)
        {
            if(dpSize > 1)
            {
                periodIntervalStep = (this.periodIntervalMs.get() / (dpSize - 1)) +
                        ((this.periodIntervalMs.get() % (dpSize - 1) == 0) ? 0 : 1);
            }
            // only one data point
            // @TODO should we handle this differently? we can just return
            // the datapoint modifier instead of calculating average value
            else
            {
                periodIntervalStep = this.periodIntervalMs.get();
            }
        }

        if(periodIntervalStep <= 0)
        {
            throw new RuntimeException("Characteristic interval step cannot be less or equal to zero.");
        }
        
        return periodIntervalStep;
    }
    
    private void constructDataPoints()
    {
        double step = (X_UPPER_BOUND - X_LOWER_BOUND) / (NUM_POINTS - 1); 
        
        if(step <= 0)
        {
            throw new RuntimeException("Time interval of " + step + 
                    " with " + NUM_POINTS + " slices is too small.");
        }
        
        this.dataPoints.add(new ConnectionCharacteristicPoint(X_LOWER_BOUND, Y_UPPER_BOUND));
        this.dataPoints.add(new ConnectionCharacteristicPoint(X_UPPER_BOUND, Y_UPPER_BOUND));
        
        for(int i = 1; i < NUM_POINTS - 1; i++)
        {
            this.dataPoints.add(new ConnectionCharacteristicPoint(i * step, Y_UPPER_BOUND));
        }
        
    }
    
    private double getModifierAtTime(long sTime)
    {        
        long periodIntervalStep = this.getPeriodIntervalStep();
        int p1;
        // ending boundary
        if(sTime != this.periodIntervalMs.get())
        {
            p1 = (int) ((sTime % this.periodIntervalMs.get()) / periodIntervalStep);
        }
        else
        {
            p1 = this.dataPoints.size() - 1;
        }
        
        // @TODO this is not entirely accurate
        // check p1 boundary
        if(p1 > (this.dataPoints.size() - 1))
        {
            p1 = this.dataPoints.size() - 1;
        }
        
        // p2 index
        int p2 = p1 + 1;
        
        // check p2 boundary
        if(p2 > (this.dataPoints.size() - 1))
        {
            p2 = 0;
        }
        
        double t = (double) (sTime % periodIntervalStep) / periodIntervalStep;
        
        double p1val = this.dataPoints.get(p1).yProperty().get();
        double p2val = this.dataPoints.get(p2).yProperty().get();
        
        // A(1 - t) + Bt
        return (p1val * (1 - t)) + p2val * t;
    }
    
    public void setDiscretePoints(List<ConnectionCharacteristicPoint> points)
    {
        this.dataPoints.clear();
        this.dataPoints.addAll(points);
    }

    public List<ConnectionCharacteristicPoint> getDiscretePoints()
    {
        return this.dataPoints;
    }

    @Override public double getYLowerBound()
    {
        return Y_LOWER_BOUND;
    }

    @Override public double getYUpperBound()
    {
        return Y_UPPER_BOUND;
    }

    @Override public double getXLowerBound()
    {
        return X_LOWER_BOUND;
    }

    @Override public double getXUpperBound()
    {
        return X_UPPER_BOUND;
    }

    @Override public void setPeriodInterval(long time)
    {
        this.periodIntervalMs.set(time);
    }

    @Override public LongProperty periodIntervalProperty()
    {
        return this.periodIntervalMs;
    }

    @Override public double getAverageBandwidthModifier(long sTime, long intervalLength)
    {
        if(intervalLength == 0)
        {
            return this.getModifierAtTime(sTime);
        }
        
        double sum = 0;
        long mCount = 0;
        long mTime = sTime;
        
        long periodIntervalStep = this.getPeriodIntervalStep();
        while(mTime < (sTime + intervalLength))
        {
            // do an average from two points at time mTime and mTime + slice
            sum += (this.getModifierAtTime(mTime) + this.getModifierAtTime(mTime + periodIntervalStep)) / 2d; 
            
            mTime += periodIntervalStep;
            mCount++;
        }
        
        return sum / mCount;
        
    }

    @Override public String getPersistableName()
    {
        return PERSISTABLE_NAME;
    }

    @Override public List<? extends StatePersistable> getPersistableChildren()
    {
        return this.dataPoints;
    }

    @Override public StatePersistableElement export(StatePersistenceLogger logger)
    {
        StatePersistableElement element = new StatePersistableElement(this.getPersistableName());
        
        element.addAttribute(new StatePersistableAttribute(PERSISTABLE_NAME_INTERVAL_MS, "" + this.periodIntervalMs.get()));
        
        return element;
    }

    @Override public void restoreState(StatePersistableElement state, StatePersistenceLogger logger, Object... args) throws InvalidPersistedStateException
    {
        if(state != null)
        {
            if(!state.getElements().isEmpty())
            {
                StatePersistableAttribute attrInterval = state.getAttribute(PERSISTABLE_NAME_INTERVAL_MS);
                
                if(attrInterval != null && Helper.isLong(attrInterval.getValue()))
                {
                    this.periodIntervalMs.set(Long.parseLong(attrInterval.getValue()));
                    
                    // clear current points
                    this.dataPoints.clear();
                    
                    for(StatePersistableElement elem : state.getElements())
                    {
                        if(elem.getName().equals(ConnectionCharacteristicPoint.PERSISTABLE_NAME))
                        {
                            ConnectionCharacteristicPoint p = new ConnectionCharacteristicPoint(0, 0);
                            p.restoreState(elem, logger, args);
                            
                            this.dataPoints.add(p);
                        }
                        else
                        {
                            throw new InvalidPersistedStateException("Unknown element for LineConnectionCharacteristic: " + elem);
                        }
                    }
                }
                else
                {
                    throw new InvalidPersistedStateException("Required attribute " + PERSISTABLE_NAME_INTERVAL_MS + " is missing or is not a long.");
                }
            }
            else
            {
                throw new InvalidPersistedStateException("Expected atleast one data point: " + state);
            }
            
        }
    }
    
}
