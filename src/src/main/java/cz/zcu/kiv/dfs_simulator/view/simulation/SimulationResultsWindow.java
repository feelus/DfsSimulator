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

import cz.zcu.kiv.dfs_simulator.helpers.AllInclusiveThroughputHistoryReducer;
import cz.zcu.kiv.dfs_simulator.helpers.DouglasPeuckerThroughputHistoryReducer;
import cz.zcu.kiv.dfs_simulator.helpers.FxHelper;
import cz.zcu.kiv.dfs_simulator.helpers.MovingAverageReducer;
import cz.zcu.kiv.dfs_simulator.helpers.Pair;
import cz.zcu.kiv.dfs_simulator.helpers.RadialDistanceThroughputHistoryReducer;
import cz.zcu.kiv.dfs_simulator.helpers.SimulationThroughputHistoryReducer;
import cz.zcu.kiv.dfs_simulator.helpers.SimulatorPreferences;
import cz.zcu.kiv.dfs_simulator.model.ByteSize;
import cz.zcu.kiv.dfs_simulator.model.ByteSpeed;
import cz.zcu.kiv.dfs_simulator.simulation.DfsStringSimulatorLogger;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 * Window presenting simulation results.
 */
public class SimulationResultsWindow extends StackPane
{
    /**
     * Sorted results by speed
     */
    private final List<FxSimulatorTaskResultSet> resultsSorted;
    /**
     * Filtered throughput samples for all methods
     */
    private final List<List<Pair<Long, Long>>> throughputFiltered = new ArrayList<>();
    
    /**
     * Table with results
     */
    @FXML private TableView<FxSimulatorTaskResultSet> resultsTable;

    /* Results table columns*/
    @FXML private TableColumn<FxSimulatorTaskResultSet, String> resultsMethodCol;
    @FXML private TableColumn<FxSimulatorTaskResultSet, String> resultsTimeCol;
    @FXML private TableColumn<FxSimulatorTaskResultSet, String> resultsAvgSpeedCol;
    @FXML private TableColumn<FxSimulatorTaskResultSet, String> resultsDownloadedCol;
    @FXML private TableColumn<FxSimulatorTaskResultSet, String> resultsUploadedCol;
    
    /**
     * Results time unit choice box
     */
    @FXML private ChoiceBox<SimulationResultTimeUnit> timeUnitChoiceBox;
    /**
     * Results speed unit choice box
     */
    @FXML private ChoiceBox<SimulationResultByteSpeed> speedUnitChoiceBox;
    /**
     * Results size unit choice box
     */
    @FXML private ChoiceBox<SimulationResultByteSize> sizeUnitChoiceBox;
    
    /**
     * Chart visualization
     */
    @FXML private LineChart<Number, Number> visualisationChart;
    /* Chart axis */
    @FXML private NumberAxis visualisationChartXAxis;
    @FXML private NumberAxis visualisationChartYAxis;
    
    /**
     * Chart visualized result - visualized method
     */
    @FXML private ChoiceBox<FxSimulatorTaskResultSet> visualisedEntityBox;
    /**
     * Chart throughput filter
     */
    @FXML private ChoiceBox<SimulationThroughputHistoryReducer> visDataFilterSelect;
    
    /**
     * Simulation log result choice box - log for which method should be displayed
     */
    @FXML private ChoiceBox<FxSimulatorTaskResultSet> logRunBox;
    /**
     * Log view
     */
    @FXML private ListView<String> logRunListView;
    
    /**
     * Stack pane used for progress overlay
     */
    @FXML private StackPane stackPane;
    /**
     * Tab pane - result tabs
     */
    @FXML private TabPane tabPane;
    
    private Task runningTask = null;

    // currently unused but could be used to cancel running task on window close
    private boolean runningTaskCancelled = false;
    
    /**
     * Which method should be selected on re-open
     */
    private FxSimulatorTaskResultSet lastSelectedEntity;
    
