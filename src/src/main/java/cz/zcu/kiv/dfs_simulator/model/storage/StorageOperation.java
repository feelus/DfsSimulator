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

import cz.zcu.kiv.dfs_simulator.model.ByteSize;
import cz.zcu.kiv.dfs_simulator.model.ByteSizeUnits;
import cz.zcu.kiv.dfs_simulator.model.ByteSpeed;
import cz.zcu.kiv.dfs_simulator.model.ByteSpeedUnits;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FsFile;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;


/**
 * Storage I/O operation.
 */
public class StorageOperation
{
    /**
     * Storage
     */
    private final ServerStorage storage;
    
    /**
     * Operation type
     */
    private final StorageOperationType type;
    
    /**
     * List of transfered files during this operation
     */
    private final List<FsFile> transferList;
    /**
     * Storage operation maximum transfer speed limiter
     */
    private final StorageOperationTransferLimiter limiter;
    
    /**
     * Total transfer size
     */
    private final ByteSize totalSize;
    /**
     * Amount of transfered size (progress)
     */
    private final ByteSize transferedSize = new ByteSize(0);
    /**
     * Maximum available throughput for this operation at this moment
     */
    private final ByteSpeed availableThroughput = new ByteSpeed(0);
    
    /**
     * Operations that will begin processing after this one
     */
    private final ArrayList<StorageOperation> followingOperations = new ArrayList<>();
    /**
     * Operation pending flag
     */
    private final BooleanProperty pending;
    
    /**
     * Linked operation (operation that is running on other storage and is linked
     * to this - eg. reading from this storage and writing to other storage)
     */
    private StorageOperation linkedOperation;
    
    /**
     * Callback that will be executed when this operation is finished
     */
    private final StorageOperationCallback callback;
    
    /**
     * Operation constructor.
     * 
     * @param storage storage on which this operation belongs
     * @param type type of operation
     * @param transferList transfered files
     * @param limiter operation transfer speed limiter
     * @param callback callback that is executed after this operation finishes
     * @param linkedOperation linked operation
     * @param pending if operation should start in a pending state
     */
    public StorageOperation(ServerStorage storage, StorageOperationType type, List<FsFile> transferList, StorageOperationTransferLimiter limiter, StorageOperationCallback callback, StorageOperation linkedOperation, boolean pending)
    {
        this.storage = storage;
        this.type = type;
        this.transferList = transferList;
        this.limiter = limiter;
        this.linkedOperation = linkedOperation;
        
        this.totalSize = new ByteSize(transferList.stream().
                mapToLong(tl -> tl.getSize().bytesProperty().get()).sum(), ByteSizeUnits.B);
        
        this.callback = callback;
        this.pending = new SimpleBooleanProperty(pending);
    }
    
    /**
     * Operation constructor.
     * 
     * @param storage storage on which this operation belongs
     * @param type type of operation
     * @param transferList transfered files
     * @param limiter operation transfer speed limiter
     * @param callback callback that is executed after this operation finishes
     * @param pending if operation should start in a pending state
     */
    public StorageOperation(ServerStorage storage, StorageOperationType type, List<FsFile> transferList, StorageOperationTransferLimiter limiter, StorageOperationCallback callback, boolean pending)
    {
        this(storage, type, transferList, limiter, callback, null, pending);
    }
    
    /**
     * Operation constructor.
     * 
     * @param storage storage on which this operation belongs
     * @param type type of operation
     * @param transferList transfered files
     * @param limiter operation transfer speed limiter
     * @param linkedOperation linked operation
     * @param pending if operation should start in a pending state
     */
    public StorageOperation(ServerStorage storage, StorageOperationType type, List<FsFile> transferList, StorageOperationTransferLimiter limiter, StorageOperation linkedOperation, boolean pending)
    {
        this(storage, type, transferList, limiter, null, linkedOperation, pending);
    }
    
    /**
     * Operation constructor.
     * 
     * @param storage storage on which this operation belongs
     * @param type type of operation
     * @param transferList transfered files
     * @param limiter operation transfer speed limiter
     * @param pending if operation should start in a pending state
     */
    public StorageOperation(ServerStorage storage, StorageOperationType type, List<FsFile> transferList, StorageOperationTransferLimiter limiter, boolean pending)
    {
        this(storage, type, transferList, limiter, null, null, pending);
    }
    
