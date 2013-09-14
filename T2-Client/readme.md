Taverna 2 Server Client
-----------------------

The Taverna 2 Server Client can be used to execute workflows created with Taverna 2 Server. 
After the user uploads the workflow to be executed, the application presents the input fields
for the workflow. Currently, the program does not recognize if an input value must be 
a string or a file. This is why the user must click on the "switch" symbol to choose between
string and file upload. If the input is multi-valued, more input fields can be added by 
clicking on the plus sign.
	
In addition to uploading a workflow from a local disk, the user can login to 
[myExperiment](http://www.myexperiment.org/ "myExperiment"). After that, a workflow belonging to the user
or to one of the user's groups can be uploaded.
		
After entering the input values, the user can execute the workflow. In the background, the 
application uses the REST interface of the Taverna Server to send the workflow and the input 
values to the server, and to read the resulting output values. Finally, the output values are 
presented as strings or as links to files.
	
For a proper execution, T2-Client requires the [Taverna Server](http://www.mygrid.org.uk/dev/taverna-server/download.html "Taverna Server") version 0.2.1 running in an application container (e. g. Apache Tomcat) on localhost:9080/tavernaserver. Currently, an exception is generated if an output of a workflow is a binary file. The workaround for this problem is to encode all output binary files like images into the base64 format with the corresponding local service in Taverna while creating the workflow.
	
**Deployment Guide**

To install the application locally, you have to get the source files, build them and deploy the resulting archives on a Java application server. This guide explains how to build the sources 
using Maven 2, and how to deploy the application in Tomcat 6. It is assumed that those tools are 
already installed on your system.
	
**HTTPS support**

If you want to execute workflows that contain HTTPS-enabled web services, you need to
import the certificates into the keystore of your java installation. An example for Java 6
on Linux:

Get the server certificate as a PEM file. For example, Firefox can export the certificate as "X.509 certificate (PEM)".

Execute as root:  

     keytool -import -file certificate.pem -keystore $JAVA_HOME/jre/lib/security/cacerts -alias myWebServiceCertificate
		
(default password for the keystore is "changeit")

**Authentication**

Currently, it is not possible to execute workflows that contain web services with authentication
using T2-Client.

**Preparing the dependent libraries**

The application needs some classes from the Taverna Server which are currently not available
through a central Maven repository. That's why they must be first installed in the local 
repository. The project containing the POM file is *t2-rest*.
	
In the downloaded directory, execute the Maven command:
	
    mvn install
	
	
**Building the application**

The project is managed using Maven 2. Again, you can use an IDE to build the sources.
Alternatively, you can execute the following command in the project directory:
	
    mvn package
	
**Deploying the application**

Maven generates a WAR archive for each frontend module (*workflow-client*) in the corresponding 'target' directory. In Tomcat, you can deploy those archives using the Tomcat Manager application. Or, simply copy the archives into the *webapp* directory of your Tomcat installation and restart Tomcat.
