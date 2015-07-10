/**
 * Copyright 2010 Ibrahim Chaehoi
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
package test.net.jawr.web.resource.bundle.handler;

import java.io.InputStream;
import java.io.Reader;
import java.util.List;
import java.util.Set;

import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.handler.reader.ResourceReader;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;

/**
 * @author Ibrahim Chaehoi
 *
 */
public class MockResourceReaderHandler implements ResourceReaderHandler {

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.handler.reader.ResourceReaderHandler#addResourceReaderToEnd(net.jawr.web.resource.handler.reader.ResourceReader)
	 */
	public void addResourceReaderToEnd(ResourceReader rd) {
		
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.handler.reader.ResourceReaderHandler#addResourceReaderToStart(net.jawr.web.resource.handler.reader.ResourceReader)
	 */
	public void addResourceReaderToStart(ResourceReader rd) {

	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.handler.reader.ResourceReaderHandler#getResource(java.lang.String)
	 */
	public Reader getResource(String resourceName)
			throws ResourceNotFoundException {
		return null;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.handler.reader.ResourceReaderHandler#getResource(java.lang.String, boolean)
	 */
	public Reader getResource(String resourceName, boolean processingBundle)
			throws ResourceNotFoundException {
		return null;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.handler.reader.ResourceReaderHandler#getResourceAsStream(java.lang.String)
	 */
	public InputStream getResourceAsStream(String resourceName)
			throws ResourceNotFoundException {
		return null;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.handler.reader.ResourceReaderHandler#getResourceAsStream(java.lang.String, boolean)
	 */
	public InputStream getResourceAsStream(String resourceName,
			boolean processingBundle) throws ResourceNotFoundException {
		return null;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.handler.reader.ResourceReaderHandler#getWorkingDirectory()
	 */
	public String getWorkingDirectory() {
		return null;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.handler.reader.WorkingDirectoryLocationAware#setWorkingDirectory(java.lang.String)
	 */
	public void setWorkingDirectory(String workingDir) {
	
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.handler.reader.ResourceBrowser#getResourceNames(java.lang.String)
	 */
	public Set<String> getResourceNames(String path) {
		return null;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.handler.reader.ResourceBrowser#isDirectory(java.lang.String)
	 */
	public boolean isDirectory(String path) {
		return false;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.handler.reader.ResourceReaderHandler#getResource(java.lang.String, boolean, java.util.List)
	 */
	@Override
	public Reader getResource(String resourceName, boolean processingBundle,
			List<Class<?>> excludedReader) throws ResourceNotFoundException {
		
		return null;
	}

}
