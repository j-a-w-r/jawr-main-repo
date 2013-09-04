package test.net.jawr.web.resource.bundle.generator.css;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletContext;

import net.jawr.web.DebugMode;
import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.ImageResourcesHandler;
import net.jawr.web.resource.bundle.IOUtils;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.generator.GeneratorContext;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.generator.css.less.LessCssGenerator;
import net.jawr.web.resource.bundle.handler.ClientSideHandlerGenerator;
import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;
import net.jawr.web.resource.bundle.iterator.ConditionalCommentCallbackHandler;
import net.jawr.web.resource.bundle.iterator.ResourceBundlePathsIterator;
import net.jawr.web.resource.bundle.variant.VariantSet;
import net.jawr.web.resource.handler.reader.ResourceReader;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import test.net.jawr.web.FileUtils;
import test.net.jawr.web.resource.bundle.MockJoinableResourceBundle;
import test.net.jawr.web.servlet.mock.MockServletContext;

public class LessCssGeneratorTestCase {

	private JawrConfig config;
	private GeneratorContext ctx;
	private LessCssGenerator generator;

	@Before
	public void setUp() throws Exception {
		
		// Bundle path (full url would be: /servletMapping/prefix/css/bundle.css
		final String bundlePath = "/temp.less";
		
		config = new JawrConfig("css", new Properties());
		ServletContext servletContext = new MockServletContext();
		
		String[] paths = new String[]{"/temp.less", "jar:/style.less"};
		servletContext.setAttribute(JawrConstant.CSS_CONTEXT_ATTRIBUTE, getMockBundlesHandler(config, paths));
		config.setContext(servletContext);
		config.setServletMapping("/css");
		config.setCharsetName("UTF-8");
		GeneratorRegistry generatorRegistry = addGeneratorRegistryToConfig(config, JawrConstant.CSS_TYPE);
		generator = new LessCssGenerator();
		ctx = new GeneratorContext(config, bundlePath);
		ctx.setResourceReaderHandler(getResourceReaderHandler());
		generatorRegistry.setResourceReaderHandler(ctx.getResourceReaderHandler());
		
		// Set up the Image servlet Jawr config
		JawrConfig imgServletJawrConfig = new JawrConfig("img", new Properties());
		addGeneratorRegistryToConfig(imgServletJawrConfig, "img");
		ResourceReaderHandler imgHandler = getResourceReaderHandler();
		generatorRegistry.setResourceReaderHandler(imgHandler);
		//imgServletJawrConfig.setServletMapping("/cssImg/");
		ImageResourcesHandler imgRsHandler = new ImageResourcesHandler(imgServletJawrConfig, imgHandler, null);
		servletContext.setAttribute(JawrConstant.IMG_CONTEXT_ATTRIBUTE, imgRsHandler);
		
		generator.setConfig(config);
		generator.afterPropertiesSet();
	}
	
	@Test
	public void testLessCssBundleGeneratorInProductionMode() throws Exception{
		
		ctx.setProcessingBundle(true);
		Reader rd = generator.createResource(ctx);
		StringWriter writer = new StringWriter();
		IOUtils.copy(rd, writer);
		Assert.assertEquals(FileUtils.readClassPathFile("generator/css/less/expected_prod.css"), writer.getBuffer().toString());
	}
	
	@Test
	public void testLessCssBundleGeneratorInDebugMode() throws Exception{
		
		ctx.setProcessingBundle(false);
		Reader rd = generator.createResource(ctx);
		StringWriter writer = new StringWriter();
		IOUtils.copy(rd, writer);
		Assert.assertEquals(FileUtils.readClassPathFile("generator/css/less/expected_debug.css"), writer.getBuffer().toString());
	}
	
	private GeneratorRegistry addGeneratorRegistryToConfig(JawrConfig config, String type) {
		GeneratorRegistry generatorRegistry = new GeneratorRegistry(type){

			
			public boolean isGeneratedImage(String imgResourcePath) {
				return imgResourcePath.startsWith("jar:");
			}

			public boolean isHandlingCssImage(String cssResourcePath) {
				return cssResourcePath.startsWith("jar:");
			}
		};
		generatorRegistry.setConfig(config);
		config.setGeneratorRegistry(generatorRegistry);
		return generatorRegistry;
	}
	
