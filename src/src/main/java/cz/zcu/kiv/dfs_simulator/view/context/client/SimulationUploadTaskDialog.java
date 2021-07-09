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

import cz.zcu.kiv.dfs_simulator.helpers.FxHelper;
import cz.zcu.kiv.dfs_simulator.helpers.Helper;
import cz.zcu.kiv.dfs_simulator.model.ByteSize;
import cz.zcu.kiv.dfs_simulator.model.ByteSizeUnits;
import cz.zcu.kiv.dfs_simulator.model.ModelServerNode;
import cz.zcu.kiv.dfs_simulator.simulation.PutSimulationTask;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FileSystemObject;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FsDirectory;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FsFile;
import static cz.zcu.kiv.dfs_simulator.view.context.server.FxFsFileDialog.MAX_SIZE;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;

/**
 * Simulation upload task dialog - add new task.
 */
public class SimulationUploadTaskDialog extends SimulationTaskDialog
{
    /**
     * Reachable structure (directory) table
     */
    @FXML protected TreeTableView<FileSystemObject> objectStructureTable;
    
    /** FS structure table columns */
    @FXML protected TreeTableColumn<FileSystemObject, String> objectStructureItemCol;
    @FXML protected TreeTableColumn<FileSystemObject, String> objectStructureTypeCol;
    
    /**
     * Upload task file name
     */
    @FXML protected TextField fileNameInput;
    /**
     * Upload task file size
     */
    @FXML protected TextField fileSizeInput;
    /**
     * Upload task file size unit selectbox
     */
    @FXML protected ChoiceBox<ByteSizeUnits> fileSizeUnitSelect;

    /**
     * Upload task dialog.
     */
    public SimulationUploadTaskDialog()
    {
        super(SimulationUploadTaskDialog.class.getClassLoader().getResource("fxml/view/context/client/SimulationUploadTaskDialog.fxml"));
    }
    
    /**
     * Get size from dialog input.
     * 
     * @return size
     */
    protected ByteSize getSize()
    {
        return new ByteSize(Double.parseDouble(this.fileSizeInput.getText()), 
                    this.fileSizeUnitSelect.getSelectionModel().getSelectedItem());
    }

    /**
     * {@inheritDoc}
     */
    @Override protected void addDistinctFileSystemObjects(FsDirectory root, Map<String, TreeItem<FileSystemObject>> existingTreeItems)
    {
        addTreeItem(root, this.objectStructureTable, existingTreeItems);
        
        for(FileSystemObject object : root.getChildren())
        {
            if(object instanceof FsDirectory)
            {
                addTreeItem(object, this.objectStructureTable, existingTreeItems);
                
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
        if((this.fileNameInput.getText().length() < FsFile.NAME_MIN_LENGTH || 
                this.fileNameInput.getText().length() > FsFile.NAME_MAX_LENGTH) || (!this.fileNameInput.getText().matches("[A-Za-z0-9]+")))
        {
            return false;
        }
        
        if(!Helper.isDouble(this.fileSizeInput.getText()))
        {
            return false;
        }
        
        ByteSize size = this.getSize();
        
        return (size.bytesProperty().get() > 0 && size.bytesProperty().get() <= MAX_SIZE.bytesProperty().get());
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
        
        FxHelper.initByteSizeChoiceBox(this.fileSizeUnitSelect);
        
        this.objectStructureTable.disableProperty().bind(Bindings.not(Bindings.and(
                this.fileNameInput.textProperty().isNotEmpty(), this.fileSizeInput.textProperty().isNotEmpty())));
        
        Platform.runLater(() -> 
                {
                    fileNameInput.requestFocus();
                });
    }
    
    /**
     * {@inheritDoc}
     */
    @Override protected void addSimulationTask(TreeItem<FileSystemObject> treeItem)
    {
        if(treeItem.getValue() instanceof FsDirectory)
        {
            FsDirectory parent = (FsDirectory) treeItem.getValue();
            FsFile file = new FsFile(this.fileNameInput.getText(), this.getSize(), parent);
            
            tasks.add(new PutSimulationTask(file));
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override protected void handleConfirm()
    {
        List<TreeItem<FileSystemObject>> selected = this.objectStructureTable.getSelectionModel().getSelectedItems();
        
        if(selected.isEmpty())
        {
            Dialog errDialog = FxHelper.getErrorDialog(
                    "Error creating upload task", 
                    "Couldn't create upload task", 
                    "You have to select a destination directory.");
            
            errDialog.showAndWait();
            
            return;
        }
        
        if(selected.get(0).getValue().getFullPath().equals(ModelServerNode.ROOT_DIR_NAME))
        {
            Dialog errDialog = FxHelper.getErrorDialog(
                    "Error creating upload task", 
                    "Couldn't create upload task", 
                    "Cannot upload to root directory.");
            
            errDialog.showAndWait();
            
            return;
        }
        
        if(!this.validateInput())
        {
            Dialog errDialog = FxHelper.getErrorDialog(
                    "Error creating upload task", 
                    "Couldn't create upload task", 
                    "File name has to be between " + FsFile.NAME_MIN_LENGTH + " and " + FsFile.NAME_MAX_LENGTH + " characters long and has to consist only of alphanumeric characters. "
                            + "Size has to be greater than 0B and less or equal than " + MAX_SIZE.getHumanReadableFormat());
            
            errDialog.showAndWait();
            
            return;
        }
        
        // this will have at maximum one item
        buildSimulationTasks(selected);
        
        confirmed = true;
        stage.close();
    }
    
}
