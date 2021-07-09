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

import cz.zcu.kiv.dfs_simulator.model.ByteSize;
import cz.zcu.kiv.dfs_simulator.model.ByteSizeUnits;
import cz.zcu.kiv.dfs_simulator.model.ModelServerNode;
import cz.zcu.kiv.dfs_simulator.model.storage.ServerStorage;
import cz.zcu.kiv.dfs_simulator.model.storage.StorageOperationCallback;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FileSystemObject;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FsDirectory;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FsFile;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FsGlobalObjectRegistry;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.NotEnoughSpaceLeftException;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.NotMountedException;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.ObjectRegistryEntry;
import cz.zcu.kiv.dfs_simulator.simulation.DfsSimulatorLogger;
import cz.zcu.kiv.dfs_simulator.simulation.PutSimulationTask;
import cz.zcu.kiv.dfs_simulator.simulation.SimulationType;
import cz.zcu.kiv.dfs_simulator.simulation.path.DfsPath;
import cz.zcu.kiv.dfs_simulator.simulation.path.DfsPathPicker;
import cz.zcu.kiv.dfs_simulator.simulation.path.FsObjectNotFoundException;
import cz.zcu.kiv.dfs_simulator.simulation.path.NoPathAvailableException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * File replication manager.
 */
public class FsGlobalReplicationManager 
{
    
    /**
     * Get all replica targets of {@code file}.
     * 
     * @param file file
     * @return replica targets
     */
    public static List<ReplicaTarget> getReplicaTargets(FsFile file)
    {
        List<ReplicaTarget> replicaTargets = new ArrayList<>();
        ObjectRegistryEntry ore = FsGlobalObjectRegistry.getObjectEntry(file);
        
        if(ore != null)
        {
            for(ModelServerNode server : ore.servers)
            {
                ServerStorage stor = server.getFsManager().getFsObjectMountDeviceByName(file);
                
                if(stor != null)
                {
                    replicaTargets.add(new ReplicaTarget(server, stor));
                }
            }
        }
        
        return replicaTargets;
    }
    
    /**
     * Update (replace) replica targets of {@code file} with {@code newTargets}.
     * 
     * @param file file
     * @param newTargets new replica targets
     * @throws NotEnoughSpaceLeftException thrown when device that 
     * replica is on does not have enough space to fit new size.
     */
    public static void updateReplicaTargets(FsFile file, List<ReplicaTarget> newTargets) throws NotEnoughSpaceLeftException
    {
        if(!int_updateReplicaTargets(file, newTargets, false))
        {
            throw new NotEnoughSpaceLeftException("One or more targets does not have enough space.");
        }
    }
    
    /**
     * Forcefully updates (replaces) replica targets. When any mount device
     * does not have enough space to fit replica's new size it will be automatically
     * resized to fit.
     * 
     * @param file file
     * @param newTargets new replica targets
     */
    public static void forceUpdateReplicaTargets(FsFile file, List<ReplicaTarget> newTargets)
    {
        int_updateReplicaTargets(file, newTargets, true);
    }
    
    /**
     * Internal method. Update (replace) replica targets of {@code file} with {@code newTargets}.
     * If {@code force} is set, storage will be automatically resized if not
     * enough space is available.
     * 
     * @param file file
     * @param newTargets new replica targets
     * @param force force flag
     * @return if replicas were updated (if {@code force} is set, this will always return true)
     */
    private static boolean int_updateReplicaTargets(FsFile file, List<ReplicaTarget> newTargets, boolean force)
    {
        List<ReplicaTarget> currentTargets = FsGlobalReplicationManager.getReplicaTargets(file);
        
        ArrayList<ReplicaTarget> add = new ArrayList<>(newTargets);
        ArrayList<ReplicaTarget> remove = new ArrayList<>(currentTargets);
        
        Iterator<ReplicaTarget> addIterator = add.iterator();
        
        while(addIterator.hasNext())
        {
            ReplicaTarget rt = addIterator.next();
            
            // add a new replica only if it has a new destination node
            // replicas with different storage are updated
            boolean addRt = currentTargets.stream().noneMatch(t -> (t.serverNode == rt.serverNode && t.storage == rt.storage));
            
            if(!addRt)
            {
                addIterator.remove();
            }
        }
        
        Iterator<ReplicaTarget> removeIterator = remove.iterator();
        
        while(removeIterator.hasNext())
        {
            ReplicaTarget rt = removeIterator.next();
            
            // replicas that are removed from target nodes
            boolean removeRt = newTargets.stream().noneMatch(t -> (t.serverNode == rt.serverNode));
            
            if(!removeRt)
            {
                removeIterator.remove();
            }
        }
        
        // delete them first
        remove.forEach(rR -> {
            rR.serverNode.getFsManager().removeDirectoryChild(file.getFullPath());
        });
        
        if(force)
        {
            forceReplicateFile(file, add);
        }
        else
        {
            try
            {
                replicateFile(file, add);
            }
            catch(NotEnoughSpaceLeftException ex)
            {
                return false;
            }
        }
        
        return true;
    }
        
