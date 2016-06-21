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

import static net.jawr.web.JawrConstant.URL_SEPARATOR;
import static net.jawr.web.JawrConstant.JAR_URL_PREFIX;
import static net.jawr.web.JawrConstant.JAR_URL_SEPARATOR;
import static net.jawr.web.JawrConstant.FILE_URL_PREFIX;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jawr.web.JawrConstant;
import net.jawr.web.exception.BundlingProcessException;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.bundle.factory.util.ClassLoaderResourceUtils;
import net.jawr.web.resource.bundle.factory.util.PathNormalizer;
import net.jawr.web.resource.bundle.generator.GeneratorContext;
import net.jawr.web.resource.handler.reader.ResourceBrowser;
import net.jawr.web.util.FileUtils;

/**
 * Abstract common functionality to retrieve resources (js and css) from the
 * classpath.
 * 
 * @author Jordi Hernández Sellés
 * @author ibrahim Chaehoi
 */
public class ClassPathGeneratorHelper implements ResourceBrowser {

	/** The logger */
	private static Logger LOGGER = LoggerFactory.getLogger(ClassPathGeneratorHelper.class); 
	
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
	 * 
	 * @param classpathPrefix
	 *            the classpath prefix
	 */
	public ClassPathGeneratorHelper(String classpathPrefix) {
		this.classpathPrefix = classpathPrefix;
	}

	/**
	 * Finds a resource from the classpath and returns a reader on it.
	 * 
	 * @param context
	 *            the generator context
	 * @return the reader
	 */
	public Reader createResource(GeneratorContext context) {

		InputStream is = createStreamResource(context);
		ReadableByteChannel chan = Channels.newChannel(is);
		return Channels.newReader(chan, context.getCharset().newDecoder(), -1);
	}

	/**
	 * Finds a resource from the classpath and returns an input stream on it.
	 * 
	 * @param context
	 *            the generator context
	 * @return the input stream
	 */
	public InputStream createStreamResource(GeneratorContext context) {
		InputStream is = null;
		try {

			String resourcePath = context.getPath();
			String path = getCompletePath(resourcePath);
			is = ClassLoaderResourceUtils.getResourceAsStream(path, this);
		} catch (FileNotFoundException e) {
			throw new BundlingProcessException(e);
		}
		return is;
	}

	/**
	 * Returns the complete path with the classpath prefix
	 * @param resourcePath the resource path
	 * @return the complete path with the classpath prefix
	 */
	private String getCompletePath(String resourcePath) {
		String path = PathNormalizer.normalizePath(classpathPrefix + resourcePath);
		return path;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.handler.reader.ResourceBrowser#getResourceNames(
	 * java.lang.String)
	 */
	@Override
	public Set<String> getResourceNames(String path) {

		Set<String> resources = null;
		
		URL resourceURL = null;
		
		try {
			resourceURL = ClassLoaderResourceUtils.getResourceURL(getCompletePath(path), this);
			if(resourceURL.toString().startsWith(JAR_URL_PREFIX)){
				resources = getResourceNamesFromJar(path, resourceURL);
			}else if(resourceURL.toString().startsWith(FILE_URL_PREFIX)){
				
				String dirPath = resourceURL.getFile();
				File dir = new File(dirPath);
				resources = FileUtils.getResourceNames(dir);
			}
		} catch (ResourceNotFoundException e) {

			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("Unable to find resources name for path '"+path+"'", e);
			}
			resources = Collections.emptySet();
		}
		
