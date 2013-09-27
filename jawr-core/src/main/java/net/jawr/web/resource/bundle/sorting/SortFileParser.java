/**
 * Copyright 2007-2012 Jordi Hernández Sellés, Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.sorting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.jawr.web.exception.BundlingProcessException;
import net.jawr.web.resource.bundle.factory.util.PathNormalizer;

/**
 * Reads a sorting file and generates a List containing the sorted members of a bundle
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 */
public class SortFileParser {
	
	/** The reader  */
	private Reader reader;
	
	/** The available resources */
	private Collection<String> availableResources;
	
	/** The directory name */
	private String dirName;
	
	/**
	 * Constructor
	 * @param reader the reader
	 * @param availableResources the available resources
	 * @param dirName the directory name
	 */
	public SortFileParser(Reader reader,Collection<String> availableResources,String dirName) {
		super();
		this.reader = reader;
		this.availableResources = availableResources;
		this.dirName = dirName;
	}
	
	/**
	 * Creates a list with the ordered resource names and returns it. 
	 * If a resource is not in the resources dir, it is ignored. 
	 * @return the list of ordered resource names
	 */
	public List<String> getSortedResources()
	{
		List<String> resources = new ArrayList<String>();
		BufferedReader bf = new BufferedReader(reader);
		String res;
		try {
			while((res = bf.readLine()) != null)
			{
				String name = PathNormalizer.normalizePath(res.trim());
				
				for(Iterator<String> it = availableResources.iterator();it.hasNext();) {
					String available = (String)it.next();
					if(PathNormalizer.normalizePath(available).equals(name)) {
						if(name.endsWith(".js") || name.endsWith(".css"))
							resources.add(PathNormalizer.joinPaths(dirName, name));
						else 
							resources.add(PathNormalizer.joinPaths(dirName, name + "/"));
						availableResources.remove(available);
						break;
					}					
				}
			}
		} catch (IOException e) {
			throw new BundlingProcessException("Unexpected IOException reading sort file",e);
		}
		return resources;
	}

}
