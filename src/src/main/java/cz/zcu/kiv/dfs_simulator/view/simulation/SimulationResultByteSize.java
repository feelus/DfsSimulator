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

import cz.zcu.kiv.dfs_simulator.model.ByteSizeUnits;

/**
 * Simulation result size unit option. Used in GUI with an additional option
 * of human readable format.
 */
public enum SimulationResultByteSize
{
    /**
     * Byte
     */
    B(ByteSizeUnits.B.toString(), ByteSizeUnits.B),
    /**
     * Kilobyte
     */
    KB(ByteSizeUnits.KB.toString(), ByteSizeUnits.KB),
    /**
     * Megabyte
     */
    MB(ByteSizeUnits.MB.toString(), ByteSizeUnits.MB),
    /**
     * Gigabyte
     */
    GB(ByteSizeUnits.GB.toString(), ByteSizeUnits.GB),
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
    private final ByteSizeUnits unit;
    
    /**
     * Result size unit option.
     * 
     * @param name unit name
     * @param unit backing unit
     */
    private SimulationResultByteSize(String name, ByteSizeUnits unit)
    {
        this.name = name;
        this.unit = unit;
    }

    /**
     * Get unit.
     * 
     * @return unit
     */
    public ByteSizeUnits getUnit()
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
