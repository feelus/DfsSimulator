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

import cz.zcu.kiv.dfs_simulator.model.SizeableObject;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.LongProperty;
import javafx.collections.ObservableList;

/**
 * Sum of sizes of all objects.
 */
public class TotalSizeBinding extends LongSumBinding<SizeableObject>
{
    /**
     * Sums size of all {@code children} objects.
     * 
     * @param children child objects
     */
    public TotalSizeBinding(ObservableList<? extends SizeableObject> children)
    {
        super(children);
    }

    /**
     * {@inheritDoc}
     */
    @Override protected void refreshBinding()
    {
        unbind(observedProperties);
        
        // load new properties
        List<LongProperty> tmp = new ArrayList<>();
        children.stream().map((x) -> x.getSize().bytesProperty()).forEach((ip) ->
                {
                    tmp.add(ip);
                });
        
        observedProperties = tmp.toArray(new LongProperty[0]);
        
        super.bind(observedProperties);
        this.invalidate();
    }
    
}
