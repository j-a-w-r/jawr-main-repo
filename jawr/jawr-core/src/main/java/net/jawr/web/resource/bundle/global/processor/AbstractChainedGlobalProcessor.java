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
package net.jawr.web.resource.bundle.global.processor;

/**
 * This class defines the abstract class for the preprocessor, which handle one
 * type of resource bundle.
 * 
 * @author Ibrahim Chaehoi
 */
public abstract class AbstractChainedGlobalProcessor<T extends AbstractGlobalProcessingContext>
		implements ChainedGlobalProcessor<T> {

	/** The ID of the resource type bundle processor */
	private final String id;

	/** The next processor */
	private ChainedGlobalProcessor<T> nextProcessor;

	/**
	 * Constructor
	 * 
	 * @param id
	 *            the ID of the processor
	 */
	public AbstractChainedGlobalProcessor(String id) {
		this.id = id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.global.processor.
	 * ChainedResourceTypeBundleProcessor#addNextProcessor(net.jawr.web.resource
	 * .bundle.global.processor.ChainedResourceTypeBundleProcessor)
	 */
	@Override
	public void addNextProcessor(ChainedGlobalProcessor<T> nextProcessor) {

		if (this.nextProcessor == null) {
			this.nextProcessor = nextProcessor;
		} else {
			this.nextProcessor.addNextProcessor(nextProcessor);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.global.processor.
	 * ChainedResourceTypeBundleProcessor#getId()
	 */
	@Override
	public String getId() {

		return this.id;
	}

}
