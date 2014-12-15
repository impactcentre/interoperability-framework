/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.impact_project.wsclient;

import eu.impact_project.wsclient.XmlServiceProvider.XmlService;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.configuration.ConfigurationException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Impact
 */
public class XmlServiceProviderTest
{        

    /**
     * Test of getServiceList method, of class XmlServiceProvider.
     */
    @Test
    public void testGetServiceList()
    {
        try        
        {
            // Load the directory as a resource
            URL file_url = new URL("http://localhost:8080/testResources/services.xml");            
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
