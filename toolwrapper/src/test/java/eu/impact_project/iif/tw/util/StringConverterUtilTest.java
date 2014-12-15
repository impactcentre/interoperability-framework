/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.impact_project.iif.tw.util;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Impact
 */
public class StringConverterUtilTest
{
        

    @Test
    public void testConstructor()
    {
        StringConverterUtil instance = new StringConverterUtil();
        assertNotEquals(instance, null);
    }
    
    /**
     * Test of varToProp method, of class StringConverterUtil.
     */
    @Test
    public void testVarToProp()
    {        
        String var = "var1_prop1";
        String expResult = "var1.prop1";
        String result = StringConverterUtil.varToProp(var);
        assertEquals(expResult, result);                
    }

    
    
}
