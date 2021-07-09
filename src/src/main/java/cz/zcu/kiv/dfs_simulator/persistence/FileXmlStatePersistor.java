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
import java.util.LinkedList;
import java.util.Queue;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Persists state into XML file.
 */
public class FileXmlStatePersistor extends StatePersistor<File>
{
    /**
     * Output file
     */
    private final File file;
    /**
     * Persistence logger
     */
    private final StatePersistenceLogger logger;
    
    /**
     * Construct XML file persistor.
     * 
     * @param file output file
     * @param logger persistence logger
     */
    public FileXmlStatePersistor(File file, StatePersistenceLogger logger)
    {
        this.file =  file;
        this.logger = logger;
    }

    /**
     * {@inheritDoc}
     */
    @Override public boolean persist(StatePersistable root) throws ParserConfigurationException, 
            TransformerException
    {
        DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = fac.newDocumentBuilder();

        Document doc = builder.newDocument();

        this.traverseAndPersistNode(doc, doc, root);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(this.file);

        // format output
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        transformer.transform(source, result);

        return true;
    }
    
    /**
     * Persist node and all it's children.
     * 
     * @param doc output document
     * @param root XML root element
     * @param persistableNode persistable node
     */
    private void traverseAndPersistNode(Document doc, Node root, StatePersistable persistableNode)
    {
        Element nodeElement = this.traverseAndPersistElement(doc, root, persistableNode.export(this.logger));
        
        Queue<StatePersistable> q = new LinkedList<>();
        q.addAll(persistableNode.getPersistableChildren());
        
        while(!q.isEmpty())
        {
            StatePersistable cur = q.poll();
            
            this.traverseAndPersistNode(doc, nodeElement, cur);
        }
    }
    
    /**
     * Persist element and all it's child elements.
     * 
     * @param doc output document
     * @param root XML root element
     * @param element persistable element
     * @return 
     */
    private Element traverseAndPersistElement(Document doc, Node root, StatePersistableElement element)
    {
        Element docElement = doc.createElement(element.getName());
        root.appendChild(docElement);
        
        for(StatePersistableAttribute attr : element.getAttributes())
        {
            docElement.setAttribute(attr.getName(), attr.getValue());
        }
        
        for(StatePersistableElement ele : element.getElements())
        {
            this.traverseAndPersistElement(doc, docElement, ele);
        }
        
        return docElement;
    }

    /**
     * {@inheritDoc}
     */
    @Override public File getPersistedState()
    {
        return this.file;
    }
    
}
