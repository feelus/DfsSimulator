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

package cz.zcu.kiv.dfs_simulator.view.content;

import cz.zcu.kiv.dfs_simulator.persistence.InvalidPersistedStateException;
import cz.zcu.kiv.dfs_simulator.persistence.StatePersistable;
import cz.zcu.kiv.dfs_simulator.persistence.StatePersistableElement;
import cz.zcu.kiv.dfs_simulator.persistence.StatePersistenceLogger;
import cz.zcu.kiv.dfs_simulator.helpers.FxHelper;
import cz.zcu.kiv.dfs_simulator.model.ByteSpeed;
import cz.zcu.kiv.dfs_simulator.model.ModelNodeRegistry;
import cz.zcu.kiv.dfs_simulator.model.connection.ModelNodeConnection;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FsGlobalObjectRegistry;
import cz.zcu.kiv.dfs_simulator.view.DragContainer;
import cz.zcu.kiv.dfs_simulator.view.content.events.FxNodeEvent;
import cz.zcu.kiv.dfs_simulator.view.content.events.FxNodeLabelEvent;
import cz.zcu.kiv.dfs_simulator.view.content.events.FxNodeLinkEvent;
import cz.zcu.kiv.dfs_simulator.view.content.connection.BezierFxNodeLink;
import cz.zcu.kiv.dfs_simulator.view.content.connection.LineFxNodeLink;
import cz.zcu.kiv.dfs_simulator.view.content.selection.MultiNodeSelectionContainer;
import cz.zcu.kiv.dfs_simulator.view.content.selection.RubberBandSelection;
import cz.zcu.kiv.dfs_simulator.view.content.selection.Selectable;
import cz.zcu.kiv.dfs_simulator.view.content.connection.FxNodeConnectionWrapper;
import cz.zcu.kiv.dfs_simulator.view.content.connection.FxNodeConnectionWrapperManager;
import cz.zcu.kiv.dfs_simulator.view.content.connection.FxNodeLink;
import cz.zcu.kiv.dfs_simulator.view.content.connection.FxNodeLinkCharacteristicDialog;
import cz.zcu.kiv.dfs_simulator.view.content.connection.FxNodeLinkDialog;
import cz.zcu.kiv.dfs_simulator.view.toolbar.LibraryNode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;

/**
 * Class representing the main content pane that has all the permanent nodes 
 * of type {@code DraggableContentNode}.
 */
public class ModelGraphLayoutPane extends ScrollPane implements StatePersistable
{
    /**
     * Key for {@code DragContainer} storage of {@code DragEvent} that has been
     * invoked after dragging a {@code DraggableLibraryNode} onto the content pane.
     */
    public static final String DROP_NODE_EVENT_KEY = "create_event";
    
    /**
     * Persistable identifier
     */
    protected static final String PERSISTABLE_NAME = "content_pane";
    
    /**
     * Handler for drag over operations
     */
    private EventHandler<DragEvent> contentDragOver;
    /**
     * Handler for drag dropped operations
     */
    private EventHandler<DragEvent> contentDragDropped;
    /**
     * Handler for drag done operations
     */
    private EventHandler<DragEvent> contentDragDone;
    
    /**
     * Multiselection node container
     */
    private final MultiNodeSelectionContainer selectionContainer = new MultiNodeSelectionContainer();
    
    /**
     * Single node selection (left clicked)
     */
    private Selectable selectedNode = null;
    
    /**
     * Pane (node) selection instance
     */
//    private RubberBandSelection selection;
    
    /**
     * Using bezier links
     */
    private boolean useBezierNodeLinks = false;
    
    /**
     * FX Node connection manager
     */
    private final FxNodeConnectionWrapperManager fxConnectionManager = new FxNodeConnectionWrapperManager();

    /**
     * Graph layout pane
     */
    @FXML private AnchorPane graphLayoutPane;
    
    /**
     * Graph layout pane context menu
     */
    private final ContextMenu layoutContextMenu = new ContextMenu();
    
