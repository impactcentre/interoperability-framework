/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.impact_project.iif.t2.client;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Impact
 */
public class WorkflowInfoTest
{
    
    
    /**
     * Test of getWfId method, of class WorkflowInfo.
     */
    @Test
    public void testGets()
    {
        
        WorkflowInfo instance = new WorkflowInfo("prueba");
        instance = new WorkflowInfo();
        
        instance.setTitle("prueba");
        instance.setWfId("prueba");
        
        assertEquals("prueba", instance.getTitle());
        assertEquals("prueba", instance.getWfId());
    }

    
    
}
