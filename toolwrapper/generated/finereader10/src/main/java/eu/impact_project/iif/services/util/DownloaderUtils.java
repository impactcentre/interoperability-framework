// DownloaderUtil
// mirror the contents of an apache style directory listing.
// only 1 level deep, so subdirs, but no subdirs of subdirs.
package eu.impact_project.iif.service.util;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.net.URLConnection;

//import eu.impact_project.iif.service.util.FileUtils;
//import org.apache.commons.io.FileUtils;

public final class DownloaderUtils {
    public static final String JAVA_TMP = System.getProperty("java.io.tmpdir");
    private static final String TMP_DIR = "finereader10-tmp-store";

    public static File mirror(String base_url) {

	String inputLine;
	Boolean is_index = false;
	Boolean pd = false;
	List<String> files = new ArrayList<String>();
	List<String> subdirs = new ArrayList<String>();

	String store_path = JAVA_TMP + File.separator + DownloaderUtils.TMP_DIR + File.separator + "mirror" + File.separator;
	File mirror_path = new File(store_path);
	mirror_path.mkdirs();

	URL inputurl;
	URLConnection uc;
	BufferedReader in;
	File destination;
	File file;

	try {
            inputurl = new URL(base_url);
	    uc = inputurl.openConnection();
            in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
            while ((inputLine = in.readLine()) != null)  {
		if (inputLine.indexOf("<title>Index of ") > -1) {
		    is_index = true;
		}
		for (String out: inputLine.split(">")) {
		    if (out.indexOf("</a") > -1) {
			if (pd == true ) {
			    out = out.split("<")[0].trim();
			    if (out.endsWith("/"))  {
			      subdirs.add(out);	    
			    } else {
			      files.add(out);
			    }
			}
		    }
		    if (out.endsWith("Parent Directory</a")) {
			pd = true;
		    }
		}
	    }
	    in.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}

	for (String item: files) {
	    try {
                inputurl = new URL(base_url + item);
		destination = new File(store_path + item);
		FileUtils.urlToPath(inputurl, destination);
	    } catch (Exception e) {
		e.printStackTrace();
	    }
 	}

	File subdirfile;
	for (String subdir: subdirs) {
	    files = new ArrayList<String>();
	    String store_path_subdir = store_path + File.separator + subdir;
	    file = new File(store_path_subdir);
	    file.mkdirs();
	    pd = false;
	    try {
		inputurl = new URL(base_url + subdir );
		uc = inputurl.openConnection();
		in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
		while ((inputLine = in.readLine()) != null)  {
		    if (inputLine.indexOf("<title>Index of ") > -1) {
			is_index = true;
		    }
		    for (String out: inputLine.split(">")) {
			if (out.indexOf("</a") > -1) {
			    if (pd == true ) {
				out = out.split("<")[0].trim();
				if (!out.endsWith("/"))  {
				    files.add(out);
				}
			    }
			}
			if (out.endsWith("Parent Directory</a")) {
			    pd = true;
			}
		    }
		}
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	    for (String item: files) {
		try {
		    inputurl = new URL(base_url + subdir + "/" + item);
		    destination = new File(store_path_subdir + File.separator + item);
		    FileUtils.urlToPath(inputurl, destination);
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }
	}
    return (new File(store_path));
    }
}
