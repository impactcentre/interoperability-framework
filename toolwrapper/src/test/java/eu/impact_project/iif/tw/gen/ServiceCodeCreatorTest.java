/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.impact_project.iif.tw.gen;

import eu.impact_project.iif.tw.conf.Configuration;
import eu.impact_project.iif.tw.tmpl.OperationCode;
import eu.impact_project.iif.tw.tmpl.ServiceCode;
import eu.impact_project.iif.tw.tmpl.ServiceXml;
import eu.impact_project.iif.tw.toolspec.InOut;
import eu.impact_project.iif.tw.toolspec.Operation;
import eu.impact_project.iif.tw.toolspec.Service;
import eu.impact_project.iif.tw.toolspec.Toolspec;
import eu.impact_project.iif.tw.util.FileUtil;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Impact
 */
public class ServiceCodeCreatorTest
{        
    
    private PropertiesSubstitutor st;
    private Configuration ioc;
    
    @Test
    public void testOperationEvaluationID() throws GeneratorException, IOException
    {
        ioc = new Configuration();
        URL url = this.getClass().getResource("/layoutevaluation.xml");        
        File testXml = new File(url.getFile());
        ioc.setXmlConf(testXml);
        url = this.getClass().getResource("/toolwrapper.properties");
        File testProjConf = new File(url.getFile());
        ioc.setProjConf(testProjConf);
        st = new PropertiesSubstitutor(ioc.getProjConf());
        
        JAXBContext context;
	try {
	    context = JAXBContext
		    .newInstance("eu.impact_project.iif.tw.toolspec");
	    Unmarshaller unmarshaller = context.createUnmarshaller();
	    Toolspec toolspec = (Toolspec) unmarshaller.unmarshal(new File(ioc
		    .getXmlConf()));
	    
	    // List of services for the tool
	    List<Service> services = toolspec.getServices().getService();
	    // For each service a different maven project will be generated
	    for (Service service : services) 
            {
		
                // Properties substitutor is created for each service
                PropertiesSubstitutor st = new PropertiesSubstitutor(ioc.getProjConf());
                // Service name is composed of Service Name and Tool Version
                ServiceDef sdef = new ServiceDef(service.getName(), toolspec.getVersion());
                st.setServiceDef(sdef);
                st.addVariable("tool_version", sdef.getVersion());
                st.addVariable("project_title", sdef.getName());
                st.addVariable("global_package_name", service.getServicepackage());
                String cpp = service.getContextpathprefix();
                st.addVariable("contextpath_prefix", ((cpp == null) ? "" : cpp));
                st.deriveVariables();
                
                File dir = new File(st.getTemplateDir());
                st.processDirectory(dir);
                String generatedDir = st.getGenerateDir();
                String projMidfix = st.getProjectMidfix();
                String projDir = st.getProjectDirectory();

                // target service wsdl
                String wsdlSourcePath = FileUtil.makePath("tmpl") + "Template.wsdl";
                
                // target service wsdl
                String wsdlTargetPath = FileUtil.makePath(generatedDir, projDir, "src",
                        "main", "webapp") + projMidfix + ".wsdl";
                

                List<Operation> operations = service.getOperations().getOperation();
                WsdlCreator wsdlCreator = new WsdlCreator(st, wsdlSourcePath,
                        wsdlTargetPath, operations);
                wsdlCreator.insertDataTypes();               
                
                st.processFile(new File(wsdlTargetPath));

                // service code
                String sjf = FileUtil.makePath(generatedDir, projDir, "src", "main",
                        "java", st.getProjectPackagePath()) + projMidfix + ".java";                
                String serviceTmpl = st.getProp("project.template.service");
                // service operation java code template
                ServiceCode sc = new ServiceCode(serviceTmpl);
                // service xml
                ServiceXml sxml = new ServiceXml("tmpl/servicexml.vm");
                sxml.put(st.getContext());
                ServiceCodeCreator scc = new ServiceCodeCreator(st, sc, sxml,
                        operations);
                scc.createOperations();
                
	    }
	} catch (JAXBException ex) {	    
	    throw new GeneratorException(
		    "Unable to create XML binding for toolspec");
	}
    }

    
    
}
