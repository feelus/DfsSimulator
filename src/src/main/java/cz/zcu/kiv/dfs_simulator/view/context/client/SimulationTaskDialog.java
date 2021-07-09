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

package cz.zcu.kiv.dfs_simulator.view.context.client;

import cz.zcu.kiv.dfs_simulator.model.ModelServerNode;
import cz.zcu.kiv.dfs_simulator.simulation.SimulationTask;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FileSystemObject;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FsDirectory;
import cz.zcu.kiv.dfs_simulator.view.BaseInputDialog;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Add new simulation task dialog.
 */
public abstract class SimulationTaskDialog extends BaseInputDialog
{
    
    /**
     * Simulation tasks
     */
    protected final List<SimulationTask> tasks = new ArrayList<>();
    
    /**
     * Directory icon - for file structure table
     */
    protected final Image dirIconImage;
    /**
     * File icon - for file structure table
     */
    protected final Image fileIconImage;
    
    /**
     * Simulation task dialog.
     * 
     * @param schemaURL dialog FXML schema
     */
    public SimulationTaskDialog(URL schemaURL)
    {
        super(schemaURL);
        
        this.dirIconImage = new Image(getClass().getClassLoader().getResourceAsStream("img/fs_dir.png"));
        this.fileIconImage = new Image(getClass().getClassLoader().getResourceAsStream("img/fs_file.png"));
    }
    
    /**
     * Build file structure from all reachable servers.
     * 
     * @param reachable reachable servers
     */
    public void buildReachableTree(List<ModelServerNode> reachable)
    {
        Map<String, TreeItem<FileSystemObject>> existingTreeItems = new HashMap<>();
        
        for(ModelServerNode serverNode : reachable)
        {
            this.addDistinctFileSystemObjects(serverNode.getRootDir(), existingTreeItems);
        }
    }
    
    /**
     * Add fs object to reachable tree structure.
     * 
     * @param object object
     * @param objectStructureTable fs table
     * @param existingTreeItems existing items in fs table
     */
    protected void addTreeItem(FileSystemObject object, TreeTableView<FileSystemObject> objectStructureTable, Map<String, TreeItem<FileSystemObject>> existingTreeItems)
    {
        if(!existingTreeItems.containsKey(object.getFullPath()))
        {
            // adding root
            if(object.getParent() == null)
            {
                TreeItem<FileSystemObject> newTreeItem = createTreeItem(object);
                
                newTreeItem.setExpanded(true);
                
                objectStructureTable.setRoot(newTreeItem);
                existingTreeItems.put(object.getFullPath(), newTreeItem);
            }
            else
            {
                TreeItem<FileSystemObject> parent = existingTreeItems.get(object.getParent().getFullPath());
                
                // map should always have this object's parent since this method
                // is called recursively from root directory
                if(parent != null)
                {
                    TreeItem<FileSystemObject> newTreeItem = createTreeItem(object);
                    
                    parent.getChildren().add(newTreeItem);
                    parent.setExpanded(true);
                    
                    existingTreeItems.put(object.getFullPath(), newTreeItem);
                }
            }
        }
    }
    
    /**
     * Create tree item (available tree structure item) for given object {@code object}.
     * 
     * @param object object
     * @return tree item
     */
    protected TreeItem<FileSystemObject> createTreeItem(FileSystemObject object)
    {
        ImageView treeItemView = new ImageView((object instanceof FsDirectory) ? this.dirIconImage : this.fileIconImage);
        TreeItem<FileSystemObject> treeItem = new TreeItem<>(object, treeItemView);
        
        return treeItem;
    }
    
    /**
     * Get simulation tasks.
     * 
     * @return tasks
     */
    public List<SimulationTask> getTasks()
    {
        return this.tasks;
    }
    
    /**
     * Build simulation tasks from selected file structure objects.
     * 
     * @param selected selected objects
     */
    protected void buildSimulationTasks(List<TreeItem<FileSystemObject>> selected)
    {
        for(TreeItem<FileSystemObject> treeItem : selected)
        {
            this.addSimulationTask(treeItem);
        }
    }
    
    /**
     * Checks, whether item {@code treeItem} has already selected parent - item
     * will be added via it's parent.
     * 
     * @param treeItem item
     * @param objectStructureTable structure table
     * @param isParent whether this item is parent
     * @return whether item has selected parent
     */
    protected boolean hasSelectedParent(TreeItem<FileSystemObject> treeItem, TreeTableView<FileSystemObject> objectStructureTable, boolean isParent)
    {
        int index = objectStructureTable.getRow(treeItem);
        
        // either direct parent is selected
        if(isParent && objectStructureTable.getSelectionModel().isSelected(index))
        {
            return true;
        }
        
        // or any parent up the tree
        if(treeItem.getParent() != null)
        {
            return hasSelectedParent(treeItem.getParent(), objectStructureTable, true);
        }
        
        return false;
    }
    
    /**
     * Add new simulation task from {@code treeItem}.
     * 
     * @param treeItem item
     */
    protected abstract void addSimulationTask(TreeItem<FileSystemObject> treeItem);
    /**
     * Add only distinct FS objects from tree into simulation tasks (plan).
     * 
     * @param root server root directory
     * @param existingTreeItems existing items
     */
    protected abstract void addDistinctFileSystemObjects(FsDirectory root, Map<String, TreeItem<FileSystemObject>> existingTreeItems);
    
}
