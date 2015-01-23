/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.impact_project.iif.t2.client;

import java.io.IOException;
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
public class WorkflowDownloaderTest
{   
    /**
     * Test of doGet method, of class WorkflowDownloader.
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
        when(request.getParameterValues("id")).thenReturn(new String[]{"914"});
        when(response.getWriter()).thenReturn(null);                
        
        when(config.getServletContext()).thenReturn(context);        
        when(context.getRequestDispatcher("/")).thenReturn(dispatcher);                
        
        WorkflowDownloader downloader = new WorkflowDownloader();
        try
        {
            downloader.init(config);
            downloader.doGet(request, response);

            //verify(request, atLeast(1)).getParameter("username"); // only if you want to verify username was called...                
        } catch (ServletException ex)
        {
            fail("Should not raise exception " + ex.toString());
        } catch (IOException ex)
        {
            fail("Should not raise exception " + ex.toString());
        }
        catch (Exception ex)
        {
            
        }
    }
    
}
