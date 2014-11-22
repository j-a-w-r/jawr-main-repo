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

import static net.jawr.web.util.ServletContextUtils.getContextPath;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.servlet.ServletContext;

import net.jawr.web.exception.JmxConfigException;
import net.jawr.web.util.ServletContextUtils;
import net.jawr.web.util.StringUtils;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;

/**
 * Utility class for JMX.
 * 
 * @author Ibrahim Chaehoi
 */
public final class JmxUtils {

	/** The logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(JmxUtils.class);

	/** The default prefix value */
	private static final String DEFAULT_PREFIX = "default";
	
	/** The Jawr App Config Manager type */
	private static final String JAWR_APP_CONFIG_MANAGER_TYPE = "JawrAppConfigManager";

	/** The Jawr Config Manager type */
	private static final String JAWR_CONFIG_MANAGER_TYPE = "JawrConfigManager";

	
	/**
	 * Constructor 
	 */
	private JmxUtils() {
		
	}
	
	/**
	 * Initialize the JMX Bean
	 *  
	 * @param appConfigMgr The application config manager
	 * @param servletContext the servlet context
	 * @param resourceType the resource type
	 * @param mBeanPrefix 
	 */
	public static void initJMXBean(JawrApplicationConfigManager appConfigMgr, ServletContext servletContext, String resourceType, String mBeanPrefix) {
		
		try {

			MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
			
			if(mbs != null){
				
				ObjectName jawrConfigMgrObjName = JmxUtils.getMBeanObjectName(servletContext, resourceType, mBeanPrefix);
				
				// register the jawrApplicationConfigManager if it's not already done
				ObjectName appJawrMgrObjectName = JmxUtils.getAppJawrConfigMBeanObjectName(servletContext, mBeanPrefix);
				if(!mbs.isRegistered(appJawrMgrObjectName)){
					mbs.registerMBean(appConfigMgr, appJawrMgrObjectName);
				}
				
				if(mbs.isRegistered(jawrConfigMgrObjName)){
					LOGGER.warn("The MBean '"+jawrConfigMgrObjName.getCanonicalName()+"' already exists. It will be unregisterd and registered with the new JawrConfigManagerMBean.");
					mbs.unregisterMBean(jawrConfigMgrObjName);
				}
				JawrConfigManagerMBean configMgr = appConfigMgr.getConfigMgr(resourceType);
				mbs.registerMBean(configMgr, jawrConfigMgrObjName);
			}
			
		} catch (Exception e) {
			LOGGER.error("Unable to instanciate the Jawr MBean for resource type '"+resourceType+"'", e);
		}

	}
	
	/**
	 * Returns the current MBean server or create a new one if not exist.
	 * 
	 * @return the current MBean server or create a new one if not exist.
	 */
	public static MBeanServer getMBeanServer() {

		return ManagementFactory.getPlatformMBeanServer();
	}
	
	/**
	 * Returns the object name for the Jawr configuration Manager MBean
	 * @param servletContext the servelt context
	 * @param resourceType the resource type
	 * @param mBeanPrefix 
	 * @return the object name for the Jawr configuration Manager MBean
	 * @throws Exception if an exception occurs
	 */
	public static ObjectName getMBeanObjectName(final ServletContext servletContext, final String resourceType, final String mBeanPrefix) {
		
		String contextPath = getContextPath(servletContext);
		return getJawrConfigMBeanObjectName(contextPath, resourceType, mBeanPrefix);
	}

	/**
	 * Returns the object name for the Jawr configuration Manager MBean
	 * @param contextPath the context path
	 * @param resourceType  the resource type
	 * @param mBeanPrefix the MBean prefix
	 * 
	 * @return the object name for the Jawr configuration Manager MBean
	 * @throws Exception if an exception occurs
	 */
	public static ObjectName getJawrConfigMBeanObjectName(final String contextPath, final String resourceType, final String mBeanPrefix) {
		
		return getMBeanObjectName(contextPath, JAWR_CONFIG_MANAGER_TYPE, mBeanPrefix, resourceType);
	}
	
	/**
	 * Returns the object name for the Jawr Application configuration Manager MBean
	 * @param servletContext the servelt context
	 * @param mBeanPrefix the MBean prefix
	 * @return the object name for the Jawr configuration Manager MBean
	 * @throws Exception if an exception occurs
	 */
	public static ObjectName getAppJawrConfigMBeanObjectName(ServletContext servletContext, String mBeanPrefix) {
		
		return getMBeanObjectName(getContextPath(servletContext), JAWR_APP_CONFIG_MANAGER_TYPE, mBeanPrefix, null);
	}
	
	/**
	 * Returns the object name for the Jawr MBean
	 * @param contextPath the context path
	 * @param objectType the type of the MBean object
	 * @param mBeanPrefix the MBean prefix
	 * @param resourceType  the resource type
	 * @return the object name for the Jawr MBean
	 * @throws Exception if an exception occurs
	 */
	private static ObjectName getMBeanObjectName(final String contextPath, final String objectType, final String mBeanPrefix, final String resourceType) {
		
		String curCtxPath = contextPath;
		if(StringUtils.isEmpty(curCtxPath)){
			
			curCtxPath = ServletContextUtils.getContextPath(null);
		}
		
		if(curCtxPath.charAt(0) == ('/')){
			curCtxPath = curCtxPath.substring(1);
		}
		String prefix = mBeanPrefix;
		if(prefix == null){
			prefix = DEFAULT_PREFIX;
		}
		StringBuilder objectNameStr = new StringBuilder("net.jawr.web.jmx:type="+objectType+",prefix="+prefix+",webappContext="+curCtxPath);
		if(resourceType != null){
			objectNameStr.append(",name="+resourceType+"MBean");
		}
		
		return getObjectName(objectNameStr.toString());
	}
	
	/**
     * Create an object name from the name passed in parameter
	 * @param name the name
	 * @return the objectName
	 */
	private static ObjectName getObjectName(String name){
		
		ObjectName mBeanName = null;
		try {
			mBeanName = new ObjectName(name);
		} catch (MalformedObjectNameException e) {
			throw new JmxConfigException(e);
		} catch (NullPointerException e) {
			throw new JmxConfigException(e);
		}
		
		return mBeanName;
	}
	
}
