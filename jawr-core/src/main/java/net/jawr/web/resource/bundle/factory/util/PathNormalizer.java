/**
 * Copyright 2007-2011 Jordi Hernández Sellés, Matt Ruby, Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.factory.util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.jawr.web.JawrConstant;
import net.jawr.web.exception.JawrLinkRenderingException;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.renderer.BundleRenderer;
import net.jawr.web.servlet.JawrRequestHandler;
import net.jawr.web.util.StringUtils;

/**
 * Utility class to work with relative paths. 
 * 
 * @author Jordi Hernández Sellés
 * @author Matt Ruby
 * @author Ibrahim Chaehoi
 *
 */
public final class PathNormalizer {
	
	/** The URL separator pattern */
	private static final Pattern URL_SEPARATOR_PATTERN = Pattern.compile("([^/]*)/");

	/** The pattern to go to the root */
	private static final String ROOT_REPLACE_PATTERN = "../";

	/**
	 * Constructor 
	 */
	private PathNormalizer() {
		
	}
	
	/**
	 * Converts the bundle path to a physical path
	 * @param bundlePath the bundle path
	 * @return the physical path
	 */
	public static String escapeToPhysicalPath(String bundlePath){
		return bundlePath.replaceAll(":", "_");
	}
	
	/**
	 * Removes the URL prefix defined in the configuration from a path. If the prefix contains a variant information, it adds it to the name.
	 * 
	 * @param path the path
	 * @return the path without the prefix
	 */
	public static String removeVariantPrefixFromPath(String path) {
		
		String resultPath = path;
		
		// Remove first slash
		if(path.charAt(0) == '/'){
			resultPath = path.substring(1);
		}
		
		// eval the existence of a suffix
		String prefix = resultPath.substring(0, resultPath.indexOf("/"));

		// The prefix also contains variant information after a '.'
		if (prefix.indexOf('.') != -1) {
			String variantPrefix = prefix.substring(prefix.indexOf('.') + 1);
			String suffix = '@' + variantPrefix + resultPath.substring(resultPath.lastIndexOf('.'));
			resultPath = resultPath.substring(resultPath.indexOf("/"), resultPath.lastIndexOf('.')) + suffix;
		} else
			resultPath = resultPath.substring(resultPath.indexOf("/"));
		
		return resultPath;
	}
	
	/**
	 * Removes the URL prefix defined in the configuration from a path. If the prefix contains a variant information, it adds it to the name.
	 * 
	 * @param path the path
	 * @return the bundle info from the  
	 */
	public static String[] extractBundleInfoFromPath(String path) {
		
		String[] result = new String[3];
		
		String resultPath = null;
		if(path.startsWith(BundleRenderer.GZIP_PATH_PREFIX)){
			// Remove the gzip prefix
			resultPath = path.substring(BundleRenderer.GZIP_PATH_PREFIX.length());
		}else{
			// Remove first slash
			resultPath = path.substring(1);
		}
		
		// eval the existence of a suffix
		int realPathStartIdx = resultPath.indexOf("/");
		String prefix = "";
		if(realPathStartIdx != -1){
			prefix = resultPath.substring(0, realPathStartIdx);
		}
		
		String hashcode = null;
		String variantPrefix = null;
		// The prefix also contains variant information after a '.'
		int hashCodeVariantSeparatorIdx = prefix.indexOf('.');
		if (hashCodeVariantSeparatorIdx != -1) {
			hashcode = prefix.substring(0, hashCodeVariantSeparatorIdx);
			variantPrefix = prefix.substring(hashCodeVariantSeparatorIdx + 1);
			resultPath = resultPath.substring(realPathStartIdx);
		} else{
			hashcode = prefix.substring(0, realPathStartIdx);
			resultPath = resultPath.substring(realPathStartIdx);
		}
		
		// Remove leading slash if it's a bundle containing a generator prefix
		if(resultPath.startsWith("/") && resultPath.contains(":")){
			resultPath = resultPath.substring(1);
		}
		
		result[0] = resultPath;
		result[1] = variantPrefix;
		result[2] = hashcode;
		
		return result;
	}
	
