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
import java.util.Optional;

/**
 * State persisted element.
 */
public class StatePersistableElement
{
    /**
     * Element name
     */
    protected final String name;
    
    /**
     * Element attributes
     */
    protected final List<StatePersistableAttribute> attributes = new ArrayList<>();
    /**
     * Element children
     */
    protected final List<StatePersistableElement> elements = new ArrayList<>();
    
    /**
     * State persisted element.
     * 
     * @param name element name
     */
    public StatePersistableElement(String name)
    {
        this.name = name;
    }
    
    /**
     * Add element attribute.
     * 
     * @param attribute attribute
     */
    public void addAttribute(StatePersistableAttribute attribute)
    {
        this.attributes.add(attribute);
    }
    
    /**
     * Add child element.
     * 
     * @param element element
     */
    public void addElement(StatePersistableElement element)
    {
        this.elements.add(element);
    }
    
    /**
     * Get attribute by name.
     * 
     * @param name attribute name
     * @return found attribute or null
     */
    public StatePersistableAttribute getAttribute(String name)
    {
        
        Optional<StatePersistableAttribute> attr =  this.attributes.stream().filter(x -> x.getName().equals(name)).findFirst();
        
        if(attr != null && attr.isPresent())
        {
            return attr.get();
        }
        
        return null;
    }
    
    /**
     * Get child element by name.
     * 
     * @param name child element name
     * @return found element or null
     */
    public StatePersistableElement getElement(String name)
    {
        Optional<StatePersistableElement> elemm =  this.elements.stream().filter(x -> x.getName().equals(name)).findFirst();
        
        if(elemm != null && elemm.isPresent())
        {
            return elemm.get();
        }
        
        return null;
    }
    
    /**
     * Get element and remove it from children.
     * 
     * @param name element name
     * @return found element or null
     */
    public StatePersistableElement getAndRemoveElement(String name)
    {
        StatePersistableElement element = this.getElement(name);
        if(element != null)
        {
            this.elements.remove(element);
        }
        
        return element;
    }

    /**
     * Get element name.
     * 
     * @return element name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Returns element attributes.
     * 
     * @return element attributes
     */
    public List<StatePersistableAttribute> getAttributes()
    {
        return attributes;
    }

    /**
     * Returns element child elements.
     * 
     * @return child elements
     */
    public List<StatePersistableElement> getElements()
    {
        return elements;
    }
    
    /**
     * Textual representation of element.
     * 
     * @return string representation
     */
    @Override public String toString()
    {
        return "Persistable element [" + this.name + "]";
    }
}
