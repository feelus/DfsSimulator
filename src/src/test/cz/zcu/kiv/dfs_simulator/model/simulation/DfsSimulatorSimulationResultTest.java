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

import cz.zcu.kiv.dfs_simulator.simulation.DfsSimulatorSimulationResult;
import cz.zcu.kiv.dfs_simulator.simulation.SimulationType;
import cz.zcu.kiv.dfs_simulator.simulation.DfsSimulatorTaskResultState;
import cz.zcu.kiv.dfs_simulator.simulation.PutSimulationTask;
import cz.zcu.kiv.dfs_simulator.simulation.SimulationTask;
import cz.zcu.kiv.dfs_simulator.simulation.DfsSimulatorTaskResult;
import cz.zcu.kiv.dfs_simulator.simulation.FilteringThroughputSampler;
import cz.zcu.kiv.dfs_simulator.model.ByteSize;
import cz.zcu.kiv.dfs_simulator.model.ByteSizeUnits;
import cz.zcu.kiv.dfs_simulator.model.ByteSpeed;
import cz.zcu.kiv.dfs_simulator.model.ByteSpeedUnits;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FsFile;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Test {@link DfsSimulatorSimulationResult}.
 */
public class DfsSimulatorSimulationResultTest
{
    private DfsSimulatorSimulationResult result;
    
    @Before public void setUp()
    {
        ArrayList<DfsSimulatorTaskResult> resultSet = new ArrayList<>();
        
        SimulationTask tsk = new PutSimulationTask(new FsFile("test", new ByteSize(10, ByteSizeUnits.GB), null));
        resultSet.add(new DfsSimulatorTaskResult(tsk, DfsSimulatorTaskResultState.SUCCESS, 10000, new ByteSpeed(1, ByteSpeedUnits.GBPS), new ArrayList<>(), new FilteringThroughputSampler()));
        resultSet.add(new DfsSimulatorTaskResult(tsk, DfsSimulatorTaskResultState.SUCCESS, 40000, new ByteSpeed(0.25, ByteSpeedUnits.GBPS), new ArrayList<>(), new FilteringThroughputSampler()));
        
        this.result = new DfsSimulatorSimulationResult(SimulationType.SHORTEST, resultSet);
    }
    
    /**
     * Test method {@link DfsSimulatorSimulationResult#buildCumulativeStats()}.
     */
    @Test public void buildCumulativeStatsTest()
    {
        this.result.buildCumulativeStats();

        assertEquals(50000, this.result.getTotalElapsedTime().get());
        
    }
    
}
