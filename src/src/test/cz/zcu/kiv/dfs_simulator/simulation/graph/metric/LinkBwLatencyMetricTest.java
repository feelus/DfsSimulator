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
import cz.zcu.kiv.dfs_simulator.simulation.GetSimulationTask;
import cz.zcu.kiv.dfs_simulator.simulation.SimulationType;
import cz.zcu.kiv.dfs_simulator.simulation.graph.DijkstraGraphSearcher;
import cz.zcu.kiv.dfs_simulator.simulation.path.DfsPath;
import cz.zcu.kiv.dfs_simulator.simulation.path.FsObjectNotFoundException;
import cz.zcu.kiv.dfs_simulator.simulation.path.MetricDfsPathPicker;
import cz.zcu.kiv.dfs_simulator.simulation.path.NoPathAvailableException;
import cz.zcu.kiv.dfs_simulator.model.storage.ServerStorage;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FsFile;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.NotEnoughSpaceLeftException;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.NotMountedException;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test {@link LinkBwLatencyMetric}.
 */
public class LinkBwLatencyMetricTest
{
    
    public LinkBwLatencyMetricTest()
    {
    }
    
    
    @Before public void setUp()
    {
    }
        
    /**
     * Test metric - if path with best latency is chosen.
     * 
     * @throws NoPathAvailableException no path found
     * @throws NotEnoughSpaceLeftException not enough space for upload
     * @throws FsObjectNotFoundException object not found
     * @throws NotMountedException object not mounted
     */
    @Test public void testLatencyMetric() throws NotEnoughSpaceLeftException, NoPathAvailableException, FsObjectNotFoundException, NotMountedException
    {
        ModelClientNode origin = new ModelClientNode();
        MetricDfsPathPicker builder = new MetricDfsPathPicker(
                new DijkstraGraphSearcher(new LinkBwLatencyMetric()));
        
        // 2
        ModelServerNode s1 = new ModelServerNode();
        // 3
        ModelServerNode s2 = new ModelServerNode();
        
        // 5
        ModelServerNode target = new ModelServerNode();
        
        ByteSpeed bw = new ByteSpeed(10);
        
        
        origin.getConnectionManager().addConnection(new ModelNodeConnection(origin, s1, bw, 5));
        s1.getConnectionManager().addConnection(new ModelNodeConnection(s1, origin, bw, 5));
        origin.getConnectionManager().addConnection(new ModelNodeConnection(origin, s2, bw, 5));
        s2.getConnectionManager().addConnection(new ModelNodeConnection(s2, origin, bw, 5));
        
        s2.getConnectionManager().addConnection(new ModelNodeConnection(s2, target, bw, 10));
        target.getConnectionManager().addConnection(new ModelNodeConnection(target, s2, bw, 10));
        
        s1.getConnectionManager().addConnection(new ModelNodeConnection(s1, target, bw, 8));
        target.getConnectionManager().addConnection(new ModelNodeConnection(target, s1, bw, 8));
        
        FsFile file = new FsFile("soubor", new ByteSize(1, ByteSizeUnits.GB), target.getRootDir());
        
        ServerStorage stor1 = new ServerStorage(new ByteSize(100, ByteSizeUnits.GB), new ByteSpeed(100, ByteSpeedUnits.MBPS));
        target.getFsManager().addDirectoryChild(target.getRootDir(), file);
        target.getStorageManager().getStorage().add(stor1);
        target.getFsManager().mount(stor1, file);
        
        DfsPath result = builder.selectPath(origin, new GetSimulationTask(file), 0, null, SimulationType.LINK_BANDWIDTH_LATENCY);
        
        assertNotNull(result);
        assertEquals(2, result.getPath().size());
        assertEquals(s1, result.getPath().get(0).getNeighbour());
    }
        
    /**
     * Test metric - if path with best both bandwidth and latency is chosen.
     * 
     * @throws NoPathAvailableException no path found
     * @throws NotEnoughSpaceLeftException not enough space for upload
     * @throws FsObjectNotFoundException object not found
     * @throws NotMountedException object not mounted
     */
    @Test public void testBwAndLatencyMetric() throws NotEnoughSpaceLeftException, NoPathAvailableException, FsObjectNotFoundException, NotMountedException
    {
        // 1
        ModelClientNode origin = new ModelClientNode();
        MetricDfsPathPicker builder = new MetricDfsPathPicker(
                new DijkstraGraphSearcher(new LinkBwLatencyMetric()));
        
        // 2
        ModelServerNode s1 = new ModelServerNode();
        // 3
        ModelServerNode target = new ModelServerNode();
        
        ByteSpeed bw = new ByteSpeed(5, ByteSpeedUnits.MBPS);
        ByteSpeed bw2 = new ByteSpeed(10, ByteSpeedUnits.MBPS);
        
        // origin directly to target but slow link (with good latency)
        origin.getConnectionManager().addConnection(new ModelNodeConnection(origin, target, bw, 1));
        origin.getConnectionManager().addConnection(new ModelNodeConnection(origin, s1, bw2, 10));
        s1.getConnectionManager().addConnection(new ModelNodeConnection(s1, target, bw2, 15));
        
        FsFile file = new FsFile("soubor", new ByteSize(1, ByteSizeUnits.GB), target.getRootDir());
        
        ServerStorage stor1 = new ServerStorage(new ByteSize(100, ByteSizeUnits.GB), new ByteSpeed(100, ByteSpeedUnits.MBPS));
        target.getFsManager().addDirectoryChild(target.getRootDir(), file);
        target.getStorageManager().getStorage().add(stor1);
        target.getFsManager().mount(stor1, file);
        
        DfsPath result = builder.selectPath(origin, new GetSimulationTask(file), 0, null, SimulationType.LINK_BANDWIDTH_LATENCY);
        
        assertNotNull(result);
        assertEquals(2, result.getPath().size());
        assertEquals(s1, result.getPath().get(0).getNeighbour());
    }
        
}