	/**
	 * Normalizes a bundle path mapping. If it ends with a wildcard, the wildcard is removed. 
	 * @param pathMapping the bundle path mapping
	 * @return the normalized path mapping
	 */
	public static String normalizePathMapping(String pathMapping) {
		
		String normalizedPathMapping = normalizePath(pathMapping);
		if(normalizedPathMapping.endsWith("/**"))
			normalizedPathMapping = normalizedPathMapping.substring(0,normalizedPathMapping.length()-3);
		return normalizedPathMapping;
	}
	
	/**
	 * Normalizes a path and adds a separator at its start. 
	 * @param path
	 * @return
	 */
	public static String asPath(String path) {
		return(JawrConstant.URL_SEPARATOR + normalizePath(path));
	}
	
	/**
	 * Normalizes a path and adds a separator at its start and its end. 
	 * @param path the path
	 * @return the normalized path
	 */
	public static String asDirPath(String path) {
		String dirPath = path;
		if(!path.equals(JawrConstant.URL_SEPARATOR)){
			dirPath = JawrConstant.URL_SEPARATOR + normalizePath(path) + JawrConstant.URL_SEPARATOR;  
		}
		return dirPath;
	}
	
	/**
	 * Normalizes two paths and joins them as a single path. 
	 * @param prefix
	 * @param path
	 * @return the joined path
	 */
	public static String joinPaths(String prefix,String path, GeneratorRegistry generatorRegistry) {
		
		return  joinPaths(prefix, path, generatorRegistry.isPathGenerated(prefix));
	}
	
	/**
	 * Normalizes two paths and joins them as a single path. 
	 * @param prefix
	 * @param path
	 * @return the joined path
	 */
	public static String joinPaths(String prefix,String path, boolean generatedPath) {
		
		String result = null;
		if(generatedPath){
			result = joinDomainToPath(prefix , path);
		}else{
			result = joinPaths(prefix, path); 
		}
		return  result; 
	}
	
	/**
	 * Normalizes two paths and joins them as a single path. 
	 * @param prefix
	 * @param path
	 * @return the joined path
	 */
	public static String joinPaths(String prefix,String path) {
		
		String joinedPath = null;
		if(prefix.startsWith(JawrConstant.HTTP_URL_PREFIX) || prefix.startsWith(JawrConstant.HTTPS_URL_PREFIX) || prefix.startsWith("//")){
			joinedPath = joinDomainToPath(prefix, path);
		}else{
			
			String normalizedPrefix = PathNormalizer.normalizePath(prefix);
			
			StringBuffer sb = new StringBuffer(JawrConstant.URL_SEPARATOR);
			if(!"".equals(normalizedPrefix))
				sb.append(normalizedPrefix).append(JawrConstant.URL_SEPARATOR);
			sb.append(PathNormalizer.normalizePath(path));
			joinedPath = sb.toString();
		}
		
		return joinedPath; 
	}
	
	/**
	 * Normalizes a dmoain name and a path and joins them as a single url. 
	 * @param domainName
	 * @param path
	 * @return
	 */
	public static String joinDomainToPath(String domainName,String path) {
		StringBuffer sb = new StringBuffer();
		if(domainName.endsWith(JawrConstant.URL_SEPARATOR)) {
			sb.append(domainName.substring(0,domainName.length()-1));
		}else{
			sb.append(domainName);
		}
		
		sb.append(JawrConstant.URL_SEPARATOR).append(PathNormalizer.normalizePath(path));
		
		return sb.toString();
		
	}	
	
