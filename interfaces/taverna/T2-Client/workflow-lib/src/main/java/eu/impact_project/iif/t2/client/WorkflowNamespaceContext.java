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

import java.util.Iterator;
import javax.xml.*;
import javax.xml.namespace.NamespaceContext;

/**
 * Resolves namespace prefixes for xpath
 * 
 * @author dennis
 * 
 */

public class WorkflowNamespaceContext implements NamespaceContext {

	public String getNamespaceURI(String prefix) {
		if (prefix == null)
			throw new NullPointerException("Null prefix");
		else if ("t".equals(prefix))
			return "http://taverna.sf.net/2008/xml/t2flow";
		else if ("xml".equals(prefix))
			return XMLConstants.XML_NS_URI;
		return XMLConstants.NULL_NS_URI;
	}

	// This method isn't necessary for XPath processing.
	public String getPrefix(String uri) {
		throw new UnsupportedOperationException();
	}

	// This method isn't necessary for XPath processing either.
	public Iterator getPrefixes(String uri) {
		throw new UnsupportedOperationException();
	}

}
