/**
 * Copyright 2015 Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.mappings;

import net.jawr.web.resource.bundle.JoinableResourceBundle;

/**
 * This class define a path mapping
 * 
 * @author Ibrahim Chaehoi
 */
public class PathMapping {

	
	/** The directory path mapping suffix */
	private static final String DIR_PATH_SUFFIX = "/";

	/** The recursive directory path mapping suffix */
	private static final String RECURSIVE_PATH_SUFFIX = "/**";

	/** The path mapping */
	private final String mapping;
	
	/** The kind of path mapping */
	private final PathMappingKind kind;
	
	/** The bundle */
	private final JoinableResourceBundle bundle;
	
	/**
	 * Constructor
	 * @param mapping the mapping
	 */
	public PathMapping(JoinableResourceBundle bundle, String mapping) {
		this.bundle = bundle;
		if(mapping.endsWith(DIR_PATH_SUFFIX)){
			this.mapping = mapping;
			this.kind = PathMappingKind.DIRECTORY;
		}else if(mapping.endsWith(RECURSIVE_PATH_SUFFIX)){
			this.mapping = mapping.substring(0, mapping.length()-RECURSIVE_PATH_SUFFIX.length()+1);
			kind = PathMappingKind.RECURSIVE_DIRECTORY;
		}else{
			this.mapping = mapping;
			kind = PathMappingKind.ASSET;
		}
	}
	
	/**
	 * Returns the bundle
	 * @return the bundle
	 */
	public JoinableResourceBundle getBundle() {
		return bundle;
	}

	/**
	 * Returns the path
	 * @return the path
	 */
	public String getPath() {
		return mapping;
	}
	
	/**
	 * Returns true if the mapping is a directory mapping
	 * @return true if the mapping is a directory mapping
	 */
	public boolean isAsset(){
		return kind == PathMappingKind.ASSET;
	}
	
	/**
	 * Returns true if the mapping is a directory mapping
	 * @return true if the mapping is a directory mapping
	 */
	public boolean isDirectory(){
		return kind == PathMappingKind.DIRECTORY;
	}
	
	/**
	 * Returns true if the mapping is a recursive directory mapping
	 * @return true if the mapping is a recursive directory mapping
	 */
	public boolean isRecursive(){
		return kind == PathMappingKind.RECURSIVE_DIRECTORY;
	}

	/**
	 * The enumeration of the different kind of path mapping
	 * @author Ibrahim Chaehoi
	 */
	private enum PathMappingKind{
		
		ASSET, DIRECTORY, RECURSIVE_DIRECTORY
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((kind == null) ? 0 : kind.hashCode());
		result = prime * result + ((mapping == null) ? 0 : mapping.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PathMapping other = (PathMapping) obj;
		if (kind != other.kind)
			return false;
		if (mapping == null) {
			if (other.mapping != null)
				return false;
		} else if (!mapping.equals(other.mapping))
			return false;
		return true;
	}

}
