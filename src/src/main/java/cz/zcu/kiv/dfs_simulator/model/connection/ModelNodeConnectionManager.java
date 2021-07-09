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

package cz.zcu.kiv.dfs_simulator.model.connection;

import cz.zcu.kiv.dfs_simulator.model.ModelNode;
import cz.zcu.kiv.dfs_simulator.model.ModelServerNode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * A manager of {@link ModelNode} connections.
 */
public class ModelNodeConnectionManager
{
    /**
     * Managed node
     */
    protected final ModelNode node;
    /**
     * List of connections
     */
    protected final List<ModelNodeConnection> connections = new ArrayList<>();
    
    /**
     * Constructs a manager of connections for {@code node}.
     * 
     * @param node managed node
     */
    public ModelNodeConnectionManager(ModelNode node)
    {
        this.node = node;
    }
    
    /**
     * Returns list of node connections.
     * 
     * @return list fo connections
     */
    public List<ModelNodeConnection> getConnections()
    {
        return this.connections;
    }
    
    /**
     * Add new connection.
     * 
     * @param connection node connection
     * @return returns true if connection didn't previously exists, false otherwise
     */
    public boolean addConnection(ModelNodeConnection connection)
    {
        if(!this.connectionExists(connection.getNeighbour()))
        {
            this.connections.add(connection);
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Removes an existing connection with {@code neighbour}.
     * 
     * @param neighbour neighbour node
     */
    public void removeConnectionWithNode(ModelNode neighbour)
    {
        Iterator<ModelNodeConnection> ncIterator = this.connections.iterator();
        
        while(ncIterator.hasNext())
        {
            ModelNodeConnection nc = ncIterator.next();
            
            if(nc.getNeighbour().equals(neighbour))
            {
                ncIterator.remove();
                
                break;
            }
        }
    }
    
    /**
     * Checks if connection with {@code neighbour} exists.
     * 
     * @param neighbour neighbour node
     * @return true if exists, false otherwise
     */
    public boolean connectionExists(ModelNode neighbour)
    {
        return this.connections.stream().anyMatch(n -> n.getNeighbour().equals(neighbour));
    }
    
    /**
     * Returns an instance of {@code ModelNodeConnection} with {@code neighbour}.
     * 
     * @param neighbour neighbour node
     * @return connection with neighbour
     */
    public ModelNodeConnection getNeighbourConnection(ModelNode neighbour)
    {
        Optional<ModelNodeConnection> attr =  this.connections.stream().filter(x -> x.getNeighbour().equals(neighbour)).findFirst();
        
        if(attr != null && attr.isPresent())
        {
            return attr.get();
        }
        
        return null;
    }
    
    /**
     * Returns a list of all reachable nodes {@link ModelServerNode} from this
     * node.
     * 
     * @return list of reachable server nodes
     */
    public List<ModelServerNode> getReachableServers()
    {
        List<ModelServerNode> neighbours = new ArrayList<>();
        
        this.int_getReachableServers(neighbours, new ArrayList<>());
        
        return neighbours;
    }
    
    /**
     * Returns a list of all direct neighbours of class {@link ModelServerNode}.
     * 
     * @return list of server neighbours
     */
    public List<ModelNodeConnection> getDirectServerConnections()
    {
        List<ModelNodeConnection> neighbours = new ArrayList<>();
        
        this.connections.stream()
                .filter((conn) -> (conn.getNeighbour() instanceof ModelServerNode))
                .forEachOrdered((conn) ->
        {
            neighbours.add(conn);
        });
        
        return neighbours;
    }
    
    /**
     * Recursively find all reachble nodes of class {@link ModelServerNode}.
     * 
     * @param neighbours list that all neighbours will be added to
     * @param discovered list of already discovered neighbours
     */
    protected void int_getReachableServers(List<ModelServerNode> neighbours, List<ModelNodeConnection> discovered)
    {
        for(ModelNodeConnection conn : this.connections)
        {
            if(discovered.contains(conn))
            {
                continue;
            }
            
            discovered.add(conn);
            
            if(conn.getNeighbour() instanceof ModelServerNode)
            {
                ModelServerNode serverNode = (ModelServerNode) conn.getNeighbour();
                
                if(!neighbours.contains(serverNode))
                {
                    neighbours.add(serverNode);
                    
                    serverNode.getConnectionManager().int_getReachableServers(neighbours, discovered);
                }
            }
        }
    }
}