	/**
	 * Removes leading and trailing separators from a path, and removes 
	 * double separators (// is replaced by /). 
	 * @param path the path to normalize
	 * @return the normalized path
	 */
	public static final String normalizePath(String path) {
		String normalizedPath = path.replaceAll("//", JawrConstant.URL_SEPARATOR);
		StringTokenizer tk = new StringTokenizer(normalizedPath,JawrConstant.URL_SEPARATOR);
		StringBuffer sb = new StringBuffer();
		while(tk.hasMoreTokens()) {
			sb.append(tk.nextToken());
			if(tk.hasMoreTokens())
				sb.append(JawrConstant.URL_SEPARATOR);
		}
		return sb.toString();
		
	}
	
	/**
	 * Normalizes all the paths in a Set. 
	 * @param paths
	 * @return
	 */
	public static final Set<String> normalizePaths(Set<String> paths) {
		Set<String> ret = new HashSet<String>();
		for(Iterator<String> it = paths.iterator();it.hasNext();) {
			String path = normalizePath((String)it.next());
			ret.add(path);
		}
		return ret;
	}
	
	/**
	 * converts a generation path (such as jar:/some/path/file) into 
	 * a request path that the request handler can understand and process.  
	 * @param path
	 * @return
	 */
	public static String createGenerationPath(String path, GeneratorRegistry registry, String randomParam){
		
		String requestPath = null; 
		try {
			
			requestPath = registry.getDebugModeGenerationPath(path);
			if(randomParam != null){
				requestPath += "?"+randomParam+"&"; 
			}else{
				requestPath += "?";
			}
			requestPath += JawrRequestHandler.GENERATION_PARAM 
				+ "=" 
				+ URLEncoder.encode(path, "UTF-8");
		} catch (UnsupportedEncodingException neverHappens) {
			/*URLEncoder:how not to use checked exceptions...*/
			throw new JawrLinkRenderingException("Something went unexpectedly wrong while encoding a URL for a generator. ",
										neverHappens);
		}
		return requestPath;
	}

	/**
	 * Adds a key and value to the request path
	 * & or ? will be used as needed
	 * 
	 * path + ? or & + parameterKey=parameter
	 * 
	 * @param path the url to add the parameterKey and parameter too
	 * @param parameterKey the key in the get request (parameterKey=parameter)
	 * @param parameter the parameter to add to the end of the url
	 * @return a String with the url parameter added: path + ? or & + parameterKey=parameter
	 */
	public static String addGetParameter(String path, String parameterKey, String parameter){
		StringBuffer sb = new StringBuffer(path); 
		if(path.indexOf("?") > 0) {
			sb.append("&");
		} else {
			sb.append("?");
		}
		sb.append(parameterKey + "="+ parameter);
		return sb.toString();
	}
	
	///------------------------------------------------------
	// The following methods are coming from :
	// 		* org.codehaus.plexus.util.PathTool
	//		* the concatWebPath method, is a method inspired from 
	//       the concat method of org.apache.commons.io.FileNameUtils
	//-------------------------------------------------------
	
	
	// New method
    /**
     * Determines the parent path of a filename or a directory.
     * 
     * <pre>
     * PathUtils.getParentPath( null )                           = ""
     * PathUtils.getParentPath( "" )                             = ""
     * PathUtils.getParentPath( "/" )                            = "/"
     * PathUtils.getParentPath( "/usr/local/" )                  = "/usr/local/"
     * PathUtils.getRelativePath( "/usr/local/bin/java.sh" )     = ""/usr/local/bin/"
     * </pre>
     * 
     * @param path the path
     * @return the parent path.
     */
    public static String getParentPath(String path){
    
    	String parentPath = null;
    	if(StringUtils.isEmpty(path)){
    		parentPath = "";
    	}else{
    		
    		parentPath = path;
	    	if(parentPath.length() > 1 && parentPath.endsWith(JawrConstant.URL_SEPARATOR)){
	    		parentPath = parentPath.substring(0, parentPath.length()-2);
	    	}
	    	int index = parentPath.lastIndexOf(JawrConstant.URL_SEPARATOR);
	    	if(index > 0){
	    		return parentPath.substring(0, index+1);
	    	}else{
	    		parentPath = JawrConstant.URL_SEPARATOR;
	    	}
    	}
    	
    	return parentPath; 
    }
    
