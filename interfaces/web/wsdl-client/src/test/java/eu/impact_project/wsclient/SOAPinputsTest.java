/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.impact_project.wsclient;

import eu.impact_project.iif.ws.generic.SoapService;
import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author Impact
 */
public class SOAPinputsTest
{
    
    @BeforeClass
	public static void setUp() throws Exception {
		ServerStarter.startWebServer(9001);
	}

	@AfterClass
	public static void tearDown() throws Exception {
		ServerStarter.stopAll();
	}
    
    
    @Test
    public void testDoGet()
    {
        SOAPinputs info = new SOAPinputs();
        try
        {     
            info.doGet(null, null);            
            //verify(request, atLeast(1)).getParameter("username"); // only if you want to verify username was called...                
        } catch (ServletException ex)
        {
            fail("Should not raise exception "+ex.toString());
        } catch (IOException ex)
        {
            fail("Should not raise exception "+ex.toString());
        }
    }
    
    @Test
    public void testDoPost() throws Exception
    {
        HttpServletRequest request = mock(HttpServletRequest.class);       
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession sesion = mock(HttpSession.class);
        ServletConfig config = mock(ServletConfig.class);
        ServletContext context = mock(ServletContext.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);        
        SoapService service = new SoapService("http://localhost:9001/Tesseract302.xml");
        
        
        when(config.getServletContext()).thenReturn(context);
        when(context.getRequestDispatcher("/interface.jsp")).thenReturn(dispatcher);
        when(sesion.getAttribute("serviceObject")).thenReturn(service);
        when(request.getSession()).thenReturn(sesion);
        when(request.getParameter("displayDefaults")).thenReturn("true");
        
        
        SOAPinputs info = new SOAPinputs();
        try
        {
            info.init(config);        
            info.doPost(request, response);
            
            //verify(request, atLeast(1)).getParameter("username"); // only if you want to verify username was called...                
        } catch (ServletException ex)
        {
            fail("Should not raise exception "+ex.toString());
        } catch (IOException ex)
        {
            fail("Should not raise exception "+ex.toString());
        }
    }
    
    @Test
    public void testDoPostDisplay() throws Exception
    {
        HttpServletRequest request = mock(HttpServletRequest.class);       
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession sesion = mock(HttpSession.class);
        ServletConfig config = mock(ServletConfig.class);
        ServletContext context = mock(ServletContext.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);        
        SoapService service = new SoapService("http://localhost:9001/Tesseract302.xml");
        
        
        when(config.getServletContext()).thenReturn(context);
        when(context.getRequestDispatcher("/interface.jsp")).thenReturn(dispatcher);
        when(sesion.getAttribute("serviceObject")).thenReturn(service);
        when(request.getSession()).thenReturn(sesion);
        when(request.getParameter("displayDefaults")).thenReturn(null);
        when(request.getParameter("currentOperation")).thenReturn("tesseract");
        
        SOAPinputs info = new SOAPinputs();
        try
        {
            info.init(config);        
            info.doPost(request, response);
            
            //verify(request, atLeast(1)).getParameter("username"); // only if you want to verify username was called...                
        } catch (ServletException ex)
        {
            fail("Should not raise exception "+ex.toString());
        } catch (IOException ex)
        {
            fail("Should not raise exception "+ex.toString());
        }
    }
    
}