    /**
     * Stores local X coordinate of context menu show event
     */
    private Double layoutContextMenuX = null;
    /**
     * Stores local Y coordinate of context menu show event
     */
    private Double layoutContextMenuY = null;

    /**
     * Create new instance and set appropriate handlers
     */
    public ModelGraphLayoutPane()
    {
        this.fxInit();
    }
    
    /**
     * Load FXML layout
     */
    private void fxInit()
    {
        FxHelper.loadFXMLAndSetController(getClass().getClassLoader().getResource("fxml/view/content/ModelGraphLayoutPane.fxml"), this);
    }
    
    /**
     * Initiate pane context menu (add new server/client node).
     */
    private void initContextMenu()
    {
        MenuItem newServerItem = new MenuItem("Server node");
        newServerItem.setOnAction((ActionEvent event) -> {
            addNewNode(new FxModelServerNode(ModelGraphLayoutPane.this, true), 
                    layoutContextMenuX, 
                    layoutContextMenuY);
        });
        
        MenuItem newClientItem = new MenuItem("Client node");
        newClientItem.setOnAction((ActionEvent event) -> {
            addNewNode(new FxModelClientNode(ModelGraphLayoutPane.this, true), 
                    layoutContextMenuX, 
                    layoutContextMenuY);
        });
        
        this.layoutContextMenu.getItems().addAll(newServerItem, newClientItem);
    }
    
    /**
     * Add new node {@code modelNode} to pane.
     * 
     * @param modelNode node
     * @param x x coordinate (in layout bounds)
     * @param y y coordinate (in layout bounds)
     */
    private void addNewNode(FxModelNode modelNode, double x, double y)
    {
        // set coordinates to mouse coordinates
        modelNode.setLayoutX(x);
        modelNode.setLayoutY(y);
        
        // display initial connection points since 
        // the actual node wont get a mouse entered event
        modelNode.handleNodeDropCreated();
        
        // add to content pane
        addDisplayableNode(modelNode);
    }
    
    /**
     * Initialize graphic components.
     */
    @FXML public void initialize()
    {
        this.constructDragHandlers();
        
        this.initContextMenu();
        this.setMouseHandlers();
        this.setActionEventHandlers();
        this.setDragHandlers();
        
        // enable rubber band selection mode
        new RubberBandSelection(this.graphLayoutPane, this.selectionContainer);
    }
    
    /**
     * Set mouse handlers (mainly selection and context menu).
     */
    private void setMouseHandlers()
    {        
        // handle mouse pressed event 
        this.graphLayoutPane.setOnMousePressed(mouseEvent ->
        {
            if(selectedNode != null)
            {
                selectedNode.onNodeDeselected();
                
                selectedNode = null;
            }
            
            if(mouseEvent.getButton() == MouseButton.SECONDARY)
            {
                // @TODO not sure if i can get those coordinates
                // when new node is created from context menu elsewhere
                layoutContextMenuX = mouseEvent.getX();
                layoutContextMenuY = mouseEvent.getY();
                
                layoutContextMenu.show(graphLayoutPane, 
                        mouseEvent.getScreenX(), mouseEvent.getScreenY());
            }
            else
            {
                layoutContextMenuX = layoutContextMenuY = null;
                
                layoutContextMenu.hide();
            }
            
            // requests focus on click so that we can handle focus change
            requestFocus();
        });
    }

