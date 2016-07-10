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

import static java.lang.Boolean.valueOf;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static net.jawr.web.JawrConstant.BINARY_TYPE;
import static net.jawr.web.JawrConstant.CSS_TYPE;
import static net.jawr.web.JawrConstant.JS_TYPE;
import net.jawr.web.exception.JmxConfigException;
import static net.jawr.web.util.PropertyUtils.getProperty;
import static net.jawr.web.util.PropertyUtils.setProperty;

/**
 * This interface defines the MBean which manage the Jawr configuration for a we
 * application, so it will affect all JawrConfigManagerMBean associated to Jawr
 * Servlets.
 * 
 * @author Ibrahim Chaehoi
 */
public class JawrApplicationConfigManager implements JawrApplicationConfigManagerMBean {

	/** The charset name property */
	private static final String CHARSET_NAME = "charsetName";

	/** The debug mode on property */
	private static final String DEBUG_MODE_ON = "debugModeOn";

	/** The debug override key property */
	private static final String DEBUG_OVERRIDE_KEY = "debugOverrideKey";

	/** The gzip flag for IE6 property */
	private static final String GZIP_RESOURCES_FOR_IE_SIX_ON = "gzipResourcesForIESixOn";

	/** The gzip resource mode property */
	private static final String GZIP_RESOURCES_MODE_ON = "gzipResourcesModeOn";

	/** The overriden context path property */
	private static final String CONTEXT_PATH_OVERRIDE = "contextPathOverride";

	/** The overriden SSL context path property */
	private static final String CONTEXT_PATH_SSL_OVERRIDE = "contextPathSslOverride";

	/**
	 * The property indicating if the context path should be override in debug
	 * mode
	 */
	private static final String USE_CONTEXT_PATH_OVERRIDE_IN_DEBUG_MODE = "useContextPathOverrideInDebugMode";

	/** The Jawr working directory property */
	private static final String JAWR_WORKING_DIRECTORY = "jawrWorkingDirectory";

	/** The use bundle mapping property */
	private static final String USE_BUNDLE_MAPPING = "useBundleMapping";

	/**
	 * The message of the property, when the values are not equals for the
	 * different configuration manager
	 */
	private static final String NOT_IDENTICAL_VALUES = "Value for this property are not identical";

	/**
	 * The message when an error occured during the retrieve of the property
	 * value
	 */
	private static final String ERROR_VALUE = "An error occured while retrieving the value for this property";

	/** The configuration manager for the Javascript handler */
	private JawrConfigManagerMBean jsMBean;

	/** The configuration manager for the CSS handler */
	private JawrConfigManagerMBean cssMBean;

	/**
	 * The configuration manager for the binary resource handler (images, fonts,
	 * ...)
	 */
	private JawrConfigManagerMBean binaryMBean;

	/**
	 * The set of session ID for which all requests will be executed in debug
	 * mode
	 */
	private final Set<String> debugSessionIdSet = new HashSet<>();

	/**
	 * Constructor
	 */
	public JawrApplicationConfigManager() {

	}

	/**
	 * Sets the configuration manager for the Javascript handler
	 * 
	 * @param jsMBean
	 *            the configuration manager to set
	 */
	public void setJsMBean(final JawrConfigManagerMBean jsMBean) {
		this.jsMBean = jsMBean;
	}

	/**
	 * Sets the configuration manager for the CSS handler
	 * 
	 * @param cssMBean
	 *            the configuration manager to set
	 */
	public void setCssMBean(final JawrConfigManagerMBean cssMBean) {
		this.cssMBean = cssMBean;
	}

