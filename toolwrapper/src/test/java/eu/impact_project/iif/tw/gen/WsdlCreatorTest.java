/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.impact_project.iif.tw.gen;

import eu.impact_project.iif.tw.Constants;
import eu.impact_project.iif.tw.conf.Configuration;
import eu.impact_project.iif.tw.tmpl.ServiceCode;
import eu.impact_project.iif.tw.tmpl.ServiceXml;
import eu.impact_project.iif.tw.toolspec.Dataexchange;
import eu.impact_project.iif.tw.toolspec.Default;
import eu.impact_project.iif.tw.toolspec.Deployment;
import eu.impact_project.iif.tw.toolspec.Deployments;
import eu.impact_project.iif.tw.toolspec.Deployref;
import eu.impact_project.iif.tw.toolspec.Deployto;
import eu.impact_project.iif.tw.toolspec.InOut;
import eu.impact_project.iif.tw.toolspec.Input;
import eu.impact_project.iif.tw.toolspec.Inputs;
import eu.impact_project.iif.tw.toolspec.Installation;
import eu.impact_project.iif.tw.toolspec.Manager;
import eu.impact_project.iif.tw.toolspec.ObjectFactory;
import eu.impact_project.iif.tw.toolspec.Operation;
import eu.impact_project.iif.tw.toolspec.Operations;
import eu.impact_project.iif.tw.toolspec.Os;
import eu.impact_project.iif.tw.toolspec.Output;
import eu.impact_project.iif.tw.toolspec.Outputs;
import eu.impact_project.iif.tw.toolspec.Port;
import eu.impact_project.iif.tw.toolspec.Ports;
import eu.impact_project.iif.tw.toolspec.Restriction;
import eu.impact_project.iif.tw.toolspec.Service;
import eu.impact_project.iif.tw.toolspec.Services;
import eu.impact_project.iif.tw.toolspec.Toolspec;
import eu.impact_project.iif.tw.util.FileUtil;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

/**
 *
 * @author Impact
 */
public class WsdlCreatorTest
{        

    private PropertiesSubstitutor st;
    private Configuration ioc;

    /**
     * Reads the config and sets up substitutor
     * @throws GeneratorException
     */
    @Before
    public void setUp() throws GeneratorException 
    {
        
    }
    
