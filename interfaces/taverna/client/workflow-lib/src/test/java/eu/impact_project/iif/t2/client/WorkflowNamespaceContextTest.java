/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.impact_project.iif.t2.client;

import java.util.Iterator;
import javax.xml.XMLConstants;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Impact
 */
public class WorkflowNamespaceContextTest
{
    
    public WorkflowNamespaceContextTest()
    {
    }

    /**
     * Test of getNamespaceURI method, of class WorkflowNamespaceContext.
     */
    @Test
    public void testGetNamespaceURI()
    {        
        WorkflowNamespaceContext instance = new WorkflowNamespaceContext();
        String result;
          
        try
        {
            result = instance.getNamespaceURI(null);
            fail("Should raise exception");
        }
        catch (NullPointerException ex)
        {
            //test ok
        }
        
        result = instance.getNamespaceURI("t");
        assertEquals("http://taverna.sf.net/2008/xml/t2flow", result);
        
        result = instance.getNamespaceURI("xml");
        assertEquals(XMLConstants.XML_NS_URI, result);
        
        result = instance.getNamespaceURI("");
        assertEquals(XMLConstants.NULL_NS_URI, result);
                
    }

    /**
     * Test of getPrefix method, of class WorkflowNamespaceContext.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testGetPrefix()
    {        
        WorkflowNamespaceContext instance = new WorkflowNamespaceContext();        
        String result = instance.getPrefix(null);
        
    }

    /**
     * Test of getPrefixes method, of class WorkflowNamespaceContext.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testGetPrefixes()
    {
        
        WorkflowNamespaceContext instance = new WorkflowNamespaceContext();        
        Iterator result = instance.getPrefixes(null);
        
    }
    
}
