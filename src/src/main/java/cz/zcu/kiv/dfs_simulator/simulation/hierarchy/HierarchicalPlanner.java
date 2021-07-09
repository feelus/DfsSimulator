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

package cz.zcu.kiv.dfs_simulator.simulation.hierarchy;

import cz.zcu.kiv.dfs_simulator.helpers.Pair;
import cz.zcu.kiv.dfs_simulator.model.ByteSize;
import cz.zcu.kiv.dfs_simulator.model.ByteSizeUnits;
import cz.zcu.kiv.dfs_simulator.model.ModelServerNode;
import cz.zcu.kiv.dfs_simulator.simulation.DfsSimulatorLogger;
import cz.zcu.kiv.dfs_simulator.simulation.GetSimulationTask;
import cz.zcu.kiv.dfs_simulator.simulation.SimulationTask;
import cz.zcu.kiv.dfs_simulator.model.storage.ServerStorage;
import cz.zcu.kiv.dfs_simulator.model.storage.StorageOperation;
import cz.zcu.kiv.dfs_simulator.model.storage.StorageOperationCallback;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FsFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Monitors file access and plans file migration during simulation of a 
 * hierarchical method.
 */
public abstract class HierarchicalPlanner implements HierarchicalAccessMonitor
{
    
    /**
     * Calculates {@code storage} available space from storage currently unused
     * space and reserved space for ongoing migration operations.
     * 
     * Returned {@code ByteSize} bytes will be always non-negative.
     * 
     * @param server target server
     * @param storage target storage
     * @return available space
     */
    public ByteSize getStorageAvailableSpace(ServerStorage storage, ModelServerNode server)
    {
        ByteSize unusedTargetStorageSize = server.getFsManager().getStorageUnusedSize(storage);
        ByteSize reserverdTargetStorageSize = storage.getOperationManager().getReservedSpace();
        
        long bpsLeft = unusedTargetStorageSize.bytesProperty().get() - reserverdTargetStorageSize.bytesProperty().get();
        
        if(bpsLeft > 0)
        {
            return new ByteSize(bpsLeft, ByteSizeUnits.B);
        }
        
        return new ByteSize(0);
    }
        
    /**
     * Internal method. Select highest storage that {@code file} can be 
     * migrated onto and simultaneously build a migration plan in order
     * to fit {@code file} onto {@code server}.
     * 
     * @param storageList list of available storage devices
     * @param file migrated file
     * @param server target server
     * @param migrationPlanList migration plan
     * @return fastest possible storage
     */
    private ServerStorage int_selectHighestPossibleStorage(List<ServerStorage> storageList, FsFile file, ModelServerNode server, List<MigrationPlan> migrationPlanList)
    {
        // sort them by speed (ASC - highest index highest speed)
        Collections.sort(storageList, (a, b) -> Long.compare(a.getMaximumSpeed().bpsProperty().get(), b.getMaximumSpeed().bpsProperty().get()));

        // get current storage
        ServerStorage cStor = server.getFsManager().getFsObjectMountDevice(file);

        // get current storage index (-1 if none)
        int cIndex = storageList.indexOf(cStor);
        
        // iterate from fastest storage down
        for (int i = (storageList.size() - 1); i > cIndex; i--)
        {
            ByteSize availSpace = this.getStorageAvailableSpace(storageList.get(i), server);
            
            if(availSpace.bytesProperty().get() > file.getSize().bytesProperty().get())
            {
                if(migrationPlanList != null)
                {
                    MigrationPlan mp = new MigrationPlan();
                    mp.source = cStor;
                    mp.target = storageList.get(i);
                    mp.subset = new ArrayList<>();
                    mp.subset.add(file);
                    
                    migrationPlanList.add(mp);
                }
                
                return storageList.get(i);
            }
        }
        
        // no storage had enough space available, try to find a storage 
        // with file subset that can be moved down in order to get enough space
        // to fit the desired file
        for(int i = (storageList.size() - 1); i > cIndex; i-- )
        {
            List<MigrationPlan> mp = this.buildMigrationPlansToFit(file, cStor, 
                    storageList.get(i), storageList, server);

            if(mp != null && !mp.isEmpty())
            {
                if(migrationPlanList != null)
                {
                    migrationPlanList.addAll(mp);
                }

                return storageList.get(i);
            }
        }
        
        return null;
    }
    
