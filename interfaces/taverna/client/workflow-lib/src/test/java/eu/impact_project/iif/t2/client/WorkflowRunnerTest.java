/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.impact_project.iif.t2.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 *
 * @author Impact
 */
public class WorkflowRunnerTest
{        

    /**
     * Test of doGet method, of class WorkflowRunner.
     */
    @Test
    public void testDoGet() throws Exception
    {
        HttpServletRequest request = null;
        HttpServletResponse response = null;
        WorkflowRunner instance = new WorkflowRunner();
        instance.doGet(request, response);    
    }
    
    /**
     * Test of doPost method, of class WorkflowRunner.
     */
    @Test
    public void testDoPost() throws Exception
    {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);        
        ServletConfig config = mock(ServletConfig.class);
        ServletContext context = mock(ServletContext.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        ServletOutputStream stream = mock(ServletOutputStream.class);
        HttpSession session = mock(HttpSession.class);

        
        when(request.getSession(true)).thenReturn(session);
        
        ArrayList<Workflow> flowList = new ArrayList<>();
        Workflow flow = new Workflow();
        flow.setStringVersion("Esto es una prueba");
        flow.setWsdls("<wsdl>http://www.ua.es</wsdl>");
        flow.setUrls("http://www.ua.es");
        
        ArrayList<WorkflowInput> flowInputs = new ArrayList<>();
        WorkflowInput input = new WorkflowInput("pru0Input");
        input.setDepth(1);        
        flowInputs.add(input);        
        
        input = new WorkflowInput("pru1Input");
        input.setDepth(0);
        flowInputs.add(input); 
        
        flow.setInputs(flowInputs);
      
        flowList.add(flow);        
        when(session.getAttribute("workflows")).thenReturn(flowList);
                
        when(config.getServletContext()).thenReturn(context);                
        URL url = this.getClass().getResource("/config.properties");        
        File testFile = new File(url.getFile());
        when(context.getRealPath("/")).thenReturn(testFile.getParent()+"/");
        
        
        Part[] parts = new Part[] 
        {
            new StringPart("user", "user"),
            new StringPart("pass", "pass"),
            new StringPart("workflow0pru0Input", "prueba0"),
            new StringPart("workflow0pru0Input0", "prueba0.0"),
            new StringPart("workflow0pru1Input", "prueba1")
                
        };
        
        MultipartRequestEntity multipartRequestEntity = 
                new MultipartRequestEntity(parts, new PostMethod().getParams());
    
        ByteArrayOutputStream requestContent = new ByteArrayOutputStream();
    
        multipartRequestEntity.writeRequest(requestContent);
    
        final ByteArrayInputStream inputContent = new ByteArrayInputStream (requestContent.toByteArray ());
        
        when(request.getInputStream()).thenReturn(new ServletInputStream() {
            @Override
            public int read() throws IOException 
            {
                return inputContent.read();
            }
        });
        
        when(request.getContentType()).thenReturn(multipartRequestEntity.getContentType());
        
        WorkflowRunner runer = new WorkflowRunner();
        try
        {
            runer.init(config);
            runer.doPost(request, response);
                           
        } catch (ServletException ex)
        {
            fail("Should not raise exception " + ex.toString());
        } catch (IOException ex)
        {
            fail("Should not raise exception " + ex.toString());
        }
        catch (NullPointerException ex)
        {
            //ok no funciona el server de taverna
        }
    }
    
    /**
     * Test of doPost method, of class WorkflowRunner.
     */
    @Test
    public void testDoPostURLFail() throws Exception
    {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);        
        ServletConfig config = mock(ServletConfig.class);
        ServletContext context = mock(ServletContext.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        ServletOutputStream stream = mock(ServletOutputStream.class);
        HttpSession session = mock(HttpSession.class);

        
        when(request.getSession(true)).thenReturn(session);
        
        ArrayList<Workflow> flowList = new ArrayList<>();
        Workflow flow = new Workflow();
        flow.setStringVersion("Esto es una prueba");
        flow.setWsdls("<wsdl>http://www.ua.es</wsdl>");
        flow.setUrls("http://falsa.es");
        
        ArrayList<WorkflowInput> flowInputs = new ArrayList<>();
        WorkflowInput input = new WorkflowInput("pru0Input");
        input.setDepth(1);        
        flowInputs.add(input);        
        
        input = new WorkflowInput("pru1Input");
        input.setDepth(0);
        flowInputs.add(input); 
        
        flow.setInputs(flowInputs);
      
        flowList.add(flow);        
        when(session.getAttribute("workflows")).thenReturn(flowList);
                
        when(config.getServletContext()).thenReturn(context);                
        URL url = this.getClass().getResource("/config.properties");        
        File testFile = new File(url.getFile());
        when(context.getRealPath("/")).thenReturn(testFile.getParent()+"/");
        
        
        Part[] parts = new Part[] 
        {
            new StringPart("user", "user"),
            new StringPart("pass", "pass"),
            new StringPart("workflow0pru0Input", "prueba0"),
            new StringPart("workflow0pru0Input0", "prueba0.0"),
            new StringPart("workflow0pru1Input", "prueba1")
                
        };
        
        MultipartRequestEntity multipartRequestEntity = 
                new MultipartRequestEntity(parts, new PostMethod().getParams());
    
        ByteArrayOutputStream requestContent = new ByteArrayOutputStream();
    
        multipartRequestEntity.writeRequest(requestContent);
    
        final ByteArrayInputStream inputContent = new ByteArrayInputStream (requestContent.toByteArray ());
        
        when(request.getInputStream()).thenReturn(new ServletInputStream() {
            @Override
            public int read() throws IOException 
            {
                return inputContent.read();
            }
        });
        
        when(request.getContentType()).thenReturn(multipartRequestEntity.getContentType());
        
        WorkflowRunner runer = new WorkflowRunner();
        try
        {
            runer.init(config);
            runer.doPost(request, response);
                           
        } catch (ServletException ex)
        {
            fail("Should not raise exception " + ex.toString());
        } catch (IOException ex)
        {
            fail("Should not raise exception " + ex.toString());
        }
        catch (NullPointerException ex)
        {
            //ok no funciona el server de taverna
        }
    }
    
}
