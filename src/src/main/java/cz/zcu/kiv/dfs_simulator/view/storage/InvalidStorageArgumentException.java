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

package cz.zcu.kiv.dfs_simulator.view.storage;

/**
 * Thrown when storage has invalid parameters.
 */
public class InvalidStorageArgumentException extends Exception
{
    public InvalidStorageArgumentException(String exception)
    {
        super(exception);
    }
}
