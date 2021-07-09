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

package cz.zcu.kiv.dfs_simulator.view.content.events;

import javafx.event.Event;
import static javafx.event.Event.ANY;
import javafx.event.EventTarget;
import javafx.event.EventType;

/**
 * Events related to {@code FxModelNode} objects.
 */
public class FxNodeEvent extends Event
{
    /**
     * Remove node from layout pane
     */
    public static final EventType<FxNodeEvent> REMOVE_CONTENT_NODE = new EventType(ANY, "REMOVE_CONTENT_NODE");
    /**
     * Duplicate node on layout pane
     */
    public static final EventType<FxNodeEvent> DUPLICATE_CONTENT_NODE = new EventType(ANY, "DUPLICATE_CONTENT_NODE");
    
    /**
     * Focus node (hard select)
     */
    public static final EventType<FxNodeEvent> NODE_SELECTED = new EventType(ANY, "NODE_SELECTED");
    
    /**
     * Display node context (setting) dialog
     */
    public static final EventType<FxNodeEvent> NODE_CONTEXT_DIALOG_REQUESTED = new EventType(ANY, "NODE_CONTEXT_DIALOG_REQUESTED");
    
    public FxNodeEvent(Object source, EventTarget target, EventType<? extends Event> eventType)
    {
        super(source, target, eventType);
    }

    public FxNodeEvent(EventType<? extends Event> eventType)
    {
        super(eventType);
    }
}
