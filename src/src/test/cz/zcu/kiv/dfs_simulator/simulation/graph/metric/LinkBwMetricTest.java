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
import cz.zcu.kiv.dfs_simulator.model.storage.StorageOperation;
import cz.zcu.kiv.dfs_simulator.model.storage.StorageOperationTransferLimiter;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FsFile;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.NotEnoughSpaceLeftException;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.NotMountedException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test {@link LinkBwMetric}.
 */
public class LinkBwMetricTest
{
    
    protected ModelClientNode origin;
    
    @Before public void setUp()
    {
        this.origin = new ModelClientNode();
    }
    /**
     * Test metric - if path with highest bandwidth is chosen (widest path).
     * 
     * @throws NoPathAvailableException no path found
     * @throws NotEnoughSpaceLeftException not enough space for upload
     * @throws FsObjectNotFoundException object not found
     * @throws NotMountedException object not mounted
     */
    @Test public void testMetric() throws NotEnoughSpaceLeftException, NoPathAvailableException, FsObjectNotFoundException, NotMountedException
    {
        MetricDfsPathPicker builder = new MetricDfsPathPicker(
                new DijkstraGraphSearcher(new LinkBwMetric()));
        
        // 2
        ModelServerNode s1 = new ModelServerNode();
        // 3
        ModelServerNode s2 = new ModelServerNode();
        
        // 4
        ModelServerNode s3 = new ModelServerNode();
        
        // 5
        ModelServerNode target = new ModelServerNode();
        
        ByteSpeed bw1 = new ByteSpeed(10, ByteSpeedUnits.MBPS);
        ByteSpeed bw2 = new ByteSpeed(5, ByteSpeedUnits.MBPS);
        
        this.origin.getConnectionManager().addConnection(new ModelNodeConnection(this.origin, s1, bw1, 5));
        s1.getConnectionManager().addConnection(new ModelNodeConnection(s1, this.origin, bw1, 5));
        this.origin.getConnectionManager().addConnection(new ModelNodeConnection(this.origin, s2, bw1, 5));
        s2.getConnectionManager().addConnection(new ModelNodeConnection(s2, this.origin, bw1, 5));
        
        s1.getConnectionManager().addConnection(new ModelNodeConnection(s1, s3, bw1, 10));
        s3.getConnectionManager().addConnection(new ModelNodeConnection(s3, s1, bw1, 10));
        
        s3.getConnectionManager().addConnection(new ModelNodeConnection(s3, target, bw1, 10));
        target.getConnectionManager().addConnection(new ModelNodeConnection(target, s3, bw1, 10));
        
        s2.getConnectionManager().addConnection(new ModelNodeConnection(s2, target, bw2, 10));
        target.getConnectionManager().addConnection(new ModelNodeConnection(target, s2, bw2, 10));
        
        FsFile file = new FsFile("soubor", new ByteSize(1, ByteSizeUnits.GB), target.getRootDir());
        
        ServerStorage stor1 = new ServerStorage(new ByteSize(100, ByteSizeUnits.GB), new ByteSpeed(100, ByteSpeedUnits.MBPS));
                
        target.getFsManager().addDirectoryChild(target.getRootDir(), file);
        target.getStorageManager().getStorage().add(stor1);
        target.getFsManager().mount(stor1, file);
        
        DfsPath result = builder.selectPath(this.origin, new GetSimulationTask(file), 0, null, SimulationType.LINK_BANDWIDTH);
        List<FsFile> transferList = new ArrayList<>();
        transferList.add(file);
        
        StorageOperation storageOp = stor1.getOperationManager().addUnmanagedReadOperation(transferList, new StorageOperationTransferLimiter()
        {
            @Override public ByteSpeed getTransferLimit(long sTime)
            {
                return result.getCurrentLinkBandwidth(sTime);
            }
        });
        stor1.getOperationManager().updateAvailableThroughput(0);
        result.setRunningOperation(storageOp);
        
        assertNotNull(result);
        assertEquals(3, result.getPath().size());
        assertEquals(bw1.bpsProperty().get(), result.getCurrentPossibleThroughput(0).bpsProperty().get());
    }
    
}
