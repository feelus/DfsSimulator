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

import cz.zcu.kiv.dfs_simulator.persistence.InvalidPersistedStateException;
import cz.zcu.kiv.dfs_simulator.persistence.StatePersistable;
import cz.zcu.kiv.dfs_simulator.persistence.StatePersistableAttribute;
import cz.zcu.kiv.dfs_simulator.persistence.StatePersistableElement;
import cz.zcu.kiv.dfs_simulator.persistence.StatePersistenceLogger;
import cz.zcu.kiv.dfs_simulator.model.ByteSpeed;
import cz.zcu.kiv.dfs_simulator.model.connection.ModelNodeConnection;
import cz.zcu.kiv.dfs_simulator.view.content.ModelGraphLayoutPane;
import cz.zcu.kiv.dfs_simulator.view.content.FxModelNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.application.Platform;

/**
 * Manager of existing {@link FxNodeConnectionWrapper} instances.
 */
public class FxNodeConnectionWrapperManager implements StatePersistable
{
    /**
     * Persistable identifier
     */
    public static final String PERSISTABLE_NAME = "node_connections";
    
    /**
     * Connection wrappers
     */
    protected final List<FxNodeConnectionWrapper> connections = new ArrayList<>();
    
    /**
     * Checks, whether {@code n1} and {@code n2} have an existing connection.
     * 
     * @param n1 node 1
     * @param n2 node 2
     * @return true if connection exists, false otherwise
     */
    public boolean areNodesConnected(FxModelNode n1, FxModelNode n2)
    {
        return (this.connections.stream().anyMatch(conn -> 
                (conn.getSource().equals(n1) && conn.getTarget().equals(n2)) ||
                        (conn.getSource().equals(n2) && conn.getTarget().equals(n1))));
    }
    
    /**
     * Get connection wrappers.
     * 
     * @return connection wrappers
     */
    public List<FxNodeConnectionWrapper> getConnections()
    {
        return this.connections;
    }
    
    /**
     * Get a node all connection wrappers where {@code node} is either
     * source or target node.
     * 
     * @param node node
     * @return connection wrappers
     */
    public List<FxNodeConnectionWrapper> getNodeConnections(FxModelNode node)
    {
        return (this.connections.stream()
                .filter(conn -> conn.getSource().equals(node) || conn.getTarget().equals(node))
                .collect(Collectors.toList()));
    }
    
    /**
     * Get a connection wrapper associated with {@code link}.
     * 
     * @param link visible link
     * @return connection wrapper
     */
    public FxNodeConnectionWrapper getFxNodeLinkConnection(FxNodeLink link)
    {
        Optional<FxNodeConnectionWrapper> wrapper = this.connections.stream().filter(
                conn -> conn.getFxNodeLink().equals(link)).findFirst();
        
        if(wrapper != null && wrapper.isPresent())
        {
            return wrapper.get();
        }
        
        return null;
    }
    
    /**
     * Get a connection wrapper associated with an underlying connection {@code link}.
     * Underlying connection can be either source-target or target-source.
     * 
     * @param link underlying model connection
     * @return wrapper or null
     */
    public FxNodeConnectionWrapper getNodeConnectionWraper(ModelNodeConnection link)
    {
        FxNodeConnectionWrapper result = null;
        
        for(FxNodeConnectionWrapper connWrapper : this.connections)
        {
            if((connWrapper.getSource().getNode().equals(link.getOrigin()) && connWrapper.getTarget().getNode().equals(link.getNeighbour())) ||
                    (connWrapper.getTarget().getNode().equals(link.getOrigin()) && connWrapper.getSource().getNode().equals(link.getNeighbour())))
            {
                result = connWrapper;
                
                break;
            }
        }
        
        return result;
    }
    
    /**
     * Remove connection wrapper.
     * 
     * @param conn wrapper
     */
    public void removeConnection(FxNodeConnectionWrapper conn)
    {
        conn.destroy();
        
        this.connections.remove(conn);
    }

