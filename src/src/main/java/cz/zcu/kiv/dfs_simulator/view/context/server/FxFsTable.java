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
import cz.zcu.kiv.dfs_simulator.model.ModelServerNode;
import cz.zcu.kiv.dfs_simulator.model.storage.ServerStorage;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FsDirectory;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FsFile;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FileSystemObject;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.NotEnoughSpaceLeftException;
import cz.zcu.kiv.dfs_simulator.model.storage.replication.FsGlobalReplicationManager;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.binding.Bindings;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

// @TODO d&d comments - drag and drop features
// main problem with drag and drop is that we can drag and drop replicated files
// which shouldnt be possible - maybe when dragged - remove replica from this server?

// d&d public class FxFsTable extends DraggableTreeTableView<FileSystemObject>
/**
 * File structure table.
 */
public class FxFsTable extends TreeTableView<FileSystemObject>
{
    /**
     * Directory icon
     */
    private final Image dirIconImage;
    /**
     * File icon
     */
    private final Image fileIconImage;
    
    /**
     * Parent dialog (server dialog)
     */
    protected FxServerNodeContextDialog parentDialog;
    /**
     * Underlying node
     */
    protected ModelServerNode serverNode;
    
    /**
     * File structure table.
     */
    public FxFsTable()
    {        
        this.dirIconImage = new Image(getClass().getClassLoader().getResourceAsStream("img/fs_dir.png"));
        this.fileIconImage = new Image(getClass().getClassLoader().getResourceAsStream("img/fs_file.png"));
        
        this.init();
    }
    
    /**
     * Initiate row factory.
     */
    private void init()
    {
        setRowFactory(this::rowFactory);
    }
    
    /**
     * Set underlying parent dialog (server dialog).
     * 
     * @param parentDialog parent dialog
     */
    protected void setParentDialog(FxServerNodeContextDialog parentDialog)
    {
        this.parentDialog = parentDialog;
    }
    
    /**
     * Set underlying server node.
     * 
     * @param serverNode server node
     */
    protected void setServerNode(ModelServerNode serverNode)
    {
        this.serverNode = serverNode;
    }
    
    /**
     * Initiate dialog - necessary to build table structures.
     * 
     * @param serverNode server node
     * @param parentDialog parent dialog (server context dialog)
     */
    public void init(ModelServerNode serverNode, FxServerNodeContextDialog parentDialog)
    {
        this.setParentDialog(parentDialog);
        this.setServerNode(serverNode);
        
        addFileStructureItem(this.serverNode.getRootDir(), null);
    }
    
    /**
     * Add file structure item into FS tree.
     * 
     * @param object item
     * @param parent parent directory
     */
    private void addFileStructureItem(FileSystemObject object, TreeItem<FileSystemObject> parent)
    {
        TreeItem<FileSystemObject> tObject;
        
        if(object instanceof FsDirectory)
        {
            FsDirectory dir = (FsDirectory) object;
            
            tObject = new TreeItem<>(dir, new ImageView(dirIconImage));
            
            // add to parent if its not null
            if(parent != null)
            {
                parent.getChildren().add(tObject);
            }
            
            if(getRoot() == null)
            {
                setRoot(tObject);
            }
            
            if(!dir.getChildren().isEmpty())
            {
                tObject.setExpanded(true);
                
                dir.getChildren().forEach(c -> this.addFileStructureItem(c, tObject));
            }
        }
        else
        {
            tObject = new TreeItem<>(object, new ImageView(fileIconImage));
            // parent SHOULDNT be null here
            parent.getChildren().add(tObject);
        }
    }
    
