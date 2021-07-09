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

package cz.zcu.kiv.dfs_simulator.helpers;

/**
 * Simple key-value container.
 * 
 * @param <T> key type
 * @param <U> value type
 */
public class Pair<T, U>
{
    /**
     * Key
     */
    public final T first;
    /**
     * Value
     */
    public final U second;

    /**
     * Construct key-value container.
     * 
     * @param first key
     * @param second value
     */
    public Pair(T first, U second)
    {
        this.first = first;
        this.second = second;
    }
}
