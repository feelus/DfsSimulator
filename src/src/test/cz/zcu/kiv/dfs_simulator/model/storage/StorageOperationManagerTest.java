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

package cz.zcu.kiv.dfs_simulator.model.storage;

import cz.zcu.kiv.dfs_simulator.model.ByteSize;
import cz.zcu.kiv.dfs_simulator.model.ByteSizeUnits;
import cz.zcu.kiv.dfs_simulator.model.ByteSpeed;
import cz.zcu.kiv.dfs_simulator.model.ByteSpeedUnits;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FsFile;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test {@link StorageOperationManager}.
 */
public class StorageOperationManagerTest
{
    private ServerStorage storage;
    private StorageOperationManager manager;
    
    private StorageOperation op1;
    private StorageOperation op2;
    
    @Before public void setUp()
    {
        this.storage = new ServerStorage(new ByteSize(10, ByteSizeUnits.GB), new ByteSpeed(100, ByteSpeedUnits.MBPS));
        this.manager = new StorageOperationManager(this.storage);
        
        // create fake file - does not matter if it is not mounted anywhere
        List<FsFile> transferList = new ArrayList<>();
        FsFile f = new FsFile("soubor", new ByteSize(1, ByteSizeUnits.GB), null);
        transferList.add(f);
        
        // add two read operations total without limit
        this.op1 = this.manager.addReadOperation(transferList, new StorageOperationTransferLimiter()
        {
            @Override
            public ByteSpeed getTransferLimit(long sTime)
            {
                return new ByteSpeed(100, ByteSpeedUnits.MBPS);
            }
        }, false);        
        this.op2 = this.manager.addReadOperation(transferList, new StorageOperationTransferLimiter()
        {
            @Override
            public ByteSpeed getTransferLimit(long sTime)
            {
                return new ByteSpeed(100, ByteSpeedUnits.MBPS);
            }
        }, false);
    }
    
    /**
     * Test method {@link StorageOperationManager#updateAvailableThroughput(long)}.
     */
    @Test public void testUpdateAvailableThroughput()
    {
        assertNotNull(this.op1);
        assertNotNull(this.op2);
        
        // check if their bw is not set beforehand
        assertEquals(0, this.op1.getAvailableThroughput().bpsProperty().get());
        assertEquals(0, this.op2.getAvailableThroughput().bpsProperty().get());
        
        // update throughput
        this.manager.updateAvailableThroughput(10);
        
        assertEquals(this.storage.getMaximumSpeed().bpsProperty().get() / 2, this.op1.getAvailableThroughput().bpsProperty().get());
        assertEquals(this.storage.getMaximumSpeed().bpsProperty().get() / 2, this.op2.getAvailableThroughput().bpsProperty().get());
    }

    /**
     * Test method {@link StorageOperationManager#updateTransferedSize(long, long)}.
     */
    @Test public void testUpdateTransferedSize()
    {
        assertNotNull(this.op1);
        assertNotNull(this.op2);
        
        // update throughput and transfered size (1 second)
        this.manager.updateAvailableThroughput(0);
        this.manager.updateTransferedSize(1000, 0);
        
        // check transfered size 
        // should be 50 mB/sec for each op
        ByteSize fiftyMb = new ByteSize(50, ByteSizeUnits.MB);
        
        assertEquals(fiftyMb.bytesProperty().get(), this.op1.getTransferedSize().bytesProperty().get());
        assertEquals(fiftyMb.bytesProperty().get(), this.op2.getTransferedSize().bytesProperty().get());
        
    }
}
