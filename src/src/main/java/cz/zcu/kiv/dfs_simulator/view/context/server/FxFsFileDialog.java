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

import cz.zcu.kiv.dfs_simulator.model.storage.replication.ReplicaTarget;
import cz.zcu.kiv.dfs_simulator.helpers.FxHelper;
import cz.zcu.kiv.dfs_simulator.helpers.Helper;
import cz.zcu.kiv.dfs_simulator.model.ByteSize;
import cz.zcu.kiv.dfs_simulator.model.ByteSizeUnits;
import cz.zcu.kiv.dfs_simulator.model.ModelNodeRegistry;
import cz.zcu.kiv.dfs_simulator.model.ModelServerNode;
import cz.zcu.kiv.dfs_simulator.model.storage.ServerStorage;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FileSystemObject;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FsDirectory;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FsFile;
import cz.zcu.kiv.dfs_simulator.model.storage.replication.FsGlobalReplicationManager;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.NotEnoughSpaceLeftException;
import cz.zcu.kiv.dfs_simulator.view.BaseInputDialog;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

/**
 * Create file dialog.
 */
public class FxFsFileDialog extends BaseInputDialog 
{
    /**
     * Maximum file size
     */
    public static final ByteSize MAX_SIZE = new ByteSize(10000, ByteSizeUnits.GB);
    
    /**
     * File name input
     */
    @FXML private TextField nameInput;
    /**
     * File size input
     */
    @FXML private TextField sizeInput;
    /**
     * File size unit selectbox
     */
    @FXML private ChoiceBox<ByteSizeUnits> sizeUnitSelect;

    /**
     * Mount storage selectbox
     */
    @FXML private ChoiceBox<ServerStorage> storageChoiceBox;
    
    @FXML private Label replicationConfigLabel;
    /**
     * Add replica button
     */
    @FXML private Button addReplicaButton;
    /**
     * Replication table
     */
    @FXML private TableView<ReplicaTarget> replicationTable;
    /* Replication table columns */
    @FXML private TableColumn<ReplicaTarget, String> serverNodeColumn;
    @FXML private TableColumn<ReplicaTarget, String> targetStorageColumn;
    
    /**
     * Confirm dialog button
     */
    @FXML private Button okButton;
    
    /**
     * Underlying server node
     */
    private final ModelServerNode serverNode;
    
    /**
     * File parent directory
     */
    private FsDirectory parentDir;
    /**
     * Existing file
     */
    protected FsFile file;

    /**
     * Create file dialog.
     * 
     * @param serverNode server node
     * @param parentDir file parent directory
     */
    public FxFsFileDialog(ModelServerNode serverNode, FsDirectory parentDir)
    {
        super(FxFsFileDialog.class.getClassLoader().getResource("fxml/view/context/server/FxFsFileDialog.fxml"));
        this.serverNode = serverNode;
        this.parentDir = parentDir;
        
        this.afterFxInit();
    }
    
    /**
     * Create file dialog.
     * 
     * @param serverNode server node
     * @param file existing file
     */
    public FxFsFileDialog(ModelServerNode serverNode, FsFile file)
    {
        super(FxFsFileDialog.class.getClassLoader().getResource("fxml/view/context/server/FxFsFileDialog.fxml"));
        this.serverNode = serverNode;
        this.file = file;
        
        this.afterFxInit();
    }
    
    /**
     * After graphic elements have been initialized add storages
     * and set existing file if any.
     */
    private void afterFxInit()
    {
        this.storageChoiceBox.getItems().addAll(
                this.serverNode.getStorageManager().getStorage());
        
        if(this.file != null)
        {
            this.setFile();
        }
    }
    
    /**
     * Bind replica controls.
     */
    private void bindReplicaDisableProperties()
    {
        this.replicationConfigLabel.disableProperty().bind(
                    Bindings.not(
                            Bindings.and(
                                this.storageChoiceBox.getSelectionModel().selectedItemProperty().isNotNull(),
                                Bindings.and(
                                    this.nameInput.textProperty().isNotEmpty(), 
                                    this.sizeInput.textProperty().isNotEmpty()
                                )
                            )
                    )
        );
        
        this.addReplicaButton.disableProperty().bind(this.replicationConfigLabel.disableProperty());
        this.replicationTable.disableProperty().bind(this.replicationConfigLabel.disableProperty());
        
        this.okButton.disableProperty().bind(this.replicationConfigLabel.disableProperty());
    }
    
    /**
     * Load values into inputs from existing file.
     */
    private void setFile()
    {
        this.nameInput.setText(this.file.nameProperty().get());
        
        ByteSizeUnits nominal = this.file.getSize().getNominalUnits();
        String sizeInpuText = FxHelper.getNominalSize(this.file.getSize());
        
        this.sizeInput.setText(sizeInpuText);
        this.sizeUnitSelect.getSelectionModel().select(nominal);
        
        ServerStorage stor = this.serverNode.getFsManager().getFsObjectMountEntry(this.file);
        if(stor != null)
        {
            this.storageChoiceBox.getSelectionModel().select(stor);
        }
        
        List<ReplicaTarget> replicaTargets = FsGlobalReplicationManager.getReplicaTargets(this.file);
        replicaTargets.removeIf(rt -> rt.serverNode == this.serverNode);
        
        this.replicationTable.getItems().addAll(replicaTargets);
    }
    