    /**
     * Simulation results window.
     * 
     * @param results simulation results
     */
    public SimulationResultsWindow(List<FxSimulatorTaskResultSet> results)
    {
        // copy results and sort them by speed (DESC)
        this.resultsSorted = new ArrayList<>(results);
        
        // build stats
        this.resultsSorted.stream().forEach((r) ->
        {
            r.getSimulationResult().buildCumulativeStats();
        });
        
        Collections.sort(this.resultsSorted, (a, b) -> 
                Long.compare(b.getSimulationResult().getTotalAverageSpeed().bpsProperty().get(), a.getSimulationResult().getTotalAverageSpeed().bpsProperty().get()));
        
        this.fxInit();
    }
    
    /**
     * Load FXML schema
     */
    private void fxInit()
    {
        FxHelper.loadFXMLAndSetController(
                SimulationResultsWindow.class.getClassLoader().getResource("fxml/view/simulation/SimulationResultsWindow.fxml"), this);
    }
    
    /**
     * Initialize result unit choice boxes.
     */
    private void initUnitChoiceBoxes()
    {
        FxHelper.initSimulationResultTimeUnitChoiceBox(this.timeUnitChoiceBox);
        FxHelper.initSimulationResultByteSpeedChoiceBox(this.speedUnitChoiceBox);
        FxHelper.initSimulationResultByteSizeChoiceBox(this.sizeUnitChoiceBox);
    }
    
    /**
     * Forcefully update results table.
     */
    private void forceUpdateTable()
    {
        this.resultsTable.getColumns().get(0).setVisible(false);
        this.resultsTable.getColumns().get(0).setVisible(true);
    }
    
    /**
     * Set result time column factory.
     */
    private void setTimeColFactory()
    {
        this.resultsTimeCol.setCellValueFactory((TableColumn.CellDataFeatures<FxSimulatorTaskResultSet, String> p) -> {
            return new ReadOnlyStringWrapper(getConvertedResultTime(p.getValue()));
        });
                
        this.forceUpdateTable();
    }
    
    /**
     * Set result speed column factory.
     */
    private void setSpeedColFactory()
    {
        this.resultsAvgSpeedCol.setCellValueFactory((TableColumn.CellDataFeatures<FxSimulatorTaskResultSet, String> p) -> {
            return new ReadOnlyStringWrapper(getConvertedResultSpeed(p.getValue().getSimulationResult().getTotalAverageSpeed()));
        });
        
        this.forceUpdateTable();
    }
    
    /**
     * Set result size column factory.
     */
    private void setSizeColFactory()
    {
        this.resultsDownloadedCol.setCellValueFactory((TableColumn.CellDataFeatures<FxSimulatorTaskResultSet, String> p) -> {
            return new ReadOnlyStringWrapper(getConvertedResultSize(p.getValue().getSimulationResult().getTotalDownloaded()));
        });
        
        this.resultsUploadedCol.setCellValueFactory((TableColumn.CellDataFeatures<FxSimulatorTaskResultSet, String> p) -> {
            return new ReadOnlyStringWrapper(getConvertedResultSize(p.getValue().getSimulationResult().getTotalUploaded()));
        });
        
        this.forceUpdateTable();
    }
    
    /**
     * Initialize result unit choice box listeners.
     */
    private void initUnitChoiceListeners()
    {
        this.timeUnitChoiceBox.getSelectionModel().selectedItemProperty().addListener( (observable, oldValue, newValue) -> {
            setTimeColFactory();
        });
        
        this.speedUnitChoiceBox.getSelectionModel().selectedItemProperty().addListener( (observable, oldValue, newValue) -> {
            setSpeedColFactory();
        });
        
        this.sizeUnitChoiceBox.getSelectionModel().selectedItemProperty().addListener( (observable, oldValue, newValue) -> {
            setSizeColFactory();
        });
    }