	private ResourceBundlesHandler getMockBundlesHandler(final JawrConfig config, final String[] paths) {
		
		ResourceBundlesHandler bundlesHandler = new ResourceBundlesHandler() {
			
			public void writeBundleTo(String bundlePath, Writer writer)
					throws ResourceNotFoundException {
				
			}
			
			public void streamBundleTo(String bundlePath, OutputStream out)
					throws ResourceNotFoundException {
				
			}
			
			public JoinableResourceBundle resolveBundleForPath(String path) {
				
				JoinableResourceBundle bundle = new MockJoinableResourceBundle(){

					/* (non-Javadoc)
					 * @see test.net.jawr.web.resource.bundle.MockJoinableResourceBundle#getVariants()
					 */
					@Override
					public Map<String, VariantSet> getVariants() {
						return new HashMap<String, VariantSet>();
					}
				};
				
				return bundle;
			}
			
			public boolean isGlobalResourceBundle(String resourceBundleId) {
				return false;
			}
			
			public void initAllBundles() {
				
			}
			
			public List<JoinableResourceBundle> getContextBundles() {
				return null;
			}
			
			public JawrConfig getConfig() {
				return config;
			}
			
			public ClientSideHandlerGenerator getClientSideHandler() {
				return null;
			}
			
			public ResourceBundlePathsIterator getBundlePaths(String bundleId,
					ConditionalCommentCallbackHandler commentCallbackHandler,
					Map<String, String> variants) {
				
				
				return new ResourceBundlePathsIterator() {
					
					private Iterator<String> it = Arrays.asList(paths).iterator();
					
					public void remove() {
						
					}
					
					public String next() {
						return it.next();
					}
					
					public boolean hasNext() {
						return it.hasNext();
					}
					
					public String nextPath() {
						return (String) it.next();
					}
				};
			}

			public ResourceBundlePathsIterator getGlobalResourceBundlePaths(
					ConditionalCommentCallbackHandler commentCallbackHandler,
					Map<String, String> variants) {
				return null;
			}

			public ResourceBundlePathsIterator getGlobalResourceBundlePaths(
					DebugMode debugMode,
					ConditionalCommentCallbackHandler commentCallbackHandler,
					Map<String, String> variants) {
				return null;
			}

			public ResourceBundlePathsIterator getGlobalResourceBundlePaths(
					String bundlePath,
					ConditionalCommentCallbackHandler commentCallbackHandler,
					Map<String, String> variants) {
				return null;
			}

			public ResourceBundlePathsIterator getBundlePaths(
					DebugMode debugMode, String bundleId,
					ConditionalCommentCallbackHandler commentCallbackHandler,
					Map<String, String> variants) {
				return null;
			}

			public boolean containsValidBundleHashcode(String requestedPath) {
				return false;
			}

			public String getBundleTextDirPath() {
				return null;
			}
			
			public List<JoinableResourceBundle> getGlobalBundles() {
				return new ArrayList<JoinableResourceBundle>();
			}

			public String getResourceType() {
				return null;
			}
		};
		return bundlesHandler;
	}

	private ResourceReaderHandler getResourceReaderHandler() {
		
		return new ResourceReaderHandler() {
			
			public void setWorkingDirectory(String workingDir) {
				
			}
			
			public boolean isDirectory(String resourcePath) {
				return false;
			}
			
			public String getWorkingDirectory() {
				return null;
			}
			
			public Set<String> getResourceNames(String dirPath) {
				return null;
			}
			
			public Reader getResource(String resourceName)
					throws ResourceNotFoundException {
				
				return getResource(resourceName, false);
			}
		
			public Reader getResource(String resourceName,
					boolean processingBundle) throws ResourceNotFoundException {
				
				StringBuffer content = new StringBuffer();
				if(resourceName.equals("/temp.less")){
					try {
						content.append(FileUtils.readClassPathFile("generator/css/less/temp.less"));
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
				return new StringReader(content.toString());
			}
			
			public InputStream getResourceAsStream(String resourceName,
					boolean processingBundle) throws ResourceNotFoundException {
				return new ByteArrayInputStream("fakeData".getBytes());
			}
			
			public InputStream getResourceAsStream(String resourceName)
					throws ResourceNotFoundException {
				return new ByteArrayInputStream("fakeData".getBytes());
			}
			
			public void addResourceReaderToStart(ResourceReader rd) {
				
			}
			
			public void addResourceReaderToEnd(ResourceReader rd) {
				
			}

			@Override
			public Reader getResource(String resourceName,
					boolean processingBundle, List<Class<?>> excludedReader)
					throws ResourceNotFoundException {
				return getResource(resourceName);
			}
		};
	}
}
