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

package cz.zcu.kiv.dfs_simulator.view.simulation;

import cz.zcu.kiv.dfs_simulator.helpers.FxHelper;
import cz.zcu.kiv.dfs_simulator.model.ByteSize;
import cz.zcu.kiv.dfs_simulator.model.ByteSpeed;
import cz.zcu.kiv.dfs_simulator.model.connection.ModelNodeConnection;
import cz.zcu.kiv.dfs_simulator.simulation.DfsSimulatorTaskResult;
import cz.zcu.kiv.dfs_simulator.simulation.GetSimulationTask;
import cz.zcu.kiv.dfs_simulator.simulation.SimulationTaskType;
import cz.zcu.kiv.dfs_simulator.simulation.path.DfsPath;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * Reconstructs events from simulation results.
 */
public class FxSimulationPlayer
{
    /**
     * Progress update delay
     */
    protected final static int PLAY_EVENT_DELAY_MS = 100;
    /**
     * Delay after failed (error) task
     */
    protected final static int PLAY_EVENT_ERROR_DELAY_MS = 1500;
    /**
     * Sub-event failed icon
     */
    protected final static Image SUBEVENT_FAILED_IMAGE;
    /**
     * Sub-event success icon
     */
    protected final static Image SUBEVENT_SUCCESS_IMAGE;
    
    /**
     * Initiate sub-event images
     */
    static
    {
        SUBEVENT_FAILED_IMAGE = new Image(FxSimulationPlayer.class.getClassLoader().getResourceAsStream(("img/sim_subevent_fail.png")));
        SUBEVENT_SUCCESS_IMAGE = new Image(FxSimulationPlayer.class.getClassLoader().getResourceAsStream("img/sim_subevent_success.png"));
    }
    
    /**
     * Simulation results
     */
    protected final List<FxSimulatorTaskResultSet> resultSet;
    /**
     * Simulation log that will display reconstructed events
     */
    protected final SimulationLogDisplayable simulationLog;
    
    /**
     * Elapsed time of reconstructed events - for single simulation
     */
    protected long elapsedTime = 0;
    
    /**
     * Average speed of reconstructed events
     */
    protected ByteSpeed averageSpeed = new ByteSpeed(0);
    /**
     * Amount of downloaded data of reconstructed events
     */
    protected ByteSize downloaded = new ByteSize(0);
    /**
     * Amount of uploaded data of reconstructed events
     */
    protected ByteSize uploaded = new ByteSize(0);
    
    /**
     * Synchronization during task playback
     */
    protected final Object taskPlaying = new Object();
    
    /**
     * Reconstructs simulation results into simulation events that can be
     * visualized.
     * 
     * @param resultSet simulation results
     * @param simulationLog object that will visualize events
     */
    public FxSimulationPlayer(List<FxSimulatorTaskResultSet> resultSet, SimulationLogDisplayable simulationLog)
    {
        this.resultSet = resultSet;
        this.simulationLog = simulationLog;
    }
    
    /**
     * Begin reconstruction and playback.
     */
    public void play()
    {
        Task task = new Task<Void>()
        {
            @Override protected Void call() throws Exception
            {
                int setId = 1;
                boolean success = true;
                
                simulationLog.beginSimulation();
                for(FxSimulatorTaskResultSet results : resultSet)
                {
                    simulationLog.switchSimulationDisplay(results.getSimulationResult().getType(), setId++);
                    simulationLog.updateSimulationStatus("Running (" + results.getSimulationResult().getType() + ")", Color.CORNFLOWERBLUE);
                    
                    Map<DfsSimulatorTaskResult, FxSimulationLogEvent> resultEvents = createResultEvents(results.getSimulationResult().getResults());
                    
                    int taskCount = results.getSimulationResult().getResults().size();
                    int taskI = 1;
                    for(DfsSimulatorTaskResult result : results.getSimulationResult().getResults())
                    {
                        simulationLog.updateTaskCounter((taskI++) + " / " + taskCount);
                        if(!playResult(result, resultEvents.get(result)))
                        {
                            success = false;
                            break;
                        }
                    }
                    
                    simulationLog.updateSimulationStatus("Finished", (success) ? Color.GREEN : Color.RED);
                    resetCounters();
                }
                
                simulationLog.endSimulation(success);
                return null;
            }
        };
        
        task.setOnSucceeded(e -> {
            //resultSet.clear();
        });
        
        new Thread(task).start();
    }
    