    /**
     * Create instance drag handlers.
     */
    private void constructDragHandlers()
    {
        // accept draggable objects from library
        this.contentDragOver = (DragEvent dragEvent) ->
        {
            DragContainer libContainer = (DragContainer) 
                    dragEvent.getDragboard().getContent(DragContainer.ADD_NODE);
            
            if(libContainer != null)
            {
                dragEvent.acceptTransferModes(TransferMode.ANY);
                dragEvent.consume();
            }
        };
        
        // signalize to root layout that drag has been completed
        // and a new node should be added
        this.contentDragDropped = (DragEvent dragEvent) ->
        {
            // we have to store coords that will be used when adding the node
            DragContainer container = (DragContainer) 
                    dragEvent.getDragboard().getContent(DragContainer.ADD_NODE);

            container.addData(DROP_NODE_EVENT_KEY, dragEvent);
            
            ClipboardContent clipboardContent = new ClipboardContent();
            clipboardContent.put(DragContainer.ADD_NODE, container);
            
            dragEvent.getDragboard().setContent(clipboardContent);
            
            // creating new content node
            dragEvent.setDropCompleted(true);
            dragEvent.consume();
        };
        
        // a link has been created between two nodes
        this.contentDragDone = (DragEvent dragEvent) ->
        {
            DragContainer container = (DragContainer)
                    dragEvent.getDragboard().getContent(DragContainer.ADD_LINK);
            
            // check if we have source node
            if (container.getValue(FxModelNode.DRAG_LINK_SOURCE_NODE_KEY) != null)
            {
                FxModelNode source
                            = (FxModelNode) container.getValue(FxModelNode.DRAG_LINK_SOURCE_NODE_KEY);
                
                if(container.getValue(FxModelNode.DRAG_LINK_TARGET_NODE_KEY) != null)
                {
                    // display link options dialog
                    FxNodeLinkDialog dialog = new FxNodeLinkDialog();
                    FxNodeLinkDialog.setUpAndShowDialog(dialog, getScene().getWindow(), "Node link dialog");

                    if (dialog.isConfirmed())
                    {
                        final ByteSpeed bandwidth = dialog.getBandwidth();
                        final int latency = dialog.getLatency();

                        FxNodeLink link = getNodeLinkInstance();

                        FxModelNode target
                                = (FxModelNode) container.getValue(FxModelNode.DRAG_LINK_TARGET_NODE_KEY);

                        // check if link already exists
                        if (!fxConnectionManager.areNodesConnected(source, target))
                        {
                            // notify source that linking is finished
                            source.handleLinkDone();

                            // two-way link between nodes
                            link.graphicNodeBind(source, target);

                            // create two-way node connection
                            FxNodeConnectionWrapper fxNodeConnection = new FxNodeConnectionWrapper(source, target, link, bandwidth, latency);

                            // add link to content pane
                            addNodeConnection(fxNodeConnection);
                        }
                        else
                        {
                            source.handleLinkDropped();
                        }
                    }
                    else
                    {
                        source.handleLinkDropped();
                    }
                }
                else
                {
                    source.handleLinkDropped();
                }
                
                dragEvent.consume();
            }
            else if(dragEvent.getTarget() instanceof FxModelNode)
            {
                FxModelNode srcNode = (FxModelNode) dragEvent.getTarget();
                srcNode.handleLinkDropped();
                
                resetDragOverHandler();
                resetDragDroppedHandler();
                
                dragEvent.consume();
            }
        };
    }
    
    /**
     * Set instance drag handlers.
     */
    private void setDragHandlers()
    {
        this.resetDragOverHandler();
        this.resetDragDroppedHandler();
        this.resetDragDoneHandler();
    }
    
    /**
     * Sets default handler for drag over operations.
     */
    public void resetDragOverHandler()
    {
        this.graphLayoutPane.setOnDragOver(this.contentDragOver);   
    }
    
    /**
     * Sets default handler for drag dropped operations.
     */
    public void resetDragDroppedHandler()
    {
        this.graphLayoutPane.setOnDragDropped(this.contentDragDropped);
    }
    
    /**
     * Sets default handler for drag done operations.
     */
    public void resetDragDoneHandler()
    {
        this.graphLayoutPane.setOnDragDone(this.contentDragDone);
    }
    
    /**
     * Should be invoked after a {@link LibraryNode} has been 
     * dropped into this pane. A new {@link FxModelNode} will be created
     * and placed onto content pane with coordinates from the {@code DragEvent} event.
     * 
     * @param e event that has the original source node and content pane coordinates
     */
    public void handleLibraryDrop(DragEvent e)
    {
        LibraryNode dln = (LibraryNode) e.getGestureSource();        
        FxModelNode dcn = dln.getPermanentNode();
                       
        this.addNewNode(dcn, e.getX(), e.getY());
    }
    
