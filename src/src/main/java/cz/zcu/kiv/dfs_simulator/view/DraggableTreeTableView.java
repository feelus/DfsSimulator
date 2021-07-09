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

import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

/**
 * Draggable tree table view.
 * 
 * Based on: http://programmingtipsandtraps.blogspot.cz/2015/10/drag-and-drop-in-treetableview-with.html
 */
public abstract class DraggableTreeTableView<T> extends TreeTableView<T>
{
    /**
     * Serialized MIME type
     */
    private static final DataFormat SERIALIZED_MIME_TYPE = new DataFormat("application/x-java-serialized-object");
    
    /**
     * Draggable tree table view.
     */
    public DraggableTreeTableView()
    {
        this.init();
    }
    
    /**
     * Init table
     */
    private void init()
    {
        setRowFactory(this::rowFactory);
    }
    
    /**
     * Drag and drop enable {@link TreeTableRow} factory.
     * 
     * @param view table view
     * @return table row
     */
    protected TreeTableRow<T> rowFactory(TreeTableView<T> view)
    {
        TreeTableRow<T> row = new TreeTableRow();

        row.setOnDragDetected(event ->
        {
            if(!row.isEmpty())
            {
                Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
                db.setDragView(row.snapshot(null, null));

                ClipboardContent cc = new ClipboardContent();
                cc.put(SERIALIZED_MIME_TYPE, row.getIndex());

                db.setContent(cc);
                event.consume();
            }
        });

        row.setOnDragOver(event ->
        {
            Dragboard db = event.getDragboard();

            if(dropAcceptable(db, row))
            {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                event.consume();
            }
        });

        row.setOnDragDropped(event ->
        {
            Dragboard db = event.getDragboard();

            if(this.dropAcceptable(db, row))
            {
                int index = (Integer) db.getContent(SERIALIZED_MIME_TYPE);

                // remove from previous parent
                final TreeItem<T> item = getTreeItem(index);
                final TreeItem<T> oldParent = item.getParent();
                final TreeItem<T> newParent = getDropTarget(row);
                
                DragRowResult result = onRowDragDropped(item.getValue(), 
                        oldParent.getValue(), newParent.getValue());
                
                switch(result)
                {
                    case ACCEPTED:
                        onDropAccepted(item, oldParent, newParent);
                        break;
                    case MERGED:
                        onDropMerged(item, oldParent, newParent);
                        break;
                    default:
                    case REFUSED:
                        break;
                }
                
                event.setDropCompleted(true);
                event.consume();
            }
        });

        return row;
    }
    
    /**
     * Handle drop accepted.
     * 
     * @param item dropped item
     * @param oldParent old item parent
     * @param newParent new item parent
     */
    protected void onDropAccepted(TreeItem<T> item, TreeItem<T> oldParent, TreeItem<T> newParent)
    {
        // update tree 
        oldParent.getChildren().remove(item);
        newParent.getChildren().add(item);

        // expand parent
        newParent.setExpanded(true);

        // select dropped item
        getSelectionModel().select(item);

        // force table graphics update
        getColumns().get(0).setVisible(false);
        getColumns().get(0).setVisible(true);
    }
    
    /**
     * Handle drop merged.
     * 
     * @param item dropped item
     * @param oldParent old item parent
     * @param newParent new item parent
     */
    protected void onDropMerged(TreeItem<T> item, TreeItem<T> oldParent, TreeItem<T> newParent)
    {
        oldParent.getChildren().remove(item);
        
        newParent.setExpanded(true);
        
        // force table graphics update
        getColumns().get(0).setVisible(false);
        getColumns().get(0).setVisible(true);
    }
    
    /**
     * Checks, whether row can be dropped into row given by context of {@code db}.
     * 
     * @param db dragboard context
     * @param row dragged row
     * @return true if can be dropped, false otherwise
     */
    protected boolean dropAcceptable(Dragboard db, TreeTableRow<T> row)
    {
        boolean r = false;
        
        if(db.hasContent(SERIALIZED_MIME_TYPE))
        {
            int index = (Integer) db.getContent(SERIALIZED_MIME_TYPE);
            
            // cant drop into same row
            if(row.getIndex() != index)
            {
                TreeItem target = this.getDropTarget(row);
                TreeItem item = getTreeItem(index);
                
                r = !this.isRowParent(item, target);
            }
        }
        
        return r;
    }
    
    /**
     * Get {@code TreeItem} that {@code row} has been dropped into.
     * 
     * @param row dropped row
     * @return drop destination
     */
    private TreeItem getDropTarget(TreeTableRow<T> row)
    {
        TreeItem target = getRoot();
        
        if(!row.isEmpty())
        {
            target = row.getTreeItem();
        }
        
        return target;
    }
    
    /**
     * Checks if {@code parent} is predecessor of {@code child}.
     * 
     * @param parent parent
     * @param child child
     * @return whether {@code parent} is predecessor of {@code child}
     */
    private boolean isRowParent(TreeItem parent, TreeItem child)
    {
        boolean r = false;
        
        while(!r && child != null)
        {
            r = child.getParent() == parent;
            child = child.getParent();
        }
        
        return r;
    }
    
    /**
     * Handle drop of {@code item} from {@code oldParent} into {@code newParent}.
     * 
     * @param item dropped item
     * @param oldParent old parent
     * @param newParent new parent
     * @return drop result
     */
    abstract protected DragRowResult onRowDragDropped(T item, T oldParent, T newParent);
}
