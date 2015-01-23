/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.impact_project.wsclient;

import eu.impact_project.wsclient.XmlServiceProvider.XmlService;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.configuration.ConfigurationException;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 * @author Impact
 */
public class XmlServiceProviderTest
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
     * Test of getServiceList method, of class XmlServiceProvider.
     */
    @Test
    public void testGetServiceList()
    {
        
        try        
        {
            
            // Load the directory as a resource
            URL file_url = new URL("http://localhost:9001/services.xml");            
            // Turn the resource into a File object            
            XmlServiceProvider sp = new XmlServiceProvider(file_url);
            sp.getServiceList();
            
            XmlService service = sp.new XmlService(1, "prueba", "prueba", new URL("http://prueba"));
            service.getDescription();
            service.getIdentifier();
            service.getTitle();
            service.getURL();
            service.compareTo(service);
           
            
        } catch (MalformedURLException ex)
        {
            fail("Should not raise exception "+ex.toString());
        } catch (ConfigurationException ex)
        {
            fail("Should not raise exception "+ex.toString());
        }
        
        
    }   
    
}
