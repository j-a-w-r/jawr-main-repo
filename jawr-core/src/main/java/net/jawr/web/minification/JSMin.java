/**
 * Copyright 2014 Ibrahim Chaehoi
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
/**
 * 
 * JSMin.java 2006-02-13
 * 
 * Updated 2007-08-20 with updates from jsmin.c (2007-05-22)
 * 
 * Copyright (c) 2006 John Reilly (www.inconspicuous.org)
 * 
 * This work is a translation from C to Java of jsmin.c published by
 * Douglas Crockford.  Permission is hereby granted to use the Java 
 * version under the same conditions as the jsmin.c on which it is
 * based.  
 * 
 * 
 * 
 * 
 * jsmin.c 2003-04-21
 * 
 * Copyright (c) 2002 Douglas Crockford (www.crockford.com)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * The Software shall be used for Good, not Evil.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.jawr.web.minification;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;

/**
 * 
 * This work is a translation from C to Java of jsmin.c published by Douglas
 * Crockford. See www.inconspicuous.org and www.crockford.com for more
 * information.
 * 
 * @author (Original) John Reilly - Douglas Crockford
 * @author ibrahim Chaehoi
 */
public class JSMin {
	private static final int EOF = -1;

	private PushbackInputStream in;
	private OutputStream out;

	private int theA;
	private int theB;

	private int currentByteIndex;
	private int line;
	private int column;

	/**
	 * Constructor
	 * @param in
	 * @param out
	 */
	public JSMin(InputStream in, OutputStream out) {
		this.in = new PushbackInputStream(in);
		this.out = out;
		line = 0;
		column = 0;
	}

