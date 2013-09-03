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
package net.jawr.web.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;

/**
 * Utility methods for using Java Reflection APIs to facilitate generic
 * property getter and setter operations on Java objects. 
 * This is a slightly modified version of PropertyUtilsBean.
 * 
 * @author Ibrahim Chaehoi
 *
 */
public class PropertyUtils {

	/** The logger */
	private static final Logger LOGGER = Logger.getLogger(PropertyUtils.class);

	/**
     * Return the value of the specified simple property of the specified
     * bean, with string conversions.
     *
     * @param bean Bean whose property is to be extracted
     * @param name Name of the property to be extracted
     * @return the value of the specified simple property of the specified
     * bean, with string conversions.
     * 
     * @exception IllegalAccessException if the caller does not have
     *  access to the property accessor method
     * @exception IllegalArgumentException if <code>bean</code> or
     *  <code>name</code> is null
     * @exception IllegalArgumentException if the property name
     *  is nested or indexed
     * @exception InvocationTargetException if the property accessor method
     *  throws an exception
     * @exception NoSuchMethodException if an accessor method for this
     *  propety cannot be found
     */
    public static String getProperty(Object bean, String name) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException{
	
		String value = null;
		
		// Retrieve the property getter method for the specified property
		PropertyDescriptor descriptor = getPropertyDescriptor(bean, name);
        
        if (descriptor == null) {
            throw new NoSuchMethodException("Unknown property '" +
                    name + "'");
        }
        
        Method readMethod = descriptor.getReadMethod();
        if (readMethod == null) {
            throw new NoSuchMethodException("Property '" + name +
                    "' has no getter method");
        }

        // Call the property getter and return the value
        Object result = (Object) invokeMethod(readMethod, bean, new Object[0]);
        if(result != null){
        	value = result.toString();
        }
        
		return value;
	}

    /**
     * <p>Retrieve the property descriptor for the specified property of the
     * specified bean, or return <code>null</code> if there is no such
     * descriptor.  This method resolves indexed and nested property
     * references in the same manner as other methods in this class, except
     * that if the last (or only) name element is indexed, the descriptor
     * for the last resolved property itself is returned.</p>
     *
     * @param bean Bean for which a property descriptor is requested
     * @param name Possibly indexed and/or nested name of the property for
     *  which a property descriptor is requested
     *
     * @exception IllegalAccessException if the caller does not have
     *  access to the property accessor method
     * @exception IllegalArgumentException if <code>bean</code> or
     *  <code>name</code> is null
     * @exception IllegalArgumentException if a nested reference to a
     *  property returns null
     * @exception InvocationTargetException if the property accessor method
     *  throws an exception
     * @exception NoSuchMethodException if an accessor method for this
     *  propety cannot be found
     */
    private static PropertyDescriptor getPropertyDescriptor(Object bean,
			String name) {
		PropertyDescriptor descriptor = null;
		PropertyDescriptor descriptors[] = getPropertyDescriptors(bean.getClass());
        if (descriptors != null) {
            
            for (int i = 0; i < descriptors.length; i++) {
                if (name.equals(descriptors[i].getName()))
                	descriptor = descriptors[i];
            }
        }
		return descriptor;
	}
	
	/**
     * <p>Retrieve the property descriptors for the specified class,
     * introspecting and caching them the first time a particular bean class
     * is encountered.</p>
     *
     * @param beanClass Bean class for which property descriptors are requested
     *
     * @exception IllegalArgumentException if <code>beanClass</code> is null
     */
    public static PropertyDescriptor[]
            getPropertyDescriptors(Class<?> beanClass) {

        if (beanClass == null) {
            throw new IllegalArgumentException("No bean class specified");
        }

        // Look up any cached descriptors for this bean class
        PropertyDescriptor descriptors[] = null;
        
        // Introspect the bean and cache the generated descriptors
        BeanInfo beanInfo = null;
        try {
            beanInfo = Introspector.getBeanInfo(beanClass);
        } catch (IntrospectionException e) {
            return (new PropertyDescriptor[0]);
        }
        descriptors = beanInfo.getPropertyDescriptors();
        if (descriptors == null) {
            descriptors = new PropertyDescriptor[0];
        }
        
        return (descriptors);

    }
    
    /**
     * Set the value of the specified simple property of the specified bean,
     * with no type conversions.
     *
     * @param bean Bean whose property is to be modified
     * @param name Name of the property to be modified
     * @param value Value to which the property should be set
     *
     * @exception IllegalAccessException if the caller does not have
     *  access to the property accessor method
     * @exception IllegalArgumentException if <code>bean</code> or
     *  <code>name</code> is null
     * @exception IllegalArgumentException if the property name is
     *  nested or indexed
     * @exception InvocationTargetException if the property accessor method
     *  throws an exception
     * @exception NoSuchMethodException if an accessor method for this
     *  propety cannot be found
     */
    public static void setProperty(Object bean,
                                         String name, Object value)
            throws IllegalAccessException, InvocationTargetException,
            NoSuchMethodException {

        if (bean == null) {
            throw new IllegalArgumentException("No bean specified");
        }
        if (name == null) {
            throw new IllegalArgumentException("No name specified");
        }

        // Retrieve the property setter method for the specified property
        PropertyDescriptor descriptor =
                getPropertyDescriptor(bean, name);
        if (descriptor == null) {
            throw new NoSuchMethodException("Unknown property '" +
                    name + "'");
        }
        Method writeMethod = descriptor.getWriteMethod();
        if (writeMethod == null) {
            throw new NoSuchMethodException("Property '" + name +
                    "' has no setter method");
        }

        // Call the property setter method
        Object values[] = new Object[1];
        values[0] = value;
        
        invokeMethod(writeMethod, bean, values);

    }
    
    /**
     * This utility method just catches and wraps IllegalArgumentException.
     * @param method the method to call
     * @param bean the bean
     * @param values the values
     * @return the returned value of the method
     * @throws IllegalAccessException if an exception occurs
     * @throws InvocationTargetException if an exception occurs
     */
    private static Object invokeMethod(
                        Method method, 
                        Object bean, 
                        Object[] values) 
                            throws
                                IllegalAccessException,
                                InvocationTargetException {
        try {
            
            return method.invoke(bean, values);
        
        } catch (IllegalArgumentException e) {
            
            LOGGER.error("Method invocation failed.", e);
            throw new IllegalArgumentException(
                "Cannot invoke " + method.getDeclaringClass().getName() + "." 
                + method.getName() + " - " + e.getMessage());
            
        }
    }
}
