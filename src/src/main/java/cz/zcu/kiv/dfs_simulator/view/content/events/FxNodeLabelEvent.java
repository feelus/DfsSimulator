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

import cz.zcu.kiv.dfs_simulator.view.content.FxModelNodeLabel;
import javafx.event.Event;
import static javafx.event.Event.ANY;
import javafx.event.EventTarget;
import javafx.event.EventType;

/**
 * Events related to {@link FxModelNodeLabel}.
 */
public class FxNodeLabelEvent extends Event
{
    /**
     * Display label event
     */
    public static final EventType<FxNodeLabelEvent> DISPLAY_LABEL = new EventType(ANY, "DISPLAY_LABEL");
    /**
     * Remove label event
     */
    public static final EventType<FxNodeLabelEvent> REMOVE_LABEL = new EventType(ANY, "REMOVE_LABEL");
    
    /**
     * Associated label
     */
    private final FxModelNodeLabel label;

    public FxNodeLabelEvent(Object source, EventTarget target, EventType<? extends Event> eventType, FxModelNodeLabel label)
    {
        super(source, target, eventType);
        this.label = label;
    }

    public FxNodeLabelEvent(EventType<? extends Event> eventType, FxModelNodeLabel label)
    {
        super(eventType);
        this.label = label;
    }
    
    /**
     * Get associated label.
     * 
     * @return label
     */
    public FxModelNodeLabel getLabel()
    {
        return this.label;
    }
}
