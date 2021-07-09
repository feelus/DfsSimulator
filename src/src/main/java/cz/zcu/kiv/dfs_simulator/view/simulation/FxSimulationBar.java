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

package cz.zcu.kiv.dfs_simulator.view.simulation;

import cz.zcu.kiv.dfs_simulator.helpers.FxHelper;
import cz.zcu.kiv.dfs_simulator.model.ByteSize;
import cz.zcu.kiv.dfs_simulator.model.ByteSizeUnits;
import cz.zcu.kiv.dfs_simulator.model.ByteSpeed;
import cz.zcu.kiv.dfs_simulator.model.ByteSpeedUnits;
import cz.zcu.kiv.dfs_simulator.model.connection.ModelNodeConnection;
import cz.zcu.kiv.dfs_simulator.simulation.SimulationTaskType;
import cz.zcu.kiv.dfs_simulator.simulation.SimulationType;
import cz.zcu.kiv.dfs_simulator.view.content.ModelGraphLayoutPane;
import cz.zcu.kiv.dfs_simulator.view.content.events.FxNodeSimulationEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

/**
 * Simulation bar displaying simulation progress.
 */
public class FxSimulationBar extends AnchorPane implements SimulationLogDisplayable
{
    /**
     * Download finished successfully class
     */
    protected final static PseudoClass CLASS_DONE_DOWNLOAD_SUCCESS = PseudoClass.getPseudoClass("download-done-success");
    /**
     * Download uploaded successfully class
     */
    protected final static PseudoClass CLASS_DONE_UPLOAD_SUCCESS = PseudoClass.getPseudoClass("upload-done-success");
    /** 
     * Task skipped 
     */
    protected final static PseudoClass CLASS_DONE_SKIPPED = PseudoClass.getPseudoClass("done-skipped"); 
    /**
     * Task error
     */
    protected final static PseudoClass CLASS_DONE_ERROR = PseudoClass.getPseudoClass("done-error"); 
   
    /**
     * Running download classes - used to simulate progress bar for tree items
     */
    protected final static PseudoClass[] CLASS_DOWNLOAD_RUNNING;
    /**
     * Running upload classes - used to simulate progress bar for tee items
     */
    protected final static PseudoClass[] CLASS_UPLOAD_RUNNING;
    
    /**
     * Constructed dialog with results
     */
    private SimulationResultsWindow resultsDialog = null;
    /**
     * Whether simulation progress display has finished
     */
    private boolean finished = false;
    
    /**
     * Simulation control button - finish/close based on current status
     */
    @FXML protected Button controlSimulationButton;
    /**
     * Close simulation bar button
     */
    @FXML protected Button closeSimulationButton;
    
    /**
     * Populate running classes
     */
    static
    {
        CLASS_DOWNLOAD_RUNNING = new PseudoClass[20];
        CLASS_UPLOAD_RUNNING = new PseudoClass[20];
        
        for(int i = 0; i < 20; i++)
        {
            CLASS_DOWNLOAD_RUNNING[i] = PseudoClass.getPseudoClass("download-running-" + (i * 5));
            CLASS_UPLOAD_RUNNING[i] = PseudoClass.getPseudoClass("upload-running-" + (i * 5));
        }
    }
    
    /**
     * Progress tree - simulated tasks
     */
    @FXML protected TreeView<FxSimulationLogEvent> simulationLogView;
    
    /* Simulation progress values */
    @FXML protected Text simulationInfoStatus;
    @FXML protected Text simulationInfoElapsedTime;
    @FXML protected Text simulationInfoAverageSpeed;
    @FXML protected Text simulationInfoDownloaded;
    @FXML protected Text simulationInfoUploaded;
    @FXML protected Text taskCountStatus;
    
    /**
     * Simulation run select box - can view history
     */
    @FXML private ChoiceBox<FxSimulationBarState> historySelect;
    
    /**
     * Whether simulation visualization is enabled
     */
    protected final BooleanProperty requestedDelayedDisplay = new SimpleBooleanProperty(true);
    /**
     * Layout pane
     */
    protected final ModelGraphLayoutPane graphLayoutPane;
    
    /**
     * Dummy task root - not visible
     */
    protected TreeItem<FxSimulationLogEvent> dummyRoot = new TreeItem<>();
    /**
     * Map of added task events and their respective tree items
     */
    protected Map<FxSimulationLogEvent, TreeItem<FxSimulationLogEvent>> eventMap = new HashMap<>();
    
