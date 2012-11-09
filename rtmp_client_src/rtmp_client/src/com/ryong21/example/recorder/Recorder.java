package com.ryong21.example.recorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ryong21.example.recorder.RecorderClient;

public class Recorder {
	
	protected static Logger log = LoggerFactory.getLogger(Recorder.class);
		
	public static void main(String[] args) {
		
		String playFileName = "2.mp3";
		String saveAsFileName = "22.flv";
		
		String host = "192.168.1.200";
		int port = 1935;
		String app = "vod";

		RecorderClient client = new RecorderClient();		
		client.setHost(host);
		client.setPort(port);
		client.setApp(app);	

		client.start(playFileName, saveAsFileName);
	}
}
