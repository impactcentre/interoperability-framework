/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.impact_project.wsclient;

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
public class GeneratePagesTest
{
        
    /**
     * Test of doGet method, of class GeneratePages.
     */
    @Test
    public void testDoGet() throws Exception
    {        
        HttpServletRequest request = null;
        HttpServletResponse response = null;
        GeneratePages instance = new GeneratePages();
        instance.doGet(request, response);
        
    }

    /**
     * Test of doPost method, of class GeneratePages.
     */
    @Test
    public void testDoPost() throws Exception
    {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession sesion = mock(HttpSession.class);
        ServletConfig config = mock(ServletConfig.class);
        ServletContext context = mock(ServletContext.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        ServletOutputStream stream = mock(ServletOutputStream.class);

        when(config.getServletContext()).thenReturn(context);
        when(sesion.getServletContext()).thenReturn(context);        
        when(context.getRequestDispatcher("/prueba.jsp")).thenReturn(dispatcher);
        //when(context.getAttribute("FILES_DIR")).thenReturn("");        
        URL url = this.getClass().getResource("/prueba.txt");        
        File testFile = new File(url.getFile());
        when(context.getRealPath("/")).thenReturn(testFile.getParent()+"/");
        when(request.getParameter("wsId")).thenReturn("/prueba?");        
        when(request.getSession()).thenReturn(sesion);
        when(request.getSession(true)).thenReturn(sesion);
        when(response.getOutputStream()).thenReturn(stream);
        
        GeneratePages pages = new GeneratePages();
        try
        {
            pages.init(config);
            pages.doPost(request, response);

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
