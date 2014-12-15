/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.impact_project.iif.tw.conf;

import eu.impact_project.iif.tw.gen.GeneratorException;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Impact
 */
public class ConfigurationTest
{           

    /**
     * Test of setProjConf method, of class Configuration.
     */
    @Test
    public void testSetProjConfError() 
    {        
        File confFile = new File("");
        Configuration instance = new Configuration();
        try
        {
            instance.setProjConf(confFile);
            fail("Should raise exception");
        } catch (GeneratorException ex)
        {
            //test ok
        }        
    }

    /**
     * Test of setXmlConf method, of class Configuration.
     */
    @Test
    public void testSetXmlConfError() throws Exception
    {
        File confFile = new File("");
        Configuration instance = new Configuration();
        try
        {
            instance.setXmlConf(confFile);
            fail("Should raise exception");
        } catch (GeneratorException ex)
        {
            //test ok
        }  
    }   
    
}
