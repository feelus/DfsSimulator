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
import cz.zcu.kiv.dfs_simulator.helpers.Helper;
import cz.zcu.kiv.dfs_simulator.model.ByteSpeed;
import cz.zcu.kiv.dfs_simulator.model.connection.LineConnectionCharacteristic;
import cz.zcu.kiv.dfs_simulator.view.content.FxModelNode;
import cz.zcu.kiv.dfs_simulator.model.connection.ModelNodeConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Group;

/**
 * Graphical wrapper of a connection between two nodes. Wrapper allows 
 * only symmetrical connections.
 */
public class FxNodeConnectionWrapper extends Group implements StatePersistable
{
    /**
     * Persistable identifier
     */
    public static final String PERSISTABLE_NAME = "connection";
    
    /**
     * Graphical source node
     */
    protected final FxModelNode source;
    /**
     * Graphical target node
     */
    protected final FxModelNode target;
    
    /**
     * Connection bandwidth (both ways)
     */
    protected final ByteSpeed bandwidth;
    /**
     * Connection latency
     */
    protected final IntegerProperty latency;
    
    /**
     * Underlying visible link - this is what user sees
     */
    protected FxNodeLink fxNodeLink;
    
    /**
     * Symmetrical connection characteristic
     */
    private LineConnectionCharacteristic characteristic;
    
    /**
     * Oriented connection from source to target
     */
    private ModelNodeConnection sourceTargetConn;
    /**
     * Oriented connection from target to source
     */
    private ModelNodeConnection targetSourceConn;
    
    /**
     * Graphical connection wrapper between two nodes {@code source} and {@code target}.
     * Created connection is symmetrical. Wrapper connection is oriented from 
     * {@code source} to {@code target}.
     * 
     * @param source source node
     * @param target target node
     * @param fxNodeLink visible link displayed on layout pane
     * @param bandwidth connection bandwidth
     * @param latency connection latency in ms
     */
    public FxNodeConnectionWrapper(FxModelNode source, FxModelNode target, FxNodeLink fxNodeLink, ByteSpeed bandwidth, int latency)
    {
        this.source = source;
        this.target = target;
                
        this.fxNodeLink = fxNodeLink;
        this.bandwidth = bandwidth;
        this.latency = new SimpleIntegerProperty(latency);
        
        this.characteristic = new LineConnectionCharacteristic();

        this.addLinkToChildren();
        this.setNodeConnections();
        this.setLinkLabel();
        this.bindLinkLabel();
    }
    
    /**
     * Add visible to children nodes.
     */
    private void addLinkToChildren()
    {
        getChildren().add(this.fxNodeLink);
    }
    
    /**
     * Create connection {@link ModelNodeConnection} from source to target
     * and from target to source. Afterwards, created connections are added
     * to their respective node's connection manager.
     */
    private void setNodeConnections()
    {
        this.sourceTargetConn = 
                new ModelNodeConnection(this.source.getNode(), this.target.getNode(), this.bandwidth, this.latency, this.characteristic);
        this.targetSourceConn = 
                new ModelNodeConnection(this.target.getNode(), this.source.getNode(), this.bandwidth, this.latency, this.characteristic);
        
        // add n2 as neighbour to n1
        this.source.getFxConnectionManager().getConnections().add(this.sourceTargetConn);
        // add n1 as neighbour to n2
        this.target.getFxConnectionManager().getConnections().add(targetSourceConn);
    }
    
    /**
     * Replace visible link with a different one.
     * 
     * @param fxNodeLink visible link
     */
    private void replaceFxLink(FxNodeLink fxNodeLink)
    {
        // attempt to remove current node link
        getChildren().remove(this.fxNodeLink);
        
        this.fxNodeLink = fxNodeLink;
        
        getChildren().add(this.fxNodeLink);
    }
    
