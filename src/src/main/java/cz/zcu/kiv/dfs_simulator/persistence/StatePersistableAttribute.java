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

package cz.zcu.kiv.dfs_simulator.persistence;

/**
 * Persisted attribute (key-value).
 */
public class StatePersistableAttribute
{
    /**
     * Attribute name
     */
    protected final String name;
    /**
     * Value
     */
    protected final String value;
    
    /**
     * Persisted attribute (key-value pair).
     * 
     * @param name attribute name
     * @param value attribute value
     */
    public StatePersistableAttribute(String name, String value)
    {
        this.name = name;
        this.value = value;
    }

    /**
     * Returns attribute name.
     * 
     * @return name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Returns attribute value.
     * 
     * @return value
     */
    public String getValue()
    {
        return value;
    }
    
    /**
     * Textual representation of attribute.
     * 
     * @return string representation
     */
    @Override public String toString()
    {
        return "Persistable attribute [name=" + this.name + ", value=" + this.value + "]";
    }
}
