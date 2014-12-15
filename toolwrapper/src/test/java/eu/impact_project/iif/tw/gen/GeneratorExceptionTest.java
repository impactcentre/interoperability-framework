/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.impact_project.iif.tw.gen;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Impact
 */
public class GeneratorExceptionTest
{        

    @Test
    public void testConstructors()
    {
        GeneratorException ex = new GeneratorException();
        GeneratorException ex2 = new GeneratorException("prueba", ex);
    }
    
}
