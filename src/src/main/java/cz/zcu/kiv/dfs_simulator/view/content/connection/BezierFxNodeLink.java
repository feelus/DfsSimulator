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
import javafx.beans.binding.When;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.CubicCurve;

/**
 * Bezier graphic node link. Based on tutorial
 * https://monograff76.wordpress.com/2015/03/23/drag-and-drop-in-javafx-linkingnodes-with-cubic-curves-part-2/
 */
public class BezierFxNodeLink extends FxNodeLink
{
    /**
     * Visible curve
     */
    private final CubicCurve visibleCurve = new CubicCurve();
    /**
     * Invisible curve
     */
    private final CubicCurve invisibleCurve = new CubicCurve();
    
    /* Bezier curve control points */
    private final DoubleProperty controlOrientationX1 = new SimpleDoubleProperty();
    private final DoubleProperty controlOrientationY1 = new SimpleDoubleProperty();
    private final DoubleProperty controlOrientationX2 = new SimpleDoubleProperty();
    private final DoubleProperty controlOrientationY2 = new SimpleDoubleProperty();
    
    /* Bezier curve control points offsets (controls the shape of the curve) */
    private final DoubleProperty controlOffsetX = new SimpleDoubleProperty(100.0);
    private final DoubleProperty controlOffsetY = new SimpleDoubleProperty(50.0);
    
    /**
     * Bezier graphic node link.
     */
    public BezierFxNodeLink()
    {
        this.initVisibleCurve();
        this.initInvisibleCurve();
        this.initMouseHandlers();
        this.bindInvisibleCurve();
        
        this.initContextMenu();
        
        this.addCurves();
    }
    
