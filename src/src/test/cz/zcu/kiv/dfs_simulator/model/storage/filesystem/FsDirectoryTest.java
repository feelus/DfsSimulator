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
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * Test {@link FsDirectory}.
 */
public class FsDirectoryTest
{
    protected FsDirectory root;

    @Before public void setUp() throws Exception
    {
        this.root = new FsDirectory(FsDirectory.DIR_PATH_SEPARATOR);
    }

    /**
     * Test method {@link FsDirectory#getChildObject(java.lang.String)}.
     */
    @Test public void testGetChildObject()
    {
        this.root.getChildren().add(
                new FsFile("soubor", new ByteSize(1, ByteSizeUnits.GB), this.root));
        
        FileSystemObject found = this.root.getChildObject("soubor");
        
        assertNotNull(found);
        assertTrue(found instanceof FsFile);
        assertEquals("soubor", found.nameProperty().get());
    }
    
    /**
     * Test method {@link FsDirectory#getChildObject(java.lang.String)} with
     * non-direct child (child of one of this directory's child).
     */
    @Test public void testGetIndirectChildObject()
    {
        FsDirectory subdir1 = new FsDirectory("subdir", this.root);
        FsDirectory subdir2 = new FsDirectory("subdir2", subdir1);
        
        subdir1.getChildren().add(subdir2);
        this.root.getChildren().add(subdir1);
        
        FsFile file = new FsFile("soubor", new ByteSize(1, ByteSizeUnits.B), subdir2);
        
        subdir2.getChildren().add(file);
        
        FileSystemObject found = this.root.getChildObject("/subdir/subdir2/soubor");
        
        assertNotNull(found);
        assertTrue(found instanceof FsFile);
        assertEquals("soubor", found.nameProperty().get());
    }
    
    /**
     * Test method {@link FsDirectory#getFullPath()}.
     */
    @Test public void testGetFullPath()
    {
        FsDirectory mnt = new FsDirectory("mnt", this.root);
        
        this.root.getChildren().add(mnt);
        
        assertEquals("/mnt/", mnt.getFullPath());
    }
    
    /**
     * Test method {@link FsDirectory#getChildObject(java.lang.String)} with
     * child file and directory with same name - whether method can distinguish
     * between path for files and for directories (path for directory ends with an /).
     */
    @Test public void testGetDirectoryChildType()
    {
        FsDirectory dir = new FsDirectory("nazev", this.root);
        FsFile file = new FsFile("nazev", new ByteSize(1, ByteSizeUnits.GB), this.root);
        
        this.root.getChildren().addAll(dir, file);
        
        FileSystemObject foundDir = this.root.getChildObject(dir.getFullPath());
        
        assertNotNull(foundDir);
        assertTrue(foundDir instanceof FsDirectory);
        
        FileSystemObject foundFile = this.root.getChildObject(file.getFullPath());
        assertNotNull(foundFile);
        assertTrue(foundFile instanceof FsFile);
    }
    
}
