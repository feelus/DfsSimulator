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

package cz.zcu.kiv.dfs_simulator.view.context.server;

import cz.zcu.kiv.dfs_simulator.helpers.FxHelper;
import cz.zcu.kiv.dfs_simulator.model.ByteSize;
import cz.zcu.kiv.dfs_simulator.model.ModelServerNode;
import cz.zcu.kiv.dfs_simulator.model.storage.ServerStorage;
import java.util.Optional;
import java.util.Stack;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;

/**
 * Server storage table.
 */
public class FxStorageTable extends TableView<ServerStorage>
{
    /**
     * Deleted storage items
     */
    protected final Stack<ServerStorage> deletedItems = new Stack<>();
    /**
     * If there are any deleted storage items
     */
    protected final BooleanProperty deletedItemsProperty = new SimpleBooleanProperty();
    
    /**
     * Underlying server node
     */
    protected ModelServerNode serverNode;
    
    /**
     * Server storage table.
     */
    public FxStorageTable()
    {
        this.init();
    }

    /**
     * Initialize row factory and set context menu.
     */
    private void init()
    {
        setRowFactory(this::rowFactory);        
        setContextMenu(new ContextMenu(this.getUndoDeleteMenuItem()));
    }
    
    /**
     * Set underlying server node.
     * 
     * @param serverNode server node
     */
    public void setServerNode(ModelServerNode serverNode)
    {
        this.serverNode = serverNode;
    }
    
    /**
     * Row factory - adds context menu to rows.
     * 
     * @param view view
     * @return row
     */
    protected TableRow<ServerStorage> rowFactory(TableView<ServerStorage> view)
    {
        TableRow<ServerStorage> row = new TableRow<>();

        final MenuItem edit = getEditMenuItem();
        final MenuItem delete = getDeleteMenuItem();
        final MenuItem undoDelete = getUndoDeleteMenuItem();
        
        final ContextMenu cm = new ContextMenu();
        
        cm.getItems().addAll(edit, delete, undoDelete);
        
        row.contextMenuProperty().bind(
                Bindings.when(row.emptyProperty())
                .then((ContextMenu) null)
                .otherwise(cm)
        );
        
        return row;
    }
    
    /**
     * Returns edit storage menu item - edit storage size and or capacity.
     * 
     * @return edit storage item
     */
    protected MenuItem getEditMenuItem()
    {
        MenuItem edit = new MenuItem("Edit storage");
        
        edit.setOnAction(event ->
        {
            ServerStorage storage = getSelectionModel().getSelectedItem();
            
            if(storage != null)
            {
                FxFsStorageDialog dialog = new FxFsStorageDialog();
                dialog.setStorage(storage);
                FxFsStorageDialog.setUpAndShowDialog(dialog, getScene().getWindow(), "Edit storage");
                
                if(dialog.isConfirmed())
                {
                    // check if we can reduce storage size
                    ByteSize usedSize = serverNode.getFsManager().getStorageUsedSize(storage);
                    ByteSize newSize = dialog.getSize();
                    
                    if(usedSize.bytesProperty().get() <= newSize.bytesProperty().get())
                    {
                        storage.getMaximumSpeed().bpsProperty().set(dialog.getSpeed().bpsProperty().get());
                        storage.getSize().bytesProperty().set(newSize.bytesProperty().get());
                    }
                    else
                    {
                        Alert alertDialog = FxHelper.getErrorDialog("Error editing storage", 
                                "Storage size too small", 
                                "Storage has " + usedSize.getHumanReadableFormat() + " used space, can not set its capacity to " + newSize.getHumanReadableFormat());
                        
                        alertDialog.showAndWait();
                    }
                }
            }
        });
        
        return edit;
    }
    
    /**
     * Returns delete storage item - delete existing storage.
     * 
     * @return delete item
     */
    protected MenuItem getDeleteMenuItem()
    {
        MenuItem delete = new MenuItem("Delete");
        
        delete.setOnAction(event ->
        {
            ServerStorage storage = getSelectionModel().getSelectedItem();
            
            if(storage != null)
            {
                // alert user that this storage is mounted and ask for confirmation
                if(serverNode.getFsManager().isStorageUsed(storage))
                {
                    Dialog dialog = FxHelper.getConfirmationDialog("Confirm storage delete", 
                            "Deleting storage", 
                            "This storage is mounted on on ore more FS objects. Mount points will be deleted as well and cannot be restored.");

                    Optional<ButtonType> result = dialog.showAndWait();

                    if (result.isPresent() && result.get() != ButtonType.OK)
                    {
                        return;
                    }
                    
                    // remove storage from fs manager
                    serverNode.getFsManager().removeStorage(storage);
                }
                
                getItems().remove(storage);
                deletedItems.push(storage);
                
                // notify undo delete buttons
                deletedItemsProperty.set(true);
            }
        });
        
        return delete;
    }
    
    /**
     * Returns undo delete menu item - restore last deleted storage.
     * 
     * @return undo delete item
     */
    protected MenuItem getUndoDeleteMenuItem()
    {
        MenuItem undoDelete = new MenuItem("Undo delete");
        
        undoDelete.setOnAction(event ->
        {
            if(!deletedItems.isEmpty())
            {
                getItems().add(deletedItems.pop());
                
                if(deletedItems.isEmpty())
                {
                    deletedItemsProperty.set(false);
                }
            }
        });
        
        // disable it by default and add listener
        undoDelete.disableProperty().bind(deletedItemsProperty.not());
        
        return undoDelete;
    }
}
