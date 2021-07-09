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

package cz.zcu.kiv.dfs_simulator.view.content;

import cz.zcu.kiv.dfs_simulator.persistence.StatePersistable;
import cz.zcu.kiv.dfs_simulator.model.ModelClientNode;
import cz.zcu.kiv.dfs_simulator.model.ModelNodeRegistry;
import cz.zcu.kiv.dfs_simulator.view.content.connection.FxNodeLink;
import cz.zcu.kiv.dfs_simulator.view.content.events.FxNodeSimulationEvent;
import cz.zcu.kiv.dfs_simulator.view.simulation.FxSimulatorTaskResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.image.Image;

/**
 * Graphical client node.
 */
public class FxModelClientNode extends FxModelNode
{
    /**
     * Persistable identifier
     */
    public static final String PERSISTABLE_NAME = "fx_client_node";
    
    /**
     * Graphical client node. Creates a new instance of {@link ModelClientNode}
     * used as underlying node.
     * 
     * @param graphLayoutPane graph layout pane (where this node will be placed)
     * @param registerModelNode if node should be registered via {@link ModelNodeRegistry#registerNode(cz.zcu.kiv.dfs_simulator.model.ModelNode)}
     */
    public FxModelClientNode(ModelGraphLayoutPane graphLayoutPane, boolean registerModelNode)
    {        
        super(new ModelClientNode(registerModelNode), graphLayoutPane);
    }
    
    /**
     * Graphical client node. Creates a new instance of {@link ModelClientNode}
     * used as underlying node, which is automatically registered by 
     * {@link ModelNodeRegistry#registerNode(cz.zcu.kiv.dfs_simulator.model.ModelNode)}.
     
     * @param graphLayoutPane graph layout pane (where this node will be placed)
     */
    public FxModelClientNode(ModelGraphLayoutPane graphLayoutPane)
    {
        this(graphLayoutPane, true);
    }
    
    /**
     * Get underlying client node.
     * 
     * @return client node
     */
    public ModelClientNode getClientNode()
    {
        return (ModelClientNode) node;
    }
    
    /**
     * Trigger simulation play requested event, that should be caught by
     * any parent up the hierarchy.
     * 
     * @param resultSet simulation results
     */
    public void nodeSimulationPlayRequested(List<FxSimulatorTaskResultSet> resultSet)
    {
        FxNodeSimulationEvent playEvent = new FxNodeSimulationEvent(
                FxNodeSimulationEvent.SIMULATION_VISUALISATION_ON_REQUESTED, resultSet);
        fireEvent(playEvent);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public FxModelNode duplicate()
    {
        return new FxModelClientNode(graphLayoutPane);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override protected void initiateImageViewImage()
    {
        try
        {
            Image img = new Image(getClass().getClassLoader().getResourceAsStream("img/client.png"));
            
            imageView.setImage(img);
        }
        catch(NullPointerException ex)
        {
            Logger.getLogger(FxNodeLink.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public List<StatePersistable> getPersistableChildren()
    {
        List<StatePersistable> l = new ArrayList<>();
        l.add(node);
        
        return l;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public String getPersistableName()
    {
        return PERSISTABLE_NAME;
    }
}
