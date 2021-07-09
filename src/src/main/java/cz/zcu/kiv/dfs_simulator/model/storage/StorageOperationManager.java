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

import cz.zcu.kiv.dfs_simulator.helpers.Pair;
import cz.zcu.kiv.dfs_simulator.model.ByteSize;
import cz.zcu.kiv.dfs_simulator.model.ByteSizeUnits;
import cz.zcu.kiv.dfs_simulator.model.ByteSpeed;
import cz.zcu.kiv.dfs_simulator.model.ByteSpeedUnits;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FsFile;
import cz.zcu.kiv.dfs_simulator.simulation.path.DfsPath;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

/**
 * Manager of storage operations of {@link ServerStorage}.
 */
public class StorageOperationManager
{
    /**
     * Managed storage
     */
    private final ServerStorage storage;
    
    /**
     * Currently running operations
     */
    private final List<StorageOperation> runningOperations = new ArrayList<>();
    /**
     * Operations ready to be switched to running state
     */
    private final List<StorageOperation> preparedOperations = new ArrayList<>();
    /**
     * Currently pending operations
     */
    private final List<StorageOperation> pendingOperations = new ArrayList<>();

    /**
     * Operations to be processed (switched either to running or prepared state)
     */
    private final Queue<StorageOperation> addedOperations = new ArrayDeque<>();
    
    /**
     * Synchronization monitor when updating operations
     */
    private final Object updateMonitor = new Object();
    /**
     * Whether we are currently updating operations
     */
    private boolean updateInProgress = false;
    
    /**
     * Space reserved for pending write operations (so that no other
     * operation can use this reserved space before pending operation
     * is switched to running state)
     */
    private final ByteSize reservedSpace = new ByteSize(0);
    
    /**
     * Storage operation constructor. 
     * 
     * @param storage managed storage
     */
    public StorageOperationManager(ServerStorage storage)
    {
        this.storage = storage;
    }
    
    /**
     * Adds new replication operation when moving files from {@code source}
     * to this manager's storage. New write operation is created on this storage
     * and new read operation is created on source storage.
     * 
     * @param transferList transfered files
     * @param source source storage
     * @param path path used for data transfer
     * @param callback operation callback (called only once from write operation)
     * @param pending if operation should be created in pending status
     * @return pair of created operations where first is write operation 
     * to this storage and second is read operation from source storage
     */
    public Pair<StorageOperation, StorageOperation> addReplicationOperation(List<FsFile> transferList, ServerStorage source, DfsPath path, StorageOperationCallback callback, boolean pending)
    {
        StorageOperation writeOp = this.addWriteOperation(transferList, new StorageOperationTransferLimiter()
        {
            @Override public ByteSpeed getTransferLimit(long sTime)
            {
                return path.getCurrentLinkBandwidth(sTime);
            }
        }, callback, true, pending);
        
        StorageOperation readOp = source.getOperationManager().addReadOperation(transferList, new StorageOperationTransferLimiter()
        {
            @Override public ByteSpeed getTransferLimit(long sTime)
            {
                return path.getCurrentLinkBandwidth(sTime);
            }
        }, pending);
        
        // link operations together
        readOp.setLinkedOperation(writeOp);
        writeOp.setLinkedOperation(readOp);
        
        return new Pair<>(writeOp, readOp);
    }    
    
    /**
     * Adds new replication operation when moving files from {@code source}
     * to this manager's storage. New write operation is created on this storage
     * and new read operation is created on source storage.
     * 
     * @param transferList transfered files
     * @param source source storage
     * @param callback operation callback (called only once from write operation)
     * @param autoReserveSpace if space should be reserved on this storage
     * @param pending if operation should be created in pending status
     * @return pair of created operations where first is write operation 
     * to this storage and second is read operation from source storage
     */
    public Pair<StorageOperation, StorageOperation> addMigrationOperation(List<FsFile> transferList, ServerStorage source, StorageOperationCallback callback, boolean autoReserveSpace, boolean pending)
    {
        long maxBps = Math.min(this.storage.getMaximumSpeed().bpsProperty().get(), source.getMaximumSpeed().bpsProperty().get());
        ByteSpeed maxSpeed = new ByteSpeed(maxBps, ByteSpeedUnits.BPS);
        
        StorageOperation writeOp = this.addWriteOperation(transferList, new StorageOperationTransferLimiter()
        {
            @Override public ByteSpeed getTransferLimit(long sTime)
            {
                return maxSpeed;
            }
        }, callback, autoReserveSpace, pending);
        
        StorageOperation readOp = source.getOperationManager().addReadOperation(transferList, new StorageOperationTransferLimiter()
        {
            @Override public ByteSpeed getTransferLimit(long sTime)
            {
                return maxSpeed;
            }
        }, pending);
        
        // link operations together
        readOp.setLinkedOperation(writeOp);
        writeOp.setLinkedOperation(readOp);
        
        return new Pair<>(writeOp, readOp);
    }
    
