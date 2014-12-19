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
public class FilePrinterTest
{        

    /**
     * Test of doGet method, of class FilePrinter.
     */
    @Test
    public void testDoGet() throws Exception
    {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);        
        ServletConfig config = mock(ServletConfig.class);
        ServletContext context = mock(ServletContext.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        ServletOutputStream stream = mock(ServletOutputStream.class);

        when(config.getServletContext()).thenReturn(context);        
        when(context.getRequestDispatcher("/interface.jsp")).thenReturn(dispatcher);        
        URL url = this.getClass().getResource("/prueba.txt");        
        File testFile = new File(url.getFile());
        when(context.getAttribute("javax.servlet.context.tempdir")).thenReturn(new File(testFile.getParent()));        
        when(response.getOutputStream()).thenReturn(stream);
        
        FilePrinter file = new FilePrinter();
        try
        {
            file.init(config);
            file.doGet(request, response);

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
     * Test of doPost method, of class FilePrinter.
     */
    @Test
    public void testDoPost() throws Exception
    {        
        HttpServletRequest request = null;
        HttpServletResponse response = null;
        FilePrinter instance = new FilePrinter();
        instance.doPost(request, response);        
    }
    
}