    /**
     * Add visible and invisible curves to this pane.
     */
    private void addCurves()
    {
        getChildren().add(this.invisibleCurve);
        getChildren().add(0, this.visibleCurve);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public void setStart(Point2D start)
    {
        this.visibleCurve.setStartX(start.getX());
        this.visibleCurve.setStartY(start.getY());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public void setEnd(Point2D end)
    {
        this.visibleCurve.setEndX(end.getX());
        this.visibleCurve.setEndY(end.getY());
    }
    
    /**
     * Initiate visible curve coordinates.
     */
    private void initVisibleCurve()
    {
        this.visibleCurve.setFill(Color.rgb(0, 0, 0, 0));
        this.visibleCurve.setStroke(Color.BLACK);
        
        // determine the orientation of our control points based on the 
        // orientation of start and end points of our bezier curve
        this.controlOrientationX1.bind(new When(
                this.visibleCurve.startXProperty().greaterThan(this.visibleCurve.endXProperty())
        ).then(-1.0).otherwise(1.0));
        
        this.controlOrientationX2.bind(new When(
                this.visibleCurve.startXProperty().greaterThan(this.visibleCurve.endXProperty())
        ).then(1.0).otherwise(-1.0));
        
        // calculate control point 1 X coordinate
        this.visibleCurve.controlX1Property().bind(
                Bindings.add(
                        this.visibleCurve.startXProperty(), 
                        this.controlOffsetX.multiply(this.controlOrientationX1)
                )
        );
        
        // calculate control point 1 Y coordinate
        this.visibleCurve.controlY1Property().bind(
                Bindings.add(
                        this.visibleCurve.startYProperty(),
                        this.controlOffsetY.multiply(this.controlOrientationY1)
                )
        );
        
        // calculate control point 2 X coordinate
        this.visibleCurve.controlX2Property().bind(
                Bindings.add(
                        this.visibleCurve.endXProperty(),
                        this.controlOffsetX.multiply(this.controlOrientationX2)
                )
        );
        
        // calculate control point 2 Y coordinate
        this.visibleCurve.controlY2Property().bind(
                Bindings.add(
                        this.visibleCurve.endYProperty(),
                        this.controlOffsetY.multiply(this.controlOrientationY2)
                )
        );
    }
    
    /**
     * Initiate invisible curve. Invisible curve actually registers all mouse
     * related events (it's thickness is bigger than visible curve). 
     */
    private void initInvisibleCurve()
    {
        this.invisibleCurve.setFill(Color.rgb(0, 0,0 , 0));
        this.invisibleCurve.setStroke(Color.rgb(0, 0, 0, 0));
        this.invisibleCurve.setStrokeWidth(20.0);
    }
    
    /**
     * Set mouse handlers for invisible curve (mouse entered, exited for highlighting).
     */
    private void initMouseHandlers()
    {
        this.invisibleCurve.setOnMouseEntered((MouseEvent event) ->
        {
            setLinkHighlighted(true);
        });
        
        this.invisibleCurve.setOnMouseExited((MouseEvent event) ->
        {
            setLinkHighlighted(false);
        });
    }
    
    /**
     * Bind invisible curve coordinates to visible curve coordinates. 
     * Visible curve is the one that is being moved when nodes move.
     */
    private void bindInvisibleCurve()
    {
        // bind control properties
        this.invisibleCurve.controlX1Property().bind(this.visibleCurve.controlX1Property());
        this.invisibleCurve.controlY1Property().bind(this.visibleCurve.controlY1Property());
        
        this.invisibleCurve.controlX2Property().bind(this.visibleCurve.controlX2Property());
        this.invisibleCurve.controlY2Property().bind(this.visibleCurve.controlY2Property());
        
        // bind start and end properties
        this.invisibleCurve.startXProperty().bind(this.visibleCurve.startXProperty());
        this.invisibleCurve.startYProperty().bind(this.visibleCurve.startYProperty());
        
        this.invisibleCurve.endXProperty().bind(this.visibleCurve.endXProperty());
        this.invisibleCurve.endYProperty().bind(this.visibleCurve.endYProperty());
    }
    
    /**
     * Initiate context menu for invisible curve.
     */
    private void initContextMenu()
    {
        final ContextMenu cm = getContextMenu();
        
        final EventHandler<MouseEvent> mpHandler = (MouseEvent event) -> 
        {
            if(event.getButton() == MouseButton.SECONDARY)
            {
                cm.show(invisibleCurve, event.getScreenX(), event.getScreenY());

                event.consume();
            }
        };
        
        this.invisibleCurve.addEventHandler(MouseEvent.MOUSE_PRESSED, mpHandler);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public void graphicNodeBind(FxModelNode source, FxModelNode target)
    {
        this.visibleCurve.startXProperty().bind(
                Bindings.add(source.layoutXProperty(), (source.getWidth() / 2.0))
        );
        
        this.visibleCurve.startYProperty().bind(
                Bindings.add(source.layoutYProperty(), (source.getHeight() / 2.0))
        );
        
        this.visibleCurve.endXProperty().bind(
                Bindings.add(target.layoutXProperty(), (target.getWidth() / 2.0))
        );
        
        this.visibleCurve.endYProperty().bind(
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
            this.visibleCurve.setStroke(Color.rgb(0, 0, 0, 0.35));
        }
        else
        {
            this.visibleCurve.setStroke(Color.rgb(0, 0, 0, 1));
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public Point2D getStart()
    {
        return new Point2D(this.visibleCurve.getStartX(), this.visibleCurve.getStartY());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public Point2D getEnd()
    {
        return new Point2D(this.visibleCurve.getEndX(), this.visibleCurve.getEndY());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public void setLinkHighlighted(boolean highlighted)
    {
        if(highlighted)
        {
            this.visibleCurve.setStroke(HIGHLIGHT_COLOR);
            this.visibleCurve.setStrokeWidth(HIGHLIGHT_STROKE_WIDTH);
        }
        else
        {
            this.visibleCurve.setStroke(NORMAL_COLOR);
            this.visibleCurve.setStrokeWidth(NORMAL_STROKE_WIDTH);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override protected void adjustLabelPosition()
    {
        double midpointX = (this.visibleCurve.getStartX() + this.visibleCurve.getEndX()) / 2;
        double midpointY = (this.visibleCurve.getStartY() + this.visibleCurve.getEndY()) / 2;
        
        linkLabel.setTranslateX(midpointX);
        linkLabel.setTranslateY(midpointY);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override protected void setLabelPropertyHandlers()
    {
        this.visibleCurve.startXProperty().addListener(listener -> 
        {
            adjustLabelPosition();
        });
        
        this.visibleCurve.startYProperty().addListener(listener -> 
        {
            adjustLabelPosition();
        });
        
        this.visibleCurve.endXProperty().addListener(listener -> 
        {
            adjustLabelPosition();
        });
        
        this.visibleCurve.endYProperty().addListener(listener -> 
        {
            adjustLabelPosition();
        });
    }
}
