/**
 * Copyright 2008-2014  Jordi Hernández Sellés, Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.factory.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import net.jawr.web.config.jmx.JmxUtils;
import net.jawr.web.context.ThreadLocalJawrContext;
import net.jawr.web.exception.BundlingProcessException;
import net.jawr.web.exception.ResourceNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities to access resources from the classpath
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 */
public class ClassLoaderResourceUtils {

	/** The logger */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(ClassLoaderResourceUtils.class);

	/**
	 * Attempots to load a resource from the classpath, either usinf the
	 * caller's class loader or the current thread's context classloader.
	 * 
	 * @param resourcePath
	 * @param source
	 * @return
	 * @throws FileNotFoundException
	 */
	public static InputStream getResourceAsStream(String resourcePath,
			Object source) throws FileNotFoundException {

		// Try the current classloader
		InputStream is = source.getClass().getResourceAsStream(resourcePath);

		// Weblogic 10 likes this one better..
		if (null == is) {
			ClassLoader cl = source.getClass().getClassLoader();
			if (null != cl)
				is = cl.getResourceAsStream(resourcePath);
		}

		// If current classloader failed, try with the Threads context
		// classloader. If that fails ott, the resource is either not on the
		// classpath or inaccessible from the current context.

		if (null == is) {
			is = Thread.currentThread().getContextClassLoader()
					.getResourceAsStream(resourcePath);
		}

		if (null == is) {

			// Try to use the classloader of the current Jawr Config Manager
			// MBean
			// This will be used when a refresh is done in the configuration
			// using the JMX MBean
			MBeanServer mbs = JmxUtils.getMBeanServer();
			if (mbs != null) {

				ObjectName name = ThreadLocalJawrContext
						.getJawrConfigMgrObjectName();
				if (name != null) {
					try {

						ClassLoader cl = mbs.getClassLoaderFor(name);
						is = cl.getResourceAsStream(resourcePath);
					} catch (Exception e) {
						LOGGER.error("Unable to instanciate the Jawr MBean '"
								+ name.getCanonicalName() + "'", e);
					}
				}
			}

		}

		// Try to retrieve by URL
		if (null == is) {

			try {
				URL url = getResourceURL(resourcePath, source);
				is = new FileInputStream(new File(url.getFile()));
			} catch (ResourceNotFoundException e) {
				throw new FileNotFoundException(resourcePath
						+ " could not be found. ");
			} catch (IOException e) {
				throw new FileNotFoundException(resourcePath
						+ " could not be found. ");
			}
		}

		return is;
	}

	/**
	 * Attempts to find the URL of a resource from the classpath, either usinf
	 * the caller's class loader or the current thread's context classloader.
	 * 
	 * @param resourcePath
	 *            the resource path
	 * @param source
	 *            the object
	 * @return the URL.
	 * @throws ResourceNotFoundException
	 *             if the resource is not found
	 */
	public static URL getResourceURL(String resourcePath, Object source)
			throws ResourceNotFoundException {

		// Try the current classloader
		URL url = source.getClass().getResource(resourcePath);

		// Weblogic 10 likes this one better..
		if (null == url) {
			ClassLoader cl = source.getClass().getClassLoader();
			if (null != cl)
				url = cl.getResource(resourcePath);
		}

		// If current classloader failed, try with the Threads context
		// classloader. If that fails ott, the resource is either not on the
		// classpath or inaccessible from the current context.
		if (null == url) {
			url = Thread.currentThread().getContextClassLoader()
					.getResource(resourcePath);

			// Last chance, hack in the classloader
			if (null == url) {
				ClassLoader threadClassLoader = Thread.currentThread()
						.getContextClassLoader();
				try {
					Thread.currentThread().setContextClassLoader(
							source.getClass().getClassLoader());
					if (Thread.currentThread().getContextClassLoader() != null) {
						url = Thread.currentThread().getContextClassLoader()
								.getResource(resourcePath);
					}
				} finally {
					Thread.currentThread().setContextClassLoader(
							threadClassLoader);
				}

			}
			if (null == url) {
				throw new ResourceNotFoundException(resourcePath
						+ " could not be found. ");
			}
		}

		return url;
	}