    /**
     * Switch currently visible link with a different one 
     * constructed from given class {@code className}.
     * 
     * @param className link class name
     */
    public void switchFxLinkType(String className)
    {
        try
        {
            Object o = Class.forName(className).newInstance();
            
            if(o instanceof FxNodeLink)
            {
                FxNodeLink newLink = (FxNodeLink) o;
                
                newLink.graphicNodeBind(source, target);
                
                this.replaceFxLink(newLink);
                this.setLinkLabel();
            }
        }
        catch(ClassNotFoundException | InstantiationException | IllegalAccessException ex)
        {
            Logger.getLogger(FxNodeLink.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Return visible link.
     * 
     * @return visible link
     */
    public FxNodeLink getFxNodeLink()
    {
        return this.fxNodeLink;
    }
    
    /**
     * Returns connection source.
     * 
     * @return source
     */
    public FxModelNode getSource()
    {
        return this.source;
    }
    
    /**
     * Returns connection target.
     * 
     * @return target
     */
    public FxModelNode getTarget()
    {
        return this.target;
    }
    
    /**
     * Returns wrapper connection bandwidth.
     * 
     * @return bandwidth
     */
    public ByteSpeed getBandwidth()
    {
        return this.bandwidth;
    }
    
    /**
     * Sets wrapper connection latency.
     * 
     * @param latency latency in ms
     */
    public void setLatency(int latency)
    {
        this.latency.set(latency);
    }
    
    /**
     * Returns wrapper connection latency.
     * 
     * @return latency in ms
     */
    public int getLatency()
    {
        return this.latency.get();
    }
    
    /**
     * Destroy underlying connections between nodes. Removes connection
     * from their connection manager.
     */
    public void destroy()
    {
        this.source.getFxConnectionManager().removeConnectionWithNode(this.target);
        this.target.getFxConnectionManager().removeConnectionWithNode(this.source);
    }
    
    /**
     * Restore connection wrapper - creates underlying node connections.
     */
    public void restore()
    {
        this.setNodeConnections();
    }
    
    /**
     * Bind visible link label text to bandwidth and latency values.
     */
    private void bindLinkLabel()
    {
        this.bandwidth.bpsProperty().addListener((observable, oldValue, newValue) ->
        {
            setLinkLabel();
        });
        
        this.latency.addListener((observable, oldValue, newValue) ->
        {
            setLinkLabel();
        });
    }
    
    /**
     * Set visible link label from bandwidth and latency.
     */
    private void setLinkLabel()
    {
        this.fxNodeLink.setLinkLabel(this.bandwidth.humanReadableProperty().get()+ ", " + this.latency.get() + 
                " " + ModelNodeConnection.LATENCY_UNITS);
    }
    
    /**
     * Get wrapper connection characteristic. Characteristic is applied
     * to both underlying connections (source-target, target-source).
     * 
     * @return connection characteristic
     */
    public LineConnectionCharacteristic getConnectionCharacteristic()
    {
        return this.characteristic;
    }

    /**
     * {@inheritDoc}
     */
    @Override public List<StatePersistable> getPersistableChildren()
    {
        List<StatePersistable> l = new ArrayList<>();
        l.add(this.characteristic);
        
        return l;
    }

    /**
     * {@inheritDoc}
     */
    @Override public StatePersistableElement export(StatePersistenceLogger logger)
    {
        StatePersistableElement element = new StatePersistableElement(this.getPersistableName());
        
        element.addAttribute(new StatePersistableAttribute("bandwidth", "" + this.bandwidth.bpsProperty().get()));
        element.addAttribute(new StatePersistableAttribute("latency", "" + this.latency.get()));
        element.addAttribute(new StatePersistableAttribute("n1", "" + this.source.getNode().getNodeID()));
        element.addAttribute(new StatePersistableAttribute("n2", "" + this.target.getNode().getNodeID()));
        
        return element;
    }

    /**
     * {@inheritDoc}
     */
    @Override public void restoreState(StatePersistableElement state, StatePersistenceLogger logger, Object... args) throws InvalidPersistedStateException
    {
        if(state != null)
        {
            StatePersistableAttribute attrBw = state.getAttribute("bandwidth");
            StatePersistableAttribute attrLat = state.getAttribute("latency");
            
            if(attrBw == null || attrLat == null || !Helper.isLong(attrBw.getValue()) || 
                    !Helper.isInteger(attrLat.getValue()))
            {
                throw new InvalidPersistedStateException("Invalid attributes for connection, expected bandwidth and latency: " + state);
            }
            
            // expect one element (characteristic)
            if(state.getElements().size() != 1)
            {
                throw new InvalidPersistedStateException("Expected one child element of ConnectionCharacteristic: " + state);
            }
            
            StatePersistableElement characteristicElement = state.getElements().get(0);
            
            // @TODO this could be done inside ConnectionCharacteristic
            // since we shouldnt have to know all possible implementations
            if(characteristicElement.getName().equals(LineConnectionCharacteristic.PERSISTABLE_NAME))
            {
                LineConnectionCharacteristic newCharacteristic = new LineConnectionCharacteristic();
                newCharacteristic.restoreState(characteristicElement, logger, args);
                
                this.characteristic = newCharacteristic;
                
                // update connection characteristics
                this.sourceTargetConn.setCharasteristic(this.characteristic);
                this.targetSourceConn.setCharasteristic(this.characteristic);
                
            }
            else
            {
                throw new InvalidPersistedStateException("Unknown characteristic type of " + characteristicElement.getName() + ": " + state);
            }
            
            this.bandwidth.setBps(Long.parseLong(attrBw.getValue()));
            this.latency.set(Integer.parseInt(attrLat.getValue()));
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
