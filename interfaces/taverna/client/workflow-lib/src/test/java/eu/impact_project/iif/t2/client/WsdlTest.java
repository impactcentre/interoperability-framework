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
public class WsdlTest
{        

    /**
     * Test of setUrl method, of class Wsdl.
     */
    @Test
    public void testGets()
    {        
        Wsdl instance = new Wsdl("http://www", "user", "pass");
        instance.setUrl("http://www");
        instance.setUser("user");
        instance.setPass("user");
        
        assertEquals("http://www", instance.getUrl());
        assertEquals("user", instance.getUser());
        assertEquals("pass", instance.getPass());
    }

    
    
}
