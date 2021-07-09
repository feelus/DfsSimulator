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

import cz.zcu.kiv.dfs_simulator.simulation.SimulationPlan;
import cz.zcu.kiv.dfs_simulator.simulation.SimulationTask;
import java.util.Stack;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;

/**
 * Table with simulation tasks.
 */
public class SimulationTable extends TableView<SimulationTask>
{
    /**
     * Underlying simulation plan
     */
    protected SimulationPlan plan;
    /**
     * Deleted tasks
     */
    protected final Stack<SimulationTask> deleted = new Stack<>();
    /**
     * Whether any tasks were deleted
     */
    protected final BooleanProperty deletedItemsProperty = new SimpleBooleanProperty(false);
    
    /**
     * Initiate table with given plan {@code plan}.
     * 
     * @param plan plan
     */
    public void init(SimulationPlan plan)
    {
        this.plan = plan;
        
        // build an observable list on top of our plan's tasks
        setItems(FXCollections.observableList(plan.getTasks()));
        
        setRowFactory(this::rowFactory);
        setContextMenu(new ContextMenu(this.getUndoDeleteTaskMenuItem()));
    }
    
    /**
     * Table row factory - context menu added.
     * 
     * @param view view
     * @return table row
     */
    protected TableRow<SimulationTask> rowFactory(TableView<SimulationTask> view)
    {
        TableRow<SimulationTask> row = new TableRow<>();
        
        final MenuItem delete = this.getDeleteTaskMenuItem();
        final MenuItem undoDelete = this.getUndoDeleteTaskMenuItem();
        
        final ContextMenu cm = new ContextMenu(delete, undoDelete);
        
        row.contextMenuProperty().bind(
                Bindings.when(row.emptyProperty())
                        .then((ContextMenu) null)
                        .otherwise(cm));
                
        return row;
    }
    
    /**
     * Returns delete menu item - delete task from table.
     * 
     * @return delete menu item
     */
    protected MenuItem getDeleteTaskMenuItem()
    {
        final MenuItem delete = new MenuItem("Delete");
        
        delete.setOnAction(action ->
        {
            SimulationTask selected = getSelectionModel().getSelectedItem();
            
            if(selected != null)
            {
                deleted.push(selected);
                getItems().remove(selected);
                
                deletedItemsProperty.set(true);
            }
        });
        
        return delete;
    }
    
    /**
     * Returns undo delete menu item - restores deleted task.
     * 
     * @return undo delete item
     */
    protected MenuItem getUndoDeleteTaskMenuItem()
    {
        final MenuItem undoDelete = new MenuItem("Undo delete");
        
        undoDelete.setOnAction(action ->
        {
            if(!deleted.isEmpty())
            {
                getItems().add(deleted.pop());
                
                if(deleted.isEmpty())
                {
                    deletedItemsProperty.set(false);
                }
            }
        });
        
        undoDelete.disableProperty().bind(deletedItemsProperty.not());
        
        return undoDelete;
    }
    
    /**
     * Get simulation plan.
     * 
     * @return simulation plan
     */
    public SimulationPlan getPlan()
    {
        return this.plan;
    }
    
    /**
     * Reload tasks from plan.
     */
    public void resetItems()
    {
        setItems(FXCollections.observableList(this.plan.getTasks()));
    }
}
