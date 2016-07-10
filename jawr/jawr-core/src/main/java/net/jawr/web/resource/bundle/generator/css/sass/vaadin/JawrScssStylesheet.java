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
package net.jawr.web.resource.bundle.generator.css.sass.vaadin;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.InputSource;

import com.vaadin.sass.internal.ScssStylesheet;
import com.vaadin.sass.internal.handler.SCSSDocumentHandlerImpl;
import com.vaadin.sass.internal.handler.SCSSErrorHandler;
import com.vaadin.sass.internal.parser.ParseException;
import com.vaadin.sass.internal.parser.Parser;
import com.vaadin.sass.internal.parser.SCSSParseException;
import com.vaadin.sass.internal.resolver.ScssStylesheetResolver;

import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.mappings.FilePathMapping;

/**
 * The Jawr implementation of ScssStylesheet to handle more easily string
 * content
 * 
 * @author Ibrahim Chaehoi
 */
public class JawrScssStylesheet extends ScssStylesheet {

	/** The serial version UID */
	private static final long serialVersionUID = 933939723158980010L;

	/** The resource path */
	private String path;

	/** The stylesheet resolver */
	private ScssStylesheetResolver resolver;

	/**
	 * Constructor
	 * 
	 * @param bundle
	 *            the bundle
	 * @param content
	 *            the content
	 * @param path
	 *            the path
	 * @param scssResolver
	 *            the scss resolver
	 * @param charset
	 *            the charset
	 * @throws IOException
	 *             if an {@link IOException} occurs
	 * @throws CSSException
	 *             if a {@link CSSException} occurs
	 */
	public JawrScssStylesheet(JoinableResourceBundle bundle, String content, String path, JawrScssResolver scssResolver,
			Charset charset) throws CSSException, IOException {

		this.path = path;
		addSourceUris(Arrays.asList(path));
		// Use default resolvers
		addResolver(scssResolver);

		InputSource source = new InputSource(new StringReader(content));
		Parser parser = new Parser();
		parser.setErrorHandler(new SCSSErrorHandler());
		SCSSDocumentHandlerImpl docHandler = new SCSSDocumentHandlerImpl(this);
		parser.setDocumentHandler(docHandler);

		FilePathMapping fMapping = scssResolver.getFilePathMapping(path);
		if (fMapping != null) {
			scssResolver.addLinkedResource(fMapping);
			if (bundle != null) {
				bundle.getFilePathMappings()
						.add(new FilePathMapping(bundle, fMapping.getPath(), fMapping.getLastModified()));
			}
		}

		try {
			parser.parseStyleSheet(source);
		} catch (ParseException e) {
			// catch ParseException, re-throw a SCSSParseException which has
			// file name info.
			throw new SCSSParseException(e, path);
		}

		setCharset(charset.toString());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vaadin.sass.internal.ScssStylesheet#resolveStylesheet(java.lang.
	 * String, com.vaadin.sass.internal.ScssStylesheet)
	 */
	@Override
	public InputSource resolveStylesheet(String identifier, ScssStylesheet parentStylesheet) {

		return resolver.resolve(parentStylesheet, identifier);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vaadin.sass.internal.ScssStylesheet#getFileName()
	 */
	@Override
	public String getFileName() {

		return path;
	}

}
