/**
 * Copyright 2012 Ibrahim Chaehoi
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package net.jawr.web.util.bom;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.InvalidParameterException;

/**
 * The <code>UnicodeBOMInputReader</code> class wraps any <code>Reader</code>
 * and detects the presence of any Unicode BOM (Byte Order Mark) at its
 * beginning, as defined by <a href="http://www.faqs.org/rfcs/rfc3629.html">RFC
 * 3629 - UTF-8, a transformation format of ISO 10646</a>
 * 
 * <p>
 * The <a href="http://www.unicode.org/unicode/faq/utf_bom.html">Unicode FAQ</a>
 * defines 5 types of BOMs:
 * <ul>
 * <li>
 * 
 * <pre>
 * 00 00 FE FF  = UTF-32, big-endian
 * </pre>
 * 
 * </li>
 * <li>
 * 
 * <pre>
 * FF FE 00 00  = UTF-32, little-endian
 * </pre>
 * 
 * </li>
 * <li>
 * 
 * <pre>
 * FE FF        = UTF-16, big-endian
 * </pre>
 * 
 * </li>
 * <li>
 * 
 * <pre>
 * FF FE        = UTF-16, little-endian
 * </pre>
 * 
 * </li>
 * <li>
 * 
 * <pre>
 * EF BB BF     = UTF-8
 * </pre>
 * 
 * </li>
 * </ul>
 * </p>
 * 
 * <p>
 * Use the {@link #getBOM()} method to know whether a BOM has been detected or
 * not.
 * </p>
 * <p>
 * Use the {@link #skipBOM()} method to remove the detected BOM from the wrapped
 * <code>Reader</code> object.
 * </p>
 * 
 * @author Ibrahim CHAEHOI inspired from UnicodeBOMInputStream from Gregory
 *         Pakosz
 */
public class UnicodeBOMReader extends Reader {

	private final PushbackReader in;
	private final Charset charset;
	private final BOM bom;
	private boolean skipped = false;

	/**
	 * Constructs a new <code>UnicodeBOMInputStream</code> that wraps the
	 * specified <code>InputStream</code>.
	 * 
	 * @param inputStream
	 *            an <code>InputStream</code>.
	 * 
	 * @param strCharset
	 *            a charset.
	 * 
	 * @throws IOException
	 *             on reading from the specified <code>InputStream</code> when
	 *             trying to detect the Unicode BOM.
	 */
	public UnicodeBOMReader(final Reader reader, final String strCharset)
			throws IOException

	{
		this(reader, Charset.forName(strCharset));
	}

	/**
	 * Constructs a new <code>UnicodeBOMInputStream</code> that wraps the
	 * specified <code>InputStream</code>.
	 * 
	 * @param inputStream
	 *            an <code>InputStream</code>.
	 * 
	 * @param strCharset
	 *            a charset.
	 * 
	 * @throws IOException
	 *             on reading from the specified <code>InputStream</code> when
	 *             trying to detect the Unicode BOM.
	 */
	public UnicodeBOMReader(final Reader reader, final Charset pCharset)
			throws IOException

	{
		if (reader == null)
			throw new InvalidParameterException(
					"invalid reader: null is not allowed");

		if (pCharset == null)
			throw new InvalidParameterException(
					"invalid charset: null is not allowed");

		in = new PushbackReader(reader, 4);
		charset = pCharset;

		final char[] chBom = new char[4];
		final int read = in.read(chBom);

		CharBuffer cbuf = CharBuffer.wrap(chBom);
		ByteBuffer bbuf = charset.encode(cbuf);
		final byte[] bom = bbuf.array();

		switch (read) {
		case 4:
			if ((bom[0] == (byte) 0xFF) && (bom[1] == (byte) 0xFE)
					&& (bom[2] == (byte) 0x00) && (bom[3] == (byte) 0x00)) {
				this.bom = BOM.UTF_32_LE;
				break;
			} else if ((bom[0] == (byte) 0x00) && (bom[1] == (byte) 0x00)
					&& (bom[2] == (byte) 0xFE) && (bom[3] == (byte) 0xFF)) {
				this.bom = BOM.UTF_32_BE;
				break;
			}

		case 3:
			if ((bom[0] == (byte) 0xEF) && (bom[1] == (byte) 0xBB)
					&& (bom[2] == (byte) 0xBF)) {
				this.bom = BOM.UTF_8;
				break;
			}

		case 2:
			if ((bom[0] == (byte) 0xFF) && (bom[1] == (byte) 0xFE)) {
				this.bom = BOM.UTF_16_LE;
				break;
			} else if ((bom[0] == (byte) 0xFE) && (bom[1] == (byte) 0xFF)) {
				this.bom = BOM.UTF_16_BE;
				break;
			}

		default:
			this.bom = BOM.NONE;
			break;
		}

		if (read > 0)
			in.unread(chBom, 0, read);
	}

	/**
	 * Returns the <code>BOM</code> that was detected in the wrapped
	 * <code>InputStream</code> object.
	 * 
	 * @return a <code>BOM</code> value.
	 */
	public final BOM getBOM() {
		// BOM type is immutable.
		return bom;
	}

	/**
	 * Skips the <code>BOM</code> that was found in the wrapped
	 * <code>InputStream</code> object.
	 * 
	 * @return this <code>UnicodeBOMInputStream</code>.
	 * 
	 * @throws IOException
	 *             when trying to skip the BOM from the wrapped
	 *             <code>InputStream</code> object.
	 */
	public final synchronized UnicodeBOMReader skipBOM() throws IOException {
		if (!skipped) {

			ByteBuffer bbuf = ByteBuffer.wrap(bom.getBytes());
			CharBuffer cbuf = charset.decode(bbuf);
			char[] bom = cbuf.array();
			int length = 0;
			for (int i = 0; i < bom.length; i++) {
				if (bom[i] == 0) {
					break;
				}
				length++;
			}
			in.skip(length);
			skipped = true;
		}
		return this;
	}

	/**
	 * Returns true if a BOM has been detected
	 * 
	 * @return true if a BOM has been detected
	 */
	public boolean hasBOM() {

		return !bom.equals(BOM.NONE);
	}

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		return in.read(cbuf, off, len);
	}

	public int read(CharBuffer target) throws IOException {
		return in.read(target);
	}

	public int read() throws IOException {

		return in.read();
	}

	public int read(char[] cbuf) throws IOException {
		return in.read(cbuf);
	}

	public long skip(long n) throws IOException {
		return in.skip(n);
	}

	public boolean ready() throws IOException {
		return in.ready();
	}

	public boolean markSupported() {
		return in.markSupported();
	}

	public void mark(int readAheadLimit) throws IOException {
		in.mark(readAheadLimit);
	}

	public void reset() throws IOException {
		in.reset();
	}

	public void close() throws IOException {
		in.close();
	}

}
