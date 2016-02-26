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
package net.jawr.web.resource.bundle.generator.css.sass;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.w3c.css.sac.InputSource;

import com.vaadin.sass.internal.ScssStylesheet;
import com.vaadin.sass.internal.resolver.AbstractResolver;

import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.factory.util.PathNormalizer;
import net.jawr.web.resource.bundle.mappings.FilePathMapping;
import net.jawr.web.resource.bundle.mappings.FilePathMappingUtils;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;

/**
 * This class defines the Jawr resolver for Sass compiler
 * 
 * @author Ibrahim Chaehoi
 */
public class JawrScssResolver extends AbstractResolver {

	/** The serial version UID */
	private static final long serialVersionUID = -6883663899372194879L;

	/** The resource reader handler */
	private ResourceReaderHandler rsHandler;

	/** The bundle */
	private JoinableResourceBundle bundle;

	/** The parent SCSS stylesheet */
	private JawrScssStylesheet parent;

	/**
	 * Constructor
	 * 
	 * @param bundle
	 *            the bundle
	 * @param rsHandler
	 *            the resource reader handler
	 */
	public JawrScssResolver(JoinableResourceBundle bundle, ResourceReaderHandler rsHandler) {
		this.bundle = bundle;
		this.rsHandler = rsHandler;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.vaadin.sass.internal.resolver.ScssStylesheetResolver#resolve(java
	 * .lang.String)
	 */
	@Override
	public InputSource resolve(ScssStylesheet parentStylesheet, String identifier) {
		// Remove a possible ".scss" suffix
		identifier = identifier.replaceFirst(".scss$", "");
		String parentPath = parentStylesheet.getFileName().replace('\\', '/');
		String path = PathNormalizer.concatWebPath(parentPath, identifier);

		InputSource source = resolveNormalized(path);

		if (source != null) {
			return source;
		}

		// Try to find partial import (_identifier.scss)
		path = PathNormalizer.getParentPath(path) + "_" + PathNormalizer.getPathName(path);

		source = normalizeAndResolve(path);

		if (source != null) {
			return source;
		}

		return normalizeAndResolve(identifier);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.vaadin.sass.internal.resolver.AbstractResolver#resolveNormalized(java
	 * .lang.String)
	 */
	@Override
	public InputSource resolveNormalized(String identifier) {
		String fileName = identifier;
		if (!fileName.endsWith(".css")) {
			fileName += ".scss";
		}

		List<Class<?>> excluded = new ArrayList<Class<?>>();
		excluded.add(ISassResourceGenerator.class);
		Reader rd = null;
		try {
			rd = rsHandler.getResource(bundle, fileName, false, excluded);
			FilePathMapping linkedResource = getFilePathMapping(fileName);
			if (linkedResource != null) {
				addLinkedResource(linkedResource);
				if (bundle != null) {
					bundle.getFilePathMappings().add(
							new FilePathMapping(bundle, linkedResource.getPath(), linkedResource.getLastModified()));
				}
			}
		} catch (ResourceNotFoundException e) {
			// Do nothing
		}

		if (rd != null) {

			InputSource source = new InputSource();
			source.setCharacterStream(rd);
			source.setURI(fileName);
			return source;
		} else {
			return null;
		}
	}

	/**
	 * Returns the file path mapping
	 * 
	 * @param fileName
	 *            the path
	 * @return the file path mapping
	 */
	public FilePathMapping getFilePathMapping(String fileName) {
		return FilePathMappingUtils.buildFilePathMapping(fileName, rsHandler);
	}

	/**
	 * Sets the parent stylesheet
	 * 
	 * @param sheet
	 *            the Scss stylesheet
	 */
	public void setParentScssStyleSheet(JawrScssStylesheet sheet) {
		this.parent = sheet;
	}

	/**
	 * Adds a linked resource to the less source
	 * 
	 * @param linkedResource
	 *            the linked resource to add
	 */
	private void addLinkedResource(FilePathMapping linkedResource) {
		if (parent != null) {
			parent.addLinkedResource(linkedResource);
		}
	}

}
