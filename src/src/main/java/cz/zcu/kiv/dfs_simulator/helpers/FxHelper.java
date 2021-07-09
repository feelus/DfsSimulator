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

package cz.zcu.kiv.dfs_simulator.helpers;

import cz.zcu.kiv.dfs_simulator.model.ByteSize;
import cz.zcu.kiv.dfs_simulator.model.ByteSizeUnits;
import cz.zcu.kiv.dfs_simulator.model.ByteSpeed;
import cz.zcu.kiv.dfs_simulator.model.ByteSpeedUnits;
import cz.zcu.kiv.dfs_simulator.view.simulation.SimulationResultByteSize;
import cz.zcu.kiv.dfs_simulator.view.simulation.SimulationResultByteSpeed;
import cz.zcu.kiv.dfs_simulator.view.simulation.SimulationResultTimeUnit;
import static java.awt.SystemColor.text;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

/**
 * General purpose methods related to GUI.
 */
public class FxHelper
{
    /**
     * Return an instance of {@link FileChooser} using 
     * initial directory set from {@link SimulatorPreferences#getLastOpenDir()}
     * if not null or user's home directory.
     * 
     * @return an instance of {@link FileChooser} with initial directory set
     */
    public static FileChooser getSimulatorFileChooser()
    {
        FileChooser fileChooser = new FileChooser();

        // check if last open path exists 
        String lastOpenPath = SimulatorPreferences.getLastOpenDir();
        File lastOpenFile;
        if(lastOpenPath != null && (lastOpenFile = new File(lastOpenPath)).exists())
        {
            fileChooser.setInitialDirectory(
                    lastOpenFile
            );
        }
        else
        {
            fileChooser.setInitialDirectory(
                    new File(System.getProperty("user.home"))
            );
        }
        
        return fileChooser;
    }
    
