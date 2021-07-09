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

import cz.zcu.kiv.dfs_simulator.helpers.FxHelper;
import java.net.URL;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 * Basic dialog.
 */
public abstract class BaseDialog extends AnchorPane
{
    /**
     * Dialog state
     */
    protected Stage stage;
    /**
     * Dialog confirmed flag
     */
    protected boolean confirmed = false;
    /**
     * Dialog resizable flag
     */
    protected boolean resizable = false;
    
    /**
     * FXML schema
     */
    protected final URL schemaURL;
    
    /**
     * Basic dialog.
     * 
     * @param schemaURL FXML schema
     */
    public BaseDialog(URL schemaURL)
    {
        this.schemaURL = schemaURL;
        
        this.fxInit();
    }
    
    /**
     * Init dialog.
     */
    private void fxInit()
    {
        FxHelper.loadFXMLAndSetController(schemaURL, this);
    }
    
    /**
     * Set dialog state.
     * 
     * @param stage dialog stage
     */
    public void setDialogStage(Stage stage)
    {
        this.stage = stage;
    }
    
    /**
     * Set-up dialog, display it and wait for user action.
     * 
     * @param dialog dialog
     * @param owner dialog parent
     * @param title dialog title
     */
    public static void setUpAndShowDialog(BaseDialog dialog, Window owner, String title)
    {
        Stage dialogStage = new Stage();

        dialogStage.initStyle(StageStyle.DECORATED);
        dialogStage.setTitle(title);
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.setResizable(dialog.resizable);
        dialogStage.initOwner(owner);

        Scene scene = new Scene(dialog);
        dialogStage.setScene(scene);
        dialog.setDialogStage(dialogStage);
        
        dialogStage.showAndWait();
    }
    
    /**
     * Initialize graphic portions of dialog.
     */
    @FXML public abstract void initialize();
}
