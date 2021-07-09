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

package cz.zcu.kiv.dfs_simulator.model.storage.filesystem;

import javafx.beans.binding.LongBinding;
import javafx.beans.property.LongProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

public abstract class LongSumBinding<T> extends LongBinding
{
    /**
     * List of children
     */
    protected final ObservableList<? extends T> children;
    /**
     * List change listener
     */
    protected final ListChangeListener<T> boundListChangeListener;
    
    /**
     * Array of observed properties
     */
    protected LongProperty[] observedProperties = {};
    
    /**
     * Construct a sum binding of selected properties.
     * 
     * @param children list that will be observed
     */
    public LongSumBinding(ObservableList<? extends T> children)
    {
        this.children = children;
        
        this.boundListChangeListener = (ListChangeListener.Change<? extends T> change) -> 
        {
                refreshBinding();
        };
        this.children.addListener(this.boundListChangeListener);
        
        this.onAfterCreate();
    }
    
    /**
     * Triggered when observed list changes.
     * 
     * @param change change listener
     */
    protected void onChange(ListChangeListener.Change<? extends T> change) {}
    
    /**
     * Initiates binding
     */
    protected void onAfterCreate()
    {
        this.refreshBinding();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override protected long computeValue()
    {
        long i = 0;
        
        for(LongProperty ip : this.observedProperties)
        {
            i += ip.get();
        }
        
        return i;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public void dispose()
    {
        this.children.removeListener(this.boundListChangeListener);
        unbind(observedProperties);
    }
    
    /**
     * Refresh bound properties.
     */
    abstract protected void refreshBinding();
}
