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

package cz.zcu.kiv.dfs_simulator.model.connection;

import cz.zcu.kiv.dfs_simulator.model.ByteSpeed;
import cz.zcu.kiv.dfs_simulator.model.ByteSpeedUnits;
import cz.zcu.kiv.dfs_simulator.model.ModelClientNode;
import cz.zcu.kiv.dfs_simulator.model.ModelNode;
import cz.zcu.kiv.dfs_simulator.model.ModelServerNode;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test {@link ModelNodeConnection}.
 */
public class ModelNodeConnectionTest
{
    private ModelNodeConnection connection;
    private ModelNode origin;
    private ModelNode target;
    private ByteSpeed bw;
    private final int latency = 10;
    
    @Before public void setUp()
    {
        this.origin = new ModelClientNode();
        this.target = new ModelServerNode();
        this.bw = new ByteSpeed(100, ByteSpeedUnits.MBPS);
        
        this.connection = new ModelNodeConnection(this.origin, this.target, this.bw, this.latency);
    }
    
    /**
     * Test method {@link ModelNodeConnection#getOrigin()}.
     */
    @Test public void getOriginTest()
    {
        assertEquals(this.origin, this.connection.getOrigin());
    }
        
    /**
     * Test method {@link ModelNodeConnection#getNeighbour()}.
     */
    @Test public void getNeighbourTest()
    {
        assertEquals(this.target, this.connection.getNeighbour());
    }
        
    /**
     * Test method {@link ModelNodeConnection#getMaximumBandwidth()}.
     */
    @Test public void getMaximumBandwidthTest()
    {
        assertEquals(this.bw.bpsProperty().get(), this.connection.getMaximumBandwidth().bpsProperty().get());
    }
        
    /**
     * Test method {@link ModelNodeConnection#getAverageBandwidth(long, long)}.
     */
    @Test public void getAverageBandwidthTest()
    {
        assertNotNull(this.connection.getCharacteristic());
        // default char
        assertTrue(this.connection.getCharacteristic() instanceof LineConnectionCharacteristic);
        LineConnectionCharacteristic characteristic = (LineConnectionCharacteristic) this.connection.getCharacteristic();
        
        assertTrue(!characteristic.getDiscretePoints().isEmpty());
        
        characteristic.getDiscretePoints().get(0).yProperty().set(0.2);
        assertEquals(20000000, this.connection.getAverageBandwidth(0, 0).bpsProperty().get());
    }
}
