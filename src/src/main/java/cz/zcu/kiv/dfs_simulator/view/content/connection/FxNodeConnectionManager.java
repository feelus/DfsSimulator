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

package cz.zcu.kiv.dfs_simulator.view.content.connection;

import cz.zcu.kiv.dfs_simulator.model.connection.ModelNodeConnection;
import cz.zcu.kiv.dfs_simulator.model.connection.ModelNodeConnectionManager;
import cz.zcu.kiv.dfs_simulator.view.content.FxModelNode;
import java.util.Iterator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Graphical connection manager, provides GUI related methods for working
 * with {@link ModelNodeConnectionManager}.
 */
public class FxNodeConnectionManager
{
    /**
     * Observable node connections
     */
    protected final ObservableList<ModelNodeConnection> connections;
    
    /**
     * Graphical connection manager extension.
     * 
     * @param manager connection manager
     */
    public FxNodeConnectionManager(ModelNodeConnectionManager manager)
    {
        this.connections = FXCollections.observableList(manager.getConnections());
    }
    
    /**
     * Get connections.
     * 
     * @return connections
     */
    public ObservableList<ModelNodeConnection> getConnections()
    {
        return this.connections;
    }
    
    /**
     * Remove connection with given graphical node {@code node}.
     * 
     * @param node neighbour node
     */
    public void removeConnectionWithNode(FxModelNode node)
    {
        Iterator<ModelNodeConnection> ncIterator = this.connections.iterator();
        
        while(ncIterator.hasNext())
        {
            ModelNodeConnection nc = ncIterator.next();
            
            if(nc.getNeighbour().equals(node.getNode()))
            {
                ncIterator.remove();
                
                break;
            }
        }
    }
}
