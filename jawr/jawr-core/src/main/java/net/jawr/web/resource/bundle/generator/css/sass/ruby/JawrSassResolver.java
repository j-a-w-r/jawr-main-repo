/**
 * Copyright 2016 Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.generator.css.sass.ruby;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.bundle.IOUtils;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.css.CssImageUrlRewriter;
import net.jawr.web.resource.bundle.factory.util.PathNormalizer;
import net.jawr.web.resource.bundle.generator.css.sass.ISassResourceGenerator;
import net.jawr.web.resource.bundle.mappings.FilePathMapping;
import net.jawr.web.resource.bundle.mappings.FilePathMappingUtils;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;

/**
 * The Resolver for Sass Ruby
 * 
 * @author Ibrahim Chaehoi
 */
public class JawrSassResolver {

	/** The resource reader handler */
	private ResourceReaderHandler rsHandler;

	/** The bundle */
	private JoinableResourceBundle bundle;

	/** The resource path */
	private String scssPath;

	/** The flag indicating if we are using absolute URL or not */
	private boolean useAbsoluteUrl;

	/** The linked resources */
	private List<FilePathMapping> linkedResources = new ArrayList<>();

	/**
	 * Constructor
	 * 
	 * @param bundle
	 *            the resource bundle
	 * @param path
	 *            the resource path
	 * @param rsHandler
	 *            the resource reader handler
	 * @param useAbsoluteUrl
	 *            the flag indicating if we are using absolute URL
	 * 
	 */
	public JawrSassResolver(JoinableResourceBundle bundle, String path, ResourceReaderHandler rsHandler,
			boolean useAbsoluteUrl) {
		this.bundle = bundle;
		this.scssPath = path;
		this.rsHandler = rsHandler;
		this.useAbsoluteUrl = useAbsoluteUrl;
		addLinkedResource(path);
	}

	/**
	 * Returns the linked resources
	 * 
	 * @return the linkedResources
	 */
	public List<FilePathMapping> getLinkedResources() {
		return linkedResources;
	}

	/**
	 * Returns the path of the resource
	 * 
	 * @param base
	 *            the base path
	 * @param uri
	 *            the relative URI
	 * @return the resource path
	 * @throws ResourceNotFoundException
	 *             if the resource is not found
	 * @throws IOException
	 *             if an IOException occurs
	 */
	public String getPath(String base, String uri) throws ResourceNotFoundException, IOException {

		String fileName = uri;
		if (!fileName.endsWith(".scss")) {
			fileName += ".scss";
		}

		String parentPath = base.replace('\\', '/');
		fileName = fileName.replace('\\', '/');

		return PathNormalizer.concatWebPath(parentPath, fileName);
	}

	/**
	 * Return the content of the resource using the base path and the relative
	 * URI
	 * 
	 * @param base
	 *            the base path
	 * @param uri
	 *            the relative URI
	 * @return the content of the resource using the base path and the relative
	 *         URI
	 * @throws ResourceNotFoundException
	 *             if the resource is not found
	 * @throws IOException
	 *             if an IOException occurs
	 */
	public String findRelative(String base, String uri) throws ResourceNotFoundException, IOException {

		String path = getPath(base, uri);

		String source = resolveAndNormalize(path);

		if (source != null) {
			return source;
		}

		// Try to find partial import (_identifier.scss)
		path = PathNormalizer.getParentPath(path) + "_" + PathNormalizer.getPathName(path);

		source = resolveAndNormalize(path);

		if (source != null) {
			return source;
		}

		return resolveAndNormalize(uri);

	}

	/**
	 * Finds and and normalized the content of the resource
	 * 
	 * @param path
	 *            the resource path
	 * @return the normalized resource content
	 * @throws ResourceNotFoundException
	 *             if the resource is not found
	 * @throws IOException
	 *             if an IOException occurs
	 */
	protected String resolveAndNormalize(String path) throws ResourceNotFoundException, IOException {

		List<Class<?>> excluded = new ArrayList<>();
		excluded.add(ISassResourceGenerator.class);
		Reader rd = null;
		try {
			rd = rsHandler.getResource(bundle, path, false, excluded);
			addLinkedResource(path);
		} catch (ResourceNotFoundException e) {
			// Do nothing
		}

		String content = null;
		if (rd != null) {
			content = IOUtils.toString(rd);
			if (!useAbsoluteUrl) {
				CssImageUrlRewriter rewriter = new CssImageUrlRewriter();
				content = rewriter.rewriteUrl(path, this.scssPath, content).toString();
			}
			content = SassRubyUtils.normalizeMultiByteString(content);
		}
		return content;
	}

	/**
	 * Adds a path to the linked resource
	 * 
	 * @param path
	 *            the resource path
	 */
	protected void addLinkedResource(String path) {
		FilePathMapping linkedResource = getFilePathMapping(path);
		if (linkedResource != null) {
			addLinkedResource(linkedResource);
			if (bundle != null) {
				bundle.getLinkedFilePathMappings()
						.add(new FilePathMapping(bundle, linkedResource.getPath(), linkedResource.getLastModified()));
			}
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
	 * Adds a linked resource
	 * 
	 * @param linkedResource
	 *            the linked resource to add
	 */
	private void addLinkedResource(FilePathMapping linkedResource) {
		linkedResources.add(linkedResource);
	}

}
