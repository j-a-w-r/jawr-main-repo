/**
 * Copyright 2009-2012 Ibrahim Chaehoi
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


/**
 * Interface implemented by objects, which can manage resource readers.
 * 
 * @author Ibrahim Chaehoi
 *
 */
public interface ResourceReaderHandler extends WorkingDirectoryLocationAware, ResourceBrowser {

	
	/**
	 * Adds the resource reader to the end of the list of available readers 
	 * @param rd the resource reader to add
	 */
	void addResourceReaderToEnd(ResourceReader rd);
	
	/**
	 * Adds the resource reader to the beginning of the list of available readers 
	 * @param rd the resource reader to add
	 */
	void addResourceReaderToStart(ResourceReader rd);

	/**
	 * Returns the Jawr working directory
	 * @return the Jawr working directory
	 */
	String getWorkingDirectory();

	/**
	 * Retrieves a single resource. 
	 * @param resourceName String Name of the resource.  
	 * @return a reader for the resource
	 * @throws ResourceNotFoundException if the resource is not found
	 */
	public Reader getResource(String resourceName) throws ResourceNotFoundException;
	
	/**
	 * Retrieves a single resource. 
	 * @param resourceName String Name of the resource.  
	 * @param processingBundle the flag indicating that we are currently processing the bundles
	 * @return the reader to the resource
	 * @throws ResourceNotFoundException if the resource is not found
	 */
	public Reader getResource(String resourceName, boolean processingBundle) throws ResourceNotFoundException ;
	
	/**
	 * Retrieves a single resource. 
	 * @param resourceName String Name of the resource.  
	 * @param processingBundle the flag indicating that we are currently processing the bundles
	 * @param excludes the list of excluded reader
	 * @return the reader to the resource
	 * @throws ResourceNotFoundException if the resource is not found
	 */
	public Reader getResource(String resourceName, boolean processingBundle, List<Class<?>> excludedReader) throws ResourceNotFoundException;

   /**
	 * Retrieves the input stream of a resource defined in the web application. 
	 * @param resourceName the name of the resource.  
	 * @return a input stream of the resource
	 * @throws ResourceNotFoundException if the resource is not found
	 */
	public InputStream getResourceAsStream(String resourceName) throws ResourceNotFoundException;
	
	/**
	 * Retrieves the input stream of a resource defined in the web application. 
	 * @param resourceName the name of the resource.  
	 * @param processingBundle the flag indicating that we are currently processing the bundles
	 * @return a input stream of the resource
	 * @throws ResourceNotFoundException if the resource is not found
	 */
	public InputStream getResourceAsStream(String resourceName, boolean processingBundle) throws ResourceNotFoundException;

}
