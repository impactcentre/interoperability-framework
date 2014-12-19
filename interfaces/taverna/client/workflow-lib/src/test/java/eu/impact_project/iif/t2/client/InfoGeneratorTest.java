/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.impact_project.iif.t2.client;

import java.io.File;
import java.io.IOException;
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
public class InfoGeneratorTest
{          

    /**
     * Test of doGet method, of class InfoGenerator.
     */
    @Test
    public void testDoGet() throws Exception
    {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class); 
        HttpSession sesion = mock(HttpSession.class);
        ServletConfig config = mock(ServletConfig.class);
        ServletContext context = mock(ServletContext.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        ServletOutputStream stream = mock(ServletOutputStream.class);

        when(request.getSession(true)).thenReturn(sesion);
        when(request.getParameter("id")).thenReturn("16");
        //when(request.getParameter("id")).thenReturn("4555");
        when(sesion.getAttribute("user")).thenReturn(null);
        when(sesion.getAttribute("password")).thenReturn(null);
        
        when(config.getServletContext()).thenReturn(context);        
        when(context.getRequestDispatcher("/info.jsp")).thenReturn(dispatcher);                
        
        InfoGenerator info = new InfoGenerator();
        try
        {
            info.init(config);
            info.doGet(request, response);

            //verify(request, atLeast(1)).getParameter("username"); // only if you want to verify username was called...                
        } catch (ServletException ex)
        {
            fail("Should not raise exception " + ex.toString());
        } catch (IOException ex)
        {
            fail("Should not raise exception " + ex.toString());
        }
    }

    /**
     * Test of doPost method, of class InfoGenerator.
     */
    @Test
    public void testDoPost() throws Exception
    {
        
        HttpServletRequest request = null;
        HttpServletResponse response = null;
        InfoGenerator instance = new InfoGenerator();
        instance.doPost(request, response);
        
    }
    
}
