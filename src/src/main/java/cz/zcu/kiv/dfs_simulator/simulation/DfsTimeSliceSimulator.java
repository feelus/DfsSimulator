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

package cz.zcu.kiv.dfs_simulator.simulation;

import cz.zcu.kiv.dfs_simulator.model.ByteSize;
import cz.zcu.kiv.dfs_simulator.model.ByteSpeed;
import cz.zcu.kiv.dfs_simulator.model.ByteSpeedUnits;
import cz.zcu.kiv.dfs_simulator.model.ModelClientNode;
import cz.zcu.kiv.dfs_simulator.model.ModelNodeRegistry;
import cz.zcu.kiv.dfs_simulator.model.ModelServerNode;
import cz.zcu.kiv.dfs_simulator.model.storage.ServerStorageManager;
import cz.zcu.kiv.dfs_simulator.simulation.path.DfsPath;
import cz.zcu.kiv.dfs_simulator.simulation.path.FsObjectNotFoundException;
import cz.zcu.kiv.dfs_simulator.simulation.path.NoNeighboursAvailableException;
import cz.zcu.kiv.dfs_simulator.simulation.path.NoPathAvailableException;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FsFile;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.NotEnoughSpaceLeftException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import cz.zcu.kiv.dfs_simulator.model.storage.StorageOperation;
import cz.zcu.kiv.dfs_simulator.model.storage.StorageOperationTransferLimiter;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.NotEnoughSpaceLeftReplicaException;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.NotMountedException;
import cz.zcu.kiv.dfs_simulator.model.storage.replication.FsGlobalReplicationManager;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import cz.zcu.kiv.dfs_simulator.simulation.path.DfsPathPicker;

/**
 * Discrete simulator. Takes all tasks and consecutively executes them. For each task,
 * data transfer is split into intervals of {@link #TIME_RESOLUTION_MS}.
 */
public class DfsTimeSliceSimulator implements DfsSimulator
{
    /**
     * Simulation time slice
     */
    private static final int TIME_RESOLUTION_MS = 500;

    /**
     * Client (origin)
     */
    protected final ModelClientNode client;
    /**
     * Simulation plan (tasks)
     */
    protected final SimulationPlan plan;
    /**
     * Path picker - used to select path for tasks
     */
    protected final DfsPathPicker pathPicker;
    
    /**
     * Simulation type
     */
    protected final SimulationType simType;
    
    /**
     * Simulation results
     */
    protected final List<DfsSimulatorTaskResult> results = new ArrayList<>();
    
