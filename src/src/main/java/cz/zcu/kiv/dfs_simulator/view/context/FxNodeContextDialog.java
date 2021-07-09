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

package cz.zcu.kiv.dfs_simulator.view.context;

import cz.zcu.kiv.dfs_simulator.model.connection.ModelNodeConnection;
import cz.zcu.kiv.dfs_simulator.view.content.FxModelNode;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * Node context (node settings) dialog.
 */
public abstract class FxNodeContextDialog extends GridPane
{
    /**
     * Associated node
     */
    protected final FxModelNode fxNode;
    
    /**
     * Node context (settings) dialog.
     * 
     * @param fxNode node
     */
    public FxNodeContextDialog(FxModelNode fxNode)
    {
        this.fxNode = fxNode;
    }
    
    /**
     * Set delete button handler - deletes node and closes this dialog.
     * 
     * @param button delete button
     */
    protected void setDeleteButtonHandlers(Button button)
    {
        button.setOnAction(actionEvent ->
        {
            if(fxNode.handleUserDelete())
            {
                // close itself since the node no longer exists
                ((Stage)getScene().getWindow()).close();
            }
        });
    }
    
    /**
     * Populate connection table from {@code connectionTable}. 
     * 
     * @param connectionTable connection table
     * @param linkBandwidthCol bandwidth column
     */
    protected void initConnectionTable(FxConnectionTable connectionTable, TableColumn<ModelNodeConnection, String> linkBandwidthCol)
    {
        connectionTable.setFxNode(this.fxNode);
        connectionTable.setItems(this.fxNode.getFxConnectionManager().getConnections());
        
        linkBandwidthCol.setCellValueFactory((TableColumn.CellDataFeatures<ModelNodeConnection, String> p) -> p.getValue().getMaximumBandwidth().humanReadableProperty());
    }
    
    /**
     * Add listener to node connections and update connection count.
     * 
     * @param infoConnectionCount connection count
     */
    protected void setConnectionListener(Text infoConnectionCount)
    {
        this.fxNode.getFxConnectionManager().getConnections().addListener((ListChangeListener) listener ->
        {
            updateConnectionCount(infoConnectionCount);
        });
    }
    
    /**
     * Update connection count (text).
     * 
     * @param infoConnectionCount connection count
     */
    protected void updateConnectionCount(Text infoConnectionCount)
    {
        infoConnectionCount.setText(fxNode.getFxConnectionManager().getConnections().size() + "");
    }
}
