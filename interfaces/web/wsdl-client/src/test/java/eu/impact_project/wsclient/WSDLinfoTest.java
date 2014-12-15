/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.impact_project.wsclient;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.GenericServlet;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mockito.Mockito;

/**
 *
 * @author Impact
 */
public class WSDLinfoTest extends Mockito
{
        
    /**
     * Test of init method, of class WSDLinfo.
     */
    @Test
    public void testInit() throws Exception
    {
        WSDLinfo info = new WSDLinfo();
        ServletConfig config = new GenericServlet()
        {

            @Override
            public void service(ServletRequest sr, ServletResponse sr1) throws ServletException, IOException
            {
                
            }
        };
        
        info.init(config);
    }

    /**
     * Test of doGet method, of class WSDLinfo.
     */
    @Test
    public void testDoGet()
    {
        HttpServletRequest request = mock(HttpServletRequest.class);       
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession sesion = mock(HttpSession.class);
        ServletConfig config = mock(ServletConfig.class);
        ServletContext context = mock(ServletContext.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        String key = "1234567891234567";
        
        when(config.getServletContext()).thenReturn(context);
        when(context.getRequestDispatcher("/interface.jsp")).thenReturn(dispatcher);
        when(request.getParameter("wsId")).thenReturn(null);
        when(request.getParameter("wsdlURL")).thenReturn("http://impact.dlsi.ua.es/services/Tesseract302?wsdl");
        when(request.getParameter("wsName")).thenReturn("nombre");
        when(request.getParameter("user")).thenReturn(Security.encrypt("usuario",key));
        when(request.getParameter("pass")).thenReturn(Security.encrypt("password",key));
        when(request.getSession(true)).thenReturn(sesion);
        
        
        WSDLinfo info = new WSDLinfo();
        try
        {
            info.init(config);        
            info.doGet(request, response);
            
            //verify(request, atLeast(1)).getParameter("username"); // only if you want to verify username was called...                
        } catch (ServletException ex)
        {
            fail("Should not raise exception "+ex.toString());
        } catch (IOException ex)
        {
            fail("Should not raise exception "+ex.toString());
        }
    }

    /**
     * Test of doPost method, of class WSDLinfo.
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

        String key = "1234567891234567";
        
        when(config.getServletContext()).thenReturn(context);
        when(context.getRequestDispatcher("/interface.jsp")).thenReturn(dispatcher);
        when(request.getParameter("wsId")).thenReturn(null);
        when(request.getParameter("wsdlURL")).thenReturn("http://impact.dlsi.ua.es/services/Tesseract302?wsdl");
        when(request.getParameter("wsName")).thenReturn("nombre");
        when(request.getParameter("user")).thenReturn(Security.encrypt("usuario",key));
        when(request.getParameter("pass")).thenReturn(Security.encrypt("password",key));
        when(request.getSession(true)).thenReturn(sesion);
        
        
        WSDLinfo info = new WSDLinfo();
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
    public void testDoGetId()
    {
        HttpServletRequest request = mock(HttpServletRequest.class);       
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession sesion = mock(HttpSession.class);
        ServletConfig config = mock(ServletConfig.class);
        ServletContext context = mock(ServletContext.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        String key = "1234567891234567";
        
        when(config.getServletContext()).thenReturn(context);
        when(context.getRequestDispatcher("/interface.jsp")).thenReturn(dispatcher);
        when(request.getParameter("wsId")).thenReturn("http://impact.dlsi.ua.es/services/Tesseract302?wsdl");
        when(request.getParameter("wsdlURL")).thenReturn("http://impact.dlsi.ua.es/services/Tesseract302?wsdl");
        when(request.getParameter("wsName")).thenReturn("nombre");
        when(request.getParameter("user")).thenReturn(Security.encrypt("usuario",key));
        when(request.getParameter("pass")).thenReturn(Security.encrypt("password",key));
        when(request.getSession(true)).thenReturn(sesion);
        
        
        WSDLinfo info = new WSDLinfo();
        try
        {
            info.init(config);        
            info.doGet(request, response);
            
            //verify(request, atLeast(1)).getParameter("username"); // only if you want to verify username was called...                
        } catch (ServletException ex)
        {
            fail("Should not raise exception "+ex.toString());
        } catch (IOException ex)
        {
            fail("Should not raise exception "+ex.toString());
        }
    }

    /**
     * Test of doPost method, of class WSDLinfo.
     */
    @Test
    public void testDoPostId() throws Exception
    {
        HttpServletRequest request = mock(HttpServletRequest.class);       
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession sesion = mock(HttpSession.class);
        ServletConfig config = mock(ServletConfig.class);
        ServletContext context = mock(ServletContext.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        String key = "1234567891234567";
        
        when(config.getServletContext()).thenReturn(context);
        when(context.getRequestDispatcher("/interface.jsp")).thenReturn(dispatcher);
        when(request.getParameter("wsId")).thenReturn("http://impact.dlsi.ua.es/services/Tesseract302?wsdl");
        when(request.getParameter("wsdlURL")).thenReturn("http://impact.dlsi.ua.es/services/Tesseract302?wsdl");
        when(request.getParameter("wsName")).thenReturn("nombre");
        when(request.getParameter("user")).thenReturn(Security.encrypt("usuario",key));
        when(request.getParameter("pass")).thenReturn(Security.encrypt("password",key));
        when(request.getSession(true)).thenReturn(sesion);
        
        
        WSDLinfo info = new WSDLinfo();
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
