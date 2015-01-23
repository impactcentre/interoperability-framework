package eu.impact_project.wsclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
public class UploadFiles extends HttpServlet {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	   private ServletFileUpload uploader = null;

		public void init(ServletConfig config) throws ServletException {
			super.init(config);
		}
	   
		protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
			System.out.println("Entro al doGet");
			String fileName = request.getParameter("filename");
			if(fileName == null || fileName.equals("")){
				throw new ServletException("File Name can't be null or empty");
			}
			File file = new File(request.getSession().getServletContext().getAttribute("FILES_DIR")+File.separator+fileName);
			if(!file.exists()){
				throw new ServletException("File doesn't exists on server.");
			}
			ServletContext ctx = getServletContext();
			InputStream fis = new FileInputStream(file);
			String mimeType = ctx.getMimeType(file.getAbsolutePath());
			
			
			response.setContentType("application/force-download");  
                        response.setDateHeader("Expires", 0);  
			response.setContentType(mimeType != null? mimeType:"application/octet-stream");
			response.setContentLength((int) file.length());
                        response.setHeader("Content-Transfer-Encoding", "binary");  
                        response.setHeader("Cache-Control", "private");  
			response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

			ServletOutputStream os = response.getOutputStream();
			byte[] bufferData = new byte[1024];
			int read=0;
			while((read = fis.read(bufferData))!= -1){
				os.write(bufferData, 0, read);
			}
			os.flush();
			os.close();
			fis.close();
			System.out.println("File downloaded at client successfully");
		}

		protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
			if(!ServletFileUpload.isMultipartContent(request)){
				throw new ServletException("Content type is not multipart/form-data");
			}
			DiskFileItemFactory fileFactory = new DiskFileItemFactory();
			File filesDir = (File) request.getSession().getServletContext().getAttribute("FILES_DIR_FILE");
			
			fileFactory.setRepository(filesDir);
			this.uploader = new ServletFileUpload(fileFactory);

			response.setContentType("text/html");
	                PrintWriter out = response.getWriter();
                        response.setContentType("text/plain");  // Set content type of the response so that jQuery knows what it can expect.
                        response.setCharacterEncoding("UTF-8"); // You want world domination, huh?
	                //out.write("<html><head></head><body>");
	        
			try {
				List<FileItem> fileItemsList = uploader.parseRequest(request);
				Iterator<FileItem> fileItemsIterator = fileItemsList.iterator();
				while(fileItemsIterator.hasNext()){
					FileItem fileItem = fileItemsIterator.next();
					String filename = fileItem.getName();                                        
					filename = filename.replaceAll("[^\\dA-Za-z0-9.]", "").replaceAll("\\s+", "");                                                             
                                        Timestamp currentTimestamp = new java.sql.Timestamp(Calendar.getInstance().getTime().getTime());                                        
                                        String name, extension;
                                        if(filename.lastIndexOf(".")!=-1)
                                        {
                                            name = filename.substring(0, filename.lastIndexOf("."));
                                            extension = filename.substring(filename.lastIndexOf("."));
                                        }
                                        else
                                        {
                                            name = filename;
                                            extension = "";
                                        }
                                        filename = name + currentTimestamp.getTime() + extension;
                                        
					System.out.println("filename: " + filename);
					System.out.println("FieldName="+fileItem.getFieldName());
					System.out.println("FileName="+fileItem.getName());
					System.out.println("ContentType="+fileItem.getContentType());
					System.out.println("Size in bytes="+fileItem.getSize());
					System.out.println("FILES_DIR: " + request.getSession().getServletContext().getAttribute("FILES_DIR") + " ## File.separator" + File.separator + " ## getName: " + fileItem.getName());
				 
					File file = new File(request.getSession().getServletContext().getAttribute("FILES_DIR") + File.separator + filename);
					System.out.println("Absolute Path at server="+file.getAbsolutePath());
					fileItem.write(file);
					//out.write("File "+fileItem.getName()+ " uploaded successfully.");
                                        //out.write("<br>");
					//response.setHeader("Content-Disposition", "attachment; filename=\"UploadDownloadFileServlet?filename=" + filename );
                                        out.write(filename);
				}
			} catch (FileUploadException e) {
				System.out.println("Exception in uploading file.");
			} catch (Exception e) {
				System.out.println("Exception in uploading file.");
			}
			//out.write("</body></html>");
			//RequestDispatcher rd = getServletContext().getRequestDispatcher(
    			//	"/interface.jsp");
    		        //rd.forward(request, response);
		}
}
