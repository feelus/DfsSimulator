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

package cz.zcu.kiv.dfs_simulator.persistence;

import java.util.ArrayList;
import java.util.List;

/**
 * String persistence logger.
 */
public class SimpleStatePersistenceLogger extends StatePersistenceLogger
{
    /**
     * Logged messages
     */
    private final List<SimplePersistenceMessage> messages = new ArrayList<>();

    /**
     * {@inheritDoc}
     */
    @Override public void logOperation(String elementName, String message, boolean success)
    {
        this.messages.add(this.getFormattedMessage(elementName, message, success));
    }
    
    /**
     * Returns formatted log message.
     * 
     * @param elementName element id
     * @param message message
     * @param success success flag
     * @return formatted message
     */
    private SimplePersistenceMessage getFormattedMessage(String elementName, String message, boolean success)
    {
        String status = (success) ? "OK" : "ERROR";
        String formatted = status + " [" + elementName + "] - " + message;
        
        return new SimplePersistenceMessage(formatted, success);
    }
    
    /**
     * Returns all logged messages.
     * 
     * @return messages
     */
    public List<SimplePersistenceMessage> getMessages()
    {
        return this.messages;
    }
    
}
