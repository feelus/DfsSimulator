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

import cz.zcu.kiv.dfs_simulator.persistence.StatePersistable;
import cz.zcu.kiv.dfs_simulator.model.connection.ModelNodeConnectionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * A node.
 */
abstract public class ModelNode implements StatePersistable
{
    /**
     * Node identificator
     */
    protected final StringProperty nodeID = new SimpleStringProperty();
    /**
     * Manager of node connections
     */
    protected final ModelNodeConnectionManager connectionManager;
    
    /**
     * Creates new instance. Instance can be registered to {@link ModelNodeRegistry}
     * by setting {@code register} flag.
     * 
     * @param register register flag
     */
    public ModelNode(boolean register)
    {
        this.connectionManager = new ModelNodeConnectionManager(this);
        
        if(register)
        {
            this.regNode();
        }
    }
    
    /**
     * Creates new instance that will be registered to {@link ModelNodeRegistry}.
     */
    public ModelNode()
    {
        this(true);
    }
    
    private void regNode()
    {
        // will generate ID for us
        ModelNodeRegistry.createNodeRegistration(this);
    }
    
    public String getNodeID()
    {
        return this.nodeID.get();
    }
    
    public StringProperty nodeIdProperty()
    {
        return this.nodeID;
    }
    
    public ModelNodeConnectionManager getConnectionManager()
    {
        return this.connectionManager;
    }
    
    public void changeId(String newId) throws LabelException
    {
        ModelNodeRegistry.renameNode(newId, this);
    }
    
    /**
     * Returns {@link NodeType} type.
     * 
     * @return node type
     */
    abstract public NodeType getType();
    
    /**
     * Textual representation of this node.
     * 
     * @return textual representation
     */
    @Override abstract public String toString();
}
