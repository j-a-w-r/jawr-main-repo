/**
 * Copyright 2007-2014 Ibrahim Chaehoi
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
package net.jawr.web.minification;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;

/* jsmin.c
 2013-03-29

 Copyright (c) 2002 Douglas Crockford  (www.crockford.com)

 Permission is hereby granted, free of charge, to any person obtaining a copy of
 this software and associated documentation files (the "Software"), to deal in
 the Software without restriction, including without limitation the rights to
 use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 of the Software, and to permit persons to whom the Software is furnished to do
 so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 The Software shall be used for Good, not Evil.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */

/**
 * 
 * This work is a translation from C to Java of jsmin.c published by Douglas
 * Crockford. See www.inconspicuous.org and www.crockford.com for more
 * information. Now available on Github :
 * https://github.com/douglascrockford/JSMin
 * 
 * @author (Original) John Reilly - Douglas Crockford
 * @author Jordi Hernández Sellés
 * @author ibrahim Chaehoi
 */
public class JSMin {
	private static final int EOF = -1;

	/** The input stream */
	private PushbackInputStream in;
	
	/** The output stream */
	private OutputStream out;

	private int theA;
	private int theB;
	private int theLookahead = EOF;
	private int theX = EOF;
	private int theY = EOF;

	/** The current byte index */
	private int currentByteIndex;
	
	/** The current line number */
	private int line;
	
	/** The current column number */
	private int column;

	/**
	 * Constructor
	 * 
	 * @param in
	 * @param out
	 */
	public JSMin(InputStream in, OutputStream out) {
		this.in = new PushbackInputStream(in);
		this.out = out;
		line = 0;
		column = 0;
	}

	/*
	 * isAlphanum -- return true if the character is a letter, digit,
	 * underscore, dollar sign, or non-ASCII character.
	 */

