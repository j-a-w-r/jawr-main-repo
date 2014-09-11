/**
 * Copyright 2009 Ibrahim Chaehoi
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
package net.jawr.web.config.jmx;

/**
 * This interface defines the MBean which manage the Jawr configuration for a servlet.
 * 
 * @author Ibrahim Chaehoi
 */
public interface JawrConfigManagerMBean {

	/**
	 * @return
	 * @see net.jawr.web.config.JawrConfig#getContextPathOverride()
	 */
	String getContextPathOverride();


	/**
	 * @param ctxPathOverride
	 * @see net.jawr.web.config.JawrConfig#setContextPathOverride(java.lang.String)
	 */
	void setContextPathOverride(String ctxPathOverride);

	/**
	 * @return
	 * @see net.jawr.web.config.JawrConfig#getContextPathOverride()
	 */
	String getContextPathSslOverride();


	/**
	 * @param ctxPathOverride
	 * @see net.jawr.web.config.JawrConfig#setContextPathOverride(java.lang.String)
	 */
	void setContextPathSslOverride(String ctxPathOverride);

	/**
	 * @param contextPathOverride
	 * @see net.jawr.web.config.JawrConfig#setUseContextPathOverrideInDebugMode(boolean)
	 */
	void setUseContextPathOverrideInDebugMode(boolean useCtxPathOverrideInDebugMode);

	/**
	 * @param contextPathOverride
	 * @see net.jawr.web.config.JawrConfig#getUseContextPathOverrideInDebugMode()
	 */
	boolean getUseContextPathOverrideInDebugMode();

	/**
	 * @return
	 * @see net.jawr.web.config.JawrConfig#getDebugOverrideKey()
	 */
	String getDebugOverrideKey();

	/**
	 * @param debugOverrideKey
	 * @see net.jawr.web.config.JawrConfig#setDebugOverrideKey(java.lang.String)
	 */
	void setDebugOverrideKey(String debugOverrideKey);

	/**
	 * @return
	 * @see net.jawr.web.config.JawrConfig#getDwrMapping()
	 */
	String getDwrMapping();

	/**
	 * @param dwrMapping
	 * @see net.jawr.web.config.JawrConfig#setDwrMapping(java.lang.String)
	 */
	void setDwrMapping(String dwrMapping);

	/**
	 * @return
	 * @see net.jawr.web.config.JawrConfig#getBinaryResourcesDefinition()
	 */
	String getBinaryResourcesDefinition();

	/**
	 * @param imgResourcesDef
	 * @see net.jawr.web.config.JawrConfig#setBinaryResourcesDefinition(java.lang.String)
	 */
	void setBinaryResourcesDefinition(String imgResourcesDef);

	/**
	 * @return the binary hash algorithm
	 * @see net.jawr.web.config.JawrConfig#getBinaryHashAlgorithm()
	 */
	String getBinaryHashAlgorithm();

	/**
	 * @param binaryHashAlgorithm the binary hash algorithm to set
	 * @see net.jawr.web.config.JawrConfig#setBinaryHashAlgorithm(java.lang.String)
	 */
	void setBinaryHashAlgorithm(String binaryHashAlgorithm);

	/**
	 * @return
	 * @see net.jawr.web.config.JawrConfig#isDebugModeOn()
	 */
	boolean isDebugModeOn();

	/**
	 * @param debugMode
	 * @see net.jawr.web.config.JawrConfig#setDebugModeOn(boolean)
	 */
	void setDebugModeOn(boolean debugMode);

	/**
	 * @return
	 * @see net.jawr.web.config.JawrConfig#isGzipResourcesForIESixOn()
	 */
	boolean isGzipResourcesForIESixOn();

	/**
	 * @param gzipForIE6On
	 * @see net.jawr.web.config.JawrConfig#setGzipResourcesForIESixOn(boolean)
	 */
	void setGzipResourcesForIESixOn(boolean gzipForIE6On);

	/**
	 * @return
	 * @see net.jawr.web.config.JawrConfig#isGzipResourcesModeOn()
	 */
	boolean isGzipResourcesModeOn();

	/**
	 * @param gzipModeOn
	 * @see net.jawr.web.config.JawrConfig#setGzipResourcesModeOn(boolean)
	 */
	void setGzipResourcesModeOn(boolean gzipModeOn);
	
	/**
	 * @return
	 * @see net.jawr.web.config.JawrConfig#isCssClasspathImageHandledByClasspathCss()
	 */
	boolean isCssClasspathImageHandledByClasspathCss();

	/**
	 * @param useClasspathCssImgServlet
	 * @see net.jawr.web.config.JawrConfig#setCssClasspathImageHandledByClasspathCss(boolean)
	 */
	void setCssClasspathImageHandledByClasspathCss(boolean useClasspathCssImgServlet);
	
	/**
	 * @param charsetName
	 * @see net.jawr.web.config.JawrConfig#getCharsetName(java.lang.String)
	 */
	String getCharsetName();

	/**
	 * @param charsetName
	 * @see net.jawr.web.config.JawrConfig#setCharsetName(java.lang.String)
	 */
	void setCharsetName(String charsetName);

	/**
	 * @param cssLinkFlavor
	 * @see net.jawr.web.config.JawrConfig#setCssLinkFlavor(java.lang.String)
	 */
	String getCssLinkFlavor();

	/**
	 * @param cssLinkFlavor
	 * @see net.jawr.web.config.JawrConfig#setCssLinkFlavor(java.lang.String)
	 */
	void setCssLinkFlavor(String cssLinkFlavor);

	/**
	 * Returns the flag which defines if we should process the bundle at server startup. defaults to false.
	 * @return the flag which defines if we should process the bundle at server startup.
	 */
	boolean isUseBundleMapping();

	/**
	 * Sets the flag which defines if we should process the bundle at server startup. 
	 * @param usBundleMapping the flag to set
	 */
	void setUseBundleMapping(boolean usBundleMapping);

	/** 
	 * Returns the jawr working directory path
	 * @return the jawr working directory path
	 */
	String getJawrWorkingDirectory();
	
	/** 
	 * Sets the jawr working directory path
	 * @param jawrWorkingDirectory the path to set
	 */
	void setJawrWorkingDirectory(String jawrWorkingDirectory);
	
	/**
	 * Refresh the configuration. 
	 */
	void refreshConfig();
	
}
