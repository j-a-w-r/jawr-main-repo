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
package test.net.jawr.web.resource.bundle.generator.classpath;

import java.io.File;
import java.io.Reader;
import java.util.Properties;

import javax.servlet.ServletContext;

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

import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.resource.bundle.IOUtils;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.generator.GeneratorContext;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.generator.classpath.ClasspathJSGenerator;
import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;
import test.net.jawr.web.FileUtils;
import test.net.jawr.web.servlet.mock.MockServletContext;

/**
 *
 * @author Ibrahim Chaehoi
 */
@RunWith(MockitoJUnitRunner.class)
public class ClassPathJsGeneratorTestCase {

	private static final String WORK_DIR = "workDirClasspathJs";

	private JawrConfig config;
	private GeneratorContext ctx;
	private ClasspathJSGenerator generator;

	@Mock
	private ResourceReaderHandler rsReaderHandler;

	@Mock
	private ResourceBundlesHandler jsBundleHandler;

	@Mock
	private GeneratorRegistry generatorRegistry;

	@Mock
	private JoinableResourceBundle bundle;

	@Before
	public void setUp() throws Exception {

		FileUtils.clearDirectory(FileUtils.getClasspathRootDir() + "/" + WORK_DIR);
		FileUtils.createDir(WORK_DIR);

		Properties props = new Properties();
		config = new JawrConfig(JawrConstant.JS_TYPE, props);
		ServletContext servletContext = new MockServletContext();

		servletContext.setAttribute(JawrConstant.CSS_CONTEXT_ATTRIBUTE, jsBundleHandler);
		config.setContext(servletContext);
		config.setServletMapping("/jawr/js");
		config.setCharsetName("UTF-8");

		config.setGeneratorRegistry(generatorRegistry);

		generator = new ClasspathJSGenerator();

		Mockito.doAnswer(new Answer<Long>() {
			@Override
			public Long answer(InvocationOnMock invocation) throws Throwable {
				File f = new File((String) invocation.getArguments()[0]);
				return f.lastModified();
			}
		}).when(rsReaderHandler).getLastModified(Matchers.anyString());

		generator.setWorkingDirectory(FileUtils.getClasspathRootDir() + "/" + WORK_DIR);
		generator.setResourceReaderHandler(rsReaderHandler);
		generator.setConfig(config);
		generator.afterPropertiesSet();
		
		FileUtils.copyFile("generator/classpath/js/temp.js.backup", "generator/classpath/js/temp.js");
	}

	@After
	public void tearDown() throws Exception {
		FileUtils.deleteDirectory(FileUtils.getClasspathRootDir() + "/" + WORK_DIR);
		FileUtils.copyFile("generator/classpath/js/temp.js.backup", "generator/classpath/js/temp.js");
	}

	@Test
	public void testJsBundleGenerator() throws Exception {

		ctx = new GeneratorContext(bundle, config, "/generator/classpath/js/temp.js");
		ctx.setResourceReaderHandler(rsReaderHandler);

		// Check result in Production mode
		ctx.setProcessingBundle(true);
		Reader rd = generator.createResource(ctx);
		String result = FileUtils.removeCarriageReturn(IOUtils.toString(rd));
		Assert.assertEquals(FileUtils.readClassPathFile("generator/classpath/js/temp_expected.js"), result);

		// Check retrieve from cache in Production mode
		rd = generator.createResource(ctx);
		result = FileUtils.removeCarriageReturn(IOUtils.toString(rd));
		Assert.assertEquals(FileUtils.readClassPathFile("generator/classpath/js/temp_expected.js"), result);

		// Check result in debug mode
		ctx.setProcessingBundle(false);
		rd = generator.createResource(ctx);
		result = FileUtils.removeCarriageReturn(IOUtils.toString(rd));
		Assert.assertEquals(FileUtils.readClassPathFile("generator/classpath/js/temp_expected.js"), result);

		FileUtils.copyFile("generator/classpath/js/temp2.js", "generator/classpath/js/temp2.js");

		// Check updated result in Production mode
		ctx.setProcessingBundle(true);
		rd = generator.createResource(ctx);
		result = FileUtils.removeCarriageReturn(IOUtils.toString(rd));
		Assert.assertEquals(FileUtils.readClassPathFile("generator/classpath/js/temp_updated_expected.js"), result);

		// Check retrieve from cache in Production mode
		rd = generator.createResource(ctx);
		result = FileUtils.removeCarriageReturn(IOUtils.toString(rd));
		Assert.assertEquals(FileUtils.readClassPathFile("generator/classpath/js/temp_updated_expected.js"), result);

		// Check updated result in debug mode
		ctx.setProcessingBundle(false);
		rd = generator.createResource(ctx);
		result = FileUtils.removeCarriageReturn(IOUtils.toString(rd));
		Assert.assertEquals(FileUtils.readClassPathFile("generator/classpath/js/temp_updated_expected.js"), result);

	}

}
