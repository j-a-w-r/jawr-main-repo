/**
 * Copyright 2014 Ibrahim Chaehoi
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
package net.jawr.application.controller;

import net.jawr.web.servlet.JawrSpringController;

/**
 * The abstract Jawr controller which loads the configuration from the jawr.properties file
 *  
 * @author ibrahim Chaehoi
 *
 */
public abstract class AbstractJawrController extends JawrSpringController {

	/**
	 * Constructor
	 */
	public AbstractJawrController(String type) {
		setConfigLocation("jawr.properties");
		setType(type);
		
	}
	
}
