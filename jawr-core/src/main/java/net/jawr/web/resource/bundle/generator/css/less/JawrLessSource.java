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
import net.jawr.web.resource.bundle.factory.util.PathNormalizer;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;

/**
 * The Jawr implementation of LessSource.
 * 
 * @author Ibrahim Chaehoi
 */
public class JawrLessSource extends StringSource {

	/** The resource reader handler */
	private ResourceReaderHandler rsReaderHandler;
	
	/**
	 * Constructor
	 * 
	 * @param content
	 *            the content
	 * @param name
	 *            the resource name
	 */
	public JawrLessSource(String content, String name, ResourceReaderHandler rsReaderHandler) {
		super(content, name);
		this.rsReaderHandler = rsReaderHandler;
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
		try {
			Reader rd = getResourceReader(resource);
			result = IOUtils.toString(rd);
		} catch (ResourceNotFoundException e) {
			throw new BundlingProcessException(e);
		} catch (IOException e) {
			throw new BundlingProcessException(e);
		}
		
		return new JawrLessSource(result, resource, rsReaderHandler);
	}

	/**
	 * Returns the resource reader
	 * 
	 * @param resource
	 *            the resource
	 * @return the resource reader
	 * @throws ResourceNotFoundException
	 *             if the resoure is not found
	 */
	private Reader getResourceReader(String resource) throws ResourceNotFoundException {
		List<Class<?>> excluded = new ArrayList<Class<?>>();
		excluded.add(ILessCssResourceGenerator.class);
		return rsReaderHandler.getResource(resource, false, excluded);
	}
}
