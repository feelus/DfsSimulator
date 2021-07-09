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

package cz.zcu.kiv.dfs_simulator.model;

import cz.zcu.kiv.dfs_simulator.persistence.InvalidPersistedStateException;
import cz.zcu.kiv.dfs_simulator.persistence.StatePersistable;
import cz.zcu.kiv.dfs_simulator.persistence.StatePersistableAttribute;
import cz.zcu.kiv.dfs_simulator.persistence.StatePersistableElement;
import cz.zcu.kiv.dfs_simulator.persistence.StatePersistenceLogger;
import cz.zcu.kiv.dfs_simulator.model.storage.ServerStorageManager;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FsDirectory;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.ServerFileSystemManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Server node.
 */
public class ModelServerNode extends ModelNode
{
    /**
     * Persistable identificator
     */
    protected static final String PERSISTABLE_NAME = "server_node";
    /**
     * Persistable identificator of file structure
     */
    protected static final String PERSISTABLE_NAME_FS = "file_structure";
    
    /**
     * Root directory name
     */
    public static final String ROOT_DIR_NAME = FsDirectory.DIR_PATH_SEPARATOR;
    
    /**
     * Manager of all storage devices on this server
     */
    protected final ServerStorageManager storageManager;
    /**
     * Manager of this server's file system
     */
    protected final ServerFileSystemManager fsManager;

    /**
     * Root filesystem directory
     */
    protected final FsDirectory rootDir;
    
    /**
     * Creates new instance of a server node. Instance can be registered to 
     * {@link ModelNodeRegistry} by setting {@code register} flag.
     * 
     * @param rootDir root directory
     * @param register register flag
     */
    public ModelServerNode(FsDirectory rootDir, boolean register)
    {
        super(register);
        
        this.rootDir = rootDir;
        this.fsManager = new ServerFileSystemManager(this);
        this.storageManager = new ServerStorageManager(this);
    }
    
    /**
     * Creates new instance of a server node that will be registered to {@link ModelNodeRegistry}.
     * 
     * @param rootDir root directory
     */
    public ModelServerNode(FsDirectory rootDir)
    {
        this(rootDir, true);
    }
    
    /**
     * Creates new instance of a client node. Instance can be registered to 
     * {@link ModelNodeRegistry} by setting {@code register} flag. Root directory
     * defaults to {@link #ROOT_DIR_NAME}.
     * 
     * @param register register flag
     */
    public ModelServerNode(boolean register)
    {
        this(new FsDirectory(ROOT_DIR_NAME), register);
    }
    
    /**
     * Creates new instance of a server node that will be registered to {@link ModelNodeRegistry}.
     */
    public ModelServerNode()
    {
        this(true);
    }
    
    /**
     * Returns storage manager.
     * 
     * @return storage manager
     */
    public ServerStorageManager getStorageManager()
    {
        return this.storageManager;
    }
    
    /**
     * Returns filesystem manager.
     * 
     * @return file system manager
     */
    public ServerFileSystemManager getFsManager()
    {
        return this.fsManager;
    }
    
    /**
     * Returns root directory.
     * 
     * @return root directory
     */
    public FsDirectory getRootDir()
    {
        return this.rootDir;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public NodeType getType()
    {
        return NodeType.SERVER;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public List<StatePersistable> getPersistableChildren()
    {
        List<StatePersistable> l = new ArrayList<>();
        
        l.add(new StatePersistable()
        {
            @Override public List<StatePersistable> getPersistableChildren()
            {
                List<StatePersistable> sl = new ArrayList<>();
                sl.add(rootDir);
                
                return sl;
            }

            @Override public StatePersistableElement export(StatePersistenceLogger logger)
            {
                return new StatePersistableElement(this.getPersistableName());
            }

            @Override public void restoreState(StatePersistableElement state, StatePersistenceLogger logger, Object... args)
            {
                
            }

            @Override public String getPersistableName()
            {
                return PERSISTABLE_NAME_FS;
            }
        });
        
        l.add(this.storageManager);
        l.add(this.fsManager);
        
        return l;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public StatePersistableElement export(StatePersistenceLogger logger)
    {
        StatePersistableElement element = new StatePersistableElement(this.getPersistableName());
        
        element.addAttribute(new StatePersistableAttribute("id", "" + nodeID.get()));
        
        return element;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public void restoreState(StatePersistableElement state, StatePersistenceLogger logger, Object... args) throws InvalidPersistedStateException
    {
        if(state != null)
        {
            StatePersistableAttribute id = state.getAttribute("id");
            
            if(id != null && !id.getValue().isEmpty())
            {
                try
                {
                    changeId(id.getValue());
                }
                catch (LabelException ex)
                {
                    throw new InvalidPersistedStateException(ex + ": " + state);
                } 
            }
            
            this.restoreFileStructure(state.getElement(PERSISTABLE_NAME_FS), logger);
            
            this.storageManager.restoreState(state.getElement(this.storageManager.getPersistableName()), logger);
            this.fsManager.restoreState(state.getElement(this.fsManager.getPersistableName()), logger);
       }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public String getPersistableName()
    {
        return PERSISTABLE_NAME;
    }
    
    /**
     * Restore filesystem structure from {@code state}.
     * 
     * @param state persisted state
     * @param logger information logger
     * @throws InvalidPersistedStateException thrown when {@code state} is ivalid
     */
    private void restoreFileStructure(StatePersistableElement state, StatePersistenceLogger logger) throws InvalidPersistedStateException
    {
        if(state != null)
        {
            // first directory has to be root
            StatePersistableElement rootElem = state.getElement(FsDirectory.PERSISTABLE_NAME);
            
            if(rootElem != null)
            {
                this.rootDir.restoreState(rootElem, logger, this.fsManager);
            }
            else
            {
                throw new InvalidPersistedStateException("Unexpected root directory, expected " + FsDirectory.DIR_PATH_SEPARATOR);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public String toString()
    {
        return "ServerNode " + this.nodeID.get();
    }
}
