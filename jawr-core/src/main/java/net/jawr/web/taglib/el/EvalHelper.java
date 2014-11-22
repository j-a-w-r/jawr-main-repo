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
package net.jawr.web.taglib.el;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

import org.apache.taglibs.standard.lang.support.ExpressionEvaluatorManager;

/**
 * Evaluation helper utility class.
 * 
 * @author Ibrahim Chaehoi
 *
 */
public class EvalHelper {

	/**
     * Evaluate the string EL expression passed as parameter
     * @param propertyName the property name
     * @param propertyValue the property value
     * @param tag the tag
     * @param pageContext the page context
     * @return the value corresponding to the EL expression passed as parameter
     * @throws JspException if an exception occurs
     */
    public static String evalString(String propertyName, String propertyValue, Tag tag, PageContext pageContext) throws JspException{
    	
    	return (String) ExpressionEvaluatorManager.evaluate(propertyName,
    			propertyValue, String.class, tag, pageContext);
    }
    
    /**
     * Evaluate the boolean EL expression passed as parameter
     * @param propertyName the property name
     * @param propertyValue the property value
     * @param tag the tag
     * @param pageContext the page context
     * @return the value corresponding to the EL expression passed as parameter
     * @throws JspException if an exception occurs
     */
    public static Boolean evalBoolean(String propertyName, String propertyValue, Tag tag, PageContext pageContext) throws JspException{
    	
    	return (Boolean) ExpressionEvaluatorManager.evaluate(propertyName,
    			propertyValue, Boolean.class, tag, pageContext);
    }
}
