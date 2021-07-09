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
 * Representation of size. Allows conversion between multiples.
 */
public class ByteSize
{
    /**
     * Size in bytes
     */
    protected final LongProperty bytes;
    /**
     * Size in kilobytes
     */
    protected final DoubleProperty kiloBytes;
    /**
     * Size i megabytes
     */
    protected final DoubleProperty megaBytes;
    /**
     * Size in gigabytes
     */
    protected final DoubleProperty gigaBytes;
    
    /**
     * Human readable representation of size
     */
    protected final StringProperty humanReadable;
    
    /**
     * Constructs new instance with size set to 0.
     */
    public ByteSize()
    {
        this.bytes = new SimpleLongProperty();
        this.kiloBytes = new SimpleDoubleProperty();
        this.megaBytes = new SimpleDoubleProperty();
        this.gigaBytes = new SimpleDoubleProperty();
        
        this.humanReadable = new SimpleStringProperty("0 B");
        
        this.addHumanReadableListener();
        this.bindUnitMultiples();
    }
    
    /**
     * Constructs new instance with size set from converting {@code size}
     * to bytes using {@code units}.
     * 
     * @param size size value
     * @param units units of {@code size} value
     */
    public ByteSize(long size, ByteSizeUnits units)
    {
        this();
        this.bytes.set(size * units.getMu());
    }
    
    /**
     * Constructs new instance with size {@code size} bytes.
     * 
     * @param size size in bytes
     */
    public ByteSize(long size)
    {
        this(size, ByteSizeUnits.B);
    }
    
    /**
     * Constructs new instance with size set from converting {@code size}
     * to bytes using {@code units}.
     * 
     * @param size size value
     * @param units units of {@code size} value
     */
    public ByteSize(double size, ByteSizeUnits units)
    {
        this();
        this.bytes.set((long) (size * units.getMu()));
    }
    
    /**
     * Binds human readable property value to {@code bytes} value.
     */
    private void addHumanReadableListener()
    {
        this.bytes.addListener((observable, oldVal, newVal) ->
        {
            humanReadable.set(getHumanReadableFormat());
        });
    }
    
    /**
     * Binds size multiples to {@code bytes} value.
     */
    private void bindUnitMultiples()
    {
        this.kiloBytes.bind(this.bytes.divide((double) ByteSizeUnits.KB.getMu()));
        this.megaBytes.bind(this.bytes.divide((double) ByteSizeUnits.MB.getMu()));
        this.gigaBytes.bind(this.bytes.divide((double) ByteSizeUnits.GB.getMu()));
    }
    
    /**
     * Set size in bytes.
     * 
     * @param b size in bytes
     */
    public void setBytes(long b)
    {
        this.bytes.set(b);
    }
    
    /**
     * Returns a writable {@link LongProperty} with size in bytes.
     * 
     * @return size in bytes
     */
    public LongProperty bytesProperty()
    {
        return this.bytes;
    }
    
    /**
     * Set size from kilobytes.
     * 
     * @param kb size in kilobytes
     */
    public void setKiloBytes(double kb)
    {
        this.bytes.set( (long) (kb * ByteSizeUnits.KB.getMu()) );
    }
    
    /**
     * Returns a writable {@link DoubleProperty} with size in kilobytes.
     * 
     * @return size in kilobytes
     */
    public DoubleProperty kiloBytesProperty()
    {
        return this.kiloBytes;
    }
    
    /**
     * Set size from megabytes.
     * 
     * @param mb size in megabytes
     */
    public void setMegaBytes(double mb)
    {
        this.bytes.set( (long) (mb * ByteSizeUnits.MB.getMu()) );
    }
    
    /**
     * Returns a writable {@link DoubleProperty} with size in megabytes.
     * 
     * @return size in megabytes
     */
    public DoubleProperty megaBytesProperty()
    {
        return this.megaBytes;
    }
    
    /**
     * Set size from gigabytes.
     * 
     * @param gb size in gigabytes
     */
    public void setGigaBytes(double gb)
    {
        this.bytes.set( (long) (gb * ByteSizeUnits.GB.getMu()) );
    }
    
    /**
     * Returns a writable {@link DoubleProperty} with size in gigabytes.
     * 
     * @return size in gigabytes
     */
    public DoubleProperty gigaBytesProperty()
    {
        return this.gigaBytes;
    }
    
    /**
     * Returns a human readable representation of size.
     * 
     * Based on http://stackoverflow.com/a/3758880
     * 
     * @param si flag indicating whether value should be converted
     * using SI units (1000) or not (1024)
     * @return human readable format
     */
    public String getHumanReadableFormat(boolean si)
    {
        int mu = (si) ? 1000 : 1024;
        
        if(this.bytes.get() < mu)
        {
            return this.bytes.get() + " B";
        }
        
        int exp = (int) (Math.log(this.bytes.get()) / Math.log(mu));
        String pre = (si ? "kmgTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        
        return String.format("%.1f %sB", this.bytes.get() / Math.pow(mu, exp), pre);
    }
    
    /**
     * Returns a human readable representation of size in SI units (1000 vs 1024).
     * 
     * @return human readable format
     */
    public String getHumanReadableFormat()
    {
        return this.getHumanReadableFormat(true);
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
     * Return the largest possible prefix for this instance's size.
     * 
     * @param si SI units
     * @return largest possible prefix
     */
    public ByteSizeUnits getNominalUnits(boolean si)
    {
        int mu = (si) ? 1000 : 1024;
        
        if(this.bytes.get() < mu)
        {
            return ByteSizeUnits.B;
        }
        else if(this.bytes.get() < (mu * mu) )
        {
            return ByteSizeUnits.KB;
        }
        else if(this.bytes.get() < (mu * mu * mu))
        {
            return ByteSizeUnits.MB;
        }
        else 
        {
            return ByteSizeUnits.GB;
        }
    }
    
    /**
     * Return the largest possible prefix for this instance's size.
     * 
     * @return largest possible prefix
     */
    public ByteSizeUnits getNominalUnits()
    {
        return this.getNominalUnits(true);
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