    /**
     * Forcefully replicate new file {@code file}.
     * 
     * @param file file
     * @param targets replica targets
     */
    public static void forceReplicateFile(FsFile file, List<ReplicaTarget> targets)
    {
        for(ReplicaTarget rt : targets)
        {
            FsDirectory repParentDir = 
                    (FsDirectory) rt.serverNode.getRootDir().getChildObject(file.getParent().getFullPath(), true);
            
            if(repParentDir != null)
            {
                // check if file exists so that we keep an existing references intact
                FileSystemObject repFile = repParentDir.getChildObject(file.nameProperty().get());
                if(repFile == null || !(repFile instanceof FsFile))
                {
                    repFile = new FsFile(file.nameProperty().get(), 
                        new ByteSize(file.getSize().bytesProperty().get(), ByteSizeUnits.B), 
                        repParentDir);
                }
                
                try
                {
                    rt.serverNode.getFsManager().addDirectoryChild(repParentDir, repFile);
                    rt.serverNode.getFsManager().forceMount(rt.storage, repFile);
                }
                catch(NotEnoughSpaceLeftException ex)
                {
                    throw new RuntimeException("Unable to add directory child due "
                            + "to not enough space, but the child is mounted elsewhere!");
                }
            }
        }
    }
    
    /**
     * Checks if replicas of file {@code file} can be resized - if their storage 
     * devices have enough space available.
     * 
     * @param file file
     * @return true if replica can be resized, false otherwise
     */
    public static boolean canReplicaBeResized(FsFile file)
    {
        return canReplicaBeResized(file, getReplicaTargets(file));
    }
    
