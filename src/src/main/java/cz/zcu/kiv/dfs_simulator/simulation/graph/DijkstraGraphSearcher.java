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

package cz.zcu.kiv.dfs_simulator.simulation.graph;

import cz.zcu.kiv.dfs_simulator.model.ByteSpeed;
import cz.zcu.kiv.dfs_simulator.simulation.graph.metric.GraphMetric;
import cz.zcu.kiv.dfs_simulator.model.ModelNode;
import cz.zcu.kiv.dfs_simulator.model.ModelServerNode;
import cz.zcu.kiv.dfs_simulator.model.connection.ModelNodeConnection;
import cz.zcu.kiv.dfs_simulator.simulation.GetSimulationTask;
import cz.zcu.kiv.dfs_simulator.simulation.SimulationTask;
import cz.zcu.kiv.dfs_simulator.simulation.SimulationType;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FileSystemObject;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.NotMountedException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of modified Dijkstra algorithm.
 */
public class DijkstraGraphSearcher implements GraphSearcher
{
    /**
     * Metric used to evaluate edges
     */
    private final GraphMetric metric;
    
    /**
     * Settled nodes
     */
    private Set<ModelNode> settled;
    /**
     * Unsettled nodes
     */
    private Set<ModelNode> unsettled;
    
    /**
     * Predecessors - used to build path
     */
    private Map<ModelNode, ModelNodeConnection> predecessors;
    /**
     * Distance (value) to node (key)
     */
    private Map<ModelNode, Long> distance;
    
    /**
     * Modified Dijkstra's algorithm for selecting paths.
     * 
     * @param metric graph metric used to evaluate edges
     */
    public DijkstraGraphSearcher(GraphMetric metric)
    {
        this.metric = metric;
    }
    
    /**
     * Build (calculate) paths from {@code origin} to all other nodes.
     * 
     * @param origin origin node
     * @param task current simulation task
     * @param diskBandwidth maximum possible disk bandwidth for this task
     * @param sTime simulation time
     */
    private void buildPaths(ModelNode origin, SimulationTask task, ByteSpeed diskBandwidth, long sTime)
    {
        this.settled = new HashSet<>();
        this.unsettled = new HashSet<>();
        
        this.distance = new HashMap<>();
        this.predecessors = new HashMap<>();
        
        this.distance.put(origin, this.metric.getBestMetricValue());
        this.unsettled.add(origin);
        
        Comparator<Long> comparator = this.metric.getComparator();
        
        while(!this.unsettled.isEmpty())
        {
            ModelNode n = this.getMin(this.unsettled, comparator);
            this.settled.add(n);
            
            this.unsettled.remove(n);
            this.findMinDistances(n, comparator, task, diskBandwidth, sTime);
        }
    }
    
    /**
     * Find min distance (best metric) to {@code n}.
     * 
     * @param n node
     * @param comparator distance comparator
     * @param task current simulation task
     * @param diskBandwidth maximum possible disk bandwidth for this task
     * @param sTime simulation time
     */
    private void findMinDistances(ModelNode n, Comparator<Long> comparator, SimulationTask task, ByteSpeed diskBandwidth, long sTime)
    {
        List<ModelNodeConnection> adjacent = this.getNeighbours(n);
        
        for(ModelNodeConnection adj : adjacent)
        {
            long dist = this.metric.getCombinedEdgeWeight(this.getShortestDistance(n), this.getDistance(adj, task, diskBandwidth, sTime), adj);
            
            if(comparator.compare(this.getShortestDistance(adj.getNeighbour()), dist) > 0)
            {
                this.distance.put(adj.getNeighbour(), dist);
                this.predecessors.put(adj.getNeighbour(), adj);
                this.unsettled.add(adj.getNeighbour());
            }
        }
    }
    
    /**
     * Get edge weight of {@code conn}.
     * 
     * @param conn edge (node connection)
     * @param task current simulation task
     * @param diskBandwidth maximum possible disk bandwidth for this task
     * @param sTime simulation time
     * @return edge weight
     */
    private long getDistance(ModelNodeConnection conn, SimulationTask task, ByteSpeed diskBandwidth, long sTime)
    {
        if(conn != null)
        {
            return this.metric.getEdgeWeight(conn, task.getFile().getSize(), diskBandwidth, sTime);
        }
        
        throw new RuntimeException("Unable to get path distance");
    }
    
    /**
     * Discover all neighbours of node {@code n}.
     * 
     * @param n node
     * @return neighbours
     */
    private List<ModelNodeConnection> getNeighbours(ModelNode n)
    {
        List<ModelNodeConnection> neighbourConnections = n.getConnectionManager().getDirectServerConnections();
        List<ModelNodeConnection> filtered = new ArrayList<>();
        
        for(ModelNodeConnection conn : neighbourConnections)
        {
            if(!this.settled.contains(conn.getNeighbour()))
            {
                filtered.add(conn);
            }
        }
        
        return filtered;
    }
    
    /**
     * Get distance to node {@code n}.
     * 
     * @param n node
     * @return shortest distance
     */
    private Long getShortestDistance(ModelNode n)
    {
        Long d = this.distance.get(n);
        
        if(d != null)
        {
            return d;
        }
        
        return this.metric.getWorstMetricValue();
    }
    
    /**
     * Select node with best edge-weight.
     * 
     * @param nodeSet set of nodes
     * @param comparator edge-weight comparator
     * @return node with best edge-weight
     */
    private ModelNode getMin(Set<ModelNode> nodeSet, Comparator<Long> comparator)
    {
        ModelNode min = null;
        
        for(ModelNode n : nodeSet)
        {
            if(min == null)
            {
                min = n;
            }
            else if( comparator.compare(this.getShortestDistance(n), this.getShortestDistance(min)) < 0)
            {
                min = n;
            }
        }
        
        return min;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public Long findPath(ModelNode origin, ModelServerNode target, 
            SimulationTask task, long sTime, List<ModelNodeConnection> path, SimulationType simType) throws NotMountedException
    {
        FileSystemObject targetObj = (task instanceof GetSimulationTask) ? task.getFile() : task.getFile().getParent();
        
        ByteSpeed maximumStorageThroughput = this.metric.getPossibleDiskBandwidth(target, targetObj, simType);
        
        if(maximumStorageThroughput == null || maximumStorageThroughput.bpsProperty().get() <= 0)
        {
            throw new NotMountedException("Object " + targetObj.toString() + " is not mounted");
        }
        
        this.buildPaths(origin, task, maximumStorageThroughput, sTime);
        
        ModelNode s = target;
        
        if(!this.predecessors.containsKey(s))
        {
            return null;
        }
        
        ModelNodeConnection conn;
        while((conn = this.predecessors.get(s)) != null)
        {
            path.add(conn);
            s = conn.getOrigin();
        }
        
        Collections.reverse(path);
        
        return this.distance.get(target);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public GraphMetric getMetric()
    {
        return this.metric;
    }
    
}
