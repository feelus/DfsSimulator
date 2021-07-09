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

package cz.zcu.kiv.dfs_simulator.model;

/**
 * Representation of speed multiples.
 */
public enum ByteSpeedUnits
{
    /**
     * Bytes per second
     */
    BPS("B/s", 1L),
    /**
     * Kilobytes per second
     */
    KBPS("kB/s", 1000L),
    /**
     * Megabytes per second
     */
    MBPS("mB/s", 1000L * 1000L),
    /**
     * Gigabytes per second
     */
    GBPS("gB/s", 1000L * 1000L * 1000L);
    
    /**
     * Text representation of prefix.
     */
    private final String name;
    /**
     * Prefix multiplicator.
     */
    private final long mu;
    
    /**
     * Constructs a size prefix with text representation {@code name}
     * and prefix multiplicator {@code mu}.
     * 
     * @param name text representation
     * @param mu conversion multiplicator
     */
    private ByteSpeedUnits(String name, long mu)
    {
        this.name = name;
        this.mu = mu;
    }
    
    /**
     * Returns prefix multiplicator.
     * 
     * @return prefix multiplicator
     */
    public long getMu()
    {
        return this.mu;
    }
    
    /**
     * Text representation of prefix.
     * 
     * @return text representation
     */
    @Override public String toString()
    {
        return this.name;
    }
}
