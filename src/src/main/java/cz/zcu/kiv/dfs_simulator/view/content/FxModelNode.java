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
import cz.zcu.kiv.dfs_simulator.persistence.StatePersistableAttribute;
import cz.zcu.kiv.dfs_simulator.persistence.StatePersistableElement;
import cz.zcu.kiv.dfs_simulator.persistence.StatePersistenceLogger;
import cz.zcu.kiv.dfs_simulator.helpers.FxHelper;
import cz.zcu.kiv.dfs_simulator.helpers.Helper;
import cz.zcu.kiv.dfs_simulator.model.LabelException;
import cz.zcu.kiv.dfs_simulator.model.ModelNode;
import cz.zcu.kiv.dfs_simulator.view.DragContainer;
import cz.zcu.kiv.dfs_simulator.view.InvalidLabelException;
import cz.zcu.kiv.dfs_simulator.view.content.connection.FxNodeConnectionManager;
import cz.zcu.kiv.dfs_simulator.view.content.events.FxNodeEvent;
import cz.zcu.kiv.dfs_simulator.view.content.events.MultiNodeSelectionEvent;
import cz.zcu.kiv.dfs_simulator.view.content.connection.FxNodeLink;
import cz.zcu.kiv.dfs_simulator.view.content.events.FxNodeLabelEvent;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

/**
 * Graphical node.
 */
abstract public class FxModelNode extends AnchorPane implements GraphDisplayable
{
    public static final long serialVersionUID = 0L;
    
    /**
     * Drag link dragboard source node key
     */
    public static final String DRAG_LINK_SOURCE_NODE_KEY = "source_node";
    /**
     * Drag link dragboard target node key
     */
    public static final String DRAG_LINK_TARGET_NODE_KEY = "target_node";
    
    /**
     * Whether node label should be displayed by default
     */
    public static final boolean LABEL_DEFAULT_VISIBLE = true;
    
    /**
     * Minimum label length
     */
    public static final int MIN_LABEL_LENGTH = 1;
    /**
     * Maximum label length
     */
    public static final int MAX_LABEL_LENGTH = 20;
    
    /**
     * Temporal link that is displayed while dragging a new connection
     */
    private FxNodeLink tempNodeLink;
    
    /**
     * Link dragged over handler
     */
    private EventHandler<DragEvent> contentLinkDragOver;
    /**
     * Link dropped handler
     */
    private EventHandler<DragEvent> contentLinkDragDropped;
    
    /* Node appearance */
    
    /**
     * Number of vertical points displayed vertically (total number of points
     * will be x2 this)
     */
    private static final int NUM_VERTICAL_LINK_POINTS = 2;
    /**
     * Radius of connection drag origin point that is displayed to the user
     */
    private static final int VISIBLE_LINK_POINT_RADIUS = 5;
    /**
     * Padding of connection drag origin point that is displayed to the user
     */
    private static final int VISIBLE_LINK_POINT_OUTER_PADDING = -8;
    /**
     * This is the actual radius of connection drag origin point, which
     * will register drag events
     */
    private static final int ACTUAL_LINK_POINT_RADIUS = 12;
    
    /**
     * Connection drag origin point fill color
     */
    private static final Color LINK_POINT_FILL_COLOR = Color.rgb(0, 125, 252, 0.55);
    /**
     * Connection drag origin point stroke color
     */
    private static final Color LINK_POINT_STROKE_HEX = Color.rgb(239, 239, 239);
    
    /**
     * Connection drag box border padding
     */
    private static final int LINK_BORDER_PADDING = -8;
    /**
     * Connection drag box border spacing
     */
    private static final double LINK_BORDER_SPACING = 2.0;
    /**
     * Connection drag box border color
     */
    private static final Color LINK_BORDER_HEX = Color.rgb(158, 158, 158);
    
    /**
     * Underlying node
     */
    protected final ModelNode node;
    /**
     * Graph layout pane this node is displayed on
     */
    protected final ModelGraphLayoutPane graphLayoutPane;
    
    /**
     * Node size (width/height) property listener
     */
    private ChangeListener sizePropertyListener;
    /**
     * Flag indicating whether {@link #sizePropertyListener} has been bound
     */
    private volatile boolean sizePropertyListenerBound = false;
    
    /**
     * Whether this node is currently in the process of linking to other node
     * (node connection is being dragged from one of its points)
     */
    protected volatile boolean linkingState = false;
    
