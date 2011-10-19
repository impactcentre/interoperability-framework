<!--
/*
	
	Copyright 2011 The IMPACT Project
	
	@author Dennis Neumann

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
-->

<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@page import="eu.impact_project.wsclient.ServiceProvider"%>
<%@page import="eu.impact_project.wsclient.WSDLinfo"%>
<%@page import="eu.impact_project.wsclient.ServiceProvider.Service"%>
<%@page import="eu.impact_project.wsclient.*"%>

<%@page import="java.util.*"%>
<%@page import="java.io.InputStream"%>
<%@page import="java.io.File"%>
<%@page import="java.net.URL"%>
<%@page import="java.util.Properties"%>

<%@page import="org.slf4j.Logger"%>
<%@page import="org.slf4j.LoggerFactory"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" type="text/css" href="http://www.impact-project.eu/fileadmin/css/iframe.css" media="screen" />
<title>IMPACT Web Service Client</title>
</head>
<body onload="document.forms['defaultForm'].submit()">

<% 


String folder = application.getRealPath("/");

Properties props = new Properties();
InputStream stream = new URL("file:" + folder + "config.properties").openStream();

props.load(stream);
stream.close();

String loadDefault = props.getProperty("loadDefaultWebService");

String defaultWsdl = props.getProperty("defaultWsdl");




if (loadDefault.equals("true")) {
%>

<form name="defaultForm" action="WSDLinfo" method="post">
<input type="hidden" name="wsName" value="user_defined">

<input type="hidden" name="wsdlURL" value="<%= defaultWsdl %>">

</form>


<%
} else {
%>
<!-- &gt;&gt; <a href="/WS-Client/">load services list from local XML file</a> &lt;&lt;  
<br></br>
<br></br>
-->

<a href="http://www.impact-project.eu/taa/dp/" target="_top">Demonstrator Platform</a>
<hr/>
<br/>
<h1>Web Service Client</h1>

<%
	Set<String> bannedServices = new HashSet<String>();
	bannedServices.add("wso2carbon-sts ");
	bannedServices.add(" X K M S Admin Service ");
	bannedServices.add(" X K M S ");
	bannedServices.add(" External Tryit Service ");

	
	Logger logger = LoggerFactory.getLogger(this.getClass());

	ServletConfig c = getServletConfig();
	ServletContext sc = c.getServletContext();
	String baseDir = sc.getRealPath(".") + File.separator;
	logger.info("Directory context is at: " + sc.getRealPath(".")
			+ File.separator);

	/*
	 Set<String> rs = sc.getResourcePaths("/WEB-INF/classes/services.xml");

	 for (String r: rs) {
	 logger.info(r);
	 }
	 */

	ServiceProvider sp;
	/*
	 if (configUrl != null && new File(configUrl.toURI()).exists()) {
	 sp = new FileServiceProvider(configUrl);
	 WSDLinfo.configLocation = new File(configUrl.toURI()).getAbsolutePath();
	 } else if (new File(baseDir + "WEB-INF/classes/services.xml").exists()) {
	 sp = new FileServiceProvider(baseDir + "WEB-INF/classes/services.xml");
	 WSDLinfo.configLocation = baseDir + "WEB-INF/classes/services.xml";
	 } else {
	 sp = new FileServiceProvider(WSDLinfo.configLocation);
	 }
	 */

	 String servicesXml = props.getProperty("servicesXml");
	logger.info("Creating service list from XML");
	URL serviceListUrl = new URL(servicesXml);

	try{
	sp = new XmlServiceProvider(serviceListUrl);

	List<Service> services = sp.getServiceList();

	for (Service s : services) {
		if(!bannedServices.contains(s.getTitle())) {
		logger.debug("Got service " + s.getIdentifier());
		out.print("<form name=\"myForm" + s.getIdentifier()
				+ "\" action=\"WSDLinfo\" method=\"post\">");
		out.print("<input type=\"hidden\" name=\"wsName\" value=\""
				+ s.getTitle() + "\">");
		out.print("<input type=\"hidden\" name=\"wsId\" value=\""
				+ s.getURL() + "\">");
		out.print("<br><a href=\"javascript:document.myForm"
				+ s.getIdentifier() + ".submit()\">" + s.getTitle()
				+ "</a><br>");
		out.print(s.getDescription() + "<br><br></form>");
		}

	}
	
	}catch(RuntimeException e) {
		out.print("IMPACT services list could not be loaded");
	}


	if (false) {
//	if (services == null || services.isEmpty()) {
		logger.debug("Falling back to XML config file");
		URL configUrl = this
				.getClass()
				.getResource(
						"./workspaceEE/WS-Client/src/main/resources/services.xml");
		String cwd = new File(".").getAbsolutePath();
		logger.info("Directory \".\" is at: " + cwd);
		FileServiceProvider.setServletContext(sc);

		sp = new FileServiceProvider(FileServiceProvider.findConfig());

		//sp = new FileServiceProvider(baseDir + "WEB-INF/classes/services.xml");
		//ServiceProvider sp = new FileServiceProvider(WSDLinfo.configLocation);

		List<Service> fileServices = sp.getServiceList();
		logger.info("Creating service list");

		for (Service s : fileServices) {
			logger.debug("Got service " + s.getIdentifier());
			out.print("<form name=\"myForm" + s.getIdentifier()
					+ "\" action=\"WSDLinfo\" method=\"post\">");
			//out.print("<input type=\"hidden\" name=\"wsdlURL\" value=\"" + s.getURL().toString() + "\">");
			out.print("<input type=\"hidden\" name=\"wsName\" value=\""
					+ s.getTitle() + "\">");
			out.print("<input type=\"hidden\" name=\"wsId\" value=\""
					+ s.getIdentifier() + "\">");
			out.print("<br><a href=\"javascript:document.myForm"
					+ s.getIdentifier() + ".submit()\">" + s.getTitle()
					+ "</a><br>");
			out.print(s.getDescription() + "<br><br></form>");
			//out.print("<input type=\"submit\" value=\"-> Try out\"></form>");

		}

	}
%>

<br>

<br><br>

<form action="WSDLinfo" method="post">
<input type="hidden" name="wsName" value="user_defined">

<h4>Other</h4>
<input type="text" size="70" name="wsdlURL" value="<%if (session.getAttribute("wsdlURL") != null)
				out.print(session.getAttribute("wsdlURL"));%>">
<br>
<input type="submit" value="-> Try out">

</form>

<%
} // the big "else"
%>

</body>
</html>