package com.garretwilson.sound.sampled;

import java.io.*;
import javax.sound.sampled.*;

import com.globalmentor.util.Debug;

/**A collection of methods for manipulating sampled sounds.
@author Garret Wilson
*/
public class SampledSoundUtilities
{
	/**Constructor that prevents this class from being instantiated.*/
  private SampledSoundUtilities() {}

	/**Constructs and returns a data line of the specified type from the given
		input stream. If a <code>Clip</code> is returned it will already be open.
		The input stream should be one that allows marking so that
		<code>AudioSystem.getAudioFileFormat()</code> can correctly extract
		information before the audio is actually read.
	@param inputStream The input stream of the audio.
	@param lineClass The type of line desired (for example <code>Clip.class</code>).
	@return A data line from the input stream. This line will already be open if
		the line is a <code>Clip</code>.
	@exception IOException Thrown if there is an error reading from the given
		input stream.
	@exception UnsupportedAudioFileException Thrown if the audio file represented
		by the input stream is not supported.
	@exception LineUnavailableException Thrown if the line is not available because,
		for example, it is already in use by another application.
	*/
	public static DataLine getDataLine(final InputStream inputStream, final Class lineClass) throws IOException, UnsupportedAudioFileException, LineUnavailableException
	{
//G***fix		play(inputStream);  //G***testing
//G***del		AudioFileFormat audioFileFormat=AudioSystem.getAudioFileFormat(inputStream);
		AudioInputStream audioInputStream=AudioSystem.getAudioInputStream(inputStream);	//get an audio input stream from the input stream
		AudioFormat audioFormat=audioInputStream.getFormat(); //get the format of the input stream
Debug.trace("got format: ", audioFormat); //G***del
		DataLine.Info dataLineInfo=new DataLine.Info(lineClass, audioFormat);	//create info for a new data line based on the format
		if(!AudioSystem.isLineSupported(dataLineInfo))  //if we don't support this type of line, try to convert the audio input stream to something we do support
		{
Debug.trace("line not supported: ", dataLineInfo);  //G***del
			//create a new audio format to convert from the unsupported format;
			//note that some VBR implementations return negative numbers for sample
			//  frame sizes, so the following will not always work:
			//new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, audioFormat.getSampleRate(), audioFormat.getSampleSizeInBits()*2, audioFormat.getChannels(), audioFormat.getFrameSize()*2, audioFormat.getFrameRate(), true)
			audioFormat=new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
				audioFormat.getSampleRate(),
				16,
				audioFormat.getChannels(),
				audioFormat.getChannels() * 2,
				audioFormat.getSampleRate(),
				false);
Debug.trace("new format: ", audioFormat); //G***del
				  //create a new audio input stream with the conversion audio format
			audioInputStream=AudioSystem.getAudioInputStream(audioFormat, audioInputStream);
//G***del			audioFormat=conversionAudioFormat;	//use the new audio format now
			dataLineInfo=new DataLine.Info(lineClass, audioFormat);	//create info for a new data line based on the new format
		}
/*G***old stuff; testing new stuff
Debug.trace("getting data line"); //G***del
		final AudioFileFormat audioFileFormat=AudioSystem.getAudioFileFormat(inputStream);	//get the file format of the audio
Debug.trace("got audio file format: ", audioFileFormat); //G***del
		AudioFormat audioFormat=audioFileFormat.getFormat();	//get the format of the audio itself
Debug.trace("got format: ", audioFormat); //G***del
		AudioInputStream audioInputStream=AudioSystem.getAudioInputStream(inputStream);	//get an audio input stream from the input stream
Debug.trace("input stream has encoding: ", audioFormat.getEncoding());  //G***del; testing
Debug.trace("sample rate: "+audioFormat.getSampleRate());
Debug.trace("channels: ", audioFormat.getChannels());
Debug.trace("frame size: ", audioFormat.getFrameSize());
Debug.trace("sample size in bits: ", audioFormat.getSampleSizeInBits());
*/

/*G***fix
		if(audioFormat.getEncoding()==AudioFormat.Encoding.ULAW || audioFormat.getEncoding()==AudioFormat.Encoding.ALAW)
		{
			//create a new audio format to convert from the compressed audio
			AudioFormat conversionAudioFormat=new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
				audioFormat.getSampleRate(), audioFormat.getSampleSizeInBits()*2,
				audioFormat.getChannels(), audioFormat.getFrameSize()*2,
				audioFormat.getFrameRate(), true);
			audioInputStream=AudioSystem.getAudioInputStream(conversionAudioFormat, audioInputStream);	//create a new audio input stream with the conversion audio format
			audioFormat=conversionAudioFormat;	//use the new audio format now
		}
*/
/*G***del; works, but moved
			//create a new audio format to convert from the compressed audio
			AudioFormat conversionAudioFormat=new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
				audioFormat.getSampleRate(),
				16,
				audioFormat.getChannels(),
				audioFormat.getChannels() * 2,
				audioFormat.getSampleRate(),
				false);
			audioInputStream=AudioSystem.getAudioInputStream(conversionAudioFormat, audioInputStream);	//create a new audio input stream with the conversion audio format
			audioFormat=conversionAudioFormat;	//use the new audio format now
*/


/*G***fix
		  AudioFormat	sourceFormat = audioFormat; //G***testing

			AudioFormat	targetFormat = new AudioFormat(
				AudioFormat.Encoding.PCM_SIGNED,
				sourceFormat.getSampleRate(),
				16,
				sourceFormat.getChannels(),
				sourceFormat.getChannels() * 2,
				sourceFormat.getSampleRate(),
				false);
			audioFormat=targetFormat;	//use the new audio format now

*/
		final DataLine dataLine=(DataLine)AudioSystem.getLine(dataLineInfo);	//get a new line from the audio system and return it
		if(dataLine instanceof Clip)	//if this is a clip
			((Clip)dataLine).open(audioInputStream);	//open the clip here, since the calling function does not have access to the audio input stream
		return dataLine;	//return the line we opened
	}

	protected static void play(final InputStream inputStream) throws IOException, UnsupportedAudioFileException, LineUnavailableException
	{
Debug.trace("playing input stream");  //G***del

		AudioInputStream audioInputStream=AudioSystem.getAudioInputStream(inputStream);	//get an audio input stream from the input stream
		AudioFormat audioFormat=audioInputStream.getFormat();
Debug.trace("got format: ", audioFormat); //G***del

			//create a new audio format to convert from the compressed audio
			AudioFormat conversionAudioFormat=new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
				audioFormat.getSampleRate(),
				16,
				audioFormat.getChannels(),
				audioFormat.getChannels() * 2,
				audioFormat.getSampleRate(),
				false);
			audioInputStream=AudioSystem.getAudioInputStream(conversionAudioFormat, audioInputStream);	//create a new audio input stream with the conversion audio format
			audioFormat=conversionAudioFormat;	//use the new audio format now

