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
package net.jawr.web.config.jmx;

import java.util.List;
import java.util.Properties;

import net.jawr.web.config.JawrConfig;
import net.jawr.web.servlet.JawrRequestHandler;

/**
 * This class defines the MBean which manage the Jawr configuration for a
 * servlet.
 * 
 * @author Ibrahim Chaehoi
 */
public class JawrConfigManager implements JawrConfigManagerMBean {

	/** The request handler */
	private final JawrRequestHandler requestHandler;

	/** The configuration properties */
	private final Properties configProperties;

	/**
	 * Constructor
	 * 
	 * @param requestHandler
	 *            the request handler
	 * @param properties
	 *            the properties
	 */
	public JawrConfigManager(JawrRequestHandler requestHandler, Properties properties) {
		this.requestHandler = requestHandler;
		this.configProperties = (Properties) properties.clone();
	}

	/**
	 * @return
	 * @see net.jawr.web.config.JawrConfig#getContextPathOverride()
	 */
	@Override
	public String getContextPathOverride() {
		return configProperties.getProperty(JawrConfig.JAWR_URL_CONTEXTPATH_OVERRIDE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.config.jmx.JawrConfigManagerMBean#getContextPathSslOverride(
	 * )
	 */
	@Override
	public String getContextPathSslOverride() {

		return configProperties.getProperty(JawrConfig.JAWR_URL_CONTEXTPATH_SSL_OVERRIDE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrConfigManagerMBean#
	 * getUseContextPathOverrideInDebugMode()
	 */
	@Override
	public boolean getUseContextPathOverrideInDebugMode() {

		return Boolean
				.parseBoolean(configProperties.getProperty(JawrConfig.JAWR_USE_URL_CONTEXTPATH_OVERRIDE_IN_DEBUG_MODE));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrConfigManagerMBean#getDebugOverrideKey()
	 */
	@Override
	public String getDebugOverrideKey() {
		return configProperties.getProperty(JawrConfig.JAWR_DEBUG_OVERRIDE_KEY);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrConfigManagerMBean#getDwrMapping()
	 */
	@Override
	public String getDwrMapping() {
		return configProperties.getProperty(JawrConfig.JAWR_DWR_MAPPING);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrConfigManagerMBean#
	 * getBinaryResourcesDefinition()
	 */
	@Override
	public String getBinaryResourcesDefinition() {
		return configProperties.getProperty(JawrConfig.JAWR_BINARY_RESOURCES);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.config.jmx.JawrConfigManagerMBean#getBinaryHashAlgorithm()
	 */
	@Override
	public String getBinaryHashAlgorithm() {
		return configProperties.getProperty(JawrConfig.JAWR_BINARY_HASH_ALGORITHM);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrConfigManagerMBean#isDebugModeOn()
	 */
	@Override
	public boolean isDebugModeOn() {
		return Boolean.parseBoolean(configProperties.getProperty(JawrConfig.JAWR_DEBUG_ON));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.config.jmx.JawrConfigManagerMBean#isGzipResourcesForIESixOn(
	 * )
	 */
	@Override
	public boolean isGzipResourcesForIESixOn() {
		return Boolean.parseBoolean(configProperties.getProperty(JawrConfig.JAWR_GZIP_IE6_ON));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.config.jmx.JawrConfigManagerMBean#isGzipResourcesModeOn()
	 */
	@Override
	public boolean isGzipResourcesModeOn() {
		return Boolean.parseBoolean(configProperties.getProperty(JawrConfig.JAWR_GZIP_ON));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrConfigManagerMBean#
	 * isCssClasspathImageHandledByClasspathCss()
	 */
	@Override
	public boolean isCssClasspathImageHandledByClasspathCss() {
		return Boolean.parseBoolean(configProperties.getProperty(JawrConfig.JAWR_CSS_CLASSPATH_HANDLE_IMAGE));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.config.jmx.JawrConfigManagerMBean#getJawrWorkingDirectory()
	 */
	@Override
	public String getJawrWorkingDirectory() {
		return configProperties.getProperty(JawrConfig.JAWR_WORKING_DIRECTORY);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrConfigManagerMBean#isUseBundleMapping()
	 */
	@Override
	public boolean isUseBundleMapping() {
		return Boolean.parseBoolean(configProperties.getProperty(JawrConfig.JAWR_USE_BUNDLE_MAPPING));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.config.jmx.JawrConfigManagerMBean#setCharsetName(java.lang.
	 * String)
	 */
	@Override
	public void setCharsetName(String charsetName) {
		configProperties.setProperty(JawrConfig.JAWR_CHARSET_NAME, charsetName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrConfigManagerMBean#getCharsetName()
	 */
	@Override
	public String getCharsetName() {
		return configProperties.getProperty(JawrConfig.JAWR_CHARSET_NAME);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.config.jmx.JawrConfigManagerMBean#setContextPathOverride(
	 * java.lang.String)
	 */
	@Override
	public void setContextPathOverride(String contextPathOverride) {
		configProperties.setProperty(JawrConfig.JAWR_URL_CONTEXTPATH_OVERRIDE, contextPathOverride);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.config.jmx.JawrConfigManagerMBean#setContextPathSslOverride(
	 * java.lang.String)
	 */
	@Override
	public void setContextPathSslOverride(String contextPathOverride) {

		configProperties.setProperty(JawrConfig.JAWR_URL_CONTEXTPATH_SSL_OVERRIDE, contextPathOverride);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrConfigManagerMBean#
	 * setUseContextPathOverrideInDebugMode(boolean)
	 */
	@Override
	public void setUseContextPathOverrideInDebugMode(boolean useContextPathOverrideInDebugMode) {

		configProperties.setProperty(JawrConfig.JAWR_USE_URL_CONTEXTPATH_OVERRIDE_IN_DEBUG_MODE,
				Boolean.toString(useContextPathOverrideInDebugMode));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.config.jmx.JawrConfigManagerMBean#setCssLinkFlavor(java.lang
	 * .String)
	 */
	@Override
	public void setCssLinkFlavor(String cssLinkFlavor) {
		configProperties.setProperty(JawrConfig.JAWR_CSSLINKS_FLAVOR, cssLinkFlavor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrConfigManagerMBean#getCssLinkFlavor()
	 */
	@Override
	public String getCssLinkFlavor() {
		return configProperties.getProperty(JawrConfig.JAWR_CSSLINKS_FLAVOR);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.config.jmx.JawrConfigManagerMBean#setDebugModeOn(boolean)
	 */
	@Override
	public void setDebugModeOn(boolean debugMode) {
		configProperties.setProperty(JawrConfig.JAWR_DEBUG_ON, Boolean.toString(debugMode));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.config.jmx.JawrConfigManagerMBean#setDebugOverrideKey(java.
	 * lang.String)
	 */
	@Override
	public void setDebugOverrideKey(String debugOverrideKey) {
		configProperties.setProperty(JawrConfig.JAWR_DEBUG_OVERRIDE_KEY, debugOverrideKey);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.config.jmx.JawrConfigManagerMBean#setDwrMapping(java.lang.
	 * String)
	 */
	@Override
	public void setDwrMapping(String dwrMapping) {
		configProperties.setProperty(JawrConfig.JAWR_DWR_MAPPING, dwrMapping);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.config.jmx.JawrConfigManagerMBean#setGzipResourcesForIESixOn
	 * (boolean)
	 */
	@Override
	public void setGzipResourcesForIESixOn(boolean gzipResourcesForIESixOn) {
		configProperties.setProperty(JawrConfig.JAWR_GZIP_IE6_ON, Boolean.toString(gzipResourcesForIESixOn));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.config.jmx.JawrConfigManagerMBean#setGzipResourcesModeOn(
	 * boolean)
	 */
	@Override
	public void setGzipResourcesModeOn(boolean gzipResourcesModeOn) {
		configProperties.setProperty(JawrConfig.JAWR_GZIP_ON, Boolean.toString(gzipResourcesModeOn));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrConfigManagerMBean#
	 * setBinaryResourcesDefinition(java.lang.String)
	 */
	@Override
	public void setBinaryResourcesDefinition(String imageResourcesDefinition) {
		configProperties.setProperty(JawrConfig.JAWR_BINARY_RESOURCES, imageResourcesDefinition);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.config.jmx.JawrConfigManagerMBean#setBinaryHashAlgorithm(
	 * java.lang.String)
	 */
	@Override
	public void setBinaryHashAlgorithm(String imageHashAlgorithm) {
		configProperties.setProperty(JawrConfig.JAWR_BINARY_HASH_ALGORITHM, imageHashAlgorithm);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrConfigManagerMBean#
	 * setCssClasspathImageHandledByClasspathCss(boolean)
	 */
	@Override
	public void setCssClasspathImageHandledByClasspathCss(boolean useClasspathCssImgServlet) {
		configProperties.setProperty(JawrConfig.JAWR_CSS_CLASSPATH_HANDLE_IMAGE,
				Boolean.toString(useClasspathCssImgServlet));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.config.jmx.JawrConfigManagerMBean#setJawrWorkingDirectory(
	 * java.lang.String)
	 */
	@Override
	public void setJawrWorkingDirectory(String jawrWorkingDirectory) {
		configProperties.setProperty(JawrConfig.JAWR_WORKING_DIRECTORY, jawrWorkingDirectory);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrConfigManagerMBean#setUseBundleMapping(
	 * boolean)
	 */
	@Override
	public void setUseBundleMapping(boolean usBundleMapping) {
		configProperties.setProperty(JawrConfig.JAWR_USE_BUNDLE_MAPPING, Boolean.toString(usBundleMapping));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrConfigManagerMBean#refreshConfig()
	 */
	@Override
	public void refreshConfig() {

		requestHandler.configChanged(configProperties);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrConfigManagerMBean#rebuildDirtyDundles()
	 */
	@Override
	public void rebuildDirtyBundles() {
		requestHandler.rebuildDirtyBundles();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrConfigManagerMBean#getDirtyBundles()
	 */
	@Override
	public List<String> getDirtyBundleNames() {
		return requestHandler.getDirtyBundleNames();
	}

}
