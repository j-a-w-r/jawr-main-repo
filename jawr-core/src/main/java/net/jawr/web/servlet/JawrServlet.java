/**
 * Copyright 2007-2012 Jordi Hern�ndez Sell�s, Ibrahim Chaehoi
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

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.jawr.web.JawrConstant;

import org.apache.log4j.Logger;

/**
 *
 * Main Jawr servlet. Maps logical URLs to script bundles, which are generated on the fly (may 
 * be cached), and served as a single file. 
 * 
 * 
 * @author Jordi Hern�ndez Sell�s
 * @author Ibrahim Chaehoi
 */
public class JawrServlet extends HttpServlet implements ServletContextListener {
	
	/** The serial version UID */ 
	private static final long serialVersionUID = -4551240917172286444L;

	/** The logger */
	private static final Logger LOGGER = Logger.getLogger(JawrServlet.class);

	/** The request handler */
	protected JawrRequestHandler requestHandler;

	/* (non-Javadoc)
	 * @see javax.servlet.GenericServlet#init()
	 */
	public void init() throws ServletException {
		try {
			String type = getServletConfig().getInitParameter(JawrConstant.TYPE_INIT_PARAMETER);
			if(JawrConstant.IMG_TYPE.equals(type)){
				requestHandler = new JawrImageRequestHandler(getServletContext(),getServletConfig());
			}else{
				requestHandler = new JawrRequestHandler(getServletContext(),getServletConfig());
			}
			//getServletConfig().getServletContext().
		}catch (ServletException e) {
			LOGGER.fatal("Jawr servlet with name " +  getServletConfig().getServletName() +" failed to initialize properly. ");
			LOGGER.fatal("Cause:");
			LOGGER.fatal(e.getMessage(),e);
			throw e;
		}catch (RuntimeException e) {
			LOGGER.fatal("Jawr servlet with name " +  getServletConfig().getServletName() +" failed to initialize properly. ");
			LOGGER.fatal("Cause: ");
			LOGGER.fatal(e.getMessage(),e);
			throw new ServletException(e);
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		requestHandler.doGet(req, resp);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.GenericServlet#destroy()
	 */
	public void destroy() {		
		requestHandler.destroy();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		requestHandler.destroy();		
	}
     
}
