/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.impact_project.wsclient;

import java.net.URL;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Impact
 */
public class ServiceProviderTest
{        

    /**
     * Test of getServiceList method, of class ServiceProvider.
     */
    @Test
    public void testGetServiceList()
    {
        System.out.println("getServiceList");
        ServiceProvider instance = new ServiceProviderImpl();
        List<ServiceProvider.Service> expResult = null;
        List<ServiceProvider.Service> result = instance.getServiceList();
        assertEquals(expResult, result);        
    }

    /**
     * Test of getUrl method, of class ServiceProvider.
     */
    @Test
    public void testGetUrl()
    {
        System.out.println("getUrl");
        String id = "";
        ServiceProvider instance = new ServiceProviderImpl();
        URL expResult = null;
        URL result = instance.getUrl(id);
        assertEquals(expResult, result);        
    }

    public class ServiceProviderImpl implements ServiceProvider
    {

        public List<Service> getServiceList()
        {
            return null;
        }

        public URL getUrl(String id)
        {
            return null;
        }
    }
    
}
