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

package cz.zcu.kiv.dfs_simulator.view.context;

import cz.zcu.kiv.dfs_simulator.model.connection.ModelNodeConnection;
import cz.zcu.kiv.dfs_simulator.view.content.FxModelNode;
import cz.zcu.kiv.dfs_simulator.view.content.connection.FxNodeConnectionWrapper;
import java.util.Stack;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;

/**
 * Graphical connection table of a given node.
 */
public class FxConnectionTable extends TableView<ModelNodeConnection>
{
    /**
     * Deleted connections (history)
     */
    protected final Stack<FxNodeConnectionWrapper> deletedItems = new Stack<>();
    /**
     * If any items were deleted
     */
    protected final BooleanProperty deletedItemsProperty = new SimpleBooleanProperty();
    
    /**
     * Associated node
     */
    protected FxModelNode fxNode;
    
    /**
     * Graphical connection table of a given node.
     */
    public FxConnectionTable()
    {
        this.init();
    }
    
    /**
     * Initiate row factory and context menu.
     */
    private void init()
    {
        setRowFactory(this::rowFactory);
        setContextMenu(new ContextMenu(this.getUndoDeleteMenuItem()));
    }
    
    /**
     * Set associated node.
     * 
     * @param fxNode node
     */
    public void setFxNode(FxModelNode fxNode)
    {
        this.fxNode = fxNode;
    }
    
    /**
     * Set row factory with proper context menu.
     * 
     * @param view view
     * @return row
     */
    protected TableRow<ModelNodeConnection> rowFactory(TableView<ModelNodeConnection> view)
    {
        TableRow<ModelNodeConnection> row = new TableRow<>();
        
        final MenuItem alter = this.getAlterLinkMenuItem();
        final MenuItem delete = this.getDeleteLinkMenuItem();
        final MenuItem undoDelete = this.getUndoDeleteMenuItem();
        
        final ContextMenu cm = new ContextMenu(alter, delete, undoDelete);
        
        row.contextMenuProperty().bind(
                Bindings.when(row.emptyProperty())
                        .then((ContextMenu) null)
                        .otherwise(cm));
                
        return row;
    }
    
    /**
     * Returns alter link menu item. Allows editing connection.
     * 
     * @return alter link menu item
     */
    private MenuItem getAlterLinkMenuItem()
    {
        final MenuItem alter = new MenuItem("Alter link");
        
        alter.setOnAction(event ->
        {
            ModelNodeConnection selected = getSelectionModel().getSelectedItem();
            
            if(selected != null)
            {
                FxNodeConnectionWrapper connWrapper = 
                        fxNode.getGraphLayoutPane().getFxConnectionWrapperManager().getNodeConnectionWraper(selected);
                
                if(connWrapper != null)
                {
                    // delegate event to fx node link
                    connWrapper.getFxNodeLink().handleUserAlterLink();
                }
            }
        });
        
        return alter;
    }
    
    /**
     * Returns delete menu item - delete connection.
     * 
     * @return delete menu item
     */
    private MenuItem getDeleteLinkMenuItem()
    {
        final MenuItem delete = new MenuItem("Delete link");
        
        delete.setOnAction(event ->
        {
            ModelNodeConnection selected = getSelectionModel().getSelectedItem();
            
            if(selected != null)
            {
                FxNodeConnectionWrapper connWrapper = 
                        fxNode.getGraphLayoutPane().getFxConnectionWrapperManager().getNodeConnectionWraper(selected);
                
                if(connWrapper != null)
                {
                    // delegate event to fx node link
                    connWrapper.getFxNodeLink().handleUserDeleteLink();
                    
                    // push deleted wrapper onto stack and set property flag
                    deletedItems.push(connWrapper);
                    deletedItemsProperty.set(true);
                }
            }
        });
        
        return delete;
    }
    
    /**
     * Returns undo delete menu item - undo deleted connections.
     * 
     * @return undo delete menu item
     */
    private MenuItem getUndoDeleteMenuItem()
    {
        final MenuItem undoDelete = new MenuItem("Undo delete");
        
        undoDelete.setOnAction(event ->
        {
            if(!deletedItems.isEmpty())
            {
                final FxNodeConnectionWrapper connWrapper = deletedItems.pop();
                
                // since the connection was previously destroyed, we have to restore
                // it, so that the underlying nodes have synced data
                connWrapper.restore();
                
                fxNode.getGraphLayoutPane().handleUserAddNodeConnection(connWrapper);
                
                if(deletedItems.isEmpty())
                {
                    deletedItemsProperty.set(false);
                }
            }
        });
        
        // disable by default
        undoDelete.disableProperty().bind(deletedItemsProperty.not());
        
        return undoDelete;
    }
}
