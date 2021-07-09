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

import cz.zcu.kiv.dfs_simulator.persistence.StatePersistable;
import cz.zcu.kiv.dfs_simulator.persistence.StatePersistableElement;
import cz.zcu.kiv.dfs_simulator.persistence.StatePersistor;
import cz.zcu.kiv.dfs_simulator.persistence.FileXmlStatePersistor;
import cz.zcu.kiv.dfs_simulator.persistence.FileXmlStateRestorer;
import cz.zcu.kiv.dfs_simulator.persistence.InvalidPersistedStateException;
import cz.zcu.kiv.dfs_simulator.persistence.SimpleStatePersistenceLogger;
import cz.zcu.kiv.dfs_simulator.persistence.StatePersistenceLogger;
import cz.zcu.kiv.dfs_simulator.persistence.StateRestorer;
import cz.zcu.kiv.dfs_simulator.helpers.FxHelper;
import cz.zcu.kiv.dfs_simulator.helpers.SimulatorPreferences;
import cz.zcu.kiv.dfs_simulator.view.configuration.ConfigurationLogDialog;
import cz.zcu.kiv.dfs_simulator.view.content.ModelGraphLayoutPane;
import cz.zcu.kiv.dfs_simulator.view.content.FxModelClientNode;
import cz.zcu.kiv.dfs_simulator.view.content.events.FxNodeEvent;
import cz.zcu.kiv.dfs_simulator.view.content.FxModelServerNode;
import cz.zcu.kiv.dfs_simulator.view.content.events.FxNodeSimulationEvent;
import cz.zcu.kiv.dfs_simulator.view.context.client.FxClientNodeContextDialog;
import cz.zcu.kiv.dfs_simulator.view.simulation.SimulationResultsWindow;
import cz.zcu.kiv.dfs_simulator.view.context.server.FxServerNodeContextDialog;
import cz.zcu.kiv.dfs_simulator.view.simulation.FxSimulationBar;
import cz.zcu.kiv.dfs_simulator.view.simulation.FxSimulationPlayer;
import cz.zcu.kiv.dfs_simulator.view.toolbar.ToolbarPanel;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.input.DragEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Main pane for DFS modeling.
 */
public class SimulatorLayoutPane extends BorderPane implements StatePersistable
{
    /**
     * Persistable identificator
     */
    protected static final String PERSISTABLE_NAME = "root_layout";
    
    /**
     * Remove all menu item
     */
    @FXML protected MenuItem menuRemoveAll;
    /**
     * Use line links menu item
     */
    @FXML protected RadioMenuItem menuUseLinkLine;
    /**
     * Use bezier links menu item
     */
    @FXML protected RadioMenuItem menuUseLinkBezier;
    
    /**
     * Toolbar panel (library)
     */
    protected ToolbarPanel toolbarPanel;

    /**
     * Actual pane used for modeling
     */
    protected ModelGraphLayoutPane graphLayoutPane;
    
    /**
     * Main pane for DFS modeling.
     */
    public SimulatorLayoutPane()
    {
        this.fxInit();
    }

    /**
     * Initiate FX schema
     */
    private void fxInit()
    {
        FxHelper.loadFXMLAndSetController(getClass().getClassLoader().getResource("fxml/view/SimulatorLayoutPane.fxml"), this);
    }
    
    /**
     * Initialize FX elements
     */
    @FXML public void initialize()
    {
        this.initLayout();
        this.initMenuHandlers();
        this.initDragHandlers();
        this.initEventHandlers();
        
        Platform.runLater(() -> 
        {
            graphLayoutPane.requestFocus();
        });
    }
    
    /**
     * Initialize layout elements
     */
    private void initLayout()
    {
        this.graphLayoutPane = new ModelGraphLayoutPane();
        this.toolbarPanel = new ToolbarPanel(this.graphLayoutPane);
        
        this.setLeft(this.toolbarPanel);
        this.setCenter(this.graphLayoutPane);
    }
    
    /**
     * Initialize menu item handlers
     */
    private void initMenuHandlers()
    {
        this.menuRemoveAll.setOnAction(actionEvent -> 
        {
            graphLayoutPane.removeContent();
        });
        
        this.menuUseLinkLine.setOnAction(actionEvent -> 
        {
            graphLayoutPane.useBezierLinks(!menuUseLinkLine.isSelected());
        });
        
        this.menuUseLinkBezier.setOnAction(actionEvent ->
        {
            graphLayoutPane.useBezierLinks(menuUseLinkBezier.isSelected());
        });
    }
    
