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
import cz.zcu.kiv.dfs_simulator.helpers.Helper;
import cz.zcu.kiv.dfs_simulator.model.ByteSize;
import cz.zcu.kiv.dfs_simulator.model.ByteSizeUnits;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FsDirectory;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FsFile;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.image.Image;

/**
 * Put (upload) simulation task.
 */
public class PutSimulationTask extends SimulationTask
{
    /**
     * Persistable identifier
     */
    public static final String PERSISTABLE_NAME = "put_task";

    /**
     * Put (upload) task.
     * 
     * @param file uploaded file
     */
    public PutSimulationTask(FsFile file)
    {
        super(file, SimulationTaskType.PUT);
    }

    /**
     * {@inheritDoc}
     */
    // this should probably be handled in GUI
    @Override public Image getImage()
    {
        return new Image(getClass().getClassLoader().getResourceAsStream("img/sim_upload.png"));
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
        element.addAttribute(new StatePersistableAttribute("size", ((file instanceof FsFile)? file.getSize().bytesProperty().get() + "" : "0")));
        
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
            StatePersistableAttribute sizeAttr = state.getAttribute("size");
            
            if((pathAttr != null && pathAttr.getValue().contains(FsDirectory.DIR_PATH_SEPARATOR)) && 
                    (sizeAttr != null && Helper.isLong(sizeAttr.getValue())))
            {
                // dir
                if(!pathAttr.getValue().endsWith(FsDirectory.DIR_PATH_SEPARATOR))
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
                    FsFile f = new FsFile(fn, new ByteSize(Long.parseLong(sizeAttr.getValue()), ByteSizeUnits.B), pd);
                    
                    this.file = f;
                }
                // file
                else
                {
                    throw new InvalidPersistedStateException("Put simulation task cannot be a directory.");                    
                }
            }
        }
    }
    
}
