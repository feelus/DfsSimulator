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

package cz.zcu.kiv.dfs_simulator.model.storage.replication;

import cz.zcu.kiv.dfs_simulator.model.ModelServerNode;
import cz.zcu.kiv.dfs_simulator.model.storage.ServerStorage;
import java.util.Objects;

/**
 * Replica target.
 */
public class ReplicaTarget
{
    /**
     * Target server
     */
    public ModelServerNode serverNode;
    /**
     * Target storage device
     */
    public ServerStorage storage;
    
    /**
     * Construct replica target.
     * 
     * @param serverNode target server
     * @param storage target storage
     */
    public ReplicaTarget(ModelServerNode serverNode, ServerStorage storage)
    {
        this.serverNode = serverNode;
        this.storage = storage;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public int hashCode()
    {
        int hash = 5;
        hash = 17 * hash + Objects.hashCode(this.serverNode);
        hash = 17 * hash + Objects.hashCode(this.storage);
        return hash;
    }

    /**
     * {@inheritDoc}
     */
    @Override public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final ReplicaTarget other = (ReplicaTarget) obj;
        if (!Objects.equals(this.serverNode, other.serverNode))
        {
            return false;
        }
        return Objects.equals(this.storage, other.storage);
    }
    
}
