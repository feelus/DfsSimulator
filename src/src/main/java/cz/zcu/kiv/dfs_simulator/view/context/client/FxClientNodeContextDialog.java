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

package cz.zcu.kiv.dfs_simulator.view.context.client;

import cz.zcu.kiv.dfs_simulator.persistence.FileXmlStatePersistor;
import cz.zcu.kiv.dfs_simulator.persistence.FileXmlStateRestorer;
import cz.zcu.kiv.dfs_simulator.persistence.InvalidPersistedStateException;
import cz.zcu.kiv.dfs_simulator.persistence.NullStatePersistenceLogger;
import cz.zcu.kiv.dfs_simulator.persistence.StatePersistor;
import cz.zcu.kiv.dfs_simulator.persistence.StateRestorer;
import cz.zcu.kiv.dfs_simulator.helpers.FxHelper;
import cz.zcu.kiv.dfs_simulator.helpers.SimulatorPreferences;
import cz.zcu.kiv.dfs_simulator.model.connection.ModelNodeConnection;
import cz.zcu.kiv.dfs_simulator.simulation.DfsSimulator;
import cz.zcu.kiv.dfs_simulator.simulation.DfsSimulatorSimulationResult;
import cz.zcu.kiv.dfs_simulator.simulation.DfsTimeSliceSimulator;
import cz.zcu.kiv.dfs_simulator.simulation.DfsStringSimulatorLogger;
import cz.zcu.kiv.dfs_simulator.simulation.SimulationTask;
import cz.zcu.kiv.dfs_simulator.simulation.SimulationTaskType;
import cz.zcu.kiv.dfs_simulator.simulation.SimulationType;
import cz.zcu.kiv.dfs_simulator.simulation.graph.DijkstraGraphSearcher;
import cz.zcu.kiv.dfs_simulator.simulation.path.MetricDfsPathPicker;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FsFile;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FsGlobalObjectRegistry;
import cz.zcu.kiv.dfs_simulator.view.content.FxModelClientNode;
import cz.zcu.kiv.dfs_simulator.view.context.FxConnectionTable;
import cz.zcu.kiv.dfs_simulator.view.context.FxNodeContextDialog;
import cz.zcu.kiv.dfs_simulator.view.simulation.FxSimulatorTaskResultSet;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.controlsfx.control.CheckComboBox;
import cz.zcu.kiv.dfs_simulator.simulation.path.DfsPathPicker;

/**
 * Client node context (settings) dialog. Allows the node to be configured.
 */
public class FxClientNodeContextDialog extends FxNodeContextDialog
{
    /**
     * Node icon
     */
    @FXML private ImageView nodeImageView;
    
    /**
     * Node ID
     */
    @FXML private Text infoNodeID;
    /**
     * Node connection count
     */
    @FXML private Text infoNodeConnections;
    
    /**
     * Delete node button
     */
    @FXML private Button deleteNodeButton;
    
    /**
     * Client simulation table (simulation tasks)
     */
    @FXML private SimulationTable simulationTable;
    
    /* Simulation table columns */
    @FXML private TableColumn<SimulationTask, String> simulationOperationCol;
    @FXML private TableColumn<SimulationTask, String> simulationPathCol;
    @FXML private TableColumn<SimulationTask, String> simulationSizeCol;
    @FXML private TableColumn<SimulationTask, String> simulationTypeCol;
    
    /**
     * Add download task button
     */
    @FXML private Button addDownloadTask;
    /**
     * Add upload task button
     */
    @FXML private Button addUploadTask;
    /**
     * Begin simulation button
     */
    @FXML private Button beginSimulationButton;
    /**
     * Clear simulation plan button
     */
    @FXML private Button clearSimulationPlanButton;
    /**
     * Multi-selectbox of simulation type
     */
    @FXML private CheckComboBox<SimulationType> simulationTypeSelect;
    
    /**
     * Connection table
     */
    @FXML private FxConnectionTable connectionTable;
    
    /* Connection table columns */
    @FXML private TableColumn<ModelNodeConnection, String> linkBandwidthCol;
    