    /**
     * Initiate drag and drop handlers
     */
    private void initDragHandlers()
    {
        // fired when a library node (from toolbar) has been added to content pane
        setOnDragDone(dragEvent -> {
            DragContainer container = (DragContainer) 
                    dragEvent.getDragboard().getContent(DragContainer.ADD_NODE);
            
            if(container != null)
            {
                DragEvent e = container.getValue(ModelGraphLayoutPane.DROP_NODE_EVENT_KEY);
                
                if(e != null)
                {
                    this.graphLayoutPane.handleLibraryDrop(e);
                }
            }
        });
    }
    
    /**
     * Set right panel.
     * 
     * @param n panel
     */
    private void setRightPanel(Node n)
    {
        setRight(n);
    }
    
    /**
     * Initiate global event handlers. Handles mainly events that require communication
     * between multiple panels.
     */
    private void initEventHandlers()
    {
        // node context (setting) dialog
        addEventHandler(FxNodeEvent.NODE_CONTEXT_DIALOG_REQUESTED, event ->
        {
            GridPane dialog;
            
            if(event.getTarget() instanceof FxModelServerNode)
            {
                dialog = new FxServerNodeContextDialog((FxModelServerNode) event.getTarget());
            }
            else
            {
                dialog = new FxClientNodeContextDialog((FxModelClientNode) event.getTarget());
            }
            
            // init new stage
            Stage stage = new Stage();

            // prepare scene with context dialog
            stage.setScene(new Scene(dialog));

            stage.setTitle("Node configuration");
            stage.initStyle(StageStyle.DECORATED);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(getScene().getWindow());
            stage.setResizable(false);

            stage.show();

            event.consume();
            
        });
        
        // begin simulation visualisation
        addEventHandler(FxNodeSimulationEvent.SIMULATION_VISUALISATION_ON_REQUESTED, event ->
        {
            if(event instanceof FxNodeSimulationEvent)
            {
                FxNodeSimulationEvent simEvent = (FxNodeSimulationEvent) event;
                                                
                FxSimulationBar simBar = new FxSimulationBar(simEvent.getResultSet(), graphLayoutPane);
                setRightPanel(simBar);
                
                FxSimulationPlayer simPlayer = new FxSimulationPlayer(simEvent.getResultSet(), simBar);
                simPlayer.play();
                
                event.consume();
            }
        });
        
        // end simulation visualisation
        addEventHandler(FxNodeSimulationEvent.SIMULATION_VISUALISATION_OFF_REQUESTED, event ->
        {
            setRightPanel(null);
            
            event.consume();
        });
        
        // disable controls while displaying simulation
        addEventHandler(FxNodeSimulationEvent.NODE_SIMULATION_DISABLE_CONTROLS_REQUEST, event ->
        {
            graphLayoutPane.setMouseTransparent(true);
            toolbarPanel.setMouseTransparent(true);
            ((Stage) getScene().getWindow()).setResizable(false);
            
            event.consume();
        });
        
        // enable controls after displaying simulation
        addEventHandler(FxNodeSimulationEvent.NODE_SIMULATION_ENABLE_CONTROLS_REQUEST, event ->
        {
            graphLayoutPane.setMouseTransparent(false);
            toolbarPanel.setMouseTransparent(false);
                    
            ((Stage) getScene().getWindow()).setResizable(true);
            
            event.consume();
        });
        
        // simulation finished successfuly
        addEventHandler(FxNodeSimulationEvent.NODE_SIMULATION_FINISHED_SUCCESS, event ->
        {
            // has to be invoked on a gui thread
            // @TODO is it not automatically?
            Platform.runLater(() ->
            {
                SimulationResultsWindow d = ((FxNodeSimulationEvent) event).getFinishedDialog();
                if(d != null)
                {
                    d.displayAsDialog(getScene().getWindow(), "Simulation results");
                }
            });
            
            event.consume();
        });
        
        // simulation failed
        addEventHandler(FxNodeSimulationEvent.NODE_SIMULATION_FINISHED_FAILURE, event ->
        {            
            // has to be invoked on a gui thread
            Platform.runLater(() ->
            {
                Alert a = FxHelper.getErrorDialog(
                        "Simulation error", 
                        "Simulation unsuccessful", 
                        "There has been an error during one or more simulation tasks.");
                
                a.showAndWait();
            });
            
            event.consume();
        });
    }
    
