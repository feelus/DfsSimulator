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

/**
 * Type of {@link FileSystemObject}.
 */
public enum FsObjectType
{
    /**
     * Directory
     */
    DIRECTORY("Directory"),
    
    /**
     * File
     */
    FILE("File");
    
    /**
     * Textual representation of type
     */
    private final String name;
    
    /**
     * Construct type with name.
     * 
     * @param name name
     */
    private FsObjectType(String name)
    {
        this.name = name;
    }
    
    /**
     * Textual representation of type.
     * 
     * @return type
     */
    @Override public String toString()
    {
        return this.name;
    }
}
