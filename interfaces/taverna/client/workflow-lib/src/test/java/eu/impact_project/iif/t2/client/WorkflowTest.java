/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.impact_project.iif.t2.client;

import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Impact
 */
public class WorkflowTest
{    

    /**
     * Test of getStringVersion method, of class Workflow.
     */
    @Test
    public void testGets()
    {        
        Workflow instance = new Workflow();
        try
        {
            instance.setStringVersion("pru1");
            instance.setUrls("http://www.ua.es");
            instance.setWsdls("<wsdl>http://www.ua.es</wsdl>");
            
            List<WorkflowInput> winputs = new ArrayList<>();
            WorkflowInput winput = new WorkflowInput("pruInput");
            winput.setBinary(true);
            winput.setDepth(1);
            winput.setExampleValue("Hello");
            
            WorkflowInput winput2 = new WorkflowInput();
            winput2.setName("pruInput2");
            winput2.setBinary(true);
            winput2.setDepth(1);
            winput2.setExampleValue("Hello");
            
            assertEquals("pruInput2", winput2.getName());
            assertEquals("Hello", winput2.getExampleValue());
            assertEquals(true, winput2.isBinary());
            assertEquals(1, winput2.getDepth());
            
            winputs.add(winput);
            winputs.add(winput2);
            
            instance.setInputs(winputs);                        
            
        }
        catch (Exception e)
        {
            fail("Should not raise exception "+e.toString());
        }
        
        instance.getSecurity("");
        instance.getStringVersion();
        instance.getUrls();
        instance.getWsdls();
        instance.getInputs();        
        
    }

    /**
     * Test of testUrls method, of class Workflow.
     */
    @Test
    public void testTestUrls()
    {
        Workflow instance = new Workflow("pru1");
        try
        {            
            instance.setUrls("http://falsa");
            instance.setUrls("https://github.com/noexist");
            instance.testUrls(instance.getUrls());
        }
        catch (Exception e)
        {
            fail("Should not raise exception "+e.toString());
        }
        
    }
    
}
