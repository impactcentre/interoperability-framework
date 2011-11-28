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
package eu.impact_project.wsclient.generic.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class MyUtils {

	public static String normalize(String str) {
		return str.replaceAll("\\s+", " ").trim();
	}
	
	public static Document toDocument(String xml) throws IOException {
		InputStream soapStream = new ByteArrayInputStream(xml.getBytes());
		SAXBuilder parser = new SAXBuilder();
		Document result = null;
		try {
			result = parser.build(soapStream);
		} catch (JDOMException e) {
			throw new IOException("Could not parse XML.", e);
		}
		return result;
	}
}
