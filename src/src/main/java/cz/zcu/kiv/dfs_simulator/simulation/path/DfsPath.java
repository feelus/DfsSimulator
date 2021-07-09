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

package cz.zcu.kiv.dfs_simulator.simulation.path;

import cz.zcu.kiv.dfs_simulator.model.ByteSize;
import cz.zcu.kiv.dfs_simulator.model.ByteSizeUnits;
import cz.zcu.kiv.dfs_simulator.model.ByteSpeed;
import cz.zcu.kiv.dfs_simulator.model.ByteSpeedUnits;
import cz.zcu.kiv.dfs_simulator.model.ModelNode;
import cz.zcu.kiv.dfs_simulator.model.ModelServerNode;
import cz.zcu.kiv.dfs_simulator.model.connection.ModelNodeConnection;
import cz.zcu.kiv.dfs_simulator.simulation.PutSimulationTask;
import cz.zcu.kiv.dfs_simulator.simulation.SimulationTask;
import cz.zcu.kiv.dfs_simulator.model.storage.ServerStorage;
import cz.zcu.kiv.dfs_simulator.model.storage.StorageOperation;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FileSystemObject;
import cz.zcu.kiv.dfs_simulator.model.storage.filesystem.FsFile;
import java.util.List;
import java.util.Objects;

/**
 * Node path.
 */
public class DfsPath
{
    /**
     * Connections forming the path
     */
    private final List<ModelNodeConnection> path;
    /**
     * Simulated task
     */
    private final SimulationTask task;
    /**
     * Simulation time
     */
    private final long sTime;
    
    /**
     * Operation that is transfering data through this path
     */
    private StorageOperation runningOperation;
    
    /**
     * Set running operation.
     * 
     * @param runningOperation running operation
     */
    public void setRunningOperation(StorageOperation runningOperation)
    {
        this.runningOperation = runningOperation;
    }
    
    /**
     * Calculate amount of data that will be transfered with throughput {@code throughput}
     * in given time {@code timeMs}.
     * 
     * @param cTime simulation time
     * @param throughput throughput
     * @param timeMs time in milliseconds
     * @return amount of transfered data
     */
    public static ByteSize getDataTransferedInTime(long cTime, ByteSpeed throughput, long timeMs)
    {
        if(throughput.bpsProperty().get() > 0)
        {
            double secs = (timeMs / 1000.0);
        
            return new ByteSize((throughput.bpsProperty().get() * secs), ByteSizeUnits.B);
        }
        
        return new ByteSize(0);
    }
    
    /**
     * Calculate time required to transfer data of size {@code size} with
     * speed {@code throughput}.
     * 
     * @param cTime simulation time
     * @param throughput throughput
     * @param size size of data
     * @return time in milliseconds
     */
    public static long getDataTransferTime(long cTime, ByteSpeed throughput, ByteSize size)
    {
        if(throughput.bpsProperty().get() > 0)
        {
            long pBw = throughput.bpsProperty().get();
            long transferSize = size.bytesProperty().get();

            double sec = (double) transferSize / pBw;

            return (long) (sec * 1000);
        }
        
        return -1;
    }
    
    /**
     * Node path.
     * 
     * @param path node connections forming the path
     * @param task task associated with this path
     * @param sTime simulation time the path was selected
     */
    public DfsPath(List<ModelNodeConnection> path, SimulationTask task, long sTime)
    {
        this.path = path;
        this.task = task;
        this.sTime = sTime;
    }
    
    /**
     * Get average speed of transfer through this path in interval starting at
     * {@code cTime} and ending at {@code cTime} + {@code intervalLenght}.
     * 
     * @param cTime interval start
     * @param intervalLenght interval length
     * @return average speed
     */
    public ByteSpeed getAverageTransferThroughput(long cTime, long intervalLenght)
    {
        if(this.runningOperation != null)
        {
            ByteSpeed bottleneck = this.runningOperation.getAvailableThroughput();
            
            for(ModelNodeConnection conn : this.path)
            {
                ByteSpeed cBw = conn.getAverageBandwidth(cTime, intervalLenght);
                
                if(bottleneck.bpsProperty().get() > cBw.bpsProperty().get())
                {
                    bottleneck = cBw;
                }
            }
            
            return new ByteSpeed(bottleneck.bpsProperty().get(), ByteSpeedUnits.BPS);
            
        }
        
        return new ByteSpeed(0);
    }
    
