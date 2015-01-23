/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.impact_project.iif.t2.client;

import java.net.URI;
import java.net.URISyntaxException;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import uk.org.taverna.server.client.OutputPort;

/**
 *
 * @author Impact
 */
public class WorkflowOutputTest
{
    
    
    /**
     * Test of getValue method, of class WorkflowOutput.
     */
    @Test
    public void testGets() throws URISyntaxException
    {
        
        WorkflowOutput instance = new WorkflowOutput();
        
        instance.setBinary(true);
        instance.setUrl("prueba");
        instance.setValue("prueba");
        
        assertEquals(true, instance.isBinary());
        assertEquals("prueba", instance.getUrl());
        assertEquals("prueba", instance.getValue());   
                
    }

    
    
}