Debug.trace("new format: ", audioFormat); //G***del

		int bytesPerFrame=audioFormat.getFrameSize();
Debug.trace("frame size: ", bytesPerFrame);
		DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
Debug.trace("got data line info for audio format: ", dataLineInfo);

		if(AudioSystem.isLineSupported(dataLineInfo))
		{
Debug.trace("audio system supports line info");

		  try
			{
				SourceDataLine line=(SourceDataLine)AudioSystem.getLine(dataLineInfo);
Debug.trace("got line: ", line);
				line.open(audioFormat);
Debug.trace("opened line"); //G***del
				line.start();
Debug.trace("line started");  //G***del


		final int	EXTERNAL_BUFFER_SIZE = 128000;
		byte[]	abData = new byte[EXTERNAL_BUFFER_SIZE];

		int	nBytesRead = 0;
		while (nBytesRead != -1)
		{
			try
			{
				nBytesRead = audioInputStream.read(abData, 0, abData.length);
			}
			catch (IOException e)
			{
				Debug.error(e);	//TODO fix
			}
			if (nBytesRead >= 0)
			{
				int	nBytesWritten = line.write(abData, 0, nBytesRead);
			}
		}

/*G***del

while (total < totalToRead && !stopped)}
    int numBytesRead = audioInputStream.read(myData, 0, numBytesToRead);
    if (numBytesRead == -1) break;
    total += numBytesRead;
    line.write(myData, 0, numBytesRead);

}
*/
		  }
			catch (LineUnavailableException ex)
			{
				Debug.error(ex);	//TODO fix
		  }
		}

	}

/*G***scratch
boolean isConversionSupported(AudioFormat targetFormat,
    AudioFormat sourceFormat)
*/
}