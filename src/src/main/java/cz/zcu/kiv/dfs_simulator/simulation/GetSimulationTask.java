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

import cz.zcu.kiv.dfs_simulator.persistence.InvalidPersistedStateException;
import cz.zcu.kiv.dfs_simulator.persistence.StatePersistable;
import cz.zcu.kiv.dfs_simulator.persistence.StatePersistableAttribute;
import cz.zcu.kiv.dfs_simulator.persistence.StatePersistableElement;
import cz.zcu.kiv.dfs_simulator.persistence.StatePersistenceLogger;
import cz.zcu.kiv.dfs_simulator.model.ByteSize;
import cz.zcu.kiv.dfs_simulator.model.ByteSizeUnits;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FsDirectory;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FsFile;
import cz.zcu.kiv.dfs_simulator.model.storage.replication.FsGlobalReplicationManager;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.image.Image;

/**
 * Get (download) simulation task.
 */
public class GetSimulationTask extends SimulationTask
{
    /**
     * Persistable identifier
     */
    public static final String PERSISTABLE_NAME = "get_task";

    /**
     * Get (download) simulation task.
     * 
     * @param file downloaded file
     */
    public GetSimulationTask(FsFile file)
    {
        super(file, SimulationTaskType.GET);
    }

    /**
     * {@inheritDoc}
     */
    // this should probably be handled in GUI 
    @Override public Image getImage()
    {
        return new Image(getClass().getClassLoader().getResourceAsStream("img/sim_download.png"));
    }

    /**
     * {@inheritDoc}
     */
    @Override public String getPersistableName()
    {
        return PERSISTABLE_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override public List<StatePersistable> getPersistableChildren()
    {
        return new ArrayList<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override public StatePersistableElement export(StatePersistenceLogger logger)
    {
        StatePersistableElement element = new StatePersistableElement(this.getPersistableName());
        
        element.addAttribute(new StatePersistableAttribute("path", file.getFullPath()));
        
        return element;
    }

    /**
     * {@inheritDoc}
     */
    @Override public void restoreState(StatePersistableElement state, StatePersistenceLogger logger, Object... args) throws InvalidPersistedStateException
    {
        if(state != null)
        {
            StatePersistableAttribute pathAttr = state.getAttribute("path");
            
            if(pathAttr != null && pathAttr.getValue().contains(FsDirectory.DIR_PATH_SEPARATOR))
            {
                // file
                if(!pathAttr.getValue().endsWith(FsDirectory.DIR_PATH_SEPARATOR))
                {
                    // try to find an existing file
                    FsFile exReplicaFile = FsGlobalReplicationManager.getReplicaInstance(pathAttr.getValue());
                    
                    if(exReplicaFile != null)
                    {
                        this.file = exReplicaFile;
                    }
                    // create a dummy file in order to simulate GET on a non existing file
                    else
                    {
                        int ei = pathAttr.getValue().lastIndexOf(FsDirectory.DIR_PATH_SEPARATOR);
                        String pn = pathAttr.getValue().substring(0, ei);
                        String fn = pathAttr.getValue().substring(ei + 1);

                        // remove trailing separator if it isnt root
                        if(pn.endsWith(FsDirectory.DIR_PATH_SEPARATOR) && pn.length() > 0)
                        {
                            pn = pn.substring(0, pn.length() - 1);
                        }

                        FsDirectory pd = new FsDirectory(pn);
                        FsFile f = new FsFile(fn, new ByteSize(0, ByteSizeUnits.B), pd);

                        this.file = f;
                    }
                }
                
                // dir
                else
                {
                    throw new InvalidPersistedStateException("Get simulation task cannot be a directory.");
                }
            }
        }
    }

}
