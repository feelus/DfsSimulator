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

package cz.zcu.kiv.dfs_simulator.simulation.graph.metric;

import cz.zcu.kiv.dfs_simulator.model.ByteSize;
import cz.zcu.kiv.dfs_simulator.model.ByteSizeUnits;
import cz.zcu.kiv.dfs_simulator.model.ByteSpeed;
import cz.zcu.kiv.dfs_simulator.model.ByteSpeedUnits;
import cz.zcu.kiv.dfs_simulator.model.ModelClientNode;
import cz.zcu.kiv.dfs_simulator.model.ModelServerNode;
import cz.zcu.kiv.dfs_simulator.model.connection.ModelNodeConnection;
import cz.zcu.kiv.dfs_simulator.model.storage.ServerStorage;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FsFile;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.NotEnoughSpaceLeftException;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.NotMountedException;
import cz.zcu.kiv.dfs_simulator.simulation.GetSimulationTask;
import cz.zcu.kiv.dfs_simulator.simulation.SimulationType;
import cz.zcu.kiv.dfs_simulator.simulation.graph.DijkstraGraphSearcher;
import cz.zcu.kiv.dfs_simulator.simulation.path.DfsPath;
import cz.zcu.kiv.dfs_simulator.simulation.path.FsObjectNotFoundException;
import cz.zcu.kiv.dfs_simulator.simulation.path.MetricDfsPathPicker;
import cz.zcu.kiv.dfs_simulator.simulation.path.NoPathAvailableException;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test {@link PathThroughputLatencyMetric}.
 */
public class PathThroughputLatencyMetricTest
{
    
    protected ModelClientNode origin;
    
    @Before public void setUp()
    {
        this.origin = new ModelClientNode();
    }
    /**
     * Test metric - if path with highest throughput (link and storage) and lowest 
     * latency is chosen.
     * 
     * @throws NoPathAvailableException no path found
     * @throws NotEnoughSpaceLeftException not enough space for upload
     * @throws FsObjectNotFoundException object not found
     * @throws NotMountedException object not mounted
     */
    @Test public void testMetric() throws NotEnoughSpaceLeftException, NoPathAvailableException, FsObjectNotFoundException, NotMountedException
    {
        MetricDfsPathPicker builder = new MetricDfsPathPicker(
                new DijkstraGraphSearcher(new PathThroughputLatencyMetric()));
        
        // 2
        ModelServerNode s1 = new ModelServerNode();
        // 3
        ModelServerNode s2 = new ModelServerNode();
        
        // 4
        ModelServerNode s3 = new ModelServerNode();
        // 5
        ModelServerNode s4 = new ModelServerNode();
        
        // 6
        ModelServerNode target1 = new ModelServerNode();
        // 7
        ModelServerNode target2 = new ModelServerNode();
        
        ByteSpeed bwDisk = new ByteSpeed(100, ByteSpeedUnits.MBPS);
        
        ServerStorage stor1 = new ServerStorage(new ByteSize(1000, ByteSizeUnits.GB), bwDisk);
        ServerStorage stor2 = new ServerStorage(new ByteSize(1000, ByteSizeUnits.GB), bwDisk);
        
        FsFile d1 = new FsFile("soubor", new ByteSize(5, ByteSizeUnits.GB), target1.getRootDir());
        FsFile d2 = new FsFile("soubor", new ByteSize(5, ByteSizeUnits.GB), target2.getRootDir());
        
        target1.getFsManager().addDirectoryChild(target1.getRootDir(), d1);
        target2.getFsManager().addDirectoryChild(target2.getRootDir(), d2);
        
        target1.getFsManager().mount(stor1, d1);
        target2.getFsManager().mount(stor2, d2);
        
        
        ByteSpeed linkBandwidth = new ByteSpeed(50, ByteSpeedUnits.MBPS);
        
        // link paths, distance same, same disk bandwidths, latency different
        // 1 - 2 - 4 - 6 (low latency)
        this.origin.getConnectionManager().addConnection(new ModelNodeConnection(this.origin, s1, linkBandwidth, 10));
        s1.getConnectionManager().addConnection(new ModelNodeConnection(s1, s3, linkBandwidth, 10));
        s3.getConnectionManager().addConnection(new ModelNodeConnection(s3, target1, linkBandwidth, 10));
        
        // 1 - 3 - 5 - 7 (high latency)
        this.origin.getConnectionManager().addConnection(new ModelNodeConnection(this.origin, s2, linkBandwidth, 100));
        s2.getConnectionManager().addConnection(new ModelNodeConnection(s2, s4, linkBandwidth, 15));
        s4.getConnectionManager().addConnection(new ModelNodeConnection(s4, target2, linkBandwidth, 10));
        
        DfsPath path = builder.selectPath(this.origin, new GetSimulationTask(d1), 0, null, SimulationType.PATH_THROUGHPUT);
        
        assertNotNull(path);
        assertEquals(3, path.getPath().size());
        assertEquals(s1, path.getPath().get(0).getNeighbour());
        assertEquals(s3, path.getPath().get(1).getNeighbour());
        assertEquals(target1, path.getPath().get(2).getNeighbour());
    }
    
}
