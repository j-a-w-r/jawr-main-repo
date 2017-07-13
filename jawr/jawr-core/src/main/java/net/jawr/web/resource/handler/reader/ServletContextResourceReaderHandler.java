/**
 * Copyright 2009-2017 Ibrahim Chaehoi
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
package net.jawr.web.resource.handler.reader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.context.ThreadLocalJawrContext;
import net.jawr.web.exception.InterruptBundlingProcessException;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.FileNameUtils;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.factory.util.ClassLoaderResourceUtils;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.generator.ResourceGenerator;
import net.jawr.web.servlet.util.MIMETypesSupport;
import net.jawr.web.util.StringUtils;

/**
 * This class defines the manager for resource reader.
 * 
 * @author Ibrahim Chaehoi
 */
public class ServletContextResourceReaderHandler implements ResourceReaderHandler {

	/** The logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(ServletContextResourceReaderHandler.class);

	/** The Jawr configuration */
	private final JawrConfig config;

	/** The servlet context */
	private ServletContext servletContext;

	/** The working directory */
	private String workingDirectory;

	/** The generator registry */
	private GeneratorRegistry generatorRegistry;

	/** The list of resource readers */
	private final List<TextResourceReader> resourceReaders = new ArrayList<>();

	/** The list of stream resource readers */
	private final List<StreamResourceReader> streamResourceReaders = new ArrayList<>();

	/** The list of resource generator info providers */
	private final List<ResourceBrowser> resourceGeneratorInfoProviders = new ArrayList<>();

	/** The list of resource info providers */
	private final List<ResourceBrowser> resourceInfoProviders = new ArrayList<>();

	/** The allowed file extension */
	private final List<String> allowedExtensions = new ArrayList<>();

