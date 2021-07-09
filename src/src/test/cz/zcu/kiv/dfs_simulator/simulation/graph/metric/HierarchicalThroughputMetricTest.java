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
import cz.zcu.kiv.dfs_simulator.model.ModelServerNode;
import cz.zcu.kiv.dfs_simulator.model.storage.ServerStorage;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FsFile;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.NotEnoughSpaceLeftException;
import cz.zcu.kiv.dfs_simulator.simulation.SimulationType;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test {@link HierarchicalThroughputMetric}.
 */
public class HierarchicalThroughputMetricTest
{
    
    /**
     * Test method {@link HierarchicalThroughputMetric#getPossibleDiskBandwidth(
     * cz.zcu.kiv.dfs_simulator.model.ModelServerNode, 
     * cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FileSystemObject, 
     * cz.zcu.kiv.dfs_simulator.simulation.SimulationType)}.
     * 
     * @throws NotEnoughSpaceLeftException when storage does not have enough space
     * for test file
     */
    @Test public void testGetPossibleDiskBandwidth() throws NotEnoughSpaceLeftException
    {
        HierarchicalThroughputMetric metric = new HierarchicalThroughputMetric();
        ModelServerNode s1 = new ModelServerNode();

        ByteSpeed bwDiskSlow = new ByteSpeed(10, ByteSpeedUnits.MBPS);
        ByteSpeed bwDiskFast = new ByteSpeed(100, ByteSpeedUnits.MBPS);
        
        ServerStorage storSlow = new ServerStorage(new ByteSize(1000, ByteSizeUnits.GB), bwDiskSlow);
        ServerStorage storFast = new ServerStorage(new ByteSize(10, ByteSizeUnits.GB), bwDiskFast);
        
        FsFile downloadedSlow = new FsFile("soubor", new ByteSize(5, ByteSizeUnits.GB), s1.getRootDir());
        
        s1.getStorageManager().getStorage().add(storSlow);
        s1.getStorageManager().getStorage().add(storFast);
        
        s1.getFsManager().addDirectoryChild(s1.getRootDir(), downloadedSlow);
        s1.getFsManager().mount(storSlow, downloadedSlow);
        
        ByteSpeed possibleBw = 
                metric.getPossibleDiskBandwidth(s1, downloadedSlow, SimulationType.HIERARCHICAL_DYNAMIC_PATH_THROUGHPUT_LATENCY_ADVANCED);
        
        assertNotNull(possibleBw);
        assertEquals(storFast.getMaximumSpeed().bpsProperty().get(), possibleBw.bpsProperty().get());
    }
    
    /**
     * Test method {@link HierarchicalThroughputMetric#getPossibleDiskBandwidth(
     * cz.zcu.kiv.dfs_simulator.model.ModelServerNode, 
     * cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FileSystemObject, 
     * cz.zcu.kiv.dfs_simulator.simulation.SimulationType)}.
     * 
     * File is larger than maximum capacity on faster disk, should return slower.
     * 
     * @throws NotEnoughSpaceLeftException when storage does not have enough space
     * for test file
     */
    @Test public void testGetPossibleDiskBandwidth2() throws NotEnoughSpaceLeftException
    {
        HierarchicalThroughputMetric metric = new HierarchicalThroughputMetric();
        ModelServerNode s1 = new ModelServerNode();

        ByteSpeed bwDiskSlow = new ByteSpeed(10, ByteSpeedUnits.MBPS);
        ByteSpeed bwDiskFast = new ByteSpeed(100, ByteSpeedUnits.MBPS);
        
        ServerStorage storSlow = new ServerStorage(new ByteSize(1000, ByteSizeUnits.GB), bwDiskSlow);
        ServerStorage storFast = new ServerStorage(new ByteSize(10, ByteSizeUnits.GB), bwDiskFast);
        
        FsFile downloadedSlow = new FsFile("soubor", new ByteSize(50, ByteSizeUnits.GB), s1.getRootDir());
        
        s1.getStorageManager().getStorage().add(storSlow);
        s1.getStorageManager().getStorage().add(storFast);
        
        s1.getFsManager().addDirectoryChild(s1.getRootDir(), downloadedSlow);
        s1.getFsManager().mount(storSlow, downloadedSlow);
        
        ByteSpeed possibleBw = 
                metric.getPossibleDiskBandwidth(s1, downloadedSlow, SimulationType.HIERARCHICAL_DYNAMIC_PATH_THROUGHPUT_LATENCY_ADVANCED);
        
        assertNotNull(possibleBw);
        assertEquals(storSlow.getMaximumSpeed().bpsProperty().get(), possibleBw.bpsProperty().get());
    }
}