    /**
     * Underlying stack pane - used to display progress bar over dialog
     */
    @FXML private StackPane contextStackPane;
    /**
     * Dialog pane
     */
    @FXML private DialogPane dialogPane;
    
    /**
     * Associated client node
     */
    private final FxModelClientNode clientNode;
    
    /**
     * Client configuration dialog.
     * 
     * @param clientNode client node
     */
    public FxClientNodeContextDialog(FxModelClientNode clientNode)
    {
        super(clientNode);
        
        this.clientNode = clientNode;
        this.fxInit();
        this.initElements();
    }
    
    /**
     * Load FXML schema.
     */
    private void fxInit()
    {
        FxHelper.loadFXMLAndSetController(getClass().getClassLoader().getResource("fxml/view/context/client/FxClientNodeContextDialog.fxml"), this);
    }
    
    /**
     * Initiate graphic elements.
     */
    private void initElements()
    {
        this.nodeImageView.setImage(this.fxNode.getNodeImage());
        
        setDeleteButtonHandlers(this.deleteNodeButton);
        initConnectionTable(this.connectionTable, this.linkBandwidthCol);
        setConnectionListener(this.infoNodeConnections);
        updateConnectionCount(this.infoNodeConnections);
        
        this.initInfoPanel();
        this.initSimulationControls();
        this.initSimulationTableCellFactories();
        this.initSimulationButtons();
    }
    
    /**
     * Initiate top info panel.
     */
    private void initInfoPanel()
    {
        setDeleteButtonHandlers(this.deleteNodeButton);
        
        this.nodeImageView.setImage(this.fxNode.getNodeImage());
        this.infoNodeID.setText(this.fxNode.getNode().getNodeID() + "");
    }
    
    /**
     * Initiate simulation graphic controls - simulation type multi select.
     */
    private void initSimulationControls()
    {
        ObservableList<SimulationType> choices = FXCollections.observableArrayList();
        
        choices.addAll(SimulationType.SHORTEST, 
                SimulationType.LINK_BANDWIDTH,
                SimulationType.LINK_BANDWIDTH_LATENCY, 
                SimulationType.PATH_THROUGHPUT, 
                SimulationType.PATH_THROUGHPUT_AND_LATENCY, 
                SimulationType.DYNAMIC_PATH_THROUGHPUT_AND_LATENCY,
                SimulationType.HIERARCHICAL_DYNAMIC_PATH_THROUGHPUT_AND_LATENCY,
                SimulationType.HIERARCHICAL_DYNAMIC_PATH_THROUGHPUT_LATENCY_ADVANCED);
        
        simulationTypeSelect.getItems().addAll(choices);
        simulationTypeSelect.getCheckModel().checkIndices(0);
    }
    
    /**
     * Initiate simulation table cell factories and set initial simulation plan.
     */
    private void initSimulationTableCellFactories()
    {
        // add icon based on task type
        this.simulationOperationCol.setCellFactory((TableColumn<SimulationTask, String> param) ->
        {
            TableCell<SimulationTask, String> cell = new TableCell<SimulationTask, String>()
            {
                @Override public void updateItem(String item, boolean empty)
                {
                    super.updateItem(item, empty);
                    
                    if(item != null)
                    {
                        HBox hbox = new HBox(5);
                        
                        hbox.setAlignment(Pos.CENTER);
                        if(this.getTableRow() != null)
                        {
                            SimulationTask task = simulationTable.getItems().get(
                                    this.getTableRow().getIndex());
                            
                            if(task != null)
                            {
                                hbox.getChildren().add(new ImageView(task.getImage()));
                            }
                        }
                        
                        hbox.getChildren().add(new Label(item));
                        setGraphic(hbox);
                    }
                    else
                    {
                        setGraphic(null);
                    }
                }
            };
            
            return cell;
        });
        
        this.simulationOperationCol.setCellValueFactory((TableColumn.CellDataFeatures<SimulationTask, String> p) -> 
        {
            return new ReadOnlyStringWrapper(p.getValue().getType().toString());
        });
        
        this.simulationPathCol.setCellValueFactory((TableColumn.CellDataFeatures<SimulationTask, String> p) -> 
        {
            return new ReadOnlyStringWrapper(p.getValue().getFile().getFullPath());
        });
        
        this.simulationSizeCol.setCellValueFactory((TableColumn.CellDataFeatures<SimulationTask, String> p) -> 
        {
            if(p.getValue().getFile() instanceof FsFile)
            {
                return p.getValue().getFile().getSize().humanReadableProperty();
            }
            
            return new ReadOnlyStringWrapper("-");
        });
        
        this.simulationTypeCol.setCellValueFactory((TableColumn.CellDataFeatures<SimulationTask, String> p) -> 
        {
                return new ReadOnlyStringWrapper(p.getValue().getFile().getType().toString());
        });
        
        this.simulationTable.init(this.clientNode.getClientNode().getSimulationPlan());
    }
    
