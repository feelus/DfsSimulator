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

package cz.zcu.kiv.dfs_simulator.view.simulation;

import cz.zcu.kiv.dfs_simulator.model.ByteSpeedUnits;

/**
 * Simulation result speed unit option. Used in GUI with an additional option
 * of human readable format.
 */
public enum SimulationResultByteSpeed
{
    /**
     * Byte per second
     */
    BPS(ByteSpeedUnits.BPS.toString(), ByteSpeedUnits.BPS),
    /**
     * Kilobyte per second
     */
    KBPS(ByteSpeedUnits.KBPS.toString(), ByteSpeedUnits.KBPS),
    /**
     * Megabyte per second
     */
    MBPS(ByteSpeedUnits.MBPS.toString(), ByteSpeedUnits.MBPS),
    /**
     * Gigabyte per second
     */
    GBPS(ByteSpeedUnits.GBPS.toString(), ByteSpeedUnits.GBPS),
    /**
     * Human readable format
     */
    HUMAN_READABLE("Human readable", null);
    
    /**
     * Unit name
     */
    private final String name;
    /**
     * Backing unit
     */
    private final ByteSpeedUnits unit;
    
    /**
     * Result speed unit option.
     * 
     * @param name unit name
     * @param unit backing unit
     */
    private SimulationResultByteSpeed(String name, ByteSpeedUnits unit)
    {
        this.name = name;
        this.unit = unit;
    }
    
    /**
     * Get unit.
     * 
     * @return unit
     */
    public ByteSpeedUnits getUnit()
    {
        return unit;
    }
    
    /**
     * Convert unit to text - it's name.
     * 
     * @return string representation
     */
    @Override public String toString()
    {
        return this.name;
    }
}