    /**
     * Get input file name.
     * 
     * @return file name
     */
    public String getName()
    {
        return this.nameInput.getText();
    }
    
    /**
     * Get input file size.
     * 
     * @return file size
     */
    public ByteSize getSize()
    {
        if(Helper.isDouble(this.sizeInput.getText()))
        {
            return new ByteSize(Double.parseDouble(this.sizeInput.getText()), 
                    this.sizeUnitSelect.getSelectionModel().getSelectedItem());
        }
        
        return null;
    }
    
    /**
     * Get selected storage device.
     * 
     * @return storage device
     */
    public ServerStorage getStorage()
    {
        return this.storageChoiceBox.getSelectionModel().getSelectedItem();
    }
    
    /**
     * Get dialog file.
     * 
     * @return file
     */
    public FsFile getFile()
    {
        return this.file;
    }
    
    /**
     * Handle add replica button action. Displays dialog with other servers
     * and their storages where file can be replicated to.
     */
    private void setAddReplicaAction()
    {
        this.addReplicaButton.setOnAction(event -> {
            List<ModelServerNode> serverNodes = ModelNodeRegistry.getServerNodes();
            // remove all nodes that are already set to be replicated
            serverNodes.removeAll(
                    replicationTable.getItems().stream().map(rep -> rep.serverNode).collect(Collectors.toList()));
            
            FxFsFileReplicationDialog repDialog = new FxFsFileReplicationDialog();
            repDialog.setServerNodes(serverNodes);
            FxFsFileReplicationDialog.setUpAndShowDialog(repDialog, stage, "Select replica target");
            
            if(repDialog.isConfirmed())
            {
                ReplicaTarget replicaTarget = new ReplicaTarget(repDialog.getServerNode(), repDialog.getServerStorage());
                
                replicationTable.getItems().add(replicaTarget);
            }
        });
    }
    
    /**
     * Column factories.
     */
    private void setColumnValueFactories()
    {
        this.serverNodeColumn.setCellValueFactory((TableColumn.CellDataFeatures<ReplicaTarget, String> p) -> {
            return new ReadOnlyStringWrapper(p.getValue().serverNode.toString());
        });
        
        this.targetStorageColumn.setCellValueFactory((TableColumn.CellDataFeatures<ReplicaTarget, String> p) -> {
            return new ReadOnlyStringWrapper(p.getValue().storage.toString());
        });
    }
    
    /**
     * Create new file from dialog inputs.
     * 
     * @return true if file was created, false otherwise
     */
    private boolean createNewFile()
    {
        if(this.parentDir.getChildObject(this.getName()) == null)
        {
        
            FsFile fileReplicaTemplate = new FsFile(this.getName(), this.getSize(), this.parentDir);

            if(this.int_replicateFile(fileReplicaTemplate))
            {
                // get newly created file for this 
                FileSystemObject foundObj = this.parentDir.getChildObject(this.getName());

                if(foundObj != null && foundObj instanceof FsFile)
                {
                    this.file = (FsFile) foundObj;

                    return true;
                }
            }
        }
        else
        {
            Alert a = FxHelper.getErrorDialog("File already exists", 
                    "File with name " + this.getName() + " already exists in specified path.", null);
            a.showAndWait();
        }
        
        return false;
    }
    