    /**
     * Display this object as a dialog.
     * 
     * @param owner dialog owner
     * @param title dialog title
     */
    public void displayAsDialog(Window owner, String title)
    {
        Stage dialogStage = new Stage();

        dialogStage.initStyle(StageStyle.DECORATED);
        dialogStage.setTitle(title);
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.setResizable(true);
        dialogStage.initOwner(owner);
        
        if(getScene() == null)
        {
            Scene scene = new Scene(this);
            dialogStage.setScene(scene);
        }
        else
        {
            dialogStage.setScene(getScene());
        }
        
        dialogStage.setOnCloseRequest(event -> {
            if(this.runningTask != null)
            {
                this.runningTask.cancel();
                this.runningTaskCancelled = true;
            }
        });
        
        dialogStage.setOnShown(event -> {
            // previously cancelled task
            if(this.runningTaskCancelled)
            {
                this.initVisualisation();
                this.runningTaskCancelled = false;
            }
        });
        
        dialogStage.showAndWait();
    }
    
    /**
     * Filter measured throughput samples. During reducing, progress overlay
     * is displayed.
     * 
     * @param reducer throughput filter (reducer)
     */
    private void filterThroughputData(SimulationThroughputHistoryReducer reducer)
    {
        this.throughputFiltered.clear();
        
        final ProgressBar pb = new ProgressBar(0);
        
        Task t = new Task<Void>() 
        {
            @Override protected Void call() throws Exception
            {
                int pbMax = resultsSorted.size();
                int pbDone = 0;
                
                updateProgress(pbDone, pbMax);
                
                for(FxSimulatorTaskResultSet rs : resultsSorted)
                {   
                    if(!isCancelled())
                    {
                        throughputFiltered.add(reducer.reduce(rs.getSimulationResult().getCumThroughputHistory(), 
                                rs.getSimulationResult().getTotalElapsedTime().get(), rs.getSimulationResult().getTotalAverageSpeed()));

                        pbDone++;
                        updateProgress(pbDone, pbMax);
                    }
                }
                
                return null;
            }
            
        };
        
        pb.progressProperty().bind(t.progressProperty());

        VBox box = new VBox();
        
        t.setOnCancelled(e -> {
            getChildren().remove(box);
            tabPane.setDisable(false);
            reducer.cancel();
        });
        
        t.setOnFailed( e -> {
            getChildren().remove(box);
            tabPane.setDisable(false);
            
            Alert a = FxHelper.getErrorDialog("Results graph build error", 
                    "There was an error while building results graphs.", 
                    "Unable to display measured values.");
            
            a.showAndWait();
        });
        
        t.setOnRunning(e -> {
            Text text = new Text("Building results...");
            text.setFont(new Font(22));
            
            box.setSpacing(15d);
            box.setAlignment(Pos.CENTER);
            
            box.getChildren().add(text);
            box.getChildren().add(pb);
            
            tabPane.setDisable(true);
            stackPane.getChildren().add(box);
        });
        
        t.setOnSucceeded(e -> {
            stackPane.getChildren().remove(box);
            tabPane.setDisable(false);
            
            // set Y max range
            if(reducer.getMaximumY() != null)
            {
                visualisationChartYAxis.setUpperBound(reducer.getMaximumY() * 1.2d);
                visualisationChartYAxis.setTickUnit(reducer.getMaximumY() / 10);
            }
            
            this.addVisualisedEntityOptions();
        });
                
        (new Thread(t)).start();
        
        this.runningTask = t;
    }
        
