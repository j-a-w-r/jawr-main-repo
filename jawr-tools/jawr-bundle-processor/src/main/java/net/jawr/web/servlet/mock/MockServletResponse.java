/**
 * Copyright 2009-2013 Ibrahim Chaehoi
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
package net.jawr.web.servlet.mock;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import net.jawr.web.resource.bundle.IOUtils;

/**
 * Mock implementation of the HttpServletResponse interface.
 * Supports the Servlet 2.5 API level.
 *  
 * @author Ibrahim Chaehoi
 */
public class MockServletResponse implements HttpServletResponse {

	/** The destination file */
	private OutputStream outStream;
	
	/** The writer */
	private PrintWriter writer;

	/** The string writer */
	private StringWriter strWriter = new StringWriter();

	
	/**
	 * Constructor 
	 */
	public MockServletResponse() {
		
	}

	/**
	 * Sets the output stream for the response
	 * @param out the output stream to set
	 */
	public void setOutputStream(OutputStream out) {
		this.outStream = out;
		writer = new PrintWriter(out);
	}
	
	/**
	 * Close the streams 
	 */
	public void close() {
		
		writer.close();
		IOUtils.close(outStream);
	}
	
	/**
	 * Returns the response content
	 * 
	 * @return the response content
	 */
	public String getContent() {

		return strWriter.getBuffer().toString();
	}

	/**
	 * Clears the response content
	 */
	public void clear() {

		strWriter.getBuffer().setLength(0);
	}

	public void addCookie(Cookie cookie) {

	}

	public void addDateHeader(String name, long date) {

	}

	public void addHeader(String name, String value) {

	}

	public void addIntHeader(String name, int value) {

	}

	public boolean containsHeader(String name) {

		return false;
	}

	public String encodeRedirectURL(String url) {

		return null;
	}

	public String encodeRedirectUrl(String url) {

		return null;
	}

	public String encodeURL(String url) {

		return null;
	}

	public String encodeUrl(String url) {

		return null;
	}

	public void sendError(int sc) throws IOException {

	}

	public void sendError(int sc, String msg) throws IOException {

	}

	public void sendRedirect(String location) throws IOException {

	}

	public void setDateHeader(String name, long date) {

	}

	public void setHeader(String name, String value) {

	}

	public void setIntHeader(String name, int value) {

	}

	public void setStatus(int sc) {

	}

	public void setStatus(int sc, String sm) {

	}

	public void flushBuffer() throws IOException {

	}

	public int getBufferSize() {

		return 0;
	}

	public String getCharacterEncoding() {

		return null;
	}

	public Locale getLocale() {

		return null;
	}

	public ServletOutputStream getOutputStream() throws IOException {

		ServletOutputStream out = new ServletOutputStream(){

			public void write(int b) throws IOException {
				outStream.write(b);
			}
			
		};
		return out;
	}

	public PrintWriter getWriter() throws IOException {
		return writer;
	}

	public boolean isCommitted() {

		return false;
	}

	public void reset() {

	}

	public void resetBuffer() {

	}

	public void setBufferSize(int size) {

	}

	public void setContentLength(int len) {

	}

	public void setContentType(String type) {

	}

	public void setLocale(Locale loc) {

	}

	@Override
	public String getContentType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setCharacterEncoding(String charset) {
		// TODO Auto-generated method stub
		
	}

}
