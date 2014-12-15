/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.impact_project.wsclient;

import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Impact
 */
public class SOAPoperationTest
{        

    /**
     * Test of getName method, of class SOAPoperation.
     */
    @Test
    public void testGetName()
    {
        System.out.println("getName");
        SOAPoperation instance = new SOAPoperation("prueba");
        String expResult = "prueba";
        String result = instance.getName();
        assertEquals(expResult, result);        
    }

    /**
     * Test of setName method, of class SOAPoperation.
     */
    @Test
    public void testSetName()
    {
        System.out.println("setName");
        String name = "prueba";
        SOAPoperation instance = new SOAPoperation(name, null);
        instance.setName(name);
        
    }

    /**
     * Test of getInputs method, of class SOAPoperation.
     */
    @Test
    public void testGetInputs()
    {
        System.out.println("getInputs");
        SOAPoperation instance = new SOAPoperation("prueba");
        List<SOAPinputField> expResult = null;
        List<SOAPinputField> result = instance.getInputs();
        assertEquals(expResult, result);        
    }

    /**
     * Test of setInputs method, of class SOAPoperation.
     */
    @Test
    public void testSetInputs()
    {
        System.out.println("setInputs");
        List<SOAPinputField> inputs = null;
        SOAPoperation instance = new SOAPoperation("prueba");
        instance.setInputs(inputs);
        
    }

    /**
     * Test of getDefaultMessage method, of class SOAPoperation.
     */
    @Test
    public void testGetDefaultMessage()
    {
        System.out.println("getDefaultMessage");
        SOAPoperation instance = new SOAPoperation("prueba");        
        String result = instance.getDefaultMessage();        
    }

    /**
     * Test of setDefaultMessage method, of class SOAPoperation.
     */
    @Test
    public void testSetDefaultMessage()
    {
        System.out.println("setDefaultMessage");
        String defaultMessage = "";
        SOAPoperation instance = new SOAPoperation("prueba");
        instance.setDefaultMessage(defaultMessage);       
    }

    /**
     * Test of getDocumentation method, of class SOAPoperation.
     */
    @Test
    public void testGetDocumentation()
    {
        System.out.println("getDocumentation");
        SOAPoperation instance = new SOAPoperation("prueba");        
        String result = instance.getDocumentation();        
    }

    /**
     * Test of setDocumentation method, of class SOAPoperation.
     */
    @Test
    public void testSetDocumentation()
    {
        System.out.println("setDocumentation");
        String documentation = "";
        SOAPoperation instance = new SOAPoperation("prueba");
        instance.setDocumentation(documentation);        
    }
    
}
