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

/**
 * This interface defines the MBean which manage the Jawr configuration for a we
 * application, so it will affect all JawrConfigManagerMBean associated to Jawr
 * Servlets.
 * 
 * @author Ibrahim Chaehoi
 */
public interface JawrApplicationConfigManagerMBean {

	/**
	 * @return the debugOverrideKey that is used to override production mode per
	 *         request
	 * @see net.jawr.web.config.JawrConfig#getDebugOverrideKey()
	 */
	public String getDebugOverrideKey();

	/**
	 * Set the debugOverrideKey
	 * 
	 * @param debugOverrideKey
	 *            the String to set as the key
	 * @see net.jawr.web.config.JawrConfig#setDebugOverrideKey(java.lang.String)
	 */
	public void setDebugOverrideKey(String debugOverrideKey);

	/**
	 * @return the debug mode flag.
	 * @see net.jawr.web.config.JawrConfig#isDebugModeOn()
	 */
	public String getDebugModeOn();

	/**
	 * Set debug mode.
	 * 
	 * @param debugMode
	 *            the flag to set
	 * @see net.jawr.web.config.JawrConfig#setDebugModeOn(boolean)
	 */
	public void setDebugModeOn(String debugMode);

	/**
	 * @return the flag indicating if the resource must be gzipped for IE6 or
	 *         less
	 * @see net.jawr.web.config.JawrConfig#isGzipResourcesForIESixOn()
	 */
	public String getGzipResourcesForIESixOn();

	/**
	 * Sets the flag indicating if the resource must be gzipped for IE6 or less
	 * 
	 * @param gzipResourcesForIESixOn
	 *            the flag to set.
	 * @see net.jawr.web.config.JawrConfig#setGzipResourcesForIESixOn(boolean)
	 */
	public void setGzipResourcesForIESixOn(String gzipResourcesForIESixOn);

	/**
	 * @return the flag indicating if the resource must be gzipped or not
	 * @see net.jawr.web.config.JawrConfig#isGzipResourcesModeOn()
	 */
	public String getGzipResourcesModeOn();

	/**
	 * Sets the flag indicating if the resource must be gzipped or not
	 * 
	 * @param gzipResourcesModeOn
	 *            the flag to set
	 * @see net.jawr.web.config.JawrConfig#setGzipResourcesModeOn(boolean)
	 */
	public void setGzipResourcesModeOn(String gzipResourcesModeOn);

	/**
	 * @return the charset name
	 */
	public String getCharsetName();

	/**
	 * Set the charsetname to be used to interpret and generate resource.
	 * 
	 * @param charsetName
	 *            the charset name to set
	 * @see net.jawr.web.config.JawrConfig#setCharsetName(java.lang.String)
	 */
	public void setCharsetName(String charsetName);

	/**
	 * @return The string to use instead of the regular context path.
	 * @see net.jawr.web.config.JawrConfig#getContextPathOverride()
	 */
	public String getContextPathOverride();

	/**
	 * Set the string to use instead of the regular context path. If it is an
	 * empty string, urls will be relative to the path (i.e, not start with a
	 * slash).
	 * 
	 * @param contextPathOverride
	 *            The string to use instead of the regular context path.
	 * @see net.jawr.web.config.JawrConfig#setContextPathOverride(java.lang.String)
	 */
	public void setContextPathOverride(String contextPathOverride);

	/**
	 * @return the contextPathSslOverride
	 * @see net.jawr.web.config.JawrConfig#getContextPathSslOverride()
	 */
	public String getContextPathSslOverride();

	/**
	 * @param contextPathSslOverride
	 *            the contextPathSslOverride to set
	 * @see net.jawr.web.config.JawrConfig#setContextPathSslOverride(java.lang.String)
	 */
	public void setContextPathSslOverride(String contextPathOverride);

	/**
	 * @param useContextPathOverrideInDebugMode
	 *            the useContextPathOverrideInDebugMode to set
	 * @see net.jawr.web.config.JawrConfig#setUseContextPathOverrideInDebugMode(boolean)
	 */
	public void setUseContextPathOverrideInDebugMode(String useContextPathOverrideInDebugMode);

	/**
	 * @return the flag indicating if the overriden context path should be used
	 *         in debug mode
	 * @see net.jawr.web.config.JawrConfig#getUseContextPathOverrideInDebugMode()
	 */
	public String getUseContextPathOverrideInDebugMode();

	/**
	 * Returns the flag which defines if we should process the bundle at server
	 * startup. defaults to false.
	 * 
	 * @return the flag which defines if we should process the bundle at server
	 *         startup.
	 */
	public String getUseBundleMapping();

	/**
	 * Sets the flag which defines if we should process the bundle at server
	 * startup.
	 * 
	 * @param usBundleMapping
	 *            the flag to set
	 */
	public void setUseBundleMapping(String usBundleMapping);

	/**
	 * Returns the jawr working directory path
	 * 
	 * @return the jawr working directory path
	 */
	public String getJawrWorkingDirectory();

	/**
	 * Sets the jawr working directory path
	 * 
	 * @param jawrWorkingDirectory
	 *            the path to set
	 */
	public void setJawrWorkingDirectory(String jawrWorkingDirectory);

	/**
	 * Refresh the configuration.
	 */
	public void refreshConfig();

	/**
	 * Rebuild dirty bundles.
	 */
	public void rebuildDirtyBundles();

	/**
	 * Add a session ID, to the set of debug session ID. All request make by
	 * sessions where their IDs is contained in the debug session Set, will be
	 * threated as in debug mode.
	 * 
	 * @param sessionId
	 *            the session ID to add
	 */
	public void addDebugSessionId(String sessionId);

	/**
	 * Add a session ID, to the set of debug session ID. All request make by
	 * sessions where their IDs is contained in the debug session Set, will be
	 * threated as in debug mode.
	 * 
	 * @param sessionId
	 *            the session ID to add
	 */
	public void removeDebugSessionId(String sessionId);

	/**
	 * Remove All debug session ID.
	 */
	public void removeAllDebugSessionId();

	/**
	 * Returns true if the session ID passed in parameter is a debuggable
	 * session ID
	 * 
	 * @param sessionId
	 *            the session ID
	 * @return true if the session ID passed in parameter is a debuggable
	 *         session ID
	 */
	public boolean isDebugSessionId(String sessionId);
}
