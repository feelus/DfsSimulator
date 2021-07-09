package cz.zcu.kiv.dfs_simulator.simulation.path;

import cz.zcu.kiv.dfs_simulator.model.ModelClientNode;
import cz.zcu.kiv.dfs_simulator.model.ModelNode;
import cz.zcu.kiv.dfs_simulator.model.ModelServerNode;
import cz.zcu.kiv.dfs_simulator.model.connection.ModelNodeConnection;
import cz.zcu.kiv.dfs_simulator.simulation.GetSimulationTask;
import cz.zcu.kiv.dfs_simulator.simulation.SimulationTask;
import cz.zcu.kiv.dfs_simulator.simulation.SimulationType;
import cz.zcu.kiv.dfs_simulator.simulation.graph.DijkstraGraphSearcher;
import cz.zcu.kiv.dfs_simulator.simulation.graph.metric.DistanceMetric;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FileSystemObject;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FsGlobalObjectRegistry;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.NotMountedException;
import java.util.List;
import cz.zcu.kiv.dfs_simulator.simulation.graph.GraphSearcher;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class MetricDfsPathPicker implements DfsPathPicker
{
    
    private final GraphSearcher graphSearcher;
    
    public MetricDfsPathPicker(GraphSearcher graphSearcher)
    {
        this.graphSearcher = graphSearcher;
    }
    
    public MetricDfsPathPicker()
    {
        this(new DijkstraGraphSearcher(new DistanceMetric()));
    }
    
    @Override public DfsPath selectPath(ModelNode origin, SimulationTask task, long sTime, ModelServerNode forceTarget, SimulationType simType) throws NoPathAvailableException, FsObjectNotFoundException, NotMountedException
    {
        // get list of server nodes that have our desired object
        final FileSystemObject taskObject = 
                (task instanceof GetSimulationTask) ? task.getFile() : task.getFile().getParent();
                
        Set<ModelServerNode> serverNodes;
        
        // fixed destination
        if(forceTarget != null)
        {
            serverNodes = new HashSet<>();
            serverNodes.add(forceTarget);
        }
        else
        {
            serverNodes = FsGlobalObjectRegistry.getEntryMountedNodeList(taskObject);
        }
        
        if(serverNodes == null || serverNodes.isEmpty())
        {
            throw new FsObjectNotFoundException("Couldn't find requested file in file registry.");
        }
        
        // get metric comparator
        Comparator<Long> comparator = this.graphSearcher.getMetric().getComparator();
        
        List<ModelNodeConnection> bestPath = null;
        Long bestPathMetric = null;
        // get shortest path to each node
        for(ModelServerNode serverNode : serverNodes)
        {
            List<ModelNodeConnection> cBestPath = new ArrayList<>();
            Long cPathMetric = this.graphSearcher.findPath(origin, serverNode, task, sTime, cBestPath, simType);
            
            // no path to server node
            if(cPathMetric == null)
            {
                continue;
            }
            
            if(bestPath == null || comparator.compare(cPathMetric, bestPathMetric) < 0)
            {
                bestPath = cBestPath;
                bestPathMetric = cPathMetric;
            }
        }
        
        if(bestPath == null)
        {
            throw new NoPathAvailableException("No path available");
        }
        
        return new DfsPath(bestPath, task, sTime);
    }

    @Override public long getObjectRegistryQueryTime(ModelClientNode origin) throws NoNeighboursAvailableException
    {
        List<ModelNodeConnection> serverConnections = 
                origin.getConnectionManager().getDirectServerConnections();
        
        if(serverConnections.isEmpty())
        {
            throw new NoNeighboursAvailableException("Client node has no neighbour servers.");
        }
        
        // get first available link with server and return its latency
        return serverConnections.get(0).getLatency();
    }
    
}