    /**
     * Checks if replicas of file {@code file} can be resized - if their storage 
     * devices have enough space available.
     * 
     * @param file file
     * @param targets replica targets
     * @return true if replica can be resized, false otherwise
     */
    public static boolean canReplicaBeResized(FsFile file, List<ReplicaTarget> targets)
    {
        for(ReplicaTarget rt : targets)
        {
            ByteSize unused = rt.serverNode.getFsManager().getStorageUnusedSize(rt.storage);
            
            if(unused.bytesProperty().get() < file.getSize().bytesProperty().get())
            {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Replicate new file to targets {@code targets}.
     * 
     * @param file file
     * @param targets replica targets
     * @throws NotEnoughSpaceLeftException thrown when any target device does not have
     * enough space to fit new replica.
     */
    public static void replicateFile(FsFile file, List<ReplicaTarget> targets) throws NotEnoughSpaceLeftException
    {
        // @TODO get rid of duplicate code from {@code canReplicaBeResized}
        for(ReplicaTarget rt : targets)
        {
            ByteSize unused = rt.serverNode.getFsManager().getStorageUnusedSize(rt.storage);
            
            if(unused.bytesProperty().get() < file.getSize().bytesProperty().get())
            {
                throw new NotEnoughSpaceLeftException("Server " + rt.serverNode + " does not have enough space on storage " + rt.storage);
            }
        }
        
        // all nodes have enough space
        forceReplicateFile(file, targets);
    }
    
    /**
     * Delete all replicas of object {@code object}.
     * 
     * @param object object
     */
    public static void deleteReplicatedObject(FileSystemObject object)
    {
        ObjectRegistryEntry ore = FsGlobalObjectRegistry.getObjectEntry(object);
        
        if(ore != null)
        {
            // we have to make copy since we will be altering the global registry
            ArrayList<ModelServerNode> serversCpy = new ArrayList<>(ore.servers);
            
            for(ModelServerNode server : serversCpy)
            {
                server.getFsManager().removeDirectoryChild(object.getFullPath());
            }
            
            FsGlobalObjectRegistry.removePath(object.getFullPath());
        }
    }
    
    /**
     * Rename all replicas of object {@code object} to {@code newName}.
     * 
     * @param object object
     * @param newName new name
     */
    public static void renameReplicatedObject(FileSystemObject object, String newName)
    {
        ObjectRegistryEntry ore = FsGlobalObjectRegistry.getObjectEntry(object);
        
        if(ore != null)
        {
            // remove existing path
            FsGlobalObjectRegistry.removePath(object.getFullPath());
            
            // rename all objects
            ore.fsObjects.stream().forEach(replicatedObject -> replicatedObject.nameProperty().set(newName));
            
            // register new path
            FsGlobalObjectRegistry.addPath(object.getFullPath(), ore);
        }
    }
    
    /**
     * Forcefully resize replicated file {@code file} to {@code newSize}. Any
     * replica target devices that do not have enough space available will
     * be automatically resized.
     * 
     * @param file file
     * @param newSize new size
     */
    public static void forceResizeReplicatedFile(FsFile file, ByteSize newSize)
    {
        ObjectRegistryEntry ore = FsGlobalObjectRegistry.getObjectEntry(file);
        
        // resize on all nodes
        ore.servers.stream().forEach(srv -> {
            FileSystemObject serverObject = srv.getRootDir().getChildObject(file.getFullPath());

            if(serverObject != null && serverObject instanceof FsFile)
            {
                if(!srv.getFsManager().canFileFitStorage((FsFile) serverObject, newSize))
                {
                    srv.getFsManager().resizeStorageToFit((FsFile) serverObject, newSize);
                }
                
                ((FsFile) serverObject).setSize(newSize);
            }
        });
    }
    
    /**
     * Resize replicated file {@code file} to {@code newSize}.
     * 
     * @param file file
     * @param newSize new size
     * @throws NotEnoughSpaceLeftException thrown when any target device does
     * not have enough space to fit replica with new size {@code newSize}.
     */
    public static void resizeReplicatedFile(FsFile file, ByteSize newSize) throws NotEnoughSpaceLeftException
    {
        ObjectRegistryEntry ore = FsGlobalObjectRegistry.getObjectEntry(file);
        
        if(ore != null && file.getSize().bytesProperty().get() < newSize.bytesProperty().get())
        {
            // first check all nodes
            for(ModelServerNode server : ore.servers)
            {
                FileSystemObject serverObject = server.getRootDir().getChildObject(file.getFullPath());

                if(serverObject != null && 
                        serverObject instanceof FsFile &&
                        !server.getFsManager().canFileFitStorage((FsFile) serverObject, newSize))
                {
                    throw new NotEnoughSpaceLeftException("Not enough space left on server id " + server.getNodeID());
                }
            }
        }
        
        forceResizeReplicatedFile(file, newSize);
    }
    
    /**
     * Get an instance of (random) replica from path {@code path}.
     * 
     * @param path path
     * @return replica instance if found, null otherwise
     */
    public static FsFile getReplicaInstance(String path)
    {
        ObjectRegistryEntry ore = FsGlobalObjectRegistry.getObjectEntry(path);
        
        if(ore != null && !ore.fsObjects.isEmpty())
        {
            FileSystemObject fsObj = ore.fsObjects.get(0);
            
            if(fsObj instanceof FsFile)
            {
                return (FsFile) fsObj;
            }
        }
        
        return null;
    }
    
    /**
     * Checks whether file is replicated to multiple targets.
     * 
     * @param file file
     * @return true if replicated, false otherwise
     */
    public static boolean isFileReplicated(FsFile file)
    {
        return getReplicaTargets(file).size() > 1;
    }
    
    /**
     * Propagate resize of file {@code file} - all replicas have to be updates, 
     * therefore we need to create appropriate transfers (and disk operations).
     * 
     * @param file file
     * @param origin server, that has updated replica (originating server)
     * @param pathBuilder path builder used to build path
     * @param simulationType running simulation type
     * @param logger simulation logger
     * @param sTime simulation time
     */
    // since we do not have any way to share connection link between multiple
    // 'users', the link bandwidth is unaffected by this transfer, although
    // it definitely should be - if simulation picks this path for transfer
    // it will retain its throughput although we are using it to replicate
    // this file
    public static void propagateReplicaResize(FsFile file, ModelServerNode origin, 
            DfsPathPicker pathBuilder, SimulationType simulationType, DfsSimulatorLogger logger,
            long sTime)
    {
        ServerStorage originStorage = origin.getFsManager().getFsObjectMountDevice(file);
        
        if(originStorage != null)
        {
            List<ReplicaTarget> replicas = getReplicaTargets(file);
            
            // create dummy task
            PutSimulationTask dummyTask = new PutSimulationTask(file);
            
            // create transfer list for i/o operations
            List<FsFile> transferList = new ArrayList<>();
            transferList.add(file);
            
            if(!replicas.isEmpty())
            {
                replicas.forEach((replica) ->
                {
                    // resize other replicas only
                    if(!replica.serverNode.equals(origin))
                    {
                        // find path to target replica server
                        try 
                        {
                            // get path to replica server
                            DfsPath replicaPath = pathBuilder.selectPath(origin, dummyTask, sTime, replica.serverNode, simulationType);

                            // create replication operations
                            replica.storage.getOperationManager().addReplicationOperation(transferList, originStorage, replicaPath, new StorageOperationCallback()
                            {
                                @Override public void onOperationStarted(long sTime)
                                {
                                    logger.logReplicationStart(file, origin, replica.serverNode, sTime);
                                }

                                @Override public void onOperationFinished(long sTime)
                                {
                                    FileSystemObject replicaFile = replica.serverNode.getRootDir().getChildObject(file.getFullPath());
                                    
                                    if(replicaFile instanceof FsFile)
                                    {
                                        ((FsFile) replicaFile).setSize(file.getSize());
                                    }
                                    
                                    logger.logReplicationFinish(file, origin, replica.serverNode, sTime);
                                }
                            }, false);
                        }
                        catch(FsObjectNotFoundException | NotMountedException | NoPathAvailableException ex)
                        {
                            logger.logError("Unable to replicate file due to an exception: " + ex.getMessage(), sTime);
                            
                            throw new RuntimeException("Unable to replicate file " + file.toString() + 
                                    " from server " + origin.toString() + " to server " + replica.serverNode.toString() + ": " + ex.getMessage());
                        }
                    }
                });
            }
        }
    }
    
}