 // New method
    /**
     * Determines the parent path of a filename or a directory.
     * 
     * <pre>
     * PathNormalizer.getPathName( null )                           = ""
     * PathNormalizer.getPathName( "" )                             = ""
     * PathNormalizer.getPathName( "/" )                            = "/"
     * PathNormalizer.getPathName( "/usr/local/" )                  = "local"
     * PathNormalizer.getPathName( "/usr/local/bin/java.sh" )     = "java.sh"
     * </pre>
     * 
     * @param path the path
     * @return the path name.
     */
    public static String getPathName(String path){
    
    	String pathName = null;
    	if(StringUtils.isEmpty(path)){
    		pathName = "";
    	}else{
    		pathName = path;
    		if(pathName.length() > 1 && pathName.endsWith(JawrConstant.URL_SEPARATOR)){
    			pathName = pathName.substring(0, pathName.length()-1);
        	}
        	int index = pathName.lastIndexOf(JawrConstant.URL_SEPARATOR);
        	if(index > 0){
        		pathName = pathName.substring(index+1);
        	}else{
        		pathName = JawrConstant.URL_SEPARATOR;
        	}
    	}
    	
    	return pathName; 
    }
    
    /**
     * Determines the relative path of a filename from a base directory.
     * This method is useful in building relative links within pages of
     * a web site.  It provides similar functionality to Anakia's
     * <code>$relativePath</code> context variable.  The arguments to
     * this method may contain either forward or backward slashes as
     * file separators.  The relative path returned is formed using
     * forward slashes as it is expected this path is to be used as a
     * link in a web page (again mimicking Anakia's behavior).
     * <p/>
     * This method is thread-safe.
     * <br/>
     * <pre>
     * PathUtils.getRelativePath( null, null )                                   = ""
     * PathUtils.getRelativePath( null, "/usr/local/java/bin" )                  = ""
     * PathUtils.getRelativePath( "/usr/local/", null )                          = ""
     * PathUtils.getRelativePath( "/usr/local/", "/usr/local/java/bin" )         = ".."
     * PathUtils.getRelativePath( "/usr/local/", "/usr/local/java/bin/java.sh" ) = "../.."
     * PathUtils.getRelativePath( "/usr/local/java/bin/java.sh", "/usr/local/" ) = ""
     * </pre>
     *
     * @param basedir The base directory.
     * @param filename The filename that is relative to the base
     * directory.
     * @return The relative path of the filename from the base
     * directory.  This value is not terminated with a forward slash.
     * A zero-length string is returned if: the filename is not relative to
     * the base directory, <code>basedir</code> is null or zero-length,
     * or <code>filename</code> is null or zero-length.
     */
    public static final String getRelativePath( String basedir, String filename )
    {
        String basedirPath = uppercaseDrive(basedir);
        String filenamePath = uppercaseDrive(filename);

        /*
         * Verify the arguments and make sure the filename is relative
         * to the base directory.
         */
        if ( basedirPath == null || basedirPath.length() == 0 || filenamePath == null
            || filenamePath.length() == 0 || !filenamePath.startsWith( basedirPath ) )
        {
            return "";
        }

        /*
         * Normalize the arguments.  First, determine the file separator
         * that is being used, then strip that off the end of both the
         * base directory and filename.
         */
        String separator = determineSeparator( filenamePath );
        basedirPath = StringUtils.chompLast( basedirPath, separator );
        filenamePath = StringUtils.chompLast( filenamePath, separator );

        /*
         * Remove the base directory from the filename to end up with a
         * relative filename (relative to the base directory).  This
         * filename is then used to determine the relative path.
         */
        String relativeFilename = filenamePath.substring( basedirPath.length() );

        return determineRelativePath( relativeFilename, separator );
    }

