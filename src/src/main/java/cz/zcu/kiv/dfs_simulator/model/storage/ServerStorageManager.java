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

import cz.zcu.kiv.dfs_simulator.persistence.InvalidPersistedStateException;
import cz.zcu.kiv.dfs_simulator.persistence.StatePersistable;
import cz.zcu.kiv.dfs_simulator.persistence.StatePersistableElement;
import cz.zcu.kiv.dfs_simulator.persistence.StatePersistenceLogger;
import cz.zcu.kiv.dfs_simulator.model.ByteSize;
import cz.zcu.kiv.dfs_simulator.model.ByteSpeed;
import cz.zcu.kiv.dfs_simulator.model.ModelNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Server storage manager.
 */
public class ServerStorageManager implements StatePersistable
{
    /**
     * Persistable identificator
     */
    protected static final String PERSISTABLE_NAME = "server_storage";
    
    /**
     * Managed node
     */
    protected final ModelNode node;
    /**
     * List of storage devices
     */
    protected final List<ServerStorage> storage = new ArrayList<>();
    
    /**
     * Construct storage manager for {@link ModelNode} {@code node}.
     * 
     * @param node node
     */
    public ServerStorageManager(ModelNode node)
    {
        this.node = node;
    }
    
    /**
     * Update currently available throughput of all operations on all storages
     * of this server. 
     * 
     * @param sTime current simulation time
     */
    public void updateStorageAvailableThroughput(long sTime)
    {
        for(ServerStorage s : this.storage)
        {
            s.getOperationManager().updateAvailableThroughput(sTime);
        }
    }
    
    /**
     * Update transfered size for all operations on all storages
     * in interval beginning at {@code sTime} and ending at {@code sTime} + {@code timeInterval}.
     * 
     * @param timeInterval interval length
     * @param sTime interval start
     */
    public void updateStorageTransferedSize(long timeInterval, long sTime)
    {
        for(ServerStorage s : this.storage)
        {
            s.getOperationManager().updateTransferedSize(timeInterval, sTime);
        }
    }
    
    /**
     * Force finish all currently running and pending operations on all storages.
     * 
     * @param sTime current time
     */
    public void finishStorageOperations(long sTime)
    {
        for(ServerStorage s : this.storage)
        {
            s.getOperationManager().finish(sTime);
        }
    }
    
    /**
     * Return all storage devices of this server.
     * 
     * @return storage devices
     */
    public List<ServerStorage> getStorage()
    {
        return this.storage;
    }
    
    /**
     * Get storage by it's id
     * 
     * @param storageID storage id
     * @return an instance of {@link ServerStorage} or null
     */
    public ServerStorage getStorageByID(String storageID)
    {
        Optional<ServerStorage> stor = this.storage.stream().filter(
                x -> x.idProperty().get().equals(storageID)).findFirst();
        
        if(stor != null && stor.isPresent())
        {
            return stor.get();
        }
        
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override public List<StatePersistable> getPersistableChildren()
    {
        List<StatePersistable> l = new ArrayList<>();
        l.addAll(this.storage);
        
        return l;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public StatePersistableElement export(StatePersistenceLogger logger)
    {
        return new StatePersistableElement(this.getPersistableName());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public void restoreState(StatePersistableElement state, StatePersistenceLogger logger, Object... args) throws InvalidPersistedStateException
    {
        if(state != null && state.getName().equals(PERSISTABLE_NAME))
        {
            for(StatePersistableElement childElem : state.getElements())
            {
                if(childElem.getName().equals(ServerStorage.PERSISTABLE_NAME))
                {
                    ServerStorage stor = new ServerStorage(new ByteSize(0), new ByteSpeed(0));
                    stor.restoreState(childElem, logger);
                    
                    this.storage.add(stor);
                }
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public String getPersistableName()
    {
        return PERSISTABLE_NAME;
    }
}
