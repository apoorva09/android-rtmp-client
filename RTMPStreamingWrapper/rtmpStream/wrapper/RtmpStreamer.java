package wrapper;
import rtmp.RtmpClientManager;

/**
 * This class handles the RTMP Streaming calls of the client.
 *
 */
public class RtmpStreamer
{
	private RtmpClientManager clientManager;

	/**
	 * Constructor of RtmpStreamer. It initializes the clientManager.
	 */
	public RtmpStreamer()
	{
		clientManager = new RtmpClientManager();

	}
	/**
	 * Connects to the server.
	 * @param host : host address.eg. 192.168.1.67. One need not to mention "rtmp" before ip address
	 * @param port : port
	 * @param stream : name of the stream to retrieve from server
	 * @param AppName : application name
	 * @param userName : userName if authentication is required
	 * @param password : password
	 * @return
	 */
	public String connectToServer  (final String host,
								  	final int port,
								  	final String stream,
									final String AppName,
									final String userName,
									final String password)
	{
		return clientManager.connectToServer(host, port, stream, AppName, userName, password);
	}

	/**
	 * DisConnects the user from the server
	 * @return
	 */
	public int disconnectFromServer()
	{
		return clientManager.disconnectFromServer();
	}

	/**
	 * Sets the audio decoder specific info of the stream
	 * @param decSpecInfo : Audio decoder specific info
	 */
	/**
	 * Sets the audio decoder specific info of the stream
	 * @param SampleFreq - sampling frequency of audio stream
	 * @param NumChannel - no. of channels of audio stream
	 */
	public void setAACDecoderSpecInfo(int SampleFreq, int NumChannel)
	{
		clientManager.setAACDecoderSpecInfo(DecoderSpecificInfo.AudioConstructorInfo(2, //Objecttype = 2 for AAC
												SampleFreq,
												NumChannel));
	}

	/**
	 * Sets the video decoder specific info of the stream
	 * @param decSpecInfo : video decoder specific info returned by encoder.
	 * @param decSpecInfoSize : size of video decoder specific info returned by encoder.
	 */
	public void setH264DecoderSpecInfo(byte[] decSpecInfo, int decSpecInfoSize)
	{
		clientManager.setH264DecoderSpecInfo(DecoderSpecificInfo.VideoConstructorInfo(decSpecInfo, decSpecInfoSize));
	}

	/**
	 * This method publishes audio data to server along with it's timeStamp. It has to be called once the connection to server is established.
	 * @param timeStamp : timeStamp of the data to publish to server
	 * @param buf : audio data
	 * @param size : size of audio data
	 * @throws Exception : throws exception if connection to server get disconnect
	 */
	public synchronized void putAudioData(final long timeStamp,
										  final byte[] buf,
										  final int size) throws Exception
	{
		clientManager.putAudioData(timeStamp, buf, size);
	}

	/**
	 * This method publishes video data to server along with it's timestamp. It has to be called once the connection to server is established.
	 * @param timeStamp : timeStamp of the data to publish to server
	 * @param buf : video data
	 * @param size : size of video data
	 * @param keyframe
	 * @throws Exception : throws exception if connection to server get disconnect
	 */
	public synchronized void putVideoData(final long timeStamp,
										  final byte[] buf,
										  final int size,
										  final boolean keyframe) throws Exception
	{
		clientManager.putVideoData(timeStamp, buf, size, keyframe);
	}

	/*
	*	Main Function to test the Api Calls & for integration in the application.
	*
	*/


//	public static void main(String[] args)
//	{
//		RtmpClientManager rtmpClientManager = new RtmpClientManager();
//		rtmpClientManager.connectToServer("192.168.1.100", 1935, "test", "AppName", "user", "pass");
//		byte[] AudioDecoderSpecInfo = new byte[2];
//		int	SampleFreq = 44100;
//		int NumChannel = 2;
//		byte[] audioInfo= DecoderSpecificInfo.AudioConstructorInfo(2, 				//Objecttype = 2 for AAC
//												SampleFreq,
//												NumChannel);
//		rtmpClientManager.setAACDecoderSpecInfo(audioInfo);
		/*
		* test byte array is the example of the data which waw collected from the encoder.
		* decSpecInfoSize = h264encoder.TatvikH264EncoderGetDecoderSpecInfo(test, 128); //128 is the size of the test byte buffer
		*
		*/
//		int decSpecInfoSize = 27; // 27 is the buffer size of the data which is equal to decSpecInfoSize
//		byte[] test = {0, 0, 0, 1, 39, 66, 0, 41, -110, 84, 13, 10, -78, 120, 0, 15, 49, -110, -128, 0, 0, 0, 1, 40, -50, 60, -128, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
//		byte[] videoInfo = DecoderSpecificInfo.VideoConstructorInfo(test,decSpecInfoSize);
//		rtmpClientManager.setH264DecoderSpecInfo(videoInfo);
//		System.out.println(rtmpClientManager.outputResult);
//		if(rtmpClientManager.outputResult.contains("Connection Successed"))
//			rtmpClientManager.disconnectFromServer();
//	}

}

