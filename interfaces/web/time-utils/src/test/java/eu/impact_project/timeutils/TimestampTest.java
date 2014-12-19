/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.impact_project.timeutils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author Impact
 */
public class TimestampTest
{
        

    /**
     * Test of doGet method, of class Timestamp.
     */
    @org.junit.Test
    public void testDoGet() throws Exception
    {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletConfig config = mock(ServletConfig.class);
        PrintWriter out = new PrintWriter(System.out);
        
        when(response.getWriter()).thenReturn(out);                
        
        Timestamp time = new Timestamp();
        try
        {
            time.init(config);
            time.doGet(request, response);

            //verify(request, atLeast(1)).getParameter("username"); // only if you want to verify username was called...                
        } catch (ServletException ex)
        {
            fail("Should not raise exception " + ex.toString());
        } catch (IOException ex)
        {
            fail("Should not raise exception " + ex.toString());
        }
        
    }
    
}
