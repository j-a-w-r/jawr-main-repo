/**
 * Copyright 2009-2012 Ibrahim Chaehoi
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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.jawr.web.JawrConstant;
import net.jawr.web.exception.JmxConfigException;
import net.jawr.web.util.PropertyUtils;

/**
 * This interface defines the MBean which manage the Jawr configuration for a we application, so it will affect all JawrConfigManagerMBean associated
 * to Jawr Servlets.
 * 
 * @author Ibrahim Chaehoi
 */
public class JawrApplicationConfigManager implements
		JawrApplicationConfigManagerMBean {

	
	private static final String CHARSET_NAME = "charsetName";

	private static final String DEBUG_MODE_ON = "debugModeOn";

	private static final String DEBUG_OVERRIDE_KEY = "debugOverrideKey";

	private static final String GZIP_RESOURCES_FOR_IE_SIX_ON = "gzipResourcesForIESixOn";

	private static final String GZIP_RESOURCES_MODE_ON = "gzipResourcesModeOn";

	private static final String CONTEXT_PATH_OVERRIDE = "contextPathOverride";

	private static final String CONTEXT_PATH_SSL_OVERRIDE = "contextPathSslOverride";

	private static final String USE_CONTEXT_PATH_OVERRIDE_IN_DEBUG_MODE = "useContextPathOverrideInDebugMode";

	private static final String JAWR_WORKING_DIRECTORY = "jawrWorkingDirectory";

	private static final String USE_BUNDLE_MAPPING = "useBundleMapping";

	/** The message of the property, when the values are not equals for the different configuration manager */
	private static final String NOT_IDENTICAL_VALUES = "Value for this property are not identical";

	/** The message when an error occured during the retrieve of the property value */
	private static final String ERROR_VALUE = "An error occured while retrieving the value for this property";

	/** The configuration manager for the Javascript handler */
	private JawrConfigManagerMBean jsMBean;

	/** The configuration manager for the CSS handler */
	private JawrConfigManagerMBean cssMBean;

	/** The configuration manager for the binary resource handler (images, fonts, ...) */
	private JawrConfigManagerMBean binaryMBean;

	/** The set of session ID for which all requests will be executed in debug mode */
	private Set<String> debugSessionIdSet = new HashSet<String>();

	/**
	 * Constructor
	 */
	public JawrApplicationConfigManager() {

	}

	/**
	 * Sets the configuration manager for the Javascript handler
	 * 
	 * @param jsMBean the configuration manager to set
	 */
	public void setJsMBean(final JawrConfigManagerMBean jsMBean) {
		this.jsMBean = jsMBean;
	}

	/**
	 * Sets the configuration manager for the CSS handler
	 * 
	 * @param cssMBean the configuration manager to set
	 */
	public void setCssMBean(final JawrConfigManagerMBean cssMBean) {
		this.cssMBean = cssMBean;
	}

	/**
	 * Sets the configuration manager for the binary handler
	 * 
	 * @param binaryMBean the configuration manager to set
	 */
	public void setBinaryMBean(final JawrConfigManagerMBean binaryMBean) {
		this.binaryMBean = binaryMBean;
	}

	/**
	 * Returns the list of initialized configuration managers.
	 * 
	 * @return the list of initialized configuration managers.
	 */
	private List<JawrConfigManagerMBean> getInitializedConfigurationManagers() {

		final List<JawrConfigManagerMBean> mBeans = new ArrayList<JawrConfigManagerMBean>();
		if (jsMBean != null) {
			mBeans.add(jsMBean);
		}

		if (cssMBean != null) {
			mBeans.add(cssMBean);
		}
		if (binaryMBean != null) {
			mBeans.add(binaryMBean);
		}

		return mBeans;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#getCharsetName()
	 */
	public String getCharsetName() {

		return getStringValue(CHARSET_NAME);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#getDebugOverrideKey()
	 */
	public String getDebugOverrideKey() {

		return getStringValue(DEBUG_OVERRIDE_KEY);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#isDebugModeOn()
	 */
	public String getDebugModeOn() {

		return getStringValue(DEBUG_MODE_ON);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#isGzipResourcesForIESixOn()
	 */
	public String getGzipResourcesForIESixOn() {

		return getStringValue(GZIP_RESOURCES_FOR_IE_SIX_ON);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#isGzipResourcesModeOn()
	 */
	public String getGzipResourcesModeOn() {

		return getStringValue(GZIP_RESOURCES_MODE_ON);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#getContextPathOverride()
	 */
	public String getContextPathOverride() {
		return getStringValue(CONTEXT_PATH_OVERRIDE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#getContextPathSslOverride()
	 */
	public String getContextPathSslOverride() {

		return getStringValue(CONTEXT_PATH_SSL_OVERRIDE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#getUseContextPathOverrideInDebugMode()
	 */
	public String getUseContextPathOverrideInDebugMode() {
	
		return getStringValue(USE_CONTEXT_PATH_OVERRIDE_IN_DEBUG_MODE);
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#getJawrWorkingDirectory()
	 */
	public String getJawrWorkingDirectory() {
		return getStringValue(JAWR_WORKING_DIRECTORY);
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#getUseBundleMapping()
	 */
	public String getUseBundleMapping() {
		return getStringValue(USE_BUNDLE_MAPPING);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#setCharsetName(java.lang.String)
	 */
	public void setCharsetName(final String charsetName) {
	
		setStringValue(CHARSET_NAME, charsetName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#setDebugModeOn(boolean)
	 */
	public void setDebugModeOn(final String debugMode) {
	
		setBooleanValue(DEBUG_MODE_ON, debugMode);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#setDebugOverrideKey(java.lang.String)
	 */
	public void setDebugOverrideKey(String debugOverrideKey) {
	
		setStringValue(DEBUG_OVERRIDE_KEY, debugOverrideKey);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#setGzipResourcesForIESixOn(boolean)
	 */
	public void setGzipResourcesForIESixOn(String gzipResourcesForIESixOn) {
	
		setBooleanValue(GZIP_RESOURCES_FOR_IE_SIX_ON, gzipResourcesForIESixOn);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#setGzipResourcesModeOn(boolean)
	 */
	public void setGzipResourcesModeOn(String gzipResourcesModeOn) {
	
		setBooleanValue(GZIP_RESOURCES_MODE_ON, gzipResourcesModeOn);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#setContextPathOverride(java.lang.String)
	 */
	public void setContextPathOverride(String contextPathOverride) {

		setStringValue(CONTEXT_PATH_OVERRIDE, contextPathOverride);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#setContextPathSslOverride(java.lang.String)
	 */
	public void setContextPathSslOverride(String contextPathSslOverride) {

		setStringValue(CONTEXT_PATH_SSL_OVERRIDE, contextPathSslOverride);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#setUseContextPathOverrideInDebugMode(java.lang.String)
	 */
	public void setUseContextPathOverrideInDebugMode(
			String useContextPathOverrideInDebugMode) {
		
		setBooleanValue(USE_CONTEXT_PATH_OVERRIDE_IN_DEBUG_MODE, useContextPathOverrideInDebugMode);
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#setJawrWorkingDirectory(java.lang.String)
	 */
	public void setJawrWorkingDirectory(String jawrWorkingDirectory) {
		
		setStringValue(JAWR_WORKING_DIRECTORY, jawrWorkingDirectory);
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#setUseBundleMapping(java.lang.String)
	 */
	public void setUseBundleMapping(String useBundleMapping) {
		
		setBooleanValue(USE_BUNDLE_MAPPING, useBundleMapping);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#refreshConfig()
	 */
	public void refreshConfig() {

		if (jsMBean != null) {
			jsMBean.refreshConfig();
		}
		if (cssMBean != null) {
			cssMBean.refreshConfig();
		}
		if (binaryMBean != null) {
			binaryMBean.refreshConfig();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#addDebugSessionId(java.lang.String)
	 */
	public void addDebugSessionId(String sessionId) {
		debugSessionIdSet.add(sessionId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#removeDebugSessionId(java.lang.String)
	 */
	public void removeDebugSessionId(String sessionId) {
		debugSessionIdSet.remove(sessionId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#removeAllDebugSessionId()
	 */
	public void removeAllDebugSessionId() {
		debugSessionIdSet.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#isDebugSessionId(java.lang.String)
	 */
	public boolean isDebugSessionId(String sessionId) {
		return debugSessionIdSet.contains(sessionId);
	}

	/**
	 * Returns the config manager MBean from the resource type
	 * @param resourceType the resource type
	 * @return the config manager MBean from the resource type
	 */
	public JawrConfigManagerMBean getConfigMgr(String resourceType){
	
		JawrConfigManagerMBean configMgr = null;
		if(resourceType.equals(JawrConstant.JS_TYPE)){
			configMgr = jsMBean;
		}else if(resourceType.equals(JawrConstant.CSS_TYPE)){
			configMgr = cssMBean;
		}else if(resourceType.equals(JawrConstant.IMG_TYPE) || resourceType.equals(JawrConstant.BINARY_TYPE)){
			configMgr = binaryMBean;
		}
		
		return configMgr;
	}
	
	/**
	 * Returns the string value of the configuration managers
	 * 
	 * @param property the property to retrieve
	 * @return the string value of the configuration managers
	 */
	public String getStringValue(String property) {

		final List<JawrConfigManagerMBean> mBeans = getInitializedConfigurationManagers();
		try {

			if (mBeans.size() == 3) {

				if (areEquals(PropertyUtils.getProperty(jsMBean, property),
						PropertyUtils.getProperty(cssMBean, property), PropertyUtils.getProperty(binaryMBean, property))) {

					return PropertyUtils.getProperty(jsMBean, property);
				} else {
					return NOT_IDENTICAL_VALUES;
				}
			}

			if (mBeans.size() == 2) {
				JawrConfigManagerMBean mBean1 = (JawrConfigManagerMBean) mBeans
						.get(0);
				JawrConfigManagerMBean mBean2 = (JawrConfigManagerMBean) mBeans
						.get(1);

				if (areEquals(PropertyUtils.getProperty(mBean1, property),
						PropertyUtils.getProperty(mBean2, property))) {
					return PropertyUtils.getProperty(mBean1, property);
				} else {
					return NOT_IDENTICAL_VALUES;
				}
			}

			JawrConfigManagerMBean mBean1 = (JawrConfigManagerMBean) mBeans
					.get(0);

			return PropertyUtils.getProperty(mBean1, property);

		} catch (Exception e) {
			return ERROR_VALUE;
		}

	}

	/**
	 * Update the property with the string value in each config manager.
	 * 
	 * @param property the property to update
	 * @param value the value to set
	 */
	public void setStringValue(String property, String value) {
		try {
			if (jsMBean != null) {
				PropertyUtils.setProperty(jsMBean, property, value);
			}
			if (cssMBean != null) {
				PropertyUtils.setProperty(cssMBean, property, value);
			}
			if (binaryMBean != null) {
				PropertyUtils.setProperty(binaryMBean, property, value);
			}
		} catch (IllegalAccessException e) {
			throw new JmxConfigException("Exception while setting the string value", e);
		} catch (InvocationTargetException e) {
			throw new JmxConfigException("Exception while setting the string value", e);
		} catch (NoSuchMethodException e) {
			throw new JmxConfigException("Exception while setting the string value", e);
		}
	}

	/**
	 * Update the property with the string value in each config manager.
	 * 
	 * @param property the property to update
	 * @param value the value to set
	 */
	public void setBooleanValue(String property, String value) {
		try {
			if (jsMBean != null) {
				PropertyUtils.setProperty(jsMBean, property, Boolean.valueOf(value));
			}
			if (cssMBean != null) {
				PropertyUtils.setProperty(cssMBean, property, Boolean.valueOf(value));
			}
			if (binaryMBean != null) {
				PropertyUtils.setProperty(binaryMBean, property, Boolean.valueOf(value));
			}
		} catch (IllegalAccessException e) {
			throw new JmxConfigException("Exception while setting the boolean value", e);
		} catch (InvocationTargetException e) {
			throw new JmxConfigException("Exception while setting the boolean value", e);
		} catch (NoSuchMethodException e) {
			throw new JmxConfigException("Exception while setting the boolean value", e);
		}
	}
	
	/**
	 * Returns true if the 2 string are equals.
	 * 
	 * @param str1 the first string
	 * @param str2 the 2nd string
	 * @return true if the 2 string are equals.
	 */
	public boolean areEquals(String str1, String str2) {

		return (str1 == null && str2 == null || str1 != null && str2 != null
				&& str1.equals(str2));
	}

	/**
	 * Returns true if the 3 string are equals.
	 * 
	 * @param str1 the first string
	 * @param str2 the 2nd string
	 * @param str3 the 3rd string
	 * @return true if the 3 string are equals.
	 */
	public boolean areEquals(String str1, String str2, String str3) {

		return (str1 == null && str2 == null && str3 == null || str1 != null
				&& str2 != null && str3 != null && str1.equals(str2)
				&& str2.equals(str3));
	}

	
}
