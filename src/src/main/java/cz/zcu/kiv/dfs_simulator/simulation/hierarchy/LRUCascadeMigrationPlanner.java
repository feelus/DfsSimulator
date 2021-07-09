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

import cz.zcu.kiv.dfs_simulator.model.ByteSize;
import cz.zcu.kiv.dfs_simulator.model.ByteSizeUnits;
import cz.zcu.kiv.dfs_simulator.model.ModelServerNode;
import cz.zcu.kiv.dfs_simulator.model.storage.ServerStorage;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FsFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Migration planner. Uses LRU method to move files between storages.
 */
public class LRUCascadeMigrationPlanner extends HierarchicalPlanner
{
    /**
     * Select smallest subset of size {@code size} with lowest possible
     * access count.
     * 
     * @param files files
     * @param size minimum subset size
     * @return if found list of files, else null
     */
    protected List<FsFile> getLRUSubestWithSize(List<FsFile> files, ByteSize size)
    {
        ArrayList<FsFile> filesOrdered = new ArrayList<>(files);
        Collections.sort(filesOrdered, (a, b) -> Integer.compare(a.getAccessCount(), b.getAccessCount()));
        
        long cumBytes = 0;
        
        int i = 0;
        int maxI = filesOrdered.size() - 1;
        
        ArrayList<FsFile> LRUsubset = new ArrayList<>();
        while(cumBytes < size.bytesProperty().get() && i <= maxI)
        {
            cumBytes += filesOrdered.get(i).getSize().bytesProperty().get();
            LRUsubset.add(filesOrdered.get(i));
            i++;
        }
        
        if(cumBytes >= size.bytesProperty().get())
        {
            return LRUsubset;
        }
        
        return null;
    }
    
    /**
     * Find a subset of files that needs to be moved from storage {@code storage}
     * onto any other storage in order to fit {@code subset}.
     * 
     * @param subset files
     * @param storage target storage
     * @param server target server
     * @return list of files that need to be moved elsewhere or null if storage
     * has enough space
     */
    private List<FsFile> fitSubsetOntoStorage(List<FsFile> subset, ServerStorage storage, ModelServerNode server)
    {
        List<FsFile> storageFiles = server.getFsManager().getStorageMountedFiles(storage);
        // filter out files that are currently migrating
        List<FsFile> filtered = storageFiles.stream().filter(sf -> !sf.isMigrating()).collect(Collectors.toList());
        
        // calcualte how much bytes we need to fit
        long subsetBytes = subset.stream().mapToLong(sf -> sf.getSize().bytesProperty().get()).sum();
        long reqBytes = subsetBytes - getStorageAvailableSpace(storage, server).bytesProperty().get();
        
        if(reqBytes > 0)
        {
            return this.getLRUSubestWithSize(filtered, new ByteSize(reqBytes, ByteSizeUnits.B));
        }
        
        return new ArrayList<>();
    }
    
    /**
     * Internal method. See {@link #buildMigrationPlansToFit(
     * cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FsFile, 
     * cz.zcu.kiv.dfs_simulator.model.storage.ServerStorage, 
     * cz.zcu.kiv.dfs_simulator.model.storage.ServerStorage, 
     * java.util.List, cz.zcu.kiv.dfs_simulator.model.ModelServerNode)}.
     * 
     * @param subset subset of files
     * @param source source storage
     * @param target target storage
     * @param storageList list of available storage devices
     * @param server target server
     * @return migration plan
     */
    private List<MigrationPlan> int_buildMigrationPlanToFit(List<FsFile> subset, ServerStorage source, ServerStorage target, List<ServerStorage> storageList, ModelServerNode server)
    {
        // if migratDownSubset isnt empty we are migrating from TARGET to lower storage
        List<FsFile> migrateDownSubset = this.fitSubsetOntoStorage(subset, target, server);
        List<MigrationPlan> migrationPlanList = new ArrayList<>();
        
        if(migrateDownSubset != null)
        {
            // add migration plan for desired subset onto target
            MigrationPlan mp = new MigrationPlan();
            mp.source = source;
            mp.target = target;
            mp.subset = subset;
            migrationPlanList.add(mp);
            
            // target has enough space
            if(migrateDownSubset.isEmpty())
            {
                return migrationPlanList;
            }
            // we need to shift some files from target onto a lower storage
            else
            {
                // get current storage index
                int targetIndex = storageList.indexOf(target);
                
                // check if we have storage below us, else we couldnt migrate
                if(targetIndex > 0)
                {
                    List<MigrationPlan> subMigrationPlanList = 
                            this.int_buildMigrationPlanToFit(migrateDownSubset, target, storageList.get(targetIndex - 1), storageList, server);
                    
                    // if we can fit migrateDownSubset onto lower storage
                    if(subMigrationPlanList != null)
                    {
                        // merge it with our list
                        migrationPlanList.addAll(subMigrationPlanList);
                        
                        // reversed order since migrations that need to happen
                        // first were added latest
                        Collections.reverse(migrationPlanList);
                        
                        return migrationPlanList;
                    }
                }
            }
            
        }
        
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override protected List<MigrationPlan> buildMigrationPlansToFit(FsFile file, ServerStorage source, ServerStorage target, List<ServerStorage> storageList, ModelServerNode server)
    {
        List<FsFile> migrationSubset = new ArrayList<>();
        migrationSubset.add(file);
        
        return this.int_buildMigrationPlanToFit(migrationSubset, source, target, storageList, server);
    }
    
}