    /**
     * Construct graph from throughput samples.
     * 
     * @param resultIndexes result index
     */
    private void displayVisualisationData(int[] resultIndexes)
    {
        if(resultIndexes.length < 0)
        {
            return;
        }
        
        this.visualisationChartXAxis.setLabel("Simulation time (ms)");
        this.visualisationChartYAxis.setLabel("Throughput (bps)");
        
        final ProgressBar pb = new ProgressBar(0);
        
        List<XYChart.Series<Number, Number>> seriesList = new ArrayList<>();
        
        for(int i = 0; i < resultIndexes.length; i++)
        {
            seriesList.add(new XYChart.Series<>());
        }
        
        Task t = new Task<Void>() {
            
            @Override protected Void call() throws Exception
            {
                int pbMax = 0;
                int pbDone = 0;
                
                // calculate total
                for(int i = 0; i < resultIndexes.length; i++)
                {
                    pbMax += throughputFiltered.get(resultIndexes[i]).size();
                }
                
                updateProgress(pbDone, pbMax);
                
                for(int i = 0; i < resultIndexes.length && !isCancelled(); i++)
                {
                    XYChart.Series<Number, Number> resultSeries = seriesList.get(i);
                    List<Pair<Long, Long>> filteredSet = throughputFiltered.get(resultIndexes[i]);
                    
                    for(int x = 0; x < filteredSet.size() && !isCancelled(); x++)
                    {
                        resultSeries.getData().add(
                                new XYChart.Data<>(filteredSet.get(x).first, filteredSet.get(x).second));
                        
                        pbDone++;
                        // @TODO maybe dont do it every cycle since
                        // this probably slows it down?
                        updateProgress(pbDone, pbMax);
                    }
                }
                
                return null;
                
            }
            
        };
        
        pb.progressProperty().bind(t.progressProperty());
        
        VBox box = new VBox();
        
        t.setOnRunning(e -> {
            Text text = new Text("Displaying data...");
            text.setFont(new Font(22));
            
            box.setSpacing(15d);
            box.setAlignment(Pos.CENTER);
            
            box.getChildren().add(text);
            box.getChildren().add(pb);
            
            tabPane.setDisable(true);
            getChildren().add(box);
        });
        
        t.setOnCancelled(e -> {
            getChildren().remove(box);
            tabPane.setDisable(false);
        });
        
        t.setOnFailed( e -> {
            getChildren().remove(box);
            tabPane.setDisable(false);
            
            Alert a = FxHelper.getErrorDialog("Results graph build error", 
                    "There was an error while building results graphs.", 
                    "Unable to display measured values.");
            
            a.showAndWait();
        });
        
        t.setOnSucceeded(e -> {
            this.visualisationChart.getData().addAll(seriesList);
            
            getChildren().remove(box);
            tabPane.setDisable(false);
        });
        
        (new Thread(t)).start();
        
        this.runningTask = t;
    }
    
    /**
     * Initiate throughput filter listener.
     */
    private void initVisualisationListeners()
    {
        this.visDataFilterSelect.getSelectionModel().selectedItemProperty().addListener( (observable, oldValue, newValue) -> {
            if(newValue != null)
            {
                lastSelectedEntity = visualisedEntityBox.getSelectionModel().getSelectedItem();
                
                filterThroughputData(newValue);
            }
        });
        
        this.visualisedEntityBox.getSelectionModel().selectedIndexProperty().addListener( (observable, oldValue, newValue) -> {            
            if(newValue != null && newValue.intValue() >= 0 && newValue.intValue() < visualisedEntityBox.getItems().size())
            {
                visualisationChart.getData().clear();
                
                displayVisualisationData(new int[] {newValue.intValue()});
            }
        });
    }
    
    /**
     * Initialize throughput chart and it's filter.
     */
    private void initVisualisation()
    {        
        this.visualisationChart.getData().clear();
        this.visDataFilterSelect.getItems().clear();
        
        this.visualisationChart.setLegendVisible(false);
        this.visualisationChart.setCreateSymbols(false);
        this.visualisationChart.setAnimated(false);
        
        this.visDataFilterSelect.getItems().add(new MovingAverageReducer());
        this.visDataFilterSelect.getItems().add(new RadialDistanceThroughputHistoryReducer());
        this.visDataFilterSelect.getItems().add(new DouglasPeuckerThroughputHistoryReducer());
        this.visDataFilterSelect.getItems().add(new AllInclusiveThroughputHistoryReducer());
        this.visDataFilterSelect.getSelectionModel().selectFirst();
    }
    
