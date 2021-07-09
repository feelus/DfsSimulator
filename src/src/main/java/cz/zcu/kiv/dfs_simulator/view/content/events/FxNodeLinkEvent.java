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
 * Events associated with {@code FxNodeLink}.
 */
public class FxNodeLinkEvent extends Event
{
    /**
     * Remove node link
     */
    public static final EventType<FxNodeLinkEvent> REMOVE_NODE_LINK = new EventType(ANY, "REMOVE_NODE_LINK");
    /**
     * Alter node link (open setting/context dialog)
     */
    public static final EventType<FxNodeLinkEvent> ALTER_NODE_LINK = new EventType(ANY, "ALTER_NODE_LINK");
    /**
     * Open characteristic setting dialog
     */
    public static final EventType<FxNodeLinkEvent> REQUEST_CHARACTERISTIC_DIALOG = new EventType(ANY, "REQUEST_CHARACTERISTIC_DIALOG");

    public FxNodeLinkEvent(Object source, EventTarget target, EventType<? extends Event> eventType)
    {
        super(source, target, eventType);
    }

    public FxNodeLinkEvent(EventType<? extends Event> eventType)
    {
        super(eventType);
    }
}
