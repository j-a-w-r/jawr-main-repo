/**
 *    Copyright 2009 Ibrahim Chaehoi
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package net.jawr.web.bundle.processor.renderer;

/**
 * This class defines a rendered link.
 * 
 * @author Ibrahim Chaehoi
 *
 */
public class RenderedLink {
	
	/** The rendered link */
	public String link;
	
	/** The flag indicating if it a link for the debug mode or not */
	public boolean debugMode;

	/**
	 * Constructor
	 * 
	 * @param link the link
	 * @param debugMode the flag indicating if it a link for the debug mode or not
	 */
	public RenderedLink(String link, boolean debugMode) {
		super();
		this.link = link;
		this.debugMode = debugMode;
	}

	/**
	 * @return the link
	 */
	public String getLink() {
		return link;
	}

	/**
	 * @param link the link to set
	 */
	public void setLink(String link) {
		this.link = link;
	}

	/**
	 * @return the debugMode
	 */
	public boolean isDebugMode() {
		return debugMode;
	}

	/**
	 * @param debugMode the debugMode to set
	 */
	public void setDebugMode(boolean debugMode) {
		this.debugMode = debugMode;
	}

}