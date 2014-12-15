/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.impact_project.wsclient;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
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
public class FileLocationContextListenerTest
{        

    /**
     * Test of contextInitialized method, of class FileLocationContextListener.
     */
    @Test
    public void testContextInitialized()
    {
        ServletContextEvent contextEvent = mock(ServletContextEvent.class);               
        ServletContext context = mock(ServletContext.class);
        
        when(contextEvent.getServletContext()).thenReturn(context);
        when(context.getAttribute("tempfile.dir")).thenReturn("tmpfiles");
                
        FileLocationContextListener instance = new FileLocationContextListener();
         
        
        instance.contextInitialized(contextEvent);
    }

    /**
     * Test of contextDestroyed method, of class FileLocationContextListener.
     */
    @Test
    public void testContextDestroyed()
    {
        System.out.println("contextDestroyed");
        ServletContextEvent servletContextEvent = null;
        FileLocationContextListener instance = new FileLocationContextListener();
        instance.contextDestroyed(servletContextEvent);        
    }
    
}
