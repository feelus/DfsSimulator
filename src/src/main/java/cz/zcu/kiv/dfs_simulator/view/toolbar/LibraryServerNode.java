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

package cz.zcu.kiv.dfs_simulator.view.toolbar;

import cz.zcu.kiv.dfs_simulator.view.content.FxModelServerNode;
import cz.zcu.kiv.dfs_simulator.view.content.FxModelNode;
import cz.zcu.kiv.dfs_simulator.view.content.connection.FxNodeLink;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.scene.image.Image;

/**
 * Library server node. Snapshot of this node is dragged from the library onto layout pane
 * and afterwards destroyed.
 */
public class LibraryServerNode extends LibraryNode
{
    /**
     * Toolbar (library) panel
     */
    private final ToolbarPanel toolbarPanel;
    
    /**
     * Library server node.
     * 
     * @param toolbarPanel toolbar panel
     */
    public LibraryServerNode(ToolbarPanel toolbarPanel)
    {
        this.toolbarPanel = toolbarPanel;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public FxModelNode getPermanentNode()
    {
        return new FxModelServerNode(this.toolbarPanel.getGraphLayoutPane());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override protected void initializeNodeViewImage()
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
}
