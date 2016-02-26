/**
 * Copyright 2015-2016 Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.generator.css.sass;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.sass.internal.ScssContext;
import com.vaadin.sass.internal.ScssContext.UrlMode;

import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.exception.BundlingProcessException;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.bundle.IOUtils;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.generator.AbstractCSSGenerator;
import net.jawr.web.resource.bundle.generator.ConfigurationAwareResourceGenerator;
import net.jawr.web.resource.bundle.generator.GeneratorContext;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.generator.PostInitializationAwareResourceGenerator;
import net.jawr.web.resource.bundle.generator.ResourceReaderHandlerAwareResourceGenerator;
import net.jawr.web.resource.bundle.generator.resolver.ResourceGeneratorResolver;
import net.jawr.web.resource.bundle.generator.resolver.ResourceGeneratorResolverFactory;
import net.jawr.web.resource.bundle.mappings.FilePathMapping;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;
import net.jawr.web.resource.handler.reader.WorkingDirectoryLocationAware;
import net.jawr.web.util.StopWatch;

/**
 * This class defines the Sass generator
 * 
 * @author Ibrahim Chaehoi
 */
public class SassGenerator extends AbstractCSSGenerator
		implements ISassResourceGenerator, ResourceReaderHandlerAwareResourceGenerator,
		ConfigurationAwareResourceGenerator, WorkingDirectoryLocationAware, PostInitializationAwareResourceGenerator {

	/** The Logger */
	private static Logger PERF_LOGGER = LoggerFactory.getLogger(JawrConstant.PERF_PROCESSING_LOGGER);

	/** The Logger */
	private static Logger LOGGER = LoggerFactory.getLogger(SassGenerator.class);

	/** The Sass generator URL mode property name */
	public static final String SAAS_GENERATOR_URL_MODE = "jawr.css.saas.generator.urlMode";

	/** The Sass generator URL mode default value */
	public static final String SASS_GENERATOR_DEFAULT_URL_MODE = "MIXED";

	/** The resolver */
	private ResourceGeneratorResolver resolver;

	/** The resource reader handler */
	private ResourceReaderHandler rsHandler;

	/**
	 * The URL mode handling for binary resource URL present in the Scss file
	 */
	private ScssContext.UrlMode urlMode;

	/** The working directory */
	private String workingDir;

	/**
	 * The map which links a less resource path and the resources which it is
	 * linked to
	 */
	private Map<String, List<FilePathMapping>> linkedResourceMap = new ConcurrentHashMap<>();

	/**
	 * Constructor
	 */
	public SassGenerator() {
		resolver = ResourceGeneratorResolverFactory.createSuffixResolver(GeneratorRegistry.SASS_GENERATOR_SUFFIX);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.generator.BaseResourceGenerator#getResolver
	 * ()
	 */
	@Override
	public ResourceGeneratorResolver getResolver() {
		return resolver;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.generator.
	 * ConfigurationAwareResourceGenerator#setConfig(net.jawr.web.config.
	 * JawrConfig)
	 */
	@Override
	public void setConfig(JawrConfig config) {

		String value = config.getProperty(SAAS_GENERATOR_URL_MODE, SASS_GENERATOR_DEFAULT_URL_MODE);
		urlMode = UrlMode.valueOf(value);
	}

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
	 * @see net.jawr.web.resource.bundle.generator.AbstractCSSGenerator#
	 * generateResourceForBundle
	 * (net.jawr.web.resource.bundle.generator.GeneratorContext)
	 */
	@Override
	protected Reader generateResourceForBundle(GeneratorContext context) {

		String path = context.getPath();
		StopWatch stopWatch = null;
		if (PERF_LOGGER.isDebugEnabled()) {
			stopWatch = new StopWatch("Generating resource '" + path + "' with Sass generator");
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

			rd = generateSassResource(context, path);
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
	 * Generates the less resource
	 * 
	 * @param context
	 *            the generator context
	 * @param path
	 *            the path
	 * @return the generated resource
	 */
	protected Reader generateSassResource(GeneratorContext context, String path) {

		Reader rd = null;

		try {
			List<Class<?>> excluded = new ArrayList<Class<?>>();
			excluded.add(ISassResourceGenerator.class);
			JoinableResourceBundle bundle = context.getBundle();
			rd = context.getResourceReaderHandler().getResource(bundle, path, false, excluded);

			if (rd == null) {
				throw new ResourceNotFoundException(path);
			}
			String content = IOUtils.toString(rd);

			String result = compile(bundle, content, path, context.getCharset());
			rd = new StringReader(result);

			// Update the cache
			if (context.isProcessingBundle()) {
				// TODO Remove this when PostBundling process event is in place
				serializeCacheMapping();
			}

		} catch (ResourceNotFoundException e) {
			throw new BundlingProcessException("Unable to generate content for resource path : '" + path + "'", e);
		} catch (IOException e) {
			throw new BundlingProcessException("Unable to generate content for resource path : '" + path + "'", e);
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
	private Reader createTempResource(GeneratorContext context, Reader rd) {
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
			throw new BundlingProcessException(
					"Unable to create less temporary resource for '" + context.getPath() + "'", e);
		} finally {
			IOUtils.close(fwr);
		}

		return rd;
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
	 * Returns the file path of the temporary resource
	 * 
	 * @param context
	 *            the generator context
	 * @return the file path of the temporary resource
	 */
	private String getTempFilePath(GeneratorContext context) {
		return workingDir + "/" + getTempDirectoryName()
				+ context.getPath().replaceFirst(GeneratorRegistry.PREFIX_SEPARATOR, JawrConstant.URL_SEPARATOR);
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
				LOGGER.debug("Less resource '" + path + "' retrieved from cache");
			}
		}
		return rd;
	}

	/**
	 * Compile the SASS source to a CSS source
	 * 
	 * @param bundle
	 *            the bundle
	 * @param content
	 *            the resource content to compile
	 * @param path
	 *            the compiled resource path
	 * @return the compiled CSS content
	 */
	public String compile(JoinableResourceBundle bundle, String content, String path, Charset charset) {

		try {
			JawrScssResolver scssResolver = new JawrScssResolver(bundle, rsHandler);
			JawrScssStylesheet sheet = new JawrScssStylesheet(bundle, content, path, scssResolver, charset);
			scssResolver.setParentScssStyleSheet(sheet);
			sheet.compile(urlMode);
			String parsedScss = sheet.printState();
			linkedResourceMap.put(path, new CopyOnWriteArrayList<>(sheet.getLinkedResources()));
		
			return parsedScss;
		} catch (Exception e) {
			throw new BundlingProcessException("Unable to generate content for resource path : '" + path + "'", e);
		}
	}

	/**
	 * Returns the temporary directory
	 * 
	 * @return the temporary directory
	 */
	private String getTempDirectoryName() {
		return "sassCss";
	}

	/**
	 * Returns the file path of the less generator cache, which contains for
	 * each less resource, the linked resources and their last modification date
	 * 
	 * @return the file path of the less generator cache
	 */
	private String getCacheFilePath() {
		return this.workingDir + getTempDirectoryName() + "lessGeneratorCache.txt";
	}

	/**
	 * Serialize the less resource mapping to a cache file
	 * 
	 * @throws IOException
	 *             if an IO exception occurs
	 */
	private void serializeCacheMapping() throws IOException {
		// TODO put in place PostProcessBundling event for this type of action
		StringBuilder strb = new StringBuilder();
		for (Map.Entry<String, List<FilePathMapping>> entry : linkedResourceMap.entrySet()) {
			strb.append(entry.getKey() + "=");
			for (Iterator<FilePathMapping> iter = entry.getValue().iterator(); iter.hasNext();) {
				FilePathMapping fMapping = iter.next();
				strb.append(fMapping.getPath() + "#" + fMapping.getLastModified());
				if (iter.hasNext()) {
					strb.append(";");
				}
			}
		}
		FileWriter fw = new FileWriter(getCacheFilePath());
		try {
			fw.append(strb);
		} finally {
			IOUtils.close(fw);

		}
	}

	/**
	 * Loads the cache file mapping
	 */
	private void loadCacheMapping() {
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
