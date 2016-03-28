/**
 * Copyright 2009-2016 Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.exception.BundlingProcessException;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.bundle.factory.util.PathNormalizer;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;

/**
 * This class defines utilities methods for Checksum.
 * 
 * @author Ibrahim Chaehoi
 */
public final class CheckSumUtils {

	/**
	 * Return the checksum of the path given in parameter,
	 * if the resource is not found, null will b returned. 
	 * @param url the url path to the resource file
	 * @param is the resource input stream 
	 * @param jawrConfig the jawrConfig
	 * @return checksum of the path given in parameter
	 * @throws IOException if an IO exception occurs.
	 * @throws ResourceNotFoundException if the resource is not found.
	 */
	public static String getChecksum(String url, ResourceReaderHandler rsReader, JawrConfig jawrConfig) throws IOException, ResourceNotFoundException {
		
		String checksum = null;
		InputStream is = null;
		
		boolean generatedBinaryResource = jawrConfig.getGeneratorRegistry().isGeneratedBinaryResource(url);
		
		try {
			
			if(!generatedBinaryResource){
				url = PathNormalizer.asPath(url);
			}
			
			is = rsReader.getResourceAsStream(url);
			
			if(is != null){
				checksum = CheckSumUtils.getChecksum(is, jawrConfig.getBinaryHashAlgorithm());
			}else{
				throw new ResourceNotFoundException(url);
			}
		}catch (FileNotFoundException e) {
			throw new ResourceNotFoundException(url);
		}
		finally {
			IOUtils.close(is);
		}
		
		return checksum;
	}
	
	/**
	 * Return the cache busted url associated to the url passed in parameter,
	 * if the resource is not found, null will b returned. 
	 * @param url the url path to the resource file
	 * @param is the resource input stream 
	 * @param jawrConfig the jawrConfig
	 * @return the cache busted url
	 * @throws IOException if an IO exception occurs.
	 * @throws ResourceNotFoundException if the resource is not found.
	 */
	public static String getCacheBustedUrl(String url, ResourceReaderHandler rsReader, JawrConfig jawrConfig) throws IOException, ResourceNotFoundException {
		
		String checksum = getChecksum(url, rsReader, jawrConfig);
		String result = JawrConstant.CACHE_BUSTER_PREFIX;
		boolean generatedBinaryResource = jawrConfig.getGeneratorRegistry().isGeneratedBinaryResource(url);
		
		if(generatedBinaryResource){
			int idx = url.indexOf(GeneratorRegistry.PREFIX_SEPARATOR);
			String generatorPrefix = url.substring(0, idx);
			url = url.substring(idx+1);
			result = generatorPrefix+"_cb";
		}
		
		result = result+checksum;
		
		if(!url.startsWith("/")){
			result = result + "/";
		}
		// Add the cache buster extension
		return PathNormalizer.asPath(result+url);
	}
	
	/**
	 * Returns the checksum value of the input stream taking in count the algorithm passed in parameter
	 * @param is the input stream
	 * @param algorithm the checksum algorithm
	 * @return the checksum value
	 * @throws IOException if an exception occurs.
	 */
	public static String getChecksum(InputStream is, String algorithm) throws IOException {
	
		if(algorithm.equals(JawrConstant.CRC32_ALGORITHM)){
			return getCRC32Checksum(is);
		}else if(algorithm.equals(JawrConstant.MD5_ALGORITHM)){
			return getMD5Checksum(is);
		}else{
			throw new BundlingProcessException("The checksum algorithm '"+algorithm+"' is not supported.\n" +
					"The only supported algorithm are 'CRC32' or 'MD5'.");
		}
	}
	
	/**
	 * Returns the CRC 32 Checksum of the input stream
	 * 
	 * @param is the input stream
	 * 
	 * @return the CRC 32 checksum of the input stream
	 * @throws IOException if an IO exception occurs
	 */
	public static String getCRC32Checksum(InputStream is) throws IOException {

		Checksum checksum = new CRC32();

		byte[] bytes = new byte[1024];
		int len = 0;

		while ((len = is.read(bytes)) >= 0) {
			checksum.update(bytes, 0, len);
		}

		return Long.toString(checksum.getValue());
	}

	/**
	 * Returns the MD5 Checksum of the string passed in parameter
	 * 
	 * @param is the input stream
	 * @param charset the content charset
	 * 
	 * @return the Checksum of the input stream
	 * @throws IOException if an IO exception occurs
	 */
	public static String getMD5Checksum(String str, Charset charset) throws IOException {

		InputStream is = new ByteArrayInputStream(str.getBytes(charset.name()));
		return getMD5Checksum(is);
	}
	
	/**
	 * Returns the MD5 Checksum of the string passed in parameter
	 * 
	 * @param is the input stream
	 * 
	 * @return the Checksum of the input stream
	 * @throws IOException if an IO exception occurs
	 */
	public static String getMD5Checksum(String str) throws IOException {

		InputStream is = new ByteArrayInputStream(str.getBytes());
		return getMD5Checksum(is);
	}
	
	/**
	 * Returns the MD5 Checksum of the input stream
	 * 
	 * @param is the input stream
	 * 
	 * @return the Checksum of the input stream
	 * @throws IOException if an IO exception occurs
	 */
	public static String getMD5Checksum(InputStream is) throws IOException {

		byte[] digest = null;
		try {
			MessageDigest md = MessageDigest.getInstance(JawrConstant.MD5_ALGORITHM);
			InputStream digestIs = new DigestInputStream(is, md);
			// read stream to EOF as normal...
			while (digestIs.read() != -1) {

			}
			digest = md.digest();
		} catch (NoSuchAlgorithmException e) {
			throw new BundlingProcessException("MD5 algorithm needs to be installed", e);
		}

		return new BigInteger(1, digest).toString(16);
	}
}