    /**
     * Add following operation. Operation should be picked up by {@link StorageOperationManager}
     * and transfered to processing queue after this operation finishes.
     * 
     * @param followingOperation following operation
     */
    public void addFollowingOperation(StorageOperation followingOperation)
    {
        this.followingOperations.add(followingOperation);
    }

    /**
     * Get operation type.
     * 
     * @return operation type
     */
    public StorageOperationType getType()
    {
        return type;
    }

    /**
     * Get transfered files.
     * 
     * @return transfered files
     */
    public List<FsFile> getTransferList()
    {
        return transferList;
    }
    
    /**
     * Get the maximum transfer speed this operation can run at. Speed is a minimum
     * from operation transfer limiter of this and linked operation.
     * 
     * @param sTime current simulation time
     * @return maximum transfer speed
     */
    public ByteSpeed getMaxTransferThroughput(long sTime)
    {
        if(this.linkedOperation != null)
        {
            long minBps = Math.min(this.linkedOperation.limiter.getTransferLimit(sTime).bpsProperty().get(), this.limiter.getTransferLimit(sTime).bpsProperty().get());
            
            return new ByteSpeed(minBps, ByteSpeedUnits.BPS);
        }
        
        return new ByteSpeed(this.limiter.getTransferLimit(sTime).bpsProperty().get(), ByteSpeedUnits.BPS);
    }

    /**
     * Get total size of transfered files.
     * 
     * @return total size
     */
    public ByteSize getTotalSize()
    {
        return totalSize;
    }

    /**
     * Get amount of already transfered data.
     * 
     * @return amount of already transfered data
     */
    public ByteSize getTransferedSize()
    {
        return transferedSize;
    }
    
    /**
     * Set linked operation.
     * 
     * @param linkedOperation linked operation
     */
    public void setLinkedOperation(StorageOperation linkedOperation)
    {
        this.linkedOperation = linkedOperation;
    }
    
    /**
     * Set currently available throughput.
     * 
     * @param throughput currently available throughput
     */
    public void setAvailableThroughput(ByteSpeed throughput)
    {
        this.availableThroughput.setBps(throughput.bpsProperty().get());
    }
    
    /**
     * Get currently available throughput. Throughput is minimum from
     * this operation's and linked operation's available throughput.
     * 
     * @return currently available throughput
     */
    public ByteSpeed getAvailableThroughput()
    {
        if(this.linkedOperation != null)
        {
            long minBps = Math.min(this.linkedOperation.availableThroughput.bpsProperty().get(), this.availableThroughput.bpsProperty().get());
            
            return new ByteSpeed(minBps, ByteSpeedUnits.BPS);
        }
        
        return new ByteSpeed(this.availableThroughput.bpsProperty().get(), ByteSpeedUnits.BPS);
    }
    
    /**
     * Get operation callback.
     * 
     * @return operation callback
     */
    public StorageOperationCallback getCallback()
    {
        return this.callback;
    }

    /**
     * Get pending operations.
     * 
     * @return pending operations
     */
    public ArrayList<StorageOperation> getPendingOperations()
    {
        return this.followingOperations;
    }
    
    /**
     * Get writable {@link BooleanProperty} pending flag.
     * 
     * @return pending flag
     */
    public BooleanProperty pendingProperty()
    {
        return this.pending;
    }
    
    /**
     * Checks if this operation is in pending state.
     * 
     * @return pending state status
     */
    public boolean isPending()
    {
        return this.pending.get();
    }
    
    /**
     * Set operation pending state flag.
     * 
     * @param pending pending state flag
     */
    public void setPending(boolean pending)
    {
        this.pending.set(pending);
    }
    
    /**
     * Remove itself from parent storage. 
     */
    public void removeUnmanaged()
    {
        if(this.storage != null)
        {
            this.storage.getOperationManager().removeUnmanagedOperation(this);
        }
    }
    
}
