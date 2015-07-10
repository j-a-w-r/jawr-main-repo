/**
 * Copyright 2007-2013 Jordi Hernández Sellés, Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.postprocess.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.util.Iterator;

import net.jawr.web.exception.BundlingProcessException;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.postprocess.AbstractChainedResourceBundlePostProcessor;
import net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus;
import net.jawr.web.resource.bundle.postprocess.PostProcessFactoryConstant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Meant to be used after compression, this postprocessor will include the
 * license comments that may be desired to send with each bundle, including
 * those required by open source licenses.
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 * 
 */
public class LicensesIncluderPostProcessor extends
		AbstractChainedResourceBundlePostProcessor {

	/** The logger */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(LicensesIncluderPostProcessor.class);

	/**
	 * Constructor
	 */
	public LicensesIncluderPostProcessor() {
		super(PostProcessFactoryConstant.LICENSE_INCLUDER);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.postprocess.impl.
	 * AbstractChainedResourceBundlePostProcessor
	 * #doPostProcessBundle(net.jawr.web.resource.bundle.JoinableResourceBundle,
	 * java.lang.StringBuffer)
	 */
	protected StringBuffer doPostProcessBundle(BundleProcessingStatus status,
			StringBuffer bundleData) throws IOException {

		JoinableResourceBundle bundle = status.getCurrentBundle();
		Charset charset = status.getJawrConfig().getResourceCharset();
		if (bundle.getLicensesPathList().size() == 0)
			return bundleData;

		ByteArrayOutputStream baOs = new ByteArrayOutputStream();
		WritableByteChannel wrChannel = Channels.newChannel(baOs);
		Writer writer = Channels.newWriter(wrChannel, charset.name());
		BufferedWriter bwriter = new BufferedWriter(writer);

		for (Iterator<String> it = bundle.getLicensesPathList().iterator(); it
				.hasNext();) {
			String path = it.next();
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("Adding license file: " + path);

			Reader rd = null;
			try {
				rd = status.getRsReader().getResource(path);
			} catch (ResourceNotFoundException e) {
				throw new BundlingProcessException(
						"Unexpected ResourceNotFoundException when reading a sorting file ["
								+ path + "]");
			}

			// Make a buffered reader, to read line by line.
			BufferedReader bRd = new BufferedReader(rd);

			String line = bRd.readLine();

			// Write each line and the corresponding new line.
			while (line != null) {
				bwriter.write(line);
				if (((line = bRd.readLine()) != null) || it.hasNext())
					bwriter.newLine();
			}
			bRd.close();
		}
		bwriter.close();
		return new StringBuffer(baOs.toString(charset.name()))
				.append(bundleData);
	}

}