    /**
     * Remove operation from either running or pending list.
     * 
     * @param operation operation to be removed
     */
    public void removeUnmanagedOperation(StorageOperation operation)
    {
        if(operation.getType().isWriting())
        {
            this.removeSpaceReservation(operation.getTotalSize());
        }
        
        if(operation.isPending())
        {
            this.pendingOperations.remove(operation);
        }
        else
        {
            this.runningOperations.remove(operation);
            this.preparedOperations.remove(operation);
        }
    }
    
    /**
     * Add unmanaged (operation's transfered size won't be updated) read operation. 
     * 
     * @param transferList transfer list
     * @param limiter operation transfer speed limiter
     * @param callback operation callback
     * @return created operation
     */
    public StorageOperation addUnmanagedReadOperation(List<FsFile> transferList, StorageOperationTransferLimiter limiter, StorageOperationCallback callback)
    {
        return this.addOperation(StorageOperationType.CLIENT_READ, transferList, limiter, callback, false);
    }
    
    /**
     * Add unmanaged (operation's transfered size won't be updated) read operation. 
     * 
     * @param transferList transfer list
     * @param limiter operation transfer speed limiter
     * @return created operation
     */
    public StorageOperation addUnmanagedReadOperation(List<FsFile> transferList, StorageOperationTransferLimiter limiter)
    {
        return this.addUnmanagedReadOperation(transferList, limiter, null);
    }
    
    /**
     * Add unmanaged (operation's transfered size won't be updated) read operation. 
     * 
     * @param transferList transfer list
     * @param limiter operation transfer speed limiter
     * @param callback operation callback
     * @param autoReserveSpace if space should be reserved
     * @return created operation
     */
    public StorageOperation addUnmanagedWriteOperation(List<FsFile> transferList, StorageOperationTransferLimiter limiter, StorageOperationCallback callback, boolean autoReserveSpace)
    {
        StorageOperation op = this.addOperation(StorageOperationType.CLIENT_WRITE, transferList, limiter, callback, false);
        
        if(autoReserveSpace)
        {
            this.reserveSpace(op.getTotalSize());
        }
        
        return op;
    }
          
    /**
     * Add unmanaged (operation's transfered size won't be updated) read operation. 
     * 
     * @param transferList transfer list
     * @param limiter operation transfer speed limiter
     * @param autoReserveSpace if space should be reserved
     * @return created operation
     */
    public StorageOperation addUnmanagedWriteOperation(List<FsFile> transferList, StorageOperationTransferLimiter limiter, boolean autoReserveSpace)
    {
        return this.addUnmanagedWriteOperation(transferList, limiter, null, autoReserveSpace);
    }
    
    /**
     * Add read operation.
     * 
     * @param transferList transfer list
     * @param limiter operation transfer speed limiter
     * @param callback operation callback
     * @param pending if operation should start as pending
     * @return created operation
     */
    public StorageOperation addReadOperation(List<FsFile> transferList, StorageOperationTransferLimiter limiter, StorageOperationCallback callback, boolean pending)
    {
        return this.addOperation(StorageOperationType.READ, transferList, limiter, callback, pending);
    }
    
    /**
     * Add read operation.
     * 
     * @param transferList transfer list
     * @param limiter operation transfer speed limiter
     * @param pending if operation should start as pending
     * @return created operation
     */
    public StorageOperation addReadOperation(List<FsFile> transferList, StorageOperationTransferLimiter limiter, boolean pending)
    {
        return this.addReadOperation(transferList, limiter, null, pending);
    }
    
    /**
     * Add write operation.
     * 
     * @param transferList transfer list
     * @param limiter operation transfer speed limiter
     * @param callback operation callback
     * @param autoReserveSpace if space should be reserved
     * @param pending if operation should start as pending
     * @return created operation
     */
    public StorageOperation addWriteOperation(List<FsFile> transferList, StorageOperationTransferLimiter limiter, StorageOperationCallback callback, boolean autoReserveSpace, boolean pending)
    {
        StorageOperation op = this.addOperation(StorageOperationType.WRITE, transferList, limiter, callback, pending);
        
        if(autoReserveSpace)
        {
            this.reserveSpace(op.getTotalSize());
        }
        
        return op;
    }
    
