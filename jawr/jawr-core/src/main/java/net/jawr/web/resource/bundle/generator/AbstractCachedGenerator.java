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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jawr.web.JawrConstant;
import net.jawr.web.exception.BundlingProcessException;
import net.jawr.web.resource.bundle.IOUtils;
import net.jawr.web.resource.bundle.generator.CachedGenerator.CacheMode;
import net.jawr.web.resource.bundle.mappings.FilePathMapping;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;
import net.jawr.web.resource.handler.reader.WorkingDirectoryLocationAware;
import net.jawr.web.util.StopWatch;

/**
 * This class defines JS generator which handles cache for generated resource
 * 
 * @author Ibrahim Chaehoi
 */
public abstract class AbstractCachedGenerator
		implements TextResourceGenerator, PostInitializationAwareResourceGenerator, WorkingDirectoryLocationAware,
		ResourceReaderHandlerAwareResourceGenerator {

	/** The Perf Logger */
	private static Logger PERF_LOGGER = LoggerFactory.getLogger(JawrConstant.PERF_PROCESSING_LOGGER);

	/** The Logger */
	private static Logger LOGGER = LoggerFactory.getLogger(AbstractCachedGenerator.class);

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

	/**
	 * Constructor
	 */
	public AbstractCachedGenerator() {

		CachedGenerator annotation = getClass().getAnnotation(CachedGenerator.class);
		if (annotation != null) {
			useCache = true;
			name = annotation.name();
			cacheMappingFileName = annotation.mappingFileName();
			cacheDirectory = annotation.cacheDirectory();
			if (cacheDirectory.endsWith(URL_SEPARATOR)) {
				cacheDirectory = cacheDirectory + URL_SEPARATOR;
			}
			cacheMode = annotation.mode();
		}
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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.generator.
	 * PostInitializationAwareResourceGenerator#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() {

		if (useCache) {
			loadCacheMapping();
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
	 * @return the file path of the temporary resource
	 */
	protected String getTempFilePath(GeneratorContext context, CacheMode cacheMode) {
		return workingDir + URL_SEPARATOR + getTempDirectoryName() + cacheMode + URL_SEPARATOR
				+ context.getPath().replaceFirst(GeneratorRegistry.PREFIX_SEPARATOR, URL_SEPARATOR);
	}

	/**
	 * Returns the temporary directory or null if the generator don't use cache
	 * 
	 * @return the temporary directory or null if the generator don't use cache
	 */
	protected String getTempDirectoryName() {
		return cacheDirectory;
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
			List<FilePathMapping> fMappings = linkedResourceMap.get(path);
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

				// Update the cache
				if (context.isProcessingBundle()) {
					// TODO Remove this when PostBundling process event is in
					// place
					serializeCacheMapping();
				}
				if (rd != null) {
					if (cacheMode.equals(CacheMode.PROD) || cacheMode.equals(CacheMode.ALL)) {
						rd = createTempResource(context, CacheMode.PROD, rd);
					}
				}
				if (!context.isProcessingBundle()) {
					rd = generateResourceForDebug(rd, context);
					if (cacheMode.equals(CacheMode.DEBUG) || cacheMode.equals(CacheMode.ALL)) {
						rd = createTempResource(context, CacheMode.DEBUG, rd);
					}
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
	 */
	protected Reader generateResource(String path, GeneratorContext context){

		/**
		 * This method should have been declared as abstract.
		 * The cache feature was lately, and some older generators override directly the method :
		 * 		public Reader createResource(GeneratorContext context)
		 * 
		 * So to keep backward compatibility, this method has not been created in abstract.
		 */
		throw new BundlingProcessException("Please override the method if you're using the generator cache feature.");
	}

	/**
	 * Checks if the resources have been modified
	 * 
	 * @param context
	 *            the generator context
	 * @param linkedResources
	 *            the list of resources to check
	 * @return true if the resources have been modified
	 */
	protected boolean checkResourcesModified(GeneratorContext context, List<FilePathMapping> fMappings) {
		boolean resourceModified = false;
		ResourceReaderHandler readerHandler = context.getResourceReaderHandler();

		for (Iterator<FilePathMapping> iter = fMappings.iterator(); iter.hasNext() && !resourceModified;) {
			FilePathMapping fMapping = iter.next();
			resourceModified = fMapping.getLastModified() != readerHandler.getLastModified(fMapping.getPath());
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
		FileWriter fwr = null;
		try {
			File f = new File(filePath);
			if (!f.getParentFile().exists()) {
				f.getParentFile().mkdirs();
			}
			fwr = new FileWriter(filePath);
			IOUtils.copy(rd, fwr);
			rd.reset();
		} catch (IOException e) {
			throw new BundlingProcessException("Unable to create temporary resource for '" + context.getPath() + "'",
					e);
		} finally {
			IOUtils.close(fwr);
		}

		return rd;
	}

	/**
	 * Serialize the cache file mapping
	 * 
	 * @throws IOException
	 *             if an IO exception occurs
	 */
	protected void serializeCacheMapping() {
		// TODO put in place PostProcessBundling event for this type of action
		StringBuilder strb = new StringBuilder();
		for (Map.Entry<String, List<FilePathMapping>> entry : linkedResourceMap.entrySet()) {

			Iterator<FilePathMapping> iter = entry.getValue().iterator();
			if (iter.hasNext()) {

				strb.append(entry.getKey() + "=");
				for (; iter.hasNext();) {
					FilePathMapping fMapping = iter.next();
					strb.append(fMapping.getPath() + "#" + fMapping.getLastModified());
					if (iter.hasNext()) {
						strb.append(";");
					}
				}
			}
		}

		File f = new File(getCacheFilePath());
		if (!f.getParentFile().exists()) {
			f.getParentFile().mkdirs();
		}
		FileWriter fw = null;
		try {
			fw = new FileWriter(f);
			fw.append(strb);
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
		return this.workingDir + URL_SEPARATOR + getTempDirectoryName() + getCacheFileName();
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
			BufferedReader rd = null;
			try {
				rd = new BufferedReader(new FileReader(f));
				String line = null;
				while ((line = rd.readLine()) != null) {
					String[] resourceMapping = line.split("=");
					String lessResource = resourceMapping[0];
					String[] mappings = resourceMapping[1].split(";");
					List<FilePathMapping> fMappings = new CopyOnWriteArrayList<>();
					boolean mappingModified = true;
					for (String fmapping : mappings) {
						String[] mapping = fmapping.split("#");
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
						linkedResourceMap.put(lessResource, fMappings);
					}
				}
			} catch (IOException e) {
				throw new BundlingProcessException("Unable to initialize " + getName() + " Generator cache", e);
			} finally {
				IOUtils.close(rd);
			}
		}
	}
}
