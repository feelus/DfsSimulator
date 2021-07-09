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

package cz.zcu.kiv.dfs_simulator.view.content.events;

import cz.zcu.kiv.dfs_simulator.view.simulation.FxSimulatorTaskResultSet;
import cz.zcu.kiv.dfs_simulator.view.simulation.SimulationResultsWindow;
import java.util.List;
import javafx.event.Event;
import static javafx.event.Event.ANY;
import javafx.event.EventTarget;
import javafx.event.EventType;

/**
 * Events associated with simulation.
 */
public class FxNodeSimulationEvent extends Event
{
    /**
     * Begin simulation event
     */
    public static final EventType<FxNodeSimulationEvent> SIMULATION_VISUALISATION_ON_REQUESTED = new EventType(ANY, "NODE_SIMULATION_OPEN_REQUESTED");
    /**
     * Stop simulation event
     */
    public static final EventType<FxNodeSimulationEvent> SIMULATION_VISUALISATION_OFF_REQUESTED = new EventType(ANY, "NODE_SIMULATION_CLOSE_REQUESTED");
    /**
     * Node simulation finished successfully event
     */
    public static final EventType<FxNodeSimulationEvent> NODE_SIMULATION_FINISHED_SUCCESS = new EventType(ANY, "NODE_SIMULATION_FINISHED_SUCCESS");
    /**
     * Node simulation finished with an error
     */
    public static final EventType<FxNodeSimulationEvent> NODE_SIMULATION_FINISHED_FAILURE = new EventType(ANY, "NODE_SIMULATION_FINISHED_FAILURE");
    /**
     * Disable controls request upon simulation start
     */
    public static final EventType<FxNodeSimulationEvent> NODE_SIMULATION_DISABLE_CONTROLS_REQUEST = new EventType(ANY, "NODE_SIMULATION_DISABLE_CONTROLS_REQUEST");
    /**
     * Enable controls request after simulation finished
     */
    public static final EventType<FxNodeSimulationEvent> NODE_SIMULATION_ENABLE_CONTROLS_REQUEST = new EventType(ANY, "NODE_SIMULATION_ENABLE_CONTROLS_REQUEST");
    
    /**
     * Simulation result set (only used with {@link #NODE_SIMULATION_FINISHED_SUCCESS})
     */
    protected List<FxSimulatorTaskResultSet> resultSet;
    /**
     * Displayed dialog
     */
    protected SimulationResultsWindow finishedDialog;
    
    public FxNodeSimulationEvent(Object source, EventTarget target, EventType<? extends Event> eventType, List<FxSimulatorTaskResultSet> resultSet)
    {
        super(source, target, eventType);
        
        this.resultSet = resultSet;
    }
    
    public FxNodeSimulationEvent(Object source, EventTarget target, EventType<? extends Event> eventType)
    {
        this(source, target, eventType, null);
    }

    public FxNodeSimulationEvent(EventType<? extends Event> eventType, List<FxSimulatorTaskResultSet> resultSet)
    {
        super(eventType);
        
        this.resultSet = resultSet;
    }
    
    public FxNodeSimulationEvent(EventType<? extends Event> eventType)
    {
        this(eventType, (List<FxSimulatorTaskResultSet>) null);
    }
    
    public FxNodeSimulationEvent(EventType<?extends Event> eventType, SimulationResultsWindow dialog)
    {
        super(eventType);
        
        this.finishedDialog = dialog;
    }
    
    /**
     * Get result set (null for some events).
     * 
     * @return result set
     */
    public List<FxSimulatorTaskResultSet> getResultSet()
    {
        return this.resultSet;
    }

    /**
     * Get dialog after simulation finished (null for some events).
     * 
     * @return finished dialog
     */
    public SimulationResultsWindow getFinishedDialog()
    {
        return finishedDialog;
    }

}
