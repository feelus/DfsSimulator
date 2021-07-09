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

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test {@link LineConnectionCharacteristic}.
 */
public class LineConnectionCharacteristicTest
{
    private LineConnectionCharacteristic characteristic;
    
    @Before public void setUp()
    {
        this.characteristic = new LineConnectionCharacteristic();
        this.characteristic.setPeriodInterval(60000);
        this.characteristic.getDiscretePoints().clear();
        
        // 0 sec
        this.characteristic.getDiscretePoints().add(new ConnectionCharacteristicPoint(0, 0.5));
        // 12 sec
        this.characteristic.getDiscretePoints().add(new ConnectionCharacteristicPoint(0.2, 0.2));
        // 24 sec
        this.characteristic.getDiscretePoints().add(new ConnectionCharacteristicPoint(0.4, 0.8));
        // 36 sec
        this.characteristic.getDiscretePoints().add(new ConnectionCharacteristicPoint(0.6, 0.8));
        // 48 sec
        this.characteristic.getDiscretePoints().add(new ConnectionCharacteristicPoint(0.8, 0.1));
        // 60 sec
        this.characteristic.getDiscretePoints().add(new ConnectionCharacteristicPoint(1, 1));
    }    
    
    /**
     * Test method {@link LineConnectionCharacteristic#getAverageBandwidthModifier(long, long)}.
     */
    @Test public void getAverageBandwidthModifierTest()
    {
        // get actual modifier at time 0 with interval length 0
        double mod1 = this.characteristic.getAverageBandwidthModifier(0, 0);
        assertEquals(0.5, mod1, 0);
        
        // get average modifier from time 0 to time 0.2 (0 - 12 sec)
        double mod2 = this.characteristic.getAverageBandwidthModifier(0, 12000);
        // we expect it to be (0.5 + 0.2) / 2
        assertEquals(0.35, mod2, 0);
        
        // get average accross whole interval
        double mod3 = this.characteristic.getAverageBandwidthModifier(0, 60000);
        assertEquals(0.53, mod3, 0.01);
    }
    
    /**
     * Test method {@link LineConnectionCharacteristic#setPeriodInterval(long)}.
     */
    @Test public void setPeriodIntervalTest()
    {
        this.characteristic.setPeriodInterval(10000);
        assertEquals(10000, this.characteristic.periodIntervalProperty().get());
    }
    
    
}