    /**
     * Attempts to load an FXML file from {@code url} and set {@code controller}
     * as it's controller.
     * 
     * @param url URL to FXML file
     * @param controller instance of FXML controller
     */
    public static void loadFXMLAndSetController(URL url, Object controller)
    {
        FXMLLoader loader = new FXMLLoader(url);

        loader.setRoot(controller);
        loader.setController(controller);

        try
        {
            loader.load();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Checks whether mouse event {@code e} happened while mouse
     * was hovering node {@code n}.
     * 
     * @param e an instance of {@link javafx.scene.input.MouseEvent}
     * @param n an instance of {@link javafx.scene.Node}
     * @return true if event happened while hovering node, false otherwise
     */
    public static boolean isMouseEventOverNode(MouseEvent e, Node n)
    {
        return isPointInNodeBounds(e.getScreenX(), e.getScreenY(), n);
    }
    
    /**
     * Checks whether drag event {@code e} happened while mouse
     * was hovering node {@code n}.
     * 
     * @param e an instance of {@link javafx.scene.input.DragEvent}
     * @param n an instance of {@link javafx.scene.Node}
     * @return true if event happened while hovering node, false otherwise
     */
    public static boolean isDragEventOverNode(DragEvent e, Node n)
    {
        return isPointInNodeBounds(e.getScreenX(), e.getScreenY(), n);
    }
    
    /**
     * Checks whether point defined by {@code x} and {@code y} is 
     * in node's {@code n} bounds.
     * 
     * @param x x coordinate
     * @param y y coordinate
     * @param n an instance of {@link javafx.scene.Node}
     * @return true if point lies in node's bounds, false otherwise
     */
    public static boolean isPointInNodeBounds(double x, double y, Node n)
    {
        Bounds localBounds = n.getBoundsInLocal();
        Bounds screenBounds = n.localToScreen(localBounds);
        
        return (x >= screenBounds.getMinX() && 
                x <= screenBounds.getMaxX() && 
                y >= screenBounds.getMinY() && 
                y <= screenBounds.getMaxY());
    }
    
    /**
     * Calculates slope of a line segment.
     * 
     * @param x1 origin x coordinate
     * @param y1 origin y coordinate
     * @param x2 end x coordinate
     * @param y2 end y coordinate
     * @return line segment slope
     */
    public static double getLineSlope(double x1, double y1, double x2, double y2)
    {
        double div = (x2 - x1);
        
        if(div == 0)
        {
            div += 1;
        }
        
        return ((y2 - y1) / (div));
    }
    
    /**
     * Calculates line segment length in Euclidian space.
     * 
     * @param sX origin x coordinate
     * @param sY origin y coordinate
     * @param eX end x coordinate
     * @param eY end y coordinate
     * @return line segment length
     */
    public static double getLineLength(double sX, double sY, double eX, double eY)
    {
        return Math.abs(
                Math.sqrt(
                        Math.pow(eX - sX, 2) + Math.pow(eY - sY, 2)
                )
        );
    }
    
    /**
     * Calculates slope of a line segment.
     * 
     * @param line an instance of {@link javafx.scene.shape.Line}
     * @return line segment slope
     */
    public static double getFxLineSlope(Line line)
    {
        return FxHelper.getLineSlope(line.getStartX(), line.getStartY(), 
                line.getEndX(), line.getEndY());
    }
    
    /**
     * Calculates line segment length in Euclidian space.
     * 
     * @param line an instance of {@link javafx.scene.shape.Line}
     * @return line segment length
     */
    public static double getFxLineLength(Line line)
    {
        return FxHelper.getLineLength(line.getStartX(), line.getStartY(), 
                line.getEndX(), line.getEndY());
    }
    
    /**
     * Approximates length of {@code text} when displayed as 
     * {@link javafx.scene.text.Text}.
     * 
     * @param text text string
     * @return approximated length
     */
    public static double measureTextWidth(String text)
    {
        final Text fxText = new Text(text);
        new Scene(new Group(fxText));
        
        fxText.applyCss();
        
        return fxText.getLayoutBounds().getWidth();
    }
    
    /**
     * Dialog factory method.
     * 
     * @param type dialog type
     * @param title dialog title
     * @param header dialog header text
     * @param content dialog content text
     * @return an instance of {@link javafx.scene.control.Alert} with given parameters
     */
    public static Alert getDialog(Alert.AlertType type, String title, String header, String content)
    {
        Alert alert = new Alert(type);
        
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        
        return alert;
    }
    
    /**
     * Return an instance of {@link javafx.scene.control.Alert} with type
     * {@code Alert.AlertType.ERROR}.
     * 
     * @param title dialog title
     * @param header dialog header text
     * @param content dialog content text
     * @return an instance of {@link javafx.scene.control.Alert} with type of
     * {@code Alert.AlertType.ERROR}
     */
    public static Alert getErrorDialog(String title, String header, String content)
    {
        return getDialog(Alert.AlertType.ERROR, title, header, content);
    }
    
    /**
     * Return an instance of {@link javafx.scene.control.Alert} with type
     * {@code Alert.AlertType.CONFIRMATION}.
     * 
     * @param title dialog title
     * @param header dialog header text
     * @param content dialog content text
     * @return an instance of {@link javafx.scene.control.Alert} with type of
     * {@code Alert.AlertType.CONFIRMATION}
     */
    public static Alert getConfirmationDialog(String title, String header, String content)
    {
        return getDialog(Alert.AlertType.CONFIRMATION, title, header, content);
    }
    
    /**
     * Returns a value of largest prefix that will be greater than 1, for example
     * if input is of size 1100 B, it will be converted to string 1.1 (suffix kB not appended).
     * 
     * @param size input size
     * @return string representation of largest, non-fractional size prefix
     */
    public static String getNominalSize(ByteSize size)
    {
        ByteSizeUnits nominal = size.getNominalUnits();
        String nominalSize;
        
        switch(nominal)
        {
            case B:
                nominalSize = size.bytesProperty().get() + "";
                break;
            case KB:
                nominalSize = size.kiloBytesProperty().get() + "";
                break;
            case MB:
                nominalSize = size.megaBytesProperty().get() + "";
                break;
            default:
                nominalSize = size.gigaBytesProperty().get() + "";
        }
        
        return nominalSize;
    }
    
    /**
     * Returns a value of largest, non-fractional speed prefix, for example
     * if input is of speed 1100 B, it will be converted to string 1.1 (suffix kB/s not appended).
     * 
     * @param speed input speed
     * @return string representation of largest, non-fractional speed prefix
     */
    public static String getNominalSpeed(ByteSpeed speed)
    {
        ByteSpeedUnits nominal = speed.getNominalUnits();
        String nominalSpeed;
        
        switch(nominal)
        {
            case BPS:
                nominalSpeed = speed.bpsProperty().get() + "";
                break;
            case KBPS:
                nominalSpeed = speed.kBpsProperty().get() + "";
                break;
            case MBPS:
                nominalSpeed = speed.mBpsProperty().get() + "";
                break;
            default:
                nominalSpeed = speed.gBpsProperty().get() + "";
                break;
        }
        
        return nominalSpeed;
    }
    
    /**
     * Converts milliseconds to human readable format. Converted to maximum
     * order of hours, including any fractions as minutes and fractional seconds.
     * 
     * @param ms input time in milliseconds
     * @return human readable formatted time
     */
    public static String msToHumanReadable(long ms)
    {
        if(ms > 0)
        {
            long hours = TimeUnit.MILLISECONDS.toHours(ms);
            ms -= TimeUnit.HOURS.toMillis(hours);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(ms);
            ms -= TimeUnit.MINUTES.toMillis(minutes);
            long seconds = TimeUnit.MILLISECONDS.toSeconds(ms);
            ms -= TimeUnit.SECONDS.toMillis(seconds);
            
            StringBuilder format = new StringBuilder();
            
            boolean gt = false;
            if(hours > 0)
            {
                format.append(String.format("%dh", hours));
                gt = true;
            }
            
            if(gt || minutes > 0)
            {
                format.append(String.format("%dm", minutes));
            }
            
            double seconds_f = seconds + (ms / 1000.0);
            
            format.append(String.format(java.util.Locale.US,"%.3fs", seconds_f));
            
            return format.toString();
        }
        
        return "";
    }
    
    /**
     * Initializes a {@link javafx.scene.control.ChoiceBox} with 
     * {@link ByteSpeedUnits} prefixes.
     * 
     * @param box initialized choice box
     */
    public static void initByteSpeedChoiceBox(ChoiceBox<ByteSpeedUnits> box)
    {
        ObservableList<ByteSpeedUnits> choices = FXCollections.observableArrayList();
        choices.addAll(ByteSpeedUnits.BPS, ByteSpeedUnits.KBPS, ByteSpeedUnits.MBPS, ByteSpeedUnits.GBPS);
        
        box.setItems(choices);
        box.getSelectionModel().select(ByteSpeedUnits.MBPS);
    }
    
    /**
     * Initializes a {@link javafx.scene.control.ChoiceBox} with
     * {@link ByteSizeUnits} prefixes.
     * 
     * @param box initialized choice box
     */
    public static void initByteSizeChoiceBox(ChoiceBox<ByteSizeUnits> box)
    {
        ObservableList<ByteSizeUnits> list = FXCollections.observableArrayList();
        list.addAll(ByteSizeUnits.B, ByteSizeUnits.KB, ByteSizeUnits.MB, ByteSizeUnits.GB);
        
        box.setItems(list);
        box.getSelectionModel().select(ByteSizeUnits.GB);
    }
    
    /**
     * Initializes a {@link javafx.scene.control.ChoiceBox} with
     * {@link SimulationResultByteSpeed} prefixes and set's 
     * {@code SimulationResultByteSpeed.HUMAN_READABLE} as default choice.
     * 
     * @param box initialized choice nox
     */
    public static void initSimulationResultByteSpeedChoiceBox(ChoiceBox<SimulationResultByteSpeed> box)
    {
        ObservableList<SimulationResultByteSpeed> choices = FXCollections.observableArrayList();
        choices.addAll(SimulationResultByteSpeed.BPS, SimulationResultByteSpeed.KBPS, SimulationResultByteSpeed.MBPS, SimulationResultByteSpeed.GBPS, SimulationResultByteSpeed.HUMAN_READABLE);
        
        box.setItems(choices);
        box.getSelectionModel().select(SimulationResultByteSpeed.HUMAN_READABLE);
    }
    
    /**
     * Initializes a {@link javafx.scene.control.ChoiceBox} with
     * {@link SimulationResultByteSize} prefixes and set's 
     * {@code SimulationResultByteSize.HUMAN_READABLE} as default choice.
     * 
     * @param box initialized choice nox
     */
    public static void initSimulationResultByteSizeChoiceBox(ChoiceBox<SimulationResultByteSize> box)
    {
        ObservableList<SimulationResultByteSize> list = FXCollections.observableArrayList();
        list.addAll(SimulationResultByteSize.B, SimulationResultByteSize.KB, SimulationResultByteSize.MB, SimulationResultByteSize.GB, SimulationResultByteSize.HUMAN_READABLE);
        
        box.setItems(list);
        box.getSelectionModel().select(SimulationResultByteSize.HUMAN_READABLE);
    }
    
    /**
     * Initializes a {@link javafx.scene.control.ChoiceBox} with
     * {@link SimulationResultTimeUnit} prefixes and set's 
     * {@code SimulationResultTimeUnit.HUMAN_READABLE} as default choice.
     * 
     * @param box initialized choice nox
     */
    public static void initSimulationResultTimeUnitChoiceBox(ChoiceBox<SimulationResultTimeUnit> box)
    {
        ObservableList<SimulationResultTimeUnit> list = FXCollections.observableArrayList();
        list.addAll(SimulationResultTimeUnit.MILLISECOND, SimulationResultTimeUnit.SECOND, SimulationResultTimeUnit.MINUTE, SimulationResultTimeUnit.HOUR, SimulationResultTimeUnit.HUMAN_READABLE);
        
        box.setItems(list);
        box.getSelectionModel().select(SimulationResultTimeUnit.HUMAN_READABLE);
    }
}
