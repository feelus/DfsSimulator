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

import cz.zcu.kiv.dfs_simulator.model.connection.ConnectionCharacteristicPoint;
import cz.zcu.kiv.dfs_simulator.model.connection.LineConnectionCharacteristic;
import cz.zcu.kiv.dfs_simulator.view.BaseDialog;
import java.util.Random;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.input.MouseEvent;

/**
 * Chart connection characteristic point move handler (point can be dragged
 * by mouse).
 */
class MarkerNodeMoveHandler implements EventHandler<MouseEvent>
{
    /**
     * Chart data point
     */
    private final XYChart.Data<Double, Double> data;
    /**
     * Underlying characteristic point
     */
    private final ConnectionCharacteristicPoint point;
    /**
     * Associated axis
     */
    private final NumberAxis yAxis;
    /**
     * Associated characteristic
     */
    private final LineConnectionCharacteristic characteristic;
    
    /**
     * Chart data point move handler.
     * 
     * @param point underlying characteristic point
     * @param data chart data point
     * @param yAxis associated axis
     * @param characteristic associated characteristic
     */
    public MarkerNodeMoveHandler(ConnectionCharacteristicPoint point, XYChart.Data<Double, Double> data, NumberAxis yAxis, LineConnectionCharacteristic characteristic)
    {
        this.point = point;
        this.data = data;
        this.yAxis = yAxis;
        this.characteristic = characteristic;
    }
    
    /**
     * Handle mouse event. Data point can be moved along it's Y axis (vertically)
     * to adjust underlying characteristic point's value.
     * 
     * @param event mouse event
     */
    @Override public void handle(MouseEvent event)
    {
        if(event.getEventType() == MouseEvent.MOUSE_DRAGGED)
        {
            double yPosInAxis = this.yAxis.sceneToLocal(new Point2D(0, event.getSceneY())).getY();
            double y = yAxis.getValueForDisplay(yPosInAxis).doubleValue();
            
            if(y < this.characteristic.getYLowerBound())
            {
                y = this.characteristic.getYLowerBound();
            }
            else if(y > this.characteristic.getYUpperBound())
            {
                y = this.characteristic.getYUpperBound();
            }
            
            // i would bind it but it was not being properly updated for some reason
            this.point.yProperty().set(y);
            this.data.YValueProperty().set(y);
        }
    }
    
}

/**
 * Graphical dialog used for adjusting connection's {@link LineConnectionCharacteristic}.
 */
public class FxNodeLinkCharacteristicDialog extends BaseDialog
{
    /**
     * Maximum Y change between two consecutive points when randomly generating
     */
    private static final double RANDOM_MAX_STEP_CHANGE = 0.2;
    
    /**
     * Connection characteristic
     */
    private LineConnectionCharacteristic characteristic;
    
    /**
     * Chart
     */
    @FXML private LineChart<Number, Number> lineChart;
    
    /**
     * X axis (time)
     */
    @FXML private NumberAxis xAxis;
    /**
     * Y axis (throughput)
     */
    @FXML private NumberAxis yAxis;
    
    /**
     * Graphical dialog used for defining {@link LineConnectionCharacteristic}.
     */
    public FxNodeLinkCharacteristicDialog()
    {
        super(FxNodeLinkCharacteristicDialog.class.getClassLoader().getResource("fxml/view/content/connection/FxNodeLinkCharacteristicDialog.fxml"));
    }
    
    /**
     * Add mouse listeners to data marker.
     * 
     * @param markerNode marker node
     * @param d data marker
     * @param p underlying characteristic point associated with this marker
     */
    private void addMarkerNodeListeners(Node markerNode, XYChart.Data<Double, Double> d, ConnectionCharacteristicPoint p)
    {
        markerNode.setOnMouseEntered(event -> setCursor(Cursor.HAND));
        markerNode.setOnMouseExited(event -> setCursor(Cursor.DEFAULT));
        markerNode.setOnMouseDragged(new MarkerNodeMoveHandler(p, d, this.yAxis, this.characteristic));
    }
    
