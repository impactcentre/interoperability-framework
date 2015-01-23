package eu.impact_project.iif.t2.client;
import eu.impact_project.iif.t2.client.WorkflowParser.*;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.RequestDispatcher;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import uk.org.taverna.server.client.*;

public class WorkflowDownloader extends HttpServlet {

    public WorkflowDownloader() {
        super();
    }

    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {

        List<Workflow> workflows = new ArrayList<Workflow>();

        String myExpData;
        String[] wfId = request.getParameterValues("id");

        InputStream in = new URL( "http://www.myexperiment.org/workflow.xml?id=" + wfId[0]).openStream();

        try {
            myExpData = IOUtils.toString(in);
        } finally {
            IOUtils.closeQuietly(in);
        }

        PrintWriter out = response.getWriter();
        String dl = "";

        try {
            DocumentBuilder b = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            ByteArrayInputStream input = new ByteArrayInputStream(myExpData.getBytes());
            Document doc = b.parse(input);
            XPath xPath =  XPathFactory.newInstance().newXPath();

            // search the myExpiriment XML for download link
            dl = xPath.compile("//workflow/content-uri").evaluate(doc);

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }  

        in = new URL(dl).openStream();        
        myExpData = IOUtils.toString(in);

        HttpSession session = request.getSession(true);
        Workflow currentWorkflow = WorkflowParser.parseWorkflow(myExpData);
        workflows.add(currentWorkflow);
        session.setAttribute("workflows", workflows);
        request.setAttribute("round1", "round1");

        RequestDispatcher rd = getServletContext().getRequestDispatcher("/");
        rd.forward(request, response);
    }
}
