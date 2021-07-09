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

import cz.zcu.kiv.dfs_simulator.model.ByteSpeed;
import cz.zcu.kiv.dfs_simulator.model.ByteSpeedUnits;
import cz.zcu.kiv.dfs_simulator.model.ModelNode;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Oriented connection between two instances of {@link ModelNode}.
 */
public class ModelNodeConnection
{
    /**
     * Maximum allowed connection bandwidth
     */
    public static ByteSpeed BANDWIDTH_MAX = new ByteSpeed(10, ByteSpeedUnits.GBPS);
    
    /**
     * Minimum allowed connection latency (ms)
     */
    public static int LATENCY_MIN = 0;
    /**
     * Maximum allowed connection latency (ms)
     */
    public static int LATENCY_MAX = 100000;
    
    /**
     * Textual representation of latency units
     */
    public static String LATENCY_UNITS = "ms";
    
    /**
     * Origin node
     */
    protected final ModelNode origin;
    /**
     * Target node
     */
    protected final ModelNode neighbour;
    /**
     * Connection bandwidth
     */
    protected final ByteSpeed bandwidth;
    /**
     * Connection latency (ms)
     */
    protected final IntegerProperty latency;
    
    /**
     * Connection time-throughput characteristic
     */
    private ConnectionCharacteristic characteristic;
    
    // @TODO can store RX and TX data
    
    // this could be used to solve multiple clients accessing (reading/writing)
    // through shared link, basically divide speed by linkUsageCount
    // protected volatile int linkUsageCount;
    
    /**
     * Constructs new connection originating from {@code origin} to {@code neighbour}
     * with bandwidth set by {@code bandwidth}, latency {@code latency} in ms
     * and throughput modified by {@code characteristic}.
     * 
     * @param origin originating node
     * @param neighbour target node
     * @param bandwidth connection bandwidth
     * @param latency connection latency (ms)
     * @param characteristic connection time-throughput characteristic
     */
    public ModelNodeConnection(ModelNode origin, ModelNode neighbour, ByteSpeed bandwidth, IntegerProperty latency, ConnectionCharacteristic characteristic)
    {
        this.origin = origin;
        this.neighbour = neighbour;
        this.bandwidth = bandwidth;
        this.latency = latency;
        
        this.characteristic = characteristic;
    }
    
    /**
     * Constructs new connection originating from {@code origin} to {@code neighbour}
     * with bandwidth set by {@code bandwidth}, latency {@code latency} in ms
     * and throughput modified by {@code characteristic}.
     * 
     * @param origin originating node
     * @param neighbour target node
     * @param bandwidth connection bandwidth
     * @param latency connection latency (ms)
     * @param characteristic connection time-throughput characteristic
     */
    public ModelNodeConnection(ModelNode origin, ModelNode neighbour, ByteSpeed bandwidth, int latency, ConnectionCharacteristic characteristic)
    {
        this(origin, neighbour, bandwidth, new SimpleIntegerProperty(latency), characteristic);
    }
    
    /**
     * Constructs new connection originating from {@code origin} to {@code neighbour}
     * with bandwidth set by {@code bandwidth} and latency {@code latency} in ms.
     * 
     * @param origin originating node
     * @param neighbour target node
     * @param bandwidth connection bandwidth
     * @param latency connection latency (ms)
     */
    public ModelNodeConnection(ModelNode origin, ModelNode neighbour, ByteSpeed bandwidth, IntegerProperty latency)
    {
        this(origin, neighbour, bandwidth, latency, new LineConnectionCharacteristic());
    }
    
    /**
     * Constructs new connection originating from {@code origin} to {@code neighbour}
     * with bandwidth set by {@code bandwidth} and latency {@code latency} in ms.
     * 
     * @param origin originating node
     * @param neighbour target node
     * @param bandwidth connection bandwidth
     * @param latency connection latency (ms)
     */
    public ModelNodeConnection(ModelNode origin, ModelNode neighbour, ByteSpeed bandwidth, int latency)
    {
        this(origin, neighbour, bandwidth, new SimpleIntegerProperty(latency));
    }
    
    /**
     * Get originating node.
     * 
     * @return origin node
     */
    public ModelNode getOrigin()
    {
        return this.origin;
    }
    
    /**
     * Get target node
     * 
     * @return target node
     */
    public ModelNode getNeighbour()
    {
        return this.neighbour;
    }
    
    /**
     * Get target node ID
     * 
     * @return target node ID
     */
    public String getNeighbourID()
    {
        return this.neighbour.getNodeID();
    }

    /**
     * Get maximum bandwidth.
     * 
     * @return maximum bandwidth
     */
    public ByteSpeed getMaximumBandwidth()
    {
        return this.bandwidth;
    }
    
    /**
     * Get latency in ms.
     * 
     * @return latency (ms)
     */
    public int getLatency()
    {
        return this.latency.get();
    }

    /**
     * Return writable {@link IntegerProperty} of latency in ms.
     * 
     * @return latency in ms
     */
    public IntegerProperty latencyProperty()
    {
        return this.latency;
    }
    
    /**
     * Calculates average bandwidth from interval starting at {@code sTime}
     * and ending at {@code sTime} + {@code intervalLength}.
     * 
     * @param sTime start time
     * @param intervalLength end time
     * @return average bandwidth
     */
    public ByteSpeed getAverageBandwidth(long sTime, long intervalLength)
    {
        ByteSpeed modifiedSpeed = new ByteSpeed(
                this.getMaximumBandwidth().bpsProperty().get() * this.characteristic.getAverageBandwidthModifier(sTime, intervalLength),
                ByteSpeedUnits.BPS
        );
        
        return modifiedSpeed;
    }
    
    /**
     * Get connection time-throughput characteristic.
     * 
     * @return connection characteristic
     */
    public ConnectionCharacteristic getCharacteristic()
    {
        return this.characteristic;
    }
    
    /**
     * Set connection time-throughput characteristic
     * 
     * @param charasteristic connection characteristic
     */
    public void setCharasteristic(ConnectionCharacteristic charasteristic)
    {
        this.characteristic = charasteristic;
    }
}
