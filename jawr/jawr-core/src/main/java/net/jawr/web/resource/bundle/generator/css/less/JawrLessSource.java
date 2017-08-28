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
package net.jawr.web.resource.bundle.generator.css.less;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.LessSource.StringSource;

import net.jawr.web.exception.BundlingProcessException;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.bundle.IOUtils;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.factory.util.PathNormalizer;
import net.jawr.web.resource.bundle.mappings.FilePathMapping;
import net.jawr.web.resource.bundle.mappings.FilePathMappingUtils;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;

/**
 * The Jawr implementation of LessSource.
 * 
 * @author Ibrahim Chaehoi
 */
public class JawrLessSource extends StringSource {

	/** The resource reader handler */
	private ResourceReaderHandler rsReaderHandler;

	/** The resource bundle */
	private JoinableResourceBundle bundle;

	/** The parent less resource */
	private JawrLessSource parent;

	/** The linked resources */
	private List<FilePathMapping> linkedResources;

	/**
	 * Constructor
	 * 
	 * @param bundle
	 *            the bundle
	 * @param content
	 *            the content
	 * @param name
	 *            the resource name
	 * @param rsReaderHandler
	 */
	public JawrLessSource(JoinableResourceBundle bundle, String content, String name,
			ResourceReaderHandler rsReaderHandler) {
		this(bundle, content, name, null, rsReaderHandler);
	}

	/**
	 * Constructor
	 * 
	 * @param bundle
	 *            the bundle
	 * @param content
	 *            the content
	 * @param name
	 *            the resource name
	 * @param parent
	 *            the parent less source file
	 * @param rsReaderHandler
	 *            the resource reader handler
	 */
	public JawrLessSource(JoinableResourceBundle bundle, String content, String name, JawrLessSource parent,
			ResourceReaderHandler rsReaderHandler) {
		super(content, name);
		this.bundle = bundle;
		this.parent = parent;
		this.rsReaderHandler = rsReaderHandler;

		this.linkedResources = new ArrayList<>();
		FilePathMapping fMapping = FilePathMappingUtils.buildFilePathMapping(name, this.rsReaderHandler);
		if (fMapping != null) {
			linkedResources.add(fMapping);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.github.sommeri.less4j.LessSource.StringSource#relativeSource(java.
	 * lang.String)
	 */
	@Override
	public LessSource relativeSource(String resource) throws StringSourceException {

		String result = null;
		if (!resource.startsWith("/")) { // relative URL
			resource = PathNormalizer.concatWebPath(getName(), resource);
		}
		try (Reader rd = getResourceReader(resource)) {
			result = IOUtils.toString(rd);
			FilePathMapping linkedResource = FilePathMappingUtils.buildFilePathMapping(resource, rsReaderHandler);
			if (linkedResource != null) {
				addLinkedResource(linkedResource);
				if (bundle != null) {
					bundle.getFilePathMappings().add(
							new FilePathMapping(bundle, linkedResource.getPath(), linkedResource.getLastModified()));
				}
			}

		} catch (ResourceNotFoundException | IOException e) {
			throw new BundlingProcessException(e);
		}

		return new JawrLessSource(bundle, result, resource, this, rsReaderHandler);
	}

	/**
	 * Adds a linked resource to the less source
	 * 
	 * @param linkedResource
	 *            the linked resource to add
	 */
	private void addLinkedResource(FilePathMapping linkedResource) {
		linkedResources.add(linkedResource);
		if (parent != null) {
			parent.addLinkedResource(linkedResource);
		}
	}

	/**
	 * Returns the resource reader
	 * 
	 * @param resource
	 *            the resource
	 * @return the resource reader
	 * @throws ResourceNotFoundException
	 *             if the resource is not found
	 */
	private Reader getResourceReader(String resource) throws ResourceNotFoundException {
		List<Class<?>> excluded = new ArrayList<>();
		excluded.add(ILessCssResourceGenerator.class);
		return rsReaderHandler.getResource(bundle, resource, false, excluded);
	}

	/**
	 * Returns the linked resources
	 * 
	 * @return the linked resources
	 */
	public List<FilePathMapping> getLinkedResources() {
		return linkedResources;
	}
}