    /**
     * Map simulator results onto simulation playback events.
     * 
     * @param results simulator results
     * @return mapping
     */
    protected Map<DfsSimulatorTaskResult, FxSimulationLogEvent> createResultEvents(List<DfsSimulatorTaskResult> results)
    {
        Map<DfsSimulatorTaskResult, FxSimulationLogEvent> resultEvents = new HashMap<>();
        
        for(DfsSimulatorTaskResult result : results)
        {
            FxSimulationLogEvent event;
            if(result.getTask().getType() == SimulationTaskType.GET)
            {
                event = new FxSimulationLogEvent(SimulationTaskType.GET,
                    "DOWN " + result.getTask().getFile().getFullPath(), new ImageView(result.getTask().getImage()));
            }
            else
            {
                event = new FxSimulationLogEvent(SimulationTaskType.PUT,
                    "UP " + result.getTask().getFile().getFullPath(), new ImageView(result.getTask().getImage()));
            }
            
            resultEvents.put(result, event);
            this.simulationLog.playSimulationEvent(event);
        }
        
        return resultEvents;
    }
    
    /**
     * Reconstruct and play given result.
     * 
     * @param result result
     * @param parent event parent
     * @return if success
     * @throws InterruptedException thrown if playback is interrupted
     */
    protected boolean playResult(DfsSimulatorTaskResult result, final FxSimulationLogEvent parent) throws InterruptedException
    {
        if(result.getTask().getType() == SimulationTaskType.GET)
        {
            return this.playDownloadResult(result, parent);
        }
        else
        {
            return this.playUploadResult(result, parent);
        }
        
    }
    
    /**
     * Reconstruct and play download result.
     * 
     * @param result result
     * @param parent event parent
     * @return if success
     * @throws InterruptedException thrown if playback is interrupted
     */
    protected boolean playDownloadResult(DfsSimulatorTaskResult result, final FxSimulationLogEvent parent) throws InterruptedException
    {
        boolean retval = true;
        switch(result.getState())
        {
            case SUCCESS:
                this.playDownloadSuccessResult(parent, result);
                break;
            case OBJECT_NOT_FOUND:
                this.playObjectNotFound(parent, result);
                break;
            case NO_NEIGHBOURS_AVAILABLE:
                retval = false;
                this.playNoNeighboursAvailable(parent, result);
                break;
            case OBJECT_NOT_MOUNTED:
                this.playObjectNotMountedResult(parent, result);
                break;
            default:
            case NO_PATH_AVAILABLE:
                this.playObjectNoPathAvailableResult(parent, result);
                break;
        }
        
        return retval;
    }
    
    /**
     * Play task object found event.
     * 
     * @param parent event parent
     * @param result task result
     */
    protected void logObjectFound(FxSimulationLogEvent parent, DfsSimulatorTaskResult result)
    {
        
        String message;
        if(parent.getType() == SimulationTaskType.GET)
        {
            message = "Found object";
        }
        else
        {
            message = "Found target " + result.getTask().getFile().getParent().nameProperty().get();
        }
        
        FxSimulationLogEvent foundEvent = new FxSimulationLogEvent(parent.getType(), message, parent, new ImageView(SUBEVENT_SUCCESS_IMAGE));
        
        foundEvent.setStatus(FxSimulationLogEventStatus.DONE);
        this.simulationLog.playSimulationEvent(foundEvent);
        
        FxSimulationLogEvent pathEvent;
        if(result.getPathHistory().size() == 1)
        {
            pathEvent = new FxSimulationLogEvent(parent.getType(), "Path [" + this.pathToString(result.getPathHistory().get(0)) + "], latency " + result.getPathHistory().get(0).getCumLatency() + " ms", parent, new ImageView(SUBEVENT_SUCCESS_IMAGE));
            pathEvent.setStatus(FxSimulationLogEventStatus.DONE);
        }
        else
        {
            pathEvent = new FxSimulationLogEvent(parent.getType(), "Dynamic pathing", parent, new ImageView(SUBEVENT_SUCCESS_IMAGE));
        }
        
        this.simulationLog.playSimulationEvent(pathEvent);
        
    }
    