    /**
     * Sets event handlers that handle context menu actions.
     */
    private void setActionEventHandlers()
    {
        // remove content node handler
        addEventHandler(FxNodeEvent.REMOVE_CONTENT_NODE, event -> {
            if(event.getTarget() instanceof FxModelNode)
            {
                final FxModelNode node = (FxModelNode) event.getTarget();                
                final Iterator<FxNodeConnectionWrapper> connIterator = fxConnectionManager.getNodeConnections(node).iterator();
                
                while(connIterator.hasNext())
                {
                    final FxNodeConnectionWrapper link = connIterator.next();
                    
                    // remove link from node's neighbour
                    connIterator.remove();
                    
                    // remove link from content pane
                    removeNodeConnection(link);
                }
                
                // remove label if has any
                if(node.getLabelText() != null)
                {
                    graphLayoutPane.getChildren().remove(node.getLabel());
                }
                
                removeDisplayableNode(node);
            }
            
        });
        
        // @TODO duplicate node configuration?
        // duplicate content node handler
        addEventHandler(FxNodeEvent.DUPLICATE_CONTENT_NODE, event -> {
            final FxModelNode n = (FxModelNode) event.getTarget();
            
            FxModelNode newNode = n.duplicate();
            
            newNode.setLayoutX(n.getLayoutX() + 15);
            newNode.setLayoutY(n.getLayoutY() + 15);
            
            addDisplayableNode(newNode);
        });
        
        // display node label
        addEventHandler(FxNodeLabelEvent.DISPLAY_LABEL, event -> {
            graphLayoutPane.getChildren().add( ((FxNodeLabelEvent) event).getLabel() );
        });
        
        // remove node label
        addEventHandler(FxNodeLabelEvent.REMOVE_LABEL, event -> {
            graphLayoutPane.getChildren().remove( ((FxNodeLabelEvent) event).getLabel() );
        });
        
        // single node has been selected
        addEventHandler(FxNodeEvent.NODE_SELECTED, event -> {
            if(event.getTarget() instanceof FxModelNode)
            {
                // deselect multi-selected nodes if any
                selectionContainer.deselectAll();
                
                final FxModelNode node = (FxModelNode) event.getTarget();

                // check if we had any single-selected node
                if (selectedNode != null && !selectedNode.equals(node))
                {
                    selectedNode.onNodeDeselected();
                }

                node.onNodeSelected();

                // focus node (mainly for key event delegation)
                node.requestFocus();

                selectedNode = node;
            }
        });
        
        // remove link between nodes
        addEventHandler(FxNodeLinkEvent.REMOVE_NODE_LINK, event -> {
            if(event.getTarget() instanceof FxNodeLink)
            {
                final FxNodeLink link = (FxNodeLink) event.getTarget();
                
                // get connection instance based on link
                final FxNodeConnectionWrapper conn = fxConnectionManager.getFxNodeLinkConnection(link);
                
                removeNodeConnection(conn);
            }
        });
        
        // alter node link
        addEventHandler(FxNodeLinkEvent.ALTER_NODE_LINK, event -> {
            if(event.getTarget() instanceof FxNodeLink)
            {
                final FxNodeLink link = (FxNodeLink) event.getTarget();
                
                // get connection instance based on link
                final FxNodeConnectionWrapper conn = fxConnectionManager.getFxNodeLinkConnection(link);
                
                if(conn != null)
                {                
                    FxNodeLinkDialog dialog = new FxNodeLinkDialog();
                    dialog.setNodeConnectionWrapper(conn);
                    FxNodeLinkDialog.setUpAndShowDialog(dialog, getScene().getWindow(), "Alter node link");
                    
                    if(dialog.isConfirmed())
                    {
                        final ByteSpeed bandwidth = dialog.getBandwidth();
                        final int latency = dialog.getLatency();
                        
                        conn.getBandwidth().bpsProperty().set(bandwidth.bpsProperty().get());
                        conn.setLatency(latency);
                    }
                }
            }
        });
        
        // node link characteristic
        addEventHandler(FxNodeLinkEvent.REQUEST_CHARACTERISTIC_DIALOG, event -> {
            if(event.getTarget() instanceof FxNodeLink)
            {
                final FxNodeLink link = (FxNodeLink) event.getTarget();
                final FxNodeConnectionWrapper conn = fxConnectionManager.getFxNodeLinkConnection(link);
                
                if(conn != null)
                {
                    FxNodeLinkCharacteristicDialog ctxDialog = new FxNodeLinkCharacteristicDialog();
                    ctxDialog.setCharacteristics(conn.getConnectionCharacteristic());
                    
                    FxNodeLinkCharacteristicDialog.setUpAndShowDialog(ctxDialog, getScene().getWindow(), "Connection characteristic");
                }
            }
        });
    }
    