    /**
     * Add write operation.
     * 
     * @param transferList transfer list
     * @param limiter operation transfer speed limiter
     * @param autoReserveSpace if space should be reserved
     * @param pending if operation should start as pending
     * @return created operation
     */
    public StorageOperation addWriteOperation(List<FsFile> transferList, StorageOperationTransferLimiter limiter, boolean autoReserveSpace, boolean pending)
    {
        return this.addWriteOperation(transferList, limiter, null, true, pending);
    }
    
    /**
     * Add operation to storage.
     * 
     * @param type operation type
     * @param transferList transfer list
     * @param limiter operation transfer speed limiter
     * @param callback operation callback
     * @param pending if operation should start as pending
     * @return 
     */
    private StorageOperation addOperation(StorageOperationType type, List<FsFile> transferList, StorageOperationTransferLimiter limiter, StorageOperationCallback callback, boolean pending)
    {
        StorageOperation opProg = new StorageOperation(this.storage, type, transferList, limiter, callback, pending);
        
        synchronized(this.updateMonitor)
        {
            if(!this.updateInProgress)
            {
                this.addOperationToQueue(opProg);
            }
            else
            {
                this.addedOperations.add(opProg);
            }
        }
        
        return opProg;
    }
    
    /**
     * Add operation to queue with operations waiting to be processed.
     * 
     * @param operation operation
     */
    private void addOperationToQueue(StorageOperation operation)
    {
        if(operation.isPending())
        {
            this.pendingOperations.add(operation);
        }
        else
        {
            this.preparedOperations.add(operation);
        }
    }
    
    /**
     * Update all queues with operations. 
     * 
     * @param sTime simulation time
     */
    private void updateOperationQueues(long sTime)
    {
        // transfer operations that are not pending anymore to prepared queue
        Iterator<StorageOperation> pendingIterator = this.pendingOperations.iterator();
        
        while(pendingIterator.hasNext())
        {
            StorageOperation op = pendingIterator.next();
            
            if(!op.isPending())
            {
                this.preparedOperations.add(op);
                pendingIterator.remove();
            }
        }
        
        // begin operations that are prepared
        Iterator<StorageOperation> preparedIterator = this.preparedOperations.iterator();
        
        while(preparedIterator.hasNext())
        {
            StorageOperation op = preparedIterator.next();
            this.runningOperations.add(op);
            
            StorageOperationCallback callback = op.getCallback();
            
            if(callback != null)
            {
                callback.onOperationStarted(sTime);
            }
            
            preparedIterator.remove();
        }
    }
    
    /**
     * Update available throughput of running operations.
     * 
     * @param sTime simulation time
     */
    public void updateAvailableThroughput(long sTime)
    {
        this.updateOperationQueues(sTime);
        
        if(!this.runningOperations.isEmpty())
        {
            // get maximum speed per operation
            long maxBpsPerOp = (this.storage.getMaximumSpeed().bpsProperty().get() / this.runningOperations.size());
            ByteSpeed maxSpeedPerOp = new ByteSpeed(maxBpsPerOp, ByteSpeedUnits.BPS);

            long leftoverThroughput = 0;
            ArrayList<Pair<StorageOperation, Long>> leftoverCandidates = new ArrayList<>();

            // first analyze operation maximum transfer throughput
            for(StorageOperation p : this.runningOperations)
            {
                long lo = maxBpsPerOp - p.getMaxTransferThroughput(sTime).bpsProperty().get();

                // unused disk throughput
                if(lo >= 0)
                {
                    leftoverThroughput += lo;

                    // set operation throughput
                    p.setAvailableThroughput(p.getMaxTransferThroughput(sTime));
                }
                // unused operation throughput (could transfer faster)
                else if(lo < 0)
                {
                    leftoverCandidates.add(new Pair(p, lo * (-1)));


                    p.setAvailableThroughput(maxSpeedPerOp);
                }
            }

            // check if we have any leftover throughput to distribute
            if(leftoverThroughput != 0 && leftoverThroughput > leftoverCandidates.size())
            {
                // order candidates by lowest
                Collections.sort(leftoverCandidates, (a, b) -> Long.compare(a.second, b.second));

                for(int i = 0; i < leftoverCandidates.size() && leftoverThroughput > 0; i++)
                {
                    long leftoverPerOp = (leftoverThroughput / ( (leftoverCandidates.size() - i)));
                    Pair<StorageOperation, Long> cP = leftoverCandidates.get(i);

                    // requested throuhgput is less than per OP
                    if(cP.second <= leftoverPerOp)
                    {
                        // increase candidates throughput by maximum possible value
                        cP.first.setAvailableThroughput(
                                new ByteSpeed(cP.first.getAvailableThroughput().bpsProperty().get() + cP.second, ByteSpeedUnits.BPS));
                        leftoverThroughput -= cP.second;
                    }
                    else
                    {
                        cP.first.setAvailableThroughput(
                                new ByteSpeed(cP.first.getAvailableThroughput().bpsProperty().get() + leftoverPerOp));
                        leftoverThroughput -= leftoverPerOp;
                    }
                }
            }
        }
    }
    
