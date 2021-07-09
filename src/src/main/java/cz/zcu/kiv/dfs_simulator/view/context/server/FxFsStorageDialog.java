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
import cz.zcu.kiv.dfs_simulator.helpers.Helper;
import cz.zcu.kiv.dfs_simulator.model.ByteSize;
import cz.zcu.kiv.dfs_simulator.model.ByteSizeUnits;
import cz.zcu.kiv.dfs_simulator.model.ByteSpeed;
import cz.zcu.kiv.dfs_simulator.model.ByteSpeedUnits;
import cz.zcu.kiv.dfs_simulator.model.storage.ServerStorage;
import cz.zcu.kiv.dfs_simulator.view.BaseInputDialog;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;

/**
 * Storage configuration (or creation) dialog.
 */
public class FxFsStorageDialog extends BaseInputDialog
{
    /**
     * Storage maximum size
     */
    public static final ByteSize MAX_SIZE = new ByteSize(10000, ByteSizeUnits.GB);
    
    /**
     * Editing existing storage
     */
    protected ServerStorage existingStorage;
    
    /**
     * Storage capacity input
     */
    @FXML private TextField capacityInput;
    /**
     * Storage capacity unit select
     */
    @FXML private ChoiceBox<ByteSizeUnits> capacityUnitSelect;
    
    /**
     * Storage speed input
     */
    @FXML private TextField speedInput;
    /**
     * Storage speed unit select
     */
    @FXML private ChoiceBox<ByteSpeedUnits> speedUnitSelect;
    
    /**
     * Storage configuration dialog
     */
    public FxFsStorageDialog()
    {
        super(FxFsStorageDialog.class.getClassLoader().getResource("fxml/view/context/server/FxFsStorageDialog.fxml"));
    }
    
    /**
     * Set existing storage - inputs will be filled from this storage's values.
     * 
     * @param storage existing storage
     */
    public void setStorage(ServerStorage storage)
    {
        ByteSizeUnits nominalSizeUnits = storage.getSize().getNominalUnits();
        String sizeInpuText = FxHelper.getNominalSize(storage.getSize());
        
        this.capacityInput.setText(sizeInpuText);
        this.capacityUnitSelect.getSelectionModel().select(nominalSizeUnits);
        
        ByteSpeedUnits nominalSpeedUnits = storage.getMaximumSpeed().getNominalUnits();
        String speedInputText = FxHelper.getNominalSpeed(storage.getMaximumSpeed());
        
        this.speedInput.setText(speedInputText);
        this.speedUnitSelect.getSelectionModel().select(nominalSpeedUnits);
    }
    
    /**
     * Get storage size from dialog input.
     * 
     * @return storage size
     */
    public ByteSize getSize()
    {
        if(Helper.isDouble(this.capacityInput.getText()))
        {
            return new ByteSize(Double.parseDouble(this.capacityInput.getText()), 
                    this.capacityUnitSelect.getSelectionModel().getSelectedItem());
        }
        
        return null;
    }
    
    /**
     * Get storage speed from dialog input.
     * 
     * @return storage speed
     */
    public ByteSpeed getSpeed()
    {
        return new ByteSpeed(Double.parseDouble(this.speedInput.getText()), 
                this.speedUnitSelect.getSelectionModel().getSelectedItem());
    }
    
    /**
     *{@inheritDoc}
     */
    @Override public boolean validateInput()
    {
        ByteSize size = this.getSize();
        
        if(size == null || 
                size.bytesProperty().get() <= 0 || 
                size.bytesProperty().get() > ServerStorage.MAX_CAPACITY.bytesProperty().get())
        {
            return false;
        }
        
        if(Helper.isDouble(this.speedInput.getText()))
        {
            ByteSpeed speed = this.getSpeed();
            
            return (speed.bpsProperty().get() > 0 && speed.bpsProperty().get() <= ServerStorage.MAX_SPEED.bpsProperty().get());
        }
        
        return false;
    }
    
    /**
     * Initialize capacity and speed unit choice boxes.
     */
    @Override public void initialize()
    {
        FxHelper.initByteSizeChoiceBox(this.capacityUnitSelect);        
        FxHelper.initByteSpeedChoiceBox(this.speedUnitSelect);
    }
    
    /**
     *{@inheritDoc}
     */
    @Override protected void handleConfirm()
    {
        if(this.validateInput())
        {
            confirmed = true;
            stage.close();
        }
        else
        {
            Alert alert = FxHelper.getErrorDialog("Storage input error", 
                    "Error while validating storage", 
                    "Capacity has to be greater than 0 and less than " + (ServerStorage.MAX_CAPACITY.getHumanReadableFormat()) + " and speed has to be greater than 0 and less than " + ServerStorage.MAX_SPEED.getHumanReadableFormat() + ".");
            
            alert.showAndWait();
        }
    }
    
}
