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
 * Node type.
 */
public enum NodeType
{
    /**
     * Server node
     */
    SERVER("Server"),
    
    /**
     * Client node
     */
    CLIENT("Client");
    
    /**
     * Textual representation
     */
    private final String name;
    
    /**
     * Node type constructor with given {@code name} textual representation.
     * 
     * @param name node type text representation
     */
    private NodeType(String name)
    {
        this.name = name;
    }
    
    /**
     * Node type textual representation.
     * 
     * @return textual representation
     */
    @Override public String toString()
    {
        return this.name;
    }
}
