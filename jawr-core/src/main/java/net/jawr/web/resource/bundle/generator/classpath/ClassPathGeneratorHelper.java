/**
 * Copyright 2008-2015 Jordi Hernández Sellés, Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.generator.classpath;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import net.jawr.web.exception.BundlingProcessException;
import net.jawr.web.resource.bundle.factory.util.ClassLoaderResourceUtils;
import net.jawr.web.resource.bundle.factory.util.PathNormalizer;
import net.jawr.web.resource.bundle.generator.GeneratorContext;

/**
 * Abstract common functionality to retrieve resources (js and css) from the classpath. 
 * 
 * @author Jordi Hernández Sellés
 * @author ibrahim Chaehoi
 */
public class ClassPathGeneratorHelper {
	
	/** The prefix to preppend before searching resource in classpath */
	private final String classpathPrefix;
	
	/**
	 * Constructor
	 */
	public ClassPathGeneratorHelper() {
		this("");
	}
	
	/**
	 * Constructor
	 * @param classpathPrefix the classpath prefix
	 */
	public ClassPathGeneratorHelper(String classpathPrefix) {
		this.classpathPrefix = classpathPrefix;
	}
	
	/**
	 * Finds a resource from the classpath and returns a reader on it. 
	 * @param context the generator context
	 * @return the reader
	 */
	public Reader createResource(GeneratorContext context) {
		
		InputStream is = createStreamResource(context);
		ReadableByteChannel chan = Channels.newChannel(is);
		return Channels.newReader(chan,context.getCharset().newDecoder (),-1);
	}
	
	/**
	 * Finds a resource from the classpath and returns an input stream on it. 
	 * @param context the generator context
	 * @return the input stream
	 */
	public InputStream createStreamResource(GeneratorContext context) {
		InputStream is = null; 
		try {
			
			String resourcePath = context.getPath();
			String path = PathNormalizer.normalizePath(classpathPrefix+resourcePath);
			is = ClassLoaderResourceUtils.getResourceAsStream(path, this);
		} catch (FileNotFoundException e) {
			throw new BundlingProcessException(e);
		}
		return is;
	}
}