    /**
     * Concatenates a filename to a base web path.
     * If the base path doesn't end with "/", it will consider as base path the parent folder of the base path passed as parameter.
     * 
     * <pre>
     * PathUtils.concatWebPath("", null));								   			= null
	 * PathUtils.concatWebPath(null, null));								   		= null
	 * PathUtils.concatWebPath(null, ""));								   			= null
	 * PathUtils.concatWebPath(null, "a"));								   			= null			
	 * PathUtils.concatWebPath(null, "/a"));								   		= "/a" 
 	 * PathUtils.concatWebPath( "/css/folder/subfolder/", "icons/img.png" )      	= "/css/folder/subfolder/icons/img.png"
     * PathUtils.concatWebPath( "/css/folder/subfolder/style.css", "icons/img.png") = "/css/folder/subfolder/icons/img.png"
     * PathUtils.concatWebPath( "/css/folder/", "../icons/img.png" )      			= "/css/icons/img.png"
     * PathUtils.concatWebPath( "/css/folder/style.css", "../icons/img.png" ) 		= "/css/icons/img.png"
     * </pre>
     * 
     * @param basePath the base path
     * @param fullFilenameToAdd the file name to add
     * @return the concatenated path, or null if invalid
     */
    public static String concatWebPath(String basePath, String fullFilenameToAdd){
     	
    	if(fullFilenameToAdd == null || basePath == null && (fullFilenameToAdd.length() == 0
    			|| fullFilenameToAdd.charAt(0) != JawrConstant.URL_SEPARATOR_CHAR)){
    		return null;
    	}
    	
    	if(basePath == null){
    		basePath = "";
    	}
    	// If the basePath is pointing to a file, set the base path to the parent directory
    	if(basePath.length() > 1 && basePath.charAt(basePath.length()-1) != '/'){
    		basePath = getParentPath(basePath);
    	}
    	
    	int len = basePath.length();
    	String fullPath = null;
        if (len == 0) {
        	return doNormalizeIgnoreOtherSeparator(fullFilenameToAdd, true);
        }
        
    	char ch = basePath.charAt(len - 1);
        if (ch == JawrConstant.URL_SEPARATOR_CHAR) {
        	fullPath = basePath + fullFilenameToAdd;
        } else {
        	fullPath = basePath + '/' + fullFilenameToAdd;
        }
        
        return doNormalizeIgnoreOtherSeparator(fullPath, true); 
        
    }
    