    /**
     * {@inheritDoc}
     */
    @Override public List<StatePersistable> getPersistableChildren()
    {
        List<StatePersistable> l = new ArrayList<>();
        l.addAll(this.connections);
        
        return l;
    }

    /**
     * {@inheritDoc}
     */
    @Override public StatePersistableElement export(StatePersistenceLogger logger)
    {
        logger.logOperation(PERSISTABLE_NAME, "Exporting connection between nodes.", true);
        logger.logOperation(PERSISTABLE_NAME, "Exported " + this.connections.size() + " total connections.", true);
        
        return new StatePersistableElement(this.getPersistableName());
    }

    /**
     * {@inheritDoc}
     */
    @Override public void restoreState(StatePersistableElement state, StatePersistenceLogger logger, Object... args) throws InvalidPersistedStateException
    {
        if(args.length != 1 || !(args[0] instanceof ModelGraphLayoutPane))
        {
            throw new InvalidPersistedStateException("Internal error while restoring " + PERSISTABLE_NAME + ".");
        }
        
        logger.logOperation(PERSISTABLE_NAME, "Restoring connection between nodes.", true);
        
        ModelGraphLayoutPane graphLayoutPane = (ModelGraphLayoutPane) args[0];

        int i = 0;
        if(state != null)
        {
            for(StatePersistableElement childElem : state.getElements())
            {
                if(childElem.getName().equals(FxNodeConnectionWrapper.PERSISTABLE_NAME))
                {
                    i++;
                    this.restoreNodeConnectionWrapper(childElem, logger, graphLayoutPane);
                }
            }
        }
        
        if(i != 0)
        {
            logger.logOperation(PERSISTABLE_NAME, "Restored " + i + " total connections.", true);
        }
        else
        {
            logger.logOperation(PERSISTABLE_NAME, "Nothing to be restored", true);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public String getPersistableName()
    {
        return PERSISTABLE_NAME;
    }
    
    /**
     * Restore connection wrapper state.
     * 
     * @param element persisted element
     * @param logger persistence logger
     * @param graphLayoutPane layout pane
     * @throws InvalidPersistedStateException thrown when {@code element} state is invalid.
     */
    private void restoreNodeConnectionWrapper(StatePersistableElement element, StatePersistenceLogger logger, ModelGraphLayoutPane graphLayoutPane) throws InvalidPersistedStateException
    {
        // @TODO not very elegant since we have to know how is n1 and n2 exported
        // inside wrapper
        StatePersistableAttribute attrN1 = element.getAttribute("n1");
        StatePersistableAttribute attrN2 = element.getAttribute("n2");
        
        if(attrN1 == null || attrN2 == null || attrN1.getValue().isEmpty() || 
                attrN2.getValue().isEmpty())
        {
            throw new InvalidPersistedStateException("Invalid attributes for connection, expected n1 and n2: " + element);
        }
        
        FxModelNode n1 = graphLayoutPane.getChildrenNodeByID(attrN1.getValue());
        FxModelNode n2 = graphLayoutPane.getChildrenNodeByID(attrN2.getValue());
        
        if(n1 == null || n2 == null)
        {
            throw new InvalidPersistedStateException("Unable to match connection nodes by id: " + element);
        }
        
        FxNodeLink linkInstance = graphLayoutPane.getNodeLinkInstance();
        
        FxNodeConnectionWrapper wrapper = new FxNodeConnectionWrapper(n1, n2, linkInstance, new ByteSpeed(0), 0);
        
        wrapper.restoreState(element, logger);
        
        // add it to content pane
        graphLayoutPane.handleUserAddNodeConnection(wrapper);
        
        // @TODO is this guaranteed to work every time?
        // if we dont schedule it to run at a later time
        // both nodes will have 0 width and height
        Platform.runLater(() -> {
            linkInstance.graphicNodeBind(n1, n2);
        });
    }
}
