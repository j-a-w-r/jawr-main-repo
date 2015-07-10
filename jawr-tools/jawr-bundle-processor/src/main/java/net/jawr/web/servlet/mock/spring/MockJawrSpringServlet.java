/**
 * Copyright 2010-2013  Ibrahim Chaehoi
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
package net.jawr.web.servlet.mock.spring;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jawr.web.servlet.JawrSpringController;

/**
 * The servlet which will handle the requests and pass them to the
 * JawrSpringController.
 * 
 * @author Ibrahim Chaehoi
 * 
 */
public class MockJawrSpringServlet extends HttpServlet {

	/** The serial version UID */
	private static final long serialVersionUID = 1L;

	/** The logger */
	private static Logger logger = LoggerFactory
			.getLogger(MockJawrSpringServlet.class);

	/** The Jawr spring controller */
	private final JawrSpringController jawrController;

	/**
	 * Constructor
	 * 
	 * @param jawrController
	 *            the jawr controller
	 * @throws ServletException
	 *             if a servlet exception occurs
	 */
	public MockJawrSpringServlet(JawrSpringController jawrController,
			ServletConfig servletConfig) throws ServletException {

		this.jawrController = jawrController;
		init(servletConfig);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest
	 * , javax.servlet.http.HttpServletResponse)
	 */
	protected void service(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		try {
			jawrController.handleRequest(request, response);
		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.debug("Error in JawrSpringController", e);
			}
			throw new ServletException(e);
		}
	}
}
