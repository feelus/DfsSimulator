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

package cz.zcu.kiv.dfs_simulator.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Representation of speed. Allows conversion between multiples.
 */
public class ByteSpeed
{
    /**
     * Bytes per second
     */
    protected final LongProperty bps;
    /**
     * Kilobytes per second
     */
    protected final DoubleProperty kBps;
    /**
     * Megabytes per second
     */
    protected final DoubleProperty mBps;
    /**
     * Gigabytes per second
     */
    protected final DoubleProperty gBps;
    
    /**
     * Human readable representation of speed
     */
    protected final StringProperty humanReadable;
    
    /**
     * Constructs new instance with speed set to 0.
     */
    public ByteSpeed()
    {
        this.bps = new SimpleLongProperty();
        this.kBps = new SimpleDoubleProperty();
        this.mBps = new SimpleDoubleProperty();
        this.gBps = new SimpleDoubleProperty();
        
        this.humanReadable = new SimpleStringProperty("0 B/s");
        
        this.addHumanReadableListener();
        this.bindUnitMultiplies();
    }
    
    /**
     * Constructs new instance with speed set from converting {@code speed}
     * to bytes per second using {@code units}.
     * 
     * @param speed speed value
     * @param units units of {@code speed} value
     */
    public ByteSpeed(long speed, ByteSpeedUnits units)
    {
        this();
        this.bps.set(speed * units.getMu());
    }
    
    /**
     * Constructs new instance with speed {@code speed} bytes per second.
     * 
     * @param speed speed in bytes per second
     */
    public ByteSpeed(long speed)
    {
        this(speed, ByteSpeedUnits.BPS);
    }
    
    /**
     * Constructs new instance with speed set from converting {@code speed}
     * to bytes per second using {@code units}.
     * 
     * @param speed speed value
     * @param units units of {@code speed} value
     */
    public ByteSpeed(double speed, ByteSpeedUnits units)
    {
        this();
        this.bps.set((long) (speed * units.getMu()));
    }
    
    /**
     * Set speed in bytes per second.
     * 
     * @param bps speed in bytes per second
     */
    public void setBps(long bps)
    {
        this.bps.set(bps);
    }
    
    /**
     * Returns a writable {@link LongProperty} with speed in bytes per second.
     * 
     * @return speed in bytes per second
     */
    public LongProperty bpsProperty()
    {
        return this.bps;
    }
    
    /**
     * Set speed in bytes per second.
     * 
     * @param kbps speed in kilobytes per second
     */
    public void setKbps(double kbps)
    {
        this.bps.set( (long) (kbps * ByteSpeedUnits.KBPS.getMu()) );
    }
    
    /**
     * Returns a writable {@link LongProperty} with speed in kilobytes per second.
     * 
     * @return speed in kilobytes per second
     */
    public DoubleProperty kBpsProperty()
    {
        return this.kBps;
    }
    
    /**
     * Set speed in bytes per second.
     * 
     * @param mbps speed in megabytes per second
     */
    public void setMBps(double mbps)
    {
        this.bps.set( (long) (mbps * ByteSpeedUnits.MBPS.getMu()) );
    }
    
    /**
     * Returns a writable {@link LongProperty} with speed in megabytes per second.
     * 
     * @return speed in megabytes per second
     */
    public DoubleProperty mBpsProperty()
    {
        return this.mBps;
    }
    
    /**
     * Set speed in bytes per second.
     * 
     * @param gbps speed in gigabytes per second
     */
    public void setGBps(double gbps)
    {
        this.bps.set( (long) (gbps * ByteSpeedUnits.GBPS.getMu()) );
    }
    
    /**
     * Returns a writable {@link LongProperty} with speed in gigabytes per second.
     * 
     * @return speed in gigabytes per second
     */
    public DoubleProperty gBpsProperty()
    {
        return this.gBps;
    }
    
    /**
     * Binds human readable property value to {@code bytes} value.
     */
    private void addHumanReadableListener()
    {
        this.bps.addListener((observable, oldVal, newVal) ->
        {
            humanReadable.set(getHumanReadableFormat());
        });
    }
    
    /**
     * Binds speed multiples to {@code bps} value.
     */
    private void bindUnitMultiplies()
    {
        this.kBps.bind(this.bps.divide((double) ByteSizeUnits.KB.getMu()));
        this.mBps.bind(this.bps.divide((double) ByteSizeUnits.MB.getMu()));
        this.gBps.bind(this.bps.divide((double) ByteSizeUnits.GB.getMu()));
    }
    
    /**
     * Returns a human readable representation of size.
     * 
     * Based on http://stackoverflow.com/a/3758880
     * 
     * @return human readable format
     */
    public String getHumanReadableFormat()
    {
        int mu = 1000;
        
        if(this.bps.get() < mu)
        {
            return this.bps.get() + " B/s";
        }
        
        int exp = (int) (Math.log(this.bps.get()) / Math.log(mu));
        String pre = "kmgTPE".charAt(exp - 1) + "";
        
        return String.format("%.1f %sB/s", this.bps.get() / Math.pow(mu, exp), pre);
    }
    
    /**
     * Returns {@link StringProperty} with human readable representation.
     * 
     * @return human readable representation
     */
    public StringProperty humanReadableProperty()
    {
        return this.humanReadable;
    }
    
    /**
     * Return the largest possible prefix for this instance's speed.
     * 
     * @return largest possible prefix
     */
    public ByteSpeedUnits getNominalUnits()
    {
        int mu = 1000;
        
        if(this.bps.get() < mu)
        {
            return ByteSpeedUnits.BPS;
        }
        else if(this.bps.get() < (mu * mu) )
        {
            return ByteSpeedUnits.KBPS;
        }
        else if(this.bps.get() < (mu * mu * mu))
        {
            return ByteSpeedUnits.MBPS;
        }
        else 
        {
            return ByteSpeedUnits.GBPS;
        }
    }
    
    /**
     * Returns a {@link String} representation of this instance (using {@link #getHumanReadableFormat()}).
     * 
     * @return string representation
     */
    @Override public String toString()
    {
        return this.getHumanReadableFormat();
    }
}
