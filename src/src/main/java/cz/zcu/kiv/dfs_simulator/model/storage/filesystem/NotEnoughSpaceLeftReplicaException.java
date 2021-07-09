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
 * One of file replica's storage does not have enough space left.
 */
public class NotEnoughSpaceLeftReplicaException extends Exception
{
    public NotEnoughSpaceLeftReplicaException(String message)
    {
        super(message);
    }
}
