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

package eu.impact_project.wsclient;


import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

public class ServerStarter {
	
	private static Server webServer = null; // 9001		
	
	public static void startWebServer(int port) throws Exception 
        {
                /*
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
                */
                webServer = new Server(port);
 
                WebAppContext context = new WebAppContext();                
                context.setResourceBase("src/test/resources");
                context.setContextPath("/");
                context.setParentLoaderPriority(true);
 
                webServer.setHandler(context);
                webServer.start(); 

	}
	
	
	
	public static void stopAll() throws Exception {
		if (webServer != null && webServer.isStarted())
			webServer.stop();		
	}
}
