package com.ryong21.example.publisher;

import java.io.File;
import java.io.IOException;

import org.red5.server.messaging.IMessage;
import org.red5.server.stream.message.RTMPMessage;
import org.red5.server.stream.provider.FileProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Publisher {

	public static void main(String[] args) throws IOException, InterruptedException {
		
		Logger log = LoggerFactory.getLogger(Publisher.class);
		
		String publishName = "test";
		String localFile = "red5.flv";
		String host = "192.168.1.200";
		int port = 1935;
		String app = "live";
		
		IMessage msg = null;
		int timestamp = 0;
		int lastTS = 0;
		
		PublishClient client = new PublishClient();
		
		client.setHost(host);
		client.setPort(port);
		client.setApp(app);			
		
		client.start(publishName, "live", null);
		
		while(client.getState() != PublishClient.PUBLISHED){
			Thread.sleep(500);
		}
		
		FileProvider fp = new FileProvider(new File(localFile));	
		
		while(true){
			msg = fp.pullMessage(null);			
			if(msg == null){
				log.debug("done!");
				break;
			}
			timestamp = ((RTMPMessage)msg).getBody().getTimestamp();
			Thread.sleep((timestamp - lastTS));
			lastTS = timestamp;
			client.pushMessage( msg);
		}	
		client.stop();
	}

}
