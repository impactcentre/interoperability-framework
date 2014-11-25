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

package eu.impact_project.resultsrepository;

import it.could.webdav.DAVServlet;

import java.io.File;

import javax.xml.ws.Endpoint;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class ServerStarter {
	
	private static Server webServer = null; // 9001
	private static Server davServer = null; // 9002
	private static Endpoint wsEndpoint = null; // 9003
	
	public static void startWebServer(int port) throws Exception {
		webServer = new Server();
		SelectChannelConnector connector = new SelectChannelConnector();
		connector.setPort(port);
		webServer.addConnector(connector);

		ResourceHandler resource_handler = new ResourceHandler();
		resource_handler.setDirectoriesListed(true);
		//resource_handler.setWelcomeFiles(new String[] { "index.html" });

		String serverRoot = ServerStarter.class.getResource("/").getFile();

		resource_handler.setResourceBase(serverRoot);

		HandlerList handlers = new HandlerList();
		handlers.setHandlers(new Handler[] { resource_handler,
				new DefaultHandler() });
		webServer.setHandler(handlers);

		webServer.start();

	}
	
	public static void startDavServer(int port) throws Exception {
		File file = new File(System.getProperty("user.dir")
				+ "/target");
	    File davFolder = new File(file.toString() + "/dav");
		davFolder.mkdir();
	    davServer = new Server(port);
		ServletHolder davServletHolder = new ServletHolder(new DAVServlet());
		davServletHolder.setInitParameter("rootPath", davFolder.toString()
				.replace("file:/", ""));
		ServletContextHandler rootContext = new ServletContextHandler(davServer, "/", ServletContextHandler.SESSIONS);
		rootContext.addServlet(davServletHolder, "/*");
		davServer.start();

	}
	
	public static void startSoapServer(int port) {
		ServiceImpl service = new ServiceImpl();
		String address = "http://localhost:" + port + "/IMPACTResultsRepositoryService";

		wsEndpoint = Endpoint.publish(address, service);

	}
	
	public static void stopAll() throws Exception {
		if (webServer != null && webServer.isStarted())
			webServer.stop();
		if (davServer != null && davServer.isStarted())
			davServer.stop();
		if (wsEndpoint != null && wsEndpoint.isPublished())
			wsEndpoint.stop();
	}
}
