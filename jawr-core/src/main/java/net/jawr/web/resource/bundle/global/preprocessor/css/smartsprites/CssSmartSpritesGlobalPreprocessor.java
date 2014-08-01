/**
 * Copyright 2009-2014 Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.global.preprocessor.css.smartsprites;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.exception.BundlingProcessException;
import net.jawr.web.resource.ImageResourcesHandler;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.factory.global.preprocessor.GlobalPreprocessingContext;
import net.jawr.web.resource.bundle.global.processor.AbstractChainedGlobalProcessor;
import net.jawr.web.resource.bundle.iterator.BundlePath;
import net.jawr.web.resource.handler.reader.ResourceReader;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;

import org.apache.commons.io.FileUtils;
import org.carrot2.labs.smartsprites.SmartSpritesParameters;
import org.carrot2.labs.smartsprites.SmartSpritesParameters.PngDepth;
import org.carrot2.labs.smartsprites.SpriteBuilder;
import org.carrot2.labs.smartsprites.message.Message;
import org.carrot2.labs.smartsprites.message.Message.MessageLevel;
import org.carrot2.labs.smartsprites.message.MessageLog;
import org.carrot2.labs.smartsprites.message.MessageSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class defines the global preprocessor which will process all CSS files
 * which used smartsprites annotations.
 * 
 * @author Ibrahim Chaehoi
 * 
 */
