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

/**
 * General purpose methods.
 */
public class Helper
{
    /**
     * Checks whether input string is convertible to double.
     * 
     * @param string input string
     * @return true if string is convertible to double, false otherwise
     */
    public static boolean isDouble(String string)
    {
        try
        {
            Double.parseDouble(string);
            return true;
        }
        catch(NumberFormatException ex)
        {
            return false;
        }
    }
    
    /**
     * Checks whether input string is convertible to integer.
     * 
     * @param string input string
     * @return true if string is convertible to integer, false otherwise
     */
    public static boolean isInteger(String string)
    {
        try
        {
            Integer.parseInt(string);
            return true;
        }
        catch(NumberFormatException ex)
        {
            return false;
        }
    }
    
    /**
     * Checks whether input string is convertible to long.
     * 
     * @param string input string
     * @return true if string is convertible to long, false otherwise
     */
    public static boolean isLong(String string)
    {
        try
        {
            Long.parseLong(string);
            return true;
        }
        catch(NumberFormatException ex)
        {
            return false;
        }
    }
}