    /**
     * Returns the content (pane that contains permanent nodes) pane.
     * 
     * @return content pane
     */
    public AnchorPane getGraphLayoutPane()
    {
        return this.graphLayoutPane;
    }
    
    /**
     * Removes all children from the content pane.
     */
    public void removeContent()
    {        
        this.fxConnectionManager.getConnections().clear();
        ModelNodeRegistry.purge();
        FsGlobalObjectRegistry.purge();
        
        graphLayoutPane.getChildren().clear();
    }
    
    /**
     * Switch from default line links to Bezier links.
     * 
     * @param useBezier true for Bezier links, false for default
     */
    public void useBezierLinks(boolean useBezier)
    {
        this.useBezierNodeLinks = useBezier;
        
        String className;
        
        if(useBezier)
        {
            className = BezierFxNodeLink.class.getCanonicalName();
        }
        else
        {
            className = LineFxNodeLink.class.getCanonicalName();
        }
        
        this.fxConnectionManager.getConnections().stream().forEach((conn) ->
        {
            conn.switchFxLinkType(className);
        });
    }
    
    /**
     * Adds a new node connection wrapper to connection manager and to content
     * pane's children
     * 
     * @param conn node connection
     */
    protected void addNodeConnection(FxNodeConnectionWrapper conn)
    {
        this.fxConnectionManager.getConnections().add(conn);
        
        this.graphLayoutPane.getChildren().add(0, conn);
    }
    
    /**
     * Add a new node connection wrapper to connection manager from 
     * user event.
     * 
     * @param conn node connection
     */
    public void handleUserAddNodeConnection(FxNodeConnectionWrapper conn)
    {
        this.addNodeConnection(conn);
    }
    
    /**
     * Remove node connection from connection manager and content pane's
     * children.
     * 
     * @param conn node connection
     */
    protected void removeNodeConnection(FxNodeConnectionWrapper conn)
    {
        this.fxConnectionManager.removeConnection(conn);
        
        this.graphLayoutPane.getChildren().remove(conn);
    }
    
    /**
     * Get a node link instance based on current node link type settting.
     * 
     * @return node link instance
     */
    public FxNodeLink getNodeLinkInstance()
    {
        if(this.useBezierNodeLinks)
         {
            return new BezierFxNodeLink();
        }
        else
        {
            return new LineFxNodeLink();
        }
    }
    
    /**
     * Get node connection wrapper manager instance for this content pane.
     * 
     * @return node connection wrapper manager
     */
    public FxNodeConnectionWrapperManager getFxConnectionWrapperManager()
    {
        return this.fxConnectionManager;
    }
    
    /**
     * Call {@link FxNodeLink#setLinkHighlighted(boolean)} for all given
     * links in {@code connections}.
     * 
     * @param connections connections
     * @param highlight highlight flag
     */
    public void highlightNodeLinks(Collection<ModelNodeConnection> connections, boolean highlight)
    {
        for(ModelNodeConnection conn : connections)
        {
            FxNodeConnectionWrapper wrapper = this.fxConnectionManager.getNodeConnectionWraper(conn);
            
            if(wrapper != null)
            {
                wrapper.getFxNodeLink().setLinkHighlighted(highlight);
            }
        }
    }
    