    /**
     * Internal method to perform the normalization.
     *
     * @param filename  the filename
     * @param keepSeparator  true to keep the final separator
     * @return the normalized filename
     */
    private static String doNormalizeIgnoreOtherSeparator(String filename, boolean keepSeparator) {
        if (filename == null) {
            return null;
        }
        int size = filename.length();
        if (size == 0) {
            return filename;
        }
        int prefix = 0;
//        int prefix = getPrefixLength(filename);
//        if (prefix < 0) {
//            return null;
//        }
        
        char[] array = new char[size + 2];  // +1 for possible extra slash, +2 for arraycopy
        filename.getChars(0, filename.length(), array, 0);
        
        // add extra separator on the end to simplify code below
        boolean lastIsDirectory = true;
        if (array[size - 1] != JawrConstant.URL_SEPARATOR_CHAR) {
            array[size++] = JawrConstant.URL_SEPARATOR_CHAR;
            lastIsDirectory = false;
        }
        
        // adjoining slashes
        for (int i = prefix + 1; i < size; i++) {
            if (array[i] == JawrConstant.URL_SEPARATOR_CHAR && array[i - 1] == JawrConstant.URL_SEPARATOR_CHAR) {
                System.arraycopy(array, i, array, i - 1, size - i);
                size--;
                i--;
            }
        }
        
        // dot slash
        for (int i = prefix + 1; i < size; i++) {
            if (array[i] == JawrConstant.URL_SEPARATOR_CHAR && array[i - 1] == '.' &&
                    (i == prefix + 1 || array[i - 2] == JawrConstant.URL_SEPARATOR_CHAR)) {
                if (i == size - 1) {
                    lastIsDirectory = true;
                }
                System.arraycopy(array, i + 1, array, i - 1, size - i);
                size -=2;
                i--;
            }
        }
        
        // double dot slash
        outer:
        for (int i = prefix + 2; i < size; i++) {
            if (array[i] == JawrConstant.URL_SEPARATOR_CHAR && array[i - 1] == '.' && array[i - 2] == '.' &&
                    (i == prefix + 2 || array[i - 3] == JawrConstant.URL_SEPARATOR_CHAR)) {
                if (i == prefix + 2) {
                    return null;
                }
                if (i == size - 1) {
                    lastIsDirectory = true;
                }
                int j;
                for (j = i - 4 ; j >= prefix; j--) {
                    if (array[j] == JawrConstant.URL_SEPARATOR_CHAR) {
                        // remove b/../ from a/b/../c
                        System.arraycopy(array, i + 1, array, j + 1, size - i);
                        size -= (i - j);
                        i = j + 1;
                        continue outer;
                    }
                }
                // remove a/../ from a/../c
                System.arraycopy(array, i + 1, array, prefix, size - i);
                size -= (i + 1 - prefix);
                i = prefix + 1;
            }
        }
        
        if (size <= 0) {  // should never be less than 0
            return "";
        }
        if (size <= prefix) {  // should never be less than prefix
            return new String(array, 0, size);
        }
        if (lastIsDirectory && keepSeparator) {
            return new String(array, 0, size);  // keep trailing separator
        }
        return new String(array, 0, size - 1);  // lose trailing separator
    }

    /**
     * This method can calculate the relative path between two pathes on a web site.
     * <br/>
     * <pre>
     * PathUtils.getRelativeWebPath( null, null )                                          = ""
     * PathUtils.getRelativeWebPath( null, "http://plexus.codehaus.org/" )                 = ""
     * PathUtils.getRelativeWebPath( "http://plexus.codehaus.org/", null )                 = ""
     * PathUtils.getRelativeWebPath( "http://plexus.codehaus.org/",
     *                      "http://plexus.codehaus.org/plexus-utils/index.html" )        = "plexus-utils/index.html"
     * PathUtils.getRelativeWebPath( "http://plexus.codehaus.org/plexus-utils/index.html",
     *                      "http://plexus.codehaus.org/"                                 = "../../"
     * </pre>
     *
     * @param oldPath
     * @param newPath
     * @return a relative web path from <code>oldPath</code>.
     */
    public static final String getRelativeWebPath( final String oldPath, final String newPath )
    {
        if ( StringUtils.isEmpty( oldPath ) || StringUtils.isEmpty( newPath ) )
        {
            return "";
        }

        String resultPath = buildRelativePath( newPath, oldPath, '/' );

        if ( newPath.endsWith( "/" ) && !resultPath.endsWith( "/" ) )
        {
            return resultPath + "/";
        }

        return resultPath;
    }

