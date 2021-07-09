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
import javafx.beans.binding.Bindings;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

/**
 * Line graphic node link.
 */
public class LineFxNodeLink extends FxNodeLink
{    
    /**
     * Visible line - displayed
     */
    private final Line visibleLine = new Line();
    /**
     * Invisible line - used for mouse events
     */
    private final Line invisibleLine = new Line();
    
    /**,
     * Mouse entered event over invisible line
     */
    private EventHandler<MouseEvent> onMouseEntered;
    /**
     * Mouse exited from invisible line
     */
    private EventHandler<MouseEvent> onMouseExited;
    
    /**
     * Line graphic node link.
     */
    public LineFxNodeLink()
    {
        this.initInvisibleLine();
        this.initMouseHandlers();
        this.setMouseHandlers();
        
        this.bindInvisibleLine();
        
        this.addLines();
        this.initContextMenu();
    }
        
    /**
     * Initiate link context menu (right click from invisible line).
     */
    private void initContextMenu()
    {
        final ContextMenu cm = getContextMenu();
        
        final EventHandler<MouseEvent> mpHandler = (MouseEvent event) -> 
        {
            if(event.getButton() == MouseButton.SECONDARY)
            {
                cm.show(invisibleLine, event.getScreenX(), event.getScreenY());
                
                event.consume();
            }
        };
        
        this.invisibleLine.addEventHandler(MouseEvent.MOUSE_PRESSED, mpHandler);
    }
    
    /**
     * Add visible and invisible lines to pane.
     */
    private void addLines()
    {
        getChildren().add(this.invisibleLine);
        getChildren().add(0, this.visibleLine);
    }
    
    /**
     * Initiate invisible line.
     */
    private void initInvisibleLine()
    {
        this.invisibleLine.setStroke(Color.rgb(0, 0, 0, 0));
        this.invisibleLine.setStrokeWidth(20.0);
    }
    
    /**
     * Initiate line mouse handlers (highlighting).
     */
    private void initMouseHandlers()
    {
        this.onMouseEntered = (MouseEvent event) ->
        {
            setLinkHighlighted(true);
        };
        
        this.onMouseExited = (MouseEvent event) ->
        {
            setLinkHighlighted(false);
        };
    }
    
    /**
     * Set mouse handlers (highlighting).
     */
    private void setMouseHandlers()
    {
        this.invisibleLine.setOnMouseEntered(this.onMouseEntered);
        this.invisibleLine.setOnMouseExited(this.onMouseExited);
    }
    
    /**
     * Bind invisible line coordinates to those of visible line.
     */
    private void bindInvisibleLine()
    {
        this.invisibleLine.startXProperty().bind(this.visibleLine.startXProperty());
        this.invisibleLine.startYProperty().bind(this.visibleLine.startYProperty());
        
        this.invisibleLine.endXProperty().bind(this.visibleLine.endXProperty());
        this.invisibleLine.endYProperty().bind(this.visibleLine.endYProperty());
    }
     
    /**
     * {@inheritDoc}
     */
    @Override protected void adjustLabelPosition()
    {
        double midpointX = (this.visibleLine.getStartX() + this.visibleLine.getEndX()) / 2;
        double midpointY = (this.visibleLine.getStartY() + this.visibleLine.getEndY()) / 2;
        
        double dX = this.visibleLine.getEndX() - this.visibleLine.getStartX();
        double dY = this.visibleLine.getEndY() - this.visibleLine.getStartY();
        
        double offsetDistance = 5;
        
        double scale = offsetDistance / Math.sqrt(dX * dX + dY * dY);
        double x3;
        double y3;
        
        if(this.visibleLine.getStartX() >= this.visibleLine.getEndX())
        {
            x3 = dY * scale;
            y3 = -dX * scale;
        }
        else
        {
            x3 = -dY * scale;
            y3 = dX * scale;
        }
        
        linkLabel.setTranslateX(midpointX + x3);
        linkLabel.setTranslateY(midpointY + y3);
        
        // @TODO this could be calculated beforehand and we could save one translation
        // operation
        double labelMidpointX = (linkLabel.getBoundsInParent().getMinX() + 
                linkLabel.getBoundsInParent().getMaxX()) / 2;
        double labelMidpointXDistance = midpointX - labelMidpointX;
        
        linkLabel.setTranslateX(midpointX + labelMidpointXDistance);
        
    }
     
    /**
     * {@inheritDoc}
     */
    @Override public Point2D getStart()
    {
        return new Point2D(this.visibleLine.getStartX(), this.visibleLine.getEndX());
    }
     
    /**
     * {@inheritDoc}
     */
    @Override public Point2D getEnd()
    {
        
        return new Point2D(this.visibleLine.getEndX(), this.visibleLine.getEndY());
    }
     
    /**
     * {@inheritDoc}
     */
    @Override public void setLinkHighlighted(boolean highlighted)
    {
        if(highlighted)
        {
            this.visibleLine.setStroke(HIGHLIGHT_COLOR);
            this.visibleLine.setStrokeWidth(HIGHLIGHT_STROKE_WIDTH);
        }
        else
        {
            this.visibleLine.setStroke(NORMAL_COLOR);
            this.visibleLine.setStrokeWidth(NORMAL_STROKE_WIDTH);
        }
    }
         
    /**
     * {@inheritDoc}
     */
    @Override protected void setLabelPropertyHandlers()
    {
        this.visibleLine.startXProperty().addListener(listener -> 
        {
            adjustLabelPosition();
        });
        
        this.visibleLine.startYProperty().addListener(listener -> 
        {
            adjustLabelPosition();
        });
        
        this.visibleLine.endXProperty().addListener(listener -> 
        {
            adjustLabelPosition();
        });
        
        this.visibleLine.endYProperty().addListener(listener -> 
        {
            adjustLabelPosition();
        });
    }
         
    /**
     * {@inheritDoc}
     */
    @Override public void setStart(Point2D start)
    {
        this.visibleLine.setStartX(start.getX());
        this.visibleLine.setStartY(start.getY());
    }
     
    /**
     * {@inheritDoc}
     */
    @Override public void setEnd(Point2D end)
    {
        this.visibleLine.setEndX(end.getX());
        this.visibleLine.setEndY(end.getY());
    }
     
    /**
     * {@inheritDoc}
     */
    @Override public void graphicNodeBind(FxModelNode source, FxModelNode target)
    {
        this.visibleLine.startXProperty().bind(
                Bindings.add(source.layoutXProperty(), (source.getWidth() / 2.0))
        );

        this.visibleLine.startYProperty().bind(
                Bindings.add(source.layoutYProperty(), (source.getHeight() / 2.0))
        );

        this.visibleLine.endXProperty().bind(
                Bindings.add(target.layoutXProperty(), (target.getWidth() / 2.0))
        );

        this.visibleLine.endYProperty().bind(
                Bindings.add(target.layoutYProperty(), (target.getHeight() / 2.0))
        );
    }
     
    /**
     * {@inheritDoc}
     */
    @Override public void setLinkFading(boolean fading)
    {
        if(fading)
        {
            this.visibleLine.setStroke(Color.rgb(0, 0, 0, 0.35));
        }
        else
        {
            this.visibleLine.setStroke(Color.rgb(0, 0, 0, 1));
        }
    }
}
