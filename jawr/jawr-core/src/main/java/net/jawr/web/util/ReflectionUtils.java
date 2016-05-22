/**
 * Copyright 2016 Ibrahim Chaehoi
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

import java.lang.reflect.Method;

/**
 * The reflection utility class
 * 
 * @author Ibrahim Chaehoi
 */
public final class ReflectionUtils {

	/**
	 * Returns true if the method is in the method array.
	 * @param m the method
	 * @param methods the method array
	 * @return  true if the method is in the method array.
	 */
	public static boolean methodBelongsTo(Method m, Method[] methods){
		boolean result = false;
		
		for (int i = 0; i < methods.length && !result; i++) {
			if(methodEquals (methods [i], m)){
				result = true;
			}
		}
		
		return result;
	}
	
	/**
	 * Checks if the 2 methods are equals.
	 * 
	 * @param method the first method
	 * @param other the second method
	 * @return true if the 2 methods are equals
	 */
	public static boolean methodEquals( Method method, Method other){
	    
		if ((method.getDeclaringClass().equals( other.getDeclaringClass()))
	    && (method.getName().equals( other.getName()))) {
	        if (!method.getReturnType().equals(other.getReturnType()))
	            return false;
	        
	        Class<?>[] params1 = method.getParameterTypes();
	        Class<?>[] params2 = other.getParameterTypes();
	        if (params1.length == params2.length) {
	            for (int i = 0; i < params1.length; i++) {
	            if (params1[i] != params2[i])
	                return false;
	            }
	            return true;
	        }
	    }
	    return false;
	}

}
