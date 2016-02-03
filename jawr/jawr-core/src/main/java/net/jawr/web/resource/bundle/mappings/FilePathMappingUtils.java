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

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;

/**
 * The utility class for file path mapping
 * 
 * @author Ibrahim Chaehoi
 */
public class FilePathMappingUtils {

	/**The Logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(FilePathMappingUtils.class);
	
	/**
	 * Adds the File path mapping of linked resources for the bundle using the resource reader handler
	 * 
	 * @param bundle the bundle
	 * @param path the resource path
	 * @param rsHandler the resource reader handler
	 */
	public static void addLinkedFilePathMapping(JoinableResourceBundle bundle, String path,
			ResourceReaderHandler rsHandler) {

		String filePath = rsHandler.getFilePath(path);
		if(filePath != null){
			
			File f = new File(filePath);
			if(f.exists()){
				FilePathMapping fPathMapping = new FilePathMapping(bundle, filePath, f.lastModified());
				bundle.getLinkedFilePathMappings().add(fPathMapping);
			}else{
				if(LOGGER.isDebugEnabled()){
					LOGGER.debug("The file path '"+filePath+"'  associated to the URL '"+path+"' doesn't exixts.");
				}
			}
		}
	}

}
