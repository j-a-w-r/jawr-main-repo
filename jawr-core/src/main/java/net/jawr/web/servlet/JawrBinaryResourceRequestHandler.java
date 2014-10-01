/**
 * Copyright 2009-2014  Ibrahim Chaehoi
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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.jawr.web.JawrConstant;
import net.jawr.web.context.ThreadLocalJawrContext;
import net.jawr.web.exception.InvalidPathException;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.BinaryResourcesHandler;
import net.jawr.web.resource.bundle.CheckSumUtils;
import net.jawr.web.resource.bundle.IOUtils;
import net.jawr.web.resource.bundle.factory.PropertiesBundleConstant;
import net.jawr.web.resource.bundle.factory.util.PathNormalizer;
import net.jawr.web.resource.bundle.factory.util.PropertiesConfigHelper;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.handler.bundle.ResourceBundleHandler;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;
import net.jawr.web.servlet.util.MIMETypesSupport;
import net.jawr.web.util.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class defines the request handler for binary web resources (images, font, ...). 
 * Jawr binary resource servlet delegates to this class to handle requests.
 * 
 * @author Ibrahim Chaehoi
 */
public class JawrBinaryResourceRequestHandler extends JawrRequestHandler {

	/** The serial version UID */
	private static final long serialVersionUID = -8342090032443416738L;

	/** The logger */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(JawrBinaryResourceRequestHandler.class);

	/** The cache buster pattern */
	private static Pattern cacheBusterPattern = Pattern.compile("("
			+ "(([a-zA-Z0-9]+)_)?" + JawrConstant.CACHE_BUSTER_PREFIX
			+ ")[a-zA-Z0-9]+(/.*)$");

	/** The index of the generated web resource prefix in the cache buster pattern */
	private static final int GENERATED_BINARY_WEB_RESOURCE_PREFIX_INDEX = 3;

	/** The cache buster replace pattern for standard web resource */
	private static final String CACHE_BUSTER_STANDARD_BINARY_WEB_RESOURCE_REPLACE_PATTERN = "$4";

	/** The cache buster replace pattern for generated web resource */
	private static final String CACHE_BUSTER_GENERATED_BINARY_WEB_RESOURCE_REPLACE_PATTERN = "$3:$4";

	/** The resource handler */
	private ResourceReaderHandler rsReaderHandler;

	/** The resource handler */
	private ResourceBundleHandler rsBundleHandler;

	/** The binary web resource handler */
	private BinaryResourcesHandler binaryRsHandler;

	/** The bundle mapping */
	private Properties bundleMapping;

	/** The binary resource MIME map, associating the resource extension to their MIME type */
	protected Map<Object, Object> binaryMimeTypeMap;

	/**
	 * Reads the properties file and initializes all configuration using the
	 * ServletConfig object. If applicable, a ConfigChangeListenerThread will be
	 * started to listen to changes in the properties configuration.
	 * 
	 * @param servletContext
	 *            ServletContext
	 * @param servletConfig
	 *            ServletConfig
	 * @throws ServletException
	 */
	public JawrBinaryResourceRequestHandler(ServletContext context, ServletConfig config)
			throws ServletException {
		super(context, config);
		this.binaryMimeTypeMap = MIMETypesSupport.getSupportedProperties(this);
		resourceType = JawrConstant.BINARY_TYPE;
	}

	/**
	 * Alternate constructor that does not need a ServletConfig object.
	 * Parameters normally read rom it are read from the initParams Map, and the
	 * configProps are used instead of reading a .properties file.
	 * 
	 * @param servletContext
	 *            ServletContext
	 * @param servletConfig
	 *            ServletConfig
	 * @throws ServletException
	 */
	public JawrBinaryResourceRequestHandler(ServletContext context,
			Map<String, Object> initParams, Properties configProps)
			throws ServletException {

		super(context, initParams, configProps);
		this.binaryMimeTypeMap = MIMETypesSupport.getSupportedProperties(this);
		resourceType = JawrConstant.BINARY_TYPE;
	}

