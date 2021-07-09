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
 * Highlightable object.
 */
public interface Highlightable
{
    /**
     * Highlight object.
     */
    public void highlight();
    /**
     * Remove highlighting from object.
     */
    public void removeHighlight();
}
