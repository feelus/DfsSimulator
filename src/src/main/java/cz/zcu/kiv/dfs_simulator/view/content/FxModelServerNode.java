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

package cz.zcu.kiv.dfs_simulator.view.content;

import cz.zcu.kiv.dfs_simulator.model.ModelNodeRegistry;
import cz.zcu.kiv.dfs_simulator.persistence.StatePersistable;
import cz.zcu.kiv.dfs_simulator.model.ModelServerNode;
import cz.zcu.kiv.dfs_simulator.view.content.connection.FxNodeLink;
import cz.zcu.kiv.dfs_simulator.view.storage.FxServerStorageManager;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.image.Image;

/**
 * Graphical server node.
 */
public class FxModelServerNode extends FxModelNode
{
    /**
     * Persistable identifier
     */
    public static final String PERSISTABLE_NAME = "fx_server_node";
    
    /**
     * Graphical storage manager
     */
    private final FxServerStorageManager fxStorageManager;

    /**
     * Graphical server node. Creates a new instance of {@link ModelServerNode}
     * used as underlying node.
     * 
     * @param graphLayoutPane graph layout pane (where this node will be placed)
     * @param registerModelNode if node should be registered via {@link ModelNodeRegistry#registerNode(cz.zcu.kiv.dfs_simulator.model.ModelNode)}
     */
    public FxModelServerNode(ModelGraphLayoutPane graphLayoutPane, boolean registerModelNode)
    {
        super(new ModelServerNode(registerModelNode), graphLayoutPane);
        
        this.fxStorageManager = new FxServerStorageManager(((ModelServerNode) node).getStorageManager());
    }
    
    /**
     * Graphical server node. Creates a new instance of {@link ModelServerNode}
     * used as underlying node, which is automatically registered by 
     * {@link ModelNodeRegistry#registerNode(cz.zcu.kiv.dfs_simulator.model.ModelNode)}.
     
     * @param graphLayoutPane graph layout pane (where this node will be placed)
     */
    public FxModelServerNode(ModelGraphLayoutPane graphLayoutPane)
    {
        this(graphLayoutPane, true);
    }
    
    /**
     * Get graphical storage manager.
     * 
     * @return storage manager
     */
    public FxServerStorageManager getFxStorageManager()
    {
        return this.fxStorageManager;
    }
    
    /**
     * Get underlying server node.
     * 
     * @return server node
     */
    public ModelServerNode getServerNode()
    {
        return (ModelServerNode) node;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public FxModelNode duplicate()
    {
        // @TODO duplicate storage/connections..
        return new FxModelServerNode(graphLayoutPane);
    }
        
    /**
     * {@inheritDoc}
     */
    @Override protected void initiateImageViewImage()
    {
        try
        {
            Image img = new Image(getClass().getClassLoader().getResourceAsStream("img/server.png"));
            
            imageView.setImage(img);
        }
        catch(NullPointerException ex)
        {
            Logger.getLogger(FxNodeLink.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public List<StatePersistable> getPersistableChildren()
    {
        List<StatePersistable> l = new ArrayList<>();
        l.add(node);
        
        return l;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public String getPersistableName()
    {
        return PERSISTABLE_NAME;
    }
}