    /**
     * Update amount of transfered data for all running operations in interval 
     * beginning at {@code sTime} and ending at {@code sTime} + {@code timeInterval}.
     * 
     * @param timeInterval interval length
     * @param sTime start time
     */
    public void updateTransferedSize(long timeInterval, long sTime)
    {
        synchronized(this.updateMonitor)
        {
            this.updateInProgress = true;
        }
        
        double secs = (timeInterval / 1000.0);
        Iterator<StorageOperation> opProgIterator = this.runningOperations.iterator();
        
        while(opProgIterator.hasNext())
        {
            StorageOperation opProg = opProgIterator.next();
            
            if(opProg.getType().isManaged() && !opProg.isPending())
            {
                ByteSpeed throughput = opProg.getAvailableThroughput();
                ByteSize transfered = new ByteSize(throughput.bpsProperty().get() * secs, ByteSizeUnits.B);
                long bytesLeftToTransfer = (opProg.getTotalSize().bytesProperty().get() - opProg.getTransferedSize().bytesProperty().get());
                
                // if operation isnt finished yet
                if(bytesLeftToTransfer > transfered.bytesProperty().get())
                {
                    opProg.getTransferedSize().bytesProperty().set(
                            opProg.getTransferedSize().bytesProperty().get() + transfered.bytesProperty().get());
                }
                // finished, remove operation
                else
                {
                    this.int_completeOperation(opProg, sTime);
                    opProgIterator.remove();
                }
            }
        }
        
        // @TODO prekontrolovat zamykani zda je vse v poradku
        synchronized(this.updateMonitor)
        {
            this.updateInProgress = false;
            
            if(!this.addedOperations.isEmpty())
            {
                this.addedOperations.forEach(ao -> {
                    this.addOperationToQueue(ao);
                });
            }
        }
        
        this.updateOperationQueues(sTime);
        
    }
    
    /**
     * Complete operation, run callback (if any), remove reserved space.
     * 
     * @param operation operation to finish
     * @param sTime simulation time
     */
    private void int_completeOperation(StorageOperation operation, long sTime)
    {
        StorageOperationCallback callback = operation.getCallback();
        
        if(callback != null)
        {
            callback.onOperationFinished(sTime);
        }
        
        // remove reserved space
        if(operation.getType().isWriting())
        {
            this.removeSpaceReservation(operation.getTotalSize());
        }
        
        // change pending state 
        if(!operation.getPendingOperations().isEmpty())
        {
            operation.getPendingOperations().stream().forEach(po -> {
                po.setPending(false);
            });
        }
        
    }
    
    /**
     * Finish all running and pending operations.
     * 
     * @param sTime simulation time
     */
    public void finish(long sTime)
    {
        Queue<StorageOperation> q = new ArrayDeque<>(this.runningOperations);
        q.addAll(this.preparedOperations);
        q.addAll(this.pendingOperations);
        
        while(!q.isEmpty())
        {
            StorageOperation op = q.poll();
            
            this.int_completeOperation(op, sTime);
        }
    }
    
    /**
     * Reserve size of {@code bytes} bytes for writing.
     * 
     * @param bytes size
     */
    public void reserveSpace(long bytes)
    {
        this.reservedSpace.setBytes(this.reservedSpace.bytesProperty().get() + bytes);
    }
    
    /**
     * Reserve size {@code size} for writing.
     * 
     * @param size size
     */
    public void reserveSpace(ByteSize size)
    {
        this.reserveSpace(size.bytesProperty().get());
    }
    
    /**
     * Remove size of {@code bytes} from reservation.
     * 
     * @param bytes size
     */
    public void removeSpaceReservation(long bytes)
    {
        if(this.reservedSpace.bytesProperty().get() > bytes)
        {
            this.reservedSpace.setBytes(this.reservedSpace.bytesProperty().get() - bytes);
        }
        else
        {
            this.reservedSpace.setBytes(0);
        }
    }
    
    /**
     * Remove size of {@code size} from reservation.
     * 
     * @param size size
     */
    public void removeSpaceReservation(ByteSize size)
    {
        this.removeSpaceReservation(size.bytesProperty().get());
    }
    
    /**
     * Get amount of reserved (not available) space.
     * 
     * @return reserved space
     */
    public ByteSize getReservedSpace()
    {
        return this.reservedSpace;
    }
}

