/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.impact_project.wsclient;

import java.util.Iterator;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Impact
 */
public class WSDLNamespaceContextTest
{        

    /**
     * Test of getNamespaceURI method, of class WSDLNamespaceContext.
     */
    @Test
    public void testGetNamespaceURI()
    {
        WSDLNamespaceContext context = new WSDLNamespaceContext();
        
        context.getNamespaceURI("wsdl");
        context.getNamespaceURI("xmime");
        context.getNamespaceURI("xsd");
        context.getNamespaceURI("xml");
        context.getNamespaceURI("soap");
        context.getNamespaceURI("null");
        
        context.getPrefix(null);
        context.getPrefixes(null);
        try
        {
            context.getNamespaceURI(null);
        }
        catch(NullPointerException ex)
        {
            
        }                        
    }   
    
}