    /**
     * Set simulation button handlers.
     */
    private void initSimulationButtons()
    {
        this.addDownloadTask.setOnAction(event ->
        {
            displayTaskDialog(SimulationTaskType.GET);
        });
        
        this.addUploadTask.setOnAction(event ->
        {
            displayTaskDialog(SimulationTaskType.PUT);
        });
        
        this.bindSimulationItemsProp();
    }
    
    /**
     * Enable/disable simulation buttons based simulation plan size (if empty,
     * buttons are disabled).
     */
    private void bindSimulationItemsProp()
    {
        ObjectProperty<ObservableList<SimulationTask>> obp = new SimpleObjectProperty<>();
        ListProperty<SimulationTask> lstProp = new SimpleListProperty<>();
        
        lstProp.bind(obp);
        obp.set(this.simulationTable.getItems());
        
        this.beginSimulationButton.disableProperty().bind(lstProp.emptyProperty());
        
        this.beginSimulationButton.setOnAction(event ->
        {
            beginSimulation();
        });
        
        this.clearSimulationPlanButton.disableProperty().bind(lstProp.emptyProperty());
        
        this.clearSimulationPlanButton.setOnAction(event ->
        {
            simulationTable.getItems().clear();
        });
    }
    
    /**
     * Display new simulation task dialog.
     * 
     * @param type simulation task type
     */
    private void displayTaskDialog(SimulationTaskType type)
    {
        SimulationTaskDialog dialog;
        String title;
        
        switch(type)
        {
            case PUT:
                title = "Add upload task";
                dialog = new SimulationUploadTaskDialog();
                break;
            case GET:
            default:
                title = "Add download task";
                dialog = new SimulationDownloadTaskDialog();
        }
        
        dialog.buildReachableTree(clientNode.getClientNode().getConnectionManager().getReachableServers());
        SimulationTaskDialog.setUpAndShowDialog(dialog, getScene().getWindow(), title);
        
        if(dialog.isConfirmed())
        {
            addSimulationTasks(dialog.getTasks());
        }
    }
    
    /**
     * Get path picker used for simulation.
     * 
     * @param type simulation type
     * @return path picker
     */
    protected DfsPathPicker getPathBuilder(SimulationType type)
    {
        return new MetricDfsPathPicker(new DijkstraGraphSearcher(type.getMetric()));
    }
    
