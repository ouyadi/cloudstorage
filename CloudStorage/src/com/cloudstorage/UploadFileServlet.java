package com.cloudstorage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

public class UploadFileServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//if(ServletFileUpload.isMultipartContent(request)){
			ServletFileUpload upload = new ServletFileUpload();
			try {
				FileItemIterator iterator = upload.getItemIterator(request);
				while (iterator.hasNext()) {
			        FileItemStream item = iterator.next();
			        InputStream stream = item.openStream();
			        ByteBuffer bf = ByteBuffer.allocate(1024*1024*101);
			        byte[] buffer = new byte[1024];
			        while (stream.read(buffer, 0, buffer.length) != -1) {
			            bf.put(buffer);
			        }
			        buffer=bf.array();
			        HttpSession session = request.getSession();
			        session.setAttribute("buffer", buffer);
				}   
			} catch (FileUploadException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	//}
}
