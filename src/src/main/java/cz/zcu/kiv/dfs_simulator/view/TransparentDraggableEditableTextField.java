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

import cz.zcu.kiv.dfs_simulator.helpers.FxHelper;
import cz.zcu.kiv.dfs_simulator.view.content.selection.Highlightable;
import cz.zcu.kiv.dfs_simulator.view.content.selection.MultiSelectableDraggable;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

/**
 * Draggable, editable text field with transparent background.
 * @author martin
 */
public abstract class TransparentDraggableEditableTextField extends TextField implements MultiSelectableDraggable, Highlightable
{
    /**
     * Mouse X offset when dragging
     */
    private double dDragX = 0;
    /**
     * Mouse Y offset when dragging
     */
    private double dDragY = 0;
    
    /**
     * Draggable, editable text field with transparent background.
     * 
     * @param initialText initial text
     */
    public TransparentDraggableEditableTextField(String initialText)
    {
        super(initialText);
        
        this.fxmlInit();
        this.setMouseListeners();
        this.setFocusHandler();
        this.setDragHandlers();
    }
    
    /**
     * Load FXMl schema.
     */
    private void fxmlInit()
    {
        FxHelper.loadFXMLAndSetController(getClass().getClassLoader().getResource("fxml/view/TransparentDraggableTextField.fxml"), this);
        
        setEditable(false);
        setFocused(false);
        deselect();
        
        setFocusTraversable(false);
    }
    
    /**
     * Set edit mouse listeners.
     */
    private void setMouseListeners()
    {
        // enable editable
        setOnMouseClicked(event -> {
            if(event.getClickCount() == 2)
            {
                toggleEditMode(true, true);
            }
        });
        
        // confirm change or revert changes
        setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.ENTER)
            {
                toggleEditMode(false, true);
            }
            else if(event.getCode() == KeyCode.ESCAPE)
            {
                toggleEditMode(false, false);
            }
        });
    }
    
    /**
     * Set focus handler.
     */
    private void setFocusHandler()
    {
        focusedProperty().addListener( (observable, oldValue, newValue) -> {
            this.toggleFocus(newValue);
        });
    }
    
    /**
     * Set drag handlers.
     */
    private void setDragHandlers()
    {
        setOnMousePressed(event -> {
            dDragX = event.getX();
            dDragY = event.getY();
        });
        
        setOnMouseDragged(event -> {
            if(!isEditable())
            {
                deselect();
                
                double newX = getLayoutX() + event.getX() - dDragX;
                double newY = getLayoutY() + event.getY() - dDragY;
                
                if (newX > 0 && newX < getScene().getWidth())
                {
                    setLayoutX(newX);
                }

                if (newY > 0 && newY < getScene().getHeight())
                {
                    setLayoutY(newY);
                }
            }
        });
    }
    
    /**
     * Toggle focus.
     * 
     * @param focus focus state
     */
    private void toggleFocus(boolean focus)
    {
        if(!focus && isEditable())
        {
            toggleEditMode(false, true);
        }
    }
    
    /**
     * Toggle edit mode, if text changed, changes will be saved.
     * 
     * @param edit edit mode
     * @param commit do commit
     */
    private void toggleEditMode(boolean edit, boolean commit)
    {
        if(!edit && isEditable())
        {
            setEditable(false);
            setFocused(false);
            deselect();
            
            if(commit)
            {
                if(!this.commitChanges(getText()))
                {
                    setText(this.revertChanges());
                }
            }
            else
            {
                setText(this.revertChanges());
            }
            
            setTransparentBorder();
        }
        else if(edit)
        {
            setVisibleBorder();
            
            setEditable(true);
            setFocused(true);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public void setMultiSelectableState(boolean state)
    {
        
    }
        
    /**
     * {@inheritDoc}
     */
    @Override public boolean canOffsetNodeX(double offsetX)
    {
        return ( (getLayoutX() + offsetX) > 0 );
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
    @Override public void offsetNodeX(double offsetX)
    {
        // node is not present in any scene but still 
        // got the event
        if(getScene() == null)
        {
            return;
        }
        
        double newX = getLayoutX() + offsetX;
        
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
     * {@inheritDoc}
     */
    @Override public void offsetNodeY(double offsetY)
    {
        // node is not present in any scene but still 
        // got the event
        if(getScene() == null)
        {
            return;
        }
        
        double newY = getLayoutY() + offsetY;
        
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
     * Change label.
     * 
     * @param text new label text
     * @return true if changes can be commited, false otherwise
     */
    abstract protected boolean commitChanges(String text);
    /**
     * Restore original text.
     * 
     * @return original text
     */
    abstract protected String revertChanges();
    
    /**
     * Display visible border.
     */
    abstract protected void setVisibleBorder();
    /**
     * Display transparent border.
     */
    abstract protected void setTransparentBorder();
}
