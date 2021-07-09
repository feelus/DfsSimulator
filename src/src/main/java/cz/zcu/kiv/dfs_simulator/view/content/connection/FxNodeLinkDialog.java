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

package cz.zcu.kiv.dfs_simulator.view.content.connection;

import cz.zcu.kiv.dfs_simulator.helpers.FxHelper;
import cz.zcu.kiv.dfs_simulator.helpers.Helper;
import cz.zcu.kiv.dfs_simulator.model.ByteSpeed;
import cz.zcu.kiv.dfs_simulator.model.ByteSpeedUnits;
import cz.zcu.kiv.dfs_simulator.model.connection.ModelNodeConnection;
import cz.zcu.kiv.dfs_simulator.view.BaseInputDialog;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;

/**
 * Node link setting dialog.
 */
public class FxNodeLinkDialog extends BaseInputDialog
{
    /**
     * Bandwidth input
     */
    @FXML private TextField bandwidthInput;
    /**
     * Bandwidth unit select box
     */
    @FXML private ChoiceBox<ByteSpeedUnits> bandwidthUnitSelect;
    /**
     * Latency input
     */
    @FXML private TextField latencyInput;
    
    /**
     * Node link setting dialog.
     */
    public FxNodeLinkDialog()
    {
        super(FxNodeLinkDialog.class.getClassLoader().getResource("fxml/view/content/connection/FxNodeLinkDialog.fxml"));
    }
    
    /**
     * Get bandwidth from input.
     * 
     * @return bandwidth
     */
    public ByteSpeed getBandwidth()
    {
        if(Helper.isDouble(this.bandwidthInput.getText()))
        {
            return new ByteSpeed(Double.parseDouble(this.bandwidthInput.getText()), 
                    this.bandwidthUnitSelect.getSelectionModel().getSelectedItem());
        }
        
        return null;
    }
    
    /**
     * Get latency from input.
     * 
     * @return latency 
     */
    public int getLatency()
    {
        String inputData = this.latencyInput.getText();
        
        if(Helper.isInteger(inputData))
        {
            return Integer.parseInt(inputData);
        }
        
        return -1;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public boolean validateInput()
    {
        int latency = this.getLatency();
        
        if(latency < ModelNodeConnection.LATENCY_MIN || latency > ModelNodeConnection.LATENCY_MAX)
        {
            return false;
        }
        
        if(!Helper.isDouble(this.bandwidthInput.getText()))
        {
            return false;
        }
        
        ByteSpeed speed = new ByteSpeed(Double.parseDouble(this.bandwidthInput.getText()), 
                    this.bandwidthUnitSelect.getSelectionModel().getSelectedItem());
        
        return (speed.bpsProperty().get() > 0 && speed.bpsProperty().get() <= ModelNodeConnection.BANDWIDTH_MAX.bpsProperty().get());
    }
    
    /**
     * Set connection wrapper (when editing existing connection). Input values
     * will be set to those of the wrapper.
     * 
     * @param connectionWrapper wrapper
     */
    public void setNodeConnectionWrapper(FxNodeConnectionWrapper connectionWrapper)
    {
        this.latencyInput.setText(connectionWrapper.getLatency() + "");
        
        ByteSpeedUnits nominal = connectionWrapper.getBandwidth().getNominalUnits();
        String nominalBandwidth = FxHelper.getNominalSpeed(connectionWrapper.getBandwidth());
        
        this.bandwidthInput.setText(nominalBandwidth);
        this.bandwidthUnitSelect.getSelectionModel().select(nominal);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override @FXML public void initialize()
    {        
        FxHelper.initByteSpeedChoiceBox(this.bandwidthUnitSelect);
    }
        
    /**
     * {@inheritDoc}
     */
    @Override @FXML protected void handleConfirm()
    {
        if(this.validateInput())
        {
            this.confirmed = true;
            this.stage.close();
        }
        else
        {
            Dialog d = FxHelper.getErrorDialog("Node link error", "Wrong input data", 
                    "You have to select bandwidth and latency between " + ModelNodeConnection.LATENCY_MIN + " and " + ModelNodeConnection.LATENCY_MAX + " ms.");
            
            d.showAndWait();
        }
        
    }
    
}
