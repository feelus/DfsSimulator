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
 * Test {@link PathThroughputMetric}.
 */
public class PathThroughputMetricTest
{
    
    protected ModelClientNode origin;
    
    @Before public void setUp()
    {
        this.origin = new ModelClientNode();
    }
    /**
     * Test metric - if path with highest throughput (link and storage) is chosen.
     * 
     * @throws NoPathAvailableException no path found
     * @throws NotEnoughSpaceLeftException not enough space for upload
     * @throws FsObjectNotFoundException object not found
     * @throws NotMountedException object not mounted
     */
    @Test public void testMetric() throws NotEnoughSpaceLeftException, NoPathAvailableException, FsObjectNotFoundException, NotMountedException
    {
        MetricDfsPathPicker builder = new MetricDfsPathPicker(
                new DijkstraGraphSearcher(new PathThroughputMetric()));
        
        // 2
        ModelServerNode s1 = new ModelServerNode();
        // 3
        ModelServerNode s2 = new ModelServerNode();
        
        // 4
        ModelServerNode s3 = new ModelServerNode();
        // 5
        ModelServerNode s4 = new ModelServerNode();
        
        // 6
        ModelServerNode targetSlow = new ModelServerNode();
        // 7
        ModelServerNode targetFast = new ModelServerNode();
        
        ByteSpeed bwDiskSlow = new ByteSpeed(10, ByteSpeedUnits.MBPS);
        ByteSpeed bwDiskFast = new ByteSpeed(100, ByteSpeedUnits.MBPS);
        
        ServerStorage storSlow = new ServerStorage(new ByteSize(1000, ByteSizeUnits.GB), bwDiskSlow);
        ServerStorage storFast = new ServerStorage(new ByteSize(10, ByteSizeUnits.GB), bwDiskFast);
        
        FsFile downloadedSlow = new FsFile("soubor", new ByteSize(5, ByteSizeUnits.GB), targetSlow.getRootDir());
        FsFile downloadedFast = new FsFile("soubor", new ByteSize(5, ByteSizeUnits.GB), targetFast.getRootDir());
        
        targetSlow.getFsManager().addDirectoryChild(targetSlow.getRootDir(), downloadedSlow);
        targetFast.getFsManager().addDirectoryChild(targetFast.getRootDir(), downloadedFast);
        
        targetSlow.getFsManager().mount(storSlow, downloadedSlow);
        targetFast.getFsManager().mount(storFast, downloadedFast);
        
        
        ByteSpeed linkBandwidth = new ByteSpeed(50, ByteSpeedUnits.MBPS);
        
        // link paths, latency and distance same, different disk bandwidths
        // 1 - 2 - 4 - 6 (slow)
        this.origin.getConnectionManager().addConnection(new ModelNodeConnection(this.origin, s1, linkBandwidth, 10));
        s1.getConnectionManager().addConnection(new ModelNodeConnection(s1, s3, linkBandwidth, 10));
        s3.getConnectionManager().addConnection(new ModelNodeConnection(s3, targetSlow, linkBandwidth, 10));
        
        // 1 - 3 - 5 - 7 (fast)
        this.origin.getConnectionManager().addConnection(new ModelNodeConnection(this.origin, s2, linkBandwidth, 10));
        s2.getConnectionManager().addConnection(new ModelNodeConnection(s2, s4, linkBandwidth, 10));
        s4.getConnectionManager().addConnection(new ModelNodeConnection(s4, targetFast, linkBandwidth, 10));
        
        DfsPath path = builder.selectPath(this.origin, new GetSimulationTask(downloadedSlow), 0, null, SimulationType.PATH_THROUGHPUT);
        
        assertNotNull(path);
        assertEquals(3, path.getPath().size());
        assertEquals(targetFast, path.getPath().get(2).getNeighbour());
    }
    
}
