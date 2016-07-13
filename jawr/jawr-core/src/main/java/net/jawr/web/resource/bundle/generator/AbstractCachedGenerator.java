/**
 * Copyright 2016 Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.generator;

import static net.jawr.web.JawrConstant.URL_SEPARATOR;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.exception.BundlingProcessException;
import net.jawr.web.resource.bundle.IOUtils;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.generator.CachedGenerator.CacheMode;
import net.jawr.web.resource.bundle.lifecycle.BundlingProcessLifeCycleListener;
import net.jawr.web.resource.bundle.locale.LocaleUtils;
import net.jawr.web.resource.bundle.mappings.FilePathMapping;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;
import net.jawr.web.resource.handler.reader.WorkingDirectoryLocationAware;
import net.jawr.web.util.StopWatch;
import net.jawr.web.util.StringUtils;

/**
 * This class defines JS generator which handles cache for generated resource
 * 
 * @author Ibrahim Chaehoi
 */
public abstract class AbstractCachedGenerator
		implements TextResourceGenerator, ConfigurationAwareResourceGenerator, PostInitializationAwareResourceGenerator,
		WorkingDirectoryLocationAware, ResourceReaderHandlerAwareResourceGenerator, BundlingProcessLifeCycleListener {

	/** The Perf Logger */
	private static final Logger PERF_LOGGER = LoggerFactory.getLogger(JawrConstant.PERF_PROCESSING_LOGGER);

	/** The Logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCachedGenerator.class);

	/** The separator for file mappings */
	private static final String SEMICOLON = ";";

	/** The last modification separator in file mapping */
	private static final String MAPPING_TIMESTAMP_SEPARATOR = "#";

	/** The cache mapping prefix */
	private static final String JAWR_MAPPING_PREFIX = "jawr.cache.mapping.";

	/** The ResourceReaderHandler */
	protected ResourceReaderHandler rsHandler;

	/** The working directory */
	protected String workingDir;

	/** The name of the generator used to log info */
	protected String name;

	/** The cache mapping file name */
	protected String cacheMappingFileName;

	/** The cache directory */
	protected String cacheDirectory;

	/** The flag indicating that we are using cache */
	protected boolean useCache = false;

	/** The cache mode */
	protected CacheMode cacheMode;

	/** The cache properties */
	protected Properties cacheProperties = new Properties();

	/** The jawr configuration */
	protected JawrConfig config;

	/**
	 * Constructor
	 */
	public AbstractCachedGenerator() {

	}

	/**
	 * The map which links a less resource path and the resources which it is
	 * linked to
	 */
	protected Map<String, List<FilePathMapping>> linkedResourceMap = new ConcurrentHashMap<>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.handler.reader.WorkingDirectoryLocationAware#
	 * setWorkingDirectory(java.lang.String)
	 */
	@Override
	public void setWorkingDirectory(String workingDir) {
		this.workingDir = workingDir;
		if (!this.workingDir.endsWith(URL_SEPARATOR)) {
			this.workingDir = this.workingDir + URL_SEPARATOR;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.generator.
	 * ConfigurationAwareResourceGenerator#setConfig(net.jawr.web.config.
	 * JawrConfig)
	 */
	@Override
	public void setConfig(JawrConfig config) {
		this.config = config;
	}

	/**
	 * Returns the generator working directory
	 * 
	 * @return the generator working directory
	 */
	protected String getGeneratorWorkingDir() {
		return this.workingDir + JawrConstant.GENERATOR_CACHE_DIR + URL_SEPARATOR;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.generator.
	 * PostInitializationAwareResourceGenerator#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() {

		if (this.config.isUseGeneratorCache()) {

			CachedGenerator annotation = getClass().getAnnotation(CachedGenerator.class);
			if (annotation != null) {
				useCache = true;
				name = annotation.name();
				cacheMappingFileName = annotation.mappingFileName();
				cacheDirectory = annotation.cacheDirectory();
				if (!cacheDirectory.endsWith(URL_SEPARATOR)) {
					cacheDirectory = cacheDirectory + URL_SEPARATOR;
				}
				cacheMode = annotation.mode();

				loadCacheMapping();

				// reset cache if invalid
				if (!isCacheValid()) {

					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("Cache of " + getName() + " generator is invalid. Reset cache...");
					}
					resetCache();
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.generator.
	 * ResourceReaderHandlerAwareResourceGenerator
	 * #setResourceReaderHandler(net.jawr
	 * .web.resource.handler.reader.ResourceReaderHandler)
	 */
	@Override
	public void setResourceReaderHandler(ResourceReaderHandler rsHandler) {
		this.rsHandler = rsHandler;
	}

	/**
	 * Returns the file path of the temporary resource
	 * 
	 * @param context
	 *            the generator context
	 * @param cacheMode
	 *            the cache mode
	 * @return the file path of the temporary resource
	 */
	protected String getTempFilePath(GeneratorContext context, CacheMode cacheMode) {

		return getTempDirectory() + cacheMode + URL_SEPARATOR + getResourceCacheKey(context.getPath(), context);
	}

	/**
	 * Returns the temporary directory or null if the generator don't use cache
	 * 
	 * @return the temporary directory or null if the generator don't use cache
	 */
	protected String getTempDirectory() {
		return this.workingDir + JawrConstant.GENERATOR_CACHE_DIR + URL_SEPARATOR + cacheDirectory + URL_SEPARATOR;
	}

	/**
	 * Returns the name of the generator. This is used to log information.
	 * 
	 * @return the name of the generator.
	 */
	protected String getName() {
		return name != null ? name : getClass().getSimpleName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.generator.TextResourceGenerator#
	 * createResource(net.jawr.web.resource.bundle.generator.GeneratorContext)
	 */
	@SuppressWarnings("resource")
	@Override
	public Reader createResource(GeneratorContext context) {

		String path = context.getPath();
		StopWatch stopWatch = null;
		if (PERF_LOGGER.isDebugEnabled()) {
			stopWatch = new StopWatch("Generating resource '" + path + "' with " + getName() + " generator");
			stopWatch.start();
		}

		Reader rd = null;
		if (useCache) {
			List<FilePathMapping> fMappings = linkedResourceMap.get(getResourceCacheKey(path, context));
			if (fMappings != null && !checkResourcesModified(context, fMappings)) {
				// Retrieve from cache
				// Checks if temp resource is already created
				if (context.isProcessingBundle()) {

					if (cacheMode.equals(CacheMode.PROD) || cacheMode.equals(CacheMode.ALL)) {
						rd = retrieveFromCache(path, context, CacheMode.PROD);
					}
				} else {
					if (cacheMode.equals(CacheMode.DEBUG) || cacheMode.equals(CacheMode.ALL)) {
						rd = retrieveFromCache(path, context, CacheMode.DEBUG);
					}
				}
			}
		}

		if (rd == null) {

			rd = generateResource(path, context);
			if (useCache) {

				if (rd != null) {
					if (cacheMode.equals(CacheMode.PROD) || cacheMode.equals(CacheMode.ALL)) {
						rd = createTempResource(context, CacheMode.PROD, rd);
					}
				}
			}
			if (context.isProcessingBundle()) {
				if (useCache && (cacheMode.equals(CacheMode.DEBUG) || cacheMode.equals(CacheMode.ALL))) {
					// Create debug cache while processing bundle if cache is
					// allowed in debug
					String content = null;
					try {
						content = IOUtils.toString(rd);
					} catch (IOException e) {
						throw new BundlingProcessException(e);
					}
					Reader dRd = generateResourceForDebug(new StringReader(content), context);
					createTempResource(context, CacheMode.DEBUG, dRd);
					rd = new StringReader(content);
				}
			} else {
				rd = generateResourceForDebug(rd, context);
				if (useCache && (cacheMode.equals(CacheMode.DEBUG) || cacheMode.equals(CacheMode.ALL))) {
					rd = createTempResource(context, CacheMode.DEBUG, rd);
				}
			}
		}

		if (PERF_LOGGER.isDebugEnabled()) {
			stopWatch.stop();
			PERF_LOGGER.debug(stopWatch.shortSummary());
		}

		return rd;
	}

	/**
	 * Returns the cache key for linked resources map
	 * 
	 * @param path
	 *            the resource path
	 * @param context
	 *            the generator context
	 * @return the cache key for linked resource map
	 */
	protected String getResourceCacheKey(String path, GeneratorContext context) {

		StringBuilder strbCacheKey = new StringBuilder(path);
		if (StringUtils.isNotEmpty(context.getBracketsParam())) {
			strbCacheKey.append("_").append(context.getBracketsParam());
		}
		if (StringUtils.isNotEmpty(context.getParenthesesParam())) {
			strbCacheKey.append("_").append(context.getParenthesesParam());
		}

		String cacheKey = strbCacheKey.toString();

		Locale locale = context.getLocale();
		if (locale != null) {
			cacheKey = LocaleUtils.toBundleName(strbCacheKey.toString(), locale);

		}

		cacheKey = cacheKey.replaceAll("[^\\w\\.\\-]", "_");

		return cacheKey;
	}

	/**
	 * Adds the linked resource to the linked resource map
	 * 
	 * @param path
	 *            the resource path
	 * @param context
	 *            the generator context
	 * @param fMapping
	 *            the file path mapping linked to the resource
	 */
	protected void addLinkedResources(String path, GeneratorContext context, FilePathMapping fMapping) {
		addLinkedResources(path, context, Arrays.asList(fMapping));
	}

	/**
	 * Adds the linked resource to the linked resource map
	 * 
	 * @param path
	 *            the resource path
	 * @param context
	 *            the generator context
	 * @param fMappings
	 *            the list of mappings linked to the resource
	 */
	protected void addLinkedResources(String path, GeneratorContext context, List<FilePathMapping> fMappings) {
		linkedResourceMap.put(getResourceCacheKey(path, context), new CopyOnWriteArrayList<>(fMappings));
		JoinableResourceBundle bundle = context.getBundle();
		if (bundle != null) {

			List<FilePathMapping> bundleFMappings = bundle.getLinkedFilePathMappings();
			for (FilePathMapping fMapping : fMappings) {
				FilePathMapping fm = new FilePathMapping(bundle, fMapping.getPath(), fMapping.getLastModified());
				if (!bundleFMappings.contains(fm)) {
					bundleFMappings.add(fm);
				}
			}
		}

	}

	/**
	 * Generates the resource content for debug mode
	 * 
	 * @param rd
	 *            the reader to the resource
	 * @param context
	 *            the context
	 * @return the resource content for debug mode
	 */
	protected Reader generateResourceForDebug(Reader rd, GeneratorContext context) {
		return rd;
	}

	/**
	 * Generates the resource which will be cached
	 * 
	 * @param path
	 *            the resource path
	 * @param context
	 *            the generator context
	 * @return the reader
	 */
	protected Reader generateResource(String path, GeneratorContext context) {

		/**
		 * This method should have been declared as abstract. The cache feature
		 * was lately, and some older generators override directly the method :
		 * public Reader createResource(GeneratorContext context)
		 * 
		 * So to keep backward compatibility, this method has not been created
		 * in abstract.
		 */
		throw new BundlingProcessException("Please override the method if you're using the generator cache feature.");
	}

	/**
	 * Checks if the resources have been modified
	 * 
	 * @param context
	 *            the generator context
	 * @param fMappings
	 *            the list of resources to check
	 * @return true if the resources have been modified
	 */
	protected boolean checkResourcesModified(GeneratorContext context, List<FilePathMapping> fMappings) {
		boolean resourceModified = false;

		for (Iterator<FilePathMapping> iter = fMappings.iterator(); iter.hasNext() && !resourceModified;) {
			FilePathMapping fMapping = iter.next();
			resourceModified = fMapping.getLastModified() != rsHandler.getLastModified(fMapping.getPath());
		}
		return resourceModified;
	}

	/**
	 * Retrieves the resource from cache if it exists
	 * 
	 * @param path
	 *            the resource path
	 * @param context
	 *            the generator context
	 * @param cacheMode
	 *            the cache mode
	 * @return the reader to the resource
	 */
	protected Reader retrieveFromCache(String path, GeneratorContext context, CacheMode cacheMode) {

		Reader rd = null;
		String filePath = getTempFilePath(context, cacheMode);
		FileInputStream fis = null;
		File file = new File(filePath);
		if (file.exists()) {
			try {
				fis = new FileInputStream(file);
			} catch (FileNotFoundException e) {
				throw new BundlingProcessException("An error occured while creating temporary resource for " + filePath,
						e);
			}
			FileChannel inchannel = fis.getChannel();
			rd = Channels.newReader(inchannel, context.getConfig().getResourceCharset().newDecoder(), -1);
			context.setRetrievedFromCache(true);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(getName() + " resource '" + path + "' retrieved from cache");
			}
		}
		return rd;
	}

	/**
	 * Creates the temporary resource
	 * 
	 * @param context
	 *            the context
	 * @param cacheMode
	 *            the cache mode
	 * @param rd
	 *            the reader of the compiled resource
	 * @return the reader
	 */
	protected Reader createTempResource(GeneratorContext context, CacheMode cacheMode, Reader rd) {
		String filePath = getTempFilePath(context, cacheMode);
		Writer wr = null;
		FileOutputStream fos = null;
		try {
			File f = new File(filePath);
			if (!f.getParentFile().exists()) {
				f.getParentFile().mkdirs();
			}
			String content = IOUtils.toString(rd);
			fos = new FileOutputStream(f);
			FileChannel channel = fos.getChannel();
			wr = Channels.newWriter(channel, config.getResourceCharset().newEncoder(), -1);
			wr.write(content);

			rd = new StringReader(content);
		} catch (IOException e) {
			throw new BundlingProcessException("Unable to create temporary resource for '" + context.getPath() + "'",
					e);
		} finally {
			IOUtils.close(wr);
			IOUtils.close(fos);
		}

		return rd;
	}

	/**
	 * Resets the cache
	 */
	protected void resetCache() {
		cacheProperties.clear();
		linkedResourceMap.clear();
		cacheProperties.put(JawrConfig.JAWR_CHARSET_NAME, config.getResourceCharset().name());
	}

	/**
	 * Returns true if the cache is valid
	 * 
	 * @return true if the cache is valid
	 */
	protected boolean isCacheValid() {

		return StringUtils.equals(cacheProperties.getProperty(JawrConfig.JAWR_CHARSET_NAME),
				config.getResourceCharset().name());

	}

	/**
	 * Serialize the cache file mapping
	 */
	protected synchronized void serializeCacheMapping() {
		for (Map.Entry<String, List<FilePathMapping>> entry : linkedResourceMap.entrySet()) {

			StringBuilder strb = new StringBuilder();
			Iterator<FilePathMapping> iter = entry.getValue().iterator();
			if (iter.hasNext()) {

				for (; iter.hasNext();) {
					FilePathMapping fMapping = iter.next();
					strb.append(fMapping.getPath()).append(MAPPING_TIMESTAMP_SEPARATOR)
							.append(fMapping.getLastModified());
					if (iter.hasNext()) {
						strb.append(SEMICOLON);
					}
				}
				cacheProperties.put(JAWR_MAPPING_PREFIX + entry.getKey(), strb.toString());
			}
		}

		File f = new File(getCacheFilePath());
		if (!f.getParentFile().exists()) {
			f.getParentFile().mkdirs();
		}
		FileWriter fw = null;
		try {
			fw = new FileWriter(f);
			cacheProperties.store(fw, "Cache properties of " + getName() + " generator");
		} catch (IOException e) {
			throw new BundlingProcessException("Unable to save cache file mapping ", e);
		} finally {
			IOUtils.close(fw);
		}
	}

	/**
	 * Returns the file path of the cache file
	 * 
	 * @return the file path of the cache file
	 */
	protected String getCacheFilePath() {
		return getTempDirectory() + getCacheFileName();
	}

	/**
	 * Returns the cache mapping file name
	 * 
	 * @return the cache mapping file name
	 */
	protected String getCacheFileName() {
		return cacheMappingFileName;
	}

	/**
	 * Loads the less file mapping
	 */
	protected void loadCacheMapping() {
		File f = new File(getCacheFilePath());
		if (f.exists()) {
			try(InputStream is = new FileInputStream(f); BufferedReader rd = new BufferedReader(new FileReader(f))) {
				cacheProperties.load(is);
				for (Enumeration<?> properyNames = cacheProperties.propertyNames(); properyNames.hasMoreElements();) {
					String propName = (String) properyNames.nextElement();
					if (propName.startsWith(JAWR_MAPPING_PREFIX)) {

						String value = cacheProperties.getProperty(propName);

						String resourceMapping = propName.substring(JAWR_MAPPING_PREFIX.length());
						String[] mappings = value.split(SEMICOLON);
						List<FilePathMapping> fMappings = new CopyOnWriteArrayList<>();

						// TODO check the use of mappingModified
						boolean mappingModified = true;
						for (String fmapping : mappings) {
							String[] mapping = fmapping.split(MAPPING_TIMESTAMP_SEPARATOR);
							long lastModified = Long.parseLong(mapping[1]);
							String filePath = mapping[0];
							if (rsHandler.getLastModified(filePath) != lastModified) {
								mappingModified = false;
								break;
							}
							FilePathMapping fmap = new FilePathMapping(filePath, lastModified);
							fMappings.add(fmap);
						}

						if (mappingModified) {
							linkedResourceMap.put(resourceMapping, fMappings);
						}
					}
				}

			} catch (IOException e) {
				throw new BundlingProcessException("Unable to initialize " + getName() + " Generator cache", e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.handler.BundlingProcessLifeCycleListener#
	 * beforeBundlingProcess()
	 */
	@Override
	public void beforeBundlingProcess() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.handler.BundlingProcessLifeCycleListener#
	 * afterBundlingProcess()
	 */
	@Override
	public void afterBundlingProcess() {

		if (useCache) {
			// Update the cache
			serializeCacheMapping();
		}
	}

}
