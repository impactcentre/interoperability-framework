/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.impact_project.iif.tw.gen;

import eu.impact_project.iif.tw.util.FileUtil;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Impact
 */
public class DeploymentCreatorTest
{
    
    @Test
    public void testErrorCantReadPom() 
    {
        DeploymentCreator dc = new DeploymentCreator("", null, null);
        try
        {
            dc.createPom();
            fail("Must raise GeneratorException");
        } 
        catch (GeneratorException ex)
        {
            //test OK
        }
        
    }           
    
}
