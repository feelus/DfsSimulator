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
import cz.zcu.kiv.dfs_simulator.model.connection.ModelNodeConnection;
import cz.zcu.kiv.dfs_simulator.model.storage.ServerStorage;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FileSystemObject;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.TotalSizeBinding;
import cz.zcu.kiv.dfs_simulator.view.content.FxModelServerNode;
import cz.zcu.kiv.dfs_simulator.view.context.FxConnectionTable;
import cz.zcu.kiv.dfs_simulator.view.context.FxNodeContextDialog;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

/**
 * Server setting (context) dialog.
 */
public class FxServerNodeContextDialog extends FxNodeContextDialog
{
    /**
     * Server icon
     */
    @FXML private ImageView nodeImageView;
    
    /**
     * Server node ID
     */
    @FXML private Text infoNodeID;
    /**
     * Server connection count
     */
    @FXML private Text infoNodeConnections;
    /**
     * Server storage info (used / total size)
     */
    @FXML private Text infoNodeStorage;
    
    /**
     * Delete node button
     */
    @FXML private Button deleteNodeButton;
    
    /**
     * Server storage table
     */
    @FXML private FxStorageTable storageTable;
    @FXML private TableColumn<ServerStorage, String> storageIdCol;
    @FXML private TableColumn<ServerStorage, String> storageCapacity;
    @FXML private TableColumn<ServerStorage, String> storageSpeedCol;
    
    /**
     * Server file system structure table
     */
    @FXML private FxFsTable fileStructureTable;
    @FXML private TreeTableColumn<FileSystemObject, String> fileStructureItemCol;
    @FXML private TreeTableColumn<FileSystemObject, String> fileStructureSizeCol;
    @FXML private TreeTableColumn<FileSystemObject, String> fileStructureMountCol;
    @FXML private TreeTableColumn<FileSystemObject, String> fileStructureTypeCol;
    
    /**
     * Server connection table
     */
    @FXML private FxConnectionTable connectionTable;
    @FXML private TableColumn<ModelNodeConnection, String> linkBandwidthCol;
    
    /**
     * Underlying server node
     */
    private final FxModelServerNode serverNode;
    /**
     * Server total storage size binding
     */
    private final TotalSizeBinding totalStorageBinding;
    /**
     * Server storage used size binding
     */
    private final UsedSizeBinding usedStorageBinding;
    
    /**
     * Server total storage size
     */
    private final ByteSize totalStorage;
    /**
     * Server used storage size
     */
    private final ByteSize usedStorage;
    
    /**
     * Server configuration (context) dialog.
     * 
     * @param serverNode underlying server node
     */
    public FxServerNodeContextDialog(FxModelServerNode serverNode)
    {
        super(serverNode);
        
        this.serverNode = serverNode;
        this.totalStorageBinding = new TotalSizeBinding(serverNode.getFxStorageManager().getStorage());
        this.usedStorageBinding = new UsedSizeBinding(serverNode.getFxStorageManager().getStorage(), serverNode.getServerNode().getFsManager());
        
        this.totalStorage = new ByteSize();
        this.usedStorage = new ByteSize();
        this.totalStorage.bytesProperty().bind(this.totalStorageBinding);
        this.usedStorage.bytesProperty().bind(this.usedStorageBinding);
        
        this.fxInit();
        this.initElements();
    }
    
    /**
     * Load FXML schema
     */
    private void fxInit()
    {
        FxHelper.loadFXMLAndSetController(getClass().getClassLoader().getResource("fxml/view/context/server/FxServerNodeContextDialog.fxml"), this);
    }
    
    /**
     * Initiate tabs and their elements - connection table, storage table,
     * file structure table and info panel.
     */
    private void initElements()
    {
        // connection tab
        initConnectionTable(this.connectionTable, this.linkBandwidthCol);
        setConnectionListener(this.infoNodeConnections);
        updateConnectionCount(this.infoNodeConnections);
        
        // storage tab
        this.initStorageTable();
        this.setInfoStorage();
        
        // fs tab
        this.initFileStructureTable();
        
        // info panel
        this.initInfoPanel();
    }
    