    /**
     * Currently expanded tree item (task)
     */
    protected TreeItem<FxSimulationLogEvent> expanded;
    
    /**
     * Current state - context
     */
    private FxSimulationBarState runningState;
    
    /**
     * List of simulated results - used to construct result window
     */
    private final List<FxSimulatorTaskResultSet> resultSet;
        
    /**
     * Bar displaying simulation progress.
     * 
     * @param resultSet simulation results
     * @param graphLayoutPane layout pane
     */
    public FxSimulationBar(List<FxSimulatorTaskResultSet> resultSet, ModelGraphLayoutPane graphLayoutPane)
    {
        this.graphLayoutPane = graphLayoutPane;
        this.resultSet = resultSet;

        this.fxInit();
    }
        
    /**
     * Load FXML schema
     */
    private void fxInit()
    {
        FxHelper.loadFXMLAndSetController(getClass().getClassLoader().getResource("fxml/view/simulation/FxSimulationBar.fxml"), this);
    }
    
    /**
     * Initialize task tree cell factory
     */
    protected void initCellFactory()
    {
        this.simulationLogView.setCellFactory(tv ->
        {
            TreeCell<FxSimulationLogEvent> cell = new TreeCell<FxSimulationLogEvent>()
            {
                @Override public void updateItem(FxSimulationLogEvent item, boolean empty)
                {
                    Platform.runLater( () ->
                        {
                        super.updateItem(item, empty);

                        if(empty || item == null)
                        {
                            setText("");
                            setGraphic(null);
                        }
                        else
                        {
                            setText(item.getMessage());
                            ImageView imageView = item.getImageView();
                            if(imageView != null)
                            {
                                imageView.toFront();
                            }

                            setGraphic(imageView);

                        }
                    });
                }
            };
            
            cell.treeItemProperty().addListener((observable, oldVal, newVal) ->
            {
                Platform.runLater( () ->
                {
                    updateCellPseudoClass(cell, (newVal == null) ? null : newVal.getValue());
                });
            });
            
            return cell;
        });
    }
    
    /**
     * Tree item cell class is updated based on current event status.
     * 
     * @param cell cell
     * @param event event
     */
    protected void updateCellPseudoClass(TreeCell<FxSimulationLogEvent> cell, FxSimulationLogEvent event)
    {
        // success
        cell.pseudoClassStateChanged(CLASS_DONE_DOWNLOAD_SUCCESS, 
            (event != null && event.getType() == SimulationTaskType.GET && event.getStatus() == FxSimulationLogEventStatus.DONE && event.getParent() == null));
        cell.pseudoClassStateChanged(CLASS_DONE_UPLOAD_SUCCESS, 
            (event != null && event.getType() == SimulationTaskType.PUT && event.getStatus() == FxSimulationLogEventStatus.DONE && event.getParent() == null));

        int index = (event == null) ? -1 : (5 * Math.round((float) event.percentProperty().get()/ 5)) / 5;

        // running
        for(int i = 0; i < 20; i++)
        {
            cell.pseudoClassStateChanged(CLASS_DOWNLOAD_RUNNING[i], event != null && event.getType() == SimulationTaskType.GET && event.getStatus() == FxSimulationLogEventStatus.RUNNING && index == i);
            cell.pseudoClassStateChanged(CLASS_UPLOAD_RUNNING[i], event != null && event.getType() == SimulationTaskType.PUT && event.getStatus() == FxSimulationLogEventStatus.RUNNING && index == i);
        }
        
        // skipped
        cell.pseudoClassStateChanged(CLASS_DONE_SKIPPED, 
                (event != null && event.getParent() == null && event.getStatus() == FxSimulationLogEventStatus.SKIPPED));

        // error
        cell.pseudoClassStateChanged(CLASS_DONE_ERROR, 
            (event != null && event.getParent() == null && event.getStatus() == FxSimulationLogEventStatus.FAILED));
    }
    
    /**
     * Save simulation state - context
     */
    protected void saveSimulationState()
    {
        this.runningState.root = this.dummyRoot;
        
        this.runningState.setSimulationInfoAverageSpeed(this.simulationInfoAverageSpeed.getText());
        this.runningState.setSimulationInfoDownloaded(this.simulationInfoDownloaded.getText());
        this.runningState.setSimulationInfoElapsedTime(this.simulationInfoElapsedTime.getText());
        this.runningState.setSimulationInfoStatus(this.simulationInfoStatus.getText());
        this.runningState.setSimulationInfoUploaded(this.simulationInfoUploaded.getText());
    }
    
