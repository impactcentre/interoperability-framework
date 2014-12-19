/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.impact_project.resultsrepository.report;

import eu.impact_project.resultsrepository.ServerStarter;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 * @author Impact
 */
public class ReportTest
{        
    
    @BeforeClass
    public static void setUpClass() throws Exception {
            ServerStarter.startWebServer(9001);
            ServerStarter.startDavServer(9002);
    }

    @AfterClass
    public static void tearDown() throws Exception {
            ServerStarter.stopAll();
    }

    /**
     * Test of asExcel method, of class Report.
     */
    @Test
    public void testAsExcel() throws Exception
    {
        Report rep = new Report("pru", 10303, "pru", 1);
        
        List<AnyTool> tools = new ArrayList<>();
        List<URL> urls = new ArrayList<>();
        URL url = new URL("http://www.prima.cse.salford");
        urls.add(url);
        AnyTool tool = new AnyTool("prutool", urls, 2.0);
        tools.add(tool);
        
        List<OcrEvalTool> ocrs = new ArrayList<>();
        OcrEvalTool ocr = new OcrEvalTool("pruOCR", "PruOCR");
        ocr.addEvaluation(true);        
        ocr.addEvaluation("10.0", "1.0", "---", "1.0", "1.0", "---");
        ocr.addEvaluation("10.0", "1.0", "0.5", "1.0", "1.0", "0.5");
        ocrs.add(ocr);
        
        List<LayoutEvalTool> layouts = new ArrayList<>();
        LayoutEvalTool layout = new LayoutEvalTool("PruLay", "PRULAY");
        layout.addEvaluation("10.0", "12.0");
        layouts.add(layout);
        
        rep.setTools(tools);
        rep.setOcrEvalTools(ocrs);
        rep.setLayoutEvalTools(layouts);
        
        rep.asExcel();
        
    }
    
}
