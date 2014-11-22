/**
 * Copyright 2014  Ibrahim Chaehoi
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

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;

import net.jawr.web.exception.BundlingProcessException;
import net.jawr.web.resource.bundle.IOUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * ConfigPropertiesSource implementation that reads its values from a .json
 * file.
 * 
 * This file reads a JSON file and converts it for Jawr in a Properties object.
 * This class allows the user to not use quote for field name even if it's not
 * in the JSON standard. The Array values are converted to String by joining
 * their values and using a comma as a separator.
 * 
 * Here is a sample of JSON configuration :
 * 
 * <pre>
 * {
 * 	jawr : { 
 * 		debug : {
 * 			on : false,
 * 			"ie.force.css.bundle" : true
 * 			},
 * 		"gzip.on" : true,
 * 		"charset.name" : "UTF-8",
 * 		"use.bundle.mapping" : false,
 * 		"factory.use.orphans.mapper" : false,
 * 		"strict.mode" : false,
 * 		"bundle.hashcode.generator" : "MD5",
 * 		custom : {
 * 			generators : [ "com.mycomp.generator.ImgGenerator", "com.mycomp.generator.CssGenerator" ],
 * 			...
 * </pre>
 * 
 * @author Ibrahim Chaehoi
 */
public class JsonPropertiesSource extends PropsFilePropertiesSource {

	/** The property field name separator */
	private static final String FIELD_NAME_SEPARATOR = ".";
	
	/** The array value separator */
	private static final String ARRAY_VALUE_SEPARATOR = ",";
	
	/**
	 * Generates the properties from a JSON node
	 * 
	 * @param node
	 *            the JSON node
	 * @param props
	 *            the properties to populate
	 */
	private void generateProperties(JsonNode node, String parentPrefix,
			Properties props) {

		Iterator<Entry<String, JsonNode>> fields = node.fields();
		while (fields.hasNext()) {
			Entry<String, JsonNode> entry = fields.next();
			String fieldName = entry.getKey();
			JsonNode jsonNode = entry.getValue();
			String nodePrefix = parentPrefix == null ? fieldName : parentPrefix
					+ FIELD_NAME_SEPARATOR + fieldName;
			if (jsonNode.isTextual() || jsonNode.isBoolean()) {
				props.put(nodePrefix, jsonNode.asText());
			} else if (jsonNode.isNumber()) {
				props.put(nodePrefix, Integer.toString(jsonNode.asInt()));
			} else if (jsonNode.isArray()) {
				String arrayValue = convertToString(jsonNode);
				props.put(nodePrefix, arrayValue);
			}
			generateProperties(jsonNode, nodePrefix, props);
		}
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.factory.util.PropsFilePropertiesSource#loadConfig(java.util.Properties, java.lang.String, java.io.InputStream)
	 */
	@Override
	protected void loadConfig(Properties props, String path, InputStream is) {
		try {

			// load properties into a Properties object
			String jsonContent = IOUtils.toString(is);
			// create an ObjectMapper instance.
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES,
					true);

			// use the ObjectMapper to read the json string and create a tree
			JsonNode node = mapper.readTree(jsonContent);
			generateProperties(node, null, props);
		} catch (JsonParseException e) {

			throw new BundlingProcessException(
					"jawr configuration could not be loaded at " + path
							+ FIELD_NAME_SEPARATOR, e);
		} catch (IOException e) {
			throw new BundlingProcessException(
					"jawr configuration could not be loaded at " + path
							+ FIELD_NAME_SEPARATOR, e);
		}
	}

	/**
	 * Converts the json array node to a String
	 * 
	 * @param jsonArrayNode
	 *            the json array node to convert
	 * @return a string corresponding to the json array node
	 */
	private String convertToString(JsonNode jsonArrayNode) {

		StringBuilder strBuilder = new StringBuilder();
		Iterator<JsonNode> nodeIterator = jsonArrayNode.iterator();
		while (nodeIterator.hasNext()) {
			JsonNode jsonNode = (JsonNode) nodeIterator.next();
			strBuilder.append(jsonNode.asText());
			if (nodeIterator.hasNext()) {
				strBuilder.append(ARRAY_VALUE_SEPARATOR);
			}
		}
		return strBuilder.toString();
	}
}
