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

package cz.zcu.kiv.dfs_simulator.model;

import cz.zcu.kiv.dfs_simulator.persistence.InvalidPersistedStateException;
import cz.zcu.kiv.dfs_simulator.persistence.StatePersistable;
import cz.zcu.kiv.dfs_simulator.persistence.StatePersistableAttribute;
import cz.zcu.kiv.dfs_simulator.persistence.StatePersistableElement;
import cz.zcu.kiv.dfs_simulator.persistence.StatePersistenceLogger;
import cz.zcu.kiv.dfs_simulator.simulation.SimulationPlan;
import java.util.ArrayList;
import java.util.List;

/**
 * Client node.
 */
public class ModelClientNode extends ModelNode
{
    /**
     * Persistable identificator
     */
    protected static final String PERSISTABLE_NAME = "client_node";
    
    /**
     * Client's simulation plan (requests)
     */
    protected final SimulationPlan simulationPlan = new SimulationPlan();
    
    /**
     * Creates new instance of a client node. Instance can be registered to 
     * {@link ModelNodeRegistry} by setting {@code register} flag.
     * 
     * @param register register flag
     */
    public ModelClientNode(boolean register)
    {
        super(register);
    }
    
    /**
     * Creates new instance of a client node that will be registered to {@link ModelNodeRegistry}.
     */
    public ModelClientNode()
    {
        super(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override public NodeType getType()
    {
        return NodeType.CLIENT;
    }
    
    /**
     * Returns client's requests.
     * 
     * @return client's requests
     */
    public SimulationPlan getSimulationPlan()
    {
        return this.simulationPlan;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public List<StatePersistable> getPersistableChildren()
    {
        return new ArrayList<>();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public StatePersistableElement export(StatePersistenceLogger logger)
    {
        StatePersistableElement element = new StatePersistableElement(this.getPersistableName());
        
        element.addAttribute(new StatePersistableAttribute("id", "" + nodeID.get()));
        
        return element;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public void restoreState(StatePersistableElement state, StatePersistenceLogger logger, Object... args) throws InvalidPersistedStateException
    {
        if(state != null)
        {
            StatePersistableAttribute id = state.getAttribute("id");
            
            if(id != null && !id.getValue().isEmpty())
            {
                try
                {
                    changeId(id.getValue());
                }
                catch (LabelException ex)
                {
                    throw new InvalidPersistedStateException(ex + ": " + state);
                } 
            }
       }
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
    @Override public String toString()
    {
        return "ClientNode " + this.nodeID;
    }
    
}
