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

import cz.zcu.kiv.dfs_simulator.helpers.FxHelper;
import cz.zcu.kiv.dfs_simulator.view.DragContainer;
import cz.zcu.kiv.dfs_simulator.view.content.FxModelNode;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;

/**
 * Library node. This node is dragged from the library (toolbar) panel onto
 * layout pane and after dropping, a permanent node is created.
 */
public abstract class LibraryNode extends AnchorPane
{
    /**
     * Node icon
     */
    @FXML protected ImageView imageView;

    /**
     * Library node
     */
    public LibraryNode()
    {
        this.fxInit();
        
        this.initDragHandlers();
        this.addDraggableHint();
    }
    
    /**
     * Load FXML schema
     */
    private void fxInit()
    {
        FxHelper.loadFXMLAndSetController(getClass().getClassLoader().getResource("fxml/view/toolbar/LibraryNode.fxml"), this);
    }
    
    /**
     * Initialize node icon
     */
    @FXML public void initialize()
    {
        this.initializeNodeViewImage();
    }

    /**
     * Handle mouse drag events.
     */
    private void initDragHandlers()
    {
        // drag detected, start drag operations and set drag content
        // any further drag events will be handled by target node and parent node
        setOnDragDetected(mouseEvent -> {
            // initiate drag
            final Dragboard db = LibraryNode.this.startDragAndDrop(TransferMode.ANY);
            // create image snapshot for draggable view
            final ImageView iv = new ImageView(LibraryNode.this.snapshot(null, null));

            // set drag content
            ClipboardContent content = new ClipboardContent();
            DragContainer container = new DragContainer();
            
            content.put(DragContainer.ADD_NODE, container);
            
            db.setContent(content);
            db.setDragView(iv.getImage());
            
            mouseEvent.consume();
        });
    }

    /**
     * When drag hint - change cursor to {@link Cursor#HAND}.
     */
    private void addDraggableHint()
    {
        setCursor(Cursor.HAND);
    }

    /**
     * Get node icon.
     * 
     * @return icon
     */
    protected Image getImage()
    {
        return this.imageView.getImage();
    }
    
    /**
     * Initialize node icon.
     */
    abstract protected void initializeNodeViewImage();
    
    /**
     * Get an instance of permanent node based on this node's type.
     * 
     * @return permanent node
     */
    abstract public FxModelNode getPermanentNode();
}