    /**
     * Group of connection box points
     */
    protected Group linkPoints = null;
    /**
     * Group of connection box borders
     */
    protected Rectangle linkBorders = null;
    
    /**
     * Node drag mouse X offset
     */
    private double deltaX;
    /**
     * Node drag mouse Y offset
     */
    private double deltaY;
    
    /**
     * Whether this node is currently part of a multi-selection
     */
    protected boolean multiSelectableState = false;
    /**
     * Origin of multi-selection drag
     */
    protected Point2D multiDragOrigin;
    
    /**
     * Whether this node is focused (hard-selected)
     */
    protected boolean hardSelect = false;
    
    /**
     * Node label
     */
    protected final FxModelNodeLabel label;
    
    /**
     * Graphical connection manager
     */
    protected final FxNodeConnectionManager fxConnectionManager;
    
    /**
     * Node icon
     */
    @FXML protected ImageView imageView;
    
    /**
     * Graphical node. Constructs initial connection manager and node label,
     * initializes all mouse and key event handlers.
     * 
     * @param node underlying node
     * @param graphLayoutPane graph pane on which this node will be displayed
     */
    public FxModelNode(ModelNode node, ModelGraphLayoutPane graphLayoutPane)
    {
        this.node = node;
        this.graphLayoutPane = graphLayoutPane;
        this.fxConnectionManager = new FxNodeConnectionManager(node.getConnectionManager());

        this.label = new FxModelNodeLabel(this);
        
        this.fxInit();
        
        this.setConnectPoints();
        this.setNodeLinkReceiveHandlers();
        
        this.initKeyHandlers();
        this.initDragHandlers();
        this.initContextMenu();
        this.initConnectionBoxMouseHandlers();
        this.initLinkHandlers();
    }
    
    /**
     * Load FXML schema.
     */
    private void fxInit()
    {
        FxHelper.loadFXMLAndSetController(getClass().getClassLoader().getResource("fxml/view/content/FxModelNode.fxml"), this);
    }
    
    /**
     * Initialize graphic components.
     */
    @FXML public void initialize()
    {
        this.initiateImageViewImage();
    }
    
    /**
     * Initialize temporal node link.
     */
    private void initNodeLink()
    {
        // we have to request node link instance from content pane
        this.tempNodeLink = this.graphLayoutPane.getNodeLinkInstance();
        this.tempNodeLink.setLinkFading(true);
    }
    
    /**
     * Initialize keyboard handlers.
     */
    private void initKeyHandlers()
    {
        setOnKeyPressed(keyEvent ->
        {
            if(keyEvent.getCode() == KeyCode.DELETE && hardSelect)
            {
                handleUserDelete();
            }
        });
    }
    
    /**
     * Initialize node drag handlers.
     */
    private void initDragHandlers()
    {
        setOnMousePressed(mouseEvent -> {
            onHardSelectAction(true);
            
            if(mouseEvent.getClickCount() == 2)
            {
                nodeContextDialogRequestedAction();                
            }
            else
            {
                // store cursor delta
                deltaX = FxModelNode.this.getLayoutX() - mouseEvent.getSceneX();
                deltaY = FxModelNode.this.getLayoutY() - mouseEvent.getSceneY();

                if(multiSelectableState)
                {
                    multiDragOrigin = new Point2D(mouseEvent.getSceneX(), 
                            mouseEvent.getSceneY());
                }
            }
            
            mouseEvent.consume();
        });
                
        setOnMouseDragged(mouseEvent -> {
            onHardSelectAction(false);
            
            // move only with left button
            if(!linkingState && mouseEvent.getButton() == MouseButton.PRIMARY)
            {
                if(multiSelectableState)
                {
                    multiDragEventAction(mouseEvent);
                    
                    multiDragOrigin = new Point2D(mouseEvent.getSceneX(), 
                            mouseEvent.getSceneY());
                }
                else
                {
                    handleNodeDragged(mouseEvent);
                }
            }
            
            mouseEvent.consume();
        });
    }
    
    /**
     * Checks whether node can be moved to new X location {@code newX}.
     * 
     * @param newX new X location
     * @return true if node can be moved, false otherwise
     */
    public boolean canMoveNodeX(double newX)
    {
        return (newX <= getScene().getWidth() && newX >= 0);
    }
    
    /**
     * Checks whether node can be moved to new Y location {@code newY}.
     * 
     * @param newY new Y location
     * @return true if node can be moved, false otherwise
     */
    public boolean canMoveNodeY(double newY)
    {
        return (newY <= getScene().getHeight() && newY >= 0);
    }
    
