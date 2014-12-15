/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.impact_project.iif.tw.cli;

import eu.impact_project.iif.tw.gen.GeneratorException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 *
 * @author Impact
 */
public class ToolWrapperTest
{
    
    public ToolWrapperTest()
    {
    }

    /**
     * Test of main method, of class ToolWrapper.
     */
    @Test
    public void testMain() throws Exception
    {
        System.out.println("main");
        String[] args = new String[0];
        ToolWrapper.main(args);        
    }
    
    /**
     * Test of main method, of class ToolWrapper.
     */
    @Test
    public void testMainFail()
    {        
        String[] args = new String[1];
        args[0]="-i";
        try        
        {
            ToolWrapper.main(args);
            fail("Should raise exception");
        } catch (Exception ex)
        {
            //test OK
        }
    }
    
}
