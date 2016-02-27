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
import net.jawr.web.resource.bundle.mappings.FilePathMapping;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;
import net.jawr.web.resource.handler.reader.WorkingDirectoryLocationAware;
import net.jawr.web.util.StopWatch;

/**
 * This class defines CSS generator which handles cache for generated resource
 * 
 * @author Ibrahim Chaehoi
 */
public abstract class AbstractCssCachedGenerator extends AbstractCSSGenerator implements

		PostInitializationAwareResourceGenerator, WorkingDirectoryLocationAware,
		ResourceReaderHandlerAwareResourceGenerator {

	/** The Perf Logger */
	private static Logger PERF_LOGGER = LoggerFactory.getLogger(JawrConstant.PERF_PROCESSING_LOGGER);

	/** The Logger */
	private static Logger LOGGER = LoggerFactory.getLogger(AbstractCssCachedGenerator.class);

	/** The ResourceReaderHandler */
	protected ResourceReaderHandler rsHandler;

	/** The working directory */
	protected String workingDir;

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

		loadCacheMapping();
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
	protected String getTempFilePath(GeneratorContext context) {
		return workingDir + "/" + getTempDirectoryName()
				+ context.getPath().replaceFirst(GeneratorRegistry.PREFIX_SEPARATOR, URL_SEPARATOR);
	}

	/**
	 * Returns the temporary directory
	 * 
	 * @return the temporary directory
	 */
	protected abstract String getTempDirectoryName();

	/**
	 * Returns the name of the generator. This is used to log information.
	 * 
	 * @return the name of the generator.
	 */
	protected abstract String getName();

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.generator.AbstractCSSGenerator#
	 * generateResourceForBundle
	 * (net.jawr.web.resource.bundle.generator.GeneratorContext)
	 */
	@Override
	protected Reader generateResourceForBundle(GeneratorContext context) {

		String path = context.getPath();
		StopWatch stopWatch = null;
		if (PERF_LOGGER.isDebugEnabled()) {
			stopWatch = new StopWatch("Generating resource '" + path + "' with " + getName() + " generator");
			stopWatch.start();
		}

		Reader rd = null;
		List<FilePathMapping> fMappings = linkedResourceMap.get(path);
		if (fMappings != null && !checkResourcesModified(context, fMappings)) {
			// Retrieve from cache
			// Checks if temp resource is already created
			rd = retrieveFromCache(path, context);
		}

		if (rd == null) {

			rd = generateResource(context, path);
			// Update the cache
			if (context.isProcessingBundle()) {
				// TODO Remove this when PostBundling process event is in place
				serializeCacheMapping();
			}
			if (rd != null) {
				rd = createTempResource(context, rd);
			}
		}

		if (PERF_LOGGER.isDebugEnabled()) {
			stopWatch.stop();
			PERF_LOGGER.debug(stopWatch.shortSummary());
		}

		return rd;

	}

	/**
	 * Generates the resource which will be cached
	 * 
	 * @param context
	 *            the generator context
	 * @param path
	 *            the resource path
	 */
	protected abstract Reader generateResource(GeneratorContext context, String path);

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
	 * 
	 * @return the reader to the resource
	 */
	private Reader retrieveFromCache(String path, GeneratorContext context) {

		Reader rd = null;
		String filePath = getTempFilePath(context);
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
	 * @param rd
	 *            the reader of the compiled resource
	 * @return the reader
	 */
	protected Reader createTempResource(GeneratorContext context, Reader rd) {
		String filePath = getTempFilePath(context);
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
	private void serializeCacheMapping() {
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
	protected abstract String getCacheFileName();

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
				throw new BundlingProcessException("Unable to initialize Less Generator cache", e);
			} finally {
				IOUtils.close(rd);
			}
		}
	}
}
