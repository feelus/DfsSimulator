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
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test {@link FsFile}.
 */
public class FsFileTest
{
    /**
     * Test method {@link FsFile#
     */
    @Test public void testGetFullPath()
    {
        FsDirectory root = new FsDirectory(FsDirectory.DIR_PATH_SEPARATOR);
        FsDirectory mnt = new FsDirectory("mnt", root);
        FsDirectory data = new FsDirectory("data", mnt);
        
        FsFile file = new FsFile("soubor", new ByteSize(1), data);
        
        // not really necessary for this test
        root.getChildren().add(mnt);
        mnt.getChildren().add(data);
        data.getChildren().add(file);
        
        assertEquals("/mnt/data/soubor", file.getFullPath());
    }
    
    /**
     * Test method {@link FsFile#setSize(cz.zcu.kiv.dfs_simulator.model.ByteSize)}.
     */
    @Test public void testSetSize()
    {
        FsFile file = new FsFile("soubor", new ByteSize(1), null);
        
        assertEquals(1, file.getSize().bytesProperty().get());
        file.setSize(new ByteSize(2));
        assertEquals(2, file.getSize().bytesProperty().get());
    }
    
    /**
     * Test method {@link FsFile#getAccessCount()}.
     */
    @Test public void testGetAccessCount()
    {
        FsFile file = new FsFile("soubor", new ByteSize(1), null);
        
        assertEquals(0, file.getAccessCount());
        
        file.incrementAccessCounter();
        assertEquals(1, file.getAccessCount());
    }
    
}