    @Test
    public void testMultipleAtribute() throws GeneratorException, IOException
    {
        ioc = new Configuration();
        URL url = this.getClass().getResource("/finereader.xml");        
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

                
	    }
	} catch (JAXBException ex) {	    
	    throw new GeneratorException(
		    "Unable to create XML binding for toolspec");
	}
    }
    
    /**
     * Test of insertDataTypes method, of class WsdlCreator.
     */
    @Test
    public void testInsertDataTypesException()
    {        
        WsdlCreator instance = new WsdlCreator(null, null, null, null);
        try
        {
            instance.insertDataTypes();
            
            fail("Should raise exception");
        } catch (GeneratorException ex)
        {
            //test OK
        }
        
    }
    
    @Test
    public void testInOutClass()
    {
        InOut instance = new InOut();
        instance.setCliMapping("prueba");
        instance.setOutFileName("prueba");
    }
    
    @Test
    public void testServices()
    {
        Services instance = new Services();
        instance.getService();
    }
    
    @Test
    public void testPorts()
    {
        Ports instance = new Ports();
        instance.getPort();
    }
    
    @Test
    public void testPort()
    {
        Port instance = new Port();
        instance.setType("Prueba");
        instance.setValue(100);                
    }
    
    @Test
    public void testOutputs()
    {
        Outputs instance = new Outputs();
        instance.getOutput();
    }
    
    @Test
    public void testOperations()
    {
        Operations instance = new Operations();
        instance.getOperation();
    }
    
    @Test
    public void testInputs()
    {
        Inputs instance = new Inputs();
        instance.getInput();
    }
    
    @Test
    public void testInput()
    {
        Input instance = new Input();
        instance.setDefault(null);
        instance.setRestriction(null);
    }
    
    
    
    @Test
    public void testDeployto()
    {
        Deployto instance = new Deployto();
        instance.getDeployref();
    }
    
    @Test
    public void testDeployRef()
    {
        Deployref instance = new Deployref();
        instance.setDefault(true);
        instance.setRef(null);                
    }        
    
    @Test
    public void testRestriction()
    {
        Restriction instance = new Restriction();
        instance.getValue();
        instance.setMultiple(Boolean.TRUE);
    }
    
    @Test
    public void testDefault()
    {
        Default instance = new Default();
        instance.setValue(null);
        instance.setClireplacement("prueba");                
    }
    
    @Test
    public void testDataexchange()
    {
        Dataexchange instance = new Dataexchange();
        instance.setAccessdir("prueba");
        instance.setAccessurl("prueba");
    }
    
    @Test
    public void testManager()
    {
        Manager instance = new Manager();
        instance.setPassword("prueba");
        instance.setPath("prueba");
        instance.setUser("prueba");
    }
    
    @Test
    public void testOutput()
    {
        Output instance = new Output();
        instance.setPrefixFromInput(null);
        instance.setExtension(null);
        instance.setAutoExtension(Boolean.TRUE);
        instance.setOutfileId(null);
    }
    
    @Test
    public void testOperation()
    {
        Operation instance = new Operation();
        instance.setCommand(null);
        instance.setDescription(null);
        instance.setInputs(null);
        instance.setName(null);
        instance.setOid(100);
        instance.setOutputs(null);        
    }
    
    @Test
    public void testService()
    {
        Service instance = new Service();
        instance.setContextpathprefix(null);
        instance.setDeployto(null);
        instance.setDescription(null);
        instance.setName(null);
        instance.setOperations(null);
        instance.setServicepackage(null);
        instance.setSid(null);
        instance.setType(null);                
    }
    
    @Test
    public void testToolspec()
    {
        Toolspec instance = new Toolspec();
        instance.setDeployments(null);
        instance.setHomepage(null);
        instance.setId(null);
        instance.setInstallation(null);
        instance.setModel(100.0);
        instance.setName(null);
        instance.setServices(null);
        instance.setVersion(null);
        
        instance.getDeployments();
        instance.getHomepage();
        instance.getId();
        instance.getInstallation();
        instance.getModel();
        instance.getName();
        instance.getServices();
        instance.getVersion();                
    }
           
    @Test
    public void testDeployment()
    {
        Deployment instance = new Deployment();
        instance.setDataexchange(null);;
        instance.setHost(null);;
        instance.setId(null);
        instance.setIdentifier(null);
        instance.setManager(null);
        instance.setPorts(null);
        instance.setRef(null);
        instance.setToolsbasedir(null);
        
        instance.getRef();
    }
    
    @Test
    public void testInstalations()
    {
        Installation instance = new Installation();
        instance.getOs();                
    }
    
    @Test
    public void testDeployments()
    {
        Deployments instance = new Deployments();
        instance.getDeployment();
    }
    
    @Test
    public void testOs()
    {
        Os instance = new Os();
        instance.setType(null);
        instance.setValue(null);
        
        instance.getType();
        instance.getValue();                
    }
    
    //test ObjectFactory
    @Test
    public void testObjectFactory()
    {
        ObjectFactory instance = new ObjectFactory();
        
        instance.createAccessdir(null);
        instance.createAccessurl(null);
        instance.createCliMapping(null);
        instance.createCommand(null);
        instance.createDataexchange();
        instance.createDatatype(null);
        instance.createDefault();
        instance.createDeployment();
        instance.createDeployments();
        instance.createDeployref();
        instance.createDeployto();
        instance.createDescription(null);
        instance.createDocumentation(null);
        instance.createHomepage(null);
        instance.createHost(null);
        instance.createId(null);
        instance.createIdentifier(null);
        instance.createInOut();
        instance.createInput();
        instance.createInput(null);
        instance.createInputs();
        instance.createInstallation();
        instance.createManager();
        instance.createName(null);
        instance.createOperation();
        instance.createOperations();
        instance.createOs();
        instance.createOutFileName(null);
        instance.createOutputs();
        instance.createPassword(null);
        instance.createPath(null);
        instance.createPort();
        instance.createPorts();
        instance.createRequired(null);
        instance.createRestriction();
        instance.createService();
        instance.createServices();
        instance.createToolspec();
        instance.createUser(null);
        instance.createVersion(null);                
    }
            
            
}
