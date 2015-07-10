/**
 * Copyright 2008 Jordi Hernández Sellés
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
package net.jawr.web.resource.bundle.iterator;

/**
 * Interface for a type that is used as a callback to be invoked while iterating 
 * over a list of bundles. The callback will be invoked whenever a conditional 
 * comment should be started and finished while generating a list of paths to a bundle. 
 * 
 * @author Jordi Hernández Sellés
 */
public interface ConditionalCommentCallbackHandler {
	
	/**
	 * This method will be invoked to signal the need to start a conditional 
	 * comment, which will use the specified expression. 
	 * @param expression A string specifying the conditional comment expression to 
	 * use (such as 'if IE'). 
	 */
	public void openConditionalComment(String expression);
	
	/**
	 * This method will be invoked to signal the need to close a conditional 
	 * comment previously opened. 
	 */
	public void closeConditionalComment();
}
