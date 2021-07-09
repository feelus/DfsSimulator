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

import cz.zcu.kiv.dfs_simulator.persistence.InvalidPersistedStateException;
import cz.zcu.kiv.dfs_simulator.persistence.StatePersistable;
import cz.zcu.kiv.dfs_simulator.persistence.StatePersistableElement;
import cz.zcu.kiv.dfs_simulator.persistence.StatePersistenceLogger;
import java.util.ArrayList;
import java.util.List;

/**
 * Simulation plan (client requests - tasks).
 */
public class SimulationPlan implements StatePersistable
{
    /**
     * Persistable identifier
     */
    protected static final String PERSISTABLE_NAME = "simulation_plan";
    
    /**
     * Client requests - tasks
     */
    protected final List<SimulationTask> tasks = new ArrayList<>();
    
    /**
     * Get client tasks.
     * 
     * @return tasks
     */
    public List<SimulationTask> getTasks()
    {
        return this.tasks;
    }

    /**
     * {@inheritDoc}
     */
    @Override public String getPersistableName()
    {
        return PERSISTABLE_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<StatePersistable> getPersistableChildren()
    {
        List<StatePersistable> l = new ArrayList<>();
        l.addAll(this.tasks);
        
        return l;
    }

    /**
     * {@inheritDoc}
     */
    @Override public StatePersistableElement export(StatePersistenceLogger logger)
    {
        return new StatePersistableElement(this.getPersistableName());
    }

    /**
     * {@inheritDoc}
     */
    @Override public void restoreState(StatePersistableElement state, StatePersistenceLogger logger, Object... args) throws InvalidPersistedStateException
    {
        if(state != null)
        {
            for(StatePersistableElement childElem : state.getElements())
            {
                if(childElem.getName().equals(GetSimulationTask.PERSISTABLE_NAME))
                {
                    GetSimulationTask getTask = new GetSimulationTask(null);
                    getTask.restoreState(childElem, logger);
                    
                    this.tasks.add(getTask);
                }
                else if(childElem.getName().equals(PutSimulationTask.PERSISTABLE_NAME))
                {
                    PutSimulationTask putTask = new PutSimulationTask(null);
                    putTask.restoreState(childElem, logger);
                    
                    this.tasks.add(putTask);
                }
            }
        }
    }
}