	/**
	 * Initialize the Jawr config
	 * 
	 * @param props
	 *            the properties
	 * @throws ServletException
	 *             if an exception occurs
	 */
	protected void initializeJawrConfig(Properties props)
			throws ServletException {

		if(this.binaryMimeTypeMap == null){
			this.binaryMimeTypeMap = MIMETypesSupport.getSupportedProperties(this);
		}
		
		// init registry
		generatorRegistry = new GeneratorRegistry(resourceType);

		// Initialize config
		if (null != jawrConfig){
			jawrConfig.invalidate();
		}
		
		jawrConfig = createJawrConfig(props);

		jawrConfig.setContext(servletContext);
		jawrConfig.setGeneratorRegistry(generatorRegistry);

		// Set mapping, to be used by the tag lib to define URLs that point to
		// this servlet.
		String mapping = (String) initParameters
				.get(JawrConstant.SERVLET_MAPPING_PROPERTY_NAME);
		if (null != mapping) {
			jawrConfig.setServletMapping(mapping);
		}

		// Initialize the IllegalBundleRequest handler
		initIllegalBundleRequestHandler();

		// Initialize the resource handler
		rsReaderHandler = initResourceReaderHandler();
		rsBundleHandler = initResourceBundleHandler();

		// Initialize custom generators
		PropertiesConfigHelper propertiesHelper = new PropertiesConfigHelper(
				props, resourceType);
		Iterator<String> generators = propertiesHelper.getCommonPropertyAsSet(
				PropertiesBundleConstant.CUSTOM_GENERATORS).iterator();
		while (generators.hasNext()) {
			String generatorClass = (String) generators.next();
			generatorRegistry.registerGenerator(generatorClass);
		}

		if (jawrConfig.getUseBundleMapping()) {
			bundleMapping = rsBundleHandler.getJawrBundleMapping();
		} else {
			bundleMapping = new Properties();
		}

		binaryRsHandler = new BinaryResourcesHandler(jawrConfig, rsReaderHandler,
				rsBundleHandler);
		initMapping(binaryRsHandler);

		servletContext.setAttribute(JawrConstant.BINARY_CONTEXT_ATTRIBUTE,
				binaryRsHandler);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Configuration read. Current config:");
			LOGGER.debug(jawrConfig.toString());
		}

