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
 * Test {@link ServerFileSystemManager}.
 */
public class ServerFsManagerTest
{
    private ServerFileSystemManager manager;
    private ModelServerNode server;
    
    @Before public void setUp()
    {
        this.server = new ModelServerNode();
        this.manager = new ServerFileSystemManager(this.server);
    }
    
    /**
     * Test method {@link ServerFileSystemManager#getStorageUsedSize(cz.zcu.kiv.dfs_simulator.model.storage.ServerStorage)}.
     * 
     * @throws NotEnoughSpaceLeftException when storage does not have enough space for test object
     */
    @Test public void testGetStorageUsedSize() throws NotEnoughSpaceLeftException
    {
        ServerStorage stor1 = new ServerStorage(new ByteSize(10, ByteSizeUnits.GB), new ByteSpeed(200, ByteSpeedUnits.MBPS));
        ServerStorage stor2 = new ServerStorage(new ByteSize(10, ByteSizeUnits.GB), new ByteSpeed(200, ByteSpeedUnits.MBPS));
        
        FsDirectory root = new FsDirectory("/", null);
        FsDirectory subdir = new FsDirectory("subdir", root);
        
        root.getChildren().add(subdir);
        
        // place a 1GB file into root
        FsFile fRoot = new FsFile("rootfile", new ByteSize(1, ByteSizeUnits.GB), root);
        root.getChildren().add(fRoot);
        
        // place a 5GB file into subidr
        FsFile fSubdir = new FsFile("subdirfile", new ByteSize(5, ByteSizeUnits.GB), subdir);
        subdir.getChildren().add(fSubdir);
        
        // mount stor1 onto root
        this.manager.mount(stor1, root);
        // mount stor2 into subidr
        this.manager.mount(stor2, subdir);
        
        // expected used size of stor1 is 1GB
        assertEquals(1.0, this.manager.getStorageUsedSize(stor1).gigaBytesProperty().get(), 0.0);
        // expected used size of stor2 is 5GB
        assertEquals(5.0, this.manager.getStorageUsedSize(stor2).gigaBytesProperty().get(), 0.0);
    }
    
    /**
     * Test method {@link ServerFileSystemManager#addDirectoryChild(
     * cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FsDirectory, 
     * cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FileSystemObject)}.
     * 
     * @throws NotEnoughSpaceLeftException when storage does not have enough space for test object
     */
    @Test public void testAddDirectoryChild() throws NotEnoughSpaceLeftException
    {
        FsDirectory d1 = new FsDirectory("test", this.server.getRootDir());
        
        assertNull(this.server.getRootDir().getChildObject("test/"));
        
        this.manager.addDirectoryChild(this.server.getRootDir(), d1);
        assertNotNull(this.server.getRootDir().getChildObject("test/"));
    }
    
    /**
     * Test method {@link ServerFileSystemManager#addDirectoryChild(
     * cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FsDirectory, 
     * cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FileSystemObject)}.
     * This test expects thrown exception since storage does not have enough space.
     * 
     * @throws NotEnoughSpaceLeftException should be thrown since storage does not have enough
     * space available
     */
    @Test(expected=NotEnoughSpaceLeftException.class) public void testAddDirectoryChildFail() throws NotEnoughSpaceLeftException
    {
        ServerStorage st1 = new ServerStorage(new ByteSize(1, ByteSizeUnits.GB), new ByteSpeed(100, ByteSpeedUnits.MBPS));
        this.manager.mount(st1, this.server.getRootDir());
        
        FsFile f1 = new FsFile("x", new ByteSize(10, ByteSizeUnits.GB), this.server.getRootDir());
        
        this.manager.addDirectoryChild(this.server.getRootDir(), f1);
    }
    
    /**
     * Test method {@link ServerFileSystemManager#mount(
     * cz.zcu.kiv.dfs_simulator.model.storage.ServerStorage, 
     * cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FileSystemObject)}.
     * 
     * @throws NotEnoughSpaceLeftException when storage does not have enough space for test object
     */
    @Test public void testMount() throws NotEnoughSpaceLeftException
    {
        assertNull(this.manager.getFsObjectMountDevice(this.server.getRootDir()));
        
        ServerStorage st1 = new ServerStorage(new ByteSize(1, ByteSizeUnits.GB), new ByteSpeed(100, ByteSpeedUnits.MBPS));
        this.manager.mount(st1, this.server.getRootDir());
        
        assertEquals(st1, this.manager.getFsObjectMountDevice(this.server.getRootDir()));
    }
    
    /**
     * Test method fail {@link ServerFileSystemManager#getFsObjectMountDevice(cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FileSystemObject)}.
     * Should fail, since object isn't mounted.
     */
    @Test public void testGetFsObjectMountDeviceFail()
    {
        assertNull(this.manager.getFsObjectMountDevice(this.server.getRootDir()));
    }
    
    /**
     * Test method {@link ServerFileSystemManager#canFileFitStorage(
     * cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FsFile, 
     * cz.zcu.kiv.dfs_simulator.model.ByteSize)}.
     * 
     * @throws NotEnoughSpaceLeftException when storage does not have enough space for test object
     */
    @Test public void testCanFileFitStorage() throws NotEnoughSpaceLeftException
    {
        ServerStorage st1 = new ServerStorage(new ByteSize(1, ByteSizeUnits.GB), new ByteSpeed(100, ByteSpeedUnits.MBPS));
        
        FsFile f1 = new FsFile("test", new ByteSize(1), null);
        this.manager.mount(st1, f1);
        
        assertTrue(this.manager.canFileFitStorage(f1, new ByteSize(1, ByteSizeUnits.GB)));
    }
    
    /**
     * Test method fail {@link ServerFileSystemManager#canFileFitStorage(
     * cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FsFile, 
     * cz.zcu.kiv.dfs_simulator.model.ByteSize)}.
     * 
     * @throws NotEnoughSpaceLeftException when storage does not have enough space for test object
     */
    @Test public void testCanFileFitStorageFail() throws NotEnoughSpaceLeftException
    {
        ServerStorage st1 = new ServerStorage(new ByteSize(1, ByteSizeUnits.GB), new ByteSpeed(100, ByteSpeedUnits.MBPS));
        
        FsFile f1 = new FsFile("test", new ByteSize(1), null);
        this.manager.mount(st1, f1);
        
        assertFalse(this.manager.canFileFitStorage(f1, new ByteSize(10, ByteSizeUnits.GB)));
    }
}
