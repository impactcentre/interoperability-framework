/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.impact_project.iif.tw.util;

import eu.impact_project.iif.tw.gen.GeneratorException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Impact
 */
public class PropertyUtilTest
{
    
    PropertyUtil pu = null;        
    
    @Test
    public void testReadPropertyFileOk()
    {
        try
        {
            pu = new PropertyUtil("toolwrapper.properties");
        } 
        catch (GeneratorException ex)
        {
            fail("IO error");
        }
        
    }
    
    @Test
    public void testReadPropertyFileFail()
    {
        try
        {
            pu = new PropertyUtil("");
            fail("Should raise a GeneratorException");
        } 
        catch (GeneratorException ex)
        {
            //test OK
        }
        
    }

    
    
}