    /**
     * Initiate visualized entity choice box (populated from results).
     */
    private void addVisualisedEntityOptions()
    {        
        this.visualisedEntityBox.getItems().clear();
        this.visualisedEntityBox.getItems().addAll(this.resultsSorted);
        
        if(this.lastSelectedEntity == null)
        {
            this.visualisedEntityBox.getSelectionModel().selectFirst();
        }
        else
        {
            this.visualisedEntityBox.getSelectionModel().select(this.lastSelectedEntity);
            this.lastSelectedEntity = null;
        }
    }
    
    /**
     * Load simulation log messages into log view.
     * 
     * @param logger simulation logger
     */
    private void displaySimulatorRunLog(DfsStringSimulatorLogger logger)
    {
        this.logRunListView.getItems().addAll(logger.getMessages());
    }
    
    /**
     * Initiate log view - add all methods.
     */
    private void initLog()
    {
        this.logRunBox.getSelectionModel().selectedItemProperty().addListener( (observable, oldValue, newValue) -> {
            logRunListView.getItems().clear();
            
            displaySimulatorRunLog(newValue.getLogger());
        });
        
        this.logRunBox.getItems().addAll(this.resultsSorted);
        this.logRunBox.getSelectionModel().selectFirst();
    }
    
    /**
     * Convert time to string based on currently set time unit.
     * 
     * @param result task result
     * @return converted time
     */
    private String getConvertedResultTime(FxSimulatorTaskResultSet result)
    {
        long totalElapsedTime = result.getSimulationResult().getTotalElapsedTime().get();
        
        switch(this.timeUnitChoiceBox.getValue())
        {
            case MILLISECOND:
                return totalElapsedTime + "";
            case SECOND:
                return (totalElapsedTime * 0.001) + "";
            case MINUTE:
                return (totalElapsedTime * (0.001 / 60d)) + "";
            case HOUR:
                return (totalElapsedTime * (0.001 / 3660d)) + "";
            case HUMAN_READABLE:
            default:
                return FxHelper.msToHumanReadable(totalElapsedTime);
                
        }
    }
    
    /**
     * Convert size to string based on currently set size unit.
     * 
     * @param size size
     * @return converted size
     */
    private String getConvertedResultSize(ByteSize size)
    {
        switch(this.sizeUnitChoiceBox.getValue())
        {
            case B:
                return size.bytesProperty().get() + "";
            case KB:
                return size.kiloBytesProperty().get() + "";
            case MB:
                return size.megaBytesProperty().get() + "";
            case GB:
                return size.gigaBytesProperty().get() + "";
            case HUMAN_READABLE:
            default:
                return size.getHumanReadableFormat();
        }
    }
    
    /**
     * Convert speed to string based on currently set speed unit.
     * 
     * @param speed speed
     * @return converted speed
     */
    private String getConvertedResultSpeed(ByteSpeed speed)
    {
        switch(this.speedUnitChoiceBox.getValue())
        {
            case BPS:
                return speed.bpsProperty().get() + "";
            case KBPS:
                return speed.kBpsProperty().get() + "";
            case MBPS:
                return speed.mBpsProperty().get() + "";
            case GBPS:
                return speed.gBpsProperty().get() + "";
            case HUMAN_READABLE:
            default:
                return speed.getHumanReadableFormat();
        }
    }
    