    /**
     * Play successfully downloaded task.
     * 
     * @param parent event parent
     * @param result task result
     * @throws InterruptedException thrown if playback is interrupted
     */
    protected void playDownloadSuccessResult(FxSimulationLogEvent parent, DfsSimulatorTaskResult result) throws InterruptedException
    {
        this.logObjectFound(parent, result);
        
        FxSimulationLogEvent bandwidthEvent = new FxSimulationLogEvent(SimulationTaskType.GET, "Avg. bandwidth is " + result.getAverageSpeed().getHumanReadableFormat(), parent, new ImageView(SUBEVENT_SUCCESS_IMAGE));
        bandwidthEvent.setStatus(FxSimulationLogEventStatus.DONE);
        this.simulationLog.playSimulationEvent(bandwidthEvent);
        
        if(result.getObject().getSize().bytesProperty().get() > 0)
        {
            if(this.simulationLog.requestedDelayedDisplay().get())
            {
                this.playTaskProgress(parent, result);
            }
            else
            {
                this.completeTaskProgress(parent, result);
            }
        }
        else
        {
            this.updateElapsedTime(result.getTotalTime());
            
            parent.setStatus(FxSimulationLogEventStatus.DONE);
            parent.setMessage(parent.getMessage() + " (100%, 0s)");
            this.simulationLog.updateSimulationEvent(parent);
        }
    }
                
    /**
     * Reconstruct and play upload task.
     * 
     * @param result task result
     * @param parent event parent
     * @return true if success
     * @throws InterruptedException thrown if playback is interrupted
     */
    protected boolean playUploadResult(DfsSimulatorTaskResult result, final FxSimulationLogEvent parent) throws InterruptedException
    {
        boolean retval = false;
        switch(result.getState())
        {
            case SUCCESS:
                retval = true;
                this.playUploadSuccessResult(parent, result);
                break;
            case OBJECT_NOT_FOUND:
                this.playObjectNotFound(parent, result);
                break;
            case NOT_ENOUGH_SPACE_ON_DEVICE:
                this.playUploadNotEnoughSpaceResult(parent, result);
                break;
            case OBJECT_NOT_MOUNTED:
                this.playObjectNotMountedResult(parent, result);
                break;
            case NOT_ENOUGH_SPACE_FOR_REPLICA:
                this.playNotEnoughSpaceForReplicaResult(parent, result);
                break;
            default:
            case NO_PATH_AVAILABLE:
                this.playObjectNoPathAvailableResult(parent, result);
                break;
        }
        
        return retval;
    }
    
