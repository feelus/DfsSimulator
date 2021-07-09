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
 * Object can be part of a multi-selection.
 */
public interface MultiSelectable
{
    /**
     * Set object's multi-selectable state.
     * 
     * @param state multi-selectable state
     */
    void setMultiSelectableState(boolean state);
    
    /**
     * Remove selected node from parent (selected nodes were deleted).
     */
    void removeFromParent();
}
