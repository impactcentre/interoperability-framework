/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.impact_project.iif.tw.util;

import static eu.impact_project.iif.tw.util.FileUtil.JAVA_TMP;
import java.io.Closeable;
import java.io.File;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Impact
 */
public class FileUtilTest
{       

    /**
     * Test of getSystemTempFolder method, of class FileUtil.
     */
    @Test
    public void testGetSystemTempFolder()
    {
        FileUtil.getSystemTempFolder();        
    }

    /**
     * Test of getTmpFile method, of class FileUtil.
     */
    @Test
    public void testGetTmpFile_String_String()
    {
        File temp = new File(JAVA_TMP, "gwg-tmp-store");
        if(temp.exists())
            FileUtil.deleteTempFiles(temp);
        FileUtil.getTmpFile("prueba", "tmp");        
    }

    /**
     * Test of getTmpFile method, of class FileUtil.
     */
    @Test
    public void testGetTmpFile_3args()
    {        
        byte[] data = new byte[1];
        data[0]=(byte)0xFF;
        FileUtil.getTmpFile(data, "prueba", "tmp");        
    }

    /**
     * Test of deleteTempFiles method, of class FileUtil.
     */
    @Test
    public void testDeleteTempFiles()
    {
       File temp = new File(JAVA_TMP, "gwg-tmp-store");
       if (!temp.exists())
           FileUtil.getTmpFile("prueba", "tmp");
       byte[] data = new byte[1];
       data[0]=(byte)0xFF;
       FileUtil.getTmpFile(data, "prueba", "tmp"); 
       FileUtil.deleteTempFiles(temp);
    }

    /**
     * Test of close method, of class FileUtil.
     */
    @Test
    public void testClose()
    {
        
    }

    /**
     * Test of mkdir method, of class FileUtil.
     */
    @Test
    public void testMkdir()
    {
        
    }   

    
    
}
