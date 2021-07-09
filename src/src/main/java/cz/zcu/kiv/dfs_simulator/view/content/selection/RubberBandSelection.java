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

package cz.zcu.kiv.dfs_simulator.view.content.selection;

import cz.zcu.kiv.dfs_simulator.view.content.events.MultiNodeSelectionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;

/**
 * Rubber band selection - rectangle selection by dragging mouse, allows
 * adding and removing nodes from existing selection.
 */
public class RubberBandSelection
{
    /**
     * Selecting rectangle stroke color
     */
    private static final Color STROKE_COLOR = Color.rgb(51, 153, 255);
    /**
     * Selecting rectangle fill color
     */
    private static final Color FILL_COLOR = Color.rgb(51, 153, 255, 0.15);
    
    /**
     * Selection origin X coordinate
     */
    private double originX;
    /**
     * Selection origin Y coordinate
     */
    private double originY;
    
    /**
     * Selection rectangle
     */
    private final Rectangle selection;
    /**
     * Multi selection container
     */
    private final MultiNodeSelectionContainer selectionContainer;
    /**
     * Pane on which we are selecting nodes
     */
    private final Pane pane;
    
    /**
     * Currently selecting (dragging selection)
     */
    private boolean selecting = false;
    
    /**
     * Mouse pressed handler
     */
    private EventHandler<MouseEvent> onMousePressedHandler;
    /**
     * Mouse dragged handler
     */
    private EventHandler<MouseEvent> onMouseDraggedHandler;
    /**
     * Mouse released handler
     */
    private EventHandler<MouseEvent> onMouseReleasedHandler;
    
    /**
     * Rubber-band selection on {@code pane} allowing selection of objects
     * implementing {@link MultiSelectable}.
     * 
     * @param pane pane
     * @param selectionContainer selection container
     */
    public RubberBandSelection(Pane pane, MultiNodeSelectionContainer selectionContainer)
    {
        this.pane = pane;
        this.selectionContainer = selectionContainer;
        
        this.selection = new Rectangle(0, 0, 0, 0);
        this.selection.setStrokeWidth(1.0);
        this.selection.setStroke(STROKE_COLOR);
        this.selection.setStrokeLineCap(StrokeLineCap.ROUND);
        this.selection.setFill(FILL_COLOR);
        
        this.initPaneEventHandlers();
        
        this.setPaneEventHandlers();
        this.setMultiSelectionDragHandler();
        
        this.setPaneKeyHandlers();
        
        this.setPaneStylesheets();
    }
    
    /**
     * Initiate mouse handlers.
     */
    private void initPaneEventHandlers()
    {
        this.onMousePressedHandler = (MouseEvent event) ->
        {
            // check if we are alredy in a selection state
            if(selecting)
            {
                return;
            }

            // store originating coordinates
            originX = event.getSceneX();
            originY = event.getSceneY();
            
            // add selection to pane
            pane.getChildren().add(selection);
            
            // set coordinates to selection rectangle
            Point2D parentOrigin = pane.sceneToLocal(originX, originY);
            
            selection.setX(parentOrigin.getX());
            selection.setY(parentOrigin.getY());
            selection.setWidth(0);
            selection.setHeight(0);
                        
            // prevent additional events entering selecting mode
            selecting = true;
            
            // dont propagate further
            event.consume();
        };
        
        this.onMouseDraggedHandler = (MouseEvent event) ->
        {
            double deltaX = event.getSceneX() - originX;
            double deltaY = event.getSceneY() - originY;
            
            Point2D parentOrigin;
            if(deltaX < 0)
            {
                // recalculate coordinates
                parentOrigin = pane.sceneToLocal(event.getSceneX(), event.getSceneY());
                
                // adjust selection coordinate
                selection.setX(parentOrigin.getX());
                
                // convert delta X to a positive number
                deltaX *= -1;
            }
            
            if(deltaY < 0)
            {
                // recalculate coordinates
                parentOrigin = pane.sceneToLocal(event.getSceneX(), event.getSceneY());
                
                // adjust selection coordinate
                selection.setY(parentOrigin.getY());
                
                // convert delta Y to a positive number
                deltaY *= -1;
            }
            
            selection.setWidth(deltaX);
            selection.setHeight(deltaY);

            event.consume();
        };
        
        this.onMouseReleasedHandler = (MouseEvent event) ->
        {
            // check if we are altering an existing selection
            if(!event.isShiftDown() && !event.isControlDown())
            {
                selectionContainer.deselectAll();
            }
            
            // find nodes that are contained in our selection rectangle
            pane.getChildren().stream().forEach((n) ->
            {
                if(n instanceof MultiSelectableDraggable)
                {
                    MultiSelectableDraggable selectableNode = (MultiSelectableDraggable) n;
                    
                    if(n.getBoundsInParent().intersects(selection.getBoundsInParent()))
                    {
                        // adding to an existing selection
                        if(event.isShiftDown())
                        {
                            selectionContainer.selectNode(selectableNode);
                        }
                        // altering an existing selection
                        else if(event.isControlDown())
                        {
                            if(selectionContainer.containtsNode(selectableNode))
                            {
                                selectionContainer.deselectNode(selectableNode);
                            }
                            else
                            {
                                selectionContainer.selectNode(selectableNode);
                            }
                        }
                        else
                        {
                            selectionContainer.selectNode(selectableNode);
                        }
                    }
                }
            });
            
            if(selectionContainer.size() > 0)
            {
                setPaneFocus();
            }
            
            resetSelection();
            pane.getChildren().remove(selection);
            
            selecting = false;
            
            event.consume();
        };
    }
    
