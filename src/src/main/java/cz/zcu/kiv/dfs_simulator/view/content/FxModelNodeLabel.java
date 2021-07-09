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

import cz.zcu.kiv.dfs_simulator.helpers.FxHelper;
import cz.zcu.kiv.dfs_simulator.model.LabelException;
import cz.zcu.kiv.dfs_simulator.view.TransparentDraggableEditableTextField;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

/**
 * Label for {@link FxModelNode}.
 */
public class FxModelNodeLabel extends TransparentDraggableEditableTextField
{
    /**
     * Label border color when selected
     */
    private static final Color COLOR_BORDER_SELECTED = Color.rgb(137, 196, 255);
    
    /**
     * Label vertical padding (offset when label above node)
     */
    protected static final int VERTICAL_PADDING = 20;
    /**
     * Minimum distance of node from bounds (if node Y value is less than this,
     * label will be displayed below node)
     */
    protected static final int VERTICAL_THRESHOLD = 50;
    
    /**
     * Label text internal padding
     */
    protected static final double LABEL_PADDING = 3d;
    
    /**
     * Graphic node this label is associated with
     */
    protected final FxModelNode node;
    /**
     * Initial text property listener
     */
    protected final ChangeListener<? super String> initialPositionListener;
    /**
     * Whether {@link #initialPositionListener} has been hooked to label
     */
    protected boolean initialPositionListenerHooked = true;
    
    /**
     * Graphic node label.
     * 
     * @param node associated node
     */
    public FxModelNodeLabel(FxModelNode node)
    {
        super("");
        
        this.initialPositionListener = (observable, oldValue, newValue) -> {
            adjustLabelPosition();
        };
        
        this.node = node;
        this.init();
        this.addNodeIdListener();
    }
    
    /**
     * Initiate label - removes visible border, disables context menu
     * and set's initial label text from node ID.
     */
    private void init()
    {
        // add transparent border
        this.setTransparentBorder();
        
        // disable context menu
        setContextMenu(new ContextMenu());
        
        // we need to make sure that the property changes
        setText(this.node.getLabelText());
        
        Platform.runLater(() -> {
            textProperty().addListener( (observable, oldValue, newValue) -> {
                this.adjustLabelWidth();
            });

            widthProperty().addListener( (observable, oldValue, newValue) -> {
                this.adjustLabelWidth();
            });

            this.node.layoutBoundsProperty().addListener( (observable, oldValue, newValue) -> {
                adjustLabelPosition();
            });
        });
    }
    
    /**
     * Monitor node ID changes.
     */
    private void addNodeIdListener()
    {
        this.node.getNode().nodeIdProperty().addListener( (observable, oldValue, newValue) -> {
            setText(newValue);
        });
    }
    
    /**
     * Adjust label width to fit text.
     */
    protected void adjustLabelWidth()
    {
        Text t = new Text(getText());
        t.setFont(getFont());
        double width = t.getLayoutBounds().getWidth() + 
                getPadding().getLeft() + getPadding().getRight() +
                LABEL_PADDING;

        setPrefWidth(width);
        positionCaret(getCaretPosition());
    }
    
    /**
     * Adjust label position based on node coordinates.
     */
    public void adjustLabelPosition()
    {
        if(this.initialPositionListenerHooked)
        {
            this.initialPositionListenerHooked = false;
            textProperty().removeListener(this.initialPositionListener);
        }
        
        // @TODO hardcoded width offset since initially the text field has
        // wrong width before it is adjusted
        double labelX = this.node.getLayoutX() + 
                (this.node.getLayoutBounds().getWidth() / 2) - (getWidth() / 2) - (LABEL_PADDING * 2) - (BorderWidths.DEFAULT.getLeft() * 2);
        
        double labelY;
        
        if(this.node.getLayoutY() < VERTICAL_THRESHOLD)
        {
            labelY = this.node.getBoundsInParent().getMaxY();
        }
        else
        {
            labelY = this.node.getBoundsInParent().getMinY() - VERTICAL_PADDING;
        }
        
        setLayoutX(labelX);
        setLayoutY(labelY);
    }
    
    /**
     * Get graphic node.
     * 
     * @return graphic node
     */
    public FxModelNode getNode()
    {
        return this.node;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override protected void setVisibleBorder()
    {
        setBorder(new Border(new BorderStroke(COLOR_BORDER_SELECTED, 
                BorderStrokeStyle.DASHED, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override protected void setTransparentBorder()
    {
        setBorder(new Border(new BorderStroke(Color.TRANSPARENT, 
                BorderStrokeStyle.DASHED, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public void removeFromParent()
    {
        
    }
    
    /**
     * {@inheritDoc}
     */
    @Override protected boolean commitChanges(String text)
    {
        try
        {
            this.node.setLabelText(getText());
            
            return true;
        }
        catch (LabelException ex)
        {
            Alert a = FxHelper.getErrorDialog("Label error", "Unable to set label", ex.getMessage());
            a.showAndWait();
            
            return false;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override protected String revertChanges()
    {
        return this.node.getLabelText();
    }
        
    /**
     * {@inheritDoc}
     */
    @Override public void highlight()
    {
        this.setVisibleBorder();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public void removeHighlight()
    {
        this.setTransparentBorder();
    }
}
