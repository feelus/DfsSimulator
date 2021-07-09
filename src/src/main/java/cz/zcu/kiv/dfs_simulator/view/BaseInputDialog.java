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

package cz.zcu.kiv.dfs_simulator.view;

import java.net.URL;
import javafx.fxml.FXML;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * Base input dialog.
 */
public abstract class BaseInputDialog extends BaseDialog
{

    /**
     * Base input dialog.
     * 
     * @param schemaURL FXML schema
     */
    public BaseInputDialog(URL schemaURL)
    {
        super(schemaURL);
        this.hookHandlers();
    }
    
    /**
     * Add ENTER and ESCAPE key handlers.
     */
    private void hookHandlers()
    {
        addEventHandler(KeyEvent.KEY_PRESSED, event ->
        {
            if(event.getCode() == KeyCode.ENTER)
            {
                handleConfirm();
            }
            else if(event.getCode() == KeyCode.ESCAPE)
            {
                handleCancel();
            }
        });
    }
    
    /**
     * Checks if dialog was confirmed.
     * 
     * @return confirmed flag
     */
    public boolean isConfirmed()
    {
        return this.confirmed;
    }
    
    /**
     * Handle dialog cancel. Closes stage.
     */
    @FXML protected void handleCancel()
    {
        this.stage.close();
    }
    
    /**
     * Validate input data.
     * 
     * @return if input data was valid
     */
    public abstract boolean validateInput();    
    /**
     * Handle dialog confirm.
     */
    @FXML protected abstract void handleConfirm();        
}