    /**
     * Initialize storage table - populate storage table items and set 
     * cell factories.
     */
    private void initStorageTable()
    {
        this.storageTable.setServerNode(this.serverNode.getServerNode());
        
        // set size storage factories
        this.storageCapacity.setCellValueFactory((TableColumn.CellDataFeatures<ServerStorage, String> p) -> 
                Bindings.concat(
                        serverNode.getServerNode().getFsManager().getStorageUsedSize(p.getValue()).humanReadableProperty(), 
                        " / ", 
                        p.getValue().getSize().humanReadableProperty()
                ));
        this.storageSpeedCol.setCellValueFactory((TableColumn.CellDataFeatures<ServerStorage, String> p) -> p.getValue().getMaximumSpeed().humanReadableProperty());

        this.storageTable.setItems(this.serverNode.getFxStorageManager().getStorage());
        
        this.storageIdCol.prefWidthProperty().bind(this.storageTable.widthProperty().multiply(0.20));
        this.storageCapacity.prefWidthProperty().bind(this.storageTable.widthProperty().multiply(0.25));
        this.storageSpeedCol.prefWidthProperty().bind(this.storageTable.widthProperty().multiply(0.25));
    }
    
    /**
     * Initialize file structure table - populate file structure items and
     * set cell factories.
     */
    private void initFileStructureTable()
    {
        // set column factories
        this.fileStructureItemCol.setCellValueFactory((TreeTableColumn.CellDataFeatures<FileSystemObject, String> param) ->
                param.getValue().getValue().nameProperty());
        
        this.fileStructureSizeCol.setCellValueFactory((TreeTableColumn.CellDataFeatures<FileSystemObject, String> param) -> 
                param.getValue().getValue().getSize().humanReadableProperty());
        
        this.fileStructureMountCol.setCellValueFactory((TreeTableColumn.CellDataFeatures<FileSystemObject, String> param) ->
        {
            return param.getValue().getValue().mountDeviceIdProperty();
        });
        
        this.fileStructureTypeCol.setCellValueFactory((TreeTableColumn.CellDataFeatures<FileSystemObject, String> param) ->
        {
            return new ReadOnlyStringWrapper(param.getValue().getValue().getType().toString());
        });
        
        this.fileStructureTable.init(serverNode.getServerNode(), this);
    }
    
    /**
     * Initialize info panel values and node icon.
     */
    private void initInfoPanel()
    {
        setDeleteButtonHandlers(this.deleteNodeButton);
        
        this.nodeImageView.setImage(this.fxNode.getNodeImage());
        this.infoNodeID.setText(this.fxNode.getNode().getNodeID() + "");
    }
    
    /**
     * Create mount dialog.
     * 
     * @return mount dialog
     */
    public FxFsMountpointDialog getFxFsMountpointDialog()
    {
        return new FxFsMountpointDialog(this.storageTable.getItems());
    }
    
    /**
     * Handle action when new storage dialog is requested and if successful,
     * add new storage to storage manager.
     */
    public void handleAddStorageDevice()
    {
        FxFsStorageDialog dialog = new FxFsStorageDialog();
        FxFsStorageDialog.setUpAndShowDialog(dialog, getScene().getWindow(), "Add storage");
        
        if(dialog.isConfirmed())
        {
            this.serverNode.getFxStorageManager().addStorage(
                    dialog.getSize(), dialog.getSpeed());
        }
    }
    
    /**
     * Set information about used and total size of all storages on this server.
     */
    private void setInfoStorage()
    {
        this.infoNodeStorage.textProperty().bind(
                Bindings.concat(
                        this.usedStorage.humanReadableProperty(), 
                        " / ", 
                        this.totalStorage.humanReadableProperty())
        );
    }
}