    /**
     * Move node to new X location {@code newX}. Node wont be moved outside of
     * scene bounds.
     * 
     * @param newX new X location
     */
    public void moveNodeX(double newX)
    {
        if (newX > getScene().getWidth())
        {
            newX = getScene().getWidth();
        }
        else if (newX < 0)
        {
            newX = 0;
        }

        setLayoutX(newX);
    }
    
    /**
     * Move node to new Y location {@code newY}. Node wont be moved outside
     * of scene bounds.
     * 
     * @param newY new Y location
     */
    public void moveNodeY(double newY)
    {
        if (newY > getScene().getHeight())
        {
            newY = getScene().getHeight();
        }
        else if (newY < 0)
        {
            newY = 0;
        }

        setLayoutY(newY); 
    }
    
    /**
     * Handle node drag event. Drags node to new coordinates
     * based on mouse cursor position.
     * 
     * @param mouseEvent mouse event
     */
    private void handleNodeDragged(MouseEvent mouseEvent)
    {
        // calculate new coordinates accounting for mouse cursor delta
        double newX = mouseEvent.getSceneX() + deltaX;
        double newY = mouseEvent.getSceneY() + deltaY;
        
        if(this.canMoveNodeX(newX))
        {
            if(this.label != null)
            {
                this.label.offsetNodeX(newX - getLayoutX());
            }
            
            this.moveNodeX(newX);
        }
        
        if(this.canMoveNodeY(newY))
        {
            if(this.label != null)
            {
                this.label.offsetNodeY(newY - getLayoutY());
            }
            
            this.moveNodeY(newY);
        }

        mouseEvent.consume();
    }
    
