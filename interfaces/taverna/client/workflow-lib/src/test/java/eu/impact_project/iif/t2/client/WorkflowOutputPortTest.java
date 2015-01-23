/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.impact_project.iif.t2.client;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import uk.org.taverna.server.client.OutputPort;
import uk.org.taverna.server.client.PortListValue;
import uk.org.taverna.server.client.Run;

/**
 *
 * @author Impact
 */
public class WorkflowOutputPortTest
{        

    /**
     * Test of getName method, of class WorkflowOutputPort.
     */
    @Test
    public void testGets()
    {        
        WorkflowOutputPort instance = new WorkflowOutputPort("prueba");
        
        instance = new WorkflowOutputPort();
        
        instance.setName("prueba");
        instance.setOutput("prueba", "prueba", "prueba");
        instance.setOutput(new WorkflowOutput());
        
        List<WorkflowOutput> lista = new ArrayList<WorkflowOutput>(); 
        instance.setOutputs(lista);
        
        instance.getName();
        instance.getOutputs();                
    }

    
    
}
