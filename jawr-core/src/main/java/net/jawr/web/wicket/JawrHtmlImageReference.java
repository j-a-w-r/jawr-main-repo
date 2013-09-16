/**
 * Copyright 2009-2013 Ibrahim Chaehoi
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
package net.jawr.web.wicket;


/**
 * This class defines the Jawr Html image reference for wicket
 * 
 * @author Ibrahim Chaehoi
 */
public class JawrHtmlImageReference extends AbstractJawrImageReference {

	/** The serial version UID */
	private static final long serialVersionUID = 3738894824289808984L;

	/**
	 * Constructor
	 * @param id the component ID
	 */
	public JawrHtmlImageReference(String id) {
		super(id);
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.wicket.AbstractJawrImageReference#isPlainImage()
	 */
	@Override
	protected boolean isPlainImage() {
		return true;
	}

}
