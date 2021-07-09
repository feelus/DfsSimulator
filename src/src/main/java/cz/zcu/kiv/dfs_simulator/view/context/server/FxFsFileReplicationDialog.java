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

import cz.zcu.kiv.dfs_simulator.model.ModelServerNode;
import cz.zcu.kiv.dfs_simulator.model.storage.ServerStorage;
import cz.zcu.kiv.dfs_simulator.view.BaseInputDialog;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;

/**
 * File replication dialog.
 */
public class FxFsFileReplicationDialog extends BaseInputDialog
{
    /**
     * Target server choice box
     */
    @FXML private ChoiceBox<ModelServerNode> serverChoiceBox;
    /**
     * Target storage choice box
     */
    @FXML private ChoiceBox<ServerStorage> storageChoiceBox;
    /**
     * Confirm button
     */
    @FXML private Button okButton;

    /**
     * File replication dialog.
     */
    public FxFsFileReplicationDialog()
    {
        super(FxFsFileReplicationDialog.class.getClassLoader().getResource("fxml/view/context/server/FxFsFileReplicationDialog.fxml"));
    }
    
    /**
     * Set reachable server nodes.
     * 
     * @param serverNodes server nodes
     */
    public void setServerNodes(List<ModelServerNode> serverNodes)
    {
        this.serverChoiceBox.getItems().addAll(serverNodes);
    }
    
    /**
     * Enable confirm button only when target server and target storage
     * are selected.
     */
    private void bindOkButtonDisableProperty()
    {
        this.okButton.disableProperty().bind(Bindings.not(
                Bindings.and(
                        this.serverChoiceBox.getSelectionModel().selectedItemProperty().isNotNull(), 
                        this.storageChoiceBox.getSelectionModel().selectedItemProperty().isNotNull())));
    }
    
    /**
     * Fill storage choices based on selected server.
     */
    private void addServerChoiceListener()
    {
        this.serverChoiceBox.getSelectionModel().selectedItemProperty().addListener( (observable, oldValue, newValue) -> {
            storageChoiceBox.getItems().clear();
            
            boolean disable = true;
            if(newValue != null)
            {
                storageChoiceBox.getItems().addAll(newValue.getStorageManager().getStorage());
                disable = storageChoiceBox.getItems().isEmpty();
            }
            storageChoiceBox.setDisable(disable);
        });
    }
    
    /**
     * Get replica server node.
     * 
     * @return server node
     */
    public ModelServerNode getServerNode()
    {
        return this.serverChoiceBox.getSelectionModel().getSelectedItem();
    }
    
    /**
     * Get replica storage.
     * 
     * @return storage
     */
    public ServerStorage getServerStorage()
    {
        return this.storageChoiceBox.getSelectionModel().getSelectedItem();
    }

    /**
     * {@inheritDoc}
     */
    @Override public boolean validateInput()
    {
        return (this.getServerNode() != null && this.getServerStorage() != null);
    }

    /**
     * {@inheritDoc}
     */
    @Override protected void handleConfirm()
    {
        if(this.validateInput())
        {
            confirmed = true;
            stage.close();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override public void initialize()
    {
        this.bindOkButtonDisableProperty();
        this.addServerChoiceListener();
    }
    
}
