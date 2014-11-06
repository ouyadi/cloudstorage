package com.cloudstorage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.util.ArrayList;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
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

	private final GcsService gcsService = GcsServiceFactory.createGcsService(new RetryParams.Builder()
	.initialRetryDelayMillis(10)
	.retryMaxAttempts(10)
	.totalRetryPeriodMillis(15000)
	.build());

	public ArrayList<String> listing(String bucket, String prefix) throws IOException {
		ListResult fileList = gcsService.list(bucket, new ListOptions.Builder().setPrefix(prefix).build());
		ArrayList<String> fileNames = new ArrayList<String>();
		while(fileList.hasNext()) {
			ListItem fileItem = fileList.next();
			fileNames.add(fileItem.getName() + "/" + fileItem.getLength());
		}
		return fileNames;
	}

	public void retrieve (String bucket, String fileName,  HttpServletResponse resp) throws IOException {
		GcsFilename gcsFileName = new GcsFilename(bucket, fileName);
		GcsInputChannel readChannel = gcsService.openPrefetchingReadChannel(gcsFileName, 0, BUFFER_SIZE);
		copy(Channels.newInputStream(readChannel), resp.getOutputStream());
		String mimeType = gcsService.getMetadata(gcsFileName).getOptions().getMimeType();
		if(mimeType==null) {
			mimeType = "application/octet-stream";
		}
		resp.setContentType(mimeType);
		String headerKey = "Content-Disposition";
		String headerValue = String.format("attachment;filename=\"%s\"", gcsFileName.getObjectName());
		resp.setHeader(headerKey, headerValue);
	}

	public void insert (String bucket, String fileName, InputStream inputStream) throws IOException {
		GcsFilename filename = new GcsFilename(bucket, fileName);
		GcsOutputChannel outputChannel = gcsService.createOrReplace(filename, GcsFileOptions.getDefaultInstance());
		copy(inputStream, Channels.newOutputStream(outputChannel));
	}

	public Boolean delete (String bucket, String fileName) throws IOException {
		Boolean success = false;
		ListResult filesList = gcsService.list(bucket, new ListOptions.Builder().setPrefix(fileName).setRecursive(true).build());
		while(filesList.hasNext()) {

			success = gcsService.delete(new GcsFilename(bucket, filesList.next().getName()));
		}

		return success;
	}

	public Boolean checkExist (String bucket, String fileName) throws IOException {
		Boolean exist = false;
		ListResult filesList = gcsService.list(bucket, new ListOptions.Builder().setPrefix(fileName).setRecursive(true).build());
		exist = filesList.hasNext();
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
}
