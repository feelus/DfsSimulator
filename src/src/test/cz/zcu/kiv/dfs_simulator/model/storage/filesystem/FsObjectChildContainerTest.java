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

package cz.zcu.kiv.dfs_simulator.model.storage.filesystem;

import cz.zcu.kiv.dfs_simulator.model.ByteSize;
import cz.zcu.kiv.dfs_simulator.model.ByteSizeUnits;
import cz.zcu.kiv.dfs_simulator.model.ByteSpeed;
import cz.zcu.kiv.dfs_simulator.model.ByteSpeedUnits;
import cz.zcu.kiv.dfs_simulator.model.ModelServerNode;
import cz.zcu.kiv.dfs_simulator.model.storage.ServerStorage;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test {@link FsObjectChildContainer}.
 */
public class FsObjectChildContainerTest
{
    protected ModelServerNode node;
    
    @Before public void setUp()
    {
        this.node = new ModelServerNode();
    }
    
    /**
     * Test directory mount size (uses {@link FsObjectChildContainer}).
     * 
     * @throws NotEnoughSpaceLeftException when storage didn't have enough space for test object
     */
    @Test public void testDirectoryMountSize1() throws NotEnoughSpaceLeftException
    {
        ServerStorage stor1 = new ServerStorage(new ByteSize(100, ByteSizeUnits.GB), new ByteSpeed(200, ByteSpeedUnits.MBPS));
        ServerStorage stor2 = new ServerStorage(new ByteSize(200, ByteSizeUnits.GB), new ByteSpeed(200, ByteSpeedUnits.MBPS));
        
        this.node.getStorageManager().getStorage().add(stor1);
        this.node.getStorageManager().getStorage().add(stor2);
        
        FsDirectory d1 = new FsDirectory("dir1", this.node.getRootDir());
        FsDirectory d2 = new FsDirectory("dir2", this.node.getRootDir());
        
        // node -> d1 -> d2
        this.node.getRootDir().getChildren().add(d1);
        d1.getChildren().add(d2);
        
        // mount stor1 onto root
        this.node.getFsManager().mount(stor1, this.node.getRootDir());
        // mount stor2 onto d2
        this.node.getFsManager().mount(stor2, d2);
        
        // add 10GB file into d1
        FsFile f1 = new FsFile("file1", new ByteSize(10, ByteSizeUnits.GB), d1);
        d1.getChildren().add(f1);
        
        // add 20GB file into d2
        FsFile f2 = new FsFile("file2", new ByteSize(20, ByteSizeUnits.GB), d2);
        d2.getChildren().add(f2);
        
        assertEquals(10, d1.getMountSize().gigaBytesProperty().get(), 0.0);
        assertEquals(20, d2.getMountSize().gigaBytesProperty().get(), 0.0);
    }
    
    /**
     * Test directory mount size (uses {@link FsObjectChildContainer}).
     * 
     * @throws NotEnoughSpaceLeftException when storage didn't have enough space for test object
     */
    @Test public void testDirectoryMountSize2() throws NotEnoughSpaceLeftException
    {
        ServerStorage stor1 = new ServerStorage(new ByteSize(100, ByteSizeUnits.GB), new ByteSpeed(200, ByteSpeedUnits.MBPS));
        ServerStorage stor2 = new ServerStorage(new ByteSize(200, ByteSizeUnits.GB), new ByteSpeed(200, ByteSpeedUnits.MBPS));
        
        this.node.getStorageManager().getStorage().add(stor1);
        this.node.getStorageManager().getStorage().add(stor2);
        
        FsDirectory d1 = new FsDirectory("dir1", this.node.getRootDir());
        FsDirectory d2 = new FsDirectory("dir2", this.node.getRootDir());
        
        // node -> d1 -> d2
        this.node.getRootDir().getChildren().add(d1);
        d1.getChildren().add(d2);
        
        // mount stor1 onto root
        this.node.getFsManager().mount(stor1, this.node.getRootDir());
        // mount stor2 onto d2
        this.node.getFsManager().mount(stor2, d2);
        
        FsFile f1 = new FsFile("file1", new ByteSize(1, ByteSizeUnits.GB), d1);
        FsFile f2 = new FsFile("file2", new ByteSize(2, ByteSizeUnits.GB), d1);
        FsFile f3 = new FsFile("file3", new ByteSize(4, ByteSizeUnits.GB), d1);
        
        FsFile f4 = new FsFile("file4", new ByteSize(8, ByteSizeUnits.GB), d2);
        FsFile f5 = new FsFile("file5", new ByteSize(16, ByteSizeUnits.GB), d2);
        
        // try adding multiple at once
        d1.getChildren().addAll(f1, f2, f3);
        d2.getChildren().addAll(f4, f5);
        
        assertEquals(7.0, d1.getMountSize().gigaBytesProperty().get(), 0.0);
        assertEquals(24.0, d2.getMountSize().gigaBytesProperty().get(), 0.0);
        
        // add 10GB to d1
        f2.getSize().setGigaBytes(12);
        
        assertEquals(17.0, d1.getMountSize().gigaBytesProperty().get(), 0.0);
    }
    
    /**
     * Test directory mount size (uses {@link FsObjectChildContainer}) after
     * files have been umounted - if size gets updated correctly.
     * 
     * @throws NotEnoughSpaceLeftException when storage didn't have enough space for test object
     */
    @Test public void testDirectoryMountSizeUmount() throws NotEnoughSpaceLeftException
    {
        ServerStorage stor1 = new ServerStorage(new ByteSize(100, ByteSizeUnits.GB), new ByteSpeed(200, ByteSpeedUnits.MBPS));
        ServerStorage stor2 = new ServerStorage(new ByteSize(200, ByteSizeUnits.GB), new ByteSpeed(200, ByteSpeedUnits.MBPS));
        
        this.node.getStorageManager().getStorage().add(stor1);
        this.node.getStorageManager().getStorage().add(stor2);
        
        FsDirectory d1 = new FsDirectory("dir1", this.node.getRootDir());
        FsDirectory d2 = new FsDirectory("dir2", this.node.getRootDir());
        
        // node -> d1 -> d2
        this.node.getRootDir().getChildren().add(d1);
        d1.getChildren().add(d2);
        
        // mount stor1 onto root
        this.node.getFsManager().mount(stor1, this.node.getRootDir());
        // mount stor2 onto d2
        this.node.getFsManager().mount(stor2, d2);
        
        // add 10GB file into d1
        FsFile f1 = new FsFile("file1", new ByteSize(10, ByteSizeUnits.GB), d1);
        d1.getChildren().add(f1);
        
        // add 20GB file into d2
        FsFile f2 = new FsFile("file2", new ByteSize(20, ByteSizeUnits.GB), d2);
        d2.getChildren().add(f2);
        
        // umount d2
        this.node.getFsManager().umount(d2);
        
        assertEquals(30.0, d1.getMountSize().gigaBytesProperty().get(), 0.0);
    }
    
}
