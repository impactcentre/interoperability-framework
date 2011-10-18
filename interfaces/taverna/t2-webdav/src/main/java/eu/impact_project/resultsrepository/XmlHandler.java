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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

/**
 * Contains functionality for parsing XML results.
 */
public class XmlHandler {
	
	/**
	 * Checks if the XML is an OCR evaluation.
	 *
	 * @param xml the xml
	 * @return true, if is evaluation
	 */
	public static boolean isEvaluation(String xml) {
		if (xml != null && xml.indexOf("<Character_Evaluation>") >= 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Gets the text content of a given element. Only the first one is found.
	 *
	 * @param xml the xml
	 * @param element the element name
	 * @return the text content
	 * @throws NoSuchFieldException if the XML does not contain the element.
	 */
	public static String getElementContent(String xml, String element)
			throws NoSuchFieldException {

		String result = "";
		// regex, because the evaluation xml is not well-formed
		Pattern p = Pattern.compile("<" + element + ">(.*)</" + element + ">",
				Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(xml);
		if (m.find())
			result = m.group(1);
		else
			throw new NoSuchFieldException("Element '" + element
					+ "' not found.");

		return result;
	}

	/**
	 * Gets the layout evaluation data from a file.
	 *
	 * @param url URL to the evaluation XML file.
	 * @return Array with the found data.
	 * @throws MalformedURLException if the URL is corrupt.
	 * @throws JDOMException if the file could not be parsed correctly.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static String[] getLayoutEvaluations(String url)
			throws MalformedURLException, JDOMException, IOException {

		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(new URL(url));
		String namespace = "http://schema.primaresearch.org/PAGE/eval/layout/2010-08-06";

		String[] evals = new String[2];
		try {
			String areaRate;
			areaRate = getByXpath(doc, namespace,
					"//*[local-name()='Metrics' and not(@type)]/@overallWeightedAreaSuccessRate");
			String countRate = getByXpath(doc, namespace,
					"//*[local-name()='Metrics' and not(@type)]/@overallWeightedCountSuccessRate");
			evals[0] = areaRate;
			evals[1] = countRate;
		} catch (NoSuchFieldException e) {
			throw new JDOMException("The layout evaluation values could not be found in file: " + url);
		}
		return evals;
	}

	/**
	 * Applies an XPath expression on a JDOM document.
	 *
	 * @param doc The document to be parsed.
	 * @param namespace the namespace
	 * @param xpathString The xpath string, must select an attribute.
	 * @return The found attribute value.
	 * @throws JDOMException the jDOM exception
	 * @throws NoSuchFieldException if the attribute was not found.
	 */
	private static String getByXpath(Document doc, String namespace,
			String xpathString) throws JDOMException, NoSuchFieldException {

		Namespace ns = Namespace.getNamespace("n", namespace);

		XPath xpath = XPath.newInstance(xpathString);
		xpath.addNamespace(ns);
		Attribute result = null;
		result = (Attribute) xpath.selectSingleNode(doc);

		if (result != null)
			return result.getValue();
		else
			throw new NoSuchFieldException();

	}

}
