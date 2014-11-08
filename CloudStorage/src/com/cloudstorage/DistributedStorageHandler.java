package com.cloudstorage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.memcache.AsyncMemcacheService;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsInputChannel;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.cloudstorage.ListItem;
import com.google.appengine.tools.cloudstorage.ListOptions;
import com.google.appengine.tools.cloudstorage.ListResult;
import com.google.appengine.tools.cloudstorage.RetryParams;

public class DistributedStorageHandler {

	private static final int BUFFER_SIZE = 100 * 1024 * 1024;
	private static final int CACHE_BUFFER_SIZE = 1024*100;
	
	private final GcsService gcsService = GcsServiceFactory.createGcsService(new RetryParams.Builder()
	.initialRetryDelayMillis(10)
	.retryMaxAttempts(10)
	.totalRetryPeriodMillis(15000)
	.build());

	public ArrayList<String> listing(String bucket, String keyWord) throws IOException {		
		ListResult fileList = gcsService.list(bucket, new ListOptions.Builder().build());
		ArrayList<String> fileNames = new ArrayList<String>();
		while(fileList.hasNext()) {
			ListItem fileItem = fileList.next();
			fileNames.add(fileItem.getName()+ "/" + fileItem.getLength()/1024+"kB");
		}
		ArrayList<String> matches = new ArrayList<String>();
		for(String fileName:fileNames) {
			if(fileName.split("/")[0].contains(keyWord)) {
				matches.add(fileName);
			}
		}
		return matches;
	}

	public HttpServletResponse retrieve (String bucket, String fileName,  HttpServletResponse resp) throws IOException, InterruptedException, ExecutionException {
	    AsyncMemcacheService asyncCache = MemcacheServiceFactory.getAsyncMemcacheService();
	    byte[] value;
	    GcsFilename gcsFileName = new GcsFilename(bucket, fileName);
	    String mimeType=null;
		Future<Object> futureValue = asyncCache.get(fileName); 
		value = (byte[])futureValue.get();
		if(value!=null){
			OutputStream output = resp.getOutputStream();
			output.write(value);
			output.close();
		}
		else{
			GcsInputChannel readChannel = gcsService.openPrefetchingReadChannel(gcsFileName, 0, BUFFER_SIZE);
			copy(Channels.newInputStream(readChannel), resp.getOutputStream());
			mimeType = gcsService.getMetadata(gcsFileName).getOptions().getMimeType();
		}
		if(mimeType==null) {
			mimeType = "application/octet-stream";
		}
		resp.setContentType(mimeType);
		String headerKey = "Content-Disposition";
		String headerValue = String.format("attachment;filename=\"%s\"", gcsFileName.getObjectName());
		resp.setHeader(headerKey, headerValue);
		return resp;
	}

	public void insert (String bucket, String fileName, InputStream inputStream) throws IOException, InterruptedException, ExecutionException {
		if(inputStream.available()<=CACHE_BUFFER_SIZE){
			byte[] value = new byte[CACHE_BUFFER_SIZE];
			inputStream.read(value);
			AsyncMemcacheService asyncCache =  MemcacheServiceFactory.getAsyncMemcacheService();
			asyncCache.put(fileName, value);
		}
		GcsFilename filename = new GcsFilename(bucket, fileName);
		GcsOutputChannel outputChannel = gcsService.createOrReplace(filename, GcsFileOptions.getDefaultInstance());
		copy(inputStream, Channels.newOutputStream(outputChannel));
	}

	public Boolean delete (String bucket, String fileName) throws IOException {
		Boolean success = false;
		MemcacheService cache =  MemcacheServiceFactory.getMemcacheService();
		if(cache.contains(fileName)){
			success = cache.delete(fileName);
		}
		ArrayList<String> fileNames = this.listFileNames(bucket);
		if(fileNames.contains(fileName)) {
			success = gcsService.delete(new GcsFilename(bucket, fileName));
		}
		return success;
	}

	public Boolean checkExist (String bucket, String fileName) throws IOException {
		Boolean exist = false;
		MemcacheService cache =  MemcacheServiceFactory.getMemcacheService();
		if(exist = cache.contains(fileName)){
			exist = true;
		}
		else{
			ArrayList<String> fileNames = this.listFileNames(bucket);
			exist = fileNames.contains(fileName);
		}
		return exist;
		
	}

	private void copy(InputStream input, OutputStream output) throws IOException {
		try {
			byte[] buffer = new byte[BUFFER_SIZE];
			int bytesRead = input.read(buffer);
			while (bytesRead != -1) {
				output.write(buffer, 0, bytesRead);
				bytesRead = input.read(buffer);
			}
		} finally {
			input.close();
			output.close();
		}
	}
	
	private ArrayList<String> listFileNames(String bucket) throws IOException {		
		ListResult fileList = gcsService.list(bucket, new ListOptions.Builder().build());
		ArrayList<String> fileNames = new ArrayList<String>();
		while(fileList.hasNext()) {
			ListItem fileItem = fileList.next();
			fileNames.add(fileItem.getName());
		}
		return fileNames;
	}
}
