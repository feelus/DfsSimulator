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
 * Result of adding child object to a directory.
 */
public enum ServerFsAddChildResult
{
    /**
    * Added new file
    */
    ADDED,
    /**
     * Unable to add child
     */
    REFUSED,
    /**
     * Merged folders
     */
    MERGED;
}
