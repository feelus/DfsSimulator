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
import cz.zcu.kiv.dfs_simulator.model.ModelServerNode;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test {@link ModelNodeConnectionManager}.
 */
public class ModelNodeConnectionManagerTest
{
    
    protected ModelClientNode origin;
    protected ModelServerNode s1;
    protected ModelServerNode s2;
    protected ModelServerNode s3;
    
    @Before public void setUp()
    {
        this.origin = new ModelClientNode();
        
        s1 = new ModelServerNode();
        s2 = new ModelServerNode();
        s3 = new ModelServerNode();
        
        ByteSpeed bw = new ByteSpeed(100, ByteSpeedUnits.MBPS);
        
        s1.getConnectionManager().addConnection(new ModelNodeConnection(s1, this.origin, bw, 10));
        this.origin.getConnectionManager().addConnection(new ModelNodeConnection(this.origin, s1, bw, 10));
        
        s1.getConnectionManager().addConnection(new ModelNodeConnection(s1, s2, bw, 10));
        s2.getConnectionManager().addConnection(new ModelNodeConnection(s2, s1, bw, 10));
        
        s2.getConnectionManager().addConnection(new ModelNodeConnection(s2, s3, bw, 10));
        s3.getConnectionManager().addConnection(new ModelNodeConnection(s3, s2, bw, 10));
    }
    
    /**
     * Test method {@link ModelNodeConnectionManager#connectionExists(cz.zcu.kiv.dfs_simulator.model.ModelNode)}.
     */
    @Test public void testConnectionExists()
    {
        assertTrue(this.origin.getConnectionManager().connectionExists(this.s1));
        assertTrue(this.s1.getConnectionManager().connectionExists(this.origin));
    }
    
    /**
     * Test method {@link ModelNodeConnectionManager#getDirectServerConnections()}.
     */
    @Test public void testGetDirectServerConnections()
    {
        List<ModelNodeConnection> dc = this.origin.getConnectionManager().getDirectServerConnections();
        
        assertNotNull(dc);
        assertEquals(1, dc.size());
        assertEquals(this.s1, dc.get(0).getNeighbour());
    }
    
    /**
     * Test method {@link ModelNodeConnectionManager#getNeighbourConnection(cz.zcu.kiv.dfs_simulator.model.ModelNode)}.
     */
    @Test public void testGetNeighbourConnection()
    {
        ModelNodeConnection conn = this.origin.getConnectionManager().getNeighbourConnection(this.s1);
        assertNotNull(conn);
        assertEquals(this.origin, conn.getOrigin());
        assertEquals(this.s1, conn.getNeighbour());
    }
        
    /**
     * Test fail method {@link ModelNodeConnectionManager#ModelNodeConnectionManager(cz.zcu.kiv.dfs_simulator.model.ModelNode)}.
     */
    @Test public void testGetNeighbourConnectionFail()
    {
        ModelNodeConnection conn = this.origin.getConnectionManager().getNeighbourConnection(this.s2);
        assertNull(conn);
    }
        
    /**
     * Test method {@link ModelNodeConnectionManager#getReachableServers()}.
     */
    @Test public void testGetReachableServers()
    {        
        List<ModelServerNode> reachable = this.origin.getConnectionManager().getReachableServers();
        
        assertNotNull(reachable);
        assertEquals(3, reachable.size());
        assertTrue(reachable.contains(s1));
        assertTrue(reachable.contains(s2));
        assertTrue(reachable.contains(s3));
    }
    
}