	private boolean isAlphanum(int c) {
		return ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')
				|| (c >= 'A' && c <= 'Z') || c == '_' || c == '$' || c == '\\' || c > 126);
	}

	/*
	 * get -- return the next character from stdin. Watch out for lookahead. If
	 * the character is a control character, translate it to a space or
	 * linefeed.
	 */
	private int get() throws IOException {

		return get(false);
	}

	/*
	 * get -- return the next character from stdin. Watch out for lookahead. If
	 * the character is a control character, translate it to a space or
	 * linefeed.
	 */
	private int get(boolean inStringLiteral) throws IOException {
		int c = theLookahead;
		theLookahead = EOF;
		if (c == EOF) {
			c = in.read();
			currentByteIndex++;
		}
		if (c == '\n') {
			line++;
			column = 0;
		} else {
			column++;
		}
		if (c >= ' ' || c == '\n' || c == EOF || (inStringLiteral && c == '\t')) { // Handle
																					// the
																					// case
																					// of
																					// tab
																					// character
																					// in
																					// String
																					// literal
			return c;
		}

		if (c == '\r') {
			return '\n';
		}
		return ' ';
	}

	/*
	 * peek -- get the next character without getting it.
	 */
	/**
	 * Get the next character without getting it.
	 */
	int peek() throws IOException {

		theLookahead = get();
		return theLookahead;
	}

	/*
	 * next -- get the next character, excluding comments. peek() is used to see
	 * if a '/' is followed by a '/' or '*'.
	 */
	private int next() throws IOException, UnterminatedCommentException {
		int c = get();
		if (c == '/') {
			switch (peek()) {
			case '/':
				for (;;) {
					c = get();
					if (c <= '\n') {
						break;
					}
				}
				break;
			case '*':
				get();
				while (c != ' ') {
					switch (get()) {
					case '*':
						if (peek() == '/') {
							get();
							c = ' ';
						}
						break;
					case EOF:
						throw new UnterminatedCommentException(
								currentByteIndex, line, column);
					}
				}
				break;
			}
		}
		theY = theX;
		theX = c;
		return c;
	}

	/*
	 * action -- do something! What you do is determined by the argument: 1
	 * Output A. Copy B to A. Get the next B. 2 Copy B to A. Get the next B.
	 * (Delete A). 3 Get the next B. (Delete B). action treats a string as a
	 * single character. Wow! action recognizes a regular expression if it is
	 * preceded by ( or , or =.
	 */

	private void action(int d) throws IOException, JSMinException {
		switch (d) {
		case 1:
			out.write(theA);
			if ((theY == '\n' || theY == ' ')
					&& (theA == '+' || theA == '-' || theA == '*' || theA == '/')
					&& (theB == '+' || theB == '-' || theB == '*' || theB == '/')) {
				out.write(theY);
			}
		case 2:
			theA = theB;
			if (theA == '\'' || theA == '"' || theA == '`') {
				for (;;) {
					out.write(theA);
					theA = get(true);
					if (theA == theB) {
						break;
					}
					if (theA == '\\') {
						out.write(theA);
						theA = get(true);
					}
					if (theA == EOF) {
						throw new UnterminatedStringLiteralException(
								currentByteIndex, line, column);
					}
				}
			}
		case 3:
			theB = next();
			if (theB == '/'
					&& (theA == '(' || theA == ',' || theA == '='
							|| theA == ':' || theA == '[' || theA == '!'
							|| theA == '&' || theA == '|' || theA == '?'
							|| theA == '+' || theA == '-' || theA == '~'
							|| theA == '*' || theA == '/' || theA == '{' || theA == '\n')) {
				out.write(theA);
				if (theA == '/' || theA == '*') {
					out.write(' ');
				}
				out.write(theB);
				for (;;) {
					theA = get();
					if (theA == '[') {
						for (;;) {
							out.write(theA);
							theA = get();
							if (theA == ']') {
								break;
							}
							if (theA == '\\') {
								out.write(theA);
								theA = get();
							}
							if (theA == EOF) {
								throw new UnterminatedRegExpLiteralException( // Unterminated
																				// set
																				// in
																				// Regular
																				// Expression
																				// literal.
										currentByteIndex, line, column);
							}
						}
					} else if (theA == '/') {
						switch (peek()) {
						case '/':
						case '*':
							throw new UnterminatedRegExpLiteralException(// Unterminated
																			// set
																			// in
																			// Regular
																			// Expression
																			// literal.
									currentByteIndex, line, column);
						}
						break;
					} else if (theA == '\\') {
						out.write(theA);
						theA = get();
					}
					if (theA == EOF) {
						throw new UnterminatedRegExpLiteralException(
								currentByteIndex, line, column);
					}
					out.write(theA);
				}
				theB = next();
			}
		}
	}

	/*
	 * jsmin -- Copy the input to the output, deleting the characters which are
	 * insignificant to JavaScript. Comments will be removed. Tabs will be
	 * replaced with spaces. Carriage returns will be replaced with linefeeds.
	 * Most spaces and linefeeds will be removed.
	 */
	public void jsmin() throws IOException, JSMinException {
		if (peek() == 0xEF) { // Handle Unicode BOM
			get();
			get();
			get();
		}
		theA = ' ';
		action(3);
		while (theA != EOF) {
			switch (theA) {
			case ' ':
				action(isAlphanum(theB) ? 1 : 2);
				break;
			case '\n':
				switch (theB) {
				case '{':
				case '[':
				case '(':
				case '+':
				case '-':
				case '!':
				case '~':
					action(1);
					break;
				case ' ':
					action(3);
					break;
				default:
					action(isAlphanum(theB) ? 1 : 2);
				}
				break;
			default:
				switch (theB) {
				case ' ':
					action(isAlphanum(theA) ? 1 : 3);
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
					case '`':
						action(1);
						break;
					default:
						action(isAlphanum(theA) ? 1 : 3);
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
		 * @param byteIndex
		 *            the byteIndex where the exception occured
		 * @param line
		 *            the line where the exception occured
		 * @param column
		 *            the column where the exception occured
		 */
		public JSMinException(int byteIndex, int line, int column) {
			super();
			this.byteIndex = byteIndex;
			this.line = line;
			this.column = column;
		}

		/**
		 * Returns the byteIndex where the exception occured
		 * 
		 * @return the byteIndex where the exception occured
		 */
		public int getByteIndex() {
			return byteIndex;
		}

		/**
		 * Returns the line where the exception occured
		 * 
		 * @return the line where the exception occured
		 */
		public int getLine() {
			return line;
		}

		/**
		 * Returns the column where the exception occured
		 * 
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
		 * @param byteIndex
		 *            the byteIndex where the exception occured
		 * @param line
		 *            the line where the exception occured
		 * @param column
		 *            the column where the exception occured
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
		 * @param byteIndex
		 *            the byteIndex where the exception occured
		 * @param line
		 *            the line where the exception occured
		 * @param column
		 *            the column where the exception occured
		 */
		public UnterminatedStringLiteralException(int byteIndex, int line,
				int column) {
			super(byteIndex, line, column);
		}

	}

	public class UnterminatedRegExpLiteralException extends JSMinException {

		/** The serial version UID */
		private static final long serialVersionUID = -7357153586067632159L;

		/**
		 * Constructor
		 * 
		 * @param byteIndex
		 *            the byteIndex where the exception occured
		 * @param line
		 *            the line where the exception occured
		 * @param column
		 *            the column where the exception occured
		 */
		public UnterminatedRegExpLiteralException(int byteIndex, int line,
				int column) {
			super(byteIndex, line, column);
		}

	}

	public class UnterminatedSetInRegExpLiteralException extends JSMinException {

		/** The serial version UID */
		private static final long serialVersionUID = 3323096122240883283L;

		/**
		 * Constructor
		 * 
		 * @param byteIndex
		 *            the byteIndex where the exception occured
		 * @param line
		 *            the line where the exception occured
		 * @param column
		 *            the column where the exception occured
		 */
		public UnterminatedSetInRegExpLiteralException(int byteIndex, int line,
				int column) {
			super(byteIndex, line, column);
		}

	}
}