    /**
     * Add a chart data marker for given point {@code p}.
     * 
     * @param p characteristic point
     * @param series graph series
     */
    private void addCharacteristicPointMarker(ConnectionCharacteristicPoint p, XYChart.Series series)
    {
        XYChart.Data<Double, Double> d = new XYChart.Data<>(p.xProperty().get(), p.yProperty().get());
        series.getData().add(d);
        
        Platform.runLater(() -> {
            addMarkerNodeListeners(d.getNode(), d, p);
        });
    }
    
    /**
     * {@inheritDoc}
     */
    @Override @FXML public void initialize()
    {
        this.lineChart.setTitle("Link time throughput characteristics");
        this.lineChart.setAnimated(false);
        
        this.lineChart.getXAxis().setAutoRanging(false);
        this.lineChart.getYAxis().setAutoRanging(false);
        this.lineChart.getYAxis().setLabel("Throughput modifier");
        this.lineChart.getXAxis().setLabel("Time");
        
        this.lineChart.setLegendVisible(false);
    }
    
    /**
     * Set characteristic that this dialog is associated with.
     * 
     * @param characteristic characteristic
     */
    public void setCharacteristics(LineConnectionCharacteristic characteristic)
    {
        this.characteristic = characteristic;
                
        double tickUnitX = (double) (this.characteristic.getXUpperBound() - this.characteristic.getXLowerBound()) / this.characteristic.getDiscretePoints().size();
        this.xAxis.setTickUnit(tickUnitX);
        double tickUnitY = (double) (this.characteristic.getYUpperBound()- this.characteristic.getYLowerBound()) / this.characteristic.getDiscretePoints().size();
        this.yAxis.setTickUnit(tickUnitY);
        
        this.xAxis.setUpperBound(this.characteristic.getXUpperBound());
        this.xAxis.setLowerBound(this.characteristic.getXLowerBound());
        
        this.yAxis.setUpperBound(this.characteristic.getYUpperBound() + tickUnitX);
        this.yAxis.setLowerBound(this.characteristic.getYLowerBound());
        
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        
        this.characteristic.getDiscretePoints().stream().forEach((p) ->
        {
            this.addCharacteristicPointMarker(p, series);
        });
        
        this.lineChart.getData().add(series);
    }
    
    /**
     * Generate random characteristic values.
     */
    public void handleGenerateRandom()
    {
        if(this.lineChart.getData().size() == 1)
        {
            XYChart.Series<Number, Number> series = this.lineChart.getData().get(0);
            
            if(!series.getData().isEmpty())
            {
                Random r = new Random(System.currentTimeMillis());
                double yRange = (this.characteristic.getYUpperBound() - this.characteristic.getYLowerBound());
                double maxYChange = yRange * RANDOM_MAX_STEP_CHANGE;
                double prevY = this.characteristic.getYLowerBound() + 
                        (yRange) * r.nextDouble();

                series.getData().get(0).setYValue(prevY);

                for(int i = 1; i < series.getData().size(); i++)
                {
                    double cL = (prevY - maxYChange) >= this.characteristic.getYLowerBound() ? (prevY - maxYChange) : this.characteristic.getYLowerBound();
                    double cH = (prevY + maxYChange) <= this.characteristic.getYUpperBound() ? (prevY + maxYChange) : this.characteristic.getYUpperBound();

                    double cY = cL + (cH - cL) * r.nextDouble();

                    series.getData().get(i).setYValue(cY);
                }
            }
        }
    }
    
    /**
     * Reset characteristic to initial values.
     */
    public void handleReset()
    {
        if(this.lineChart.getData().size() == 1)
        {
           this.lineChart.getData().get(0).getData().stream().forEach(d -> 
                   d.setYValue(characteristic.getYUpperBound()));
        }
    }
    
    /**
     * Confirm characteristic change - propagate values from chart data
     * points to underlying characteristic.
     */
    public void handleConfirm()
    {
        if(this.lineChart.getData().size() == 1)
        {
            for(int i = 0; i < this.lineChart.getData().get(0).getData().size(); i++)
            {
                this.characteristic.getDiscretePoints().get(i).yProperty().set(
                        this.lineChart.getData().get(0).getData().get(i).getYValue().doubleValue());
            }
        }
        
        stage.close();
    }
    
    /**
     * Cancel characteristic dialog - do not save changes.
     */
    public void handleCancel()
    {
        stage.close();
    }
}
