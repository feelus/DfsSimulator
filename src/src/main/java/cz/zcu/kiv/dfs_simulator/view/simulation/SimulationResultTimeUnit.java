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

import java.util.concurrent.TimeUnit;

/**
 * Simulation result time unit option. Used in GUI with an additional option
 * of human readable format.
 */
public enum SimulationResultTimeUnit 
{
    /**
     * Millisecond
     */
    MILLISECOND("Milisecond", "ms", TimeUnit.MILLISECONDS),
    /**
     * Seconds
     */
    SECOND("Second", "sec", TimeUnit.SECONDS),
    /**
     * Minutes
     */
    MINUTE("Minute", "min", TimeUnit.MINUTES),
    /**
     * Hours
     */
    HOUR("Hour", "hr", TimeUnit.HOURS),
    /**
     * Human readable format
     */
    HUMAN_READABLE("Human readable", "", null);
    
    /**
     * Unit name
     */
    private final String name;
    /**
     * Short unit name
     */
    private final String shortName;
    /**
     * Backing time unit
     */
    private final TimeUnit timeUnit;
    
    /**
     * Simulation result time unit.
     * 
     * @param name unit name
     * @param shortName short unit name
     * @param timeUnit backing time unit
     */
    private SimulationResultTimeUnit(String name, String shortName, TimeUnit timeUnit)
    {
        this.name = name;
        this.shortName = shortName;
        this.timeUnit = timeUnit;
    }
    
    /**
     * Get time unit.
     * 
     * @return unit
     */
    public TimeUnit getTimeUnit()
    {
        return this.timeUnit;
    }
    
    /**
     * Get short unit name.
     * 
     * @return short name
     */
    public String getShortName()
    {
        return this.shortName;
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