    /**
     * Convert results using currently set unit options for time, speed and size
     * into a CSV string.
     * 
     * @return CSV string
     */
    private String getResultsAsCsv()
    {
        SimulationResultTimeUnit timeUnit = this.timeUnitChoiceBox.getValue();
        SimulationResultByteSize sizeUnit = this.sizeUnitChoiceBox.getValue();
        SimulationResultByteSpeed speedUnit = this.speedUnitChoiceBox.getValue();
        
        String timeUnitShort = (timeUnit != SimulationResultTimeUnit.HUMAN_READABLE) ? "(" + timeUnit.getShortName() + ")" : "";
        String sizeUnitShort = (sizeUnit != SimulationResultByteSize.HUMAN_READABLE) ? "(" + sizeUnit.getUnit().toString() + ")": "";
        String speedUnitShort = (speedUnit != SimulationResultByteSpeed.HUMAN_READABLE) ? "(" + speedUnit.getUnit().toString() + ")" : "";
        
        StringBuilder sb = new StringBuilder("method,time");
        sb.append(timeUnitShort);
        sb.append(",avg.speed");
        sb.append(speedUnitShort);
        sb.append(",downloaded");
        sb.append(sizeUnitShort);
        sb.append(",uploaded");
        sb.append(sizeUnitShort);
        sb.append("\n");
        
        this.resultsSorted.stream().forEach(res -> {
            sb.append(res.getSimulationResult().getType());
            sb.append(",");
            sb.append(getConvertedResultTime(res));
            sb.append(",\"");
            sb.append(getConvertedResultSpeed(res.getSimulationResult().getTotalAverageSpeed()));
            sb.append("\",\"");
            sb.append(getConvertedResultSize(res.getSimulationResult().getTotalDownloaded()));
            sb.append("\",\"");
            sb.append(getConvertedResultSize(res.getSimulationResult().getTotalUploaded()));
            sb.append("\"\n");
        });

        return sb.toString();
    }
    
    /**
     * Export results using currently set time unit options into external
     * CSV file.
     */
    public void exportResultsCsv()
    {
        FileChooser fileChooser = FxHelper.getSimulatorFileChooser();
        fileChooser.setTitle("Save results");

        FileChooser.ExtensionFilter extFilter
                = new FileChooser.ExtensionFilter("CSV", "*.csv");
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setInitialFileName("simulation-results.csv");
        
        File file = fileChooser.showSaveDialog(getScene().getWindow());
        
        if (file != null) 
        {
            SimulatorPreferences.setLastOpenDir(file.getParentFile().getAbsolutePath());
            
            try(PrintStream ps = new PrintStream(file)) 
            { 
                ps.println(getResultsAsCsv()); 
            }
            catch (FileNotFoundException ex)
            {
                Dialog d = FxHelper.getErrorDialog("Error exporting results", 
                        "Unable to export results to file " + file.getAbsolutePath() + ".", ex.getMessage());
                d.showAndWait();
            }
        }
    }
    
    /**
     * Initialize graphic components.
     */
    @FXML public void initialize()
    {
        final int resultsSize = this.resultsSorted.size();
        
        this.resultsTable.setRowFactory(tv -> new TableRow<FxSimulatorTaskResultSet>()
        {
            @Override public void updateItem(FxSimulatorTaskResultSet item, boolean empty)
            {
                super.updateItem(item, empty);
                
                if (item == null)
                {
                    setStyle("");
                    setDisable(false);
                }
                else if(resultsSize >= 2)
                {
                    if(item == resultsSorted.get(0))
                    {
                        setStyle("-fx-background-color: rgb(87, 183, 87, 0.3);");
                        setDisable(true);
                    }
                    else if(item == resultsSorted.get(1))
                    {
                        setStyle("-fx-background-color: rgb(248, 163, 26, 0.3);");
                        setDisable(true);
                    }
                    else
                    {
                        setStyle("");
                        setDisable(false);
                    }
                }
            }
        });
        
        this.resultsMethodCol.setCellValueFactory((TableColumn.CellDataFeatures<FxSimulatorTaskResultSet, String> p) -> 
                new ReadOnlyStringWrapper(p.getValue().getSimulationResult().getType().toString()));
        
        this.resultsTable.getItems().addAll(this.resultsSorted);
        
        this.initUnitChoiceListeners();
        this.initUnitChoiceBoxes();
        this.initVisualisationListeners();
        this.initVisualisation();
        this.initLog();
        this.visualisationChartYAxis.setAutoRanging(false);
    }
    
}
