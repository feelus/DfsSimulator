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

import cz.zcu.kiv.dfs_simulator.model.storage.ServerStorage;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FsFile;
import java.util.List;

/**
 * Migration plan - list of files that will be migrated.
 */
public class MigrationPlan
{
    /**
     * List of files that will be migrated
     */
    public List<FsFile> subset = null;
    /**
     * Source storage
     */
    public ServerStorage source = null;
    /**
     * Target storage
     */
    public ServerStorage target = null;
}
