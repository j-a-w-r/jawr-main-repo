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
package net.jawr.web.resource.bundle.generator;

/**
 * This interface is implemented by all objects, which generates CSS resources.
 *  
 * @author Ibrahim Chaehoi
 *
 */
public interface CssResourceGenerator extends TextResourceGenerator {

	/**
	 * Returns true if the CSS image defined in the generated CSS resource
	 * are using the CSS generator.
	 * 
	 * If myGen is the prefix of the resource generator, all CSS image which are defined
	 * in the CSS resources, will be prefixed by myGen.
	 * 
	 * For example: if CSS image is referenced as
	 * 
	 * background-image : url('/myImg/temp/myIcon.png')
	 * 
	 * The image path will be interpreted as :
	 * 
	 * background-image : url('myGen:/myImg/temp/myIcon.png')
	 * 
	 * @return true if the CSS image defined in the generated CSS resource
	 * are using the CSS generator.
	 */
	public boolean isHandlingCssImage();
	
}