    /**
     * Reset selection rectangle.
     */
    private void resetSelection()
    {
        this.selection.setX(0);
        this.selection.setY(0);
        this.selection.setWidth(0);
        this.selection.setHeight(0);
    }
    
    /**
     * Set pane mouse handlers.
     */
    private void setPaneEventHandlers()
    {
        this.pane.addEventHandler(MouseEvent.MOUSE_PRESSED, this.onMousePressedHandler);
        this.pane.addEventHandler(MouseEvent.MOUSE_DRAGGED, this.onMouseDraggedHandler);
        this.pane.addEventHandler(MouseEvent.MOUSE_RELEASED, this.onMouseReleasedHandler);
    }
    
    /**
     * Set multi selection drag handler. Checks, if all selected
     * nodes can be moved by given offsets - either all nodes move or none does.
     */
    private void setMultiSelectionDragHandler()
    {
        this.pane.addEventHandler(MultiNodeSelectionEvent.DRAG_SELECTION, dragEvent -> 
        {
            if(dragEvent instanceof MultiNodeSelectionEvent)
            {
                MultiNodeSelectionEvent mEvent = (MultiNodeSelectionEvent) dragEvent;

                // prevent whole selection from dragging if one of the selected nodes
                // cannot move (eg. is hitting scene borders)
                boolean canAllNodesMoveX = true;
                boolean canAllNodesMoveY = true;
                
                for(MultiSelectable n : selectionContainer.getSelection())
                {
                    if(n instanceof MultiSelectableDraggable)
                    {
                        final MultiSelectableDraggable draggableNode = (MultiSelectableDraggable) n;
                        
                        if (!draggableNode.canOffsetNodeX(mEvent.getDragOffsetX()))
                        {
                            canAllNodesMoveX = false;
                        }

                        if (!draggableNode.canOffsetNodeY(mEvent.getDragOffsetY()))
                        {
                            canAllNodesMoveY = false;
                        }

                        if (!canAllNodesMoveX && !canAllNodesMoveY)
                        {
                            break;
                        }
                    }
                }
                
                // only move if all nodes can move
                if(canAllNodesMoveX || canAllNodesMoveY)
                {
                    for (MultiSelectable n : selectionContainer.getSelection())
                    {
                        if(n instanceof MultiSelectableDraggable)
                        {
                            if (canAllNodesMoveX)
                            {
                                ((MultiSelectableDraggable) n).offsetNodeX(mEvent.getDragOffsetX());
                            }

                            if (canAllNodesMoveY)
                            {
                                ((MultiSelectableDraggable) n).offsetNodeY(mEvent.getDragOffsetY());
                            }
                        }
                    }
                }
            }
            
            dragEvent.consume();
        });
    }
    
    /**
     * Focus underlying pane.
     */
    private void setPaneFocus()
    {
        this.pane.setFocusTraversable(true);
        this.pane.requestFocus();
    }
    
    /**
     * Set keyboard handlers for underlying pane.
     */
    private void setPaneKeyHandlers()
    {
        this.pane.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> 
        {
            // empty selection
            if(selectionContainer.size() == 0)
            {
                return;
            }
            
            // delete all selected nodes
            if(keyEvent.getCode() == KeyCode.DELETE)
            {
                selectionContainer.getSelection().stream().forEach((n) ->
                {
                    n.removeFromParent();
                });
                
                selectionContainer.clear();
            }
        });
    }
    
    /**
     * Add CSS to underlying pane.
     */
    private void setPaneStylesheets()
    {
        this.pane.getStylesheets().add(
                getClass().getClassLoader().getResource("css/RubberBandSelection.css").toExternalForm());
    }
}
