/**
 * Copyright 2010-2013 Ibrahim Chaehoi
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
package net.jawr.web.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class defines the default Illegal bundle request handler, which is used
 * in strict mode and force Jawr to return a 404 to the client
 * 
 * @author Ibrahim Chaehoi
 * 
 */
public class IllegalBundleRequestHandlerImpl implements
		IllegalBundleRequestHandler {

	/** The logger */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(IllegalBundleRequestHandlerImpl.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.servlet.IllegalBundleRequestHandler#writeResponseHeader(
	 * java.lang.String, javax.servlet.http.HttpServletRequest,
	 * javax.servlet.http.HttpServletResponse)
	 */
	public boolean writeResponseHeader(String requestedPath,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		LOGGER.debug("Illegal access to bundle : " + requestedPath
				+ ". The hashcode don't match the existing one.");
		response.sendError(HttpServletResponse.SC_NOT_FOUND);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.servlet.IllegalBundleRequestHandler#canWriteContent(java
	 * .lang.String, javax.servlet.http.HttpServletRequest)
	 */
	public boolean canWriteContent(String requestedPath,
			HttpServletRequest request) {
		return false;
	}

}
