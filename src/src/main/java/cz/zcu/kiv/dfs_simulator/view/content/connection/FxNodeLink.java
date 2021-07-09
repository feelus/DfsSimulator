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

package cz.zcu.kiv.dfs_simulator.view.content.connection;

import cz.zcu.kiv.dfs_simulator.view.content.FxModelNode;
import cz.zcu.kiv.dfs_simulator.view.content.events.FxNodeLinkEvent;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Point2D;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

/**
 * Graphical (visible) link between two nodes {@link FxModelNode}.
 */
abstract public class FxNodeLink extends AnchorPane
{
    /**
     * Link stroke width (link thickness)
     */
    protected static final double NORMAL_STROKE_WIDTH = 1.0;
    /**
     * Link stroke width when highlighted
     */
    protected static final double HIGHLIGHT_STROKE_WIDTH = 3.0;
    
    /**
     * Highlight color
     */
    protected static final Color HIGHLIGHT_COLOR = Color.rgb(0, 125, 252);
    /**
     * Regular color
     */
    protected static final Color NORMAL_COLOR = Color.BLACK;
    /**
     * Link label color
     */
    protected static final Color LABEL_COLOR = Color.rgb(5, 181, 5);
    
    /**
     * Link label
     */
    protected Label linkLabel;
    
    /**
     * Initial listener waiting for label to get it's size (width/height)
     * after being displayed
     */
    private ChangeListener initialLabelListener;
    
    /**
     * Context item, enables or disable label display
     */
    protected RadioMenuItem displayLabelItem;
    
    /**
     * Graphical (visible) node link.
     */
    public FxNodeLink()
    {
        setPickOnBounds(false);
    }
    
    /**
     * Returns link characteristics menu item.
     * 
     * @return characteristics menu item
     */
    protected MenuItem getLinkCharacteristicsContextItem()
    {
        MenuItem characteristics = new MenuItem("Link characteristics");
        
        characteristics.setOnAction(actionEvent -> {
            linkCharacteristicsAction();
        });
        
        return characteristics;
    }
    
    /**
     * Returns remove link menu item.
     * 
     * @return remove link menu item
     */
    protected MenuItem getRemoveContextItem()
    {
        MenuItem removeLink = new MenuItem("Remove link");
        
        removeLink.setOnAction(actionEvent -> {
            removeFromParentAction();
        });
        
        return removeLink;
    }
    
    /**
     * Returns alter link menu item.
     * 
     * @return alter link menu item
     */
    protected MenuItem getAlterContextItem()
    {
        MenuItem alterLink = new MenuItem("Alter link");
        
        alterLink.setOnAction(actionEvent -> {
            alterLinkAction();
        });
        
        return alterLink;
    }
    
    /**
     * Returns display label menu item.
     * 
     * @return display label menu item
     */
    protected MenuItem getDisplayLabelContextItem()
    {
        this.displayLabelItem = new RadioMenuItem("Display label");
        
        this.displayLabelItem.setOnAction(actionEvent -> {
            setLabelVisibleAction(displayLabelItem.isSelected());
        });
        
        this.displayLabelItem.setSelected(true);
        
        return this.displayLabelItem;
    }
    
    /**
     * Action that is triggered when this link is removed from parent
     * (no longer visible). Fires event {@link FxNodeLinkEvent#REMOVE_NODE_LINK}.
     */
    protected void removeFromParentAction()
    {
        this.fireEvent(new FxNodeLinkEvent(FxNodeLinkEvent.REMOVE_NODE_LINK));
    }
    
    /**
     * Action that is triggered when this link was deleted by user.
     */
    public void handleUserDeleteLink()
    {
        this.removeFromParentAction();
    }
    
    /**
     * Action taken node link alter dialog is requested. Fires
     * event {@link FxNodeLinkEvent#ALTER_NODE_LINK}.
     */
    protected void alterLinkAction()
    {
        this.fireEvent(new FxNodeLinkEvent(FxNodeLinkEvent.ALTER_NODE_LINK));
    }
    