		return resources;
	}

	/**
	 * Returns the resources name from a Jar file.
	 * @param path the directory path
	 * @param resourceURL the jar file URL
	 * @return the resources name from a Jar file.
	 */
	private Set<String> getResourceNamesFromJar(String path, URL resourceURL) {
		
		URLConnection con = null;

		try {
			if(resourceURL.toString().startsWith(JawrConstant.JAR_URL_PREFIX)){
				con = resourceURL.openConnection();
			}
		} catch (IOException e) {

			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("Unable to find resources name for path '"+path+"'", e);
			}
			return Collections.emptySet();
		}

		JarFile jarFile;
		String jarFileUrl;
		String rootEntryPath;
		boolean newJarFile = false;

		try {
			if (con instanceof JarURLConnection) {
				// Should usually be the case for traditional JAR files.
				JarURLConnection jarCon = (JarURLConnection) con;
				jarCon.setUseCaches(true);
				jarFile = jarCon.getJarFile();
				jarFileUrl = jarCon.getJarFileURL().toExternalForm();
				JarEntry jarEntry = jarCon.getJarEntry();
				rootEntryPath = (jarEntry != null ? jarEntry.getName() : "");
			} else {
				// No JarURLConnection -> need to resort to URL file parsing.
				// We'll assume URLs of the format "jar:path!/entry", with the
				// protocol
				// being arbitrary as long as following the entry format.
				// We'll also handle paths with and without leading "file:"
				// prefix.
				String urlFile = resourceURL.getFile();
				int separatorIndex = urlFile.indexOf(JAR_URL_SEPARATOR);
				if (separatorIndex != -1) {
					jarFileUrl = urlFile.substring(0, separatorIndex);
					rootEntryPath = urlFile.substring(separatorIndex + JAR_URL_SEPARATOR.length());
					jarFile = getJarFile(jarFileUrl);
				} else {
					jarFile = new JarFile(urlFile);
					jarFileUrl = urlFile;
					rootEntryPath = "";
				}
				newJarFile = true;
			}
		} catch (IOException e) {
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("Unable to find resources name for path '"+path+"'", e);
			}
			return Collections.emptySet();
		}
		try {
			if (!"".equals(rootEntryPath) && !rootEntryPath.endsWith("/")) {
				// Root entry path must end with slash to allow for proper
				// matching.
				// The Sun JRE does not return a slash here, but BEA JRockit
				// does.
				rootEntryPath = rootEntryPath + "/";
			}
			Set<String> result = new LinkedHashSet<String>(8);
			for (Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements();) {
				JarEntry entry = entries.nextElement();
				String entryPath = entry.getName();
				if (isDirectChildPath(rootEntryPath, entryPath)) {
					String relativePath = entryPath.substring(rootEntryPath.length());
					result.add(relativePath);
				}
			}
			return result;
		} finally {
			// Close jar file, but only if freshly obtained -
			// not from JarURLConnection, which might cache the file reference.
			if (newJarFile) {
				try {
					jarFile.close();
				} catch (IOException e) {
					// Nothing to do
				}
			}
		}
	}

	/**
	 * Checks if the entry is a direct child of the root Entry
	 * isDirectChildPath( '/a/b/c/' , '/a/b/c/d.txt') => true
	 * isDirectChildPath( '/a/b/c/' , '/a/b/c/d/') => true
	 * isDirectChildPath( '/a/b/c/' , '/a/b/c/d/e.txt') => false
	 * @param rootEntryPath the root entry path
	 * @param entryPath the entry path to check
	 * @return true if the entry is a direct child of the root Entry
	 */
	private boolean isDirectChildPath(String rootEntryPath, String entryPath) {
		boolean result = false;
		if(entryPath.length() > rootEntryPath.length() && entryPath.startsWith(rootEntryPath)){
			int idx = entryPath.indexOf(URL_SEPARATOR, rootEntryPath.length());
			if(idx == -1){ // case where the entry is a child file /a/b/c/d.txt 
				result = true;
			}else{
				if(entryPath.length() == idx+1){ // case where the entry is a child file /a/b/c/d/ 
					result = true;
				}
			}
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.handler.reader.ResourceBrowser#isDirectory(java.
	 * lang.String)
	 */
	@Override
	public boolean isDirectory(String path) {
		
		return path.endsWith(URL_SEPARATOR);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.handler.reader.ResourceBrowser#getFilePath(java.
	 * lang.String)
	 */
	@Override
	public String getFilePath(String resourcePath) {
		
		String filePath = null;
		String path = getCompletePath(resourcePath);
		URL url = null;
		try {
			url = ClassLoaderResourceUtils.getResourceURL(path, this);
			String strURL = url.toString();
			if(strURL.startsWith(JawrConstant.FILE_URL_PREFIX)){
				filePath = new File(url.getFile()).getAbsolutePath();
			}else if(strURL.startsWith(JawrConstant.JAR_URL_PREFIX+JawrConstant.FILE_URL_PREFIX)){
				String tmp = strURL.substring((JawrConstant.JAR_URL_PREFIX+JawrConstant.FILE_URL_PREFIX).length());
				int idxJarContentSeparator = tmp.indexOf("!");
				if(idxJarContentSeparator != -1){
					tmp = tmp.substring(0, idxJarContentSeparator);
				}
				filePath = new File(tmp).getAbsolutePath();
			}
		} catch (ResourceNotFoundException e) {
			filePath = null;
		}
		
		return filePath;
	}

	/**
	 * Resolve the given jar file URL into a JarFile object.
	 */
	protected JarFile getJarFile(String jarFileUrl) throws IOException {
		if (jarFileUrl.startsWith(JawrConstant.FILE_URL_PREFIX)) {
			try {
				return new JarFile(toURI(jarFileUrl).getSchemeSpecificPart());
			} catch (URISyntaxException ex) {
				// Fallback for URLs that are not valid URIs (should hardly ever
				// happen).
				return new JarFile(jarFileUrl.substring(JawrConstant.FILE_URL_PREFIX.length()));
			}
		} else {
			return new JarFile(jarFileUrl);
		}
	}

	/**
	 * Create a URI instance for the given URL, replacing spaces with "%20" URI
	 * encoding first.
	 * <p>
	 * Furthermore, this method works on JDK 1.4 as well, in contrast to the
	 * {@code URL.toURI()} method.
	 * 
	 * @param url
	 *            the URL to convert into a URI instance
	 * @return the URI instance
	 * @throws URISyntaxException
	 *             if the URL wasn't a valid URI
	 * @see java.net.URL#toURI()
	 */
	public static URI toURI(URL url) throws URISyntaxException {
		return toURI(url.toString());
	}

	/**
	 * Create a URI instance for the given location String, replacing spaces
	 * with "%20" URI encoding first.
	 * 
	 * @param location
	 *            the location String to convert into a URI instance
	 * @return the URI instance
	 * @throws URISyntaxException
	 *             if the location wasn't a valid URI
	 */
	public static URI toURI(String location) throws URISyntaxException {
		return new URI(location.replace(" ", "%20"));
	}
}
