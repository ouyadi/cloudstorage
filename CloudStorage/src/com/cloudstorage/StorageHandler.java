package com.cloudstorage;

import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsInputChannel;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.cloudstorage.RetryParams;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;

public class StorageHandler {

	  private final GcsService gcsService = GcsServiceFactory.createGcsService(RetryParams.getDefaultInstance());

	  public GcsService getGcsService(){
		  return this.gcsService;
	  }
	  public void writeObjectToFile(GcsFilename fileName, Object content) throws IOException {
	    GcsOutputChannel outputChannel = gcsService.createOrReplace(fileName, GcsFileOptions.getDefaultInstance());
	    @SuppressWarnings("resource")
	    ObjectOutputStream oout = new ObjectOutputStream(Channels.newOutputStream(outputChannel));
	    oout.writeObject(content);
	    oout.close();
	  }

	  
	  public void writeToFile(GcsFilename fileName, byte[] content)throws IOException {
	    @SuppressWarnings("resource")
	    GcsOutputChannel outputChannel = gcsService.createOrReplace(fileName, GcsFileOptions.getDefaultInstance());
	    outputChannel.write(ByteBuffer.wrap(content));
	    outputChannel.close();
	  }

	  
	  public Object readObjectFromFile(GcsFilename fileName)throws IOException, ClassNotFoundException {
	    GcsInputChannel readChannel = gcsService.openPrefetchingReadChannel(fileName, 0, 1024 * 1024);
	    try (ObjectInputStream oin = new ObjectInputStream(Channels.newInputStream(readChannel))) {
	      return oin.readObject();
	    }
	  }

	  
	  public byte[] readFromFile(GcsFilename fileName) throws IOException {
	    int fileSize = (int) gcsService.getMetadata(fileName).getLength();
	    ByteBuffer result = ByteBuffer.allocate(fileSize);
	    try (GcsInputChannel readChannel = gcsService.openReadChannel(fileName, 0)) {
	      readChannel.read(result);
	    }
	    return result.array();
	  }


}

