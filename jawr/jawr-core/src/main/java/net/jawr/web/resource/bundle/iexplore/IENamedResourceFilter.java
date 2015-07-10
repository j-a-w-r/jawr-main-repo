/**
 * Copyright 2007 Jordi Hernández Sellés
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
package net.jawr.web.resource.bundle.iexplore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is used to determine which files are named with the convention of using a 
 * conditional comment expression as a suffix. 
 * 
 * @author Jordi Hernández Sellés
 */
public class IENamedResourceFilter {
	
	private static final String COMMENTS_REGEX = "^.*_ie(.js|.css)$";
	private static final String OPERATORS_REGEX = "(_(lt|lte|gt|gte))?(_\\d(\\.(\\d)*)?)?_ie(.js|.css)$";
	
	private static final Pattern COMMENTS_PATTERN = Pattern.compile(COMMENTS_REGEX, Pattern.CASE_INSENSITIVE);
	private static final Pattern OPERATORS_PATTERN = Pattern.compile(OPERATORS_REGEX, Pattern.CASE_INSENSITIVE);
	
	
	/**
	 * Finds all the paths in a collection which contain IE conditional comment 
	 * syntax, extracts all of them from the collection. 
	 * @param paths 
	 * @return A Map with all the extracted paths, in which the key is the corresponding IE expression and 
	 * 			the value is a List instance containing all the paths that match that expression. 
	 */
	public Map<String, List<String>> filterPathSet(Collection<String> paths) {
		
		Map<String, List<String>> expressions = new HashMap<String, List<String>>();
		List<String> toRemove = new ArrayList<String>();
		for(Iterator<String> it = paths.iterator(); it.hasNext();) {
			String path = it.next().toString();
			if( COMMENTS_PATTERN.matcher(path).matches() ){
				
				Matcher matcher = OPERATORS_PATTERN.matcher(path);
				matcher.find();
				String sufix = matcher.group();
				sufix = sufix.substring(0,sufix.lastIndexOf("."));
				String expressionKey = createExpressionKey(sufix);
				
				if( expressions.containsKey(expressionKey) ) {
					List<String> fileNames = expressions.get(expressionKey);
					fileNames.add(path);
				}
				else {
					List<String> fileNames = new ArrayList<String>();
					fileNames.add(path);
					expressions.put(expressionKey,fileNames);					
				}
				toRemove.add(path);			
			}
		}
		// Remove extracted paths from the source collection
		for(Iterator<String> it = toRemove.iterator(); it.hasNext();) {
			paths.remove(it.next());
		}
		
		return expressions;
	}
	
	/**
	 * Creates an IE conditional expression by transforming the sufix of a filename. 
	 * @param sufix
	 * @return
	 */
	private String createExpressionKey(String sufix) {
		String[] parts =  sufix.split("_");
		StringBuffer ret = new StringBuffer("[if ");
		boolean ieAdded = false;
		for (int i = 0; i < parts.length; i++) {					
			if("".equals(parts[i]))
					continue;
			if("ie".equals(parts[i]))
				break;
			else if(Pattern.matches("(lt|lte|gt|gte)", parts[i])) 
				ret.append(parts[i] + " ");
			else {
				ret.append("IE " + parts[i]);
				ieAdded = true;
			}	
		}
		if(!ieAdded)
			ret.append("IE");
		ret.append("]");
		return ret.toString();
	}

}
