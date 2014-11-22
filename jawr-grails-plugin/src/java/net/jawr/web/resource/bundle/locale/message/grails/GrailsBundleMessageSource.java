/**
 * Copyright 2014 Ibrahim Chaehoi 
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
package net.jawr.web.resource.bundle.locale.message.grails;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import net.jawr.web.resource.bundle.locale.LocaleUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

/**
 * This class defines a MessageSource used by the for Jawr Grails plugin
 * 
 * @author Ibrahim Chaehoi
 */
public class GrailsBundleMessageSource extends
		ReloadableResourceBundleMessageSource {

	/** The logger */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(GrailsBundleMessageSource.class);

	/** The Charset ISO-8859-1 */
	private static final String CHARSET_ISO_8859_1 = "ISO-8859-1";

	/** The Charset UTF-8 */
	private static final String CHARSET_UTF_8 = "UTF-8";

	/** The message filters */
	private List<String> filters = Collections.emptyList();

	/** The flag indicating if the war is deployed */
	private boolean warDeployed;
	
	/**
	 * Constructor
	 * 
	 * @param warDeployed
	 *            the flag indicating if the war is deployed
	 */
	public GrailsBundleMessageSource(boolean warDeployed) {
		this.warDeployed = warDeployed;
	}

	/**
	 * Sets the filters
	 * 
	 * @param filters
	 *            the filters
	 */
	public void setFilters(List<String> filters) {
		this.filters = filters;
	}

	/**
	 * Returns all the messages
	 * 
	 * @return all the messages
	 */
	public Properties getAllMessages(Locale locale) {

		Properties props = new Properties();
		Properties mergedProps = null;
		if (locale == null) {
			locale = Locale.getDefault();
		}
		mergedProps = getMergedProperties(locale).getProperties();

		Set<Entry<Object, Object>> entries = mergedProps.entrySet();
		for (Entry<Object, Object> entry : entries) {
			String key = (String) entry.getKey();
			if (LocaleUtils.matchesFilter(key, filters)) {

				try {
					// Use the property encoding of the file
					String msg = getMessage(key, new Object[0], locale);
					
					// When war is deployed, the resource bundle are encoded in ISO 8859-1
					// and otherwise in UTF-8 
					// Check the following link for more detail https://grails.org/Internationalization
					if(!warDeployed){
						msg = new String(msg.getBytes(CHARSET_ISO_8859_1),
								CHARSET_UTF_8);
					}

					props.put(key, msg);
				} catch (NoSuchMessageException e) {
					// This is expected, so it's OK to have an empty catch
					// block.
					if (LOGGER.isDebugEnabled())
						LOGGER.debug("Message key [" + key + "] not found.");
				} catch (UnsupportedEncodingException e) {
					LOGGER.warn("Unable to convert value of message bundle associated to key '"
							+ key + "' because the charset is unknown");
				}
			}
		}
		return props;
	}

}