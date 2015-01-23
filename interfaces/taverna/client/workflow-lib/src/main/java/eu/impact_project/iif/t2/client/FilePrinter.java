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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

/**
 * Redirects a temporarily stored file to the response object. In effect, the
 * file is sent to the web browser as a download.
 */

public class FilePrinter extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public FilePrinter() {
		super();
	}

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	/**
	 * Reads the specified file from the temp directory and redirects the
	 * contents to the response output stream
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String fileName = "";

		try {
			fileName = request.getParameter("file");
		} catch (Exception e) 
                {
                    e.printStackTrace();
		}

		if (fileName != null && !fileName.equals("")) {
			String tmpDir = ((File) getServletContext().getAttribute("javax.servlet.context.tempdir")).getAbsolutePath();
			File file = new File(tmpDir + "/" + fileName);
			InputStream inStream = new FileInputStream(file);
			OutputStream outStream = response.getOutputStream();

			BufferedInputStream bis = new BufferedInputStream(inStream);

			int bufSize = 1024 * 8;
			byte[] bytes = new byte[bufSize];
			int count = bis.read(bytes);

			while (count != -1 && count <= bufSize) {
				outStream.write(bytes, 0, count);
				count = bis.read(bytes);
			}
			if (count != -1) outStream.write(bytes, 0, count);
			outStream.close();
		}
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
	}
}