    /**
     * Load simulation state - context (history select)
     * 
     * @param state state
     */
    private void loadSimulationState(FxSimulationBarState state)
    {
        this.eventMap.clear();
        
        if(state == null)
        {
            this.dummyRoot = new TreeItem<>();
            
            this.simulationInfoAverageSpeed.setText("");
            this.simulationInfoDownloaded.setText("0");
            this.simulationInfoElapsedTime.setText("0");
            this.simulationInfoStatus.setText("");
            this.simulationInfoUploaded.setText("0");
        }
        else
        {
            this.dummyRoot = state.root;
            
            this.simulationInfoAverageSpeed.setText(state.getSimulationInfoAverageSpeed());
            this.simulationInfoDownloaded.setText(state.getSimulationInfoDownloaded());
            this.simulationInfoElapsedTime.setText(state.getSimulationInfoElapsedTime());
            this.simulationInfoStatus.setText(state.getSimulationInfoStatus());
            this.simulationInfoUploaded.setText(state.getSimulationInfoUploaded());
        }
        
        this.simulationLogView.setRoot(this.dummyRoot);
        
        this.runningState = state;
        this.historySelect.getSelectionModel().select(state);
    }
    
    /**
     * Get history state by state id.
     * 
     * @param id state id
     * @return state or null
     */
    private FxSimulationBarState getHistoryState(int id)
    {
        Optional<FxSimulationBarState> state =  this.historySelect.getItems().stream().filter(x -> x.id == id).findAny();
        
        if(state != null && state.isPresent())
        {
            return state.get();
        }
        
        return null;
    }
    
    /**
     * Construct results window from simulation results.
     * 
     * @return results window
     */
    private SimulationResultsWindow constructResultsWindow()
    {
        if(this.resultsDialog == null)
        {
            this.resultsDialog = new SimulationResultsWindow(this.resultSet);
        }
        
        return this.resultsDialog;
    }
    
    /**
     * Change control button meaning based on simulation status.
     */
    private void switchControlButton()
    {
        if(finished)
        {
            this.controlSimulationButton.setText("Results");
        }
        else
        {
            this.controlSimulationButton.setText("Finish");
        }
    }
    
    /**
     * Fires event {@link FxNodeSimulationEvent#NODE_SIMULATION_FINISHED_SUCCESS}.
     */
    private void displayResultsDialogAction()
    {
        fireEvent(new FxNodeSimulationEvent(FxNodeSimulationEvent.NODE_SIMULATION_FINISHED_SUCCESS, this.constructResultsWindow()));
    }
    
