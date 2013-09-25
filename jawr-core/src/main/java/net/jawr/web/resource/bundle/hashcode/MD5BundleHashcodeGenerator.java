/**
 * Copyright 2010 Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.hashcode;

import java.io.IOException;
import java.io.Serializable;

import net.jawr.web.config.JawrConfig;
import net.jawr.web.exception.BundlingProcessException;
import net.jawr.web.resource.bundle.CheckSumUtils;

/**
 * This class defines the bundle hashcode generator which use MD5 as hashcode
 * algorithm
 * 
 * @author Ibrahim Chaehoi
 */
public class MD5BundleHashcodeGenerator implements BundleHashcodeGenerator,
		Serializable {

	/** The serial version UID */
	private static final long serialVersionUID = -5332600063100369915L;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.hash.BundleHashcodeGenerator#generateHashCode
	 * (net.jawr.web.config.JawrConfig, java.lang.String)
	 */
	public String generateHashCode(JawrConfig config, String content) {

		try {
			return CheckSumUtils.getMD5Checksum(content,
					config.getResourceCharset());
		} catch (IOException e) {
			throw new BundlingProcessException(
					"Unable to generate the bundle hashcode", e);
		}
	}

}
