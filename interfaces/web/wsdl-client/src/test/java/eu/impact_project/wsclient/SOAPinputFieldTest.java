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
public class SOAPinputFieldTest
{        

    /**
     * Test of getMultipleSelectValues method, of class SOAPinputField.
     */
    @Test
    public void testGetMultipleSelectValues()
    {
        System.out.println("getMultipleSelectValues");
        SOAPinputField instance = new SOAPinputField("prueba");       
        List<String> result = instance.getMultipleSelectValues();        
    }

    /**
     * Test of addMultipleSelectValue method, of class SOAPinputField.
     */
    @Test
    public void testAddMultipleSelectValue()
    {
        System.out.println("addMultipleSelectValue");
        String value = "";
        SOAPinputField instance = new SOAPinputField("prueba","prueba");  
        instance.addMultipleSelectValue(value);        
    }

    /**
     * Test of getDefaultValue method, of class SOAPinputField.
     */
    @Test
    public void testGetDefaultValue()
    {
        System.out.println("getDefaultValue");
        SOAPinputField instance = new SOAPinputField("prueba","prueba",true);  
        String expResult = "";
        String result = instance.getDefaultValue();              
    }

    /**
     * Test of setDefaultValue method, of class SOAPinputField.
     */
    @Test
    public void testSetDefaultValue()
    {
        System.out.println("setDefaultValue");
        String defaultValue = "";
        SOAPinputField instance = new SOAPinputField("prueba");  
        instance.setDefaultValue(defaultValue);        
    }

    /**
     * Test of getName method, of class SOAPinputField.
     */
    @Test
    public void testGetName()
    {
        System.out.println("getName");
        SOAPinputField instance = new SOAPinputField("prueba");  
        String expResult = "";
        String result = instance.getName();        
    }

    /**
     * Test of setName method, of class SOAPinputField.
     */
    @Test
    public void testSetName()
    {
        System.out.println("setName");
        String name = "";
        SOAPinputField instance = new SOAPinputField("prueba");  
        instance.setName(name);        
    }

    /**
     * Test of getDocumentation method, of class SOAPinputField.
     */
    @Test
    public void testGetDocumentation()
    {
        System.out.println("getDocumentation");
        SOAPinputField instance = new SOAPinputField("prueba");  
        String expResult = "";
        String result = instance.getDocumentation();        
    }

    /**
     * Test of setDocumentation method, of class SOAPinputField.
     */
    @Test
    public void testSetDocumentation()
    {
        System.out.println("setDocumentation");
        String documentation = "";
        SOAPinputField instance = new SOAPinputField("prueba");  
        instance.setDocumentation(documentation);        
    }

    /**
     * Test of isBinary method, of class SOAPinputField.
     */
    @Test
    public void testIsBinary()
    {
        System.out.println("isBinary");
        SOAPinputField instance = new SOAPinputField("prueba");  
        boolean expResult = false;
        boolean result = instance.isBinary();        
    }

    /**
     * Test of setBinary method, of class SOAPinputField.
     */
    @Test
    public void testSetBinary()
    {
        System.out.println("setBinary");
        boolean binary = false;
        SOAPinputField instance = new SOAPinputField("prueba");  
        instance.setBinary(binary);        
    }

    /**
     * Test of setPossibleValues method, of class SOAPinputField.
     */
    @Test
    public void testSetPossibleValues()
    {
        System.out.println("setPossibleValues");
        List<String> possibleValues = null;
        SOAPinputField instance = new SOAPinputField("prueba");  
        instance.setPossibleValues(possibleValues);       
    }

    /**
     * Test of getPossibleValues method, of class SOAPinputField.
     */
    @Test
    public void testGetPossibleValues()
    {
        System.out.println("getPossibleValues");
        SOAPinputField instance = new SOAPinputField("prueba");  
        List<String> expResult = null;
        List<String> result = instance.getPossibleValues();        
    }
    
}
