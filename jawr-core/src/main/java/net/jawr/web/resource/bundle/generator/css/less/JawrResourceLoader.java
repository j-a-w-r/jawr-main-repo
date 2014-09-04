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
package net.jawr.web.resource.bundle.generator.css.less;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.bundle.IOUtils;
import net.jawr.web.resource.bundle.factory.util.PathNormalizer;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;

import com.asual.lesscss.loader.ResourceLoader;

/**
 * The Jawr Resource Loader used by the LessEngine to load resources
 *  
 * @author ibrahim Chaehoi
 */
public class JawrResourceLoader implements ResourceLoader {

	/** The resource reader handler */
	private ResourceReaderHandler rsReaderHandler;
	
	/**
	 * Constructor
	 * @param rsReaderHandler The resource reader handler
	 */
	public JawrResourceLoader(ResourceReaderHandler rsReaderHandler) {
		this.rsReaderHandler = rsReaderHandler;
	}
	
	/* (non-Javadoc)
	 * @see com.asual.lesscss.loader.ResourceLoader#exists(java.lang.String, java.lang.String[])
	 */
	@Override
	public boolean exists(String resource, String[] paths) throws IOException {
		boolean exists = false;
		Reader rd = null;
		String resourcePath = PathNormalizer.concatWebPath(paths[0], resource);
		try {
			rd = getResourceReader(resourcePath);
			exists = (rd != null);
		} catch (ResourceNotFoundException e) {
			// Nothing to do : exists is already equals to false
		}finally{
			IOUtils.close(rd);
		}
		return exists;
	}

	/* (non-Javadoc)
	 * @see com.asual.lesscss.loader.ResourceLoader#load(java.lang.String, java.lang.String[], java.lang.String)
	 */
	@Override
	public String load(String resource, String[] paths, String charset)
			throws IOException {
		
		String result = null;
		String resourcePath = PathNormalizer.concatWebPath(paths[0], resource);
		try {
			Reader rd = getResourceReader(resourcePath);
			result = IOUtils.toString(rd);
		} catch (ResourceNotFoundException e) {
			throw new IOException(e);
		} 
		return result;
	}
	
	/**
	 * Returns the resource reader
	 * @param resource the resource
	 * @return the resource reader
	 * @throws ResourceNotFoundException if the resoure is not found
	 */
	private Reader getResourceReader(String resource) throws ResourceNotFoundException{
		List<Class<?>> excluded = new ArrayList<Class<?>>();
		excluded.add(ILessCssResourceGenerator.class);
		return rsReaderHandler.getResource(resource, false,
				excluded);
	}
}