    /**
     * Play successfully uploaded task.
     * 
     * @param parent event parent
     * @param result simulated task
     * @throws InterruptedException thrown if playback is interrupted
     */
    protected void playUploadSuccessResult(FxSimulationLogEvent parent, DfsSimulatorTaskResult result) throws InterruptedException
    {
        FxSimulationLogEvent uploadSizeEvent = new FxSimulationLogEvent(SimulationTaskType.PUT, "Upload object size " + result.getTask().getFile().getSize().getHumanReadableFormat(), parent, new ImageView(SUBEVENT_SUCCESS_IMAGE));
        uploadSizeEvent.setStatus(FxSimulationLogEventStatus.DONE);
        this.simulationLog.playSimulationEvent(uploadSizeEvent);
        
        this.logObjectFound(parent, result);
        
        FxSimulationLogEvent bandwidthEvent = new FxSimulationLogEvent(SimulationTaskType.PUT, "Avg. bandwidth is " + result.getAverageSpeed().getHumanReadableFormat(), parent, new ImageView(SUBEVENT_SUCCESS_IMAGE));
        bandwidthEvent.setStatus(FxSimulationLogEventStatus.DONE);
        this.simulationLog.playSimulationEvent(bandwidthEvent);
        
        // space needed has already been subtracted, need to add it
        if(result.getPathHistory().size() == 1)
        {
            DfsPath path = result.getPathHistory().get(0);
            
            ByteSize spaceAvailable = new ByteSize(
                    path.getTarget().getFsManager().getStorageUnusedSize(path.getTargetStorage()).bytesProperty().get() + result.getTask().getFile().getSize().bytesProperty().get()
            );
            
            FxSimulationLogEvent spaceLeftEvent = new FxSimulationLogEvent(SimulationTaskType.PUT, "Space available " + spaceAvailable.getHumanReadableFormat(), parent, new ImageView(SUBEVENT_SUCCESS_IMAGE));
            spaceLeftEvent.setStatus(FxSimulationLogEventStatus.DONE);
            this.simulationLog.playSimulationEvent(spaceLeftEvent);
        }
        
        if(this.simulationLog.requestedDelayedDisplay().get())
        {
            this.playTaskProgress(parent, result);
        }
        else
        {
            this.completeTaskProgress(parent, result);
        }
    }
    
    /**
     * Play target object was not mounted event.
     * 
     * @param parent event parent
     * @param result task result
     * @throws InterruptedException thrown if playback is interrupted
     */
    protected void playObjectNotMountedResult(FxSimulationLogEvent parent, DfsSimulatorTaskResult result) throws InterruptedException
    {
        this.logObjectFound(parent, result);
        
        FxSimulationLogEvent notMountedEvent = new FxSimulationLogEvent(parent.getType(), "Object not mounted", parent, new ImageView(SUBEVENT_FAILED_IMAGE));
        notMountedEvent.setStatus(FxSimulationLogEventStatus.FAILED);
        parent.setStatus(FxSimulationLogEventStatus.FAILED);
        
        this.simulationLog.playSimulationEvent(notMountedEvent);
        this.simulationLog.updateSimulationEvent(parent);
        
        if(simulationLog.requestedDelayedDisplay().get())
        {
            Thread.sleep(PLAY_EVENT_ERROR_DELAY_MS);        
        }
    }
    
    /**
     * Play target storage did not have enough space to resize replica.
     * 
     * @param parent event parent
     * @param result task result
     * @throws InterruptedException thrown if playback is interrupted
     */
    protected void playNotEnoughSpaceForReplicaResult(FxSimulationLogEvent parent, DfsSimulatorTaskResult result) throws InterruptedException
    {
        this.logObjectFound(parent, result);
        
        FxSimulationLogEvent notEnoughSpace = new FxSimulationLogEvent(parent.getType(), "Not enough space for replicas", parent, new ImageView(SUBEVENT_FAILED_IMAGE));
        notEnoughSpace.setStatus(FxSimulationLogEventStatus.FAILED);
        parent.setStatus(FxSimulationLogEventStatus.FAILED);
        
        this.simulationLog.playSimulationEvent(notEnoughSpace);
        this.simulationLog.updateSimulationEvent(parent);
        
        if(simulationLog.requestedDelayedDisplay().get())
        {
            Thread.sleep(PLAY_EVENT_ERROR_DELAY_MS);        
        }
    }
    
