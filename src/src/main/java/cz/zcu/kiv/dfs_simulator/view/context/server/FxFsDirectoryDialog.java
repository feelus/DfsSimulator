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
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FsDirectory;
import cz.zcu.kiv.dfs_simulator.view.BaseInputDialog;
import javafx.fxml.FXML;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;

/**
 * Create directory dialog.
 */
public class FxFsDirectoryDialog extends BaseInputDialog
{
    /**
     * Minimum directory name length
     */
    protected static final int NAME_MIN_LENGTH = 1;
    /**
     * Maximum directory name length
     */
    protected static final int NAME_MAX_LENGTH = 20;
    
    /**
     * Directory name input
     */
    @FXML private TextField nameInput;
    
    /**
     * Existing directory instance
     */
    protected FsDirectory directory = null;
    
    /**
     * Create directory dialog.
     */
    public FxFsDirectoryDialog()
    {
        super(FxFsDirectoryDialog.class.getClassLoader().getResource("fxml/view/context/server/FxFsDirectoryDialog.fxml"));
    }
    
    /**
     * Set existing directory.
     * 
     * @param directory existing directory
     */
    public void setDirectory(FsDirectory directory)
    {
        this.directory = directory;
        
        this.nameInput.setText(directory.nameProperty().get());
    }
    
    /**
     * Get name from dialog input.
     * 
     * @return name
     */
    public String getName()
    {
        return this.nameInput.getText();
    }
    
    /**
     * Save changes to existing directory.
     */
    protected void saveDirectoryChanges()
    {
        this.directory.nameProperty().set(this.getName());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public boolean validateInput()
    {
        return (
                (this.nameInput.getText().length() >= NAME_MIN_LENGTH && this.nameInput.getText().length() <= NAME_MAX_LENGTH) && 
                this.nameInput.getText().matches("[A-Za-z0-9]+"));
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
        if(this.validateInput())
        {
            if(this.directory != null)
            {
                this.saveDirectoryChanges();
            }
            
            confirmed = true;
            stage.close();
        }
        else
        {
            Dialog errDialog = FxHelper.getErrorDialog(
                    "Error creating directory", 
                    "Couldn't create directory", 
                    "Directory name has to be between " + NAME_MIN_LENGTH + " and " + NAME_MAX_LENGTH + " characters long and has to consist only of alphanumeric characters.");
            
            errDialog.showAndWait();
        }
    }
    
}