    /**
     * Import simulator configuration from external XML file.
     */
    public void handleImportConfiguration()
    {
        Dialog dialog = FxHelper.getConfirmationDialog("Confirm import", 
                "Overwrite existing configuration?", 
                "Do you really want to erase current configuration and import a new one?");
        
        Optional<ButtonType> result = dialog.showAndWait();
        
        if(result.isPresent() && result.get() == ButtonType.OK)
        {
            FileChooser fileChooser = FxHelper.getSimulatorFileChooser();
            fileChooser.setTitle("Import configuration");
            
            FileChooser.ExtensionFilter extFilter
                    = new FileChooser.ExtensionFilter("XML", "*.xml");
            fileChooser.getExtensionFilters().add(extFilter);
            
            File file = fileChooser.showOpenDialog(getScene().getWindow());
            
            if(file != null)
            {
                // store last open dir
                SimulatorPreferences.setLastOpenDir(file.getParentFile().getAbsolutePath());
                
                SimpleStatePersistenceLogger logger = new SimpleStatePersistenceLogger();
                
                try
                {
                    this.graphLayoutPane.removeContent();
                    
                    StateRestorer restorer = new FileXmlStateRestorer(logger);
                    restorer.restore(file, this);
                    
                    if(logger.getMessages().size() > 0)
                    {
                        ConfigurationLogDialog logDialog = new ConfigurationLogDialog(logger);
                        logDialog.setUpAndShow(getScene().getWindow(), "Import log");
                    }
                }
                catch (InvalidPersistedStateException ex)
                {
                    this.graphLayoutPane.removeContent();
                    Dialog d = FxHelper.getErrorDialog("Configuration import", "Unable to import configuration", ex.getMessage());
                    d.showAndWait();
                }
            }
        }
    }
    
    /**
     * Export simulator configuration into XML file.
     */
    public void handleExportConfiguration()
    {
        FileChooser fileChooser = FxHelper.getSimulatorFileChooser();
        fileChooser.setTitle("Save configuration");

        FileChooser.ExtensionFilter extFilter
                = new FileChooser.ExtensionFilter("XML", "*.xml");
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setInitialFileName("configuration.xml");
        
        File file = fileChooser.showSaveDialog(getScene().getWindow());
        
        if (file != null) 
        {
            SimulatorPreferences.setLastOpenDir(file.getParentFile().getAbsolutePath());
            
            SimpleStatePersistenceLogger logger = new SimpleStatePersistenceLogger();
            
            try 
            {
                StatePersistor persistor = new FileXmlStatePersistor(file, logger);
                persistor.persist(this);
                
                if(logger.getMessages().size() > 0)
                {
                    ConfigurationLogDialog logDialog = new ConfigurationLogDialog(logger);
                    logDialog.setUpAndShow(getScene().getWindow(), "Import log");
                }
            }
            catch (Exception ex) 
            {
                Dialog d = FxHelper.getErrorDialog("Error exporting config", "Unable to export simulator configuration.", ex.getMessage());
                d.showAndWait();
            }
        }
    }
    
    /**
     * Exit (close) simulator.
     */
    public void handleSimulatorClose()
    {
        // @TODO maybe show confirm dialog?
        Platform.exit();
    }
    
    /**
     * Display "About" dialog from menu.
     */
    public void handleAboutDialogRequested()
    {
        Alert a = 
                FxHelper.getDialog(Alert.AlertType.INFORMATION, 
                        "About", 
                        "ZCU/KIV: Distributed file system simulator", 
                        "This program is part of master's thesis "
                                + "\"Distributed file system simulator\" at University of West Bohemia."
                                + "\n\nVersion: 1.0"
                                + "\nAuthor: Martin Kucera (posta@mkucera.eu)"
                                + "\nDate: April, 2017");
        
        a.showAndWait();
    }

    /**
     * {@inheritDoc}
     */
    @Override public List<StatePersistable> getPersistableChildren()
    {
        List<StatePersistable> l = new ArrayList<>();
        
        l.add(this.graphLayoutPane);
        
        return l;
    }

    /**
     * {@inheritDoc}
     */
    @Override public StatePersistableElement export(StatePersistenceLogger logger)
    {
        logger.logOperation(PERSISTABLE_NAME, "Exporting root layout.", true);
        
        return new StatePersistableElement(this.getPersistableName());
    }

    /**
     * {@inheritDoc}
     */
    @Override public void restoreState(StatePersistableElement state, StatePersistenceLogger logger, Object... args) throws InvalidPersistedStateException
    {
        logger.logOperation(PERSISTABLE_NAME, "Restoring root layout.", true);
        
        if(state != null)
        {
            StatePersistableElement graphLayoutPaneState = state.getElement(this.graphLayoutPane.getPersistableName());
            
            this.graphLayoutPane.restoreState(graphLayoutPaneState, logger);
        }
        else
        {
            logger.logOperation(PERSISTABLE_NAME, "Nothing to restore.", true);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override public String getPersistableName()
    {
        return PERSISTABLE_NAME;
    }

}