	/**
	 * Sets the configuration manager for the binary handler
	 * 
	 * @param binaryMBean
	 *            the configuration manager to set
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

		final List<JawrConfigManagerMBean> mBeans = new ArrayList<>();
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
	 * @see
	 * net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#getCharsetName(
	 * )
	 */
	@Override
	public String getCharsetName() {

		return getStringValue(CHARSET_NAME);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#
	 * getDebugOverrideKey()
	 */
	@Override
	public String getDebugOverrideKey() {

		return getStringValue(DEBUG_OVERRIDE_KEY);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#isDebugModeOn()
	 */
	@Override
	public String getDebugModeOn() {

		return getStringValue(DEBUG_MODE_ON);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#
	 * isGzipResourcesForIESixOn()
	 */
	@Override
	public String getGzipResourcesForIESixOn() {

		return getStringValue(GZIP_RESOURCES_FOR_IE_SIX_ON);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#
	 * isGzipResourcesModeOn()
	 */
	@Override
	public String getGzipResourcesModeOn() {

		return getStringValue(GZIP_RESOURCES_MODE_ON);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#
	 * getContextPathOverride()
	 */
	@Override
	public String getContextPathOverride() {
		return getStringValue(CONTEXT_PATH_OVERRIDE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#
	 * getContextPathSslOverride()
	 */
	@Override
	public String getContextPathSslOverride() {

		return getStringValue(CONTEXT_PATH_SSL_OVERRIDE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#
	 * getUseContextPathOverrideInDebugMode()
	 */
	@Override
	public String getUseContextPathOverrideInDebugMode() {

		return getStringValue(USE_CONTEXT_PATH_OVERRIDE_IN_DEBUG_MODE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#
	 * getJawrWorkingDirectory()
	 */
	@Override
	public String getJawrWorkingDirectory() {
		return getStringValue(JAWR_WORKING_DIRECTORY);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#
	 * getUseBundleMapping()
	 */
	@Override
	public String getUseBundleMapping() {
		return getStringValue(USE_BUNDLE_MAPPING);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#setCharsetName(
	 * java.lang.String)
	 */
	@Override
	public void setCharsetName(final String charsetName) {

		setStringValue(CHARSET_NAME, charsetName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#setDebugModeOn(
	 * boolean)
	 */
	@Override
	public void setDebugModeOn(final String debugMode) {

		setBooleanValue(DEBUG_MODE_ON, debugMode);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#
	 * setDebugOverrideKey(java.lang.String)
	 */
	@Override
	public void setDebugOverrideKey(String debugOverrideKey) {

		setStringValue(DEBUG_OVERRIDE_KEY, debugOverrideKey);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#
	 * setGzipResourcesForIESixOn(boolean)
	 */
	@Override
	public void setGzipResourcesForIESixOn(String gzipResourcesForIESixOn) {

		setBooleanValue(GZIP_RESOURCES_FOR_IE_SIX_ON, gzipResourcesForIESixOn);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#
	 * setGzipResourcesModeOn(boolean)
	 */
	@Override
	public void setGzipResourcesModeOn(String gzipResourcesModeOn) {

		setBooleanValue(GZIP_RESOURCES_MODE_ON, gzipResourcesModeOn);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#
	 * setContextPathOverride(java.lang.String)
	 */
	@Override
	public void setContextPathOverride(String contextPathOverride) {

		setStringValue(CONTEXT_PATH_OVERRIDE, contextPathOverride);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#
	 * setContextPathSslOverride(java.lang.String)
	 */
	@Override
	public void setContextPathSslOverride(String contextPathSslOverride) {

		setStringValue(CONTEXT_PATH_SSL_OVERRIDE, contextPathSslOverride);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#
	 * setUseContextPathOverrideInDebugMode(java.lang.String)
	 */
	@Override
	public void setUseContextPathOverrideInDebugMode(String useContextPathOverrideInDebugMode) {

		setBooleanValue(USE_CONTEXT_PATH_OVERRIDE_IN_DEBUG_MODE, useContextPathOverrideInDebugMode);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#
	 * setJawrWorkingDirectory(java.lang.String)
	 */
	@Override
	public void setJawrWorkingDirectory(String jawrWorkingDirectory) {

		setStringValue(JAWR_WORKING_DIRECTORY, jawrWorkingDirectory);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#
	 * setUseBundleMapping(java.lang.String)
	 */
	@Override
	public void setUseBundleMapping(String useBundleMapping) {

		setBooleanValue(USE_BUNDLE_MAPPING, useBundleMapping);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#refreshConfig()
	 */
	@Override
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
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#
	 * rebuildDirtyBundles()
	 */
	@Override
	public void rebuildDirtyBundles() {
		if (jsMBean != null) {
			jsMBean.rebuildDirtyBundles();
		}
		if (cssMBean != null) {
			cssMBean.rebuildDirtyBundles();
		}
		if (binaryMBean != null) {
			binaryMBean.rebuildDirtyBundles();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#
	 * addDebugSessionId(java.lang.String)
	 */
	@Override
	public void addDebugSessionId(String sessionId) {
		debugSessionIdSet.add(sessionId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#
	 * removeDebugSessionId(java.lang.String)
	 */
	@Override
	public void removeDebugSessionId(String sessionId) {
		debugSessionIdSet.remove(sessionId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#
	 * removeAllDebugSessionId()
	 */
	@Override
	public void removeAllDebugSessionId() {
		debugSessionIdSet.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#
	 * isDebugSessionId(java.lang.String)
	 */
	@Override
	public boolean isDebugSessionId(String sessionId) {
		return debugSessionIdSet.contains(sessionId);
	}

	/**
	 * Returns the config manager MBean from the resource type
	 * 
	 * @param resourceType
	 *            the resource type
	 * @return the config manager MBean from the resource type
	 */
	public JawrConfigManagerMBean getConfigMgr(String resourceType) {

		JawrConfigManagerMBean configMgr = null;
		if (resourceType.equals(JS_TYPE)) {
			configMgr = jsMBean;
		} else if (resourceType.equals(CSS_TYPE)) {
			configMgr = cssMBean;
		} else if (resourceType.equals(BINARY_TYPE)) {
			configMgr = binaryMBean;
		}

		return configMgr;
	}

	/**
	 * Returns the string value of the configuration managers
	 * 
	 * @param property
	 *            the property to retrieve
	 * @return the string value of the configuration managers
	 */
	public String getStringValue(String property) {

		final List<JawrConfigManagerMBean> mBeans = getInitializedConfigurationManagers();
		try {

			if (mBeans.size() == 3) {

				if (areEquals(getProperty(jsMBean, property), getProperty(cssMBean, property),
						getProperty(binaryMBean, property))) {

					return getProperty(jsMBean, property);
				} else {
					return NOT_IDENTICAL_VALUES;
				}
			}

			if (mBeans.size() == 2) {
				JawrConfigManagerMBean mBean1 = mBeans.get(0);
				JawrConfigManagerMBean mBean2 = mBeans.get(1);

				if (areEquals(getProperty(mBean1, property), getProperty(mBean2, property))) {
					return getProperty(mBean1, property);
				} else {
					return NOT_IDENTICAL_VALUES;
				}
			}

			JawrConfigManagerMBean mBean1 = mBeans.get(0);

			return getProperty(mBean1, property);

		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			return ERROR_VALUE;
		}

	}

	/**
	 * Update the property with the string value in each config manager.
	 * 
	 * @param property
	 *            the property to update
	 * @param value
	 *            the value to set
	 */
	public void setStringValue(String property, String value) {
		try {
			if (jsMBean != null) {
				setProperty(jsMBean, property, value);
			}
			if (cssMBean != null) {
				setProperty(cssMBean, property, value);
			}
			if (binaryMBean != null) {
				setProperty(binaryMBean, property, value);
			}
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			throw new JmxConfigException("Exception while setting the string value", e);
		}
	}

	/**
	 * Update the property with the string value in each config manager.
	 * 
	 * @param property
	 *            the property to update
	 * @param value
	 *            the value to set
	 */
	public void setBooleanValue(String property, String value) {
		try {
			if (jsMBean != null) {
				setProperty(jsMBean, property, valueOf(value));
			}
			if (cssMBean != null) {
				setProperty(cssMBean, property, valueOf(value));
			}
			if (binaryMBean != null) {
				setProperty(binaryMBean, property, valueOf(value));
			}
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			throw new JmxConfigException("Exception while setting the boolean value", e);
		}
	}

	/**
	 * Returns true if the 2 string are equals.
	 * 
	 * @param str1
	 *            the first string
	 * @param str2
	 *            the 2nd string
	 * @return true if the 2 string are equals.
	 */
	public boolean areEquals(String str1, String str2) {

		return (str1 == null && str2 == null || str1 != null && str2 != null && str1.equals(str2));
	}

	/**
	 * Returns true if the 3 string are equals.
	 * 
	 * @param str1
	 *            the first string
	 * @param str2
	 *            the 2nd string
	 * @param str3
	 *            the 3rd string
	 * @return true if the 3 string are equals.
	 */
	public boolean areEquals(String str1, String str2, String str3) {

		return (str1 == null && str2 == null && str3 == null
				|| str1 != null && str2 != null && str3 != null && str1.equals(str2) && str2.equals(str3));
	}

}