    /**
     * Initialize simulation bar - initialize task tree and add handlers
     * to controls.
     */
    @FXML public void initialize()
    {
        this.simulationLogView.setRoot(this.dummyRoot);
        this.simulationLogView.setShowRoot(false);
        
        // request simulation bar
        this.closeSimulationButton.setOnAction(action ->
        {
            fireEvent(new FxNodeSimulationEvent(FxNodeSimulationEvent.SIMULATION_VISUALISATION_OFF_REQUESTED));
        });
        this.closeSimulationButton.setDisable(true);
        
        // if finished, display results dialog, else finish simulation display
        this.controlSimulationButton.setOnAction(action ->
        {
            if(finished)
            {
                displayResultsDialogAction();
            }
            else
            {
                requestedDelayedDisplay.set(false);
            }
        });
        this.controlSimulationButton.setDisable(true);
        
        this.initCellFactory();
        
        this.historySelect.getSelectionModel().selectedItemProperty().addListener((observable, oldVal, newVal) ->
        {
            if(newVal != null)
            {
                saveSimulationState();
                loadSimulationState(newVal);
            }
        });
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public void beginSimulation()
    {
        Platform.runLater(() ->
        {
            historySelect.setDisable(true);
            controlSimulationButton.setDisable(false);
            fireEvent(new FxNodeSimulationEvent(FxNodeSimulationEvent.NODE_SIMULATION_DISABLE_CONTROLS_REQUEST));
        });
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public void endSimulation(boolean success)
    {
        Platform.runLater(() ->
        {
            historySelect.setDisable(false);
            closeSimulationButton.setDisable(false);
            
            finished = true;
            switchControlButton();
            
            fireEvent(new FxNodeSimulationEvent(FxNodeSimulationEvent.NODE_SIMULATION_ENABLE_CONTROLS_REQUEST));

            if(success)
            {
                displayResultsDialogAction();
            }
            else
            {
                fireEvent(new FxNodeSimulationEvent(FxNodeSimulationEvent.NODE_SIMULATION_FINISHED_FAILURE));
            }
            
        });
    }
        
    /**
     * {@inheritDoc}
     */
    @Override public void playSimulationEvent(FxSimulationLogEvent event)
    {
        Platform.runLater( () ->
        {
            TreeItem<FxSimulationLogEvent> eventItem = new TreeItem<>(event);

            this.eventMap.put(event, eventItem);

            TreeItem<FxSimulationLogEvent> rootItem;

            if(event.getParent() != null)
            {
                rootItem = this.eventMap.get(event.getParent());
            }
            else
            {
                rootItem = this.dummyRoot;
            }

            if(rootItem != null)
            {
                rootItem.getChildren().add(eventItem);

                if(expanded != null && expanded != dummyRoot)
                {
                    expanded.setExpanded(false);
                }

                rootItem.setExpanded(true);
                expanded = rootItem;
            }
        });
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public void updateSimulationEvent(FxSimulationLogEvent event)
    {
        Platform.runLater(() ->
        {
            TreeItem<FxSimulationLogEvent> eventItem = this.eventMap.get(event);

            if(eventItem != null)
            {
                eventItem.setValue(null);
                eventItem.setValue(event);
            }
        });
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public void updateSimulationStatus(String status, Color c)
    {
        Platform.runLater(() ->
        {
            this.simulationInfoStatus.setText(status);
            this.simulationInfoStatus.setFill(c);
        });
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public void updateSimulationElapsedTime(long elapsedTime)
    {
        // not all these update methods have to be necessarily run
        // on the GUI thread but they are so that the order of operations
        // is preserved
        Platform.runLater( () -> {
            this.simulationInfoElapsedTime.setText(FxHelper.msToHumanReadable(elapsedTime));
        });
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public void updateSimulationAverageSpeed(ByteSpeed averageSpeed)
    {
        // we are only guaranteed that averageSpeed will be constant throughout
        // this function, but any lambda functions executed later might not 
        // get the intended object
        ByteSpeed averageSpeedCpy = new ByteSpeed(averageSpeed.bpsProperty().get(), ByteSpeedUnits.BPS);
        
        Platform.runLater(() -> {
            this.simulationInfoAverageSpeed.setText(averageSpeedCpy.getHumanReadableFormat());
        });
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public void updateSimulationDownloaded(ByteSize downloaded)
    {
        ByteSize downloadedCpy = new ByteSize(downloaded.bytesProperty().get(), ByteSizeUnits.B);
        
        Platform.runLater(() -> {
            this.simulationInfoDownloaded.setText(downloadedCpy.getHumanReadableFormat());
        });
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public void updateSimulationUploaded(ByteSize uploaded)
    {
        ByteSize uploadedCpy = new ByteSize(uploaded.bytesProperty().get(), ByteSizeUnits.B);
        
        Platform.runLater(() -> {
            this.simulationInfoUploaded.setText(uploadedCpy.getHumanReadableFormat());
        });
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public void highlightPath(Collection<ModelNodeConnection> path, boolean highlight)
    {
        Platform.runLater(() ->
        {
            this.graphLayoutPane.highlightNodeLinks(path, highlight);
        });
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public void switchSimulationDisplay(SimulationType simulationType, int id)
    {        
        Platform.runLater(() ->
        {
            if (this.runningState != null)
            {
                this.saveSimulationState();
            }
            
            FxSimulationBarState state = this.getHistoryState(id);
            if(state == null)
            {
                state = new FxSimulationBarState(simulationType, id);
                this.historySelect.getItems().add(state);
            }
            this.loadSimulationState(state);
        });
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public BooleanProperty requestedDelayedDisplay()
    {
        return this.requestedDelayedDisplay;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public void updateTaskCounter(String counter)
    {
        Platform.runLater( () ->
        {
            this.taskCountStatus.setText(counter);
        });
    }

}