    /**
     * Begin simulation with simulation plan of this client.
     */
    private void beginSimulation()
    {
        List<SimulationType> selected = simulationTypeSelect.getCheckModel().getCheckedItems();
        
        if(selected == null || selected.isEmpty())
        {
            Alert a = FxHelper.getErrorDialog("Simulation error", 
                    "You have to select atleast one simulation type to run.", 
                    "Atleast one simulation type has to be selected in order to run simulation.");
            
            a.showAndWait();
            
            return;
        }
        
        List<FxSimulatorTaskResultSet> resultSet = new ArrayList<>();
        ProgressBar pb = new ProgressBar(0);
        
        Task t = new Task<Void>() {
            
            @Override public Void call()
            {
                int pbMax = selected.size();
                int pbDone = 0;
                
                updateProgress(0, pbMax);
                
                for(SimulationType type : selected)
                {
                    DfsSimulator simulator = new DfsTimeSliceSimulator(clientNode.getClientNode(),
                            simulationTable.getPlan(), getPathBuilder(type), type);
                    DfsStringSimulatorLogger logger = new DfsStringSimulatorLogger();
                    
                    simulator.run(logger);
                    
                    resultSet.add(new FxSimulatorTaskResultSet(
                            new DfsSimulatorSimulationResult(type, simulator.getResults()), logger));
                    
                    pbDone += 1;
                    updateProgress(pbDone, pbMax);
                    
                    // reset access counters
                    FsGlobalObjectRegistry.resetAccessCounters();
                }
                
                return null;
            }
            
        };
        
        pb.progressProperty().bind(t.progressProperty());
        
        // if succeeded request simulation animation and close this dialog
        t.setOnSucceeded( e -> {
            clientNode.nodeSimulationPlayRequested(resultSet);

            // close ourselves
            ((Stage)getScene().getWindow()).close();
        });
        
        t.setOnFailed( e -> {
            Alert a = FxHelper.getErrorDialog("Simulation error", 
                    "There was an internal error during simulation.", 
                    "Simulation didn't finish correctly.");
            
            if(t.getException() != null)
            {
                t.getException().printStackTrace();
            }
            
            a.showAndWait();
        });
        
        t.setOnRunning(e -> {
            Text text = new Text("Running simulation...");
            text.setFont(new Font(22));
            
            VBox box = new VBox();
            
            box.setSpacing(15d);
            box.setAlignment(Pos.CENTER);
            
            box.getChildren().add(text);
            box.getChildren().add(pb);
            
            // Grey Background            
            dialogPane.setDisable(true);
            contextStackPane.getChildren().add(box);
        });
        
        (new Thread(t)).start();
    }
    
    /**
     * Add simulation tasks into simulation plan.
     * 
     * @param tasks newly added tasks
     */
    private void addSimulationTasks(List<SimulationTask> tasks)
    {
        this.simulationTable.getItems().addAll(tasks);
    }
    
    /**
     * Import simulation plan from external XMP file.
     */
    public void handleImportPlan()
    {
        Dialog dialog = FxHelper.getConfirmationDialog("Confirm import", 
                "Overwrite existing plan?", 
                "Do you really want to erase current plan and import a new one?");
        
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
                // store last open path
                SimulatorPreferences.setLastOpenDir(file.getParentFile().getAbsolutePath());
                
                NullStatePersistenceLogger logger = new NullStatePersistenceLogger();
                
                try
                {
                    StateRestorer restorer = new FileXmlStateRestorer(logger);
                    restorer.restore(file, this.simulationTable.getPlan());
                    
                    this.simulationTable.resetItems();
                    this.bindSimulationItemsProp();
                }
                catch (InvalidPersistedStateException ex)
                {
                    this.simulationTable.getItems().clear();
                    
                    Dialog d = FxHelper.getErrorDialog("Configuration import", "Unable to import configuration", ex.getMessage());
                    d.showAndWait();
                }
            }
        }
    }
    
    /**
     * Export simulation plan into external XML file.
     */
    public void handleExportPlan()
    {
        FileChooser fileChooser = FxHelper.getSimulatorFileChooser();
        fileChooser.setTitle("Save configuration");

        FileChooser.ExtensionFilter extFilter
                = new FileChooser.ExtensionFilter("XML", "*.xml");
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setInitialFileName("simulation-plan.xml");
        
        File file = fileChooser.showSaveDialog(getScene().getWindow());
        
        if (file != null) 
        {
            SimulatorPreferences.setLastOpenDir(file.getParentFile().getAbsolutePath());
            
            NullStatePersistenceLogger logger = new NullStatePersistenceLogger();
            try 
            {
                StatePersistor persistor = new FileXmlStatePersistor(file, logger);
                persistor.persist(this.simulationTable.getPlan());
            }
            catch (Exception ex) 
            {
                Dialog d = FxHelper.getErrorDialog("Error exporting plan", "Unable to export simulation plan.", ex.getMessage());
                d.showAndWait();
            }
        }
        else
        {
            Dialog d = FxHelper.getErrorDialog("Plan export", "No file selected.", "You have to select a file.");
            d.showAndWait();
        }
        
    }
}
