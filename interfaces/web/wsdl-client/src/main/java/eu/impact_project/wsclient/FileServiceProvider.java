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

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;

/**
 * Finds and reads the configuration file containing the URLs of known web
 * services (project tools).
 */
public class FileServiceProvider implements ServiceProvider {
	final static Logger logger = LoggerFactory
			.getLogger(FileServiceProvider.class);
	public final static String CONFIGURATION_FILE = "services.xml";
	private Configuration config;
	private List<Service> services = new ArrayList<Service>();
	private Map<Integer, URL> urlMap = new HashMap<Integer, URL>();
	private static ServletContext sc = null;
	private URL configUrl;

	/**
	 * 
	 * @param url
	 *            URL to the configuration file
	 * @throws ConfigurationException
	 */
	public FileServiceProvider(URL url) throws ConfigurationException {
		configUrl = url;
		if (url == null) {
			logger.error("URL is null");
			return;
		}
		loadConfig();
	}

	/**
	 * 
	 * @param filename
	 *            Name of the configuration file
	 * @throws ConfigurationException
	 * @throws MalformedURLException
	 */
	public FileServiceProvider(String filename) throws ConfigurationException,
			MalformedURLException {
		logger.info("Loading file " + filename);
		File file = new File(filename);
		if (!file.exists()) {
			logger.error("Configuration " + filename + " doesn't exist!");
			return;
		}

		this.configUrl = file.toURI().toURL();
		loadConfig();

	}

	public FileServiceProvider() throws ConfigurationException,
			MalformedURLException {
		this(CONFIGURATION_FILE);
	}

	/**
	 * Parses the configuration file and stores the information about the web
	 * services in FileService objects which are put in a map and a list.
	 * 
	 * @throws ConfigurationException
	 */
	private void loadConfig() throws ConfigurationException {
		config = new XMLConfiguration(configUrl);
		Integer serviceCount = config.getList("service").size();
		for (int i = 0; i < serviceCount; i++) {
			String title = config.getString("service(" + i + ")");
			String description = config.getString("service(" + i
					+ ")[@description]");
			String url = config.getString("service(" + i + ")[@url]");
			int id = config.getInt("service(" + i + ")[@id]");
			// String desc = config.getString("service(" + i +
			// ")[@description]"); oopps: description wird schon verwendet.
			// Lieber titel<=>description! MD
			try {
				Service s = new FileService(id, title, description,
						new URL(url));
				urlMap.put(id, new URL(url));
				logger.info("Got service description: ID: " + id
						+ " , description: " + title + ", URL: " + url);
				services.add(s);
			} catch (MalformedURLException e) {
				logger.error("URL " + url + " is malformed", e);
			}
		}
		logger.info("Loaded " + services.size() + " service descriptions");
	}

	@Override
	public List<Service> getServiceList() {
		return services;
	}

	public class FileService implements Service {
		int id;
		String title;
		String description;
		URL url;

		public FileService(int id, String title, String description, URL url) {
			this.id = id;
			this.title = title;
			this.description = description;
			this.url = url;
		}

		@Override
		public String getDescription() {
			return description;
		}

		@Override
		public int getIdentifier() {
			return id;
		}

		@Override
		public URL getURL() {
			return url;
		}

		@Override
		public String getTitle() {
			return title;
		}

	}

	@Override
	public URL getUrl(String id) {
		return urlMap.get(id);
	}

	/**
	 * Tries to find the configuration file.
	 * 
	 * @return URL of the found configuration file
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	public static URL findConfig() throws MalformedURLException,
			URISyntaxException {
		URL configUrl = null;
		String baseDir = null;
		if (sc != null) {
			if (sc.getInitParameter("serviceList") != null) {
				logger.debug("Using servlet parameter "
						+ sc.getInitParameter("serviceList"));
				configUrl = new File(sc.getInitParameter("serviceList"))
						.toURI().toURL();
			}
			baseDir = sc.getRealPath(".") + File.separator;
			logger.debug("Base directory is " + baseDir);
		}

		if (configUrl != null && new File(configUrl.toURI()).exists()) {
			logger.debug(configUrl + " exists");
			return configUrl;
		} else if (baseDir != null
				&& new File(baseDir + "WEB-INF/classes/services.xml").exists()) {

			configUrl = new File(baseDir + "WEB-INF/classes/services.xml")
					.toURI().toURL();
		} else if (new File(baseDir + "./WEB-INF/classes/services.xml")
				.exists()) {
			configUrl = new File("./WEB-INF/classes/services.xml").toURI().toURL();
		} else if (new File(WSDLinfo.configLocation).exists()) {
			configUrl = new File(WSDLinfo.configLocation).toURI().toURL();
		}

		return configUrl;
	}

	public static void setServletContext(ServletContext sc) {
		FileServiceProvider.sc = sc;
	}

}
