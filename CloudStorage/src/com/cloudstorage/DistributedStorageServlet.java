package com.cloudstorage;

import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.servlet.ServletFileUpload;




import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;



import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class DistributedStorageServlet extends HttpServlet {
	
	public static final boolean SERVE_USING_BLOBSTORE_API = false;
	public static final String BUCKET_NAME = "my-project";
	
	
//	private final GcsService gcsService = GcsServiceFactory.createGcsService(new RetryParams.Builder()
//    .initialRetryDelayMillis(10)
//    .retryMaxAttempts(10)
//    .totalRetryPeriodMillis(15000)
//    .build());
	
	private final DistributedStorageHandler dsHandler = new DistributedStorageHandler();
	
//	private static final int BUFFER_SIZE = 100 * 1024 * 1024;
	
	@Override
	  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
		String[] splits = req.getRequestURI().split("/", 5);

	    if (!splits[0].equals("") || !splits[1].equals("gcs")) 
	    {
	      throw new IllegalArgumentException("The URL is not formed as expected. " +
	          "Expecting /upload/<bucket>/<object>");
	    }
	    else if (splits[3].equals("download")) 
	    {
	    	try {
				dsHandler.retrieve(splits[2], splits[4], resp);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	    	try {
				dsHandler.retrieve(splits[2], splits[4], resp);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	    else if(splits[3].equals("list"))
	    {
		    resp.setContentType("text/plain");
	    	ArrayList<String> fileNames = dsHandler.listing(splits[2], splits[4]);
	    	for(String fileName : fileNames) {
				System.out.println(fileName);
			    resp.getWriter().println(fileName);
	    	}
	    }
	    else
	    {
		      throw new IllegalArgumentException("Unexpected URI");
	    }
	    
		
		
//		System.out.println(req);
//		processGetRequest(req);
//	    GcsFilename fileName = getFileName(req);
//	    GcsInputChannel readChannel = gcsService.openPrefetchingReadChannel(fileName, 0, BUFFER_SIZE);
//	    copy(Channels.newInputStream(readChannel), resp.getOutputStream());
//	    String mimeType = gcsService.getMetadata(fileName).getOptions().getMimeType();
//	    if(mimeType==null){
//	    	mimeType = "application/octet-stream";
//	    }
//	    resp.setContentType(mimeType);
//	    String headerKey = "Content-Disposition";
//	    String headerValue = String.format("attachment;filename=\"%s\"", fileName.getObjectName());
//	    resp.setHeader(headerKey, headerValue);
	  }
	
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws  IOException, ServletException {
		
		
		try {
		      ServletFileUpload upload = new ServletFileUpload();
		      resp.setContentType("text/plain");
		      
		      StringWriter writer = new StringWriter();
		      
		      FileItemIterator iterator = upload.getItemIterator(req);
		      while (iterator.hasNext()) {
		        FileItemStream item = iterator.next();
		        InputStream stream = item.openStream();

		        if (item.isFormField()) {
		        	if(item.getContentType()==null){
		        		continue;
		        	}
		        	else if(item.getFieldName().equals("action")){
		        		IOUtils.copy(stream, writer);
		        	}
		        } else {
		        	dsHandler.insert(BUCKET_NAME, item.getName(), stream);
//		        	GcsFilename filename = new GcsFilename("my-project",item.getName());
//		        	GcsOutputChannel outputChannel = gcsService.createOrReplace(filename, GcsFileOptions.getDefaultInstance());
//		        	copy(stream, Channels.newOutputStream(outputChannel));
		        }
		      }
		    } catch (Exception ex) {
		      throw new ServletException(ex);
		    }
		
		
		
		
	}
	
//	private GcsFilename getFileName(HttpServletRequest req) {
//	    String[] splits = req.getRequestURI().split("/", 5);
//	    if (!splits[0].equals("") || !splits[1].equals("gcs")) {
//	      throw new IllegalArgumentException("The URL is not formed as expected. " +
//	          "Expecting /upload/<bucket>/<object>");
//	    }
//	    return new GcsFilename(splits[2], splits[3]);
//	}
//	
//	private void copy(InputStream input, OutputStream output) throws IOException {
//	    try {
//	      byte[] buffer = new byte[BUFFER_SIZE];
//	      int bytesRead = input.read(buffer);
//	      while (bytesRead != -1) {
//	        output.write(buffer, 0, bytesRead);
//	        bytesRead = input.read(buffer);
//	      }
//	    } finally {
//	      input.close();
//	      output.close();
//	    }
//	}
	
	
	

}