	/**
	 * Constructor
	 * 
	 * @param servletContext
	 *            the servlet context
	 * @param jawrConfig
	 *            the Jawr config
	 * @param generatorRegistry
	 *            the generator registry
	 * @throws IOException
	 *             if an IOException occurs.
	 */
	public ServletContextResourceReaderHandler(ServletContext servletContext, JawrConfig jawrConfig,
			GeneratorRegistry generatorRegistry) throws IOException {

		String tempWorkingDirectory = ((File) servletContext.getAttribute(JawrConstant.SERVLET_CONTEXT_TEMPDIR))
				.getCanonicalPath();

		this.config = jawrConfig;
		if (config.getUseBundleMapping() && StringUtils.isNotEmpty(config.getJawrWorkingDirectory())) {
			tempWorkingDirectory = config.getJawrWorkingDirectory();
		}

		if (tempWorkingDirectory == null) {
			throw new IllegalStateException("There is no temporary directory configured for this web application.\n"
					+ "The servlet context attribute '" + JawrConstant.SERVLET_CONTEXT_TEMPDIR
					+ "' should contain the temporary directory attribute.");
		}
		this.servletContext = servletContext;
		this.generatorRegistry = generatorRegistry;
		this.generatorRegistry.setResourceReaderHandler(this);
		if (tempWorkingDirectory.startsWith(JawrConstant.FILE_URI_PREFIX)) {
			tempWorkingDirectory = tempWorkingDirectory.substring(JawrConstant.FILE_URI_PREFIX.length());
		}

		this.workingDirectory = tempWorkingDirectory + File.separator + JawrConstant.JAWR_WRK_DIR;

		// add the default extension
		allowedExtensions.addAll(JawrConstant.DEFAULT_RESOURCE_EXTENSIONS);

		if (JawrConstant.BINARY_TYPE.equals(config.getResourceType())) {
			for (Object key : MIMETypesSupport.getSupportedProperties(JawrConfig.class).keySet()) {
				if (!this.allowedExtensions.contains((String) key)) {
					this.allowedExtensions.add((String) key);
				}
			}
		} else {
			allowedExtensions.add(config.getResourceType());
		}

		ServletContextResourceReader rd = (ServletContextResourceReader) ClassLoaderResourceUtils
				.buildObjectInstance(config.getServletContextResourceReaderClass());
		rd.init(servletContext, jawrConfig);
		addResourceReader(rd);

		// Add FileSystemResourceReader if needed
		String baseContextDir = config.getProperty(JawrConstant.JAWR_BASECONTEXT_DIRECTORY);
		if (StringUtils.isNotEmpty(baseContextDir)) {
			ResourceReader fileRd = new FileSystemResourceReader(jawrConfig);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("The base directory context is set to " + baseContextDir);
			}

			addResourceReader(fileRd);
			boolean baseContextDirHighPriority = Boolean
					.valueOf(config.getProperty(JawrConstant.JAWR_BASECONTEXT_DIRECTORY_HIGH_PRIORITY));
			if (LOGGER.isDebugEnabled()) {
				if (baseContextDirHighPriority) {
					LOGGER.debug(
							"Jawr will search in priority in the base directory context before searching in the war content.");
				} else {
					LOGGER.debug(
							"Jawr will search in priority in the war content before searching in the base directory context.");
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.handler.ResourceReaderHandler#getWorkingDirectory()
	 */
	@Override
	public String getWorkingDirectory() {
		return this.workingDirectory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.handler.WorkingDirectoryLocationAware#
	 * setWorkingDirectory(java.lang.String)
	 */
	@Override
	public void setWorkingDirectory(String workingDir) {
		this.workingDirectory = workingDir;
	}

	/**
	 * Initialize the reader
	 * 
	 * @param obj
	 *            the reader to initialize
	 */
	private void initReader(Object obj) {
		if (obj instanceof WorkingDirectoryLocationAware) {
			((WorkingDirectoryLocationAware) obj).setWorkingDirectory(workingDirectory);
		}
		if (obj instanceof ServletContextAware) {
			((ServletContextAware) obj).setServletContext(servletContext);
		}

		if (obj instanceof ResourceBrowser) {
			if(obj instanceof ResourceGenerator){
				resourceGeneratorInfoProviders.add(0, (ResourceBrowser) obj);
			}else{
				resourceInfoProviders.add(0, (ResourceBrowser) obj);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.handler.reader.ResourceReaderHandler#
	 * addResourceReader(net.jawr.web.resource.handler.reader. ResourceReader)
	 */
	@Override
	public void addResourceReader(ResourceReader rd) {

		if (rd instanceof TextResourceReader) {
			resourceReaders.add((TextResourceReader) rd);
			Collections.sort(resourceReaders, new ResourceReaderComparator(config));
		}

		if (rd instanceof StreamResourceReader) {
			streamResourceReaders.add((StreamResourceReader) rd);
			Collections.sort(streamResourceReaders, new ResourceReaderComparator(config));
		}

		initReader(rd);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.handler.reader.ResourceReaderHandler#getResource(
	 * java.lang.String)
	 */
	@Override
	public Reader getResource(String resourceName) throws ResourceNotFoundException {

		return getResource(null, resourceName, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.handler.reader.ResourceReaderHandler#getResource(
	 * net.jawr.web.resource.bundle.JoinableResourceBundle, java.lang.String)
	 */
	@Override
	public Reader getResource(JoinableResourceBundle bundle, String resourceName) throws ResourceNotFoundException {

		return getResource(bundle, resourceName, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.handler.reader.ResourceReaderHandler#getResource(
	 * net.jawr.web.resource.bundle.JoinableResourceBundle, java.lang.String,
	 * boolean)
	 */
	@Override
	public Reader getResource(JoinableResourceBundle bundle, String resourceName, boolean processingBundle)
			throws ResourceNotFoundException {

		return getResource(bundle, resourceName, processingBundle, new ArrayList<Class<?>>());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.handler.reader.ResourceReaderHandler#getResource(
	 * net.jawr.web.resource.bundle.JoinableResourceBundle, java.lang.String,
	 * boolean, java.util.List)
	 */
	@Override
	public Reader getResource(JoinableResourceBundle bundle, String resourceName, boolean processingBundle,
			List<Class<?>> excludedReader) throws ResourceNotFoundException {

		if (ThreadLocalJawrContext.isInterruptingProcessingBundle()) {
			throw new InterruptBundlingProcessException();
		}
		Reader rd = null;

		String resourceExtension = FileNameUtils.getExtension(resourceName);
		boolean generatedPath = generatorRegistry.isPathGenerated(resourceName);
		if (generatedPath || allowedExtensions.contains(resourceExtension.toLowerCase())) {
			List<TextResourceReader> list = new ArrayList<>();
			list.addAll(resourceReaders);
			for (TextResourceReader rsReader : list) {
				if (!isInstanceOf(rsReader, excludedReader)) {
					if (!(rsReader instanceof ResourceGenerator)
							|| ((ResourceGenerator) rsReader).getResolver().matchPath(resourceName)) {
						try {
							rd = rsReader.getResource(bundle, resourceName, processingBundle);
						} catch (Exception e) {
							if (LOGGER.isDebugEnabled()) {
								LOGGER.debug("An exception occured while trying to read resource '" + resourceName
										+ "'. Continuing with other readers. Error : ", e);
							} else if (LOGGER.isInfoEnabled()) {
								LOGGER.info("An exception occured while trying to read resource '" + resourceName
										+ "'. Continuing with other readers. Error : " + e.getMessage());
							}
						}
						if (rd != null) {
							break;
						}
					}
				}
			}
		} else {
			LOGGER.warn("The resource '" + resourceName + "' will not be read as its extension is not an allowed one.");
		}

		if (rd == null) {
			throw new ResourceNotFoundException(resourceName);
		}

		return rd;
	}

	/**
	 * Checks if an object is an instance of on interface from a list of
	 * interface
	 * 
	 * @param rd
	 *            the object
	 * @param interfacesthe
	 *            list of interfaces
	 * @return true if the object is an instance of on interface from a list of
	 *         interface
	 */
	private boolean isInstanceOf(Object rd, List<Class<?>> interfaces) {

		boolean result = false;

		for (Class<?> class1 : interfaces) {
			if (class1.isInstance(rd)) {
				result = true;
				break;
			}
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.handler.reader.ResourceReaderHandler#
	 * getResourceAsStream(net.jawr.web.resource.bundle.JoinableResourceBundle,
	 * java.lang.String)
	 */
	@Override
	public InputStream getResourceAsStream(String resourceName) throws ResourceNotFoundException {

		return getResourceAsStream(resourceName, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.handler.reader.ResourceReaderHandler#
	 * getResourceAsStream(net.jawr.web.resource.bundle.JoinableResourceBundle,
	 * java.lang.String, boolean)
	 */
	@Override
	public InputStream getResourceAsStream(String resourceName, boolean processingBundle)
			throws ResourceNotFoundException {

		if (ThreadLocalJawrContext.isInterruptingProcessingBundle()) {
			throw new InterruptBundlingProcessException();
		}

		generatorRegistry.loadGeneratorIfNeeded(resourceName);
		InputStream is = null;

		String resourceExtension = FileNameUtils.getExtension(resourceName);
		boolean generatedPath = generatorRegistry.isPathGenerated(resourceName);
		if (generatedPath || allowedExtensions.contains(resourceExtension.toLowerCase())) {
			List<StreamResourceReader> list = new ArrayList<>();
			list.addAll(streamResourceReaders);
			for (StreamResourceReader rsReader : list) {
				if (!(rsReader instanceof ResourceGenerator)
						|| ((ResourceGenerator) rsReader).getResolver().matchPath(resourceName)) {
					try {
						is = rsReader.getResourceAsStream(resourceName);
					} catch (Exception e) {
						if (LOGGER.isDebugEnabled()) {
							LOGGER.debug("An exception occured while trying to read resource '" + resourceName
									+ "'. Continuing with other readers. Error : ", e);
						} else if (LOGGER.isInfoEnabled()) {
							LOGGER.info("An exception occured while trying to read resource '" + resourceName
									+ "'. Continuing with other readers. Error : " + e.getMessage());
						}
					}
					if (is != null) {
						break;
					}
				}
			}
		} else {
			LOGGER.warn("The resource '" + resourceName + "' will not be read as its extension is not an allowed one.");
		}
		if (is == null) {
			throw new ResourceNotFoundException(resourceName);
		}

		return is;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.handler.reader.ResourceBrowser#getResourceNames
	 * (java.lang.String)
	 */
	@Override
	public Set<String> getResourceNames(String dirName) {
		Set<String> resourceNames = new TreeSet<>();

		List<ResourceBrowser> list = new ArrayList<>();
		list.addAll(resourceInfoProviders);
		for (ResourceBrowser rsBrowser : list) {
			if (generatorRegistry.isPathGenerated(dirName)) {
				if (rsBrowser instanceof ResourceGenerator) {
					ResourceGenerator rsGeneratorBrowser = (ResourceGenerator) rsBrowser;
					if (rsGeneratorBrowser.getResolver().matchPath(dirName)) {
						resourceNames.addAll(rsBrowser.getResourceNames(dirName));
						break;
					}
				}
			} else {
				if (!(rsBrowser instanceof ResourceGenerator)) {
					resourceNames.addAll(rsBrowser.getResourceNames(dirName));
					break;
				}
			}
		}

		return resourceNames;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.handler.reader.ResourceBrowser#isDirectory(java
	 * .lang.String)
	 */
	@Override
	public boolean isDirectory(String resourceName) {
		boolean result = false;
		List<ResourceBrowser> browsers = new ArrayList<>();
		browsers.addAll(resourceGeneratorInfoProviders);
		if (generatorRegistry.isPathGenerated(resourceName)) {
			for (Iterator<ResourceBrowser> iterator = browsers.iterator(); iterator.hasNext() && !result;) {
				ResourceBrowser rsBrowser = iterator.next();
				if (rsBrowser instanceof ResourceGenerator) {
					ResourceGenerator rsGeneratorBrowser = (ResourceGenerator) rsBrowser;
					if (rsGeneratorBrowser.getResolver().matchPath(resourceName)) {
						result = rsBrowser.isDirectory(resourceName);
					}
				}	 				
			}
		}
		
		if(!result){
			browsers.clear();
			browsers.addAll(resourceInfoProviders);
			
			for (Iterator<ResourceBrowser> iterator = browsers.iterator(); iterator.hasNext() && !result;) {
				ResourceBrowser rsBrowser = iterator.next();
				if (generatorRegistry.isPathGenerated(resourceName)) {
					if (rsBrowser instanceof ResourceGenerator) {
						ResourceGenerator rsGeneratorBrowser = (ResourceGenerator) rsBrowser;
						if (rsGeneratorBrowser.getResolver().matchPath(resourceName)) {
							result = rsBrowser.isDirectory(resourceName);
						}
					}
				} else {
					if (!(rsBrowser instanceof ResourceGenerator)) {
						result = rsBrowser.isDirectory(resourceName);
					}
				}
			}
		}
		return result;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.handler.reader.ResourceReaderHandler#getFilePath(
	 * java.lang.String)
	 */
	@Override
	public String getFilePath(String resourcePath) {

		String filePath = null;
		
		List<ResourceBrowser> browsers = new ArrayList<>();
		
		if (generatorRegistry.isPathGenerated(resourcePath)) {
			browsers.addAll(resourceGeneratorInfoProviders);
			for (Iterator<ResourceBrowser> iterator = browsers.iterator(); iterator.hasNext() && filePath == null;) {
				ResourceBrowser rsBrowser = iterator.next();
				if (rsBrowser instanceof ResourceGenerator) {
					ResourceGenerator rsGeneratorBrowser = (ResourceGenerator) rsBrowser;
					if (rsGeneratorBrowser.getResolver().matchPath(resourcePath)) {
						filePath = rsBrowser.getFilePath(resourcePath);
					}
				}	 				
			}
		}
		
		if(filePath == null){
			browsers.clear();
			browsers.addAll(resourceInfoProviders);
			for (Iterator<ResourceBrowser> iterator = resourceInfoProviders.iterator(); iterator.hasNext() && filePath == null;) {
				ResourceBrowser rsBrowser = iterator.next();
				filePath = rsBrowser.getFilePath(resourcePath);
			}
		}
		
		return filePath;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.handler.reader.ResourceReaderHandler#
	 * getLastModified(java.lang.String)
	 */
	@Override
	public long getLastModified(String filePath) {
		long lastModified = 0;
		File f = new File(filePath);
		if (f.exists()) {
			lastModified = f.lastModified();
		}
		return lastModified;
	}
}
