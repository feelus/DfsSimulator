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

package cz.zcu.kiv.dfs_simulator.model.simulation.path;

import cz.zcu.kiv.dfs_simulator.simulation.GetSimulationTask;
import cz.zcu.kiv.dfs_simulator.simulation.SimulationType;
import cz.zcu.kiv.dfs_simulator.model.ByteSize;
import cz.zcu.kiv.dfs_simulator.model.ByteSizeUnits;
import cz.zcu.kiv.dfs_simulator.model.ByteSpeed;
import cz.zcu.kiv.dfs_simulator.model.ByteSpeedUnits;
import cz.zcu.kiv.dfs_simulator.model.ModelClientNode;
import cz.zcu.kiv.dfs_simulator.model.ModelServerNode;
import cz.zcu.kiv.dfs_simulator.model.connection.ModelNodeConnection;
import cz.zcu.kiv.dfs_simulator.simulation.path.DfsPath;
import cz.zcu.kiv.dfs_simulator.simulation.path.FsObjectNotFoundException;
import cz.zcu.kiv.dfs_simulator.simulation.path.NoPathAvailableException;
import cz.zcu.kiv.dfs_simulator.simulation.path.MetricDfsPathPicker;
import cz.zcu.kiv.dfs_simulator.model.storage.ServerStorage;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FsFile;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.NotEnoughSpaceLeftException;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.NotMountedException;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test {@link MetricDfsPathPicker}.
 */
public class MetricDfsPathPickerTest
{
    protected ModelClientNode origin;
    protected MetricDfsPathPicker finder;
    
    @Before public void setUp()
    {
        this.origin = new ModelClientNode();
        
        this.finder = new MetricDfsPathPicker();
    }
    
    /**
     * Test method {@link MetricDfsPathPicker#selectPath(
     * cz.zcu.kiv.dfs_simulator.model.ModelNode, 
     * cz.zcu.kiv.dfs_simulator.simulation.SimulationTask, 
     * long, 
     * cz.zcu.kiv.dfs_simulator.model.ModelServerNode, 
     * cz.zcu.kiv.dfs_simulator.simulation.SimulationType)}.
     * 
     * @throws NotEnoughSpaceLeftException when storage does not have enough space to mount test file 
     * @throws NoPathAvailableException when there is no path to target object
     * @throws FsObjectNotFoundException when target object cannot be found
     * @throws NotMountedException when target object isn't mounted
     */
    @Test public void testSelectPath() throws NotEnoughSpaceLeftException, NoPathAvailableException, FsObjectNotFoundException, NotMountedException
    {
        // 2
        ModelServerNode s1 = new ModelServerNode();
        // 3
        ModelServerNode s2 = new ModelServerNode();
        
        // 4
        ModelServerNode is1 = new ModelServerNode();
        // 5
        ModelServerNode is2 = new ModelServerNode();
        // 6
        ModelServerNode is3 = new ModelServerNode();
        // 7
        ModelServerNode is4 = new ModelServerNode();
        
        // 8
        ModelServerNode iis1 = new ModelServerNode();
        
        ByteSpeed bandwidth = new ByteSpeed(12, ByteSpeedUnits.MBPS);
        
        this.origin.getConnectionManager().addConnection(new ModelNodeConnection(origin, s1, bandwidth, 10));
        this.origin.getConnectionManager().addConnection(new ModelNodeConnection(origin, s2, bandwidth, 10));
        
        s1.getConnectionManager().addConnection(new ModelNodeConnection(s1, is1, bandwidth, 10));
        s1.getConnectionManager().addConnection(new ModelNodeConnection(s1, is2, bandwidth, 10));
        
        s2.getConnectionManager().addConnection(new ModelNodeConnection(s2, is3, bandwidth, 10));
        s2.getConnectionManager().addConnection(new ModelNodeConnection(s2, is4, bandwidth, 10));
        
        is3.getConnectionManager().addConnection(new ModelNodeConnection(is3, iis1, bandwidth, 10));
        
        FsFile file = new FsFile("soubor", 
                new ByteSize(1, ByteSizeUnits.GB), iis1.getRootDir());
        iis1.getFsManager().addDirectoryChild(iis1.getRootDir(), file);
        
        ServerStorage stor1 = new ServerStorage(new ByteSize(100, ByteSizeUnits.GB), new ByteSpeed(100, ByteSpeedUnits.MBPS));
        iis1.getFsManager().mount(stor1, file);
        
        DfsPath result = this.finder.selectPath(this.origin, new GetSimulationTask(file), 0, null, SimulationType.SHORTEST);
        
        assertNotNull(result);
        assertEquals(3, result.getPath().size());
        assertEquals(this.origin, result.getPath().get(0).getOrigin());
    }
    
}
