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

import cz.zcu.kiv.dfs_simulator.persistence.StatePersistable;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FsFile;
import javafx.scene.image.Image;

/**
 * Abstract simulation task.
 */
public abstract class SimulationTask implements StatePersistable
{
    /**
     * Simulation task file
     */
    protected FsFile file;
    /**
     * Simulation task type
     */
    protected final SimulationTaskType type;

    /**
     * Simulation task.
     * 
     * @param file task file
     * @param type task type
     */
    public SimulationTask(FsFile file, SimulationTaskType type)
    {
        this.file = file;
        this.type = type;
    }

    /**
     * Get task file.
     * 
     * @return task file
     */
    public FsFile getFile()
    {
        return this.file;
    }
    
    /**
     * Get task type.
     * 
     * @return task type
     */
    public SimulationTaskType getType()
    {
        return this.type;
    }
    
    /**
     * Textual representation of task.
     * 
     * @return string representation
     */
    @Override public String toString()
    {
        return type.toString().toUpperCase() + " simulation task, target: " + 
                file.getFullPath() + " of size " + file.getSize().getHumanReadableFormat() + ".";
    }
    
    /**
     * Get task icon.
     * 
     * @return icon
     */
    abstract public Image getImage();
}