	/**
	 * Attempts to find the URLs of a resource from the classpath, either using
	 * the caller's class loader or the current thread's context classloader.
	 * 
	 * @param resourcePath
	 *            the resource path
	 * @param source
	 *            the object
	 * @return the URL or null if not found
	 */
	public static Enumeration<URL> getResources(String resourcePath,
			Object source) {

		// Try the current classloader
		Enumeration<URL> urls = null;

		ClassLoader cl = source.getClass().getClassLoader();
		try {
			if (null != cl) {
				urls = cl.getResources(resourcePath);
			}

			// If current classloader failed, try with the Threads context
			// classloader. If that fails ott, the resource is either not on the
			// classpath or inaccessible from the current context.

			if (null == urls) {
				urls = Thread.currentThread().getContextClassLoader()
						.getResources(resourcePath);
			}

			// Last chance, hack in the classloader
			if (null == urls) {
				ClassLoader threadClassLoader = Thread.currentThread()
						.getContextClassLoader();
				try {
					Thread.currentThread().setContextClassLoader(
							source.getClass().getClassLoader());
					if (Thread.currentThread().getContextClassLoader() != null) {

						urls = Thread.currentThread().getContextClassLoader()
								.getResources(resourcePath);
					}
				} finally {
					Thread.currentThread().setContextClassLoader(
							threadClassLoader);
				}
			}
		} catch (IOException e) {
			LOGGER.warn("Unable to load " + resourcePath, e);
		}

		return urls;
	}

	/**
	 * Builds a class instance using reflection, by using its classname. The
	 * class must have a zero-arg constructor.
	 * 
	 * @param classname
	 *            the class to build an instance of.
	 * @return
	 */
	public static Object buildObjectInstance(String classname) {

		Class<?> clazz = getClass(classname);
		return buildObjectInstance(clazz);
	}

	/**
	 * Builds a class instance using reflection, by using its class. The class
	 * must have a zero-arg constructor.
	 * 
	 * @param clazz
	 *            the class to build an instance of.
	 * @return
	 */
	public static Object buildObjectInstance(Class<?> clazz) {
		Object rets = null;
		try {
			rets = clazz.newInstance();
		} catch (Exception e) {
			throw new BundlingProcessException(
					e.getMessage()
							+ " [The custom class "
							+ clazz.getName()
							+ " could not be instantiated, check wether it is available on the classpath and"
							+ " verify that it has a zero-arg constructor].\n"
							+ " The specific error message is: "
							+ e.getClass().getName() + ":" + e.getMessage(), e);
		}
		return rets;
	}

	/**
	 * Returns the class associated to the class name given in parameter
	 * 
	 * @param classname
	 *            the class name
	 * @return the class
	 */
	public static Class<?> getClass(String classname) {
		Class<?> clazz = null;
		try {
			clazz = Class.forName(classname);

		} catch (Exception e) {
			// Try the second approach
		}
		if (null == clazz) {
			Exception classNotFoundEx = null;
			try {
				clazz = Class.forName(classname, true,
						new ClassLoaderResourceUtils().getClass()
								.getClassLoader());
			} catch (Exception e) {
				// Try the third approach
				classNotFoundEx = e;
			}
			if (null == clazz) {
				ClassLoader threadClassLoader = Thread.currentThread()
						.getContextClassLoader();
				if (null != threadClassLoader) {
					try {
						clazz = Class.forName(classname, true,
								threadClassLoader);
					} catch (Exception e) {
						throw new BundlingProcessException(
								e.getMessage()
										+ " [The custom class "
										+ classname
										+ " could not be instantiated, check wether it is available on the classpath and"
										+ " verify that it has a zero-arg constructor].\n"
										+ " The specific error message is: "
										+ e.getClass().getName() + ":"
										+ e.getMessage(), e);
					}
				} else {
					throw new BundlingProcessException(
							classNotFoundEx.getMessage()
									+ " [The custom class "
									+ classname
									+ " could not be instantiated, check wether it is available on the classpath and"
									+ " verify that it has a zero-arg constructor].\n"
									+ " The specific error message is: "
									+ classNotFoundEx.getClass().getName()
									+ ":" + classNotFoundEx.getMessage(),
							classNotFoundEx);
				}

			}
		}
		return clazz;
	}

	/**
	 * Builds a class instance using reflection, by using its classname. The
	 * class must have a zero-arg constructor.
	 * 
	 * @param classname
	 *            the class to build an instance of.
	 * @return
	 */
	public static Object buildObjectInstance(String classname, Object[] params) {
		Object rets = null;

		Class<?>[] paramTypes = new Class[params.length];
		for (int x = 0; x < params.length; x++) {
			paramTypes[x] = params[x].getClass();
		}

		try {
			Class<?> clazz = getClass(classname);
			rets = clazz.getConstructor(paramTypes).newInstance(params);

		} catch (Exception e) {
			throw new BundlingProcessException(
					e.getMessage()
							+ " [The custom class "
							+ classname
							+ " could not be instantiated, check wether it is available on the classpath and"
							+ " verify that it has a zero-arg constructor].\n"
							+ " The specific error message is: "
							+ e.getClass().getName() + ":" + e.getMessage(), e);
		}
		return rets;
	}
}
