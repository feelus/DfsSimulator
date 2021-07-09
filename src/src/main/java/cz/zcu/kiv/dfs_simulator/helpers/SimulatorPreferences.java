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

import java.util.prefs.Preferences;

/**
 * Provides basic functionality regarding user's preferences.
 */
public class SimulatorPreferences
{
    /**
     * Reference to {@link Preferences} used for storing user preferences
     * across whole simulator.
     */
    private static final Preferences PREFS = Preferences.userNodeForPackage(SimulatorPreferences.class);
    
    /**
     * Preference key for user's last open directory (eg. last directory
     * that was imported from)
     */
    public static final String LAST_OPEN_DIR = "last_open_dir";

    
    /**
     * Get last successfully open directory (eg. directory that was imported
     * from)
     * 
     * @return directory path
     */
    public static String getLastOpenDir()
    {
        return PREFS.get(LAST_OPEN_DIR, null);
    }
    
    /**
     * Set last open directory.
     * 
     * @param path directory path
     */
    public static void setLastOpenDir(String path)
    {
        PREFS.put(LAST_OPEN_DIR, path);
    }
}
