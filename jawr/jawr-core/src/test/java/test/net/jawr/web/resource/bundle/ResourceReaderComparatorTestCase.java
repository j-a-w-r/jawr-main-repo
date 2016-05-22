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
package test.net.jawr.web.resource.bundle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

import static org.mockito.Mockito.when;

import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.resource.bundle.generator.ResourceGeneratorReaderWrapper;
import net.jawr.web.resource.bundle.generator.TextResourceGenerator;
import net.jawr.web.resource.bundle.generator.resolver.PrefixedPathResolver;
import net.jawr.web.resource.bundle.generator.resolver.ResourceGeneratorResolver;
import net.jawr.web.resource.bundle.generator.resolver.SuffixedPathResolver;
import net.jawr.web.resource.bundle.global.preprocessor.css.smartsprites.CssSmartSpritesResourceReader;
import net.jawr.web.resource.handler.reader.FileSystemResourceReader;
import net.jawr.web.resource.handler.reader.ResourceReader;
import net.jawr.web.resource.handler.reader.ResourceReaderComparator;
import net.jawr.web.resource.handler.reader.ServletContextResourceReader;

/**
 * Unit tests for ResourceReaderComparator
 * 
 * @author Ibrahim Chaehoi
 */
@RunWith(MockitoJUnitRunner.class)
public class ResourceReaderComparatorTestCase {

	/** Comparator */
	private ResourceReaderComparator comparator;

	@Mock
	private JawrConfig config;

	@Mock
	private ServletContextResourceReader servletCtxResourceReader;

	@Mock
	private CssSmartSpritesResourceReader smartSpriteResourceReader;

	@Mock 
	private FileSystemResourceReader fsResourceReader;
	
	@Mock(extraInterfaces=TextResourceGenerator.class)
	private ResourceGeneratorReaderWrapper generatorReader1;

	@Mock(extraInterfaces=TextResourceGenerator.class)
	private ResourceGeneratorReaderWrapper generatorReader2;

	@Before
	public void setUp() {

		when(config.getProperty(JawrConstant.JAWR_BASECONTEXT_DIRECTORY_HIGH_PRIORITY)).thenReturn("false");
		 
		ResourceGeneratorResolver resolver1 = new PrefixedPathResolver("test1");
		ResourceGeneratorResolver resolver2 = new SuffixedPathResolver("test2");
		when(((TextResourceGenerator)generatorReader1).getResolver()).thenReturn(resolver1);
		when(((TextResourceGenerator)generatorReader2).getResolver()).thenReturn(resolver2);
		
	}

	/**
	 * Test method for
	 * {@link net.jawr.web.resource.handler.reader.ResourceReaderComparator#compare(net.jawr.web.resource.handler.reader.ResourceReader, net.jawr.web.resource.handler.reader.ResourceReader)}
	 * .
	 */
	@Test
	public void testCompare1() {

		comparator = new ResourceReaderComparator(config);
		List<ResourceReader> readers = new ArrayList<>();
		readers.add(servletCtxResourceReader);
		readers.add(smartSpriteResourceReader);
		readers.add(generatorReader1);
		readers.add(generatorReader2);

		Collections.sort(readers, comparator);

		assertEquals(
				Arrays.asList(smartSpriteResourceReader, generatorReader2, generatorReader1, servletCtxResourceReader),
				readers);
	}

	/**
	 * Test method for
	 * {@link net.jawr.web.resource.handler.reader.ResourceReaderComparator#compare(net.jawr.web.resource.handler.reader.ResourceReader, net.jawr.web.resource.handler.reader.ResourceReader)}
	 * .
	 */
	@Test
	public void testCompare2() {

		comparator = new ResourceReaderComparator(config);
		List<ResourceReader> readers = new ArrayList<>();
		readers.add(servletCtxResourceReader);
		readers.add(smartSpriteResourceReader);
		readers.add(generatorReader2);
		readers.add(generatorReader1);

		Collections.sort(readers, comparator);

		assertEquals(
				Arrays.asList(smartSpriteResourceReader, generatorReader2, generatorReader1, servletCtxResourceReader),
				readers);
	}
	
	/**
	 * Test method for
	 * {@link net.jawr.web.resource.handler.reader.ResourceReaderComparator#compare(net.jawr.web.resource.handler.reader.ResourceReader, net.jawr.web.resource.handler.reader.ResourceReader)}
	 * .
	 */
	@Test
	public void testCompare3() {

		comparator = new ResourceReaderComparator(config);
		List<ResourceReader> readers = new ArrayList<>();
		readers.add(smartSpriteResourceReader);
		readers.add(generatorReader2);
		readers.add(servletCtxResourceReader);
		readers.add(generatorReader1);
		
		Collections.sort(readers, comparator);

		assertEquals(
				Arrays.asList(smartSpriteResourceReader, generatorReader2, generatorReader1, servletCtxResourceReader),
				readers);
	}
	
	/**
	 * Test method for
	 * {@link net.jawr.web.resource.handler.reader.ResourceReaderComparator#compare(net.jawr.web.resource.handler.reader.ResourceReader, net.jawr.web.resource.handler.reader.ResourceReader)}
	 * .
	 */
	@Test
	public void testCompareWithFsResourceReaderWithLowPriority() {

		comparator = new ResourceReaderComparator(config);
		List<ResourceReader> readers = new ArrayList<>();
		readers.add(servletCtxResourceReader);
		readers.add(smartSpriteResourceReader);
		readers.add(fsResourceReader);
		readers.add(generatorReader1);
		readers.add(generatorReader2);

		Collections.sort(readers, comparator);

		assertEquals(
				Arrays.asList(smartSpriteResourceReader, generatorReader2, generatorReader1, servletCtxResourceReader, fsResourceReader),
				readers);
	}

	/**
	 * Test method for
	 * {@link net.jawr.web.resource.handler.reader.ResourceReaderComparator#compare(net.jawr.web.resource.handler.reader.ResourceReader, net.jawr.web.resource.handler.reader.ResourceReader)}
	 * .
	 */
	@Test
	public void testCompareWithFsResourceReaderWithHighPriority() {

		when(config.getProperty(JawrConstant.JAWR_BASECONTEXT_DIRECTORY_HIGH_PRIORITY)).thenReturn("true");
		comparator = new ResourceReaderComparator(config);
		List<ResourceReader> readers = new ArrayList<>();
		readers.add(servletCtxResourceReader);
		readers.add(smartSpriteResourceReader);
		readers.add(fsResourceReader);
		readers.add(generatorReader2);
		readers.add(generatorReader1);

		Collections.sort(readers, comparator);

		assertEquals(
				Arrays.asList(smartSpriteResourceReader, generatorReader2, generatorReader1, fsResourceReader, servletCtxResourceReader),
				readers);
	}
}
