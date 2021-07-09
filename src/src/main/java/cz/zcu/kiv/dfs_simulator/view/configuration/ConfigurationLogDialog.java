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

package cz.zcu.kiv.dfs_simulator.view.configuration;

import cz.zcu.kiv.dfs_simulator.persistence.SimpleStatePersistenceLogger;
import cz.zcu.kiv.dfs_simulator.persistence.SimplePersistenceMessage;
import cz.zcu.kiv.dfs_simulator.helpers.FxHelper;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 * Configuration import/export dialog.
 */
public class ConfigurationLogDialog extends AnchorPane
{
    /**
     * Messages
     */
    @FXML private TextFlow messageFlow;
    
    /**
     * Dialog stage
     */
    private Stage stage;
    
    /**
     * Import/export logger
     */
    private final SimpleStatePersistenceLogger logger;
    
    /**
     * Construct import/export dialog from given {@code logger}.
     * 
     * @param logger logger
     */
    public ConfigurationLogDialog(SimpleStatePersistenceLogger logger)
    {
        this.logger = logger;
        
        this.fxInit();
    }
    
    /**
     * Load FXML schema
     */
    private void fxInit()
    {
        FxHelper.loadFXMLAndSetController(getClass().getClassLoader().getResource("fxml/view/configuration/ConfigurationLogDialog.fxml"), this);
    }
    
    /**
     * Set dialog stage.
     * 
     * @param stage stage
     */
    public void setDialogStage(Stage stage)
    {
        this.stage = stage;
    }
    
    /**
     * Display dialog and wait for user confirm.
     * 
     * @param owner dialog owner
     * @param title dialog title
     */
    public void setUpAndShow(Window owner, String title)
    {
        Stage dialogStage = new Stage();

        dialogStage.initStyle(StageStyle.DECORATED);
        dialogStage.setTitle(title);
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.setResizable(false);
        dialogStage.initOwner(owner);

        Scene scene = new Scene(this);
        dialogStage.setScene(scene);
        this.setDialogStage(dialogStage);

        dialogStage.showAndWait();
    }
    
    /**
     * Initialize FX elements.
     */
    @FXML public void initialize()
    {
        for(SimplePersistenceMessage message : this.logger.getMessages())
        {
            this.addMessage(message);
        }
    }
    
    /**
     * Add message to message display.
     * 
     * @param message message
     */
    private void addMessage(SimplePersistenceMessage message)
    {
        Text t = new Text(message.messsage + "\n");
        
        if(!message.success)
        {
            t.setFill(Color.RED);
        }
        
        this.messageFlow.getChildren().add(t);
    }
    
    /**
     * Handle dialog confirm.
     */
    public void handleConfirm()
    {
        stage.close();
    }
}
