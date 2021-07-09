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

import cz.zcu.kiv.dfs_simulator.model.storage.ServerStorage;
import cz.zcu.kiv.dfs_simulator.view.BaseInputDialog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;

/**
 * Storage mount dialog.
 */
public class FxFsMountpointDialog extends BaseInputDialog
{
    /**
     * Copy of available storages
     */
    private final ObservableList<ServerStorage> storageListCopy;
    
    /**
     * Storage mount select
     */
    @FXML private ChoiceBox<ServerStorage> mountPointSelect;
    
    /**
     * Storage mount dialog.
     * 
     * @param storageList observable storages
     */
    public FxFsMountpointDialog(ObservableList<ServerStorage> storageList)
    {
        super(FxFsMountpointDialog.class.getClassLoader().getResource("fxml/view/context/server/FxFsMountpointDialog.fxml"));
        
        this.storageListCopy = FXCollections.observableArrayList(storageList);
        this.initMountPointSelect();
    }
    
    /**
     * Initiate storage mount dialog - fill available storages.
     */
    private void initMountPointSelect()
    {
        this.storageListCopy.add(0, null);
        
        this.mountPointSelect.setItems(this.storageListCopy);
        this.mountPointSelect.getSelectionModel().selectFirst();
    }
    
    /**
     * Get selected mount storage.
     * 
     * @return storage
     */
    public ServerStorage getMountPoint()
    {
        return this.mountPointSelect.getSelectionModel().getSelectedItem();
    }
    
    /**
     * Set selected mount storage.
     * 
     * @param storage storage
     */
    public void setMountPoint(ServerStorage storage)
    {
        this.mountPointSelect.getSelectionModel().select(storage);
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
    }
    
    /**
     * {@inheritDoc}
     */
    @Override protected void handleConfirm()
    {
        confirmed = true;
        stage.close();
    }
    
}
