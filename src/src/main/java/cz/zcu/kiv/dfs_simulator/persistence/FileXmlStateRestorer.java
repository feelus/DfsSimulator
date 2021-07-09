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

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Restore state from XML file.
 */
public class FileXmlStateRestorer extends StateRestorer<File>
{
    /**
     * Logging information prefix
     */
    protected static final String LOG_IDENTIFIER = "XML_RESTORER";
    
    /**
     * Persistence logger
     */
    protected StatePersistenceLogger logger;
    
    /**
     * Construct XML file restorer.
     * 
     * @param logger persistence logger
     */
    public FileXmlStateRestorer(StatePersistenceLogger logger)
    {
        this.logger = logger;
    }

    /**
     * {@inheritDoc}
     */
    @Override public boolean restore(File state, StatePersistable root) throws InvalidPersistedStateException
    {
        this.logger.logOperation(LOG_IDENTIFIER, "Attempting to parse configuration.", true);
        
        try
        {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(state);
            
            doc.getDocumentElement().normalize();
            
            this.traverseAndRestoreElement(doc.getDocumentElement(), root);
            
            return true;
        }
        catch (ParserConfigurationException | SAXException | IOException ex)
        {
            Logger.getLogger(FileXmlStateRestorer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        this.logger.logOperation(LOG_IDENTIFIER, "Error parsing simulator configuration", false);
        
        return false;
    }
    
    /**
     * Restore element and all it's child elements.
     * 
     * @param root XML root element
     * @param persistableNode persistable node
     * @throws InvalidPersistedStateException thrown when the persisted state is invalid
     */
    private void traverseAndRestoreElement(Node root, StatePersistable persistableNode) throws InvalidPersistedStateException
    {
        this.logger.logOperation(LOG_IDENTIFIER, "Validating XML root element.", true);
        
        if(root.getNodeName().equals(persistableNode.getPersistableName()))
        {
            StatePersistableElement nodeState = this.createTraversedElementStructure(root);
            
            persistableNode.restoreState(nodeState, this.logger);
        }
        else
        {
            this.logger.logOperation(LOG_IDENTIFIER, "Invalid XML root.", false);
            
            throw new InvalidPersistedStateException("Unexpected XML root element name");
        }
    }
    
    /**
     * Parse {@code rootNode} into {@link StatePersistableElement}.
     * 
     * @param rootNode XML element
     * @return persistable element state
     */
    private StatePersistableElement createTraversedElementStructure(Node rootNode)
    {
        StatePersistableElement rootElem = new StatePersistableElement(rootNode.getNodeName());
        
        NamedNodeMap attrs = rootNode.getAttributes();
        
        for(int i = 0; i < attrs.getLength(); i++)
        {
            Node attr = attrs.item(i);

            rootElem.addAttribute(new StatePersistableAttribute(attr.getNodeName(), attr.getNodeValue()));
        }
        
        NodeList children = rootNode.getChildNodes();

        for(int i = 0; i < children.getLength(); i++)
        {
            Node elem = children.item(i);

            // ignore any extra data
            if(elem.getNodeType() == Node.ELEMENT_NODE)
            {
                rootElem.addElement(this.createTraversedElementStructure(elem));
            }
        }
        
        return rootElem;
    }
        
}