    /**
     * Get usable LINK bandwidth at time {@code cTime}.
     * 
     * @param cTime simulation time
     * @return bandwidth
     */
    public ByteSpeed getCurrentLinkBandwidth(long cTime)
    {
        ByteSpeed bottleneck = new ByteSpeed(Long.MAX_VALUE);
        
        for(ModelNodeConnection conn : this.path)
        {
            ByteSpeed cBw = conn.getAverageBandwidth(cTime, 0);
            
            if(bottleneck.bpsProperty().get() > cBw.bpsProperty().get())
            {
                bottleneck = cBw;
            }
        }
        
        return new ByteSpeed(bottleneck.bpsProperty().get(), ByteSpeedUnits.BPS);
    }
    
    /**
     * Get usable PATH (link and storage) bandwidth at time {@code cTime}.
     * 
     * @param cTime simulation time
     * @return throughput
     */
    public ByteSpeed getCurrentPossibleThroughput(long cTime)
    {
        if(this.runningOperation != null)
        {
            ByteSpeed bottleneck = this.runningOperation.getAvailableThroughput();
            
            for(ModelNodeConnection conn : this.path)
            {
                ByteSpeed cBw = conn.getAverageBandwidth(cTime, 0);
                
                if(bottleneck.bpsProperty().get() > cBw.bpsProperty().get())
                {
                    bottleneck = cBw;
                }
            }
            
            return new ByteSpeed(bottleneck.bpsProperty().get(), ByteSpeedUnits.BPS);
            
        }
        
        return new ByteSpeed(0);
    }
    
    /**
     * Get MAXIMUM bandwidth of this path.
     * 
     * @return bandwidth
     */
    public ByteSpeed getMaximumLinkBandwidth()
    {
        ByteSpeed bottleneck = new ByteSpeed(Long.MAX_VALUE, ByteSpeedUnits.BPS);
        
        for(ModelNodeConnection conn : this.path)
        {
            if(conn.getMaximumBandwidth().bpsProperty().get() < bottleneck.bpsProperty().get())
            {
                bottleneck.setBps(conn.getMaximumBandwidth().bpsProperty().get());
            }
        }
        
        return bottleneck;
    }
    
    /**
     * Get total latency of this path.
     * 
     * @return latency
     */
    public long getCumLatency()
    {
        long lat = 0;
        
        for(ModelNodeConnection conn : this.path)
        {
            lat += conn.getLatency();
        }
        
        return lat;
    }
    
    /**
     * Get (or create if it does not exist) file associated with this
     * path's task.
     * 
     * @return target file
     */
    public FsFile getOrCreateTargetFile()
    {
        final ModelServerNode sn = this.getTarget();
        
        if(sn != null)
        {
            FileSystemObject obj = sn.getRootDir().getChildObject(this.task.getFile().getFullPath());
            // if it is an upload and the file does not exist we create a new one
            if(this.task instanceof PutSimulationTask && obj == null)
            {
                obj = sn.getRootDir().getChildObject(this.task.getFile().getFullPath(), true);
            }
            
            if(obj instanceof FsFile)
            {
                return (FsFile) obj;
            }
        }
        
        return null;
    }
    
    /**
     * Get target server.
     * 
     * @return target server
     */
    public ModelServerNode getTarget()
    {
        ModelNode node = this.path.get(this.path.size() - 1).getNeighbour();
        
        if(node instanceof ModelServerNode)
        {
            return (ModelServerNode) node;
        }
        
        return null;
    }
    
    /**
     * Get target storage.
     * 
     * @return target storage
     */
    public ServerStorage getTargetStorage()
    {
        final FsFile targetObject = this.getOrCreateTargetFile();
        
        if(targetObject == null || this.getTarget() == null)
        {
            return null;
        }
        
        return this.getTarget().getFsManager().getFsObjectMountDevice(targetObject);
    }
    
    /**
     * Get simulation time, when this path was selected.
     * 
     * @return simulation time
     */
    public long getSTime()
    {
        return this.sTime;
    }
    
    /**
     * Get connections forming this path.
     * 
     * @return connections
     */
    public List<ModelNodeConnection> getPath()
    {
        return this.path;
    }

    /**
     * {@inheritDoc}
     */
    @Override public int hashCode()
    {
        int hash = 3;
        hash = 37 * hash + Objects.hashCode(this.path);
        return hash;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }

        final DfsPath other = (DfsPath) obj;
        
        if(other.getPath().size() != this.path.size())
        {
            return false;
        }
        
        return this.path.equals(other.getPath());
    }
    
    @Override public String toString()
    {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        
        for(ModelNodeConnection conn : this.path)
        {
            if(first)
            {
                sb.append(conn.getOrigin().getNodeID());
                sb.append("->");
                first = false;
            }
            else
            {
                sb.append("->");
            }
            sb.append(conn.getNeighbour().getNodeID());
        }
        
        return sb.toString();
    }
}
