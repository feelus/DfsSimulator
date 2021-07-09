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

import cz.zcu.kiv.dfs_simulator.persistence.StatePersistable;
import javafx.beans.property.LongProperty;

/**
 * Connection time-throughput characteristic.
 */
public interface ConnectionCharacteristic extends StatePersistable
{
    /**
     * Returns average modifier from interval beggining at {@code sTime}
     * and ending at {@code sTime} + {@code intervalLength}.
     * 
     * @param sTime simulation time
     * @param intervalLength interval length
     * @return average modifier
     */
    public double getAverageBandwidthModifier(long sTime, long intervalLength);
    
    /**
     * Set characteristic period length.
     * 
     * @param time period length
     */
    public void setPeriodInterval(long time);
    
    /**
     * Returns characteristic writable {@link LongProperty} period length.
     * 
     * @return period length
     */
    public LongProperty periodIntervalProperty();
    
    /**
     * Characteristic minimum possible Y (throughput modifier) value.
     * 
     * @return minimum Y value
     */
    public double getYLowerBound();
    /**
     * Characteristic maximum possible Y (throughput modifier) value.
     * 
     * @return maximum Y value
     */
    public double getYUpperBound();
    
    /**
     * Characteristic minimum possible X (time) value.
     * 
     * @return minimum Y value
     */
    public double getXLowerBound();
    /**
     * Characteristic maximum possible X (time) value.
     * 
     * @return maximum X value
     */
    public double getXUpperBound();
}