	/**
	 * isAlphanum -- return true if the character is a letter, digit,
	 * underscore, dollar sign, or non-ASCII character.
	 */
	static boolean isAlphanum(int c) {
		return ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')
				|| (c >= 'A' && c <= 'Z') || c == '_' || c == '$' || c == '\\' || c > 126);
	}

	int get() throws IOException
	{ return get(false); } 
	
	/**
	 * get -- return the next character from stdin. Watch out for lookahead. If
	 * the character is a control character, translate it to a space or
	 * linefeed.
	 */
	int get(boolean handleTabCharacter) throws IOException {
		int c = in.read();
		currentByteIndex++;
		if (c == '\n') {
			line++;
			column = 0;
		} else {
			column++;
		}

		if (c >= ' ' || c == '\n' || c == EOF) {
			return c;
		}

		if(handleTabCharacter && c == '\t'){
			return c; 	
		}
		
		if (c == '\r') {
			column = 0;
			return '\n';
		}

		return ' ';
	}

	/**
	 * Get the next character without getting it.
	 */
	int peek() throws IOException {
		int lookaheadChar = in.read();
		in.unread(lookaheadChar);
		return lookaheadChar;
	}

	/**
	 * next -- get the next character, excluding comments. peek() is used to see
	 * if a '/' is followed by a '/' or '*'.
	 */
	int next() throws IOException, UnterminatedCommentException {
		int c = get();
		if (c == '/') {
			switch (peek()) {
			case '/':
				for (;;) {
					c = get();
					if (c <= '\n') {
						return c;
					}
				}

			case '*':
				get();
				for (;;) {
					switch (get()) {
					case '*':
						if (peek() == '/') {
							get();
							return ' ';
						}
						break;
					case EOF:
						throw new UnterminatedCommentException(currentByteIndex, line, column);
					}
				}

			default:
				return c;
			}

		}
		return c;
	}

	/**
	 * action -- do something! What you do is determined by the argument: 1
	 * Output A. Copy B to A. Get the next B. 2 Copy B to A. Get the next B.
	 * (Delete A). 3 Get the next B. (Delete B). action treats a string as a
	 * single character. Wow! action recognizes a regular expression if it is
	 * preceded by ( or , or =.
	 */

	void action(int d) throws IOException, UnterminatedRegExpLiteralException,
			UnterminatedCommentException, UnterminatedStringLiteralException {
		switch (d) {
		case 1:
			out.write(theA);
		case 2:
			theA = theB;

			if (theA == '\'' || theA == '"') {
				for (;;) {
					out.write(theA);
					theA = get(true);
					if (theA == theB) {
						break;
					}
					if (theA <= '\n' && theA != '\t') {
						throw new UnterminatedStringLiteralException(
								currentByteIndex, line, column);
					}
					if (theA == '\\') {
						out.write(theA);
						theA = get(true);
					}
				}
			}

		case 3:
			theB = next();
			if (theB == '/'
					&& (theA == '(' || theA == ',' || theA == '='
							|| theA == ':' || theA == '[' || theA == '!'
							|| theA == '&' || theA == '|' || theA == '?'
							|| theA == '{' || theA == '}' || theA == ';' || theA == '\n')) {
				out.write(theA);
				out.write(theB);
				for (;;) {
					theA = get();
					if (theA == '/') {
						break;
					} else if (theA == '\\') {
						out.write(theA);
						theA = get();
					} else if (theA <= '\n') {
						throw new UnterminatedRegExpLiteralException(
								currentByteIndex, line, column);
					}
					out.write(theA);
				}
				theB = next();
			}
		}
	}

	/**
	 * jsmin -- Copy the input to the output, deleting the characters which are
	 * insignificant to JavaScript. Comments will be removed. Tabs will be
	 * replaced with spaces. Carriage returns will be replaced with linefeeds.
	 * Most spaces and linefeeds will be removed.
	 */
	public void jsmin() throws IOException, JSMinException {
		theA = '\n';
		action(3);
		while (theA != EOF) {
			switch (theA) {
			case ' ':
				if (isAlphanum(theB)) {
					action(1);
				} else {
					action(2);
				}
				break;
			case '\n':
				switch (theB) {
				case '{':
				case '[':
				case '(':
				case '+':
				case '-':
					action(1);
					break;
				case ' ':
					action(3);
					break;
				default:
					if (isAlphanum(theB)) {
						action(1);
					} else {
						action(2);
					}
				}
				break;
			default:
				switch (theB) {
				case ' ':
					if (isAlphanum(theA)) {
						action(1);
						break;
					}
					action(3);
					break;
				case '\n':
					switch (theA) {
					case '}':
					case ']':
					case ')':
					case '+':
					case '-':
					case '"':
					case '\'':
						action(1);
						break;
					default:
						if (isAlphanum(theA)) {
							action(1);
						} else {
							action(3);
						}
					}
					break;
				default:
					action(1);
					break;
				}
			}
		}
		out.flush();
	}

	/**
	 * The abstract class for JSMin exceptions
	 * 
	 * @author ibrahim Chaehoi
	 */
	public abstract class JSMinException extends Exception {
		
		/** The serial version UID */
		private static final long serialVersionUID = -9047848972645299111L;
		
		/** the byteIndex where the exception occured */
		private final int byteIndex;
		
		/** the line where the exception occured */
		private final int line;
		
		/** the column where the exception occured */
		private final int column;

		/**
		 * Constructor
		 * 
		 * @param byteIndex the byteIndex where the exception occured
		 * @param line the line where the exception occured
		 * @param column the column where the exception occured
		 */
		public JSMinException(int byteIndex, int line, int column) {
			super();
			this.byteIndex = byteIndex;
			this.line = line;
			this.column = column;
		}

		/**
		 * Returns the byteIndex where the exception occured 
		 * @return the byteIndex where the exception occured
		 */
		public int getByteIndex() {
			return byteIndex;
		}

		/**
		 * Returns the line where the exception occured 
		 * @return the line where the exception occured
		 */
		public int getLine() {
			return line;
		}

		/**
		 * Returns the column where the exception occured 
		 * @return the column where the exception occured
		 */
		public int getColumn() {
			return column;
		}
	}

	public class UnterminatedCommentException extends JSMinException {

		/** The serial version UID */
		private static final long serialVersionUID = 3034113564939556214L;

		/**
		 * Constructor
		 * 
		 * @param byteIndex the byteIndex where the exception occured
		 * @param line the line where the exception occured
		 * @param column the column where the exception occured
		 */
		public UnterminatedCommentException(int byteIndex, int line, int column) {
			super(byteIndex, line, column);
		}

	}

	public class UnterminatedStringLiteralException extends JSMinException {

		/** The serial version UID */
		private static final long serialVersionUID = -334185983508785451L;

		/**
		 * Constructor
		 * 
		 * @param byteIndex the byteIndex where the exception occured
		 * @param line the line where the exception occured
		 * @param column the column where the exception occured
		 */
		public UnterminatedStringLiteralException(int byteIndex, int line, int column) {
			super(byteIndex, line, column);
		}

	}

	public class UnterminatedRegExpLiteralException extends JSMinException {

		/** The serial version UID */
		private static final long serialVersionUID = -7357153586067632159L;

		/**
		 * Constructor
		 * 
		 * @param byteIndex the byteIndex where the exception occured
		 * @param line the line where the exception occured
		 * @param column the column where the exception occured
		 */
		public UnterminatedRegExpLiteralException(int byteIndex, int line, int column) {
			super(byteIndex, line, column);
		}

	}

}
