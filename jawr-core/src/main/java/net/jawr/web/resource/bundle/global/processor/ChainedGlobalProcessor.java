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
package net.jawr.web.resource.bundle.global.processor;

/**
 * This interface is implemented by any object which can handle the chained global resource preprocessor.
 * 
 * @author Ibrahim Chaehoi
 * 
 */
public interface ChainedGlobalProcessor<T extends AbstractGlobalProcessingContext> extends GlobalProcessor<T> {

	/**
	 * Returns the ID of the chained post processor
	 * 
	 * @return the ID of the chained post processor
	 */
	public String getId();

	/**
	 * Add the next processor in the end of the chain.
	 * 
	 * @param nextProcessor
	 *            the next post processor
	 */
	public void addNextProcessor(ChainedGlobalProcessor<T> nextProcessor);

}
