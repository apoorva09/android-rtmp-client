package wrapper;

/**
 * This class generates the (Audio/Video) Header Specific Info sent by RTMP Client .
 *
 */
public class DecoderSpecificInfo {

     public static final int NALU_START_CODE = 4;

	 /**
	 * Function Check for the Start Code of the NALU 0 0 0 1
	 *
	 * @param aBuffer - Stream Header Buffer
	 * @param aIndex  - Present Index
	 * @return Flag if the Start Code is there
	 */
	 public static boolean checkStartCode (final byte[] aBuffer,
	 	  	 								 final int aIndex)
	 {
	 	   return ((aBuffer[aIndex] == 0x0) &&
	 	  		 	(aBuffer[aIndex + 1] == 0x0)&&
	 	  		 	(aBuffer[aIndex + 2] == 0x0)&&
	 	  		 	(aBuffer[aIndex + 3] == 0x1));
	  }

	 /**
	 * Function to get the Next NALU in the stream
	 *
	 * @param aBuffer - Stream Header Buffer
	 * @param aIndex  - Present Index
	 * @param aLength - Buffer Length
	 * @return size of the NALU
	 */
	 public static int getNextNALU (final byte[] aBuffer,
	  	 								 final int aIndex,
	  	 								 final int aLength)
	  {
	  		 int index = aIndex+1;
	  		 {
	  			 for(;index < aLength;index ++)
	  			 {
	  				 if (checkStartCode(aBuffer,index))
	  					break;
	  			 }
	  		 }
	  		 return (index - aIndex - NALU_START_CODE);
	  }

	 /**
	 * Function to get the number of PPS(Picture Parameter Set) in the Stream
	 *
	 * @param aBuffer - Stream Header Buffer
	 * @param aIndex  - Present Index
	 * @param aLength - Buffer Length
	 * @return Number of PPS
	 */
	  public static int getPPSNumber (final byte[] aBuffer,
	  	 								 final int aIndex,
	  	 								 final int aLength)
	  {
	  		 int ppsnum = 0;
	  		 int index = aIndex;
	  		 while(index < aLength)
	  		 {
	  			 int lSize = getNextNALU(aBuffer,index,aLength);
	  			 index += (lSize + NALU_START_CODE);
	  			 ppsnum++;
	  		 }
	  		 return ppsnum;
	  }

	/**
	 * Function to Realign the Data as required by the RTMP
	 * [Size of SPS in 2 bytes][SPS Data][Number of PPS in 1 byte][Size of PPS in 2 bytes][PPS Data]......
	 * SPS Sequence Parameter Set PPS Picture Parameter Set
	 * @param decspecinfo - Video Header Info
	 * @param decSpecInfoSize  - Header Size
	 * @return VideoHeader Buffer
	 */
	 public static byte[] VideoConstructorInfo(final byte[] decspecinfo,
	 											final int decSpecInfoSize)
	 {
			int i = 0;
			byte[] aBuffer = new byte[128];
			// SPS
			int lSize = getNextNALU(decspecinfo,i,decSpecInfoSize);
			aBuffer[0] = (byte) ((lSize >> 8) & 0xFF);
			aBuffer[1] = (byte) ((lSize) & 0xFF);
			System.arraycopy(decspecinfo, NALU_START_CODE, aBuffer, 2, lSize);
			int lIndex = 2 + lSize;
			i += (lSize + NALU_START_CODE);
			// PPS
			int lppsnumber = getPPSNumber(decspecinfo,i,decSpecInfoSize);
			aBuffer[lIndex++] = (byte)lppsnumber;
			while(lppsnumber > 0)
			{
				lSize = getNextNALU(decspecinfo,i,decSpecInfoSize);
				aBuffer[lIndex] = (byte) ((lSize >> 8) & 0xFF);
				aBuffer[lIndex + 1] = (byte) ((lSize) & 0xFF);
				System.arraycopy(decspecinfo, i + NALU_START_CODE, aBuffer, lIndex + 2,lSize);
				lIndex += (2 + lSize);
				i += (lSize + NALU_START_CODE);
				lppsnumber --;
			}
			byte[] videoHeaderBuffer = new byte[lIndex];
			System.arraycopy(aBuffer, 0, videoHeaderBuffer, 0, lIndex);
			return videoHeaderBuffer;
	}

	/**
	* Function to create the Audio Header Data as required by the RTMP
	*
	*
	* @param aObjectType - Audio Object Type ( 2 for AAC)
	* @param aSampleFreq  - Sampling Frequency
	* @param aChannelCount - Number of Channels
	* @return AudioHeader Buffer
	*/

	 public static byte[] AudioConstructorInfo(final int aObjectType,
	 								  final int aSampleFreq,
	 								  final int aChannelCount)
	 {
	 		int lSampFreqIdx = 4;

			switch(aSampleFreq){
				case 96000:
					lSampFreqIdx = 0;
					break;
				case 88200:
					lSampFreqIdx = 1;
					break;
				case 64000:
					lSampFreqIdx = 2;
					break;
				case 48000:
					lSampFreqIdx = 3;
					break;
				case 44100:
					lSampFreqIdx = 4;
					break;
				case 32000:
					lSampFreqIdx = 5;
					break;
				case 24000:
					lSampFreqIdx = 6;
					break;
				case 22050:
					lSampFreqIdx = 7;
					break;
				case 16000:
					lSampFreqIdx = 8;
					break;
				case 12000:
					lSampFreqIdx = 9;
					break;
				case 11025:
					lSampFreqIdx = 10;
					break;
				case 8000:
					lSampFreqIdx = 11;
					break;
				case 7350:
					lSampFreqIdx = 12;
					break;
				default:
					lSampFreqIdx = 4;

			}

			byte[] aBuffer = new byte[2];
	 		aBuffer[0] = (byte) (((aObjectType << 3) | (lSampFreqIdx >> 1)) & 0xff);
	 		aBuffer[1] = (byte) ((((lSampFreqIdx & 1) << 7) | (aChannelCount << 3)) & 0xff);

			return aBuffer;

	}
}

