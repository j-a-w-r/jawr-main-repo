/**
 * Copyright 2007-2015 Jordi Hernández Sellés, Ibrahim Chaehoi
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
package net.jawr.web.resource.handler.bundle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.zip.GZIPOutputStream;

import net.jawr.web.JawrConstant;
import net.jawr.web.exception.BundlingProcessException;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.bundle.IOUtils;
import net.jawr.web.resource.bundle.JoinableResourceBundleContent;
import net.jawr.web.resource.bundle.factory.util.PathNormalizer;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.util.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class defines the abstract class for the resource bundle handler
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 */
public abstract class AbstractResourceBundleHandler implements
		ResourceBundleHandler {

	/** The logger */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(AbstractResourceBundleHandler.class);

	/** The name of the Jawr temp directory */
	protected static final String TEMP_SUBDIR = "jawrTmp";

	/** The name of the directory which contain the bundles in text format */
	protected static final String TEMP_TEXT_SUBDIR = "text";

	/** The name of the directory which contain the bundles in gzip format */
	protected static final String TEMP_GZIP_SUBDIR = "gzip";

	/**
	 * The name of the directory which contain the CSS defined in classpath for
	 * the DEBUG mode
	 */
	protected static final String TEMP_CSS_CLASSPATH_SUBDIR = "cssClasspath";

	/** The path of the temporary working directory */
	protected String tempDirPath;

	/** The path of the directory which contain the bundles in text format */
	protected String textDirPath;

	/** The path of the directory which contain the bundles in gzip format */
	protected String gzipDirPath;

	/**
	 * The path of the directory which contain the CSS defined in classpath for
	 * the DEBUG mode
	 */
	protected String cssClasspathDirPath;

	/** The charset to use for the files */
	protected Charset charset;

	/** The mapping file name */
	protected String mappingFileName;

	/** The resource type */
	private String resourceType;

	/**
	 * The flag indicating if the temp directory is a file system directory or
	 * if it's embedded in the web application itself
	 */
	private boolean useFileSystemTempDir = true;

	/**
	 * Build a resource handler based on the specified temporary files root path
	 * and charset.
	 * 
	 * @param tempDirRoot
	 *            Root dir for storing bundle files.
	 * @param charset
	 *            Charset to read/write characters.
	 * @param generatorRegistry
	 *            the generator registry
	 * @param resourceType
	 *            the resource type
	 */
	protected AbstractResourceBundleHandler(File tempDirRoot, Charset charset,
			GeneratorRegistry generatorRegistry, String resourceType) {

		this(tempDirRoot, charset, resourceType, true);
	}

	/**
	 * Build a resource handler based on the specified temporary files root path
	 * and charset.
	 * 
	 * @param tempDirRoot
	 *            Root dir for storing bundle files.
	 * @param charset
	 *            Charset to read/write characters.
	 * @param resourceType
	 *            the resource type
	 * @param createTempSubDir
	 *            the flag indicating if we should use the jawrTemp sub
	 *            directory
	 */
	protected AbstractResourceBundleHandler(String tempDirRoot,
			Charset charset, final String resourceType,
			final boolean createTempSubDir) {
		super();

		this.resourceType = resourceType;
		this.charset = charset;

		if (StringUtils.isEmpty(resourceType)
				|| resourceType.equals(JawrConstant.JS_TYPE)) {
			mappingFileName = JawrConstant.JAWR_JS_MAPPING_PROPERTIES_FILENAME;
		} else if (resourceType.equals(JawrConstant.CSS_TYPE)) {
			mappingFileName = JawrConstant.JAWR_CSS_MAPPING_PROPERTIES_FILENAME;
		} else if (resourceType.equals(JawrConstant.BINARY_TYPE)) {
			mappingFileName = JawrConstant.JAWR_BINARY_MAPPING_PROPERTIES_FILENAME;
		}

		if (tempDirRoot.startsWith(JawrConstant.FILE_URI_PREFIX)) {
			tempDirRoot = tempDirRoot.substring(JawrConstant.FILE_URI_PREFIX
					.length());
		} else {
			useFileSystemTempDir = false;
		}

		initTempDirectory(tempDirRoot, createTempSubDir);
	}

	/**
	 * Build a resource handler based on the specified temporary files root path
	 * and charset.
	 * 
	 * @param tempDirRoot
	 *            Root dir for storing bundle files.
	 * @param charset
	 *            Charset to read/write characters.
	 * @param resourceType
	 *            the resource type
	 * @param createTempSubDir
	 *            the flag indicating if we should use the jawrTemp sub
	 *            directory
	 */
	protected AbstractResourceBundleHandler(File tempDirRoot, Charset charset,
			String resourceType, boolean createTempSubDir) {
		super();
		this.resourceType = resourceType;
		this.charset = charset;

		if (StringUtils.isEmpty(resourceType)
				|| resourceType.equals(JawrConstant.JS_TYPE)) {
			mappingFileName = JawrConstant.JAWR_JS_MAPPING_PROPERTIES_FILENAME;
		} else if (resourceType.equals(JawrConstant.CSS_TYPE)) {
			mappingFileName = JawrConstant.JAWR_CSS_MAPPING_PROPERTIES_FILENAME;
		} else if (resourceType.equals(JawrConstant.BINARY_TYPE)) {
			mappingFileName = JawrConstant.JAWR_BINARY_MAPPING_PROPERTIES_FILENAME;
		}

		try {
			initTempDirectory(tempDirRoot.getCanonicalPath(), createTempSubDir);

		} catch (IOException e) {
			throw new BundlingProcessException(
					"Unexpected IOException creating temporary jawr directory",
					e);
		}

	}

	/**
	 * Initialize the temporary directories
	 * 
	 * @param tempDirRoot
	 *            the temporary directory root
	 * @param createTempSubDir
	 *            the flag indicating if we should create the temporary
	 *            directory.
	 */
	private void initTempDirectory(String tempDirRoot, boolean createTempSubDir) {

		tempDirPath = tempDirRoot;

		if (createTempSubDir) {
			tempDirPath = tempDirPath + File.separator + TEMP_SUBDIR;
		}

		// In windows, pathnames with spaces are returned as %20
		if (tempDirPath.indexOf("%20") != -1)
			tempDirPath = tempDirPath.replaceAll("%20", " ");

		this.textDirPath = tempDirPath + File.separator + TEMP_TEXT_SUBDIR;
		this.gzipDirPath = tempDirPath + File.separator + TEMP_GZIP_SUBDIR;
		this.cssClasspathDirPath = tempDirPath + File.separator
				+ TEMP_CSS_CLASSPATH_SUBDIR;

		if (createTempSubDir) {
			try {
				createDir(tempDirPath);
				createDir(textDirPath);
				createDir(gzipDirPath);
				createDir(cssClasspathDirPath);
			} catch (IOException e) {
				throw new BundlingProcessException(
						"Unexpected IOException creating temporary jawr directory",
						e);
			}
		}
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.handler.bundle.ResourceBundleHandler#getBundleTextDirPath()
	 */
	@Override
	public String getBundleTextDirPath() {
		return textDirPath;
	}


	/* (non-Javadoc)
	 * @see net.jawr.web.resource.handler.bundle.ResourceBundleHandler#getBundleZipDirPath()
	 */
	@Override
	public String getBundleZipDirPath() {
		return gzipDirPath;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.ResourceHandler#getResourceType()
	 */
	public String getResourceType() {

		return resourceType;
	}

	/**
	 * Checks if the mapping file exists in the working directory
	 * 
	 * @return true if the mapping file exists in the working directory
	 */
	public boolean isExistingMappingFile() {

		return getBundleMappingStream() != null;
	}

	/**
	 * Returns the bundle mapping
	 * 
	 * @return the bundle mapping
	 */
	public Properties getJawrBundleMapping() {

		final Properties bundleMapping = new Properties();
		InputStream is = null;
		try {
			is = getBundleMappingStream();
			if (is != null) {
				((Properties) bundleMapping).load(is);
			} else {
				LOGGER.info("The jawr bundle mapping '" + mappingFileName
						+ "' is not found");
			}
		} catch (IOException e) {
			LOGGER.info("Error while loading the jawr bundle mapping '"
					+ JawrConstant.JAWR_JS_MAPPING_PROPERTIES_FILENAME + "'");
		} finally {
			IOUtils.close(is);
		}

		return bundleMapping;
	}

	/**
	 * Returns the bundle mapping file
	 * 
	 * @return the bundle mapping file
	 */
	private InputStream getBundleMappingStream() {

		InputStream is = null;
		try {
			is = getTemporaryResourceAsStream(PathNormalizer.concatWebPath(
					tempDirPath + "/", mappingFileName));
		} catch (ResourceNotFoundException e) {
			// Nothing to do
		}
		return is;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.ResourceHandler#getTemporaryResourceAsStream(java
	 * .lang.String)
	 */
	public InputStream getTemporaryResourceAsStream(String resourceName)
			throws ResourceNotFoundException {
		InputStream is = null;
		if (useFileSystemTempDir) {
			File file = new File(resourceName);
			try {
				is = new FileInputStream(file);
			} catch (FileNotFoundException e) {
				throw new ResourceNotFoundException(resourceName);
			}
		} else {
			is = doGetResourceAsStream(resourceName);
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
	 * net.jawr.web.resource.ResourceHandler#getResourceAsStream(java.lang.String
	 * )
	 */
	protected abstract InputStream doGetResourceAsStream(String resourceName);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.ResourceHandler#storeJawrBundleMapping(java.util
	 * .Properties)
	 */
	public void storeJawrBundleMapping(Properties bundleMapping) {

		File bundleMappingFile = new File(tempDirPath, mappingFileName);
		OutputStream out = null;
		try {
			out = new FileOutputStream(bundleMappingFile);
			bundleMapping.store(out, "Jawr mapping");
		} catch (IOException e) {
			LOGGER.error("Unable to store the bundle mapping");
		} finally {
			IOUtils.close(out);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.ResourceHandler#getResourceBundleBytes(java
	 * .lang.String)
	 */
	public ReadableByteChannel getResourceBundleChannel(String bundleName)
			throws ResourceNotFoundException {

		return getResourceBundleChannel(bundleName, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.ResourceHandler#getResourcebundleReader(
	 * java.lang.String)
	 */
	public Reader getResourceBundleReader(String bundleName)
			throws ResourceNotFoundException {

		ReadableByteChannel inchannel = getResourceBundleChannel(bundleName,
				false);
		return Channels.newReader(inchannel, charset.newDecoder(), -1);
	}

	/**
	 * Returns the readable byte channel from the bundle name
	 * 
	 * @param bundleName
	 *            the bundle name
	 * @param gzipBundle
	 *            the flag indicating if we want to retrieve the gzip version or
	 *            not
	 * @return the readable byte channel from the bundle name
	 * @throws ResourceNotFoundException
	 *             if the resource is not found
	 */
	public ReadableByteChannel getResourceBundleChannel(String bundleName,
			boolean gzipBundle) throws ResourceNotFoundException {

		String tempFileName = getStoredBundlePath(bundleName, gzipBundle);
		InputStream is = getTemporaryResourceAsStream(tempFileName);
		return Channels.newChannel(is);
	}

	/**
	 * Resolves the file name with which a bundle is stored.
	 * 
	 * @param bundleName
	 *            the bundle name
	 * @param asGzippedBundle
	 *            the flag indicating if it's a gzipped bundle or not
	 * @return the file name.
	 */
	private String getStoredBundlePath(String bundleName,
			boolean asGzippedBundle) {
		String tempFileName;

		if (asGzippedBundle)
			tempFileName = gzipDirPath;
		else
			tempFileName = textDirPath;

		return getStoredBundlePath(tempFileName, bundleName);
	}

	/**
	 * Resolves the file path of the bundle from the root directory.
	 * 
	 * @param rootDir
	 *            the rootDir
	 * @param bundleName
	 *            the bundle name
	 * @return the file path
	 */
	private String getStoredBundlePath(String rootDir, String bundleName) {
		if (bundleName.indexOf('/') != -1) {
			bundleName = bundleName.replace('/', File.separatorChar);
		}

		if (!bundleName.startsWith(File.separator)) {
			rootDir += File.separator;
		}

		return rootDir + PathNormalizer.escapeToPhysicalPath(bundleName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.ResourceHandler#storebundle(java.lang.String
	 * , java.lang.StringBuffer)
	 */
	public void storeBundle(String bundleName, StringBuffer bundledResources) {

		JoinableResourceBundleContent bundleResourcesContent = new JoinableResourceBundleContent(
				bundledResources);
		storeBundle(bundleName, bundleResourcesContent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.ResourceHandler#storeBundle(java.lang.String,
	 * net.jawr.web.resource.bundle.JoinableResourceBundleContent)
	 */
	public void storeBundle(String bundleName,
			JoinableResourceBundleContent bundleResourcesContent) {

		// Text version
		String bundleContent = bundleResourcesContent.getContent().toString();
		storeBundle(bundleName, bundleContent, false, textDirPath);

		// binary version
		storeBundle(bundleName, bundleContent, true, gzipDirPath);
	}

	/**
	 * Stores a resource bundle either in text or binary gzipped format.
	 * 
	 * @param bundleName
	 *            the bundle name
	 * @param bundledResources
	 *            the bundledRessources
	 * @param gzipFile
	 *            a fag defining if the file is gzipped or not
	 * @param rootDir
	 *            the root directory
	 */
	@SuppressWarnings("resource")
	private void storeBundle(String bundleName, String bundledResources,
			boolean gzipFile, String rootdir) {
		if (LOGGER.isDebugEnabled()) {
			String msg = "Storing a generated "
					+ (gzipFile ? "and gzipped" : "")
					+ " bundle with an id of:" + bundleName;
			LOGGER.debug(msg);
		}

		try {
			// Create subdirs if needed
			bundleName = bundleName.replaceAll(":", "_");
			if (bundleName.indexOf('/') != -1) {
				StringTokenizer tk = new StringTokenizer(bundleName, "/");
				StringBuffer pathName = new StringBuffer(rootdir);
				while (tk.hasMoreTokens()) {
					String name = tk.nextToken();
					if (tk.hasMoreTokens()) {
						pathName.append(File.separator + name);
						createDir(pathName.toString());
					}
				}
				bundleName = bundleName.replace('/', File.separatorChar);
			}

			File store = createNewFile(rootdir + File.separator + bundleName);

			GZIPOutputStream gzOut = null;
			Writer wr = null;
			try {
				if (gzipFile) {
					FileOutputStream fos = new FileOutputStream(store);
					gzOut = new GZIPOutputStream(fos);
					byte[] data = bundledResources.toString().getBytes(
							charset.name());
					gzOut.write(data, 0, data.length);
				} else {
					FileOutputStream fos = new FileOutputStream(store);
					FileChannel channel = fos.getChannel();
					wr = Channels.newWriter(channel, charset.newEncoder(), -1);
					wr.write(bundledResources.toString());
				}
			} finally {
				IOUtils.close(gzOut);
				IOUtils.close(wr);
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new BundlingProcessException(
					"Unexpected IOException creating temporary jawr file", e);
		}
	}

	/**
	 * Creates a directory. If dir is note created for some reason a
	 * runtimeexception is thrown.
	 * 
	 * @param dir
	 * @throws IOException
	 */
	private File createDir(String path) throws IOException {
		// In windows, pathnames with spaces are returned as %20
		if (path.indexOf("%20") != -1)
			path = path.replaceAll("%20", " ");
		File dir = new File(path);
		if (!dir.exists() && !dir.mkdirs())
			throw new BundlingProcessException(
					"Error creating temporary jawr directory with path:"
							+ dir.getPath());

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Created dir: " + dir.getCanonicalPath());
		}
		return dir;
	}

	/**
	 * Creates a file. If dir is note created for some reson a runtimeexception
	 * is thrown.
	 * 
	 * @param path
	 * @return
	 * @throws IOException
	 */
	private File createNewFile(String path) throws IOException {

		// In windows, pathnames with spaces are returned as %20
		if (path.indexOf("%20") != -1)
			path = path.replaceAll("%20", " ");
		File newFile = new File(path);

		if (!newFile.exists() && !newFile.createNewFile()) {
			throw new BundlingProcessException(
					"Unable to create a temporary file at " + path);
		}

		if (LOGGER.isDebugEnabled())
			LOGGER.debug("Created file: " + newFile.getCanonicalPath());
		return newFile;
	}


}