    /**
     * Add graphical node to content pane.
     * 
     * @param <T> node type
     * @param node node
     */
    private <T extends Node & GraphDisplayable> void addDisplayableNode(T node)
    {
        this.graphLayoutPane.getChildren().add(node);
        node.onGraphDisplayed();
    }
    
    /**
     * Remove graphical node from content pane.
     * 
     * @param <T> node type
     * @param node node
     */
    private <T extends Node & GraphDisplayable> void removeDisplayableNode(T node)
    {
        this.graphLayoutPane.getChildren().remove(node);
        node.onAfterRemovedFromGraph();
    }

    /**
     * {@inheritDoc}
     */
    @Override public List<StatePersistable> getPersistableChildren()
    {
        List<StatePersistable> l = new ArrayList<>();
        
        // add all children nodes of type {@code FxNode}
        this.graphLayoutPane.getChildren().stream().filter((n) -> (n instanceof FxModelNode)).forEach((n) ->
        {
            l.add((FxModelNode) n);
        });
        
        // add node connections
        l.add(this.fxConnectionManager);
        
        return l;
    }

    /**
     * {@inheritDoc}
     */
    @Override public StatePersistableElement export(StatePersistenceLogger logger)
    {
        logger.logOperation(PERSISTABLE_NAME, "Exporting content.", true);
        logger.logOperation(PERSISTABLE_NAME, "Exported " + (this.getPersistableChildren().size() - 1) + " total nodes.", true);
        
        StatePersistableElement element = new StatePersistableElement(this.getPersistableName());
        
        return element;
    }

    /**
     * {@inheritDoc}
     */
    @Override public void restoreState(StatePersistableElement state, StatePersistenceLogger logger, Object... args) throws InvalidPersistedStateException
    {
        logger.logOperation(PERSISTABLE_NAME, "Restoring content.", true);
        
        if(state != null)
        {            
            int i = 0;
            for(StatePersistableElement childState : state.getElements())
            {
                if(childState.getName().equals(FxModelServerNode.PERSISTABLE_NAME))
                {
                    i++;
                    
                    // underlying model node is NOT registered yet
                    // will be after it's state is restored
                    FxModelServerNode fxNode = new FxModelServerNode(this, false);
                    fxNode.restoreState(childState, logger);
                    
                    addDisplayableNode(fxNode);
                }
                else if(childState.getName().equals(FxModelClientNode.PERSISTABLE_NAME))
                {
                    i++;
                    
                    // underlying model node is NOT registered yet
                    // will be after it's state is restored
                    FxModelClientNode fxNode = new FxModelClientNode(this, false);
                    fxNode.restoreState(childState, logger);
                    
                    addDisplayableNode(fxNode);
                }
            }
            
            logger.logOperation(PERSISTABLE_NAME, "Restored " + i + " total nodes.", true);
            
            // has to be restored after nodes
            this.fxConnectionManager.restoreState(
                    state.getElement(this.fxConnectionManager.getPersistableName()), 
                    logger,
                    this
            );
        }
        else
        {
            logger.logOperation(PERSISTABLE_NAME, "Nothing to restore.", true);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override public String getPersistableName()
    {
        return PERSISTABLE_NAME;
    }
    
    /**
     * Find graphical node from content pane by it's underlying (model) node ID.
     * 
     * @param nodeID node ID
     * @return found node or null
     */
    public FxModelNode getChildrenNodeByID(String nodeID)
    {
        for(Node n : this.graphLayoutPane.getChildren())
        {
            if(n instanceof FxModelNode)
            {
                FxModelNode fxNode = (FxModelNode) n;
                
                if(fxNode.getNode().getNodeID().equals(nodeID))
                {
                    return fxNode;
                }
            }
        }
        
        return null;
    }
}
