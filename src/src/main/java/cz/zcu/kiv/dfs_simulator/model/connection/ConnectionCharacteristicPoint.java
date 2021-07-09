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

package cz.zcu.kiv.dfs_simulator.model.connection;

import cz.zcu.kiv.dfs_simulator.persistence.InvalidPersistedStateException;
import cz.zcu.kiv.dfs_simulator.persistence.StatePersistable;
import cz.zcu.kiv.dfs_simulator.persistence.StatePersistableAttribute;
import cz.zcu.kiv.dfs_simulator.persistence.StatePersistableElement;
import cz.zcu.kiv.dfs_simulator.persistence.StatePersistenceLogger;
import cz.zcu.kiv.dfs_simulator.helpers.Helper;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

/**
 * Connection characteristic time-throughput modifier.
 */
public class ConnectionCharacteristicPoint implements StatePersistable
{
    /**
     * Persistable identificator
     */
    public static final String PERSISTABLE_NAME = "connection_characteristic_point";
    
    /**
     * X value (time)
     */
    private final DoubleProperty x;
    /**
     * Y value (modifier)
     */
    private final DoubleProperty y;
    
    /**
     * Connection characteristic time-throughput modifier with 
     * {@code x} time and {@code y} modifier.
     * 
     * @param x time
     * @param y modifier
     */
    public ConnectionCharacteristicPoint(double x, double y)
    {
        this.x = new SimpleDoubleProperty(x);
        this.y = new SimpleDoubleProperty(y);
    }

    /**
     * Returns writable {@link DoubleProperty} X value
     * 
     * @return X value
     */
    public DoubleProperty xProperty()
    {
        return this.x;
    }
    
    /**
     * Returns writable {@link DoubleProperty} Y value
     * 
     * @return Y value
     */
    public DoubleProperty yProperty()
    {
        return this.y;
    }

    /**
     * {@inheritDoc}
     */
    @Override public String getPersistableName()
    {
        return PERSISTABLE_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override public List<StatePersistable> getPersistableChildren()
    {
        return new ArrayList<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override public StatePersistableElement export(StatePersistenceLogger logger)
    {
        StatePersistableElement element = new StatePersistableElement(this.getPersistableName());
        
        element.addAttribute(new StatePersistableAttribute("x", "" + this.x.get()));
        element.addAttribute(new StatePersistableAttribute("y", "" + this.y.get()));
        
        return element;
    }

    /**
     * {@inheritDoc}
     */
    @Override public void restoreState(StatePersistableElement state, StatePersistenceLogger logger, Object... args) throws InvalidPersistedStateException
    {
        if(state != null)
        {
            StatePersistableAttribute xAttr = state.getAttribute("x");
            StatePersistableAttribute yAttr = state.getAttribute("y");
            
            if(xAttr != null && yAttr != null && Helper.isDouble(xAttr.getValue()) && Helper.isDouble(yAttr.getValue()))
            {
                this.x.set(Double.parseDouble(xAttr.getValue()));
                this.y.set(Double.parseDouble(yAttr.getValue()));
            }
            else
            {
                throw new InvalidPersistedStateException("Invalid attributes for ConnectionCharacteristicPoint, expected x and y: " + state);
            }
        }
        else
        {
            throw new InvalidPersistedStateException("State is null.");
        }
    }
}
