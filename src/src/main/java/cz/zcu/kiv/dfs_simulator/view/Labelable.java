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

package cz.zcu.kiv.dfs_simulator.view;

import cz.zcu.kiv.dfs_simulator.model.LabelException;

/**
 * Node can be labeled.
 */
public interface Labelable
{
    /**
     * Set label text.
     * 
     * @param label label text
     * @throws LabelException thrown when {@code label} is invalid.
     */
    public void setLabelText(String label) throws LabelException;
    /**
     * Get label text.
     * 
     * @return label text
     */
    public String getLabelText();
}
