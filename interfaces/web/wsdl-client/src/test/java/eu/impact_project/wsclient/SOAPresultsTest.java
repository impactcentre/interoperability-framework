/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.impact_project.wsclient;

import eu.impact_project.iif.ws.generic.SoapService;
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
public class SOAPresultsTest
{       
    
    @BeforeClass
    public static void setUp() throws Exception {
            ServerStarter.startWebServer(9001);
    }

    @AfterClass
    public static void tearDown() throws Exception {
            ServerStarter.stopAll();
    }

    /**
     * Test of doGet method, of class SOAPresults.
     */
    @Test
    public void testDoGet() throws Exception
    {        
        HttpServletRequest request = null;
        HttpServletResponse response = null;
        SOAPresults instance = new SOAPresults();
        instance.doGet(request, response);        
    }

    /**
     * Test of doPost method, of class SOAPresults.
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

        when(request.getParameter("user")).thenReturn("usuario");
        when(request.getParameter("pass")).thenReturn("password");
        when(request.getSession(true)).thenReturn(sesion);        
        when(config.getServletContext()).thenReturn(context);
        when(sesion.getServletContext()).thenReturn(context);
        URL url = this.getClass().getResource("/config.properties");        
        File testFile = new File(url.getFile());
        when(context.getRealPath("/")).thenReturn(testFile.getParent());                 
        
        SoapService service = new SoapService("http://localhost:9001/Tesseract302.xml");
        when(sesion.getAttribute("serviceObject")).thenReturn(service);
        when(request.getParameter("operationName")).thenReturn("tesseract");
        when(request.getParameterValues("input")).thenReturn(new String[]{"input"});
        when(request.getParameterValues("langmod")).thenReturn(new String[]{"spa"});
        
        when(context.getRequestDispatcher("/interface.jsp")).thenReturn(dispatcher);                       
        
        when(response.getOutputStream()).thenReturn(stream);
        
        SOAPresults results = new SOAPresults();
        try
        {
            results.init(config);
            results.doPost(request, response);

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