    /**
     * Play target storage did not have enough space for upload.
     * 
     * @param parent event parent
     * @param result task result
     * @throws InterruptedException thrown if playback is interrupted
     */
    protected void playUploadNotEnoughSpaceResult(FxSimulationLogEvent parent, DfsSimulatorTaskResult result) throws InterruptedException
    {
        this.logObjectFound(parent, result);
                
        FxSimulationLogEvent notEnoughSpaceEvent = new FxSimulationLogEvent(SimulationTaskType.PUT, "Not enough space available", parent, new ImageView(SUBEVENT_FAILED_IMAGE));
        notEnoughSpaceEvent.setStatus(FxSimulationLogEventStatus.FAILED);
        parent.setStatus(FxSimulationLogEventStatus.FAILED);
        
        this.simulationLog.playSimulationEvent(notEnoughSpaceEvent);
        this.simulationLog.updateSimulationEvent(parent);
        
        if(simulationLog.requestedDelayedDisplay().get())
        {
            Thread.sleep(PLAY_EVENT_ERROR_DELAY_MS);
        }
    }
    
    /**
     * Play task target object was not found.
     * 
     * @param parent event parent
     * @param result task result
     * @throws InterruptedException thrown if playback is interrupted
     */
    protected void playObjectNotFound(FxSimulationLogEvent parent, DfsSimulatorTaskResult result) throws InterruptedException
    {
        this.logObjectFound(parent, result);

        FxSimulationLogEvent notFoundEvent = new FxSimulationLogEvent(parent.getType(), "Object not found", parent, new ImageView(SUBEVENT_FAILED_IMAGE));
        notFoundEvent.setStatus(FxSimulationLogEventStatus.FAILED);
        parent.setStatus(FxSimulationLogEventStatus.FAILED);
        
        this.simulationLog.playSimulationEvent(notFoundEvent);
        this.simulationLog.updateSimulationEvent(parent);
        
        if(simulationLog.requestedDelayedDisplay().get())
        {
            Thread.sleep(PLAY_EVENT_ERROR_DELAY_MS);        
        }
    }
    
    /**
     * Play client had no neighbours.
     * 
     * @param parent event parent
     * @param result task result
     */
    protected void playNoNeighboursAvailable(FxSimulationLogEvent parent, DfsSimulatorTaskResult result)
    {
        FxSimulationLogEvent noNeighboursEvent = new FxSimulationLogEvent(parent.getType(), "No neighbour connections", parent, new ImageView(SUBEVENT_FAILED_IMAGE));
        noNeighboursEvent.setStatus(FxSimulationLogEventStatus.FAILED);
        parent.setStatus(FxSimulationLogEventStatus.FAILED);
        
        this.simulationLog.playSimulationEvent(noNeighboursEvent);
        this.simulationLog.updateSimulationEvent(parent);
    }
    
    /**
     * Play no path found to target object.
     * 
     * @param parent event parent
     * @param result task result
     */
    protected void playObjectNoPathAvailableResult(FxSimulationLogEvent parent, DfsSimulatorTaskResult result)
    {
        FxSimulationLogEvent noPathEvent = new FxSimulationLogEvent(parent.getType(), "No path available", parent, new ImageView(SUBEVENT_FAILED_IMAGE));
        noPathEvent.setStatus(FxSimulationLogEventStatus.SKIPPED);
        parent.setStatus(FxSimulationLogEventStatus.SKIPPED);
        
        parent.setRunTime(result.getTotalTime());
        parent.setMessage(parent.getMessage() + " (Skipped, " + FxHelper.msToHumanReadable(result.getTotalTime()) + ")");
        
        this.updateElapsedTime(result.getTotalTime());
        
        this.simulationLog.playSimulationEvent(noPathEvent);
        this.simulationLog.updateSimulationEvent(parent);
    }
    
