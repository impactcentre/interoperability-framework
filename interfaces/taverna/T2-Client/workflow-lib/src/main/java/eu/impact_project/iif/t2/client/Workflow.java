/*
	
	Copyright 2011 The IMPACT Project
	
	@author Dennis
	@version 0.1

	Licensed under the Apache License, Version 2.0 (the "License"); 
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
 
  		http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.

 */

package eu.impact_project.iif.t2.client;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.wsdl.Definition;
import javax.wsdl.Types;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xerces.impl.Constants;
import org.xml.sax.SAXException;
import javax.xml.ws.handler.MessageContext;
import org.apache.cxf.message.Message;
//import javax.xml.ws.handler.*;
//import org.apache.hello_world_soap_http.Greeter;
//import org.apache.hello_world_soap_http.GreeterImpl;



/**
 * Bean representing a Taverna workflow
 * 
 * @author dennis
 * 
 */

public class Workflow {

	private String stringVersion;
	private List<WorkflowInput> inputs = new ArrayList<WorkflowInput>();
	private List<String> urls = new ArrayList<String>();
	private List<Wsdl> wsdls = new ArrayList<Wsdl>();

	public Workflow(String stringVersion) {
		this.stringVersion = stringVersion;
	}

	public Workflow() {
	}

	public String getStringVersion() {
		return stringVersion;
	}

	public void setStringVersion(String stringVersion) {
		this.stringVersion = stringVersion;
	}

	public List<WorkflowInput> getInputs() {
		return inputs;
	}

	public void setInputs(List<WorkflowInput> inputs) {
		this.inputs = inputs;
	}

	public void setUrls(String inputsData) throws ParserConfigurationException, SAXException, IOException 
	{
			String patronUrl = "(http(s)?://)[^ ]*";
        	Pattern pattern = Pattern.compile(patronUrl);
	        java.util.regex.Matcher matcher = pattern.matcher(inputsData);
	        
	        while (matcher.find()) 
	        {  
	            String url = matcher.group();
	            
	            if (!this.urls.contains(url))
	            	this.urls.add(url);
	        }
	}
	
	public void setWsdls(String workflow) throws ParserConfigurationException, SAXException, IOException 
	{
			String patronUrl = "(http(s)?://)[^<]*";
        	Pattern pattern = Pattern.compile("<wsdl>" + patronUrl + "</wsdl>");
	        java.util.regex.Matcher matcher = pattern.matcher(workflow);
	        List<String> auxWsdls = new ArrayList<String>();
	        
	        while (matcher.find()) 
	        {  
	            String wsdlTAG = matcher.group();
	            String urlWsdl = wsdlTAG.substring("<wsdl>".length(), wsdlTAG.length()-"</wsdl>".length());
	            
	            //System.out.println("WSDL: " + urlWsdl);
	            if (!auxWsdls.contains(urlWsdl))
	            {
	            	Wsdl wsdl = new Wsdl(urlWsdl);
	            	
	            	auxWsdls.add(urlWsdl);
	            	this.wsdls.add(wsdl);
	            }
	        }
	}
	
	public boolean testUrl(String url)
	{
		boolean valid = true;
		
		try
		{
		  HttpURLConnection.setFollowRedirects(true);
	      //HttpURLConnection.setInstanceFollowRedirects(false);
	      HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
	      con.setRequestMethod("GET");
	      con.connect(); 
	      //con.setReadTimeout(3000);
	      if (con.getResponseCode() != HttpURLConnection.HTTP_OK)
	      {
	    	  valid = false;
	    	  System.out.println("Url not avaliable(" + con.getResponseCode() + "): " + url);
	      }
	      //else System.out.println("Url OK: " + url);
	    }
	    catch (Exception e) {
	    	valid = false;
	    	System.out.println("Catch Url not avaliable: " + url);
	        e.printStackTrace();
	    }
		
		return valid;
	}
	
	public List<String> testUrls(List<String> list)
	{
		List<String> failedUrls = new ArrayList<String>();
		
		for (String currentUrl : list) 
		{
			if (testUrl(currentUrl))
				failedUrls.add(currentUrl);
		}
		
		return failedUrls;
	}

	public List<String> getUrls() {
		return urls;
	}
	
	public List<Wsdl> getWsdls() {
		return wsdls;
	}
	
	public boolean getSecurity(String wsdl) {
		boolean security = true;

		/*		
		try
		{
			javax.xml.ws.Service s = javax.xml.ws.
		    Greeter greeter = s.getPort(portName, Greeter.class);
		    InvocationHandler handler  = Proxy.getInvocationHandler(greeter);
		    BindingProvider  bp = null;
 
		    
			WSDLFactory factory = WSDLFactory.newInstance();
			WSDLReader reader = factory.newWSDLReader();
			reader.setFeature("javax.wsdl.verbose", true);
			reader.setFeature("javax.wsdl.importDocuments", true);
			//Definition def = reader.readWSDL(null, "sample.wsdl");
			reader.getFeature(Constants.SECURITY_MANAGER_PROPERTY);
			ExtensionRegistry extensionesReader = reader.getExtensionRegistry();
			Definition def = reader.readWSDL(null, wsdl);
			
		    // get a map of the five specific parts a WSDL file
		    //Types types = def.getTypes();
		    //Map messages = def.getMessages();
		    Map portTypes = def.getAllPortTypes();
		    //Map bindings = def.getBindings();
		    Map services = def.getServices();
		    //Map imports = def.getImports();
		   // Map extensions = def.getExtensionAttributes();
		   // List elementosExtensiones = def.getExtensibilityElements();
		    //Types tipos = def.getTypes();

			WSBindingProvider bp = (WSBindingProvider)port;
			List inboundHeaders = bp.getInboundHeaders();
			
			security = true;
		}
		catch (WSDLException e)
		{
			e.printStackTrace();
		}
	*/	  
		return security;
	}

}
