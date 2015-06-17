/**
 * Copyright 2014 Ibrahim Chaehoi
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
package test.net.jawr.web.resource.bundle.generator.classpath;

import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Properties;

import javax.servlet.ServletContext;

import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.resource.BinaryResourcesHandler;
import net.jawr.web.resource.bundle.IOUtils;
import net.jawr.web.resource.bundle.generator.GeneratorContext;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.generator.TextResourceGenerator;
import net.jawr.web.resource.bundle.generator.classpath.ClassPathCSSGenerator;
import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import test.net.jawr.web.FileUtils;
import test.net.jawr.web.servlet.mock.MockServletContext;

/**
 *
 * @author Ibrahim Chaehoi
 */
@RunWith(MockitoJUnitRunner.class)
public class ClassPathCssGeneratorTestCase {

private static final String WORK_DIR = "workDirClasspathCss";
	
	private JawrConfig config;
	private GeneratorContext ctx;
	private ClassPathCSSGenerator generator;

	@Mock
	private ResourceReaderHandler rsReaderHandler;
	
	@Mock
	private ResourceReaderHandler binaryRsReaderHandler;
	
	@Mock
	private ResourceBundlesHandler cssBundleHandler;
	
	@Mock
	private GeneratorRegistry generatorRegistry;
	
	@Before
	public void setUp() throws Exception {
		
		FileUtils.clearDirectory(FileUtils.getClasspathRootDir()+"/"+WORK_DIR);
		FileUtils.createDir(WORK_DIR);
		
		Properties props = new Properties();
		props.put("jawr.css.classpath.handle.image", "true");
		config = new JawrConfig(JawrConstant.CSS_TYPE, props);
		ServletContext servletContext = new MockServletContext();
		
		servletContext.setAttribute(JawrConstant.CSS_CONTEXT_ATTRIBUTE, cssBundleHandler);
		config.setContext(servletContext);
		config.setServletMapping("/jawr/css");
		config.setCharsetName("UTF-8");
		
		config.setGeneratorRegistry(generatorRegistry);
		
		generator = new ClassPathCSSGenerator();
		
		// Set up the Image servlet Jawr config
		JawrConfig binaryServletJawrConfig = new JawrConfig(JawrConstant.BINARY_TYPE, new Properties());
		binaryServletJawrConfig.setGeneratorRegistry(generatorRegistry);
		
		// Return a new ByteArrayInputStream each time where calling  getResourceAsStream
		Mockito.doAnswer(new Answer<InputStream>() {

			@Override
			public InputStream answer(InvocationOnMock invocation) throws Throwable {
				return new ByteArrayInputStream("fakeData".getBytes());
			}
		}).when(binaryRsReaderHandler).getResourceAsStream(Matchers.anyString());
		
		
		BinaryResourcesHandler binaryRsHandler = new BinaryResourcesHandler(binaryServletJawrConfig, binaryRsReaderHandler, null);
		servletContext.setAttribute(JawrConstant.BINARY_CONTEXT_ATTRIBUTE, binaryRsHandler);
		generator.setConfig(config);
		
		when(generatorRegistry.isHandlingCssImage(Matchers.anyString())).thenReturn(true);
		when(generatorRegistry.isGeneratedBinaryResource(Matchers.startsWith("jar:"))).thenReturn(true);
		
		generator.setWorkingDirectory(FileUtils.getClasspathRootDir()+"/"+WORK_DIR);
	}
	
	@After
	public void tearDown() throws Exception{
		FileUtils.deleteDirectory(FileUtils.getClasspathRootDir()+"/"+WORK_DIR);
	}
	
	@Test
	public void testCssBundleGenerator() throws Exception{
		
		ctx = new GeneratorContext(config, "/generator/classpath/temp.css");
		ctx.setResourceReaderHandler(rsReaderHandler);
		
		// Check result in Production mode
		ctx.setProcessingBundle(true);
		Reader rd = generator.createResource(ctx);
		String result = FileUtils.removeCarriageReturn(IOUtils.toString(rd));
		Assert.assertEquals(FileUtils.readClassPathFile("generator/classpath/expected/style_expected.css"), result);
		
		// Check result in debug mode
		ctx.setProcessingBundle(false);
		rd = generator.createResource(ctx);
		result = FileUtils.removeCarriageReturn(IOUtils.toString(rd));
		Assert.assertEquals(FileUtils.readClassPathFile("generator/classpath/expected/style_debug_expected.css"), result);
		
	}
	
	@Test
	public void testClasspathGeneratorWithLessBundle() throws Exception{
		
		ctx = new GeneratorContext(config, "/generator/classpath/temp.less");
		ctx.setResourceReaderHandler(rsReaderHandler);
		Reader strReader = new StringReader(FileUtils.readClassPathFile("generator/classpath/temp.css"));
		when(generatorRegistry.isPathGenerated("/generator/classpath/temp.less")).thenReturn(true);
		TextResourceGenerator lessGenerator = Mockito.mock(TextResourceGenerator.class);
		when(lessGenerator.createResource(Matchers.any(GeneratorContext.class))).thenReturn(strReader);
		when(generatorRegistry.getResourceGenerator("/generator/classpath/temp.less")).thenReturn(lessGenerator);
		
		// Check result in Production mode
		ctx.setProcessingBundle(true);
		Reader rd = generator.createResource(ctx);
		String result = FileUtils.removeCarriageReturn(IOUtils.toString(rd));
		Assert.assertEquals(FileUtils.readClassPathFile("generator/classpath/expected/style_expected.css"), result);
		
		// Check result in debug mode
		ctx.setProcessingBundle(false);
		rd = generator.createResource(ctx);
		result = FileUtils.removeCarriageReturn(IOUtils.toString(rd));
		Assert.assertEquals(FileUtils.readClassPathFile("generator/classpath/expected/style_debug_expected.css"), result);
		
	}
	
	@Test
	public void testCssBundleGeneratorWithImageReferenceInSubDir() throws Exception{
		
		ctx = new GeneratorContext(config, "/generator/classpath/css/temp_in_subdir.css");
		ctx.setResourceReaderHandler(rsReaderHandler);
		
		// Check result in Production mode
		ctx.setProcessingBundle(true);
		Reader rd = generator.createResource(ctx);
		String result = FileUtils.removeCarriageReturn(IOUtils.toString(rd));
		Assert.assertEquals(FileUtils.readClassPathFile("generator/classpath/expected/style_in_subdir_expected.css"), result);
		
		// Check result in debug mode
		ctx.setProcessingBundle(false);
		rd = generator.createResource(ctx);
		result = FileUtils.removeCarriageReturn(IOUtils.toString(rd));
		Assert.assertEquals(FileUtils.readClassPathFile("generator/classpath/expected/style_in_subdir_debug_expected.css"), result);
		
	}

}
