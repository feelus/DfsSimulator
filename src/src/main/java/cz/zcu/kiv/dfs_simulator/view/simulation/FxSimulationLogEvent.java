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

package cz.zcu.kiv.dfs_simulator.view.simulation;

import cz.zcu.kiv.dfs_simulator.simulation.SimulationTaskType;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.scene.image.ImageView;

/**
 * Logged simulation event.
 */
public class FxSimulationLogEvent
{
    /**
     * Parent event
     */
    protected final FxSimulationLogEvent parent;
    /**
     * Associated simulation task type
     */
    protected final SimulationTaskType type;
    
    /**
     * Event message
     */
    protected String message;
    /**
     * Event status
     */
    protected FxSimulationLogEventStatus status = FxSimulationLogEventStatus.RUNNING;
    /**
     * Event icon
     */
    protected ImageView imageView;
    
    /**
     * Event percent done
     */
    protected IntegerProperty percent = new SimpleIntegerProperty(0);
    /**
     * Event running flag
     */
    protected BooleanProperty running = new SimpleBooleanProperty(true);
    
    /**
     * Event total run time
     */
    protected LongProperty runTime = new SimpleLongProperty(0L);
    
    /**
     * Simulation event.
     * 
     * @param type task type
     * @param message event message
     * @param parent event parent
     * @param status event status
     * @param imageView event icon
     */
    public FxSimulationLogEvent(SimulationTaskType type, String message, FxSimulationLogEvent parent, FxSimulationLogEventStatus status, ImageView imageView)
    {
        this.type = type;
        this.message = message;
        this.parent = parent;
        this.status = status;
        this.imageView = imageView;
        
        if(status != FxSimulationLogEventStatus.RUNNING)
        {
            this.running.set(false);
        }
    }
        
    /**
     * Simulation event.
     * 
     * @param type task type
     * @param message event message
     * @param parent event parent
     * @param imageView event icon
     */
    public FxSimulationLogEvent(SimulationTaskType type, String message, FxSimulationLogEvent parent, ImageView imageView)
    {
        this(type, message, parent, FxSimulationLogEventStatus.RUNNING, imageView);
    }
        
    /**
     * Simulation event.
     * 
     * @param type task type
     * @param message event message
     * @param parent event parent
     */
    public FxSimulationLogEvent(SimulationTaskType type, String message, FxSimulationLogEvent parent)
    {
        this(type, message, parent, null);
    }
        
    /**
     * Simulation event.
     * 
     * @param type task type
     * @param message event message
     * @param imageView event icon
     */
    public FxSimulationLogEvent(SimulationTaskType type, String message, ImageView imageView)
    {
        this(type, message, null, imageView);
    }
        
    /**
     * Simulation event.
     * 
     * @param type task type
     * @param message event message
     */
    public FxSimulationLogEvent(SimulationTaskType type, String message)
    {
        this(type, message, (ImageView) null);
    }
    
    /**
     * Set event status.
     * 
     * @param status status
     */
    public void setStatus(FxSimulationLogEventStatus status)
    {
        if(status != FxSimulationLogEventStatus.RUNNING)
        {
            this.running.set(false);
        }
        else
        {
            this.running.set(true);
        }
        
        this.status = status;
    }
    
    /**
     * Set event message.
     * 
     * @param message message
     */
    public void setMessage(String message)
    {
        this.message = message;
    }
    
    /**
     * Get event message.
     * 
     * @return event message
     */
    public String getMessage()
    {
        return this.message;
    }
    
    /**
     * Get event parent.
     * 
     * @return event parent
     */
    public FxSimulationLogEvent getParent()
    {
        return this.parent;
    }
    
    /**
     * Get event status.
     * 
     * @return event status
     */
    public FxSimulationLogEventStatus getStatus()
    {
        return this.status;
    }

    /**
     * Get event icon.
     * 
     * @return event icon
     */
    public ImageView getImageView()
    {
        return imageView;
    }
    
    /**
     * Set event percent done.
     * 
     * @param percent percent
     */
    public void setPercent(int percent)
    {
        this.percent.set(percent);
    }
    
    /**
     * Get event percent done.
     * 
     * @return percent
     */
    public IntegerProperty percentProperty()
    {
        return this.percent;
    }
    
    /**
     * Event running flag.
     * 
     * @return running flag
     */
    public BooleanProperty runningProperty()
    {
        return this.running;
    }

    /**
     * Event runtime (elapsed time).
     * 
     * @return runtime
     */
    public LongProperty runTimeProperty()
    {
        return runTime;
    }
    
    /**
     * Set event runtime (elapsed time).
     * 
     * @param runTime runtime
     */
    public void setRunTime(long runTime)
    {
        this.runTime.set(runTime);
    }

    /**
     * Get simulation type.
     * 
     * @return type
     */
    public SimulationTaskType getType()
    {
        return type;
    }
    
    
    /**
     * Convert event to string - represented as event message.
     * 
     * @return string representation
     */
    @Override public String toString()
    {
        return this.message;
    }
}
