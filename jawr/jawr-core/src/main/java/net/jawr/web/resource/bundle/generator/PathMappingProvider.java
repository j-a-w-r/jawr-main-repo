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
package net.jawr.web.resource.bundle.generator;

import java.util.List;

import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.mappings.PathMapping;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;

/**
 * The interface of generators which are able to provide specific path mapping
 * for generated resource
 * 
 * For example : the ResourceBundleMessagesGenerator will map the path
 * messages:net.jawr.messages to
 * {webappdir}/WEB-INF/classes/net/jawr/messages*.properties
 * 
 * Simple generators will not have to implement this interface as the
 * PathMapping to the resource will be the location of the resource itself. For
 * example : /less/my-theme.less will be mapped to
 * {webappdir}/less/my-theme.less
 * 
 * @author ibrahim Chaehoi
 */
public interface PathMappingProvider {

	/**
	 * Returns the path mappings for the generated resource
	 * 
	 * @param bundle
	 *            the resource bundle
	 * @param path
	 *            the path
	 * @param rsReader
	 *            the resource reader handler
	 * 
	 * @return the path mapping
	 */
	List<PathMapping> getPathMappings(JoinableResourceBundle bundle, String path, ResourceReaderHandler rsReader);

}
