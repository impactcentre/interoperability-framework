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
public class GroupSelectorTest
{
    
    

    @Test
    public void testDoGet() throws Exception
    {
        HttpServletRequest request = null;
        HttpServletResponse response = null;
        GroupSelector instance = new GroupSelector();
        instance.doGet(request, response);                
    }

    
    
    /**
     * Test of doPost method, of class InfoGenerator.
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

        when(request.getSession(true)).thenReturn(sesion);
        
        when(request.getParameter("MyExpGroup0")).thenReturn("prueba0");
        when(request.getAttribute("MyExpGroup1")).thenReturn("prueba1");
        when(request.getParameter("MyExpWorkflow0")).thenReturn("prueba0");
        when(request.getAttribute("MyExpWorkflow1")).thenReturn("prueba1");
                        
        when(config.getServletContext()).thenReturn(context);        
        when(context.getRequestDispatcher("/")).thenReturn(dispatcher);                
        
        GroupSelector group = new GroupSelector();
        try
        {
            group.init(config);
            group.doPost(request, response);

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
