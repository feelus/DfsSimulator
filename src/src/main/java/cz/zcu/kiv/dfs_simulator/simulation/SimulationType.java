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

import cz.zcu.kiv.dfs_simulator.simulation.graph.metric.DistanceMetric;
import cz.zcu.kiv.dfs_simulator.simulation.graph.metric.GraphMetric;
import cz.zcu.kiv.dfs_simulator.simulation.graph.metric.HierarchicalThroughputMetric;
import cz.zcu.kiv.dfs_simulator.simulation.graph.metric.LinkBwLatencyMetric;
import cz.zcu.kiv.dfs_simulator.simulation.graph.metric.LinkBwMetric;
import cz.zcu.kiv.dfs_simulator.simulation.graph.metric.PathThroughputLatencyMetric;
import cz.zcu.kiv.dfs_simulator.simulation.graph.metric.PathThroughputMetric;
import cz.zcu.kiv.dfs_simulator.simulation.hierarchy.HierarchicalAccessMonitor;
import cz.zcu.kiv.dfs_simulator.simulation.hierarchy.HierarchicalPlanner;
import cz.zcu.kiv.dfs_simulator.simulation.hierarchy.LRUCascadeMigrationPlanner;

/**
 * Defines simulation type (method) - defines dynamic routing, hierarchical
 * storage management and graph metric used to select paths.
 */
public enum SimulationType
{
    /**
     * Shortest path
     */
    SHORTEST("Shortest", new DistanceMetric()),
    
    /**
     * Path with highest link bandwidth
     */
    LINK_BANDWIDTH("Link BW", new LinkBwMetric()),
    
    /**
     * Path with highest link bandwidth and lowest latency
     */
    LINK_BANDWIDTH_LATENCY("Link BW (min. latency)", new LinkBwLatencyMetric()),
    
    /**
     * Path with maximum throughput (link and storage)
     */
    PATH_THROUGHPUT("Max. throughput", new PathThroughputMetric()),
    
    /**
     * Path with maximum throughput (link and storage) and lowest latency
     */
    PATH_THROUGHPUT_AND_LATENCY("Min. transfer time", new PathThroughputLatencyMetric()),
    
    /**
     * Path with maximum throughput (link and storage) and lowest latency - dynamic routing is enabled
     */
    DYNAMIC_PATH_THROUGHPUT_AND_LATENCY("Min. transfer time (dynamic)", new PathThroughputLatencyMetric(), true, 10000),
    
    /**
     * Path with maximum throughput (link and storage) and lowest latency - 
     * dynamic routing and hierarchical storage management is enabled
     */
    HIERARCHICAL_DYNAMIC_PATH_THROUGHPUT_AND_LATENCY("Hierarchical", true, 10000, true, new PathThroughputLatencyMetric(), new LRUCascadeMigrationPlanner()),
    
    /**
     * Path with maximum throughput (link and storage) and lowest latency - 
     * dynamic routing and hierarchical storage management is enabled.
     * Favors paths that could achieve highest throughput by migrating files
     * onto faster storage.
     */
    HIERARCHICAL_DYNAMIC_PATH_THROUGHPUT_LATENCY_ADVANCED("Hierarchical (advanced)", true, 10000, true, new HierarchicalThroughputMetric(), new LRUCascadeMigrationPlanner());
    
    /**
     * Method name
     */
    protected final String name;
    /**
     * Dynamic routing flag
     */
    protected final boolean dynamicRouting;
    /**
     * Dynamic routing recalculation interval
     */
    protected int dynamicRoutingRecalcInterval = -1;
    /**
     * Hierarchical storage management flag
     */
    protected final boolean hierarchical;
    /**
     * Hierarchical storage management planner
     */
    protected final HierarchicalPlanner hierarchicalPlanner;
    /**
     * Graph metric
     */
    protected final GraphMetric metric;
    
    /**
     * Simulation type (method).
     * 
     * @param name method name
     * @param dynamicRouting if dynamic routing is enabled
     * @param dynamicRoutingRecalcInterval dynamic routing recalculation interval
     * @param hierarchical if hierarchical storage management is enabled
     * @param metric graph metric
     * @param hierarchicalPlanner hierarchical storage management planner
     */
    private SimulationType(String name, boolean dynamicRouting, int dynamicRoutingRecalcInterval, boolean hierarchical, GraphMetric metric, HierarchicalPlanner hierarchicalPlanner)
    {
        this.name = name;
        this.dynamicRouting = dynamicRouting;
        this.dynamicRoutingRecalcInterval = dynamicRoutingRecalcInterval;
        this.hierarchical = hierarchical;
        this.metric = metric;
        this.hierarchicalPlanner = hierarchicalPlanner;
    }
    
    /**
     * Simulation type (method).
     * 
     * @param name method name
     * @param metric graph metric
     * @param dynamicRouting if dynamic routing is enabled
     * @param dynamicRoutingRecalcInterval dynamic routing recalculation interval
     */
    private SimulationType(String name, GraphMetric metric, boolean dynamicRouting, int dynamicRoutingRecalcInterval)
    {
        this(name, dynamicRouting, dynamicRoutingRecalcInterval, false, metric, null);
    }
    
    /**
     * Simulation type (method).
     * 
     * @param name method name
     * @param metric graph metric
     */
    private SimulationType(String name, GraphMetric metric)
    {
        this(name, metric, false, -1);
    }
        
    /**
     * If dynamic routing is enabled for this method.
     * 
     * @return true if enabled, false otherwise
     */
    public boolean isDynamicRoutingEnabled()
    {
        return this.dynamicRouting;
    }

    /**
     * Get hierarchical access monitor.
     * 
     * @return hierarchical access monitor
     */
    public HierarchicalAccessMonitor getHierarchicalMonitor()
    {
        return this.hierarchicalPlanner;
    }
    
    /**
     * Get hierarchical planner.
     * 
     * @return hierarchical planner
     */
    public HierarchicalPlanner getHierarchicalPlanner()
    {
        return this.hierarchicalPlanner;
    }
    
    /**
     * If HSM is enabled for this method.
     * 
     * @return true if enabled, false otherwise
     */
    public boolean isHierarchical()
    {
        return this.hierarchical;
    }

    /**
     * Get graph metric.
     * 
     * @return graph metric
     */
    public GraphMetric getMetric()
    {
        return metric;
    }
    
    /**
     * Get dynamic routing recalculation interval.
     * 
     * @return recalculation interval
     */
    public int getDynamicRoutingRecalcInterval()
    {
        return this.dynamicRoutingRecalcInterval;
    }
    
    /**
     * Get method name.
     * 
     * @return method name
     */
    @Override public String toString()
    {
        return this.name;
    } 
}
