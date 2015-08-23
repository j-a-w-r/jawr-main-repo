/**
 * Copyright 2007-2014 Jordi Hernández Sellés, Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.locale.message;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;

import net.jawr.web.resource.bundle.generator.JavascriptStringUtil;

/**
 * Creates a Json like structure (an object literal) from a set of message resource properties. 
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 */
public class BundleStringJsonifier {
	
	private Map<String, Object> keyMap;
	private Properties bundleValues;
	private boolean addQuoteToKey;
	
	private static final String FUNC = "p(";
	
	

	/**
	 * Constructor
	 * @param bundleValues bundle values
	 * @param addQuoteToKey the flag inidcating if quote should be added to the key
	 */
	public BundleStringJsonifier(Properties bundleValues, boolean addQuoteToKey) {
		super();
		this.addQuoteToKey = addQuoteToKey;
		this.bundleValues = bundleValues;
		this.keyMap = new TreeMap<String, Object>();
		Enumeration<Object> keys = this.bundleValues.keys();
		
		// Create map tree with all the message keys. 
		while(keys.hasMoreElements())
			processKey((String)keys.nextElement());
	}

	/**
	 * Creates a tree map structure from the message bundle. 
	 * Each messages key is split by the separator (.) and used as a
	 * key in the map, for whose value a new map is created, and so forth until all 
	 * keys form the tree structure. 
	 * For instance, 
	 * com.example.message=foo 
	 * com.example.somethingelse=baz 
	 * results in 
	 * [com [example [message,somethingelse] ] ]
	 * 
	 * @param key A key in the message bundle. 
	 */
	@SuppressWarnings("unchecked")
	private void processKey(String key) {
		StringTokenizer tk = new StringTokenizer(key,".");
		Map<String, Object> currentMap = this.keyMap;
		while(tk.hasMoreTokens()) {
			String token = tk.nextToken();
			if(!currentMap.containsKey(token))
				currentMap.put(token, new HashMap<String, Object>());
			currentMap = (Map<String, Object>) currentMap.get(token);
		}
	}
	
	/**
	 * Creates a javascript object literal representing a set of message resources. 
	 * 
	 * @return StringBuffer the object literal. 
	 */
	public StringBuffer serializeBundles() {
		StringBuffer sb = new StringBuffer("{");
		
		// Iterates over the 
		for(Iterator<String> it = keyMap.keySet().iterator(); it.hasNext();) {
			String currentKey = it.next();
			handleKey(sb,keyMap,currentKey,currentKey,!it.hasNext());
		}
		
		return sb.append("}");
	}
	
	/**
	 * Processes a leaf from the key map, adding its name and values recursively in a javascript object literal structure, 
	 * where values are invocations of a method that returns a function. 
	 * 
	 * @param sb Stringbuffer to append the javascript code. 
	 * @param currentLeaf Current Map from the keys tree. 
	 * @param currentKey Current key from the keys tree. 
	 * @param fullKey Key with ancestors as it appears in the message bundle(foo --> com.mycompany.foo)
	 * @param isLeafLast Wether this is the las item in the current leaf, to append a separator. 
	 */
	@SuppressWarnings("unchecked")
	private void handleKey( final StringBuffer sb, 
							Map<String, Object> currentLeaf, 
							String currentKey, 
							String fullKey, 
							boolean isLeafLast) {
		
		Map<String, Object> newLeaf = (Map<String, Object>) currentLeaf.get(currentKey);
		
		if(bundleValues.containsKey(fullKey)) {
			addValuedKey(sb,currentKey,fullKey);
			if(!newLeaf.isEmpty()) {
				sb.append(",({");
				for(Iterator<String> it = newLeaf.keySet().iterator(); it.hasNext();) {
					String newKey = it.next();
					handleKey(sb,newLeaf,newKey,fullKey + "." + newKey,!it.hasNext());
				}
				sb.append("}))");
			}
			else {
				sb.append(")");
			}
		}
		else if(!newLeaf.isEmpty()) {
			sb.append(getJsonKey(currentKey))
			  .append(":{");
			for(Iterator<String> it = newLeaf.keySet().iterator(); it.hasNext();) {
				String newKey = it.next();
				handleKey(sb,newLeaf,newKey,fullKey + "." + newKey,!it.hasNext());
			}
			sb.append("}");

		}
		if(!isLeafLast)
			sb.append(",");
	}
	
	/**
	 * Returns the json key for messages taking in account the addQuoteToKey attribute.
	 * @param key the key
	 * @return the json key
	 */
	private String getJsonKey(String key){
		String jsonKey = key;
		if(addQuoteToKey){
			jsonKey = JavascriptStringUtil.quote(key);
		}
		return jsonKey;
	}
	
	/**
	 * Add a key and its value to the object literal. 
	 * @param sb
	 * @param key
	 * @param fullKey
	 */
	private void addValuedKey(final StringBuffer sb, String key, String fullKey) {
		
		sb.append(getJsonKey(key))
		.append(":")
		.append(FUNC)
		.append(JavascriptStringUtil.quote(bundleValues.get(fullKey).toString()));
	}
}