    /**
     * Create node context menu.
     */
    private void initContextMenu()
    {
        final ContextMenu cm = new ContextMenu();

        // add items
        cm.getItems().add(this.getLabelContextItem());
        cm.getItems().add(this.getConfigureContextItem());
        cm.getItems().add(this.getDuplicateContextItem());
        cm.getItems().add(this.getRemoveContextItem());
        
        // add context menu to itself
        addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            if(mouseEvent.getButton() == MouseButton.SECONDARY)
            {
                cm.show(FxModelNode.this, 
                        mouseEvent.getScreenX(), mouseEvent.getScreenY());
                
                mouseEvent.consume();
            }
        });
    }
    
    /**
     * Returns remove node context item.
     * 
     * @return remove node context item
     */
    private MenuItem getRemoveContextItem()
    {
        MenuItem removeItem = new MenuItem("Remove");
        
        removeItem.setOnAction(actionEvent -> {
            handleUserDelete();
        });
        
        return removeItem;
    }
    
    /**
     * Returns configure node context item.
     * 
     * @return configure node context item
     */
    private MenuItem getConfigureContextItem()
    {
        MenuItem configureItem = new MenuItem("Configure node");
        
        configureItem.setOnAction(actionEvent ->
        {
            nodeContextDialogRequestedAction();
        });
        
        return configureItem;
    }
    
    /**
     * Display node label radio option
     */
    protected RadioMenuItem displayLabelItem;
    
    /**
     * Returns display label context item.
     * 
     * @return display label context item.
     */
    private MenuItem getLabelContextItem()
    {
        this.displayLabelItem = new RadioMenuItem("Display label");
        
        this.displayLabelItem.setOnAction(actionEvent -> {
            setLabelVisibleAction(displayLabelItem.isSelected());
        });
        
        this.displayLabelItem.setSelected(true);
        
        return this.displayLabelItem;
    }
    
    /**
     * Handle label visible state changed.
     * 
     * @param visible visible flag
     */
    protected void setLabelVisibleAction(boolean visible)
    {
        if(visible)
        {
            fireEvent(new FxNodeLabelEvent(FxNodeLabelEvent.DISPLAY_LABEL, this.label));
        }
        else
        {
            fireEvent(new FxNodeLabelEvent(FxNodeLabelEvent.REMOVE_LABEL, this.label));
        }
    }
    
    /**
     * Returns duplicate node context item.
     * 
     * @return duplicate node context item.
     */
    private MenuItem getDuplicateContextItem()
    {
        MenuItem duplicateItem = new MenuItem("Duplicate");
        
        duplicateItem.setOnAction(actionEvent -> {
            duplicateAction();
        });
        
        return duplicateItem;
    }
    
    /**
     * Handle node deleted by user. Displays confirmation dialog before
     * deleting.
     * 
     * @return true if deleted, false otherwise
     */
    public boolean handleUserDelete()
    {
        Alert dialog = FxHelper.getConfirmationDialog(
                "Confirm delete", "Delete node", "Are you sure you want to delete this node?");
        
        Optional<ButtonType> result = dialog.showAndWait();
        
        if(result.isPresent() && result.get() == ButtonType.OK)
        {
            this.removeFromParentAction();
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Handle node remove from parent. Fires event {@link FxNodeEvent#REMOVE_CONTENT_NODE}
     * that should be caught by the parent.
     */
    protected void removeFromParentAction()
    {
        // remove label first
        this.setLabelVisibleAction(false);
        
        this.fireEvent(new FxNodeEvent(FxNodeEvent.REMOVE_CONTENT_NODE));
    }
     
    /**
     * Handle node duplicate requested. Fires event {@link FxNodeEvent#DUPLICATE_CONTENT_NODE}
     * that should be caught by parent.
     */
    protected void duplicateAction()
    {
        this.fireEvent(new FxNodeEvent(FxNodeEvent.DUPLICATE_CONTENT_NODE));
    }
    
    /**
     * Init handlers that handle connection creation by mouse dragging.
     */
    private void initConnectionBoxMouseHandlers()
    {
        setOnMouseEntered(mouseEvent -> {
            displayConnectPoints();
        });
        
        setOnMouseExited(mouseEvent -> {
            if(linkPoints != null && !multiSelectableState && !hardSelect)
            {
                hideConnectPoints();
            }
        });
        
        setOnMouseReleased(mouseEvent -> {
            // display connect points only if mouse event is in our bounds
            if(FxHelper.isMouseEventOverNode(mouseEvent, FxModelNode.this))
            {
                displayConnectPoints();
            }

            // change linking state
            // @TODO why isnt linking node notified of this instead?
            linkingState = false;
            
            mouseEvent.consume();
        });
    }
    
    /**
     * Initiates connection box points that allow dragging - creating
     * links between nodes.
     */
    private synchronized void setConnectPoints()
    {
        if(this.linkPoints == null)
        {
            this.linkPoints = new Group();
        }
        
        // check if we have width/height yet
        if(getWidth() == 0 || getHeight() == 0)
        {
            // check if we have listeners already bound and waiting
            if(!this.sizePropertyListenerBound)
            {
                // have to wait for the properties to be set (for this node 
                // to be added and displayed in it's parent)
                this.sizePropertyListener = this.getConnectPropertyListener();

                // we will add our listener to both properties, since we are in a
                // synchronized context we will be able to make sure that only
                // one pass of the listener will be ran
                widthProperty().addListener(this.sizePropertyListener);
                heightProperty().addListener(this.sizePropertyListener);

                this.sizePropertyListenerBound = true;
            }
            
            return;
        }
        
        // make sure only one listener pass is possible
        if(this.sizePropertyListenerBound)
        {
            this.sizePropertyListenerBound = false;

            widthProperty().removeListener(this.sizePropertyListener);
            heightProperty().removeListener(this.sizePropertyListener);
        }
        
        // check how many vertical linking points are to be drawn
        if(NUM_VERTICAL_LINK_POINTS > 0)
        {
            double dY = (NUM_VERTICAL_LINK_POINTS > 1) ? 
                    (getHeight() / (NUM_VERTICAL_LINK_POINTS - 1)) : getHeight();
            
            for(int i = 0; i < NUM_VERTICAL_LINK_POINTS; i++)
            {                
                // visible but inactive
                this.linkPoints.getChildren().add(0, 
                        this.getLinkPointCircle(-VISIBLE_LINK_POINT_OUTER_PADDING, 
                                (dY * i) + this.getLinkPointHeightPadding(i), VISIBLE_LINK_POINT_RADIUS));
                // invisible but active
                this.linkPoints.getChildren().add(
                        this.getActualLinkPointCircle(-VISIBLE_LINK_POINT_OUTER_PADDING, 
                                (dY * i) + this.getLinkPointHeightPadding(i), ACTUAL_LINK_POINT_RADIUS));
            }
            
            for(int i = 0; i < NUM_VERTICAL_LINK_POINTS; i++)
            {
                // visible but inactive
                this.linkPoints.getChildren().add(0, 
                        this.getLinkPointCircle(getWidth() + VISIBLE_LINK_POINT_OUTER_PADDING, 
                                dY * i + this.getLinkPointHeightPadding(i), VISIBLE_LINK_POINT_RADIUS));
                // invisible but active
                this.linkPoints.getChildren().add(
                        this.getActualLinkPointCircle(getWidth() + VISIBLE_LINK_POINT_OUTER_PADDING, 
                                dY * i + this.getLinkPointHeightPadding(i), ACTUAL_LINK_POINT_RADIUS));
            }
            
            if(this.linkBorders == null)
            {
                this.linkBorders = this.getLinkBorders();
            }
        }
    }
    
    /**
     * Get connection drag point padding.
     * 
     * @param index connection point index (from 0 - top left, bottom left, top right, bottom right)
     * @return padding
     */
    private int getLinkPointHeightPadding(int index)
    {
        int coeff;
        
        switch(index)
        {
            case 0:
                coeff = -1;
                break;
            case (NUM_VERTICAL_LINK_POINTS - 1):
                coeff = +1;
                break;
            default:
                coeff = 0;
                break;
        }
        
        return (coeff * VISIBLE_LINK_POINT_OUTER_PADDING);
    }
    
    /**
     * Display node connection points. Points register drag events
     * and begin linking with other nodes.
     */
    private synchronized void displayConnectPoints()
    {
        if(!getChildren().contains(this.linkPoints) && 
                this.linkPoints != null && this.linkBorders != null)
        {
            getChildren().add(1, this.linkPoints);
            getChildren().add(0, this.linkBorders);
        }
    }
    
    /**
     * Hide node connection points.
     */
    protected void hideConnectPoints()
    {
        getChildren().remove(this.linkPoints);
        getChildren().remove(this.linkBorders);
    }
    
    /**
     * Get {@link ChangeListener} that displays connect points on change.
     * 
     * @return listener
     */
    private ChangeListener getConnectPropertyListener()
    {
        return (ChangeListener) (ObservableValue observable, Object oldValue, Object newValue) ->
        {
            if(sizePropertyListenerBound)
            {
                setConnectPoints();
            }
        };
    }
    
    /**
     * Get actual (invisible) connection point. Point has all handlers (listeners)
     * already set.
     * 
     * @param x x coordinate
     * @param y y coordinate
     * @param radius point radius
     * @return connection point
     */
    protected Circle getActualLinkPointCircle(double x, double y, double radius)
    {
        Circle c = this.getLinkPointCircle(x, y, radius);
        
        this.setLinkMouseHandlers(c);
        c.setFill(Color.rgb(0, 0, 0, 0));
        c.setStroke(Color.rgb(0, 0, 0, 0));
        c.toFront();
        
        return c;
    }
    
    /**
     * Get visible connection point. Does not have any function other than
     * displaying user where the center of actual connection points is.
     * 
     * @param x x coordinate
     * @param y y coordinate
     * @param radius visible point radius
     * @return visible connection point
     */
    protected Circle getLinkPointCircle(double x, double y, double radius)
    {
        Circle c = new Circle(x, y, radius);
        
        c.setFill(LINK_POINT_FILL_COLOR);
        c.setStroke(LINK_POINT_STROKE_HEX);
        
        return c;
    }
        
    /**
     * Get connection box borders. Borders run through connection points.
     * Borders do not handle mouse events.
     * 
     * @return link borders
     */
    protected Rectangle getLinkBorders()
    {
        Rectangle r = new Rectangle();

        r.setLayoutX(-LINK_BORDER_PADDING);
        r.setLayoutY(-LINK_BORDER_PADDING);
        
        r.setWidth(getWidth() + (2 * LINK_BORDER_PADDING));
        r.setHeight(getHeight() + (2 * LINK_BORDER_PADDING));
        
        r.setStroke(LINK_BORDER_HEX);
        r.setFill(null);
        r.getStrokeDashArray().addAll(LINK_BORDER_SPACING);
        r.setStrokeWidth(0.5);
        
        // @TODO handle this elsewhere
        r.setOnMouseDragged(mouseEvent -> {
            linkingState = true;
        });
        
        r.toFront();

        return r;
    }
    
    /**
     * Set link mouse handlers. Passed node {@code n} will be able
     * to initiate connection creation by being dragged.
     * 
     * @param n node
     */
    private void setLinkMouseHandlers(Node n)
    {
        n.setOnMouseEntered(mouseEvent -> {
            if(!multiSelectableState)
            {
                displayConnectPoints();
            }
            
            getScene().setCursor(Cursor.HAND);
        });
        
        n.setOnMouseExited(mouseEvent -> {
            getScene().setCursor(Cursor.DEFAULT);
        });
        
        n.setOnMouseDragged(mouseEvent -> {
            // set linking state to disable node drag
            linkingState = true;
        });
        
        n.setOnMousePressed(mouseEvent -> {
            mouseEvent.consume();
        });
        
        n.setOnDragDetected(dragEvent -> {
            // set handlers to content pane
            getParent().setOnDragOver(contentLinkDragOver);
            getParent().setOnDragDropped(contentLinkDragDropped);
            
            Point2D parentLinkPoint = 
                    new Point2D(FxModelNode.this.getLayoutX() + (FxModelNode.this.getWidth() / 2), 
                            FxModelNode.this.getLayoutY() + (FxModelNode.this.getHeight() / 2));
            
            // get node link instance
            initNodeLink();
            
            tempNodeLink.setStart(parentLinkPoint);
            tempNodeLink.setEnd(parentLinkPoint);
            
            // add to content pane
            graphLayoutPane.getGraphLayoutPane().getChildren().add(0, tempNodeLink);
                        
            // have to set dummy content
            ClipboardContent content = new ClipboardContent();
            DragContainer container = new DragContainer();
            
            content.put(DragContainer.ADD_LINK, container);
                        
            startDragAndDrop(TransferMode.ANY).setContent(content);
            
            dragEvent.consume();
        });
    }
    
    /**
     * Initiate default link drag and drop handlers. These handlers are used
     * by parent pane.
     */
    private void initLinkHandlers()
    {
        this.contentLinkDragOver = (DragEvent event) -> 
        {
            event.acceptTransferModes(TransferMode.ANY);

            tempNodeLink.setEnd(new Point2D(event.getX(), event.getY()));

            event.consume();
        };
        
        this.contentLinkDragDropped = (DragEvent event) ->
        {
            handleInvalidLinkDrop(event);
        };
        
    }
    
    /**
     * Initiate node link handlers. Handles drag events of connection
     * from other nodes - this node would be the target of that connection.
     */
    private void setNodeLinkReceiveHandlers()
    {
        // node link has been dropped to this node
        setOnDragDropped(dragEvent -> {
            if(canLinkWithNode(dragEvent.getGestureSource()))
            {
                DragContainer container = (DragContainer) dragEvent.getDragboard().getContent(DragContainer.ADD_LINK);

                container.addData(DRAG_LINK_SOURCE_NODE_KEY, dragEvent.getGestureSource());
                container.addData(DRAG_LINK_TARGET_NODE_KEY, FxModelNode.this);

                ClipboardContent content = new ClipboardContent();
                content.put(DragContainer.ADD_LINK, container);

                // restore original parent handler
                graphLayoutPane.resetDragOverHandler();
                graphLayoutPane.resetDragDroppedHandler();

                dragEvent.getDragboard().setContent(content);
                dragEvent.setDropCompleted(true);
                dragEvent.consume();
            }
            else if(dragEvent.getGestureSource() instanceof FxModelNode)
            {
                FxModelNode n = (FxModelNode) dragEvent.getGestureSource();
                
                n.handleInvalidLinkDrop(dragEvent);
            }
        });
        
        // dragging node link over this node
        setOnDragOver(dragEvent -> {
            if(canLinkWithNode(dragEvent.getGestureSource()))
            {
                final FxModelNode nodeSource = (FxModelNode) dragEvent.getGestureSource();
                
                // higlight link
                nodeSource.handleLinkOverNode();
                
                dragEvent.acceptTransferModes(TransferMode.ANY);

                displayConnectPoints();
            }
        });
        
        // dragging node link has exited this node
        setOnDragExited(dragEvent -> {
            if(canLinkWithNode(dragEvent.getGestureSource()))
            {
                final FxModelNode nodeSource = (FxModelNode) dragEvent.getGestureSource();
                
                // disable link highlight
                nodeSource.handleLinkExitedNode();
                
                hideConnectPoints();
            }
        });
    }
    
    /**
     * Checks whether this node can be linked with node {@code source}.
     * 
     * @param source source node
     * @return true if can link, false otherwise
     */
    private boolean canLinkWithNode(Object source)
    {
        return (source instanceof FxModelNode && 
                    !source.equals(FxModelNode.this) && 
                    (source instanceof FxModelServerNode || this instanceof FxModelServerNode));
    }
    
    /**
     * Handle case when link dragged from this node was not dropped
     * to a valid node (connection was not created).
     * 
     * @param event drag event
     */
    protected void handleInvalidLinkDrop(DragEvent event)
    {
        if (this.tempNodeLink != null)
        {
            this.graphLayoutPane.getGraphLayoutPane().getChildren().remove(this.tempNodeLink);
            this.tempNodeLink = null;
        }

        // restore original parent handler
        this.graphLayoutPane.resetDragOverHandler();
        this.graphLayoutPane.resetDragDroppedHandler();

        event.consume();
    }
    
    /**
     * Handle node connection created by dragging link.
     */
    public void handleLinkDone()
    {
        this.handleLinkDropped();
    }
    
    /**
     * Handle link was dropped outside of node (connection not created).
     */
    public void handleLinkDropped()
    {
        this.graphLayoutPane.getGraphLayoutPane().getChildren().remove(
                this.tempNodeLink);
        this.tempNodeLink = null;
    }
    
    /**
     * Request node setting (context) dialog. Fires event
     * {@link FxNodeEvent#NODE_CONTEXT_DIALOG_REQUESTED}.
     */
    private void nodeContextDialogRequestedAction()
    {
        fireEvent(new FxNodeEvent(FxNodeEvent.NODE_CONTEXT_DIALOG_REQUESTED));
    }
        
    /**
     * Handle event of multi-selection drag, which this node is source of 
     * (this node is being actually dragged by user).
     * 
     * @param mouseEvent drag event
     */
    private void multiDragEventAction(MouseEvent mouseEvent)
    {
        double dragOffsetX = (mouseEvent.getSceneX() - this.multiDragOrigin.getX());
        double dragOffsetY = (mouseEvent.getSceneY() - this.multiDragOrigin.getY());
        
        this.fireEvent(
                new MultiNodeSelectionEvent(MultiNodeSelectionEvent.DRAG_SELECTION, dragOffsetX, dragOffsetY));
    }
        
    /**
     * Get node label.
     * 
     * @return label
     */
    public FxModelNodeLabel getLabel()
    {
        return this.label;
    }
    
    /**
     * Handle case when node is being focused.
     * 
     * @param state focus state
     */
    private void onHardSelectAction(boolean state)
    {
        if(state && !multiSelectableState)
        {
            this.fireEvent(new FxNodeEvent(FxNodeEvent.NODE_SELECTED));
        }
        else
        {
            this.hardSelect = state;
        }
    }
    
    /**
     * Handle case when link from other node is being dragged over this node.
     */
    public void handleLinkOverNode()
    {
        this.tempNodeLink.setLinkHighlighted(true);
    }
    
    /**
     * Handle case when link from other node exited this node.
     */
    public void handleLinkExitedNode()
    {
        this.tempNodeLink.setLinkHighlighted(false);
    }
    
    /**
     * Handle case when this node was created by drag and drop event. Displays
     * connection point - since mouse cursor is already over this node, connection
     * box would not be displayed without exiting node first.
     */
    public void handleNodeDropCreated()
    {
        displayConnectPoints();
    }
    
    /**
     * Get connection manager.
     * 
     * @return connection manager
     */
    public FxNodeConnectionManager getFxConnectionManager()
    {
        return this.fxConnectionManager;
    }
    
    /**
     * Get graph model layout pane.
     * 
     * @return graph model layout pane
     */
    public ModelGraphLayoutPane getGraphLayoutPane()
    {
        return this.graphLayoutPane;
    }
    
    /**
     * Get underlying model node.
     * 
     * @return model node
     */
    public ModelNode getNode()
    {
        return this.node;
    }
    
    /**
     * Get node icon.
     * 
     * @return icon
     */
    public Image getNodeImage()
    {
        return this.imageView.getImage();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public void offsetNodeX(double offsetX)
    {
        this.moveNodeX(getLayoutX() + offsetX);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public void offsetNodeY(double offsetY)
    {
        this.moveNodeY(getLayoutY() + offsetY);
    }
        
    /**
     * {@inheritDoc}
     */
    @Override public void removeFromParent()
    {
        this.removeFromParentAction();
    }
        
    /**
     * {@inheritDoc}
     */
    @Override public boolean canOffsetNodeX(double offsetX)
    {
        return ( (getLayoutX() + offsetX) > 0 ) && ( (getLayoutX() + offsetX) <= getScene().getWidth());
    }
        
    /**
     * {@inheritDoc}
     */
    @Override public boolean canOffsetNodeY(double offsetY)
    {
        return ( (getLayoutY() + offsetY) > 0 );
    }
        
    /**
     * {@inheritDoc}
     */
    @Override public void onNodeSelected()
    {
        this.hardSelect = true;
        
        displayConnectPoints();
    }
        
    /**
     * {@inheritDoc}
     */
    @Override public void onNodeDeselected()
    {
        this.hardSelect = false;
        
        hideConnectPoints();
    }
        
    /**
     * {@inheritDoc}
     */
    @Override public void highlight()
    {
        this.displayConnectPoints();
    }
        
    /**
     * {@inheritDoc}
     */
    @Override public void removeHighlight()
    {
        this.hideConnectPoints();
    }
        
    /**
     * {@inheritDoc}
     */
    @Override public void setMultiSelectableState(boolean state)
    {
        this.multiSelectableState = state;
    }
        
    /**
     * {@inheritDoc}
     */
    @Override public void setLabelText(String label) throws LabelException
    {
        if(label.length() < MIN_LABEL_LENGTH || label.length() > MAX_LABEL_LENGTH)
        {
            throw new InvalidLabelException("Label has to be between " + MIN_LABEL_LENGTH + " and " + MAX_LABEL_LENGTH + " characters long.");
        }
        
        this.node.changeId(label);
    }
        
    /**
     * {@inheritDoc}
     */
    @Override public String getLabelText()
    {
        return this.node.getNodeID();
    }
    
    /**
     * Whether label should be centered (used so that label is centered
     * only once when the node is created, but we have to wait for it's
     * dimension to be set)
     */
    protected boolean adjustInitialLabelPosition = true;
        
    /**
     * {@inheritDoc}
     */
    @Override public void onGraphDisplayed()
    {
        if(LABEL_DEFAULT_VISIBLE)
        {
            Platform.runLater( () -> {
                // center it above node
                if(adjustInitialLabelPosition)
                {
                    label.adjustLabelPosition();
                }
                
                setLabelVisibleAction(true);
            });
        }
    }
    
    /**
     * {@inheritDoc}
     */  
    @Override public StatePersistableElement export(StatePersistenceLogger logger)
    {
        StatePersistableElement element = new StatePersistableElement(getPersistableName());
        
        element.addAttribute(new StatePersistableAttribute("layout-x", "" + getLayoutX()));
        element.addAttribute(new StatePersistableAttribute("layout-y", "" + getLayoutY()));
        
        if(label.isVisible())
        {
            element.addAttribute(new StatePersistableAttribute("label-x", "" + label.getLayoutX()));
            element.addAttribute(new StatePersistableAttribute("label-y", "" + label.getLayoutY()));
        }
        
        return element;
    }
    
    /**
     * {@inheritDoc}
     */  
    @Override public void restoreState(StatePersistableElement state, StatePersistenceLogger logger, Object... args) throws InvalidPersistedStateException
    {
        if(state != null)
        {
            StatePersistableAttribute layoutX = state.getAttribute("layout-x");
            StatePersistableAttribute layoutY = state.getAttribute("layout-y");
            
            if(layoutX != null && layoutY != null && Helper.isDouble(layoutX.getValue()) && Helper.isDouble(layoutY.getValue()))
            {
                setLayoutX(Double.parseDouble(layoutX.getValue()));
                setLayoutY(Double.parseDouble(layoutY.getValue()));
                
                node.restoreState(state.getElement(node.getPersistableName()), logger);
                
                StatePersistableAttribute labelX = state.getAttribute("label-x");
                StatePersistableAttribute labelY = state.getAttribute("label-y");
                
                if(labelX != null && labelY != null && Helper.isDouble(labelX.getValue()) && Helper.isDouble(labelY.getValue()))
                {
                    this.adjustInitialLabelPosition = false;
                    
                    label.setLayoutX(Double.parseDouble(labelX.getValue()));
                    label.setLayoutY(Double.parseDouble(labelY.getValue()));
                }
            }
            else
            {
                throw new InvalidPersistedStateException("Invalid attributes for FxClientNode, expected layout-x and layout-y: " + state);
            }
        }
    }
        
    /**
     * {@inheritDoc}
     */  
    @Override public void onAfterRemovedFromGraph() {}
    
    /**
     * Duplicate this node.
     * 
     * @return node duplicate
     */
    abstract public FxModelNode duplicate();
    /**
     * Initiate node icon
     */
    abstract protected void initiateImageViewImage();
}
