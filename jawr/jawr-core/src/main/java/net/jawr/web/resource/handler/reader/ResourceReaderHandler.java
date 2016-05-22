/**
 * Copyright 2009-2016 Ibrahim Chaehoi
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
package net.jawr.web.resource.handler.reader;

import java.io.InputStream;
import java.io.Reader;
import java.util.List;

import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.bundle.JoinableResourceBundle;

/**
 * Interface implemented by objects, which can manage resource readers.
 * 
 * @author Ibrahim Chaehoi
 *
 */
public interface ResourceReaderHandler extends WorkingDirectoryLocationAware, ResourceBrowser {

	/**
	 * Adds the resource reader the list of available readers
	 * 
	 * @param rd
	 *            the resource reader to add
	 */
	void addResourceReader(ResourceReader rd);

	/**
	 * Returns the Jawr working directory
	 * 
	 * @return the Jawr working directory
	 */
	String getWorkingDirectory();

	/**
	 * Retrieves a single resource.
	 * 
	 * @param resourceName
	 *            String Name of the resource.
	 * @return a reader for the resource
	 * @throws ResourceNotFoundException
	 *             if the resource is not found
	 */
	Reader getResource(String resourceName) throws ResourceNotFoundException;

	/**
	 * Retrieves a single resource.
	 * 
	 * @param bundle
	 *            The bundle.
	 * @param resourceName
	 *            String Name of the resource.
	 * @return a reader for the resource
	 * @throws ResourceNotFoundException
	 *             if the resource is not found
	 */
	public Reader getResource(JoinableResourceBundle bundle, String resourceName) throws ResourceNotFoundException;

	/**
	 * Retrieves a single resource.
	 * 
	 * @param bundle
	 *            The bundle.
	 * @param resourceName
	 *            String Name of the resource.
	 * @param processingBundle
	 *            the flag indicating that we are currently processing the
	 *            bundles
	 * @return the reader to the resource
	 * @throws ResourceNotFoundException
	 *             if the resource is not found
	 */
	// TODO check if this method can not be refactored to getResource(JoinableResourceBundle bundle, String resourceName)
	// When bundle is set consider it like if processing bundle flag to true.
	Reader getResource(JoinableResourceBundle bundle, String resourceName, boolean processingBundle)
			throws ResourceNotFoundException;

	/**
	 * Retrieves a single resource.
	 * 
	 * @param bundle
	 *            The bundle.
	 * @param resourceName
	 *            String Name of the resource.
	 * @param processingBundle
	 *            the flag indicating that we are currently processing the
	 *            bundles
	 * @param excludes
	 *            the list of excluded reader
	 * @return the reader to the resource
	 * @throws ResourceNotFoundException
	 *             if the resource is not found
	 */
	Reader getResource(JoinableResourceBundle bundle, String resourceName, boolean processingBundle,
			List<Class<?>> excludedReader) throws ResourceNotFoundException;

	/**
	 * Retrieves the input stream of a resource defined in the web application.
	 * 
	 * @param resourceName
	 *            the name of the resource.
	 * @return a input stream of the resource
	 * @throws ResourceNotFoundException
	 *             if the resource is not found
	 */
	InputStream getResourceAsStream(String resourceName) throws ResourceNotFoundException;

	/**
	 * Retrieves the input stream of a resource defined in the web application.
	 * 
	 * @param resourceName
	 *            the name of the resource.
	 * @param processingBundle
	 *            the flag indicating that we are currently processing the
	 *            bundles
	 * @return a input stream of the resource
	 * @throws ResourceNotFoundException
	 *             if the resource is not found
	 */
	InputStream getResourceAsStream(String resourceName, boolean processingBundle)
			throws ResourceNotFoundException;

	/**
	 * Returns the last modified date of the resource path available on
	 * filesystem or 0 if it doesn't exists.
	 * 
	 * @param filePath
	 *            the file Path
	 * @return the last modified date
	 */
	long getLastModified(String filePath);
	
}
