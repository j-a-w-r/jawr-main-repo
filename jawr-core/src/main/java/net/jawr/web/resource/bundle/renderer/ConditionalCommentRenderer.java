/**
 * Copyright 2008-2012 Jordi Hernández Sellés, Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.renderer;

import java.io.IOException;
import java.io.Writer;

import net.jawr.web.exception.JawrLinkRenderingException;
import net.jawr.web.resource.bundle.iterator.ConditionalCommentCallbackHandler;

/**
 * Renderer for internet explorer conditional comments. Wraps script or stylesheet tags
 * with conditional comments for IE specific contents.  
 * 
 * @author Jordi Hernández Sellés
 * @author ibrahim Chaehoi
 */
public class ConditionalCommentRenderer implements ConditionalCommentCallbackHandler {
	

    private static final String PRE_TAG = "<!--[";
    private static final String POST_TAG = "]>\n";
    private static final String CLOSING_TAG = "<![endif]-->\n";
	private Writer out;
	

	
	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.iterator.ConditionalCommentCallbackHandler#openConditionalComment(java.lang.String)
	 */
	public void openConditionalComment(String expression) {
		StringBuffer sb = new StringBuffer(PRE_TAG);
		sb.append(expression).append(POST_TAG);
		try {
			out.write(sb.toString());
		} catch (IOException e) {
			throw new JawrLinkRenderingException(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.iterator.ConditionalCommentCallbackHandler#closeConditionalComment()
	 */
	public void closeConditionalComment() {
		try {
			out.write(CLOSING_TAG);
		} catch (IOException e) {
			throw new JawrLinkRenderingException(e);
		}
	}


	/**
	 * Create a renderer that writes conditional comments to the 
	 * specified writer. 
	 * @param out Writer
	 */
	public ConditionalCommentRenderer(Writer out) {
		super();
		this.out = out;
	}


}