    /**
     * Row factory for FS structure table rows - adds context menu.
     * 
     * @param view view
     * @return row
     */
    // d&d @Override protected TreeTableRow<FileSystemObject> rowFactory(TreeTableView<FileSystemObject> view)
    protected TreeTableRow<FileSystemObject> rowFactory(TreeTableView<FileSystemObject> view)
    {
        // d&d TreeTableRow<FileSystemObject> row = super.rowFactory(view);
        TreeTableRow<FileSystemObject> row = new TreeTableRow<>();
        
        row.emptyProperty().addListener((ov, oldVal, newVal) ->
            {
                if(newVal != null)
                {
                    final MenuItem newDirectory = getFileStructureNewDirectoryMenuItem();
                    final MenuItem newFile = getFileStructureNewFileMenuItem();
                    final MenuItem edit = getEditMenuItem();
                    final MenuItem selectMountDevice = getFileStructureSelectMountDeviceMenuItem();
                    final MenuItem delete = getFileStructureDeleteMenuItem();
                    final ContextMenu cm;
                    
                    cm = new ContextMenu(newDirectory, newFile, edit, selectMountDevice, delete);
                    
                    row.contextMenuProperty().bind(
                            Bindings.when(row.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(cm));
                }
            });
        
        return row;
    }
    
    /**
     * Returns new subdirectory menu item - add new subdirectory to selected
     * parent directory.
     * 
     * @return new subdirectory menu item
     */
    private MenuItem getFileStructureNewDirectoryMenuItem()
    {
        final MenuItem newDirectory = new MenuItem("New subdirectory");
        
        newDirectory.setOnAction(event ->
        {
            TreeItem<FileSystemObject> selected = getSelectionModel().getSelectedItem();
            
            if(selected == null)
            {
                return;
            }
            
            FsDirectory parentDir;
            TreeItem<FileSystemObject> parentItem;
            
            //  if its a directory add it directly to it
            if(selected.getValue() instanceof FsDirectory)
            {
                parentDir = (FsDirectory) selected.getValue();
                parentItem = selected;
            }
            // if its a file add it to its parent
            else
            {
                parentDir = selected.getValue().getParent();
                parentItem = selected.getParent();
            }
            
            FxFsDirectoryDialog dialog = new FxFsDirectoryDialog();
            FxFsDirectoryDialog.setUpAndShowDialog(dialog, getScene().getWindow(), "Create new subdirectory");
            
            if(dialog.isConfirmed())
            {
                FsDirectory directory = new FsDirectory(dialog.getName(), parentDir);
                ImageView dirIcon = new ImageView(dirIconImage);

                try
                {
                    serverNode.getFsManager().addDirectoryChild(parentDir, directory);
                    parentItem.getChildren().add(new TreeItem<>(directory, dirIcon));

                    ServerStorage parentStor = serverNode.getFsManager().getFsObjectMountDevice(parentDir);
                    // check if parent is mounted
                    if(parentStor != null)
                    {
                        directory.onMountDeviceChanged(parentStor);
                    }

                    // expand parent
                    parentItem.setExpanded(true);

                }
                // should never happen since directory has 0 size
                catch (NotEnoughSpaceLeftException ex)
                {
                    Logger.getLogger(FxFsTable.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        return newDirectory;
    }
    
    /**
     * Returns new file menu item - adds file to selected parent directory.
     * 
     * @return new file menu item
     */
    private MenuItem getFileStructureNewFileMenuItem()
    {
        final MenuItem newFile = new MenuItem("New file");
        
        newFile.setOnAction(event ->
        {
            TreeItem<FileSystemObject> selected = getSelectionModel().getSelectedItem();
            
            if(selected == null)
            {
                return;
            }
            
            FsDirectory parentDir;
            TreeItem<FileSystemObject> parentItem;
            
            //  if its a directory add it directly to it
            if(selected.getValue() instanceof FsDirectory)
            {
                parentDir = (FsDirectory) selected.getValue();
                parentItem = selected;
            }
            // if its a file add it to its parent
            else
            {
                parentDir = selected.getValue().getParent();
                parentItem = selected.getParent();
            }
            
            FxFsFileDialog dialog = new FxFsFileDialog(this.serverNode, parentDir);
            FxFsFileDialog.setUpAndShowDialog(dialog, getScene().getWindow(), "Create new file");
            
            if(dialog.isConfirmed())
            {
                FsFile file = dialog.getFile();
                
                if(file != null)
                {
                    ImageView fileIcon = new ImageView(fileIconImage);

                    parentItem.getChildren().add(new TreeItem<>(file, fileIcon));
                    parentItem.setExpanded(true);
                }
                
            }
        });
        
        return newFile;
    }
    
    /**
     * Returns edit menu item - edit selected FS structure object.
     * 
     * @return edit menu item
     */
    private MenuItem getEditMenuItem()
    {
        final MenuItem edit = new MenuItem("Edit");
        
        edit.setOnAction(event ->
        {
            TreeItem<FileSystemObject> selected = getSelectionModel().getSelectedItem();
            
            if(selected == null)
            {
                return;
            }
            
            if(getRoot() == selected)
            {
                Alert alert = FxHelper.getErrorDialog("Object edit error", 
                        "Cannot edit root folder", "Root folder cannot be edited.");
                alert.showAndWait();
                
                return;
            }
            
            if(selected.getValue() instanceof FsDirectory)
            {
                FxFsDirectoryDialog editDialog = new FxFsDirectoryDialog();
                editDialog.setDirectory((FsDirectory) selected.getValue());
                
                FxFsDirectoryDialog.setUpAndShowDialog(editDialog, getScene().getWindow(), "Edit directory");
            }
            else
            {
                FxFsFileDialog editDialog = new FxFsFileDialog(this.serverNode, (FsFile) selected.getValue());
                
                FxFsDirectoryDialog.setUpAndShowDialog(editDialog, getScene().getWindow(), "Edit file");
            }
        });
        
        return edit;
    }
    
    /**
     * Returns delete menu item - delete selected FS object.
     * 
     * @return delete menu item
     */
    private MenuItem getFileStructureDeleteMenuItem()
    {
        final MenuItem delete = new MenuItem("Delete");
        
        delete.setOnAction(event ->
        {
            TreeItem<FileSystemObject> selected = getSelectionModel().getSelectedItem();
            
            if(selected == null)
            {
                return;
            }
            
            // cant delete root
            if(getRoot() == selected)
            {
                Dialog dialog = FxHelper.getErrorDialog("Root directory delete", 
                        "Root directory delete attempt", "Root directory cannot be deleted");
                dialog.showAndWait();
                
                return;
            }
            
            TreeItem<FileSystemObject> parent = selected.getParent();
            
            // remove from model (always has to have a parent)
            if(parent.getValue() instanceof FsDirectory)
            {
                // object can be replicated
                FsGlobalReplicationManager.deleteReplicatedObject(selected.getValue());
            }
            
            // remove from table
            parent.getChildren().remove(selected);
        });
        
        return delete;
    }
    
    /**
     * Returns select mount device menu item - select new mount device
     * for selected FS structure object.
     * 
     * @return select mount device menu item
     */
    private MenuItem getFileStructureSelectMountDeviceMenuItem()
    {
        final MenuItem selectMountpoint = new MenuItem("Select mount device");
        
        selectMountpoint.setOnAction(actionEvent ->
        {
            TreeItem<FileSystemObject> selected = getSelectionModel().getSelectedItem();
            
            if(selected == null)
            {
                return;
            }
            
            FxFsMountpointDialog dialog = parentDialog.getFxFsMountpointDialog();
            dialog.setMountPoint(serverNode.getFsManager().getFsObjectMountEntry(selected.getValue()));
            
            FxFsMountpointDialog.setUpAndShowDialog(dialog, getScene().getWindow(), "Select mount device");
            
            if(dialog.isConfirmed())
            {
                ServerStorage stor = dialog.getMountPoint();
                
                if(stor == null)
                {
                    serverNode.getFsManager().umount(selected.getValue());
                }
                else
                {
                    try
                    {
                        serverNode.getFsManager().mount(stor, selected.getValue());
                    }
                    catch (NotEnoughSpaceLeftException ex)
                    {
                        Dialog errDialog = FxHelper.getErrorDialog("Error mounting", "Error mounting FS object", ex.getMessage());
                        errDialog.showAndWait();
                    }
                }
            }
        });
        
        return selectMountpoint;
    }

    // d&d @Override protected DragRowResult onRowDragDropped(FileSystemObject item, FileSystemObject oldParent, FileSystemObject newParent)
//    {
//        if( !(oldParent instanceof FsDirectory) || !(newParent instanceof FsDirectory))
//        {
//            return DragRowResult.REFUSED;
//        }
//        
//        FsDirectory npdir = (FsDirectory) newParent;
//        
//        try
//        {
//            // cant use fs manager since it would alter its mount point
//            item.getParent().getChildren().remove(item);
//            
//            ServerFsAddChildResult result = this.serverNode.getFsManager().addDirectoryChild(npdir, item);
//            
//            switch(result)
//            {
//                case ADDED:
//                    return DragRowResult.ACCEPTED;
//                case MERGED:
//                    return DragRowResult.MERGED;
//                default:
//                case REFUSED:
//                    return DragRowResult.REFUSED;
//            }
//            
//        }
//        catch(NotEnoughSpaceLeftException ex)
//        {
//            // we can add it directly here 
//            ((FsDirectory) oldParent).getChildren().add(item);
//            
//            Dialog errDialog = FxHelper.getErrorDialog("Add file error", "Couldn't add file", ex.getMessage());
//            errDialog.showAndWait();
//        }
//        
//        return DragRowResult.REFUSED;
//    }
    
    // d&d @Override protected boolean dropAcceptable(Dragboard db, TreeTableRow<FileSystemObject> row)
//    {
//        boolean acceptable = super.dropAcceptable(db, row);
//        
//        return (acceptable && row.getTreeItem().getValue() instanceof FsDirectory);
//    }
    
    // d&d @Override protected void onDropMerged(TreeItem<FileSystemObject> item, TreeItem<FileSystemObject> oldParent, TreeItem<FileSystemObject> newParent)
//    {
//        super.onDropMerged(item, oldParent, newParent);
//        
//        TreeItem<FileSystemObject> mergedItem = null;
//        
//        // we have to find an item that we merged with
//        for(TreeItem<FileSystemObject> x : newParent.getChildren())
//        {
//            if(x.getValue().getClass().equals(item.getValue().getClass()) && 
//                    x.getValue().getFullPath().equals(item.getValue().getFullPath()))
//            {
//                mergedItem = x;
//                break;
//            }
//        }
//        
//        if(mergedItem != null)
//        {
//            newParent.getChildren().remove(mergedItem);
//            this.addFileStructureItem(mergedItem.getValue(), newParent);
//        }
//    }
    
}
