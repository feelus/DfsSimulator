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

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.LongProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

/**
 * Binding that calculates the sum of mount size of all children.
 */
public class MountChildSizeBinding extends LongSumBinding<FileSystemObject>
{
    /**
     * Listener listening for changes of mount device
     */
    private final ChangeListener<Boolean> inheritedMountDeviceChangedListener;

    /**
     * Construct sum binding of mount size.
     * 
     * @param children observed list
     */
    public MountChildSizeBinding(ObservableList<FileSystemObject> children)
    {
        super(children);
        
        this.inheritedMountDeviceChangedListener = 
                (ChangeListener) (ObservableValue observable, Object oldValue, Object newValue) ->
                {
                    refreshBinding();
                };
    }
    
    /**
     * {@inheritDoc}
     */
    @Override protected void refreshBinding()
    {
        unbind(observedProperties);
        
        // load new properties
        List<LongProperty> tmp = new ArrayList<>();
        
        // add listener to each child
        children.stream()
                .forEach(c ->
                        {
                            c.inheritedMountDeviceProperty().addListener(inheritedMountDeviceChangedListener);
                        });
        
        children.stream()
                .filter(c -> c.inheritedMountDeviceProperty().get())
                .forEach(x -> 
                        {
                            if (x instanceof FsDirectory)
                            {
                                tmp.add(x.getMountSize().bytesProperty());
                            }
                            else
                            {
                                tmp.add(x.getSize().bytesProperty());
                            }
                });
        
        observedProperties = tmp.toArray(new LongProperty[0]);
        
        super.bind(observedProperties);
        this.invalidate();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override protected void onChange(ListChangeListener.Change<? extends FileSystemObject> change)
    {
        while(change.next())
        {
            if(change.wasRemoved())
            {
                // remove listeners from removed items
                change.getRemoved().stream().forEach(o -> 
                {
                    o.inheritedMountDeviceProperty().removeListener(inheritedMountDeviceChangedListener);
                });
            }
        }
    }
    
}
