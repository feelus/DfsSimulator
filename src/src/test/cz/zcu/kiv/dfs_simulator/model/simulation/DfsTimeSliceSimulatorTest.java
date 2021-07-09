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

package cz.zcu.kiv.dfs_simulator.model.simulation;

import cz.zcu.kiv.dfs_simulator.simulation.GetSimulationTask;
import cz.zcu.kiv.dfs_simulator.simulation.DfsTimeSliceSimulator;
import cz.zcu.kiv.dfs_simulator.simulation.SimulationType;
import cz.zcu.kiv.dfs_simulator.simulation.DfsSimulatorTaskResultState;
import cz.zcu.kiv.dfs_simulator.simulation.PutSimulationTask;
import cz.zcu.kiv.dfs_simulator.simulation.DfsStringSimulatorLogger;
import cz.zcu.kiv.dfs_simulator.simulation.DfsSimulatorTaskResult;
import cz.zcu.kiv.dfs_simulator.simulation.SimulationPlan;
import cz.zcu.kiv.dfs_simulator.model.ByteSize;
import cz.zcu.kiv.dfs_simulator.model.ByteSizeUnits;
import cz.zcu.kiv.dfs_simulator.model.ByteSpeed;
import cz.zcu.kiv.dfs_simulator.model.ByteSpeedUnits;
import cz.zcu.kiv.dfs_simulator.model.ModelClientNode;
import cz.zcu.kiv.dfs_simulator.model.ModelServerNode;
import cz.zcu.kiv.dfs_simulator.model.connection.LineConnectionCharacteristic;
import cz.zcu.kiv.dfs_simulator.model.connection.ModelNodeConnection;
import cz.zcu.kiv.dfs_simulator.simulation.path.MetricDfsPathPicker;
import cz.zcu.kiv.dfs_simulator.model.storage.ServerStorage;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FileSystemObject;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FsDirectory;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FsFile;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.NotEnoughSpaceLeftException;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test {@link DfsTimeSliceSimulator}.
 */
public class DfsTimeSliceSimulatorTest
{
 
    /**
     * Test method {@link DfsTimeSliceSimulator#executeDownloadTask(
     * cz.zcu.kiv.dfs_simulator.simulation.SimulationTask, 
     * long, 
     * java.util.List, 
     * cz.zcu.kiv.dfs_simulator.simulation.SimulationThroughputSampler, 
     * cz.zcu.kiv.dfs_simulator.simulation.DfsSimulatorLogger)}.
     * 
     * @throws NotEnoughSpaceLeftException when target storage does not have
     * enough space available - cannot be thrown here
     */
    @Test public void testDownloadRun() throws NotEnoughSpaceLeftException
    {
        ModelServerNode s1 = new ModelServerNode();
        ModelClientNode c1 = new ModelClientNode();
        
        ByteSpeed bw = new ByteSpeed(10, ByteSpeedUnits.MBPS);
        ModelNodeConnection conn1 = new ModelNodeConnection(c1, s1, bw, 10);
        
        s1.getConnectionManager().addConnection(conn1);
        c1.getConnectionManager().addConnection(conn1);
        
        ServerStorage stor1 = new ServerStorage(
                new ByteSize(100, ByteSizeUnits.GB), new ByteSpeed(100, ByteSpeedUnits.MBPS));
        s1.getStorageManager().getStorage().add(stor1);
        
        FsFile f = new FsFile("stahnout", new ByteSize(1054, ByteSizeUnits.MB), s1.getRootDir());
        
        s1.getFsManager().addDirectoryChild(s1.getRootDir(), f);
        s1.getFsManager().mount(stor1, s1.getRootDir());
        
        SimulationPlan simPlan = new SimulationPlan();
        GetSimulationTask gtask = new GetSimulationTask(f);
        
        simPlan.getTasks().add(gtask);
        
        MetricDfsPathPicker pathBuilder = new MetricDfsPathPicker();
        // enable recalc just for testing purposes - makes no sense for shortest path
        DfsTimeSliceSimulator sim = new DfsTimeSliceSimulator(c1, simPlan, pathBuilder, SimulationType.DYNAMIC_PATH_THROUGHPUT_AND_LATENCY);
        
        DfsStringSimulatorLogger logger = new DfsStringSimulatorLogger();
        sim.run(logger);
        
        List<DfsSimulatorTaskResult> l = sim.getResults();
        
        assertNotNull(l);
        assertTrue(!l.isEmpty());
        assertEquals(1, l.size());
        assertEquals(DfsSimulatorTaskResultState.SUCCESS, l.get(0).getState());
    }
    