    /**
     * Action taken when user wants to edit node link.
     */
    public void handleUserAlterLink()
    {
        this.alterLinkAction();
    }
    
    /**
     * Change label visible flag.
     * 
     * @param visible visible flag
     */
    protected void setLabelVisibleAction(boolean visible)
    {
        this.linkLabel.setVisible(visible);
    }
    
    /**
     * Action taken when link characteristic dialog is requested. Fires event
     * {@link FxNodeLinkEvent#REQUEST_CHARACTERISTIC_DIALOG}.
     */
    protected void linkCharacteristicsAction()
    {
        fireEvent(new FxNodeLinkEvent(FxNodeLinkEvent.REQUEST_CHARACTERISTIC_DIALOG));
    }
    
    /**
     * Get populated context menu with alter, link characteristic, display label
     * and remove link menu items.
     * 
     * @return context menu
     */
    protected ContextMenu getContextMenu()
    {
        final ContextMenu cm = new ContextMenu();
        
        cm.getItems().add(this.getAlterContextItem());
        cm.getItems().add(this.getLinkCharacteristicsContextItem());
        cm.getItems().add(this.getDisplayLabelContextItem());
        cm.getItems().add(this.getRemoveContextItem());
        
        return cm;
    }
    
    /**
     * Returns link label.
     * 
     * @return link label
     */
    public String getLinkLabel()
    {
        return linkLabel.getText();
    }
    
    /**
     * Set link label.
     * 
     * @param textLabel link label
     */
    public void setLinkLabel(String textLabel)
    {
        if(textLabel == null)
        {
            return;
        }
        
        if(linkLabel != null)
        {
            getChildren().remove(linkLabel);
        }
        
        linkLabel = new Label(textLabel);
        linkLabel.setTextFill(LABEL_COLOR);
        
        // make it invisible before adjusting it's coordinates
        linkLabel.setVisible(false);        
        // add label to children to calculate its width
        getChildren().add(linkLabel);
        // make it mouse transparent
        linkLabel.setMouseTransparent(true);
        
        // wait for width property to change
        this.initialLabelListener = (ObservableValue observable, Object oldValue, Object newValue) ->
        {
            adjustLabelPosition();
            setLabelPropertyHandlers();
            
            linkLabel.widthProperty().removeListener(initialLabelListener);
            linkLabel.setVisible(true);
        };
        
        linkLabel.widthProperty().addListener(this.initialLabelListener);
    }
    
    /**
     * Bind pair of nodes to link instance (instance should originate 
     * from {@code source} coordinates and end at {@code target} coordinates).
     * 
     * @param source source node
     * @param target target node
     */
    abstract public void graphicNodeBind(FxModelNode source, FxModelNode target);
    
    /**
     * Set link origin coordinates.
     * 
     * @param start coordinates
     */
    abstract public void setStart(Point2D start);
    /**
     * Get link origin coordinates.
     * 
     * @return start coordinates
     */
    abstract public Point2D getStart();
    /**
     * Set link end coordinates.
     * 
     * @param end coordinates
     */
    abstract public void setEnd(Point2D end);
    /**
     * Get link end coordinates.
     * 
     * @return end coordinates
     */
    abstract public Point2D getEnd();
    
    /**
     * Set link fading status.
     * 
     * @param fading fading status
     */
    abstract public void setLinkFading(boolean fading);
    /**
     * Set link highlighted status.
     * 
     * @param highlighted highlighted status
     */
    abstract public void setLinkHighlighted(boolean highlighted);
    /**
     * Link position is adjusted to it's correct position along the link.
     */
    abstract protected void adjustLabelPosition();
    /**
     * Set handlers for label position based on link position (label's
     * position is based on link's position)
     */
    abstract protected void setLabelPropertyHandlers();
    
}