    /**
     * This method can calculate the relative path between two pathes on a file system.
     * <br/>
     * <pre>
     * PathUtils.getRelativeFilePath( null, null )                                   = ""
     * PathUtils.getRelativeFilePath( null, "/usr/local/java/bin" )                  = ""
     * PathUtils.getRelativeFilePath( "/usr/local", null )                           = ""
     * PathUtils.getRelativeFilePath( "/usr/local", "/usr/local/java/bin" )          = "java/bin"
     * PathUtils.getRelativeFilePath( "/usr/local", "/usr/local/java/bin/" )         = "java/bin"
     * PathUtils.getRelativeFilePath( "/usr/local/java/bin", "/usr/local/" )         = "../.."
     * PathUtils.getRelativeFilePath( "/usr/local/", "/usr/local/java/bin/java.sh" ) = "java/bin/java.sh"
     * PathUtils.getRelativeFilePath( "/usr/local/java/bin/java.sh", "/usr/local/" ) = "../../.."
     * PathUtils.getRelativeFilePath( "/usr/local/", "/bin" )                        = "../../bin"
     * PathUtils.getRelativeFilePath( "/bin", "/usr/local/" )                        = "../usr/local"
     * </pre>
     * Note: On Windows based system, the <code>/</code> character should be replaced by <code>\</code> character.
     *
     * @param oldPath
     * @param newPath
     * @return a relative file path from <code>oldPath</code>.
     */
    public static final String getRelativeFilePath( final String oldPath, final String newPath )
    {
        if ( StringUtils.isEmpty( oldPath ) || StringUtils.isEmpty( newPath ) )
        {
            return "";
        }

        // normalise the path delimiters
        String fromPath = new File( oldPath ).getPath();
        String toPath = new File( newPath ).getPath();

        // strip any leading slashes if its a windows path
        if ( toPath.matches( "^\\[a-zA-Z]:" ) )
        {
            toPath = toPath.substring( 1 );
        }
        if ( fromPath.matches( "^\\[a-zA-Z]:" ) )
        {
            fromPath = fromPath.substring( 1 );
        }

        // lowercase windows drive letters.
        if ( fromPath.startsWith( ":", 1 ) )
        {
            fromPath = Character.toLowerCase( fromPath.charAt( 0 ) ) + fromPath.substring( 1 );
        }
        if ( toPath.startsWith( ":", 1 ) )
        {
            toPath = Character.toLowerCase( toPath.charAt( 0 ) ) + toPath.substring( 1 );
        }

        // check for the presence of windows drives. No relative way of
        // traversing from one to the other.
        if ( ( toPath.startsWith( ":", 1 ) && fromPath.startsWith( ":", 1 ) )
                        && ( !toPath.substring( 0, 1 ).equals( fromPath.substring( 0, 1 ) ) ) )
        {
            // they both have drive path element but they dont match, no
            // relative path
            return null;
        }

        if ( ( toPath.startsWith( ":", 1 ) && !fromPath.startsWith( ":", 1 ) )
                        || ( !toPath.startsWith( ":", 1 ) && fromPath.startsWith( ":", 1 ) ) )
        {
            // one has a drive path element and the other doesnt, no relative
            // path.
            return null;
        }

        String resultPath = buildRelativePath( toPath, fromPath, File.separatorChar );

        if ( newPath.endsWith( File.separator ) && !resultPath.endsWith( File.separator ) )
        {
            return resultPath + File.separator;
        }

        return resultPath;
    }

    // ----------------------------------------------------------------------
    // Private methods
    // ----------------------------------------------------------------------

    /**
     * Determines the relative path of a filename.  For each separator
     * within the filename (except the leading if present), append the
     * "../" string to the return value.
     *
     * @param filename The filename to parse.
     * @param separator The separator used within the filename.
     * @return The relative path of the filename.  This value is not
     * terminated with a forward slash.  A zero-length string is
     * returned if: the filename is zero-length.
     */
    private static final String determineRelativePath( String filename,
                                                       String separator )
    {
        if ( filename.length() == 0 )
        {
            return "";
        }


        /*
         * Count the slashes in the relative filename, but exclude the
         * leading slash.  If the path has no slashes, then the filename
         * is relative to the current directory.
         */
        int slashCount = StringUtils.countMatches( filename, separator ) - 1;
        if ( slashCount <= 0 )
        {
            return ".";
        }

        /*
         * The relative filename contains one or more slashes indicating
         * that the file is within one or more directories.  Thus, each
         * slash represents a "../" in the relative path.
         */
        StringBuffer sb = new StringBuffer();
        for ( int i = 0; i < slashCount; i++ )
        {
            sb.append( "../" );
        }

        /*
         * Finally, return the relative path but strip the trailing
         * slash to mimic Anakia's behavior.
         */
        return StringUtils.chop( sb.toString() );
    }

