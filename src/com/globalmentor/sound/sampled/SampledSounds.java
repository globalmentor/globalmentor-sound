/*
 * Copyright © 1996-2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.globalmentor.sound.sampled;

import java.io.*;

import javax.sound.sampled.*;

import com.globalmentor.util.Debug;

/**A collection of methods for manipulating sampled sounds.
@author Garret Wilson
*/
public class SampledSounds
{

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
	public static DataLine getDataLine(final InputStream inputStream, final Class<?> lineClass) throws IOException, UnsupportedAudioFileException, LineUnavailableException
	{
		AudioInputStream audioInputStream=AudioSystem.getAudioInputStream(inputStream);	//get an audio input stream from the input stream
		AudioFormat audioFormat=audioInputStream.getFormat(); //get the format of the input stream
		DataLine.Info dataLineInfo=new DataLine.Info(lineClass, audioFormat);	//create info for a new data line based on the format
		if(!AudioSystem.isLineSupported(dataLineInfo))  //if we don't support this type of line, try to convert the audio input stream to something we do support
		{
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
				  //create a new audio input stream with the conversion audio format
			audioInputStream=AudioSystem.getAudioInputStream(audioFormat, audioInputStream);
			dataLineInfo=new DataLine.Info(lineClass, audioFormat);	//create info for a new data line based on the new format
		}
		final DataLine dataLine=(DataLine)AudioSystem.getLine(dataLineInfo);	//get a new line from the audio system and return it
		if(dataLine instanceof Clip)	//if this is a clip
		{
			((Clip)dataLine).open(audioInputStream);	//open the clip here, since the calling function does not have access to the audio input stream
		}
		return dataLine;	//return the line we opened
	}

	protected static void play(final InputStream inputStream) throws IOException, UnsupportedAudioFileException, LineUnavailableException	//TODO unfinished; finish or delete
	{
		AudioInputStream audioInputStream=AudioSystem.getAudioInputStream(inputStream);	//get an audio input stream from the input stream
		AudioFormat audioFormat=audioInputStream.getFormat();
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
		int bytesPerFrame=audioFormat.getFrameSize();
		DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
		if(AudioSystem.isLineSupported(dataLineInfo))
		{
		  try
			{
				SourceDataLine line=(SourceDataLine)AudioSystem.getLine(dataLineInfo);
				line.open(audioFormat);
				line.start();
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
		  }
			catch (LineUnavailableException ex)
			{
				Debug.error(ex);	//TODO fix
		  }
		}
	}
}