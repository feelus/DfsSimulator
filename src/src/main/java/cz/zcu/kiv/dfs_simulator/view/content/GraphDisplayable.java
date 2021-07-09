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

package cz.zcu.kiv.dfs_simulator.view.content;

import cz.zcu.kiv.dfs_simulator.persistence.StatePersistable;
import cz.zcu.kiv.dfs_simulator.view.Labelable;
import cz.zcu.kiv.dfs_simulator.view.content.selection.Highlightable;
import cz.zcu.kiv.dfs_simulator.view.content.selection.MultiSelectableDraggable;
import cz.zcu.kiv.dfs_simulator.view.content.selection.Selectable;
import java.io.Serializable;

/**
 * Object can be displayed on a {@link ModelGraphLayoutPane}.
 */
public interface GraphDisplayable 
        extends Serializable, Highlightable, 
        Selectable, MultiSelectableDraggable, 
        StatePersistable, Labelable
{
    /**
     * Called after object has been displayed.
     */
    public void onGraphDisplayed();
    /**
     * Called after object has been removed from display.
     */
    public void onAfterRemovedFromGraph();
}
