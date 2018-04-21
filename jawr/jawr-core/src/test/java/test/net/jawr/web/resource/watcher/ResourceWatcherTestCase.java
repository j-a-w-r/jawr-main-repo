/**
 * Copyright 2015 Ibrahim Chaehoi
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
package test.net.jawr.web.resource.watcher;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;
import net.jawr.web.resource.bundle.mappings.PathMapping;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;
import net.jawr.web.resource.watcher.ResourceWatcher;
import net.jawr.web.util.StringUtils;
import test.net.jawr.web.FileUtils;

/**
 * 
 * @author Ibrahim Chaehoi
 */
@RunWith(MockitoJUnitRunner.class)
public class ResourceWatcherTestCase {

	private final int[] invocationCount = new int[1];
	
	private ResourceWatcher watcher;

	@Mock
	private ResourceBundlesHandler bundlesHandler;

	@Mock
	private ResourceReaderHandler rsReader;

	@Mock
	private JoinableResourceBundle b;

	private AtomicBoolean processingBundle;
	
	private int waitTime = 500;

	/**
	 * Set up
	 */
	@Before
	public void setUp() {

		// These tests are failing intermittently on travis CI
		// Make sure we are not on travis for these tests.
		String travisFlag = System.getenv("TRAVIS");
		Assume.assumeTrue(StringUtils.isEmpty(travisFlag));
		
		JawrConfig config = new JawrConfig(JawrConstant.JS_TYPE, new Properties());
		GeneratorRegistry registry = new GeneratorRegistry(JawrConstant.JS_TYPE);
		registry.setConfig(config);
		config.setGeneratorRegistry(registry);
		
		when(b.getId()).thenReturn("/js/bundle1.js");
		when(b.getName()).thenReturn("bundle1");

		when(bundlesHandler.getConfig()).thenReturn(config);
		when(bundlesHandler.getGlobalBundles()).thenReturn(new ArrayList<JoinableResourceBundle>());
		when(bundlesHandler.getContextBundles()).thenReturn(Arrays.asList(b));
		when(bundlesHandler.getResourceType()).thenReturn(JawrConstant.JS_TYPE);
		processingBundle = new AtomicBoolean(false);
		when(bundlesHandler.isProcessingBundle()).thenReturn(processingBundle);
		
		invocationCount[0] = 0;
		
		doAnswer(new Answer<Object>() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				invocationCount[0]++;
				return null;
			}
		}).when(bundlesHandler).notifyModification(Matchers.eq(Arrays.asList(b)));

	
	}

	@After
	public void tearDown() {
		if(watcher != null){
			watcher.stopWatching();
			watcher.interrupt();
		}
	}

	/**
	 * Sets the bundle mapping
	 */
	private void setBundleMapping(String mapping) {
		when(b.getMappings()).thenReturn(Arrays.asList(new PathMapping(b, mapping)));
	}

	/**
	 * Create or modify a file
	 * 
	 * @param dir
	 *            the directory where to create the file
	 * @param fileName
	 *            the name of the file to create
	 * @throws IOException
	 *             if an IOException occurs
	 */
	private void createOrModifyFile(File dir, String fileName) throws IOException {
		createOrModifyFile(dir.getAbsolutePath() + fileName);
	}

	/**
	 * Create or modify a file
	 * 
	 * @param path
	 *            the file path
	 * @throws IOException
	 *             if an IOException occurs
	 */
	private void createOrModifyFile(String path) throws IOException {
		FileWriter fw = new FileWriter(new File(path));
		fw.write("function message(msg){ alert(msg);};");
		fw.close();
	}

	/**
	 * Create or modify a file
	 * 
	 * @param file
	 *            the file to create or modify
	 * @throws IOException
	 *             if an IOException occurs
	 */
	private void createOrModifyFile(File f) throws IOException {
		FileWriter fw = new FileWriter(f);
		fw.write("function message(msg){ alert(msg);};");
		fw.close();
	}

	/**
	 * Deletes the file in parameter or fails if it's not able to
	 * 
	 * @param path
	 *            the file path
	 */
	private void deleteFileIfExists(File f) {
		if (f.exists()) {
			if (!f.delete()) {
				fail("Impossible to delete the file '" + f.getAbsolutePath() + "'");
			}
		}
	}

	/**
	 * Deletes the file in parameter or fails if it's not able to
	 * 
	 * @param path
	 *            the file path
	 */
	private void deleteFile(File f) {
		if (!f.delete()) {
			fail("Impossible to delete the file '" + f.getAbsolutePath() + "'");
		}
	}

	@Test
	public void testModifyOneAssetMapping() throws Exception {

		setBundleMapping("/js/lib/init.js");

		File f = FileUtils.getClassPathFile("watcher/js/lib/init.js");
		String path = f.getAbsolutePath();
		when(rsReader.getFilePath("/js/lib/init.js")).thenReturn(path);

		initWatcher();
		
		watcher.start();

		// Modify the file
		createOrModifyFile(f);

		// Wait a little bit
		Thread.sleep(waitTime);
		watcher.stopWatching();
		verify(bundlesHandler, atLeastOnce()).notifyModification(Matchers.eq(Arrays.asList(b)));
	}

	/**
	 * Initialize the resource watcher
	 * 
	 * @throws IOException if an IOException occurs 
	 */
	protected void initWatcher() throws IOException {
		watcher = new ResourceWatcher(bundlesHandler, rsReader);
		watcher.initPathToResourceBundleMap(bundlesHandler.getContextBundles());
	}

	@Test
	public void testCreateOneAssetMapping() throws Exception {

		setBundleMapping("/js/lib/scriptToBeCreated.js");

		// The file to create will be in the same directory as init.js
		File dir = FileUtils.getClassPathFile("watcher/js/lib/init.js").getParentFile();
		File f = new File(dir, "scriptToBeCreated.js");

		// Ensure that the file doesn't exist before starting the watcher
		deleteFileIfExists(f);

		String path = f.getAbsolutePath();
		when(rsReader.getFilePath("/js/lib/scriptToBeCreated.js")).thenReturn(path);

		initWatcher();
		watcher.start();

		// Create a new file in the same directory
		createOrModifyFile(f);

		// Wait a little bit
		Thread.sleep(waitTime);
		watcher.stopWatching();
		verify(bundlesHandler, atLeastOnce()).notifyModification(Matchers.eq(Arrays.asList(b)));
	}

	@Test
	public void testDeleteOneAssetMapping() throws Exception {

		setBundleMapping("/js/lib/scriptToBeDeleted.js");

		File f = new File(FileUtils.getClasspathRootDir(), "watcher/js/lib/scriptToBeDeleted.js");
		String path = f.getAbsolutePath();

		// Ensure that the file to delete exists before starting the watcher
		if (!f.exists()) {
			createOrModifyFile(f);
		}

		when(rsReader.getFilePath("/js/lib/scriptToBeDeleted.js")).thenReturn(path);

		initWatcher();
		watcher.start();

		// Delete the file
		deleteFile(f);

		// Wait a little bit
		Thread.sleep(waitTime);
		watcher.stopWatching();
		verify(bundlesHandler, atLeastOnce()).notifyModification(Matchers.eq(Arrays.asList(b)));
	}

	@Test
	public void testModifyAssetNotDefineInBundle() throws Exception {

		setBundleMapping("/js/lib/init.js");

		File f = FileUtils.getClassPathFile("watcher/js/lib/init.js");
		String path = f.getAbsolutePath();
		when(rsReader.getFilePath("/js/lib/init.js")).thenReturn(path);

		// Ensure that the file to create doesn't exist before starting the
		// watcher
		File fToCreate = new File(f.getParent() + "/init2.js");
		deleteFileIfExists(fToCreate);

		initWatcher();
		watcher.start();

		// Create a new file in the same directory
		createOrModifyFile(fToCreate);

		// Wait a little bit
		Thread.sleep(waitTime);
		watcher.stopWatching();
		verify(bundlesHandler, never()).notifyModification(Matchers.eq(Arrays.asList(b)));
	}

	@Test
	public void testModifyOneAssetInDirMapping() throws Exception {

		setBundleMapping("/js/lib/chart/");

		File f = FileUtils.getClassPathFile("watcher/js/lib/chart/");
		String path = f.getAbsolutePath();
		when(rsReader.getFilePath("/js/lib/chart/")).thenReturn(path);

		initWatcher();
		watcher.start();

		// Modify the file
		createOrModifyFile(new File(f, "chart.js"));

		// Wait a little bit
		Thread.sleep(waitTime);
		watcher.stopWatching();
		verify(bundlesHandler, atLeastOnce()).notifyModification(Matchers.eq(Arrays.asList(b)));
	}

	@Test
	public void testCreateOneAssetInDirMapping() throws Exception {

		setBundleMapping("/js/lib/chart/");

		File f = FileUtils.getClassPathFile("watcher/js/lib/chart/");
		String path = f.getAbsolutePath();
		when(rsReader.getFilePath("/js/lib/chart/")).thenReturn(path);

		// Ensure that the file to create doesn't exist before starting the
		// watcher
		File fToCreate = new File(f, "scriptToBeCreated.js");
		deleteFileIfExists(fToCreate);

		initWatcher();

		watcher.start();

		// Create a file
		createOrModifyFile(fToCreate);

		// Wait a little bit
		Thread.sleep(waitTime);
		watcher.stopWatching();
		verify(bundlesHandler, atLeastOnce()).notifyModification(Matchers.eq(Arrays.asList(b)));
	}

	@Test
	public void testCreateOneDirectoryInDirMapping() throws Exception {

		setBundleMapping("/js/lib/chart/");

		File f = FileUtils.getClassPathFile("watcher/js/lib/chart/");
		String path = f.getAbsolutePath();
		when(rsReader.getFilePath("/js/lib/chart/")).thenReturn(path);

		// Ensure that the file to create doesn't exist before starting the
		// watcher
		File dirToCreate = new File(f, "tempDir");
		deleteFileIfExists(dirToCreate);

		initWatcher();
		watcher.start();

		// Create a file
		if (!dirToCreate.mkdirs()) {
			fail("Impossible to create dir '" + dirToCreate.getAbsolutePath() + "'");
		}

		// Wait a little bit
		Thread.sleep(waitTime);
		watcher.stopWatching();
		verify(bundlesHandler, never()).notifyModification(Matchers.eq(Arrays.asList(b)));
	}

	@Test
	public void testDeleteOneAssetInDirMapping() throws Exception {

		setBundleMapping("/js/lib/chart/");

		File f = FileUtils.getClassPathFile("watcher/js/lib/chart/");
		String path = f.getAbsolutePath();
		when(rsReader.getFilePath("/js/lib/chart/")).thenReturn(path);

		// Ensure that the file to delete exists before starting the watcher
		File fToDelete = new File(f, "scriptToBeDeleted1.js");
		createOrModifyFile(fToDelete);

		initWatcher();
		watcher.start();

		// Delete a file
		deleteFile(fToDelete);

		// Wait a little bit
		Thread.sleep(waitTime);
		watcher.stopWatching();
		verify(bundlesHandler, atLeastOnce()).notifyModification(Matchers.eq(Arrays.asList(b)));
	}

	@Test
	public void testModifyOneAssetInSubDirNotMapped() throws Exception {
		setBundleMapping("/js/lib/chart/");

		File f = FileUtils.getClassPathFile("watcher/js/lib/chart/");
		String path = f.getAbsolutePath();
		when(rsReader.getFilePath("/js/lib/chart/")).thenReturn(path);

		initWatcher();
		watcher.start();

		// Modify the file
		FileWriter fw = new FileWriter(new File(f, "diagram/diagram.js"));
		fw.write("function message(msg){ alert(msg);};");
		fw.close();

		// Wait a little bit
		Thread.sleep(waitTime);
		watcher.stopWatching();
		verify(bundlesHandler, never()).notifyModification(Matchers.eq(Arrays.asList(b)));
	}

	@Test
	public void testCreateOneAssetInSubDirNotMapped() throws Exception {

		setBundleMapping("/js/lib/chart/");

		File f = FileUtils.getClassPathFile("watcher/js/lib/chart/");
		String path = f.getAbsolutePath();

		when(rsReader.getFilePath("/js/lib/chart/")).thenReturn(path);

		// Ensure that the file to create doesn't exist before starting the
		// watcher
		File fToCreate = new File(f, "diagram/scriptToBeCreated1.js");
		deleteFileIfExists(fToCreate);

		initWatcher();
		watcher.start();

		// Create the file
		createOrModifyFile(fToCreate);

		// Wait a little bit
		Thread.sleep(waitTime);
		watcher.stopWatching();
		verify(bundlesHandler, never()).notifyModification(Matchers.eq(Arrays.asList(b)));
	}

	@Test
	public void testDeleteOneAssetInSubDirNotMapped() throws Exception {

		setBundleMapping("/js/lib/chart/");

		File f = FileUtils.getClassPathFile("watcher/js/lib/chart/");
		String path = f.getAbsolutePath();
		when(rsReader.getFilePath("/js/lib/chart/")).thenReturn(path);

		// Ensure that the file to delete exists before starting the watcher
		File fToDelete = new File(f, "diagram/scriptToBeDeleted1.js");
		createOrModifyFile(fToDelete);

		initWatcher();
		watcher.start();

		// Delete a file
		deleteFile(fToDelete);

		// Wait a little bit
		Thread.sleep(waitTime);
		watcher.stopWatching();
		verify(bundlesHandler, never()).notifyModification(Matchers.eq(Arrays.asList(b)));
	}

	// Recursive directory
	@Test
	public void testModifyOneAssetInRecursiveDir() throws Exception {

		setBundleMapping("/js/lib/chart/**");

		File f = FileUtils.getClassPathFile("watcher/js/lib/chart/");
		String path = f.getAbsolutePath();
		when(rsReader.getFilePath("/js/lib/chart/")).thenReturn(path);

		initWatcher();
		watcher.start();

		// Modify the file
		createOrModifyFile(f, "chart.js");

		// Wait a little bit
		Thread.sleep(waitTime);
		watcher.stopWatching();
		verify(bundlesHandler, never()).notifyModification(Matchers.eq(Arrays.asList(b)));
	}

	@Test
	public void testCreateOneAssetInRecursiveDirMapping() throws Exception {

		setBundleMapping("/js/lib/chart/**");

		File f = FileUtils.getClassPathFile("watcher/js/lib/chart/");
		String path = f.getAbsolutePath();
		when(rsReader.getFilePath("/js/lib/chart/")).thenReturn(path);

		// Ensures that the file to create doesn't exist before starting the
		// watcher
		File fToCreate = new File(f, "scriptToBeCreated1.js");
		deleteFileIfExists(fToCreate);
		File fToCreate2 = new File(f, "diagram/scriptToBeCreated1.js");
		deleteFileIfExists(fToCreate2);

		initWatcher();
		watcher.start();

		// Create one file
		createOrModifyFile(fToCreate);

		// Wait a little bit
		Thread.sleep(waitTime);
		
		int nb = invocationCount[0];
		assertTrue(nb > 0);
		
		// Create one file in a subDir
		createOrModifyFile(fToCreate2);

		// Wait a little bit
		Thread.sleep(waitTime);
		assertTrue(invocationCount[0] > nb);
	}

	@Test
	public void testCreateOneDirectoryInRecursiveMapping() throws Exception {

		setBundleMapping("/js/lib/chart/**");

		File f = FileUtils.getClassPathFile("watcher/js/lib/chart/");
		String path = f.getAbsolutePath();
		when(rsReader.getFilePath("/js/lib/chart/")).thenReturn(path);

		// Ensure that the file to create doesn't exist before starting the
		// watcher
		File dirToCreate = new File(f, "tempDir/");
		deleteFileIfExists(dirToCreate);

		File dirToCreate2 = new File(f, "diagram/tempDir/");
		deleteFileIfExists(dirToCreate2);

		initWatcher();
		watcher.start();

		// Create a directory
		if (!dirToCreate.mkdirs()) {
			fail("Impossible to create dir '" + dirToCreate.getAbsolutePath() + "'");
		}

		// Wait a little bit
		Thread.sleep(waitTime);
		int nb = invocationCount[0];
		assertTrue(nb > 0);
		
		// Create a directory in subdir
		if (!dirToCreate2.mkdirs()) {
			fail("Impossible to create dir '" + dirToCreate.getAbsolutePath() + "'");
		}

		// Wait a little bit
		Thread.sleep(waitTime);
		assertTrue(invocationCount[0] > nb);
		
		watcher.stopWatching();
	}

	@Test
	public void testDeleteOneAssetInRecursiveDirMapping() throws Exception {

		setBundleMapping("/js/lib/chart/**");

		File f = FileUtils.getClassPathFile("watcher/js/lib/chart/");
		String path = f.getAbsolutePath();
		when(rsReader.getFilePath("/js/lib/chart/")).thenReturn(path);

		// Ensure that the file to delete exists before starting the watcher
		File fToDelete = new File(f, "scriptToBeDeleted2.js");
		createOrModifyFile(fToDelete);

		File fToDelete2 = new File(f, "/diagram/scriptToBeDeleted2.js");
		createOrModifyFile(fToDelete2);

		initWatcher();
		watcher.start();

		// Delete a file
		deleteFile(fToDelete);

		// Wait a little bit
		Thread.sleep(waitTime);

		int nb = invocationCount[0];
		assertTrue(nb > 0);
		
		// Delete a file
		deleteFile(fToDelete2);

		// Wait a little bit
		Thread.sleep(waitTime);
		assertTrue(invocationCount[0] > nb);
		watcher.stopWatching();
		watcher.interrupt();
	}

	@Test
	public void testModifyOneAssetInSubRecursiveDirNotMapped() throws Exception {

		setBundleMapping("/js/lib/chart/**");

		File f = FileUtils.getClassPathFile("watcher/js/lib/chart/");
		String path = f.getAbsolutePath();
		when(rsReader.getFilePath("/js/lib/chart/")).thenReturn(path);

		// Ensure that the file exists before starting the watcher
		File fToModify = new File(f.getParentFile(), "vertex/cube/cube.js");
		createOrModifyFile(fToModify);

		// Wait a little bit
		Thread.sleep(waitTime);
	
		initWatcher();
		watcher.start();

		// Modify the file
		createOrModifyFile(fToModify);

		// Wait a little bit
		Thread.sleep(waitTime);
		watcher.stopWatching();
		
		verify(bundlesHandler, never()).notifyModification(Matchers.eq(Arrays.asList(b)));
	}

	@Test
	public void testCreateOneAssetInSubRecursiveDirNotMapped() throws Exception {

		setBundleMapping("/js/lib/chart/**");

		File f = FileUtils.getClassPathFile("watcher/js/lib/chart/");
		String path = f.getAbsolutePath();
		when(rsReader.getFilePath("/js/lib/chart/")).thenReturn(path);

		// Ensure that the file to create doesn't exist before starting the
		// watcher
		File fToCreate = new File(f.getParentFile(), "/vertex/scriptToBeCreated.js");

		initWatcher();
		watcher.start();

		// Create the file
		createOrModifyFile(fToCreate);

		// Wait a little bit
		Thread.sleep(waitTime);
		watcher.stopWatching();
		verify(bundlesHandler, never()).notifyModification(Matchers.eq(Arrays.asList(b)));
	}

	@Test
	public void testCreateOneDirectoryInSubDirRecursiveMapping() throws Exception {

		setBundleMapping("/js/lib/chart/**");

		File f = FileUtils.getClassPathFile("watcher/js/lib/chart/");
		String path = f.getAbsolutePath();
		when(rsReader.getFilePath("/js/lib/chart/")).thenReturn(path);

		// Ensure that the file to create doesn't exist before starting the
		// watcher
		File dirToCreate = new File(f.getAbsolutePath() + "/diagram/tempDir/");
		deleteFileIfExists(dirToCreate);

		initWatcher();
		watcher.start();

		// Create a file
		if (!dirToCreate.mkdirs()) {
			fail("Impossible to create dir '" + dirToCreate.getAbsolutePath() + "'");
		}

		// Wait a little bit
		Thread.sleep(waitTime);
		watcher.stopWatching();
		verify(bundlesHandler, atLeastOnce()).notifyModification(Matchers.eq(Arrays.asList(b)));
	}

	@Test
	public void testDeleteOneAssetInSubRecursiveDirNotMapped() throws Exception {
	
		setBundleMapping("/js/lib/chart/**");
		
		File f = FileUtils.getClassPathFile("watcher/js/lib/chart/");
		String path = f.getAbsolutePath();
		when(rsReader.getFilePath("/js/lib/chart/")).thenReturn(path);

		// Ensure that the file to delete exists before starting the watcher
		File fToDelete = new File(f.getParentFile(), "vertex/cube/scriptToBeDeleted.js");
		createOrModifyFile(fToDelete);

		initWatcher();
		watcher.start();

		// Delete a file
		deleteFile(fToDelete);

		// Wait a little bit
		Thread.sleep(waitTime);
		watcher.stopWatching();
	
		verify(bundlesHandler, never()).notifyModification(Matchers.eq(Arrays.asList(b)));
	}
	
	@Test
	public void testModifyOneAssetMappingWhileProcessingBundle() throws Exception {

		setBundleMapping("/js/lib/init.js");

		File f = FileUtils.getClassPathFile("watcher/js/lib/init.js");
		String path = f.getAbsolutePath();
		when(rsReader.getFilePath("/js/lib/init.js")).thenReturn(path);

		initWatcher();
		
		// Simulate bundle processing
		processingBundle.set(true);
		
		watcher.start();

		// Modify the file
		createOrModifyFile(f);
		// Wait a little bit
		Thread.sleep(waitTime);
		
		// No notification should be called until the end of the bundling process 
		verify(bundlesHandler, never()).notifyModification(Matchers.eq(Arrays.asList(b)));
		
		// Simulate the end of processing
		processingBundle.set(false);
		synchronized (processingBundle) {
			processingBundle.notifyAll();
		}
		
		// Wait a little bit
		Thread.sleep(waitTime);
		
		verify(bundlesHandler, atLeastOnce()).notifyModification(Matchers.eq(Arrays.asList(b)));
	}
}