    /**
     * Test method {@link DfsTimeSliceSimulator#executeUploadTask(
     * cz.zcu.kiv.dfs_simulator.simulation.SimulationTask, 
     * long, 
     * java.util.List, 
     * cz.zcu.kiv.dfs_simulator.simulation.SimulationThroughputSampler, 
     * cz.zcu.kiv.dfs_simulator.simulation.DfsSimulatorLogger)}.
     * 
     * @throws NotEnoughSpaceLeftException when target storage does not have
     * enough space available
     */
    @Test public void testUploadRun() throws NotEnoughSpaceLeftException
    {
        ModelServerNode s1 = new ModelServerNode();
        ModelClientNode c1 = new ModelClientNode();
        
        ByteSpeed bw = new ByteSpeed(10, ByteSpeedUnits.MBPS);
        ModelNodeConnection conn1 = new ModelNodeConnection(c1, s1, bw, 10);
        
        s1.getConnectionManager().addConnection(conn1);
        c1.getConnectionManager().addConnection(conn1);
        
        ServerStorage stor1 = new ServerStorage(
                new ByteSize(100, ByteSizeUnits.GB), new ByteSpeed(100, ByteSpeedUnits.MBPS));
        s1.getStorageManager().getStorage().add(stor1);
        
        FsDirectory parentDir = new FsDirectory("slozka", s1.getRootDir());
        FsFile f = new FsFile("nahrat", new ByteSize(1024, ByteSizeUnits.MB), parentDir);
        
        s1.getFsManager().addDirectoryChild(s1.getRootDir(), parentDir);
        s1.getFsManager().addDirectoryChild(parentDir, f);
        s1.getFsManager().mount(stor1, s1.getRootDir());
        
        SimulationPlan simPlan = new SimulationPlan();
        PutSimulationTask ptask = new PutSimulationTask(f);
        
        simPlan.getTasks().add(ptask);
        
        MetricDfsPathPicker pathBuilder = new MetricDfsPathPicker();
        DfsTimeSliceSimulator sim = new DfsTimeSliceSimulator(c1, simPlan, pathBuilder, SimulationType.DYNAMIC_PATH_THROUGHPUT_AND_LATENCY);
        
        DfsStringSimulatorLogger logger = new DfsStringSimulatorLogger();
        sim.run(logger);
        
        List<DfsSimulatorTaskResult> l = sim.getResults();
        
        assertNotNull(l);
        assertTrue(!l.isEmpty());
        assertEquals(1, l.size());
        assertEquals(DfsSimulatorTaskResultState.SUCCESS, l.get(0).getState());
        
        FileSystemObject uploadedF = s1.getRootDir().getChildObject(f.getFullPath());
        assertNotNull(uploadedF);
        assertEquals(f.getSize().bytesProperty().get(), uploadedF.getSize().bytesProperty().get());
    }
    
    /**
     * Test method {@link DfsTimeSliceSimulator#executeDownloadTask(
     * cz.zcu.kiv.dfs_simulator.simulation.SimulationTask, 
     * long, 
     * java.util.List, 
     * cz.zcu.kiv.dfs_simulator.simulation.SimulationThroughputSampler, 
     * cz.zcu.kiv.dfs_simulator.simulation.DfsSimulatorLogger)}
     * through path with modified maximum bandwidth.
     * 
     * @throws NotEnoughSpaceLeftException 
     */
    @Test public void testModifiedThroughput() throws NotEnoughSpaceLeftException
    {
        ModelServerNode s1 = new ModelServerNode();
        ModelClientNode c1 = new ModelClientNode();
        
        ByteSpeed bw = new ByteSpeed(10, ByteSpeedUnits.MBPS);
        ModelNodeConnection conn1 = new ModelNodeConnection(c1, s1, bw, 10);
        
        // default LineConnectionCharacteristic
        LineConnectionCharacteristic characteristic = (LineConnectionCharacteristic) conn1.getCharacteristic();
        
        // set connection characteristic modificators to 0.5
        characteristic.getDiscretePoints().stream().forEach((x) ->
        {
            x.yProperty().set(0.5d);
        });
        
        s1.getConnectionManager().addConnection(conn1);
        c1.getConnectionManager().addConnection(conn1);
        
        ServerStorage stor1 = new ServerStorage(
                new ByteSize(100, ByteSizeUnits.GB), new ByteSpeed(100, ByteSpeedUnits.MBPS));
        s1.getStorageManager().getStorage().add(stor1);
        
        FsFile f = new FsFile("stahnout", new ByteSize(10, ByteSizeUnits.GB), s1.getRootDir());
        
        s1.getFsManager().addDirectoryChild(s1.getRootDir(), f);
        s1.getFsManager().mount(stor1, s1.getRootDir());
        
        SimulationPlan simPlan = new SimulationPlan();
        GetSimulationTask gtask = new GetSimulationTask(f);
        
        simPlan.getTasks().add(gtask);
        
        MetricDfsPathPicker pathBuilder = new MetricDfsPathPicker();
        DfsTimeSliceSimulator sim = new DfsTimeSliceSimulator(c1, simPlan, pathBuilder, SimulationType.DYNAMIC_PATH_THROUGHPUT_AND_LATENCY);
        
        DfsStringSimulatorLogger logger = new DfsStringSimulatorLogger();
        sim.run(logger);
        
        List<DfsSimulatorTaskResult> l = sim.getResults();
        
        assertNotNull(l);
        assertTrue(!l.isEmpty());
        assertEquals(1, l.size());
        assertEquals(DfsSimulatorTaskResultState.SUCCESS, l.get(0).getState());
        
        long estTime = (f.getSize().bytesProperty().get() / (conn1.getMaximumBandwidth().bpsProperty().get() / 2)) * 1000;
        estTime += conn1.getLatency();
        
        assertEquals(estTime, l.get(0).getTotalTime());
    }
    
}