    /**
     * Internal method. Select highest storage that {@code file} can be 
     * migrated onto and simultaneously build a migration plan in order
     * to fit {@code file} onto {@code server}.
     * 
     * @param file migrated file
     * @param server target server
     * @param migrationPlanList migration plan
     * @return fastest possible storage
     */
    private ServerStorage int_getHighestAvailableStorage(FsFile file, ModelServerNode server, List<MigrationPlan> migrationPlanList)
    {
        List<ServerStorage> accessibleStorage = new ArrayList<>(server.getStorageManager().getStorage());

        return this.int_selectHighestPossibleStorage(accessibleStorage, file, server, migrationPlanList);
    }
    
    /**
     * Internal method. Select highest storage that {@code file} can be 
     * migrated onto and simultaneously build a migration plan in order
     * to fit {@code file} onto {@code server}.
     * 
     * @param file migrated file
     * @param server target server
     * @return fastest possible storage
     */
    public ServerStorage getHighestAvailableStorage(FsFile file, ModelServerNode server)
    {
        return this.int_getHighestAvailableStorage(file, server, null);
    }
    
    /**
     * Begin migration process given by {@code migrationPlanList}.
     * 
     * @param migrationPlanList migration plan
     * @param server target server
     * @param logger simulator logger
     * @param sTime simulation time
     */
    protected void beginMigrationProcess(List<MigrationPlan> migrationPlanList, ModelServerNode server, DfsSimulatorLogger logger, long sTime)
    {
        Pair<StorageOperation, StorageOperation> prevPair = null;
        
        for(int i = 0; i < migrationPlanList.size(); i++)
        {
            MigrationPlan mp = migrationPlanList.get(i);
            long totalBytes = 0;
            
            for(FsFile f : mp.subset)
            {
                f.migratingProperty().set(true);
                totalBytes += f.getSize().bytesProperty().get();
            }
            
            // reserve on each target enough space
            mp.target.getOperationManager().reserveSpace(totalBytes);
            
            Pair<StorageOperation, StorageOperation> cPair = 
                    mp.target.getOperationManager().addMigrationOperation(mp.subset, mp.source, new StorageOperationCallback()
            {
                @Override public void onOperationStarted(long sTime)
                {
                    mp.subset.forEach(sF -> {
                        logger.logHierarchicalMigrationStart(sF, server, mp.source, mp.target, sTime);
                    });
                }
                
                @Override public void onOperationFinished(long sTime)
                {
                    for(FsFile sF : mp.subset)
                    {
                        sF.migratingProperty().set(false);
                        server.getFsManager().forceMount(mp.target, sF);
                        logger.logHierarchicalMigrationFinish(sF, server, mp.source, mp.target, sTime);
                    }
                }
            }, false, (prevPair != null));
            
            if(prevPair != null)
            {
                // after previous write operation is finished current
                // operation state changes from pending to running
                prevPair.first.addFollowingOperation(cPair.first);
                prevPair.first.addFollowingOperation(cPair.second);
            }
            
            prevPair = cPair;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override public void onBeforeAccess(FsFile file, ModelServerNode server, SimulationTask task, DfsSimulatorLogger logger, long sTime)
    {
        file.incrementAccessCounter();
        
        if(!file.isMigrating())
        {
            ArrayList<MigrationPlan> mp = new ArrayList<>();
            ServerStorage highestStor = this.int_getHighestAvailableStorage(file, server, mp);

            if(!mp.isEmpty())
            {
                if(task instanceof GetSimulationTask)
                {
                    this.beginMigrationProcess(mp, server, logger, sTime);
                }
                else
                {
                    // highest storage has enough space, write directly to it
                    if(mp.size() == 1)
                    {
                        logger.logHierarchicalUploadMove(file, server, highestStor, sTime);
                        server.getFsManager().forceMount(highestStor, file);
                    }
                    // need to migrate other files in order to accomodate
                    // file onto highest storage
                    else
                    {
                        this.beginMigrationProcess(mp, server, logger, sTime);
                    }
                }
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public void onAfterAccess(FsFile file, ModelServerNode server, SimulationTask task, DfsSimulatorLogger logger, long sTime) {}
    
    /**
     * Builds a migration plan in order to fit {@code file} onto {@code target}.
     * 
     * @param file target file
     * @param source source storage
     * @param target target storage
     * @param storageList list of available storage devices
     * @param server target server
     * @return migration plan
     */
    protected abstract List<MigrationPlan> buildMigrationPlansToFit(FsFile file, ServerStorage source, ServerStorage target, List<ServerStorage> storageList, ModelServerNode server);
    
}
