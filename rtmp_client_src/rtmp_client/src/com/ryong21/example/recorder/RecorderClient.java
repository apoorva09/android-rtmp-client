package com.ryong21.example.recorder;

import java.io.File;
import java.util.Map;

import org.apache.mina.core.buffer.IoBuffer;
import org.red5.io.IStreamableFile;
import org.red5.io.ITag;
import org.red5.io.ITagWriter;
import org.red5.io.flv.FLVService;
import org.red5.io.flv.Tag;
import org.red5.io.utils.ObjectMap;
import org.red5.server.event.IEvent;
import org.red5.server.event.IEventDispatcher;
import org.red5.server.net.rtmp.RTMPClient;
import org.red5.server.net.rtmp.event.AudioData;
import org.red5.server.net.rtmp.event.IRTMPEvent;
import org.red5.server.net.rtmp.event.Ping;
import org.red5.server.net.rtmp.event.VideoData;
import org.red5.server.net.rtmp.status.StatusCodes;
import org.red5.server.service.IPendingServiceCall;
import org.red5.server.service.IPendingServiceCallback;
import org.red5.server.stream.IStreamData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * A record client record server stream to local file.
 * 
 */
public class RecorderClient implements IEventDispatcher, IPendingServiceCallback{
	private static Logger log = LoggerFactory.getLogger(RecorderClient.class);

	private static final int STOPPED = 0;
	private static final int CONNECTING = 1;
	private static final int STREAM_CREATING = 2;
	private static final int PLAYING = 3;

	private String host;
	private int port;
	private String app ;
	private String fileName;
	private String saveAsFileName;
	
	private int duration = 10*1000;
	private int start = 0;
	private int playLen = 10;
	private int streamId;
	private int state;
	
	private ITagWriter writer;
	private RTMPClient rtmpClient;

	public RecorderClient() {
		state = STOPPED;
	}

	public void setHost(String value) {
		host = value;
	}

	public void setPort(int value) {
		port = value;
	}

	public void setApp(String value) {
		app = value;
	}

	public void setDuration(int value) {
		duration = value;
	}

	public void setStartTime(int value) {
		start = value;
	}

	public int getState() {
		return state;
	}

	public void start(String playFileName, String saveAsFileName) {	
		this.fileName = playFileName;
		this.saveAsFileName = saveAsFileName;
		init();
		rtmpClient = new RTMPClient();
		rtmpClient.setStreamEventDispatcher(this);
		state=CONNECTING;
		Map<String, Object> defParams = rtmpClient.makeDefaultConnectionParams(
				host, port, app);
		rtmpClient.connect(host, port, defParams, this, null);
	}

	private void init() {
		File file = new File(saveAsFileName != null ? saveAsFileName : fileName);
		FLVService flvService = new FLVService();
		flvService.setGenerateMetadata(true);
		try {
			IStreamableFile flv = flvService.getStreamableFile(file);
			writer = flv.getWriter();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void stop() {
		if (writer != null) {
			writer.close();
			writer = null;
		}
	}

	synchronized public void resultReceived(IPendingServiceCall call) {
		log.debug("resultReceived:> {}", call.getServiceMethodName());
		if ("connect".equals(call.getServiceMethodName())) {
			state = STREAM_CREATING;
			rtmpClient.createStream(this);
		} else if ("createStream".equals(call.getServiceMethodName())) {
			state = PLAYING;
			Object result = call.getResult();
			if (result instanceof Integer) {
				Integer streamIdInt = (Integer) result;
				streamId = streamIdInt.intValue();
				log.debug("Playing state{} name {} ", state, fileName);
				rtmpClient.play(streamIdInt, fileName, start, duration);
				
				Ping ping = new Ping();
				ping.setEventType(Ping.CLIENT_BUFFER);
				ping.setValue2(streamId);
				ping.setValue3(2000);
				rtmpClient.getConnection().ping(ping);
				rtmpClient.setServiceProvider(this);

			} else {
				rtmpClient.disconnect();
				state = STOPPED;
			}
		}
	}
	
	@SuppressWarnings("rawtypes")
	public void onStatus(Object obj)
	{
		ObjectMap map = (ObjectMap) obj;
		String code = (String) map.get("code");
		String description = (String) map.get("description");
		String details = (String) map.get("details");
		
		if (StatusCodes.NS_PLAY_RESET.equals(code)){
			log.debug("{}: {}", new Object[]{code,description});
		} else if (StatusCodes.NS_PLAY_START.equals(code)){
			log.info("playing video by name: " + fileName);
			log.debug("{}: {}", new Object[]{code,description});
		} else if (StatusCodes.NS_PLAY_STOP.equals(code)){
			state = STOPPED;
			log.debug("{}: {}", new Object[]{code,description});
			log.info("Recording Complete");
			rtmpClient.disconnect();
			stop();
		} else if (StatusCodes.NS_PLAY_STREAMNOTFOUND.equals(code)){
			state = STOPPED;
			log.info("File {} Not found",new Object[]{details});
			log.debug("{} for {}", new Object[]{code,details});
			rtmpClient.disconnect();
			stop();
		}		
	}
		
	public void dispatchEvent(IEvent event) {
		IRTMPEvent rtmpEvent = (IRTMPEvent) event;

		if (!(rtmpEvent instanceof IStreamData)) {
			log.debug("skipping non stream data");
			return;
		}
		if (rtmpEvent.getHeader().getSize() == 0) {
			log.debug("skipping event where size == 0");
			return;
		}
		
		ITag tag = new Tag();
		tag.setDataType(rtmpEvent.getDataType());
		if (rtmpEvent instanceof VideoData) {
			tag.setTimestamp(rtmpEvent.getTimestamp());			
		} else if (rtmpEvent instanceof AudioData) {
			tag.setTimestamp(rtmpEvent.getTimestamp());
		}
		
		if(rtmpEvent.getTimestamp()/1000 > playLen){
			log.debug("play progress: {} seconds", playLen);
			playLen += 10;
		}
		
		@SuppressWarnings("rawtypes")
		IoBuffer data = ((IStreamData) rtmpEvent).getData().asReadOnlyBuffer();
		tag.setBodySize(data.limit());
		tag.setBody(data);
		try {
			writer.writeTag(tag);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
