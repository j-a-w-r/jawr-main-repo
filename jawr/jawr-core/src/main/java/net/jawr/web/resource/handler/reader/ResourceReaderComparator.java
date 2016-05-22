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
package net.jawr.web.resource.handler.reader;

import java.util.Comparator;

import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.resource.bundle.generator.ResourceGenerator;
import net.jawr.web.resource.bundle.generator.resolver.ResourceGeneratorResolver.ResolverType;
import net.jawr.web.resource.bundle.global.preprocessor.css.smartsprites.CssSmartSpritesResourceReader;

/**
 * This class defines comparator for resource readers
 * 
 * @author Ibrahim Chaehoi
 */
public class ResourceReaderComparator implements Comparator<ResourceReader> {

	/**
	 * The flag indicating if the base directory has high priority in resource
	 * reader list
	 */
	private boolean baseDirHighPriority;

	/**
	 * Constructor
	 * 
	 * @param config
	 *            The Jawr config
	 */
	public ResourceReaderComparator(JawrConfig config) {
		this.baseDirHighPriority = Boolean
				.valueOf(config.getProperty(JawrConstant.JAWR_BASECONTEXT_DIRECTORY_HIGH_PRIORITY));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(ResourceReader o1, ResourceReader o2) {

		return getPriority(o1) - getPriority(o2);
	}

	/**
	 * Returns the priority of the resource reader
	 * 
	 * if the FileSystemResourceReader is configured with high priority the list of priorities is as followed :
	 * 
	 * 5 - ServletContextResourceReader
	 * 4 - FileSystemResourceReader
	 * 3 - Generator with prefixed path
	 * 2 - Generator with suffixed path 
	 * 1 - CssSmartSpriteResourceReader
	 * 
	 * if the FileSystemResourceReader is configured with high priority the list of priorities is as followed :
	 *	
	 * 5 - FileSystemResourceReader
	 * 4 - ServletContextResourceReader
	 * 3 - Generator with prefixed path
	 * 2 - Generator with suffixed path 
	 * 1 - CssSmartSpriteResourceReader
	 * 
	 * @param o1 the resource reader
	 * 
	 * @return the priority of the resource reader
	 */
	private int getPriority(ResourceReader o1){
		
		int priority = 0;
		if (o1 instanceof ServletContextResourceReader){
			priority = baseDirHighPriority ? 5 : 4;
		}else if (o1 instanceof FileSystemResourceReader){
			priority = baseDirHighPriority ? 4 : 5;
		}else if (o1 instanceof CssSmartSpritesResourceReader) {
			priority = 1;
		}else if(o1 instanceof ResourceGenerator){
			if(((ResourceGenerator)o1).getResolver().getType().equals(ResolverType.PREFIXED)){
				priority = 3; 
			}else{
				priority = 2; 
			}
		}
		return priority;
	}
}
