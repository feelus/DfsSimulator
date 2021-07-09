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

package cz.zcu.kiv.dfs_simulator.view;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.input.DataFormat;

/**
 * Drag and drop container.
 */
public class DragContainer implements Serializable
{
    private static final long serialVersionUID = 7526471155622776147L;
    
    /**
     * Add node drag event identifier
     */
    public static final DataFormat ADD_NODE = new DataFormat("cz.zcu.kiv.dfs_simulator.view.modelGraphLayoutPane.DraggableLibraryNode.add");
    /**
     * Drag node drag event identifier
     */
    public static final DataFormat DRAG_NODE = new DataFormat("cz.zcu.kiv.dfs_simulator.view.modelGraphLayoutPane.DraggableLibraryNode.drag");
    /**
     * Add link drag event identifier
     */
    public static final DataFormat ADD_LINK = new DataFormat("cz.zcu.kiv.dfs_simulator.view.modelGraphLayoutPane.DraggableLibraryNode.NodeLink.add");
    
    /**
     * Key-value storage
     */
    private final Map<String, Object> DATA_MAP = new HashMap<>();
    
    /**
     * Add key-value pair.
     * 
     * @param key key
     * @param value value
     */
    public void addData(String key, Object value)
    {
        this.DATA_MAP.put(key, value);
    }
    
    /**
     * Get value from key-value storage.
     * 
     * @param <T> value type
     * @param key value key
     * @return found value or null
     */
    public <T> T getValue(String key)
    {
        return (T) this.DATA_MAP.get(key);
    }
}
