/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package net.jawr.web.test.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public class IOUtils {

	/** the buffer size for reading data */
	private static final int BUFFER_SIZE = 16384;

	/**
	 * Writes all the contents of a Reader to a Writer.
	 * 
	 * @param reader the reader to read from
	 * @param writer the writer to write to
	 */
	public static void copy(Reader reader, Writer writer) throws IOException {
		copy(reader, writer, false);
	}
	
	/**
	 * Writes all the contents of a Reader to a Writer.
	 * 
	 * @param reader the reader to read from
	 * @param writer the writer to write to
	 * @param closeStreams the flag indicating if the stream must be close at the end, even if an exception occurs
	 */
	public static void copy(Reader reader, Writer writer, boolean closeStreams) throws IOException {
		char[] buf = new char[BUFFER_SIZE];
		int num = 0;

		try{
			while ((num = reader.read(buf, 0, buf.length)) != -1) {
				writer.write(buf, 0, num);
			}
		} finally {
			if (closeStreams) {
				close(reader);
				close(writer);
			}
		}
	}

	/**
	 * Writes all the contents of a Reader to a Writer.
	 * 
	 * @param reader the reader to read from
	 * @param writer the writer to write to
	 */
	public static void copy(InputStream input, OutputStream output)
			throws IOException {
		byte[] buf = new byte[BUFFER_SIZE];
		int num = 0;

		while ((num = input.read(buf, 0, buf.length)) != -1) {
			output.write(buf, 0, num);
		}
	}

	/**
	 * Writes all the contents of an InputStream to a Writer.
	 * 
	 * @param input the input stream to read from
	 * @param writer the writer to write to
	 */
	public static void copy(InputStream input, Writer writer)
			throws IOException {
		copy(new InputStreamReader(input), writer);
	}

	/**
	 * Writes all the contents of an InputStream to an OutputStream.
	 * 
	 * @param input the input stream to read from
	 * @param output the output stream to write to
	 * @param closeStreams the flag indicating if the stream must be close at the end, even if an exception occurs
	 */
	public static void copy(InputStream input, OutputStream output,
			boolean closeStreams) throws IOException {
		try {
			copy(input, output);
		} finally {
			if (closeStreams) {
				close(input);
				close(output);
			}
		}
	}

	/**
	 * Writes all the contents of a byte array to an OutputStream.
	 * 
	 * @param input the input stream to read from
	 * @param output the output stream to write to
	 * @param closeStreams the flag indicating if the stream must be close at the end, even if an exception occurs
	 */
	public static void write(byte[] byteArray, OutputStream out) throws IOException {
		if(byteArray != null){
			out.write(byteArray);
		}
	}
	
	/**
	 * Copy the readable byte channel to the writable byte channel
	 * 
	 * @param inChannel the readable byte channel
	 * @param outChannel the writable byte channel
	 * @throws IOException if an IOException occurs.
	 */
	public static void copy(ReadableByteChannel inChannel,
			WritableByteChannel outChannel) throws IOException {

		if (inChannel instanceof FileChannel) {
			((FileChannel) inChannel).transferTo(0, ((FileChannel) inChannel)
					.size(), outChannel);
		} else {

			final ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);
			try {

				while (inChannel.read(buffer) != -1) {
					// prepare the buffer to be drained
					buffer.flip();
					// write to the channel, may block
					outChannel.write(buffer);
					// If partial transfer, shift remainder down
					// If buffer is empty, same as doing clear()
					buffer.compact();
				}
				// EOF will leave buffer in fill state
				buffer.flip();
				// make sure the buffer is fully drained.
				while (buffer.hasRemaining()) {
					outChannel.write(buffer);
				}
			} finally {
				IOUtils.close(inChannel);
				IOUtils.close(outChannel);
			}
		}
	}

	/**
	 * Close the input stream
	 * 
	 * @param stream the input stream to close
	 */
	public static void close(InputStream stream) {

		if (stream != null) {

			try {
				stream.close();
			} catch (IOException e) {
				// Nothing to do
			}
		}
	}

	/**
	 * Close the output stream
	 * 
	 * @param stream the output stream to close
	 */
	public static void close(OutputStream stream) {

		if (stream != null) {

			try {
				stream.close();
			} catch (IOException e) {
				// Nothing to do
			}
		}
	}

	/**
	 * Close the channel
	 * 
	 * @param channel the channel to close
	 */
	public static void close(Channel channel) {
		if (channel != null) {

			try {
				channel.close();
			} catch (IOException e) {
				// Nothing to do
			}
		}
	}

	/**
	 * Close the reader
	 * @param reader the reader to close
	 */
	public static void close(Reader reader) {
		if (reader != null) {

			try {
				reader.close();
			} catch (IOException e) {
				// Nothing to do
			}
		}
	}
	
	/**
	 * Close the writer
	 * @param writer the writer to close
	 */
	public static void close(Writer writer) {
		if (writer != null) {

			try {
				writer.close();
			} catch (IOException e) {
				// Nothing to do
			}
		}
	}

	

}
