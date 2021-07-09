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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Container for selecting multiple {@link MultiSelectable} objects.
 */
public class MultiNodeSelectionContainer
{
    /**
     * Selected objects
     */
    private final Set<MultiSelectable> selection = new HashSet<>();
    
    /**
     * Add node to selection.
     * 
     * @param node node
     */
    public void addNode(MultiSelectable node)
    {
        this.selection.add(node);
    }
    
    /**
     * Remove node from selection.
     * 
     * @param node node
     */
    public void removeNode(MultiSelectable node)
    {
        this.selection.remove(node);
    }
    
    /**
     * Clear selection
     */
    public void clear()
    {
        this.selection.clear();
    }
    
    /**
     * Checks, whether node {@code node} is part of this selection.
     * 
     * @param node node
     * @return true if node is in selection, false otherwise
     */
    public boolean containtsNode(MultiSelectable node)
    {
        return this.selection.contains(node);
    }
    
    /**
     * Get selected nodes.
     * 
     * @return selected nodes
     */
    public Set<MultiSelectable> getSelection()
    {
        return this.selection;
    }
    
    /**
     * Get number of selected nodes.
     * 
     * @return number of selected nodes
     */
    public int size()
    {
        return this.selection.size();
    }
    
    /**
     * Select node.
     * 
     * @param node node
     */
    public void selectNode(MultiSelectable node)
    {
        this.handleNodeSelect(node);
        this.addNode(node);
    }
    
    /**
     * Handles adding of node {@code node} to selection. Checks if node
     * is {@link Highlightable} and if so, highlights node. Notifies
     * node that its part of multi-selection.
     * 
     * @param node selected node
     */
    private void handleNodeSelect(MultiSelectable node)
    {
        if(node instanceof Highlightable)
        {
            ((Highlightable) node).highlight();
        }
        
        node.setMultiSelectableState(true);
    }
    
    /**
     * Deselect node.
     * 
     * @param node node
     */
    public void deselectNode(MultiSelectable node)
    {
        this.handleNodeDeselect(node);
        this.removeNode(node);
    }
    
    /**
     * Handles removal of node {@code node} from selection. Checks if node
     * implements {@link Highlightable} and if so, it's highlight state is set
     * to false. Node is informed that it is no longer part of selection.
     * 
     * @param node node
     */
    private void handleNodeDeselect(MultiSelectable node)
    {
        if (node instanceof Highlightable)
        {
            ((Highlightable) node).removeHighlight();
        }
        
        node.setMultiSelectableState(false);
    }
    
    /**
     * Deselect all nodes in selection.
     */
    public void deselectAll()
    {
        Iterator<MultiSelectable> it = this.selection.iterator();
        
        while(it.hasNext())
        {
            this.handleNodeDeselect(it.next());
            
            it.remove();
        }
    }
}