    /**
     * Internal method. Replicate file to selected server nodes and their
     * storages.
     * 
     * @param replicateFile file to be replicated
     * @return true if file was replicated
     */
    private boolean int_replicateFile(FsFile replicateFile)
    {
        List<ReplicaTarget> serverNodes = this.replicationTable.getItems();

        try
        {
            FsGlobalReplicationManager.replicateFile(replicateFile, serverNodes);
            
            return true;
        }
        catch(NotEnoughSpaceLeftException ex)
        {
            Dialog confirmationDialog = FxHelper.getConfirmationDialog(
                    "Confirm storage expansion", 
                    "Expand storage to fit file?", 
                    "One or more servers that stores this replicated file does not have enough storage, do you want it to be automatically expanded to fit this file?");

            Optional<ButtonType> result = confirmationDialog.showAndWait();

            if(result.isPresent() && result.get() == ButtonType.OK)
            {
                FsGlobalReplicationManager.forceReplicateFile(replicateFile, serverNodes);
                
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Alter existing file properties from input values. File replicas
     * will be updated as well.
     * 
     * @return true if file was altered, false otherwise
     */
    private boolean alterExistingFile()
    {
        boolean replicaUpdateDone = false;
        boolean resizeDone = false;
        
        ByteSize originalSize = new ByteSize(this.file.getSize().bytesProperty().get(), ByteSizeUnits.B);
        ServerStorage originalStorage = this.serverNode.getFsManager().getFsObjectMountDevice(this.file);
        
        // temporarily resize so that we can update replicated nodes
        this.file.setSize(this.getSize());
        
        try
        {   
            FsGlobalReplicationManager.updateReplicaTargets(this.file, this.replicationTable.getItems());
            replicaUpdateDone = true;
            
            FsGlobalReplicationManager.resizeReplicatedFile(this.file, this.getSize());
            resizeDone = true;
        }
        catch(NotEnoughSpaceLeftException ex)
        {
            Dialog confirmationDialog = FxHelper.getConfirmationDialog(
                    "Confirm storage expansion", 
                    "One ore more nodes does not hae enough space, expand storage?", 
                    "On one or more replicas there is not enough space to fit the newly edited file, expand storage?");
            
            Optional<ButtonType> result = confirmationDialog.showAndWait();

            // resize confirmed, propagate changes to all replicas
            if(result.isPresent() && result.get() == ButtonType.OK)
            {
                // force update replicas
                if(!replicaUpdateDone)
                {
                    FsGlobalReplicationManager.forceUpdateReplicaTargets(this.file, this.replicationTable.getItems());
                }
                
                FsGlobalReplicationManager.forceResizeReplicatedFile(this.file, this.getSize());
                resizeDone = true;
            }
        }
        
        // if replicas were changed
        if(resizeDone)
        {
            FsGlobalReplicationManager.renameReplicatedObject(this.file, this.getName());
        }
        // revert file resize and remount
        else
        {
            this.file.setSize(originalSize);
            this.serverNode.getFsManager().forceMount(originalStorage, this.file);
        }
        
        return resizeDone;
    }
    
    /**
     * Returns delete replica menu item - delete replica target.
     * 
     * @return delete replica item
     */
    private ContextMenu createReplicaTableContextMenu()
    {
        MenuItem delete = new MenuItem("Delete replica");
        delete.setOnAction(event -> {
            int selectedIndex = replicationTable.getSelectionModel().getSelectedIndex();
            
            if(selectedIndex > 0)
            {
                replicationTable.getItems().remove(selectedIndex);
            }
        });
        
        ContextMenu cm = new ContextMenu();
        cm.getItems().add(delete);
        
        return cm;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public boolean validateInput()
    {
        if((this.nameInput.getText().length() < FsFile.NAME_MIN_LENGTH || 
                this.nameInput.getText().length() > FsFile.NAME_MAX_LENGTH) || (!this.nameInput.getText().matches("[A-Za-z0-9]+")))
        {
            return false;
        }
        
        if(!Helper.isDouble(this.sizeInput.getText()))
        {
            return false;
        }
        
        ByteSize size = new ByteSize(Double.parseDouble(this.sizeInput.getText()), 
                    this.sizeUnitSelect.getSelectionModel().getSelectedItem());
        
        return (size.bytesProperty().get() > 0 && size.bytesProperty().get() <= MAX_SIZE.bytesProperty().get());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override protected void handleConfirm()
    {
        if(this.validateInput())
        {
            boolean opResult;
            // editing existing
            if(this.file != null)
            {
                opResult = this.alterExistingFile();
            }
            // creating new 
            else
            {
                opResult = this.createNewFile();
            }
            
            if(opResult)
            {
                confirmed = true;
                stage.close();
            }
        }
        else
        {
            Dialog errDialog = FxHelper.getErrorDialog(
                    "Error creating file", 
                    "Couldn't create file", 
                    "File name has to be between " + FsFile.NAME_MIN_LENGTH + " and " + FsFile.NAME_MAX_LENGTH + " characters long and has to consist only of alphanumeric characters. "
                            + "Size has to be greater than 0B and less or equal than " + MAX_SIZE.getHumanReadableFormat());
            
            errDialog.showAndWait();
        }
    }
    
    /**
     * Replication table row factory.
     * 
     * @param view view
     * @return row
     */
    protected TableRow<ReplicaTarget> rowFactory(TableView<ReplicaTarget> view)
    {
        TableRow<ReplicaTarget> row = new TableRow<>();
        
        row.emptyProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null)
            {
                if(row.getIndex() == 0)
                {
                    row.contextMenuProperty().unbind();
                    row.setContextMenu(null);
                }
                else
                {
                    final ContextMenu cm = createReplicaTableContextMenu();

                    row.contextMenuProperty().bind(
                            Bindings.when(row.emptyProperty()).then((ContextMenu) null).otherwise(cm));
                }
            }
        });
        
        return row;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public void initialize()
    {
        this.bindReplicaDisableProperties();
        this.setAddReplicaAction();
        this.setColumnValueFactories();
        this.createReplicaTableContextMenu();
        FxHelper.initByteSizeChoiceBox(this.sizeUnitSelect);
        
        this.replicationTable.setRowFactory(this::rowFactory);
        
        this.storageChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null)
            {
                if(!replicationTable.getItems().isEmpty())
                {
                    replicationTable.getItems().remove(0);
                }
                replicationTable.getItems().add(0, new ReplicaTarget(this.serverNode, newValue));
            }
        });
    }
    
}