public class CssSmartSpritesGlobalPreprocessor extends
		AbstractChainedGlobalProcessor<GlobalPreprocessingContext> {

	/** The logger */
	private static Logger LOGGER = LoggerFactory
			.getLogger(CssSmartSpritesGlobalPreprocessor.class);

	/** The error level name */
	private static final String ERROR_LEVEL = "ERROR";

	/** The warn level name */
	private static final String WARN_LEVEL = "WARN";

	/** The info level name */
	private static final String INFO_LEVEL = "INFO";

	/**
	 * Constructor
	 */
	public CssSmartSpritesGlobalPreprocessor() {
		super(JawrConstant.GLOBAL_CSS_SMARTSPRITES_PREPROCESSOR_ID);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.global.processor.GlobalProcessor#processBundles
	 * (
	 * net.jawr.web.resource.bundle.global.processor.AbstractGlobalProcessingContext
	 * , java.util.List)
	 */
	public void processBundles(GlobalPreprocessingContext ctx,
			List<JoinableResourceBundle> bundles) {

		ResourceReaderHandler rsHandler = ctx.getRsReaderHandler();
		Set<String> resourcePaths = getResourcePaths(bundles);
		JawrConfig jawrConfig = ctx.getJawrConfig();
		Charset charset = jawrConfig.getResourceCharset();

		ImageResourcesHandler imgRsHandler = (ImageResourcesHandler) jawrConfig
				.getContext().getAttribute(JawrConstant.IMG_CONTEXT_ATTRIBUTE);

		ResourceReader cssSpriteResourceReader = null;
		if (ctx.hasBundleToBeProcessed()) {
			generateSprites(rsHandler, imgRsHandler, resourcePaths, jawrConfig,
					charset);
		}

		// Update CSS resource handler
		cssSpriteResourceReader = new CssSmartSpritesResourceReader(
				rsHandler.getWorkingDirectory(), jawrConfig);
		ctx.getRsReaderHandler().addResourceReaderToStart(
				cssSpriteResourceReader);

		// Update image resource handler
		ResourceReaderHandler imgStreamRsHandler = imgRsHandler
				.getRsReaderHandler();
		imgStreamRsHandler.addResourceReaderToStart(cssSpriteResourceReader);
	}

	/**
	 * Generates the image sprites from the smartsprites annotation in the CSS,
	 * rewrite the CSS files to references the generated sprites.
	 * 
	 * @param cssRsHandler
	 *            the css resourceHandler
	 * @param imgRsHandler
	 *            the image resourceHandler
	 * @param resourcePaths
	 *            the set of CSS resource paths to handle
	 * @param jawrConfig
	 *            the Jawr config
	 * @param charset
	 *            the charset
	 */
	private void generateSprites(ResourceReaderHandler cssRsHandler,
			ImageResourcesHandler imgRsHandler, Set<String> resourcePaths,
			JawrConfig jawrConfig, Charset charset) {

		MessageLevel msgLevel = MessageLevel.valueOf(ERROR_LEVEL);
		String sinkLevel = WARN_LEVEL;
		if (LOGGER.isTraceEnabled() || LOGGER.isDebugEnabled()
				|| LOGGER.isInfoEnabled()) { // logLevel.isGreaterOrEqual(Level.DEBUG)
			msgLevel = MessageLevel.valueOf(INFO_LEVEL);
			sinkLevel = INFO_LEVEL;
		} else if (LOGGER.isWarnEnabled() || LOGGER.isErrorEnabled()) { // logLevel.isGreaterOrEqual(Level.WARN)
			msgLevel = MessageLevel.valueOf(WARN_LEVEL);
			sinkLevel = WARN_LEVEL;
		}

		MessageLog messageLog = new MessageLog(
				new MessageSink[] { new LogMessageSink(sinkLevel) });

		SmartSpritesResourceHandler smartSpriteRsHandler = new SmartSpritesResourceHandler(
				cssRsHandler, imgRsHandler.getRsReaderHandler(),
				jawrConfig.getGeneratorRegistry(), imgRsHandler.getConfig()
						.getGeneratorRegistry(), charset.toString(), messageLog);

		smartSpriteRsHandler.setContextPath(jawrConfig
				.getProperty(JawrConstant.JAWR_CSS_URL_REWRITER_CONTEXT_PATH));

		String outDir = cssRsHandler.getWorkingDirectory()
				+ JawrConstant.CSS_SMARTSPRITES_TMP_DIR;

		// Create temp directories
		File tmpDir = new File(outDir);
		
		if (!tmpDir.exists()) {
			if (!tmpDir.mkdirs()) {
				throw new BundlingProcessException(
						"Impossible to create temporary directory : " + outDir);
			}
		}else{
			// Clean temp directories
			try {
				FileUtils.cleanDirectory(tmpDir);
			} catch (IOException e) {
				throw new BundlingProcessException(
						"Impossible to clean temporary directory : " + outDir, e);
			}
		}

		SmartSpritesParameters params = new SmartSpritesParameters("/", null,
				outDir, null, msgLevel, "", PngDepth.valueOf("AUTO"), false,
				charset.toString());
		// TODO : use below parameters when Smartsprites will handle
		// keepingSpriteTrack parameter
		// SmartSpritesParameters params = new SmartSpritesParameters("/", null,
		// outDir, null, msgLevel, "", PngDepth.valueOf("AUTO"), false,
		// charset.toString(), true);

		SpriteBuilder spriteBuilder = new SpriteBuilder(params, messageLog,
				smartSpriteRsHandler);
		try {
			spriteBuilder.buildSprites(resourcePaths);
		} catch (IOException e) {
			throw new BundlingProcessException("Unable to build sprites", e);
		}
	}

	/**
	 * Returns the list of all CSS files defined in the bundles.
	 * 
	 * @param bundles
	 *            the list of bundle
	 * @return the list of all CSS files defined in the bundles.
	 */
	private Set<String> getResourcePaths(List<JoinableResourceBundle> bundles) {

		Set<String> resourcePaths = new HashSet<String>();

		for (Iterator<JoinableResourceBundle> iterator = bundles.iterator(); iterator
				.hasNext();) {
			JoinableResourceBundle bundle = iterator.next();
			for(BundlePath bundlePath : bundle.getItemPathList()){
				resourcePaths.add(bundlePath.getPath());
			}
		}

		return resourcePaths;
	}

	/**
	 * The log message sink
	 * 
	 * @author Ibrahim Chaehoi
	 */
	private static class LogMessageSink implements MessageSink {

		/**
		 * The log level
		 */
		private final String logLevel;

		/**
		 * Constructor
		 * 
		 * @param logLevel
		 *            the log level
		 */
		public LogMessageSink(String logLevel) {
			this.logLevel = logLevel != null ? logLevel : INFO_LEVEL;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.carrot2.labs.smartsprites.message.MessageSink#add(org.carrot2
		 * .labs.smartsprites.message.Message)
		 */
		public void add(Message message) {
			
			if(LOGGER.isInfoEnabled() && logLevel.equals(INFO_LEVEL)){
				LOGGER.info(message.getFormattedMessage());
			}else if(LOGGER.isWarnEnabled() && logLevel.equals(WARN_LEVEL)){
				LOGGER.warn(message.getFormattedMessage());
			}else if(LOGGER.isErrorEnabled() && logLevel.equals(ERROR_LEVEL)){
				LOGGER.error(message.getFormattedMessage());
			}
			
		}
	}
}