    /**
     * Discrete simulator.
     * 
     * @param client client (origin)
     * @param plan simulation plan (tasks)
     * @param pathPicker path picker
     * @param type simulation type
     */
    public DfsTimeSliceSimulator(ModelClientNode client, SimulationPlan plan, DfsPathPicker pathPicker, SimulationType type)
    {
        this.client = client;
        this.plan = plan;
        this.pathPicker = pathPicker;
        this.simType = type;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public void run(DfsSimulatorLogger logger)
    {
        ArrayDeque<SimulationTask> taskQueue = new ArrayDeque<>();
        taskQueue.addAll(this.plan.getTasks());
        
        // simulation time
        long sTime = 0;
        logger.logSimulationStarted(sTime);
        
        while(!taskQueue.isEmpty())
        {
            DfsSimulatorTaskResult taskResult = this.processTask(taskQueue.pop(), sTime, logger);
            
            // add it to result list
            this.results.add(taskResult);
            
            // increment simulation run time
            sTime += taskResult.getTotalTime();
        }
        
        this.finishServerStorageOperations(ModelNodeRegistry.getServerNodes(), sTime);
        logger.logSimulationEnded(sTime);
    }
    
    /**
     * Attempts to execute given simulation task. 
     * 
     * @param task task to execute
     * @param bTime simulation time at which processing of this task begins
     * @param logger simulator logger
     * @return simulation task result
     */
    private DfsSimulatorTaskResult processTask(SimulationTask task, long bTime, DfsSimulatorLogger logger)
    {
        logger.logSimulationTaskStarted(task, bTime);
        
        long tTime = -1;
        Exception caughtEx = null;
        List<DfsPath> pathHistory = new ArrayList<>();
        SimulationThroughputSampler sampler = new FilteringThroughputSampler();
        
        // download
        if(task instanceof GetSimulationTask)
        {
            try
            {
                tTime = this.executeDownloadTask(task, bTime, pathHistory, sampler, logger);
            }
            catch(NoPathAvailableException | NotMountedException | FsObjectNotFoundException ex)
            {
                caughtEx = ex;
            }
        }
        // upload
        else
        {
            try
            {
                tTime = this.executeUploadTask(task, bTime, pathHistory, sampler, logger);
            }
            catch(NoPathAvailableException | NotMountedException | NotEnoughSpaceLeftException | FsObjectNotFoundException | NotEnoughSpaceLeftReplicaException ex)
            {
                caughtEx = ex;
            }
        }
        
        DfsSimulatorTaskResult result;
        if(caughtEx == null && tTime != -1)
        {
            long totalBytes = task.getFile().getSize().bytesProperty().get();
            double tTimeSec = tTime * 0.001;
            long averageBpsec = (long) (totalBytes / tTimeSec);

            result = new DfsSimulatorTaskResult(
                    task, DfsSimulatorTaskResultState.SUCCESS, tTime, 
                    new ByteSpeed(averageBpsec, ByteSpeedUnits.BPS), pathHistory, sampler);
        }
        else
        {
            try
            {
                // get query time to chosen registry
                long qTime = this.pathPicker.getObjectRegistryQueryTime(this.client);
                DfsSimulatorTaskResultState state;

                if(caughtEx instanceof NotEnoughSpaceLeftException)
                {
                    state = DfsSimulatorTaskResultState.NOT_ENOUGH_SPACE_ON_DEVICE;
                }
                else if(caughtEx instanceof FsObjectNotFoundException)
                {
                    state = DfsSimulatorTaskResultState.OBJECT_NOT_FOUND;
                }
                else if(caughtEx instanceof NotMountedException)
                {
                    state = DfsSimulatorTaskResultState.OBJECT_NOT_MOUNTED;
                }
                else if(caughtEx instanceof NotEnoughSpaceLeftReplicaException)
                {
                    state = DfsSimulatorTaskResultState.NOT_ENOUGH_SPACE_FOR_REPLICA;
                }
                else
                {
                    state = DfsSimulatorTaskResultState.NO_PATH_AVAILABLE;
                }

                result = new DfsSimulatorTaskResult(task, state, qTime, 
                        new ByteSpeed(0), pathHistory, sampler);

            }
            // client has no connected neighbours
            catch(NoNeighboursAvailableException ex)
            {
                result = new DfsSimulatorTaskResult(task, 
                        DfsSimulatorTaskResultState.NO_NEIGHBOURS_AVAILABLE, 
                        0, new ByteSpeed(0), pathHistory, sampler);
            }
        }
        
        logger.logSimulationTaskEnded(result, (bTime + result.getTotalTime()));
         
        return result;
    }
    
    /**
     * Executes upload task.
     * 
     * @param task upload task
     * @param bTime simulation time at which processing of this task begins
     * @param pathHistory list of paths (maximum one for upload task)
     * @param throughputHistory history of sampled (at TIME_RESOLUTION_MS) average throughput
     * @param logger simulator logger
     * @return time taken to execute
     * @throws NoPathAvailableException if there is no path available to target
     * @throws NotEnoughSpaceLeftException if there is not enough space to store file in upload directory
     */
    private long executeUploadTask(SimulationTask task, long bTime, List<DfsPath> pathHistory, SimulationThroughputSampler throughputSampler, DfsSimulatorLogger logger) 
            throws NoPathAvailableException, NotMountedException, NotEnoughSpaceLeftException, FsObjectNotFoundException, NotEnoughSpaceLeftReplicaException
    {
        final FsFile f = task.getFile();
        
        if(FsGlobalReplicationManager.isFileReplicated(f) && !FsGlobalReplicationManager.canReplicaBeResized(f))
        {
            throw new NotEnoughSpaceLeftReplicaException("File cannot be resized on all replicas.");
        }

        long tTime = this.simulateTaskExecution(task, bTime, pathHistory, throughputSampler, logger);

        if(tTime != -1 && !pathHistory.isEmpty())
        {
            ModelServerNode targetServer = pathHistory.get(pathHistory.size() - 1).getTarget();
            // get target object (all paths in upload task NEED to have same target)
            final FsFile uploadedFile = pathHistory.get(pathHistory.size() - 1).getOrCreateTargetFile();
            
            // @TODO file replication

            if(uploadedFile != null)
            {
                uploadedFile.setSize(f.getSize());
                
                // if file has replicas, we need to update them
                if(FsGlobalReplicationManager.isFileReplicated(uploadedFile))
                {
                    FsGlobalReplicationManager.propagateReplicaResize(uploadedFile, 
                            targetServer, this.pathPicker, this.simType, logger, tTime);
                }
                
                return tTime;
            }
        }
        
        return -1;
    }
    
    /**
     * Executes download task.
     * 
     * @param task download task
     * @param bTime simulation time at which processing of this task begins
     * @param pathHistory list of paths (maximum one for upload task)
     * @param throughputHistory history of sampled (at TIME_RESOLUTION_MS) average throughput
     * @param logger simulator logger
     * @return time taken to execute
     * @throws NoPathAvailableException if there is no path available to target
     * @throws FsObjectNotFoundException download object (target) not found
     * @throws NotMountedException target not mounted
     */
    private long executeDownloadTask(SimulationTask task, long bTime, List<DfsPath> pathHistory, SimulationThroughputSampler throughputSampler, DfsSimulatorLogger logger) throws NoPathAvailableException, FsObjectNotFoundException, NotMountedException
    {
        return this.simulateTaskExecution(task, bTime, pathHistory, throughputSampler, logger);
    }
    
    /**
     * Creates storage operation for given task {@code task}.
     * 
     * @param task task
     * @param transferList list of transfered files
     * @param path selected path to storage
     * @return created storage operation
     */
    private StorageOperation createTaskStorageOperation(SimulationTask task, List<FsFile> transferList, DfsPath path)
    {
        if(task instanceof GetSimulationTask)
        {
            return path.getTargetStorage().getOperationManager().addUnmanagedReadOperation(transferList, new StorageOperationTransferLimiter()
            {
                @Override public ByteSpeed getTransferLimit(long sTime)
                {
                    return path.getCurrentLinkBandwidth(sTime);
                }
            });
        }
        else
        {
            return path.getTargetStorage().getOperationManager().addUnmanagedWriteOperation(transferList, new StorageOperationTransferLimiter()
            {
                @Override
                public ByteSpeed getTransferLimit(long sTime)
                {
                    return path.getCurrentLinkBandwidth(sTime);
                }
            }, true);
        }
    }
    
    /**
     * Execute task.
     * 
     * @param task task to execute
     * @param bTime time elapsed from the beginning of current simulation run
     * @return task result time
     */
    private long simulateTaskExecution(SimulationTask task, long bTime, List<DfsPath> pathHistory, SimulationThroughputSampler throughputSampler, DfsSimulatorLogger logger) throws NoPathAvailableException, FsObjectNotFoundException, NotMountedException
    {
        // time taken for this task
        long tTime = 0;
        
        long totalBytes = task.getFile().getSize().bytesProperty().get();
        long bytesTransfered = 0;
        
        boolean hierarchicalMode = (this.simType.isHierarchical() && this.simType.getHierarchicalMonitor() != null);
        
        DfsPath cPath = this.pathPicker.selectPath(this.client, task, tTime, null, this.simType);        
        
        if(cPath == null)
        {
            return -1;
        }
        
        List<ModelServerNode> serverNodes = ModelNodeRegistry.getServerNodes();
        ArrayList<FsFile> transferList = new ArrayList<>();
        transferList.add(task.getFile());
        
        // add latency
        tTime += cPath.getCumLatency();
        
        long recalcTime = 0;
        
        Map<FsFile, ModelServerNode> accessedFiles = new HashMap<>();
        
        if(hierarchicalMode)
        {
            FsFile targetFile = cPath.getOrCreateTargetFile();
            // we have to set file size before hand so that we can 
            // calculate migration possibilities in advance and possibly
            // skip writing to a slower storage
            if(task instanceof PutSimulationTask)
            {
                targetFile.setSize(task.getFile().getSize());
            }
            
            accessedFiles.put(cPath.getOrCreateTargetFile(), cPath.getTarget());
            
            this.simType.getHierarchicalMonitor().onBeforeAccess(
                    cPath.getOrCreateTargetFile(), cPath.getTarget(), task, logger, (bTime + tTime));
        }
        
        long prevStorageUpdate = 0;
        
        // create a placeholder operation that will ensure we get 
        // alloted storage bandwidth
        StorageOperation runningOpProgress = this.createTaskStorageOperation(task, transferList, cPath);
        cPath.setRunningOperation(runningOpProgress);
        
        this.updateServerStorageOperations(serverNodes, (tTime - prevStorageUpdate), (bTime + tTime));
        logger.logPathSelected(cPath, (bTime + tTime));
        
        pathHistory.add(cPath);
        while(bytesTransfered != totalBytes)
        {
            this.updateServerStorageOperations(serverNodes, (tTime - prevStorageUpdate), (bTime + tTime));
            prevStorageUpdate = tTime;
            
            // check if its time to recalculate path
            if(this.simType.isDynamicRoutingEnabled() && (recalcTime >= this.simType.getDynamicRoutingRecalcInterval()))
            {
                DfsPath rPath;
                
                // we can select different path with different destination server
                if(task instanceof GetSimulationTask)
                {
                    rPath = this.pathPicker.selectPath(this.client, task, (tTime + bTime), null, simType);
                }
                // for upload tasks we can only change path, not destination
                else
                {
                    rPath = this.pathPicker.selectPath(this.client, task, (tTime + bTime), cPath.getTarget(), simType);
                }
                
                // change path and add latency
                if(!rPath.equals(cPath))
                {
                    // cancel running storage task
                    runningOpProgress.removeUnmanaged();
                    runningOpProgress = this.createTaskStorageOperation(task, transferList, rPath);
                    // we have to update again to recalculate
                    this.updateServerStorageOperations(serverNodes, (tTime - prevStorageUpdate), (bTime + tTime));
                    
                    // set path running operation
                    rPath.setRunningOperation(runningOpProgress);
                    logger.logPathSelected(rPath, (bTime + tTime));
                    tTime += rPath.getCumLatency();
                    
                    pathHistory.add(rPath);
                    cPath = rPath;
                    
                    // record visited file (max. once)
                    accessedFiles.put(cPath.getOrCreateTargetFile(), cPath.getTarget());
                    // notify hierarchical monitor
                    if(hierarchicalMode)
                    {
                        this.simType.getHierarchicalMonitor().onBeforeAccess(
                                cPath.getOrCreateTargetFile(), cPath.getTarget(), task, logger, (bTime + tTime));
                    }
                }
                
                recalcTime = 0;
            }
            
            // @TODO this isnt entirely accurate since if the file is transfered faster than TIME_RESOLUTION_MS
            // the actual speed could be different
            ByteSpeed sliceAvgThroughput = cPath.getAverageTransferThroughput(
                    (tTime + bTime), TIME_RESOLUTION_MS);
            
            // calculate how much we downloaded in this step
            long stepTransfer = DfsPath.getDataTransferedInTime((tTime + bTime), sliceAvgThroughput, 
                    TIME_RESOLUTION_MS).bytesProperty().get();  
                        
            // too small of a fraction
            // @TODO this should be handled (maybe calculate how much ms it should take to
            // transfer given item))
            if(stepTransfer <= 0)
            {
                break;
            }
            
            if( (bytesTransfered + stepTransfer) == totalBytes )
            {
                throughputSampler.recordSample(tTime + bTime, sliceAvgThroughput, true);
                
                tTime += TIME_RESOLUTION_MS;
                break;
            }
            // we transfered the whole object in this time slice but 
            // didnt use all time
            else if( ( bytesTransfered + stepTransfer ) > totalBytes )
            {
                throughputSampler.recordSample(tTime + bTime, sliceAvgThroughput, true);
                
                // calculate how much time it takes to transfer remaining bytes
                tTime += DfsPath.getDataTransferTime((tTime + bTime), 
                        cPath.getCurrentPossibleThroughput((tTime + bTime)),
                        new ByteSize((totalBytes - bytesTransfered)));
                bytesTransfered = totalBytes;
            }
            else
            {
                throughputSampler.recordSample(tTime + bTime, sliceAvgThroughput, false);
                
                bytesTransfered += stepTransfer;
                tTime += TIME_RESOLUTION_MS;
                recalcTime += TIME_RESOLUTION_MS;
            }
        }
        
        // cleanup
        runningOpProgress.removeUnmanaged();
        this.updateServerStorageOperations(serverNodes, (tTime - prevStorageUpdate), (bTime + tTime));
        
        if(bytesTransfered > 0)
        {
            // notify hierarchical monitor about all accessed files
            if(hierarchicalMode)
            {
                for(Entry<FsFile, ModelServerNode> entry : accessedFiles.entrySet())
                {
                    this.simType.getHierarchicalMonitor().onAfterAccess(entry.getKey(), entry.getValue(), task, logger, (bTime + tTime));
                }
            }
            
            return tTime;
        }
        else if(totalBytes == 0)
        {
            return tTime;
        }
        
        // no data transfered
        return -1;
    }
    
    /**
     * Calls {@link ServerStorageManager#updateStorageAvailableThroughput(long)} 
     * and afterwards {@link ServerStorageManager#updateStorageTransferedSize(long, long)}
     * of all servers possibly affected during simulation.
     * 
     * @param servers list of servers
     * @param timeInterval how far ahead are we updating
     * @param sTime simulation time
     */
    private void updateServerStorageOperations(List<ModelServerNode> servers, long timeInterval, long sTime)
    {
        for(ModelServerNode s : servers)
        {
            s.getStorageManager().updateStorageAvailableThroughput(sTime);
        }
        
        for(ModelServerNode s : servers)
        {
            s.getStorageManager().updateStorageTransferedSize(timeInterval, sTime);
        }
    }
    
    /**
     * Calls {@link ServerStorageManager#finishStorageOperations(long)} for
     * all possibly affected (during simulation) servers.
     * 
     * @param servers list of servers
     * @param sTime simulation time
     */
    private void finishServerStorageOperations(List<ModelServerNode> servers, long sTime)
    {
        for(ModelServerNode s : servers)
        {
            s.getStorageManager().finishStorageOperations(sTime);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public List<DfsSimulatorTaskResult> getResults()
    {
        return this.results;
    }
    
}