    /**
     * Play (visualize) task progress - update in steps it's progress value.
     * 
     * @param event event
     * @param result task result
     * @throws InterruptedException thrown if playback is interrupted
     */
    protected void playTaskProgress(FxSimulationLogEvent event, DfsSimulatorTaskResult result) throws InterruptedException
    {
        String message = event.getMessage();
        
        // in order to avoid rounding errors
        long pSize;
        if(result.getTask() instanceof GetSimulationTask)
        {
            pSize = this.downloaded.bytesProperty().get();
        }
        else
        {
            pSize = this.uploaded.bytesProperty().get();
        }
        
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(PLAY_EVENT_DELAY_MS), ae -> incrementAndUpdateTaskProgress(event, message, result, pSize))
        );
        
        timeline.setCycleCount(20);
        timeline.setOnFinished(ae ->
        {
            synchronized(taskPlaying)
            {
                taskPlaying.notify();
            }
        });
        
        this.simulationLog.requestedDelayedDisplay().addListener((observable, oldVal, newVal) ->
        {
            if(!newVal)
            {
                // manually finish timeline
                synchronized(timeline)
                {
                    // @TODO kind of a hack in order to get the simulation to finish
                    timeline.setRate(1000);
                }
            }
        });
        
        this.highlightPath(result.getPathHistory(), true);
        
        synchronized(this.taskPlaying)
        {
            timeline.play();
            this.taskPlaying.wait();
        }
        
        this.highlightPath(result.getPathHistory(), false);
    }
    
    /**
     * Fast-forward playback and complete whole progress visualization.
     * 
     * @param event parent event
     * @param result task result
     */
    protected void completeTaskProgress(FxSimulationLogEvent event, DfsSimulatorTaskResult result)
    {
        if(event.getType() == SimulationTaskType.GET)
        {
            this.updateDownloaded(result.getObject().getSize().bytesProperty().get());
        }
        else
        {
            this.updateUploaded(result.getTask().getFile().getSize().bytesProperty().get());
        }
        
        this.updateAverageSpeed(result.getTotalTime(), result.getAverageSpeed());
        this.updateElapsedTime(result.getTotalTime());
        
        event.setRunTime(result.getTotalTime());
        
        String eventElapsedTime = FxHelper.msToHumanReadable(event.runTimeProperty().get());
        event.setPercent(100);
        event.setStatus(FxSimulationLogEventStatus.DONE);
        event.setMessage(event.getMessage() + " (100%, " + eventElapsedTime + ")");
        
        this.simulationLog.updateSimulationEvent(event);
    }
    
    /**
     * Increment task progress and update it's visualization.
     * 
     * @param event parent event
     * @param originalMessage task original text
     * @param result task result
     * @param prevSize previous increment size
     */
    protected void incrementAndUpdateTaskProgress(FxSimulationLogEvent event, String originalMessage, DfsSimulatorTaskResult result, long prevSize)
    {
        long incrementTime = result.getTotalTime() / 20;
                
        event.setRunTime(event.runTimeProperty().get() + incrementTime);
        event.setPercent(event.percentProperty().get() + 5);
        
        if(event.percentProperty().get() == 100)
        {
            // to avoid rounding errors
            if(event.getType() == SimulationTaskType.GET)
            {
                long sDiff = (prevSize + result.getObject().getSize().bytesProperty().get()) - this.downloaded.bytesProperty().get();
                
                this.updateDownloaded(sDiff);
            }
            else
            {
                long sDiff = (prevSize + result.getTask().getFile().getSize().bytesProperty().get()) - this.uploaded.bytesProperty().get();
                
                this.updateUploaded(sDiff);
            }
            
            event.setStatus(FxSimulationLogEventStatus.DONE);
            event.setMessage(originalMessage + " (100%, " + FxHelper.msToHumanReadable(result.getTotalTime()) + ")");

            this.updateAverageSpeed(incrementTime + (result.getTotalTime() - event.runTimeProperty().get()), result.getAverageSpeed());
            this.updateElapsedTime(incrementTime + (result.getTotalTime() - event.runTimeProperty().get()));
        }
        else
        {
            if(event.getType() == SimulationTaskType.GET)
            {
                this.updateDownloaded((result.getObject().getSize().bytesProperty().get() / 20));
            }
            else
            {
                this.updateUploaded((result.getTask().getFile().getSize().bytesProperty().get() / 20));
            }
            
            String eventElapsedTime = FxHelper.msToHumanReadable(event.runTimeProperty().get());
            event.setMessage(originalMessage + " (" + event.percentProperty().get() + "%, " + eventElapsedTime + ")");
            
            // @TODO would be nice if we could get the actual speed at this
            // timeframe rather than an overall average speed
            this.updateAverageSpeed(incrementTime, result.getAverageSpeed());
            this.updateElapsedTime(incrementTime);
        }
        
        this.simulationLog.updateSimulationEvent(event);
    }
    
    /**
     * Update playback elapsed time.
     * 
     * @param increment increment time
     */
    protected void updateElapsedTime(long increment)
    {
        this.elapsedTime += increment;
        this.simulationLog.updateSimulationElapsedTime(this.elapsedTime);
    }
    
    /**
     * Update playback amount of downloaded data.
     * 
     * @param incrementBytes increment bytes
     */
    protected void updateDownloaded(long incrementBytes)
    {
        this.downloaded.setBytes(this.downloaded.bytesProperty().get() + incrementBytes);
        this.simulationLog.updateSimulationDownloaded(this.downloaded);
    }
    
    /**
     * Update playback amount of uploaded data.
     * 
     * @param incrementBytes increment bytes
     */
    protected void updateUploaded(long incrementBytes)
    {
        this.uploaded.setBytes(this.uploaded.bytesProperty().get() + incrementBytes);
        this.simulationLog.updateSimulationUploaded(this.uploaded);
    }
    
    /**
     * Update playback average speed.
     * 
     * @param incrementTime time increment
     * @param incrementAverageSpeed average speed of time increment
     */
    protected void updateAverageSpeed(long incrementTime, ByteSpeed incrementAverageSpeed)
    {
        if(incrementTime > 0)
        {
            long newAverageSpeed = (
                    (this.elapsedTime * this.averageSpeed.bpsProperty().get()) +
                    (incrementTime * incrementAverageSpeed.bpsProperty().get())
                    ) 
                    /
                    (incrementTime + this.elapsedTime);


            this.averageSpeed.setBps(newAverageSpeed);
            this.simulationLog.updateSimulationAverageSpeed(this.averageSpeed);
        }
    }
    
    /**
     * Reset playback data - average speed, downloaded, uploaded and elapsed time.
     */
    protected void resetCounters()
    {
        this.averageSpeed.setBps(0);
        this.downloaded.setBytes(0);
        this.elapsedTime = 0;
        this.uploaded.setBytes(0);
    }
    
    /**
     * Highlight path that was used for task data transfer.
     * 
     * @param pathHistory path history
     * @param highlight if path should be highlighted
     */
    protected void highlightPath(List<DfsPath> pathHistory, boolean highlight)
    {
        if(pathHistory.size() == 1)
        {
            this.simulationLog.highlightPath(pathHistory.get(0).getPath(), highlight);
        }
        else
        {
            // build distinct nodeconnection set
            Set<ModelNodeConnection> conn = new HashSet<>();
            for(DfsPath p : pathHistory)
            {
                conn.addAll(p.getPath());
            }
            
            this.simulationLog.highlightPath(conn, highlight);
        }
    }
    
    /**
     * Convert path to text - separated list of nodes
     * 
     * @param path path
     * @return text
     */
    protected String pathToString(DfsPath path)
    {
        StringBuilder sb = new StringBuilder();
        boolean prepend = false;
        
        for(ModelNodeConnection conn : path.getPath())
        {
            if(!prepend)
            {
                sb.append(conn.getOrigin().getNodeID());
                prepend = true;
            }
            
            if(prepend)
            {
                sb.append("->");
            }
            prepend = true;
            
            sb.append(conn.getNeighbour().getNodeID());
        }
        
        return sb.toString();
    }
}
