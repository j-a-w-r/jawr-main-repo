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
package net.jawr.web.resource.bundle.generator;

import java.io.InputStream;

import net.jawr.web.config.JawrConfig;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;
import net.jawr.web.resource.handler.reader.StreamResourceReader;

/**
 * This class defines the class which wraps a stream resource generator in a stream resource reader
 * @author Ibrahim Chaehoi
 */
public class StreamResourceGeneratorReaderWrapper implements StreamResourceReader {

	/** The resource generator wrapped */
	private StreamResourceGenerator generator;
	
	/** The resource handler */
	private ResourceReaderHandler rsHandler;
	
	/** The Jawr config */
	private JawrConfig config;
	
	/**
	 * Constructor
	 * @param generator the generator
	 */
	public StreamResourceGeneratorReaderWrapper(StreamResourceGenerator generator, ResourceReaderHandler rsHandler, JawrConfig config) {
		this.generator = generator;
		this.config = config;
		this.rsHandler = rsHandler;
	}
	
	/* (non-Javadoc)
	 * @see net.jawr.web.resource.handler.stream.StreamResourceReader#getResourceAsStream(java.lang.String)
	 */
	public InputStream getResourceAsStream(String resourceName) {
		
		return getResourceAsStream(resourceName, false);
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.handler.stream.StreamResourceReader#getResourceAsStream(java.lang.String, boolean)
	 */
	public InputStream getResourceAsStream(String resourceName,
			boolean processingBundle) {
		
		GeneratorContext context = new GeneratorContext(config, generator.getResolver().getResourcePath(resourceName));
		context.setResourceReaderHandler(rsHandler);
		context.setProcessingBundle(processingBundle);
		
		return generator.createResourceAsStream(context);
	}
	
}