    /**
     * Helper method to determine the file separator (forward or
     * backward slash) used in a filename.  The slash that occurs more
     * often is returned as the separator.
     *
     * @param filename The filename parsed to determine the file
     * separator.
     * @return The file separator used within <code>filename</code>.
     * This value is either a forward or backward slash.
     */
    private static final String determineSeparator( String filename )
    {
        int forwardCount = StringUtils.countMatches( filename, "/" );
        int backwardCount = StringUtils.countMatches( filename, "\\" );

        return forwardCount >= backwardCount ? "/" : "\\";
    }

    /**
     * Cygwin prefers lowercase drive letters, but other parts of maven use uppercase
     * @param path
     * @return String
     */
    static final String uppercaseDrive(String path)
    {
    	String resultPath = null;
        if (path != null)
        {
            if (path.length() >= 2 && path.charAt(1) == ':')
	        {
            	resultPath = Character.toUpperCase( path.charAt( 0 ) ) + path.substring( 1 );
	        }else{
	        	resultPath = path;
	        }
        }
        return resultPath;
    }

    private static final String buildRelativePath( String toPath,  String fromPath, final char separatorChar )
    {
        // use tokeniser to traverse paths and for lazy checking
        StringTokenizer toTokeniser = new StringTokenizer( toPath, String.valueOf( separatorChar ) );
        StringTokenizer fromTokeniser = new StringTokenizer( fromPath, String.valueOf( separatorChar ) );

        int count = 0;

        // walk along the to path looking for divergence from the from path
        while ( toTokeniser.hasMoreTokens() && fromTokeniser.hasMoreTokens() )
        {
            if ( separatorChar == '\\' )
            {
                if ( !fromTokeniser.nextToken().equalsIgnoreCase( toTokeniser.nextToken() ) )
                {
                    break;
                }
            }
            else
            {
                if ( !fromTokeniser.nextToken().equals( toTokeniser.nextToken() ) )
                {
                    break;
                }
            }

            count++;
        }

        // reinitialise the tokenisers to count positions to retrieve the
        // gobbled token

        toTokeniser = new StringTokenizer( toPath, String.valueOf( separatorChar ) );
        fromTokeniser = new StringTokenizer( fromPath, String.valueOf( separatorChar ) );

        while ( count-- > 0 )
        {
            fromTokeniser.nextToken();
            toTokeniser.nextToken();
        }

        StringBuffer relativePath = new StringBuffer();

        // add back refs for the rest of from location.
        while ( fromTokeniser.hasMoreTokens() )
        {
            fromTokeniser.nextToken();

            relativePath.append("..");

            if ( fromTokeniser.hasMoreTokens() )
            {
            	relativePath.append(separatorChar);
            }
        }

        if ( relativePath.length() != 0 && toTokeniser.hasMoreTokens() )
        {
        	relativePath.append(separatorChar);
        }

        // add fwd fills for whatevers left of newPath.
        while ( toTokeniser.hasMoreTokens() )
        {
        	relativePath.append(toTokeniser.nextToken());

            if ( toTokeniser.hasMoreTokens() )
            {
            	relativePath.append(separatorChar);
            }
        }
        return relativePath.toString();
    }

    /**
	 * Returns the relative path of an url to go back to the root.
	 * For example : if the url path is defined as "/cssServletPath/css/myStyle.css" -> "../../"
	 * 
	 * @param url the requested url
	 * @return the relative path of an url to go back to the root.
	 */
	public static String getRootRelativePath(String url) {

		Matcher matcher = URL_SEPARATOR_PATTERN.matcher(url);
		StringBuffer result = new StringBuffer();
		boolean first = true;
		while (matcher.find()) {
			if (first) {
				matcher.appendReplacement(result, "");
				first = false;
			} else {
				matcher.appendReplacement(result, ROOT_REPLACE_PATTERN);
			}
		}

		return result.toString();
	}
}
