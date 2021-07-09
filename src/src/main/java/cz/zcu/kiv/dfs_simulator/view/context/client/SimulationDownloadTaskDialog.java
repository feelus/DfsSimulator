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

package cz.zcu.kiv.dfs_simulator.view.context.client;

import cz.zcu.kiv.dfs_simulator.simulation.GetSimulationTask;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FileSystemObject;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FsDirectory;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FsFile;
import java.util.Map;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;

/**
 * Simulation download task dialog.
 */
public class SimulationDownloadTaskDialog extends SimulationTaskDialog
{
    /**
     * Table with existing (reachable) objects that can be downloaded
     */
    @FXML protected TreeTableView<FileSystemObject> objectStructureTable;
    
    /* Object structure table columns */
    @FXML protected TreeTableColumn<FileSystemObject, String> objectStructureItemCol;
    @FXML protected TreeTableColumn<FileSystemObject, String> objectStructureTypeCol;
    
    /**
     * Simulation download task dialog.
     */
    public SimulationDownloadTaskDialog()
    {
        super(SimulationDownloadTaskDialog.class.getClassLoader().getResource("fxml/view/context/client/SimulationDownloadTaskDialog.fxml"));
    }
        
    /**
     * Internal method. Add get simulation task with target {@code object}.
     * 
     * @param object target object
     */
    private void int_addSimulationTask(FileSystemObject object)
    {
        if(object instanceof FsDirectory)
        {
            FsDirectory dir = (FsDirectory) object;
            
            dir.getChildren().forEach(child -> this.int_addSimulationTask(child));
        }
        else
        {
            tasks.add(new GetSimulationTask((FsFile) object));
        }
    }
            
    /**
     * {@inheritDoc}
     */
    @Override protected void addSimulationTask(TreeItem<FileSystemObject> treeItem)
    {
        if(!hasSelectedParent(treeItem, this.objectStructureTable, false))
        {
            this.int_addSimulationTask(treeItem.getValue());
        }
    }
                
    /**
     * {@inheritDoc}
     */
    @Override protected void addDistinctFileSystemObjects(FsDirectory root, Map<String, TreeItem<FileSystemObject>> existingTreeItems)
    {
        addTreeItem(root, this.objectStructureTable, existingTreeItems);
        
        for(FileSystemObject object : root.getChildren())
        {
            addTreeItem(object, this.objectStructureTable, existingTreeItems);
            
            if(object instanceof FsDirectory)
            {
                FsDirectory dir = (FsDirectory) object;
                
                if(!dir.getChildren().isEmpty())
                {
                    this.addDistinctFileSystemObjects(dir, existingTreeItems);
                }
            }
        }
    }
            
    /**
     * {@inheritDoc}
     */
    @Override public boolean validateInput()
    {
        return true;
    }
            
    /**
     * {@inheritDoc}
     */
    @Override public void initialize()
    {
        this.objectStructureItemCol.setCellValueFactory((TreeTableColumn.CellDataFeatures<FileSystemObject, String> param) ->
                param.getValue().getValue().nameProperty());
        this.objectStructureTypeCol.setCellValueFactory((TreeTableColumn.CellDataFeatures<FileSystemObject, String> param) ->
        {
            return new ReadOnlyStringWrapper(param.getValue().getValue().getType().toString());
        });
        
        this.objectStructureTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }
            
    /**
     * {@inheritDoc}
     */
    @Override protected void handleConfirm()
    {
        buildSimulationTasks(this.objectStructureTable.getSelectionModel().getSelectedItems());
        
        confirmed = true;
        stage.close();
    }
    
}
