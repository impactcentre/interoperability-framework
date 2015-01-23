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
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author Impact
 */
public class HelperTest
{
    
    public HelperTest()
    {
    }

    /**
     * Test of createAuthenticatingClient method, of class Helper.
     */
    @Test
    public void testCreateAuthenticatingClient()
    {        
        String domain = "http://localhost";
        String user = "user";
        String password = "pass";        
        HttpClient result = Helper.createAuthenticatingClient(domain, user, password); 
        assertNotNull(result);
    }

    /**
     * Test of applyXPathSingleNode method, of class Helper.
     */
    @Test
    public void testApplyXPathSingleNode() throws Exception
    {
        /*
        System.out.println("applyXPathSingleNode");
        InputStream xmlStream = null;
        String xpathExpression = "";
        Object expResult = null;
        Object result = Helper.applyXPathSingleNode(xmlStream, xpathExpression);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
        */
    }

    /**
     * Test of applyXPathSeveralNodes method, of class Helper.
     */
    @Test
    public void testApplyXPathSeveralNodes() throws Exception
    {
        /*
        System.out.println("applyXPathSeveralNodes");
        InputStream xmlStream = null;
        String xpathExpression = "";
        List expResult = null;
        List result = Helper.applyXPathSeveralNodes(xmlStream, xpathExpression);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
        */
    }

    /**
     * Test of parseRequest method, of class Helper.
     */
    @Test
    public void testParseRequest() throws Exception
    {
        HttpServletRequest request = mock(HttpServletRequest.class);
        URL url = this.getClass().getResource("/prueba.txt");        
        File testFile = new File(url.getFile());       
        Part[] parts = new Part[] 
        {
            new StringPart("user", "user"),
            new FilePart("file_workflow",testFile),
            new FilePart("comon_file",testFile)
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
        
        Helper.parseRequest(request);
        
    }
    
}
