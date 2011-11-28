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
package eu.impact_project.wsclient.generic;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.model.iface.Operation;

import eu.impact_project.wsclient.generic.util.MyUtils;


/**
 * High-level representation of a SOAP service.  
 *
 */
public class SoapService {

	private Map<String, SoapOperation> operations;
	WsdlDocument wsdlDoc;

	public SoapService(String url) throws IOException {
		this(new URL(url));
	}

	/**
	 * Constructs a service using a WSDL
	 * 
	 * @param url to a WSDL
	 * @throws IOException
	 */
	public SoapService(URL url) throws IOException {
		wsdlDoc = new WsdlDocument(url);
		operations = generateOperations();
	}

	/**
	 * Returns the documentation of the service
	 * 
	 * @return
	 */
	public String getDocumentation() {
		String docu = wsdlDoc.getServiceDocumentation();
		return MyUtils.normalize(docu);
	}

	/**
	 * Returns all operations defined in this service
	 * 
	 * @return
	 */
	public List<SoapOperation> getOperations() {
		return new ArrayList<SoapOperation>(operations.values());
	}

	/**
	 * Returns a specific operation
	 * 
	 * @param name of the operation
	 * @return
	 */
	public SoapOperation getOperation(String name) {
		return operations.get(name);
	}

	private Map<String, SoapOperation> generateOperations() throws IOException {

		HashMap<String, SoapOperation> soapOperations = new HashMap<String, SoapOperation>();

		WsdlInterface wsdlInterface = wsdlDoc.getWsdlInterface();
		List<Operation> operations = wsdlInterface.getOperationList();
		for (Operation operation : operations) {
			WsdlOperation op = (WsdlOperation) operation;

			String opName = op.getName();
			SoapOperation soapOp = new SoapOperation(opName, wsdlDoc);

			soapOperations.put(opName, soapOp);
		}

		return soapOperations;
	}
}
