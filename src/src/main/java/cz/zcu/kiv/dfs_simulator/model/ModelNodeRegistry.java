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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global registry of all instances of {@link ModelNode}.
 */
public class ModelNodeRegistry
{
    /**
     * Mapping of node id to {@link ModelNode} instance
     */
    private static final Map<String, ModelNode> NODE_REGISTRY = new HashMap<>();
    /**
     * Node id counter
     */
    private static int nodeId = 1;
    
    /**
     * Register {@code node} with automatically generated node id.
     * 
     * @param node {@link ModelNode} instance to be registered
     */
    public static void createNodeRegistration(ModelNode node)
    {
        for(;;)
        {
            node.nodeID.set(getNextNodeId() + "");
            
            try
            {
                registerNode(node);
                break;
            }
            catch(LabelException ex) {}
        }
    }
    
    /**
     * Register {@code node} with set node id.
     * 
     * @param node {@link ModelNode} instnace to be registered
     * @throws LabelException thrown when an instance with set id already exists
     */
    public static void registerNode(ModelNode node) throws LabelException
    {
        if(!NODE_REGISTRY.containsKey(node.getNodeID()))
        {
            NODE_REGISTRY.put(node.getNodeID(), node);
        }
        else
        {
            throw new LabelException("Attempting to register duplicate node with id " + node.getNodeID() + ".");
        }
    }
    
    /**
     * Find bode by identifier.
     * 
     * @param nodeID node identifier
     * @return if found, and instance of {@link ModelNode}, else null
     */
    public static ModelNode getNode(String nodeID)
    {
        return NODE_REGISTRY.get(nodeID);
    }
    
    /**
     * Rename an already registered node. If successful, {@code node} will have
     * {@code newId} set as it's identifier.
     * 
     * @param newId new node identifier
     * @param node registered {@link ModelNode} instance
     * @throws LabelException thrown when an instance with {@code newId} already exists
     */
    public static void renameNode(String newId, ModelNode node) throws LabelException
    {
        if(newId.equals(node.getNodeID()))
        {
            return;
        }
        
        if(!NODE_REGISTRY.containsKey(newId))
        {
            NODE_REGISTRY.remove(node.getNodeID());
            NODE_REGISTRY.put(newId, node);
            
            node.nodeID.set(newId);
        }
        else
        {
            throw new LabelException("Attempting to register duplicate node with id " + newId + ".");
        }
    }
    
    /**
     * Get all nodes that have type of {@link NodeType#SERVER}.
     * 
     * @return list of server nodes
     */
    public static List<ModelServerNode> getServerNodes()
    {
        return NODE_REGISTRY.values().stream().filter(
                n -> n.getType() == NodeType.SERVER).map(n -> (ModelServerNode) n).collect(Collectors.toList());
    }
    
    /**
     * Clear registry.
     */
    public static void purge()
    {
        NODE_REGISTRY.clear();
    }
    
    /**
     * Returns next node id. An instance could have already been MANUALLY
     * registered with returned id.
     * 
     * @return node id
     */
    public static int getNextNodeId()
    {
        return nodeId++;
    }
}
