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
 * Event associated with multi-selection (multiple nodes).
 */
public class MultiNodeSelectionEvent extends Event
{
    /**
     * Multi-selection mouse X offset
     */
    private final double dragOffsetX;
    /**
     * Multi-selection mouse Y offset
     */
    private final double dragOffsetY;

    /**
     * Drag multi-selection event
     */
    public static final EventType<MultiNodeSelectionEvent> DRAG_SELECTION = new EventType(ANY, "DRAG_SELECTION");
    
    /**
     * Multi-selection event.
     * 
     * @param source source
     * @param target target
     * @param eventType type
     * @param dragOffsetX mouse X offset
     * @param dragOffsetY mouse Y offset
     */
    public MultiNodeSelectionEvent(Object source, EventTarget target, EventType<? extends Event> eventType, double dragOffsetX, double dragOffsetY)
    {
        super(source, target, eventType);
        
        this.dragOffsetX = dragOffsetX;
        this.dragOffsetY = dragOffsetY;
    }

    public MultiNodeSelectionEvent(EventType<? extends Event> eventType, double dragOffsetX, double dragOffsetY)
    {
        super(eventType);
        
        this.dragOffsetX = dragOffsetX;
        this.dragOffsetY = dragOffsetY;
    }

    /**
     * Get mouse drag X offset.
     * 
     * @return X offset
     */
    public double getDragOffsetX()
    {
        return dragOffsetX;
    }

    /**
     * Get mouse drag Y offset.
     * 
     * @return Y offset
     */
    public double getDragOffsetY()
    {
        return dragOffsetY;
    }
}