		// Warn when in debug mode
		if (jawrConfig.isDebugModeOn()) {
			LOGGER.warn("Jawr initialized in DEVELOPMENT MODE. Do NOT use this mode in production or integration servers. ");
		}
	}

	/**
	 * Initialize the mapping of the binary web resources handler
	 * 
	 * @param binaryRsHandler
	 *            the binary web resources handler
	 */
	private void initMapping(BinaryResourcesHandler binaryRsHandler) {

		if (jawrConfig.getUseBundleMapping()
				&& rsBundleHandler.isExistingMappingFile()) {

			// Initialize the binary web resource mapping
			Iterator<Entry<Object, Object>> mapIterator = bundleMapping
					.entrySet().iterator();
			while (mapIterator.hasNext()) {
				Entry<Object, Object> entry = mapIterator.next();
				binaryRsHandler.addMapping((String) entry.getKey(), entry
						.getValue().toString());
			}

		} else {
			// Create a resource handler to read files from the WAR archive or
			// exploded dir.
			
			String binaryResourcesDefinition = jawrConfig
					.getBinaryResourcesDefinition();
			if (binaryResourcesDefinition != null) {

				StringTokenizer tokenizer = new StringTokenizer(
						binaryResourcesDefinition, ",");
				while (tokenizer.hasMoreTokens()) {
					String pathMapping = tokenizer.nextToken();

					// path is a generated image and ends with an image
					// extension
					if (generatorRegistry.isGeneratedBinaryResource(pathMapping)
							&& hasBinaryFileExtension(pathMapping)) {

						addBinaryResourcePath(binaryRsHandler, pathMapping);
					}
					// path ends in /, the folder is included without subfolders
					else if (pathMapping.endsWith("/")) {
						addItemsFromDir(binaryRsHandler, pathMapping, false);
					}
					// path ends in /, the folder is included with all
					// subfolders
					else if (pathMapping.endsWith("/**")) {
						addItemsFromDir(
								binaryRsHandler,
								pathMapping.substring(0,
										pathMapping.lastIndexOf("**")), true);
					} else if (hasBinaryFileExtension(pathMapping)) {
						addBinaryResourcePath(binaryRsHandler, pathMapping);
					} else
						LOGGER.warn("Wrong mapping ["
								+ pathMapping
								+ "] for image bundle. Please check configuration. ");
				}
			}
		}

		// Store the bundle mapping
		if (jawrConfig.getUseBundleMapping()
				&& !rsBundleHandler.isExistingMappingFile()) {
			rsBundleHandler.storeJawrBundleMapping(bundleMapping);
		}

		if (LOGGER.isDebugEnabled())
			LOGGER.debug("Finish creation of map for image bundle");
	}

	/**
	 * Add an binary resource path to the binary map
	 * 
	 * @param binRsHandler
	 *            the image resources handler
	 * @param resourcePath
	 *            the image path
	 */
	private void addBinaryResourcePath(BinaryResourcesHandler binRsHandler, String resourcePath) {

		try {
			String resultPath = CheckSumUtils.getCacheBustedUrl(resourcePath,
					rsReaderHandler, jawrConfig);
			binRsHandler.addMapping(resourcePath, resultPath);
			bundleMapping.put(resourcePath, resultPath);
		} catch (IOException e) {
			LOGGER.error(
					"An exception occurs while defining the mapping for the file : "
							+ resourcePath, e);
		} catch (ResourceNotFoundException e) {
			LOGGER.error("Impossible to define the checksum for the resource '"
					+ resourcePath
					+ "'. Unable to retrieve the content of the file.");
		}
	}

	/**
	 * Returns true of the path contains a binary file extension
	 * 
	 * @param path
	 *            the path
	 * @return the binary file extension
	 */
	private boolean hasBinaryFileExtension(String path) {
		boolean result = false;

		int extFileIdx = path.lastIndexOf(".");
		if (extFileIdx != -1 && extFileIdx + 1 < path.length()) {
			String extension = path.substring(extFileIdx + 1);
			result = binaryMimeTypeMap.containsKey(extension);
		}

		return result;
	}

	/**
	 * Adds all the resources within a path to the image map.
	 * 
	 * @param binRsHandler
	 *            the binary resources handler
	 * @param dirName
	 *            the directory name
	 * @param addSubDirs
	 *            boolean If subfolders will be included. In such case, every
	 *            folder below the path is included.
	 */
	private void addItemsFromDir(BinaryResourcesHandler binRsHandler,
			String dirName, boolean addSubDirs) {
		Set<String> resources = rsReaderHandler.getResourceNames(dirName);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Adding " + resources.size()
					+ " resources from path [" + dirName + "] to binary bundle");
		}

		GeneratorRegistry generatorRegistry = binRsHandler.getConfig()
				.getGeneratorRegistry();

		List<String> folders = new ArrayList<String>();
		boolean generatedPath = generatorRegistry.isPathGenerated(dirName);
		for (Iterator<String> it = resources.iterator(); it.hasNext();) {
			String resourceName = it.next();
			String resourcePath = PathNormalizer.joinPaths(dirName,
					resourceName, generatedPath);
			if (hasBinaryFileExtension(resourceName)) {
				addBinaryResourcePath(binRsHandler, resourcePath);

				if (LOGGER.isDebugEnabled())
					LOGGER.debug("Added to item path list:"
							+ PathNormalizer.asPath(resourcePath));
			} else if (addSubDirs) {

				try {
					if (rsReaderHandler.isDirectory(resourcePath)) {
						folders.add(resourceName);
					}
				} catch (InvalidPathException e) {
					if (LOGGER.isDebugEnabled())
						LOGGER.debug("Enable to define if the following resource is a directory : "
								+ PathNormalizer.asPath(resourcePath));
				}
			}
		}

		// Add subfolders if requested. Subfolders are added last unless
		// specified in sorting file.
		if (addSubDirs) {
			for (Iterator<String> it = folders.iterator(); it.hasNext();) {
				String folderName = it.next();
				addItemsFromDir(binRsHandler,
						PathNormalizer.joinPaths(dirName, folderName), true);
			}
		}
	}

	/**
	 * Handles a resource request.
	 * <ul>
	 * <li>If the request contains an If-Modified-Since header, the 304 status
	 * is set and no data is written to the response</li>
	 * <li>If the requested path begins with the gzip prefix, a gzipped version
	 * of the resource is served, with the corresponding content-encoding
	 * header.</li>
	 * <li>Otherwise, the resource is written as text to the response.</li>
	 * <li>If the resource is not found, the response satus is set to 404 and no
	 * response is written.</li>
	 * </ul>
	 * 
	 * @param requestedPath
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	public void processRequest(String requestedPath,
			HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		try {
			// manual reload request
			if (this.jawrConfig.getRefreshKey().length() > 0
					&& null != request.getParameter("refreshKey")
					&& this.jawrConfig.getRefreshKey().equals(
							request.getParameter("refreshKey"))) {
				this.configChanged(propertiesSource.getConfigProperties());
			}

			if (LOGGER.isDebugEnabled())
				LOGGER.debug("Request received for path:" + requestedPath);

			if (handleSpecificRequest(requestedPath, requestedPath, request,
					response)) {
				return;
			}

			// Handle the strict mode
			boolean validBundle = isValidBundle(requestedPath);

			// Set the content type
			response.setContentType(getContentType(requestedPath, request));

			boolean responseHeaderWritten = false;

			// If debug mode is off, check for If-Modified-Since and
			// If-none-match headers and set response caching headers.
			if (!this.jawrConfig.isDebugModeOn()) {
				// If a browser checks for changes, always respond 'no changes'.
				if (validBundle
						&& (null != request.getHeader(IF_MODIFIED_SINCE_HEADER) || null != request
								.getHeader(IF_NONE_MATCH_HEADER))) {
					response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
					if (LOGGER.isDebugEnabled())
						LOGGER.debug("Returning 'not modified' header. ");
					return;
				}

				if (validBundle) {
					// Add caching headers
					setResponseHeaders(response);
				} else {

					responseHeaderWritten = illegalBundleRequestHandler
							.writeResponseHeader(requestedPath, request,
									response);
					if (!responseHeaderWritten) {
						// Add caching headers
						setResponseHeaders(response);
					}
				}
			}

			// Returns the real file path
			String filePath = getRealFilePath(requestedPath);

			try {
				if (isValidRequestedPath(filePath)
						&& (validBundle || illegalBundleRequestHandler
								.canWriteContent(requestedPath, request))) {
					writeContent(response, filePath);
				} else {
					if (!responseHeaderWritten) {
						LOGGER.error("Unable to load the image for the request URI : "
								+ request.getRequestURI());
						response.sendError(HttpServletResponse.SC_NOT_FOUND);
					}
				}
			} catch (Exception ex) {

				LOGGER.error("Unable to load the image for the request URI : "
						+ request.getRequestURI(), ex);
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
			}

			if (LOGGER.isDebugEnabled())
				LOGGER.debug("request succesfully attended");

		} finally {

			// Reset the Thread local for the Jawr context
			ThreadLocalJawrContext.reset();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.servlet.JawrRequestHandler#isValidBundle(java.lang.String)
	 */
	protected boolean isValidBundle(String requestedPath) {
		boolean validBundle = true;
		if (!jawrConfig.isDebugModeOn() && jawrConfig.isStrictMode()) {
			validBundle = binaryRsHandler
					.containsValidBundleHashcode(requestedPath);
		}
		return validBundle;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.servlet.JawrRequestHandler#handleSpecificRequest(java.lang
	 * .String, javax.servlet.http.HttpServletRequest,
	 * javax.servlet.http.HttpServletResponse)
	 */
	protected boolean handleSpecificRequest(String requestedPath,
			String contentType, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		boolean processed = false;
		// Retrieve the file path
		// String filePath = getFilePath(request);
		if (requestedPath == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			processed = true;
		} else {
			// Ckeck the content type
			if (contentType == null) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				processed = true;
			}
		}

		return processed;
	}

	/**
	 * Returns the content type for the image
	 * 
	 * @param request
	 *            the request
	 * @param filePath
	 *            the image file path
	 * @return the content type of the image
	 */
	protected String getContentType(String filePath, HttpServletRequest request) {
		String requestUri = request.getRequestURI();

		// Retrieve the extension
		String extension = getExtension(filePath);
		if (extension == null) {

			LOGGER.error("No extension found for the request URI : "
					+ requestUri);
			return null;
		}

		String contentType = (String) binaryMimeTypeMap.get(extension);
		if (contentType == null) {

			LOGGER.error("No image extension match the extension '" + extension
					+ "' for the request URI : " + requestUri);
			return null;
		}
		return contentType;
	}

	/**
	 * Checks if the path is valid and can be accessed.
	 * 
	 * @param requestedPath
	 *            the requested path
	 * @return true if the path is valid and can be accessed.
	 */
	protected boolean isValidRequestedPath(String requestedPath) {

		boolean result = true;
		if (requestedPath.startsWith(JawrConstant.WEB_INF_DIR_PREFIX)
				|| requestedPath.startsWith(JawrConstant.META_INF_DIR_PREFIX)) {
			result = false;
		}

		return result;
	}

	/**
	 * Write the image content to the response
	 * 
	 * @param response
	 *            the response
	 * @param fileName
	 *            the filename
	 * @throws IOException
	 *             if an IO exception occurs.
	 */
	private void writeContent(HttpServletResponse response, String fileName)
			throws IOException {

		OutputStream os = response.getOutputStream();
		InputStream is = null;

		String resourceName = fileName;
		if (!jawrConfig.getGeneratorRegistry().isGeneratedBinaryResource(resourceName)
				&& !resourceName.startsWith("/")) {
			resourceName = "/" + resourceName;
		}

		try {
			is = rsReaderHandler.getResourceAsStream(resourceName);
		} catch (ResourceNotFoundException e) {
			// Nothing to do here
		}

		if (is == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			if (LOGGER.isInfoEnabled())
				LOGGER.info("Received a request for a non existing binary resource: "
						+ resourceName);
			return;
		}

		try {
			IOUtils.copy(is, os);
		} catch (EOFException eofex) {
			LOGGER.debug("Browser cut off response", eofex);
		} finally {
			IOUtils.close(is);
		}
	}

	/**
	 * Removes the cache buster
	 * 
	 * @param fileName
	 *            the file name
	 * @return the file name without the cache buster.
	 */
	private String getRealFilePath(String fileName) {

		String realFilePath = fileName;
		if (realFilePath.startsWith("/")) {
			realFilePath = realFilePath.substring(1);
		}

		Matcher matcher = cacheBusterPattern.matcher(realFilePath);
		StringBuffer result = new StringBuffer();
		if (matcher.find()) {
			matcher.appendReplacement(
					result,
					StringUtils.isEmpty(matcher
							.group(GENERATED_BINARY_WEB_RESOURCE_PREFIX_INDEX)) ? CACHE_BUSTER_STANDARD_BINARY_WEB_RESOURCE_REPLACE_PATTERN
							: CACHE_BUSTER_GENERATED_BINARY_WEB_RESOURCE_REPLACE_PATTERN);
			return result.toString();
		}

		return realFilePath;
	}

}
