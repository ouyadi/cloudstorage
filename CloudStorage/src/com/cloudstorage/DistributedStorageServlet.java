package com.cloudstorage;

import java.io.IOException;
import java.nio.ByteBuffer;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.google.appengine.tools.cloudstorage.GcsFilename;

@SuppressWarnings("serial")
public class DistributedStorageServlet extends HttpServlet {
	private ServletFileUpload uploader = null;
	
	
	@Override
	public void init() throws ServletException{
		
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		/*
		StringBuilder builder = new StringBuilder();
		BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
		String json = "";
		while ((json = br.readLine()) != null) {
		builder.append(json);
		}
		String result = builder.toString();
		*/
		String action = request.getParameter("action");
		if(action.equals("insert")){
			StorageHandler handler = new StorageHandler();
			String key = request.getParameter("key");
			GcsFilename name = new GcsFilename("project-bucket",key);
			HttpSession session = request.getSession();
			byte[] content = (byte[]) session.getAttribute("buffer");
			handler.writeToFile(name, content);
		}
	}
	
	

}
