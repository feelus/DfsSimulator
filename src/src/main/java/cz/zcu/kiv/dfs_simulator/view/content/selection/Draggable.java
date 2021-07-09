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


package cz.zcu.kiv.dfs_simulator.view.content.selection;

/**
 * Draggable object.
 */
public interface Draggable
{
    /**
     * Checks whether object can be dragged from its current X coordinate
     * to coordinate X + {@code offsetX}.
     * 
     * @param offsetX X offset
     * @return true if object can be dragged, false otherwise
     */
    boolean canOffsetNodeX(double offsetX);
    /**
     * Checks whether object can be dragged from its current Y coordinate
     * to coordinate Y + {@code offsetY}.
     * 
     * @param offsetY Y offset
     * @return true if object can be dragged, false otherwise
     */
    boolean canOffsetNodeY(double offsetY);
    
    /**
     * Offset node from it's current X coordinate to new coordinate X + {@code offsetX}
     * 
     * @param offsetX X offset
     */
    void offsetNodeX(double offsetX);
    /**
     * Offset node from it's current Y coordinate to new coordinate Y + {@code offsetY}
     * 
     * @param offsetY Y offset
     */
    void offsetNodeY(double offsetY);
}
