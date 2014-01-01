/**
 * Copyright 2007-2012 Jordi Hernández Sellés, Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.postprocess.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;

import net.jawr.web.exception.BundlingProcessException;
import net.jawr.web.minification.JSMin;
import net.jawr.web.minification.JSMin.JSMinException;
import net.jawr.web.resource.bundle.IOUtils;
import net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus;
import net.jawr.web.resource.bundle.postprocess.PostProcessFactoryConstant;

/**
 * This postprocessor will minify a javascript bundle using Douglas Crockford's JSMin,
 * in its java implementation (see www.crockford.com and www.inconspicuous.org). 
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 */
public class JSMinPostProcessor extends
		AbstractJsChainedResourceBundlePostProcessor {
	
	/** The line feed string */
	private static final String LF = "\n";
	
	/** the carriage return line feed string */
	private static final String CR_LF = "\r\n";

	/**
	 * Constructor for a compressor.  
	 * @param charset
	 */
	public JSMinPostProcessor() {
		super(PostProcessFactoryConstant.JSMIN);
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.postprocess.impl.AbstractChainedResourceBundlePostProcessor#doPostProcessBundle(java.lang.StringBuffer)
	 */
	protected StringBuffer doPostProcessBundle(BundleProcessingStatus status,StringBuffer bundleString)
			throws IOException {
		Charset charset = status.getJawrConfig().getResourceCharset();
		
		// The original JSMin doesn't handle Dos (CRLF) line endings
		// So here we replace the CRLF with LF only
		String bundleContent = bundleString.toString().replaceAll(CR_LF,LF);
		
		byte[] bundleBytes = bundleContent.getBytes(charset.name());
		ByteArrayInputStream bIs = new ByteArrayInputStream(bundleBytes);
		ByteArrayOutputStream bOs = new ByteArrayOutputStream();
		
		// Compress data and recover it as a byte array. 
		JSMin minifier = new JSMin(bIs,bOs);
		try {
			minifier.jsmin();
		} catch (JSMinException e) {			
			formatAndThrowJSLintError(status, bundleBytes, e);				
		} 
		byte[] minified = bOs.toByteArray();
		return  byteArrayToString(charset, minified);
	}
	
	/**
	 * Utility method for components that need to use JSMin in a different context other than 
	 * bundle postprocessing. 
	 * 
	 * @param sb the content to minify
	 * @param charset the charset
	 * @return the minified content
	 */
	public StringBuffer minifyStringBuffer(StringBuffer sb, Charset charset) throws IOException, JSMinException {
		byte[] bundleBytes = sb.toString().getBytes(charset.name());
		ByteArrayInputStream bIs = new ByteArrayInputStream(bundleBytes);
		ByteArrayOutputStream bOs = new ByteArrayOutputStream();
		
		// Compress data and recover it as a byte array. 
		JSMin minifier = new JSMin(bIs,bOs);
		minifier.jsmin();
		byte[] minified = bOs.toByteArray();
		return  byteArrayToString(charset, minified);
	}

	/**
	 * Convert a byte array to a String buffer taking into account the charset 
	 * @param charset the charset
	 * @param minified the byte array
	 * @return the string buffer
	 * @throws IOException if an IO exception occurs
	 */
	private StringBuffer byteArrayToString(Charset charset, byte[] minified)
			throws IOException {
		// Write the data into a string
		ReadableByteChannel chan = Channels.newChannel(new ByteArrayInputStream(minified));
        Reader rd = Channels.newReader(chan,charset.newDecoder(),-1);
        StringWriter writer = new StringWriter();
        IOUtils.copy(rd, writer, true);
        return writer.getBuffer();
	}

	/**
	 * Upon an exception thrown during minification, this method will throw an error with detailed information. 
	 * @param status the bundle processing status
	 * @param bundleBytes the byte array of the bundle content
	 * @param e the JSMinException
	 */
	private void formatAndThrowJSLintError(BundleProcessingStatus status, byte[] bundleBytes, JSMinException e) {
		StringBuffer errorMsg = new StringBuffer("JSMin failed to minify the bundle with id: '" + status.getCurrentBundle().getId() + "'.\n");
		errorMsg.append("The exception thrown is of type:" + e.getClass().getName() + "'.\n");
		int currentByte = e.getByteIndex();
		int startPoint;
		if(currentByte < 100)
			startPoint = 0;
		else startPoint = currentByte - 100;
		int totalSize = currentByte - startPoint;
		
		byte[] lastData = new byte[totalSize];
		
		for(int x = 0; x < totalSize; x++) {
			lastData[x] = bundleBytes[startPoint];
			startPoint++;
		}
		errorMsg.append("The error happened at this point in your javascript: \n");
		errorMsg.append("_______________________________________________\n...");
		try {
			String data = byteArrayToString(status.getJawrConfig().getResourceCharset(),lastData).toString();
			errorMsg.append(data).append("\n\n");
		} catch (IOException e1) {
			// Ignored, we have enaugh problems by this point. 
		}
		errorMsg.append("_______________________________________________");
		errorMsg.append("\nIf you can't find the error, try to check the scripts using JSLint (http://www.jslint.com/) to find the conflicting part of the code. ");
		
		throw new BundlingProcessException(errorMsg.toString(),e);
	}

}
